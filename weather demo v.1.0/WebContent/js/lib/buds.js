jQuery.ajaxSettings.traditional = true;

/* Chinese initialisation for the jQuery UI date picker plugin. */
/* Written by Ressol (ressol@gmail.com). */
jQuery(function($){
	$.datepicker.regional['zh-TW'] = {
		closeText: '關閉',
		prevText: '&#x3C;上月',
		nextText: '下月&#x3E;',
		currentText: '今天',
		monthNames: ['一月','二月','三月','四月','五月','六月','七月','八月','九月','十月','十一月','十二月'],
		monthNamesShort: ['一月','二月','三月','四月','五月','六月','七月','八月','九月','十月','十一月','十二月'],
		dayNames: ['星期日','星期一','星期二','星期三','星期四','星期五','星期六'],
		dayNamesShort: ['周日','周一','周二','周三','周四','周五','周六'],
		dayNamesMin: ['日','一','二','三','四','五','六'],
		weekHeader: '周',
		dateFormat: 'yy-mm-dd',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: true,
		yearSuffix: '年',
		//changeYear: true,
		changeMonth: true
	};
	$.datepicker.setDefaults($.datepicker.regional['zh-TW']);
});

(function() {
var NativeFields = z = 'windowzdocumentzObjectzNumberzStringzArrayzDatezMathzjQueryz_zBackbone'.split(/z/g);
z.push(window);
var win = NativeFields[z.length-1][z[0]],
returnDocument = function() {
	return win[NativeFields[1]];
},
returnObject = function() {
	return win[NativeFields[2]];
},
returnNumber = function() {
	return win[NativeFields[3]];
},
returnString = function() {
	return win[NativeFields[4]];
},
returnArray = function() {
	return win[NativeFields[5]];
},
returnDate = function() {
	return win[NativeFields[6]];
},
returnMath = function() {
	return win[NativeFields[7]];
},
returnJQuery = function() {
	return win[NativeFields[8]];
},
returnUnderscore = function() {
	return win[NativeFields[9]];
},
returnBackbone = function() {
	return win[NativeFields[10]];
};
var ARGS = [
	function() {
		var KEY = [
				'prototype',		// 0
				'LONG_ZERO',		// 1
				'DAYS_TEXT',		// 2
				'HTML_COMMENT_TAG',	// 3
				'FORMAT_ARGUMENT',		// 4
				'FORMAT_ARGUMENT_EXT',	// 5
				'EMAIL_PATTERN'		// 6
			],
			root = this,
			a = b = c = arguments,
			constants,
			util,
			pattern,
			random,
			template,
			f,
			params,
			forms,
			buds,
			strProto,
			dateProto;
		var document = a[0],
			String = a[3],
			Math = a[6],
			Backbone = a[9],
			backboneProto;
		var Object = b[1],
			Array = b[4],
			jQuery = $ = b[7];
		var Number = c[2],
			Date = c[5],
			_ = c[8];
		
		constants = {
			LONG_ZERO: '0000000000',	//KEY#1
			DAYS_TEXT: ['前天','昨天','今天','明天','後天']	//KEY#2
		};
		util =(function() {
			var stringFromCharCode = String.fromCharCode,
				entityEscapeMap = {
			      '&amp;': '&',
			      '&lt;': '<',
			      '&gt;': '>',
			      '&quot;': '"'
			    };
			return {
				length: function(data) {
					if (!data) return 0;
					if (_.isNumber(data)) return data;
					if (_.isArray(data)) return data.length;
					if (_.isString(data)) return data.length;
					if (_.isFunction(data)) return (data() || 0);
					return (new String(data)).length;
				},
				urlencode: function(str) {
					return encodeURIComponent(str);
				},
				urldecode: function(str) {
					return decodeURIComponent(str);
				},
				escape: function(str) {
					str = (str).toString();
					var change = false;
					var tmp = '';
					for (var i = 0, len = str.length; i < len; ++i) {
						var c = str.charAt(i);
						switch (c) {
							case '\\':
							case '\'':
							case '\"':
							case '\n':
							case '\r':
							case '\t':
							case '\f':
							case '<':
							case '>':
								change = true;
								tmp += '&#' + c.charCodeAt(0) + ';';
								break;
							default:
								tmp += c;
						}
					}
					return change? tmp : str;
				},
				unescape: function(str) {
					str = String(str);
					return str
						.replace(/\&\#(\d+);/gim, function() {
							var args = arguments;
							return stringFromCharCode(parseInt(args[1], 10));
						})
						.replace(/\&\#x([\da-fA-F]+);/gim, function() {
							var args = arguments;
							return stringFromCharCode(parseInt(args[1], 16));
						})
						.replace(/\&(amp|lt|gt|quot);/gim, function() {
							var args = arguments;
							return entityEscapeMap[args[0]] || args[0];
						})
					;
				},
				'left-zero': function(n, len) {
					n = String(n);
					if (n.length >= len) return n;
					var z = constants[KEY[1]];
					for (var left = len - n.length; left >= z.length; left = len - n.length) {
						n = z + n;
					}
					n = z.substring(0, left) + n;
					return n;
				},
				join: function(array) {
					if (_.isArray(array)) return array.join();
					if (_.isObject(array)) return _.values(array).join();
					if (_.isFunction(array)) return this.join(array());
					return array;
				},
				datetime: function(ts) {
					return ts&&ts>0? new Date(ts).toString('yyyy-MM-dd HH:mm:ss') : '';
				},
				date: function(ts) {
					return ts&&ts>0? new Date(ts).toString('yyyy-MM-dd') : '';
				},
				days: function(ts) {
					if (!ts || ts < 0) return '';
					var date = new Date(ts);
					var today = Date.today();
					var n = parseInt((date.getTime()-today.getTime())/86400000);
					if (0 == n) {
						return constants[KEY[2]][2];
					}
					else if (1 == n) {
						return constants[KEY[2]][3];
					}
					else if (2 == n) {
						return constants[KEY[2]][3];
					}
					else if (-1 == n) {
						return constants[KEY[2]][1];
					}
					else if (-2 == n) {
						return constants[KEY[2]][0];
					}
					return Math.abs(n)+'天'+(0>n? '前' : '後');
					//return date.toString('yyyy-MM-dd');
				},
				days_date: function(ts) {
					if (!ts || ts < 0) return '';
					var date = new Date(ts);
					var today = Date.today();
					var n = parseInt((date.getTime()-today.getTime())/86400000);
					if (0 == n) {
						return constants[KEY[2]][2];
					}
					else if (1 == n) {
						return constants[KEY[2]][3];
					}
					else if (2 == n) {
						return constants[KEY[2]][3];
					}
					else if (-1 == n) {
						return constants[KEY[2]][1];
					}
					else if (-2 == n) {
						return constants[KEY[2]][0];
					}
					return date.toString('yyyy-MM-dd');
				},
				time: function(ts) {
					return ts&&ts>0? new Date(ts).toString('HH:mm:ss') : '';
				},
				'default': function(value, default_value) {
					default_value = default_value || null;
					if (typeof(value) == 'undefined' || null == value) return default_value;
					else if ('[object Array]' == value.toString()) return value.join();
					return value;
				},
				isEmail: function(str) {
					return pattern[KEY[6]].test(str);
				},
				stringBefore: function(str, till) {
					var idx = str.indexOf(till);
					return -1==idx? str : str.substring(0, idx);
				},
				stringAfter: function(str, since) {
					var idx = str.indexOf(since);
					return -1==idx? '' : str.substring(idx);
				},
				isNumber: function(str, radix) {
					radix = radix || 10;
					var s = String(str);
					return !isNaN(parseInt(s, radix));
				},
				expand: function(obj, sources) {
					for (var p in sources) {
						var source = sources[p];
						for (var k in source) {
							obj[p+'.'+k] = source[k];
						}
					}
					return obj;
				},
				renameKeys: function(source, map) {
					var ren = _.isFunction(map)? map : function(k) {
						return map[k] || k;
					};
					var newly = {};
					for (var key in source) {
						var rename = ren(key);
						newly[rename] = source[key];
					}
					return newly;
				}
			};
		})();
		
		pattern = (function() {
			return {
				HTML_COMMENT_TAG:		/\<\!\-\-|\-\-\>/gm,	//KEY#3
				FORMAT_ARGUMENT:		/(#[HU]?\{([\.\-\w\:\_]+)(,\s*([\w\-]+)\s*)?\})/gm,	//KEY#4
				FORMAT_ARGUMENT_EXT:	/(#[HU]?\{([\.\-\w\:\_]+)(,\s*([\w\-]+)\s*((,\s*\w+\s*)+)*)?\})/gm,	//KEY#5
				EMAIL_PATTERN:			/^[a-zA-Z0-9][\w\.\-]*[a-zA-Z0-9]@[a-zA-Z0-9][\w\.\-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$/	// KEY#6
			};
		})();
		
		random = (function() {
			var rand = this.random,
				floor = this.floor,
				sqrt = this.sqrt,
				log = this.log,
				randBoolean = function() {
					return (2*rand() < 1);
				},
				randInt = function(bound) {
					bound = bound<0? -bound : bound;
					return floor(bound * rand());
				},
				haveNextNextGaussian = false,
				nextNextGaussian = null;
			return {
				nextBoolean: randBoolean,
				nextFloat: rand,
				nextInt: randInt,
				nextGaussian: function() {
					if (haveNextNextGaussian) {
						haveNextNextGaussian = false;
						return nextNextGaussian;
					}
					else {
						var v1, v2, s;
						do {
							v1 = 2 * rand() - 1;
							v2 = 2 * rand() - 1;
							s = v1 * v2 + v2 * v2;
						} while (s >= 1 || 0 == s);
						var multiplier = sqrt(-2 * log(s)/s);
						nextNextGaussian = v2 * multiplier;
						haveNextNextGaussian  = true;
						return v1 * multiplier; 
					}
				},
				flip: randBoolean,
				between: function(since, till, exclusive) {
					exclusive = 'undfined'==typeof exclusive? false : exclusive;
					var off = till - since;
					if (!exclusive) {
						++off;
					}
					return since + randInt(off);
				},
				chance: function(percent) {
					var n = randInt(100);
					return n < percent;
				},
				lottery: function(bag) {
					if (_.isObject(bag)) {
						var keys = _.keys(bag);
						var key = keys[randInt(keys.length)];
						return bag[key];
					}
					else {
						return bag[randInt(bag.length)];
					}
				}
			};
		}).call(Math);
	
		buds = this.buds = (function() {
			var _env = function() {
				//      0                 1               2               3               4              5             6             7               8                   9
				return [returnDocument(), returnObject(), returnNumber(), returnString(), returnArray(), returnDate(), returnMath(), returnJQuery(), returnUnderscore(), returnBackbone()];
			};
			return {
				Constants: constants,
				Util: util,
				Pattern: pattern,
				Random: random,
				getEnv: _env
			};
		})();
		
		f = (function() {
			return this.F = {
				noop: function() {},
				negative: function() {
					return false;
				},
				positive: function() {
					return true;
				}
			};
		}).call(buds);
		
		template = (function() {
			return this.Template = function(t, h) {
				var _template = String(t).trim();
				var _helper = $.extend({}, util, h || {});
				this.inflate = function(data, ext_helper) {
					//var tmp = _template.format(data, _.extend({}, _helper, ext_helper));
					var helper = ext_helper? _helper : _.extend({}, _helper, ext_helper);
					var tmp = _template.format(data, helper);
					return tmp;
				};
				this.getTemplate = function() {
					return _template;
				};
			};
		}).call(buds);
	
//		buds = this.buds = (function() {
//			var _env = function() {
//				return [returnDocument(), returnObject(), returnNumber(), returnString(), returnArray(), returnDate(), returnMath(), returnJQuery(), returnUnderscore(), returnBackbone()];
//			};
//			return {
//				Constants: constants,
//				Util: util,
//				Pattern: pattern,
//				Random: random,
//				F: f,
//				getEnv: _env
//			};
//		})();
		
		
		(function() {
			var _s = this;
			return this;
		}).call(String);
		
		strProto = (function() {
			
			function trim() {
				return this.replace(/^\s+|\s+$/g, '');
			};
			if (!this.trim) {
				this.trim = trim;
			}
			
			function quote(b, e, esc) {
				b = b || '"';
				e = e || b;
				return b + (esc? esc(this) : this) + e;
			};
			if (!this.quote) {
				this.quote = quote;
			}
			
			function format(args, helpers) {
				var p = pattern[KEY[5]];
				if (p.test(this)) {
					//helpers = $.extend({}, util, helpers || {});
					helpers = helpers || util;
					if (!helpers['default']) {
						helpers['default'] = util['default'];
					}
					return this.replace(p, function() {
						var a = arguments;
						var _helper_name = a[4] || 'default';
						var _helper = helpers[_helper_name] || helpers['default'];
						var _helper_args = (a[5] || '').split(/\s*,\s*/);
						_helper_args[0] = args[ a[2] ];
						_helper_args.push(a[2]);
						_helper_args.push(args);
						//return _helper.apply(this, _helper_args);
						var s = _helper.apply(this, _helper_args);
						switch (a[0].charAt(1)) {
							case 'H':
								s = util.escape(s);
								break;
							case 'U':
								s = util.urlencode(s);
								break;
						}
						return s;
					});
				}
				// no format match
				return this.toString();
			};
			this.format = format;
			
			return this;
		}).call(String[KEY[0]]);
		
		(function() {
			var _d = this;
			
			function parseWithTimezone(str, offset, format) {
				offset = -6 * Number(offset) / 10;	// in min like Date.getTimezoneOffset() form
				var d = parse(str, format);
				var corrected = new _d( d.getTime() - 60000*( d.getTimezoneOffset() - offset ) );
				return corrected;
			};
			this.parseText = parseWithTimezone;
			
			return this;
		}).call(Date);
		
		dateProto = (function() {
			var parse = Date.parseExact;
			
			
			function toStringWithTimezone(offset, format) {
				offset = -6 * Number(offset) / 10;	// in min like Date.getTimezoneOffset() form
				var date = new _date( this.getTime() + 60000*( this.getTimezoneOffset() - offset ) );
				return date.toString(format);
			};
			this.toText = toStringWithTimezone;
			
			return this;
		}).call(Date[KEY[0]]);
		
		forms = (function() {
			var
				_serialize = function(form) {
					var data = {};
					for (var i = 0, len = form.elements.length; i < len; ++i) {
						var ele = form.elements[i];
						//alert(ele.tagName+'['+ele.type+'] '+ele.name+':'+ele.value);
						if (ele.name && '' != ele.name && !ele.disabled) {
							var type = ele.type.toLowerCase();
							var value = data[ele.name];
							switch (type) {
								case "radio":
								case "checkbox":
									if (!ele.checked) break;
								case "text":
								case "password":
								case "hidden":
								case "textarea":
								case "select-one":
									var val = ele.value.trim();
									if (!value) {
										data[ele.name] = val;
									}
									else if (_.isArray(value)) {
										value.push(val);
									}
									else {
										data[ele.name] = [value, val];
									}
									break;
								case "select-multiple":
									if (!value) {
										value = [];
									}
									if (!_.isArray(value)) {
										value = [value];
									}
									for (var j = 0; j < ele.length; j++) {
										if (ele.options[j].selected) {
											value.push(ele.options[j].value.trim());
										}
									}
									if (value.length > 0) {
										data[ele.name] = 1==value.length? value[0] : value;
									}
									break;
								default:
									break;
							}
							value = null;
						}
					}
					return data;
				},
				
				_toParams = function(form) {
					return new params(form);
				};
			
			return this.Forms = {
				serialize: _serialize,
				toParams: _toParams
			};
		}).call(buds);
		
		params = (function() {
			var _params = function(data) {
				if (_.isElement(data) && 'form' == data.tagName.toLowerCase()) {
					data = forms.serialize(data);
				}
				
				var _map = _.extend({}, data);
				_.extend(this, {
					get: function(key) {
						if (!_map[key]) {
							if (false === _map[key]) return false;
							return null;
						}
						return _map[key];
					},
					put: function(key, value) {
						var tmp = _map[key];
						if (!tmp) {
							_map[key] = value;
						}
						else if (_.isArray(tmp)) {
							tmp.push(value);
						}
						else {
							_map[key] = [tmp, value];
						}
					},
					set: function(key, value) {
						_map[key] = value;
						return this;
					},
					unset: function(key) {
						delete _map[key];
						return this;
					},
					toQueryString: function(filter) {
						var m = _map;
						if (filter) {
							m = _.pick(m, filter);
						}
						var q = '';
						for (var key in m) {
							var value = m[key];
							if (_.isArray(value)) {
								for (var i = 0, len = value.length; i < len; ++i) {
									q += '&' + util.urlencode(key) + '=' + util.urlencode(value[i]);
								}
							}
							else {
								q += '&' + util.urlencode(key) + '=' + util.urlencode(value);
							}
						}
						return q.substr(1);
					},
					appendTo: function(url, filter) {
						var i1 = url.indexOf('?'),
							i2 = url.indexOf('#');
						if (i1 > i2) i1 = -1;
						var no1 = -1 == i1,
							no2 = -1 == i2;
						if (no2) {
							return url + (no1? '?' : '&') + this.toQueryString(filter);
						}
						else {
							var p1 = url.substring(0, i2),
								p2 = url.substring(i2);
							return p1 + (no1? '?' : '&') + this.toQueryString(filter) + p2;
						}
					},
					toJSON: function() {
						return _.clone(_map);
					}
				});
				
				// aliases
				this.toString = this.toQueryString;
				this.serialize = this.toQueryString;
				
				return this;
			},
			_statics = {
				parse: function(query) {
					if (!query) return null;
					var params = new _params(),
						idx = query.indexOf('?'),
						pairs;
					if (idx != -1) {
						query = query.substring(idx+1);
					}
					idx = query.indexOf('#');
					if (idx != -1) {
						query = query.substring(0, idx);
					}
					pairs = query.split("&");
					for (var i = 0, len = pairs.length; i < len; ++i) {
						var tmp = pairs[i];
						idx = tmp.indexOf("=");
						if (-1 == idx) {
							tmp = tmp.trim();
							if (tmp && tmp.length > 0) {
								tmp = util.urldecode(tmp);
								params.set(tmp, "");
							}
						}
						else {
							var key = util.urldecode(tmp.substring(0, idx));
							var value = util.urldecode(tmp.substring(idx+1));
							params.set(key, value);
						}
					}
					return params;
				},
				ofThisPage: function() {
					return _params.parse( location.search );
				}
			};
			_.extend(_params, _statics);
			return this.Params = _params;
		}).call(buds);
		
		// prevent zombie, ref.
		//	http://lostechies.com/derickbailey/2011/09/15/zombies-run-managing-page-transitions-in-backbone-apps/
		(function() {
			this.close = function() {
				this.trigger('close');
				if (this.onClose) {
					this.onClose();
				}
				this.remove();
				this.off();
			};
			this.dispel = this.close;
			return this;
		}).call(Backbone.View[KEY[0]]);
		
		return buds;
	},
	[returnDocument(), returnObject(), returnNumber(), returnString(), returnArray(), returnDate(), returnMath(), returnJQuery(), returnUnderscore(), returnBackbone()]
];
ARGS[0].apply(this, ARGS[1]);
}).call(this);