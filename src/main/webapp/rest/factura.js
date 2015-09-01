/* 
 * Copyright (C) 2015 jlgranda
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

var app = {
    // Create this closure to contain the cached modules
    module: function () {
        // Internal module cache.
        var modules = {};
        
        // Create a new module reference scaffold or load an
        // existing module.
        return function (name) {
            // If this module has already been created, return it.
            if (modules[name]) {
                return modules[name];
            }
            
            // Create a module and save it under this name
            return modules[name] = {Views: {}};
        };
    }()
};

(function (models) {
    
// Model for FacturaElectronica entity
    models.FacturaElectronica = Backbone.Model.extend({
        urlRoot: "http://localhost:8080/fede/webresources/org.jlgranda.fede.model.document.facturaelectronica/",
        defaults: {
            claveAcceso: "",
            numeroAutorizacion: "",
            contenido: "",
            code: "",
            description: "",
            active: "",
            priority: "",
            version: "",
            uuid: "",
            deleted: "",
            filename: "",
            name: "",
            moneda: "",
            status: ""
        },
        toViewJson: function () {
            var result = this.toJSON(); // displayName property is used to render item in the list
            result.displayName = this.get('name');
            return result;
        },
        isNew: function () {
            // default isNew() method imlementation is
            // based on the 'id' initialization which
            // sometimes is required to be initialized.
            // So isNew() is rediefined here
            return this.notSynced;
        },
        sync: function (method, model, options) {
            options || (options = {});
            var errorHandler = {
                error: function (jqXHR, textStatus, errorThrown) {
                    // TODO: put your error handling code here
                    // If you use the JS client from the different domain
                    // (f.e. locally) then Cross-origin resource sharing 
                    // headers has to be set on the REST server side.
                    // Otherwise the JS client has to be copied into the
                    // some (f.e. the same) Web project on the same domain
                    alert('Unable to fulfil the request');
                }}
            
            if (method == 'create') {
                options.url = 'http://localhost:8080/fede/webresources/org.jlgranda.fede.model.document.facturaelectronica/';
            }
            var result = Backbone.sync(method, model, _.extend(options, errorHandler));
            return result;
        }
        
        
    });
    
    
    // Collection class for FacturaElectronica entities
    models.FacturaElectronicaCollection = Backbone.Collection.extend({
        model: models.FacturaElectronica,
        url: "http://localhost:8080/fede/webresources/org.jlgranda.fede.model.document.facturaelectronica/",
        sync: function (method, model, options) {
            options || (options = {});
            var errorHandler = {
                error: function (jqXHR, textStatus, errorThrown) {
                    // TODO: put your error handling code here
                    // If you use the JS client from the different domain
                    // (f.e. locally) then Cross-origin resource sharing 
                    // headers has to be set on the REST server side.
                    // Otherwise the JS client has to be copied into the
                    // some (f.e. the same) Web project on the same domain
                    alert('Unable to fulfil the request');
                }}
            
            var result = Backbone.sync(method, model, _.extend(options, errorHandler));
            return result;
        }
    });
    
    
})(app.module("models"));

(function (views) {
    
    views.ListView = Backbone.View.extend({
        tagName: 'tbody',
        initialize: function (options) {
            this.options = options || {};
            this.model.bind("reset", this.render, this);
            var self = this;
            this.model.bind("add", function (modelName) {
                var row = new views.ListItemView({
                    model: modelName,
                    templateName: self.options.templateName
                }).render().el;
                $(self.el).append($(row));
                $(self.el).parent().trigger('addRows', [$(row)]);
            });
        },
        render: function (eventName) {
            var self = this;
            _.each(this.model.models, function (modelName) {
                $(this.el).append(new views.ListItemView({
                    model: modelName,
                    templateName: self.options.templateName
                }).render().el);
            }, this);
            return this;
        }
    });
    
    views.ListItemView = Backbone.View.extend({
        tagName: 'tr',
        initialize: function (options) {
            this.options = options || {};
            this.model.bind("change", this.render, this);
            this.model.bind("destroy", this.close, this);
        },
        template: function (json) {
            /*
             *  templateName is element identifier in HTML
             *  $(this.options.templateName) is element access to the element
             *  using jQuery 
             */ 
            return _.template($(this.options.templateName).html())(json);
        },
        render: function (eventName) {
            $(this.el).html(this.template(this.model.toJSON()));
            return this;
        },
        close: function () {
            var table = $(this.el).parent().parent();
            table.trigger('disable.pager');
            $(this.el).unbind();
            $(this.el).remove();
            table.trigger('enable.pager');
        }
        
    });
    
    views.ModelView = Backbone.View.extend({
        initialize: function (options) {
            this.options = options || {};
            this.model.bind("change", this.render, this);
        },
        render: function (eventName) {
            $(this.el).html(this.template(this.model.toJSON()));
            return this;
        },
        template: function (json) {
            /*
             *  templateName is element identifier in HTML
             *  $(this.options.templateName) is element access to the element
             *  using jQuery 
             */
            return _.template($(this.options.templateName).html())(json);
        },
        /*
         *  Classes "save"  and "delete" are used on the HTML controls to listen events.
         *  So it is supposed that HTML has controls with these classes.
         */
        events: {
            "change input": "change",
            "click .save": "save",
            "click .delete": "drop"
        },
        change: function (event) {
            var target = event.target;
            console.log('changing ' + target.id + ' from: ' + target.defaultValue + ' to: ' + target.value);
        },
        save: function () {
            // TODO : put save code here
            var hash = this.options.getHashObject();
            this.model.set(hash);
            if (this.model.isNew() && this.collection) {
                var self = this;
                this.collection.create(this.model, {
                    success: function () {
                        // see isNew() method implementation in the model
                        self.model.notSynced = false;
                        self.options.navigate(self.model.id);
                    }
                });
            } else {
                this.model.save();
                this.model.el.parent().parent().trigger("update");
            }
            return false;
        },
        drop: function () {
            this.model.destroy({
                success: function () {
                    /*
                     *  TODO : put your code here
                     *  f.e. alert("Model is successfully deleted");
                     */  
                    window.history.back();
                }
            });
            return false;
        },
        close: function () {
            $(this.el).unbind();
            $(this.el).empty();
        }
    });
    
    // This view is used to create new model element
    views.CreateView = Backbone.View.extend({
        initialize: function (options) {
            this.options = options || {};
            this.render();  
        },
        render: function (eventName) {
            $(this.el).html(this.template());
            return this;
        },
        template: function (json) {
            /*
             *  templateName is element identifier in HTML
             *  $(this.options.templateName) is element access to the element
             *  using jQuery 
             */
            return _.template($(this.options.templateName).html())(json);
        },
        /*
         *  Class "new" is used on the control to listen events.
         *  So it is supposed that HTML has a control with "new" class.
         */
        events: {
            "click .new": "create"
        },
        create: function (event) {
            this.options.navigate();
            return false;
        }
    });
    
})(app.module("views"));


$(function () {
    var models = app.module("models");
    var views = app.module("views");
    
    var AppRouter = Backbone.Router.extend({
        routes: {
            '': 'list',
            'new': 'create'
            ,
            ':id': 'details'
        },
        initialize: function () {
            var self = this;
            $('#create').html(new views.CreateView({
                // tpl-create is template identifier for 'create' block
                templateName: '#tpl-create',
                navigate: function () {
                    self.navigate('new', true);
                }
            }).render().el);
        },
        list: function () {
            this.collection = new models.FacturaElectronicaCollection();
            var self = this;
            this.collection.fetch({
                success: function () {
                    self.listView = new views.ListView({
                        model: self.collection,
                        // tpl-facturaelectronica-list-itemis template identifier for item
                        templateName: '#tpl-facturaelectronica-list-item'
                    });
                    $('#datatable').html(self.listView.render().el).append(_.template($('#thead').html())());
                    if (self.requestedId) {
                        self.details(self.requestedId);
                    }
                    var pagerOptions = {
                        // target the pager markup 
                        container: $('.pager'),
                        // output string - default is '{page}/{totalPages}'; possiblevariables: {page}, {totalPages},{startRow}, {endRow} and {totalRows}
                        output: '{startRow} to {endRow} ({totalRows})',
                        // starting page of the pager (zero based index)
                        page: 0,
                        // Number of visible rows - default is 10
                        size: 10
                    };
                    $('#datatable').tablesorter({widthFixed: true,
                        widgets: ['zebra']}).
                            tablesorterPager(pagerOptions);
                }
            });
        },
        details: function (id) {
            if (this.collection) {
                this.facturaelectronica = this.collection.get(id);
                if (this.view) {
                    this.view.close();
                }
                var self = this;
                this.view = new views.ModelView({
                    model: this.facturaelectronica,
                    // tpl-facturaelectronica-details is template identifier for chosen model element
                    templateName: '#tpl-facturaelectronica-details',
                    getHashObject: function () {
                        return self.getData();
                    }
                });
                $('#details').html(this.view.render().el);
            } else {
                this.requestedId = id;
                this.list();
            }
        },
        create: function () {
            if (this.view) {
                this.view.close();
            }
            var self = this;
            var dataModel = new models.FacturaElectronica();
            // see isNew() method implementation in the model
            dataModel.notSynced = true;
            this.view = new views.ModelView({
                model: dataModel,
                collection: this.collection,
                // tpl-facturaelectronica-details is a template identifier for chosen model element
                templateName: '#tpl-facturaelectronica-details',
                navigate: function (id) {
                    self.navigate(id, false);
                },
                getHashObject: function () {
                    return self.getData();
                }
            });
            $('#details').html(this.view.render().el);
        },
        getData: function () {
            return {
                claveAcceso: $('#claveAcceso').val(),
                numeroAutorizacion: $('#numeroAutorizacion').val(),
                contenido: $('#contenido').val(),
                code: $('#code').val(),
                description: $('#description').val(),
                active: $('#active').val(),
                priority: $('#priority').val(),
                version: $('#version').val(),
                uuid: $('#uuid').val(),
                deleted: $('#deleted').val(),
                filename: $('#filename').val(),
                name: $('#name').val(),
                moneda: $('#moneda').val(),
                status: $('#status').val()
            };
        }
    });
    new AppRouter();
    
    
    Backbone.history.start();
});
