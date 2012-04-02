/*
 * Copyright (C) 2012 Xavier Antoviaque <xavier@antoviaque.org>
 *
 * This software's license gives you freedom; you can copy, convey,
 * propagate, redistribute and/or modify this program under the terms of
 * the GNU Affero Gereral Public License (AGPL) as published by the Free
 * Software Foundation (FSF), either version 3 of the License, or (at your
 * option) any later version of the AGPL published by the FSF.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program in a file in the toplevel directory called
 * "AGPLv3".  If not, see <http://www.gnu.org/licenses/>.
*/

(function($) {

    $.forum = {}
    
    $.forum.auth = null;
    
    // Models //////////////////////////////////////////////////////////////
        
    $.forum.Message = Backbone.RelationalModel.extend({
        urlRoot: '/api/message',
        idAttribute: '_id',
    });

    $.forum.Thread = Backbone.RelationalModel.extend({
        urlRoot: '/api/thread',
        idAttribute: '_id',
        relations: [{
            type: Backbone.HasMany,
            key: 'messages',
            relatedModel: '$.forum.Message',
            reverseRelation: {
                key: 'thread',
                includeInJSON: '_id',
            },
        }]
    });
    
    $.forum.ThreadCollection = Backbone.Collection.extend({
        url: '/api/thread',
        model: $.forum.Thread,
    })
    
    $.forum.Auth = Backbone.RelationalModel.extend({
        url: '/api/auth',
    });

    // Viewer ///////////////////////////////////////////////////////////////
    
    // Allows to cleanly switch between views displayed in a specific area of the 
    // screen, as cleanly removing a view requires removing the view's DOM element
    $.forum.Viewer = function(dom_element) {
    	this.current_view = null;
    	this.dom_element = dom_element;
    	
    	this.show = function(view) {
    		if(this.current_view) {
    			this.current_view.close();
    		}
    		this.current_view = view;
    		this.current_view.render();
    		console.log(this.dom_element);
    		$(this.dom_element).html(this.current_view.el);
    	}
    }
    
    Backbone.View.prototype.close = function() {
        this.remove();
        this.unbind();
        if(this.on_close) {
            this.on_close();
        }
    }
    
    // Views ///////////////////////////////////////////////////////////////
    
    // Threads list //
    
    $.forum.ThreadListView = Backbone.View.extend({
        tagName: 'div',

        className: 'thread_list_view',
        
        initialize: function() {
            _.bindAll(this, 'render', 'render_thread_summary', 'on_submit', 'on_thread_created', 
            		         'on_error', 'on_close');
            $.forum.credentials.bind('reset', this.render); 
            $.forum.credentials.bind('change', this.render); 
            $.forum.credentials.bind('destroy', this.render); 
            this.model.bind('reset', this.render); 
            this.model.bind('change', this.render); 
            this.model.bind('add', this.render_thread_summary); 
        },
        
        on_close: function() {
            $.forum.credentials.unbind('reset', this.render); 
            $.forum.credentials.unbind('change', this.render); 
            $.forum.credentials.unbind('destroy', this.render); 
            this.model.unbind('reset', this.render); 
            this.model.unbind('change', this.render); 
            this.model.unbind('add', this.render_thread_summary);
        },
    
        template: Handlebars.compile($('#tpl_thread_list').html()),
    
        render: function() {
            $(this.el).html(this.template($.forum.credentials.toJSON()));
            this.model.forEach(this.render_thread_summary);
            return $(this.el).html();
        },
        
        render_thread_summary: function(thread) {
            var thread_summary_view = new $.forum.ThreadSummaryView({model: thread});
            this.$('ul.thread_list').prepend($(thread_summary_view.render()));
        },
        
        events: {
            'click .new_thread_submit': 'on_submit',
        },
        
        on_submit: function(e) {
            var thread = new $.forum.Thread({ title: this.$('.new_thread_title').val() });
            thread.save({}, { success: this.on_thread_created,
                              error: this.on_error });
        },
        
        on_thread_created: function(thread, response) {
            this.model.add(thread, {at: 0});
            var message = new $.forum.Message({ author: this.$('.new_message_author').val(),
                                                 text: this.$('.new_message_text').val(),
                                                 thread: thread.get('_id') });
            message.save({}, { 
                success: function() {
                    $.forum.app.navigate('thread/'+thread.get('_id')+'/', { trigger: true });
                },
                error: this.on_error,
            });
        },
                
        on_error: function(model, response) {
            var error = $.parseJSON(response.responseText);
            this.$('.error_message').html(error.message);
        },
    });
    
    // Thread //
    
    $.forum.ThreadSummaryView = Backbone.View.extend({
        tagName: 'li',

        className: 'thread_summary_view',
        
        initialize: function(){
            _.bindAll(this, 'render', 'on_click', 'on_close');
            this.model.bind('change', this.render);
        },

        on_close: function() {
            this.model.unbind('change', this.render);
        },
    
        template: Handlebars.compile($('#tpl_thread_summary').html()),
        
        render: function() {
            return $(this.el).html(this.template(this.model.toJSON()));
        },
        
        events: {
            'click': 'on_click',
        },
        
        on_click: function(e) {
            $.forum.app.navigate('thread/'+this.model.get('_id')+'/', {trigger: true});
        },
    });
    
    $.forum.ThreadView = Backbone.View.extend({
        tagName: 'div',

        className: 'thread_view',
        
        initialize: function(){
            _.bindAll(this, 'render', 'render_message', 'on_submit', 'on_close');
            this.model.bind('change', this.render);
            this.model.bind('reset', this.render);
            this.model.bind('add:messages', this.render_message); 
        },

        on_close: function() {
            this.model.unbind('change', this.render);
            this.model.unbind('reset', this.render);
            this.model.unbind('add:messages', this.render_message);
        },
    
        template: Handlebars.compile($('#tpl_thread').html()),
        
        render: function() {
            return $(this.el).html(this.template(this.model.toJSON()));
        },
        
        render_message: function(message) {
            var message_view = new $.forum.MessageView({model: message});
            this.$('div.message_list').append($(message_view.render()));
        },
        
        events: {
            'click .new_message_submit': 'on_submit',
        },
        
        on_submit: function(e) {
            var new_message = new $.forum.Message({author: this.$('.new_message_author').val(),
                                                    text: this.$('.new_message_text').val(),
                                                    thread: this.model});
            new_message.save();
        },
    });
    
    // Message //
    
    $.forum.MessageView = Backbone.View.extend({
        tagName: 'div',

        className: 'message_view',
        
        initialize: function() {
            _.bindAll(this, 'render', 'on_close');
            this.model.bind('change', this.render);
        },
        
        on_close: function() {
            this.model.unbind('change', this.render);
        },
    
        template: Handlebars.compile($('#tpl_message').html()),
        
        render: function() {
            return $(this.el).html(this.template(this.model.toJSON()));
        },
    });
    
    // Auth //
    
    $.forum.AuthView = Backbone.View.extend({
        tagName: 'div',

        className: 'auth_view',
        
        initialize: function() {
            _.bindAll(this, 'render', 'on_login', 'on_logout', 'on_keypress', 'on_close');
            this.model.bind('change', this.render);
            this.model.bind('reset', this.render);
        },
        
        on_close: function() {
            this.model.unbind('change', this.render);
            this.model.unbind('reset', this.render);
        },
    
        template_auth_user: Handlebars.compile($('#tpl_auth_user').html()),
        template_auth_anonymous: Handlebars.compile($('#tpl_auth_anonymous').html()),
        
        events: {
            'click .login_submit': 'on_login',
            'click .logout_submit': 'on_logout',
            'keypress input.login': 'on_keypress',
            'keypress input.password': 'on_keypress',
        },
        
        render: function() {
        	if(!this.model.get('login')) {
            	return $(this.el).html(this.template_auth_anonymous());
        	} else {
            	return $(this.el).html(this.template_auth_user(this.model.toJSON()));
        	}
        },
        
        on_keypress: function(e) {
            if(e.keyCode == 13) { // <ENTER>
            	this.on_login(e);
            }
         },
        
        on_login: function(e) {
            auth = this.model;
        	auth.save({login: this.$('.login').val(),
        			   password: this.$('.password').val()},
        			  {success: this.on_login_response});
        },
        
        on_login_failed: function() {
        	console.log('error'); // XXX FIXME
        	this.$('.login_info_message').html("Login failed");
        },
        
        on_logout: function(e) {
        	this.model.destroy({});
        	$(this.el).children().remove();
        	$.forum.app.auth_refresh();
        },
    });
    
    // Router ///////////////////////////////////////////////////////////////
    
    $.forum.Router = Backbone.Router.extend({
        routes: {
            "": "show_thread_list",
            "thread/:_id/": "show_thread",
            "refresh": "refresh",
        },
        
        initialize: function(options) {
        	this.viewers = { content: new $.forum.Viewer($('#content')),
        					  auth: new $.forum.Viewer($('#auth'))};
        },
    
        show_thread_list: function() {
            var thread_collection = new $.forum.ThreadCollection();
            var thread_list_view = new $.forum.ThreadListView({ model: thread_collection });
            thread_collection.fetch();
            this.viewers.content.show(thread_list_view);
        },
        
        show_thread: function(_id) {
            var thread = new $.forum.Thread({_id: _id});
            var thread_view = new $.forum.ThreadView({ model: thread });
            thread.fetch();
            this.viewers.content.show(thread_view);
        },
        
        auth: function() {
        	$.forum.credentials = new $.forum.Auth({});
        	var auth_view = new $.forum.AuthView({ model: $.forum.credentials });
        	$.forum.credentials.fetch();
            this.viewers.auth.show(auth_view);
        },
        
        auth_refresh: function() {
            this.auth();
            this.navigate("refresh", {trigger: true, replace: true});
        },
        
        refresh: function() {
        	this.navigate("", {trigger: true});
        },
    });
    
    
    // App /////////////////////////////////////////////////////////////////
    
    $.forum.app = null;
    
    $.forum.bootstrap = function() {
        $.forum.app = new $.forum.Router();
        $.forum.app.auth();
        Backbone.history.start({pushState: true});
    };

})(jQuery);

