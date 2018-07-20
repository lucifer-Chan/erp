;(function ($, window, document, undefined) {

    //0-全局变量
    var global = window.GLOBALS ||
        (window.GLOBALS = {
            ctxPath: '/',//生产环境可能需要替换TODO
            debug: false
        });
    //如果不是debug模式，取消打印
    global.debug && (function () {
        window.console = {
            log: function () {}
            , error: function () {}
        }
    })();

    //序列化form
    $.prototype.serializeObject = function(){
        var obj= {};
        $.each(this.serializeArray(),function(index,param){
            if(!(param.name in obj)){
                obj[param.name]=param.value;
            }
        });
        return obj;
    };

    /**
     * 定制的promise
     * @param resolver
     * @constructor
     */
    function Promise(resolver) {
        if (!(this instanceof Promise)) {
            throw new TypeError(this + ' is not a promise(…)');
        }
        if (typeof resolver !== 'function') {
            throw new TypeError('Promise resolver ' + resolver + ' is not a function(…)');
        }

        var self = this;
        /** pending = 0, fulfilled = 1, rejected = -1 */
        var state = 0;
        var result = undefined;
        var deferreds = [];
        this.then = then;
        this.catch = function (onRejected) {
            return then(undefined, onRejected);
        };
        try {
            resolver.call(self, fulfill, reject);
        } catch (e) {
            reject(e);
        }

        function fulfill(value) {
            if (state === 0) {
                state = 1;
                result = value;
                tryCallback();
            }
        }

        function reject(reason) {
            if (state === 0) {
                state = -1;
                result = reason;
                tryCallback();
            }
        }

        function tryCallback() {
            var handlerName = null;
            var nextActionName = null;
            if (state === 1) {
                handlerName = 'onFulfilled';
                nextActionName = 'nextFulfill';
            } else if (state === -1) {
                handlerName = 'onRejected';
                nextActionName = 'nextReject';
            }
            if (!handlerName) {
                return;
            }
            var deferred;
            while (deferred = deferreds.shift()) {
                var handler = deferred[handlerName];
                if (typeof handler !== 'function') {
                    deferred[nextActionName](result);
                    continue;
                }
                var nextRes = undefined;
                try {
                    nextRes = handler.call(self, result);
                } catch (e) {
                    deferred.nextReject(e);
                }
                if (nextRes instanceof Promise) {
                    nextRes.then(deferred.nextFulfill, deferred.nextReject);
                } else {
                    deferred.nextFulfill(nextRes);
                }
            }
        }

        function then(onFulfilled, onRejected) {
            if (typeof onFulfilled !== 'function' && typeof onRejected !== 'function') {
                return self;
            }
            var deferred = {};
            deferred.promise = new Promise(function (fulfill, reject) {
                deferred.onFulfilled = onFulfilled;
                deferred.onRejected = onRejected;
                deferred.nextFulfill = fulfill;
                deferred.nextReject = reject;
            });
            deferreds.push(deferred);
            tryCallback();
            return deferred.promise;
        }
    }

    function all(iterable) {
        var deferred = {};
        deferred.promise = new Promise(function (fulfill, reject) {
            deferred.fulfill = fulfill;
            deferred.reject = reject;
        });
        var states = new Array(iterable.length);
        var results = new Array(iterable.length);
        iterable.forEach(function (item, idx) {
            if (item instanceof Promise) {
                states[idx] = 0;
                item.then(function (value) {
                    states[idx] = 1;
                    results[idx] = value;
                    tryFulfillAll();
                }, function (reason) {
                    deferred.reject(reason);
                });
            } else {
                states[idx] = 1;
                results[idx] = item;
            }
        });
        tryFulfillAll();

        function tryFulfillAll() {
            for (var i = 0; i < states.length; i++) {
                if (states[i] !== 1) {
                    return;
                }
            }
            deferred.fulfill(results);
        }

        return deferred.promise;
    }

    function race(iterable) {
        var deferred = {};
        deferred.promise = new Promise(function (fulfill, reject) {
            deferred.fulfill = fulfill;
            deferred.reject = reject;
        });
        iterable.forEach(function (item, idx) {
            if (item instanceof Promise) {
                item.then(function (value) {
                    deferred.fulfill(value);
                }, function (reason) {
                    deferred.reject(reason);
                });
            } else {
                deferred.fulfill(item);
            }
        });
        return deferred.promise;
    }

    function resolve(value) {
        return new Promise(function (fulfill, reject) {
            fulfill(value);
        });
    }

    function reject(reason) {
        return new Promise(function (fulfill, reject) {
            reject(reason);
        });
    }

    Promise.all = all;
    Promise.race = race;
    Promise.resolve = resolve;
    Promise.reject = reject;

    function _to(value) {
        return typeof value === 'object' ? JSON.stringify(value) : value;
    }

    function _from(value) {
        try {
            return $.parseJSON(value);
        } catch (e){
            return value;
        }
    }


    //1-扩展基本方法
    $.extend({
        isObject : function(val) {
            return val != null && typeof val === 'object' && Array.isArray(val) === false;
        },
        Promise: Promise,
        /**
         * sessionStorage
         * @param name
         * @param value
         * @returns {object | string | null}
         */
        session: function (name, value) {
            if (!window.sessionStorage)
                layer && layer.msg('您的浏览器版本过低，暂不支持会话存储，建议使用Chrome浏览器');
            else if(name === undefined)
                window.sessionStorage.clear();
            else if (value === undefined)
                return _from(window.sessionStorage.getItem(name));
            else if (value === null)
                window.sessionStorage.removeItem(name);
            else
                window.sessionStorage.setItem(name, _to(value));
        },

        /**
         *
         * @param name
         * @param value
         * @param days
         * @returns {any}
         */
        cookie: function (name, value, days) {
            if (value === undefined) {
                //get $.cookie(key);
                var array = document.cookie.match(new RegExp("(^| )" + name + "=([^;]*)(;|$)"));
                return array != null ? decodeURIComponent(array[2]) : null;
            } else if (value === null) {
                //remove  $.cookie(key, null);
                var expires = new Date();
                expires.setTime(expires.getTime() - 1);
                var _value = this.cookie(name);
                if (_value) document.cookie = name + "=" + _value + ";expires=" + expires.toUTCString();
            } else {
                //set $.cookie(key, value) $.cookie(key, value, 10) 默认30天有效
                days = typeof days === 'number' ? days : 30;
                var expires = new Date();
                expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
                document.cookie = name + "=" + encodeURIComponent(value) + ";expires=" + expires.toUTCString();
            }
        },

        /**
         * localStorage
         * @param name
         * @param value
         * @returns {object |string | null}
         */
        local: function (name, value) {
            if (!window.localStorage)
                layer && layer.msg('您的浏览器版本过低，暂不支持本地存储，建议使用Chrome浏览器');
            else if(name === undefined)
                window.localStorage.clear();
            else if (value === undefined)
                return _from(window.localStorage.getItem(name));
            else if (value === null)
                window.localStorage.removeItem(name);
            else{
                window.localStorage.setItem(name, _to(value));
                return value;
            }
        },

        //判断访问终端
        browser: {
            version: function () {
                var u = navigator.userAgent;
                return {
                    trident: u.indexOf('Trident') > -1, //IE内核
                    presto: u.indexOf('Presto') > -1, //opera内核
                    webKit: u.indexOf('AppleWebKit') > -1, //苹果、谷歌内核
                    gecko: u.indexOf('Gecko') > -1 && u.indexOf('KHTML') === -1,//火狐内核
                    mobile: !!u.match(/AppleWebKit.*Mobile.*/), //是否为移动终端
                    ios: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), //ios终端
                    android: u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, //android终端或者uc浏览器
                    iPhone: u.indexOf('iPhone') > -1, //是否为iPhone或者QQHD浏览器
                    iPad: u.indexOf('iPad') > -1, //是否iPad
                    webApp: u.indexOf('Safari') === -1, //是否web应该程序，没有头部与底部
                    weixin: u.indexOf('MicroMessenger') > -1, //是否微信 （2015-01-22新增）
                    qq: u.match(/\sQQ/i) === " qq" //是否QQ
                };
            }(),

            language: (navigator.browserLanguage || navigator.language).toLowerCase(),

            isInternetExplorer : function () {
                var b_version = navigator.appVersion;
                var version = b_version.split(";");
                if (version.length > 1) {
                    var trim_Version = parseInt(version[1].replace(/[ ]/g, "").replace(/MSIE/g, ""));
                    if (trim_Version < 11) {
                        //layer.msg('当前浏览器版本过低，建议您升级至ie11或以上版本',{maxWidth:'400px',time:24*60*60*1000,shade:[0.4 ,'#000',true]});
                        var oDiv = $('<div>');
                        oDiv.html('当前浏览器版本过低，建议您升级至IE11或以上版本');
                        oDiv.addClass('ie-low');
                        $('body').prepend(oDiv).addClass('is-ie');
                        return false;
                    }
                }
            }
        },
        //常用contentType TODO 待增加
        contentType: {
            urlencoded: 'application/x-www-form-urlencoded'
            , json: 'application/json'
        }
    });

    /**
     * 构造ajax的选项数据
     * @param opts
     * @returns {object}
     */
    var buildOptions = function (opts) {
        var data = opts.data || {};
        opts.type = opts.type || 'GET';
        opts.contentType = opts.contentType || $.contentType.urlencoded;
        opts.dataType = opts.dataType || 'json';
        opts.url = global.ctxPath + opts.url;
        if (opts.contentType === $.contentType.json)
            opts.data = JSON.stringify(opts.data);
        console.log('http请求[' + opts.url + ']', {method: opts.type, params: data, contentType: opts.contentType});
        return opts;
    };

    /**
     * 封装http请求
     * @param opts
     * @returns {*|PromiseLike<T>|Promise<T>}
     */
    var http = function (opts) {
        var settings = $.extend({}, buildOptions(opts));
        var $http = function (opts) {
            opts = opts || {};
            return new $.Promise(function (resolve, reject) {
                opts.success = resolve;
                opts.error = reject;
                $.ajax.call($, opts);
            })
        };

        return $http(settings).then(function (br) {
                if(settings.dataType === 'html'){
                    return br;
                } else if (!br) {
                    console.error('http-系统错误[' + settings.url + ']', br);
                    return $.Promise.reject({
                        //message : '未知错误',
                        caught: false,
                        error: '调用' + settings.url + '[' + settings.type + ']失败',
                        detail: br
                    });
                } else if (br.errcode === 0) {
                    console.log('http返回[' + settings.url + ']', {
                        method: settings.type,
                        returns: br.ret || {},
                        contentType: settings.contentType
                    });
                    return $.extend({}, br.ret, {_message : br.errmsg});
                } else if(br.errcode === 999){
                    console.error('http-会话丢失[' + settings.url + ']', br);
                    window.location.href = global.ctxPath + 'login.html';
                } else {
                    console.error('http-返回错误[' + settings.url + ']', br);

                    return $.Promise.reject({
                        message: (br.errmsg && br.errmsg.length > 20) ? '请求失败' : br.errmsg,
                        caught: br.errmsg && br.errmsg.length <= 20,
                        error: '调用' + settings.url + '[' + settings.type + ']返回错误，错误码：[' + br.errcode + ']',
                        detail: br
                    });
                }
            }
            , function (reason) {
                if((reason.responseText || '').indexOf(' <div class="title"><a href="/index.html">') > -1){
                    window.location.href = global.ctxPath + 'login.html';
                }

                var json = reason.responseJSON || {};
                console.error('http-返回错误[' + settings.url + ']', json);
                return $.Promise.reject({
                    error: '调用' + settings.url + '[' + settings.type + ']失败',
                    detail: json
                });
            }
        )
    };

    var _post = function (opts, type) {
        if (typeof opts === 'string')
            opts = {url: opts};
        opts.type = type;
        opts.contentType = opts.contentType || $.contentType.urlencoded;//$.contentType.json;
        opts.dataType = opts.dataType || 'json';
        return http(opts);
    };

    $.extend({
        //2-扩展ajax方法
        http: {
            get: function (opts) {
                return typeof opts === 'string' ? http({url: opts}) : http(opts);
            },

            post: function (opts) {
                return _post(opts, 'POST');
            },

            delete: function (opts) {
                return _post(opts, 'DELETE');
            },

            put: function (opts) {
                return _post(opts, 'PUT');
            },

            patch: function (opts) {
                return _post(opts, 'PATCH');
            }
        }
    });

    if (typeof define === "function" && define.amd) {
        define("jquery", [], function () {
            return $;
        });
    }
})(jQuery, window, document);