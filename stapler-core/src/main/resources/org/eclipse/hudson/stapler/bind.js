/*******************************************************************************
 *
 * Copyright (c) 2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   Kohsuke Kawaguchi
 *
 *******************************************************************************/
// // included into the page as an adjunct
// @include org.eclipse.hudson.stapler.framework.prototype.prototype

function makeStaplerProxy(url,crumb,methods) {
    if (!url.endsWith('/')) url+='/';
    var proxy = {};

    methods.each(function(methodName) {
        proxy[methodName] = function() {
            var args = arguments;

            // the final argument can be a callback that receives the return value
            var callback = (function(){
                if (args.length==0) return null;
                var tail = args[args.length-1];
                return (typeof(tail)=='function') ? tail : null;
            })();

            // 'arguments' is not an array so we convert it into an array
            var a = [];
            for (var i=0; i<args.length-(callback!=null?1:0); i++)
                a.push(args[i]);

            new Ajax.Request(url+methodName, {
                method: 'post',
                requestHeaders: {'Content-type':'application/x-stapler-method-invocation;charset=UTF-8','Crumb':crumb},
                postBody: a.toJSON(),
                onSuccess: function(t) {
                    if (callback!=null) {
                        t.responseObject = function() {
                            return eval('('+this.responseText+')');
                        }
                        callback(t);
                    }
                }
            });
        }
    });

    return proxy;
}