<%@ page language="java" contentType="text/html; charset=UTF-8"
	import="com.jfetek.common.data.*"
	import="com.jfetek.common.util.*"
	import="com.jfetek.demo.weather.api.*"
    pageEncoding="UTF-8"%>
<%@taglib uri="/WEB-INF/taglib/web-utilities.tld" prefix="wu"%>
<%@include file="/WEB-INF/include/common-vars.jsp"%>
<%
out.clear();%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Weather API: query history by location & time range</title>
<link href="<%=webapp%>/css/jquery/ui.css?ver=<%=verStyle%>" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=true"></script>
<script type="text/javascript" src="<%=webapp%>/js/lib/json2+date+jquery+underscore+backbone+gmaps+buds+../plugin/jquery/ui.js?ver=<%=verScript%>"></script>
<script type="text/javascript"><wu:minify type="js">
var a0 = 'windowzdocumentzjQueryzBackbonezModelzCollectionzViewzweather';
var WeatherSystem = {
	lab: a0.split('z')
}; 
</wu:minify></script>
<style type="text/css"><wu:minify type="css">
.column-item {
	display: inline-block;
	width: 19%;
	margin: 0.2em auto;
	cursor: pointer;
}
.column-label {
	display: block;
}
.column-label:hover {
	background-color: #FFFFE1;
}
input.column {
	
}
#map_wrapper {
	display: block;
	width: 100%;
	height: 250px;
}
</wu:minify></style>
</head>
<body>
<h1>Weather API: query history by location & time range</h1>
<form>
<fieldset>
<legend>location by station</legend>
<select name="country"></select>
<select name="state"></select>
<select name="station"></select>
</fieldset>
<fieldset>
<legend>location by geo.</legend>
<div id="map_wrapper">loading map...</div>
</fieldset>
<fieldset>
<legend>time range</legend>
<input type="text" name="begin" value="" />
<input type="text" name="end" value="" />
</fieldset>
<fieldset>
<legend>column</legend>
<ol>
<%
boolean[] defaultChecked = {
	true, true, true, true, false,
	false, false, false, false, false,
	false, false, false, false, true,
	true, false, false, false, true,
	true, false, false, false, false,
	false
};
for (int i = 0, len = MongoAPI.RecordField.size(); i < len; ++i) {
	%><li class="column-item"><label class="column-label"><input type="checkbox" class="column" name="col" value="<%=MongoAPI.RecordField.get(i)%>" <%=defaultChecked[i]? "checked" : ""%> /><%=MongoAPI.RecordField.get(i)%></label></li><%
}
%>
</ol>
</fieldset>
<button type="button">make query</button>
</form>
<script type="text/javascript"><wu:minify type="js" charset="UTF-8"></wu:minify>
var w = this, ws = WeatherSystem;(function(Backbone) {
	var lab = ws.lab;
	(function(_Model, _Collection, _View) {
		var Model = this[_Model].extend({
			idAttribute: '_id',
		}),
			Collection = this[_Collection].extend({
				model: Model
			}),
			View = this[_View];
		var StationView = View.extend({
			el: 'select[name=station]',
			initialize: function(options) {
				this.range = options.range;
				this.listenTo(this.collection, 'reset sync', this.render);
				this.listenTo(this.collection, 'pick:station', this._select);
			},
			events: {
				'change': '_onChange'
			},
			template: new buds.Template('<option value="#H{_id}">#H{state, state}#H{name} geo(#H{geo, geo}) range[#H{date_range, range}]</option>', {
				range: function(d) {
					return d[0] + ' ... ' + d[1];
				},
				geo: function(d) {
					return '#H{1}, #H{0}'.format(d);
				},
				state: function(s) {
					return s&&''!=s? '['+s+'] ' : '';
				}
			}),
			render: function() {
				if (0 == this.collection.length) {
					this.$el.html('<option>NO STATION</option>');
				}
				else {
					var $el = this.$el.html('<option value="_">*** PICK STATION ***</option>');
					var t = this.template;
					this.collection.each(function(model) {
						$el.append(t.inflate(model.toJSON()));
					});
				}
				
				return this;
			},
			_select: function(station) {
				this.$el.val(station.get('_id'));

				var range = station.get('date_range');
				this.range.set({
					begin: range[0],
					end: range[1]
				});
			},
			_onChange: function() {
				var val = this.$el.val();
				if ('_' == val) return;
				var station = this.collection.get(val);
				this._select(station);

				this.collection.trigger('pick:station', station);
			}
		});
		var StateView = View.extend({
			el: 'select[name=state]',
			initialize: function(options) {
				this.listenTo(this.model, 'change:state', this.render);
			},
			render: function() {
				var states = this.model.get('state');
				if (states) {
					var $el = this.$el.html('<option value="_">*** PICK STATE ***</option>');
					_.each(states, function(state) {
						$el.append('<option value="#H{state}">#H{state}</option>'.format({state: state}));
					});
					$el.show();
				}
				else {
					this.$el.hide();
				}
				return this;
			}
		});
		var CountryView = View.extend({
			el: 'select[name=country]',
			initialize: function(options) {
				this.stations = options.stations;
				
				this.listenTo(this.collection, 'sync', this.render);
			},
			events: {
				'change': '_onChange'
			},
			render: function() {
				var $el = this.$el.empty();
				if (0 == this.collection.length) {
					$el.html('<option>NO COUNTRY DATA</option>');
				}
				else {
					$el.html('<option value="_">*** PICK COUNTRY ***</option>');
					var _templ = '<option value="#H{_id}">#H{_id} ： #H{name} (#H{station, length})</option>';
					this.collection.each(function(country) {
						$el.append(_templ.format(country.toJSON()));
					});
				}
				return this;
			},
			_onChange: function() {
				var val = this.$el.val();
				if ('_' == val) {
					this.stations.reset();
				}
				var country = this.collection.get(val);
				var stations = country.get('stations');
				if (stations) {
					this.stations.reset(stations);
				}
				else {
					this.stations.fetch({
						data: {
							country: val
						},
						success: function(collection, resp, options) {
							country.set('stations', resp);
						}
					});
				}
			}
		});
		var DatepickView = View.extend({
			initialize: function() {
				var today = new Date().toString('yyyy-MM-dd');
				var $begin = this.$begin = $('input:text[name=begin]').datepicker({
			        changeYear: true,
			        changeMonth: true,
			        yearRange: "1900:+0",
					dateFormat: 'yy-mm-dd',
					defaultDate: today,
					showButtonPanel: true,
					onClose: function(d) {
						$end.datepicker('option', 'minDate', d);
					}
				}).val(today);
				var $end = this.$end = $('input:text[name=end]').datepicker({
			        changeYear: true,
			        changeMonth: true,
			        yearRange: "1900:+0",
					dateFormat: 'yy-mm-dd',
					defaultDate: today,
					showButtonPanel: true,
					onClose: function(d) {
						$begin.datepicker('option', 'maxDate', d);
					}
				}).val(today);
		
				this.listenTo(this.model, 'change', this.render);
			},
			render: function() {
				var begin = this.model.get('begin');
				var end = this.model.get('end');
				this.$begin.datepicker('option', {
					minDate: begin,
					maxDate: end
				}).val(begin);
				this.$end.datepicker('option', {
					minDate: begin,
					maxDate: end
				}).val(end);
			}
		});
		var MapView = Backbone.View.extend({
			el: '#map_wrapper',
			initialize: function(options) {
				this.map = new GMaps({
					div: '#map_wrapper',
					zoom: 2,
					lat: 0,
					lng: 0,
					dblclick: _.bind(this._onClickMap, this)
				});
				
				this.listenTo(this.collection, 'pick:station', this._onPickStation);
			},
			_onClickMap: function(ev) {
				var latlng = ev.latLng;
				if (this.map.getZoom() >= 8) {
					var map = this.map;
					var collection = this.collection;
					this.collection.fetch({
						data: {
							lng: latlng.lng(),
							lat: latlng.lat()
						},
						success: function(collection, resp, options) {
							_.each(resp, function(station) {
								map.addMarker({
									lat: station.geo[1],
									lng: station.geo[0],
									title: station.name,
									infoWindow: {
										content: '<h3>'+station.name+'</h3><div>time range: ['+station.date_range[0]+' , '+station.date_range[1]+']</div>'
									},
									click: function() {
										collection.trigger('pick:station', new Model(station));
									}
								});
							});
						}
					});
				}
				var bias = 1;
				//this.map.fitLatLngBounds(
				//		[new google.maps.LatLng(latlng.lat()+bias, latlng.lng()+bias), new google.maps.LatLng(latlng.lat()-bias, latlng.lng()-bias)]
				//	);
			},
			_onPickStation: function(station) {
				var geo = station.get('geo');
				//this.map.removeMarkers();
				if ('?' == geo[0] || '?' == geo[1]) return false;
				var range = station.get('date_range');
				var bias = 0.02;
				this.map.fitLatLngBounds(
					[new google.maps.LatLng(geo[1]+bias, geo[0]+bias), new google.maps.LatLng(geo[1]-bias, geo[0]-bias)]
				);
				this.map.addMarker({
					lat: geo[1],
					lng: geo[0],
					title: station.get('name'),
					infoWindow: {
						content: '<div>time range: ['+range[1]+' , '+range[0]+']</div>'
					}
				});
			}
		});
		var CountryCollection = Collection.extend({
			url: '<%=webapp%>/country/',
			comparator: '_id',
			initialize: function(attrs, options) {
			}
		});
		var StationCollection = Collection.extend({
			url: '<%=webapp%>/station/',
			comparator: function(a, b) {
				var da = a.get('date_range');
				var db = b.get('date_range');
				if (da[1] > db[1]) {
					return -1;
				}
				else if (da[1] < db[1]) {
					return 1;
				}
				else {
					if (da[0] > db[0]) {
						return -1;
					}
					else if (da[0] < db[0]) {
						return 1;
					}
					else {
						return 0;
					}
				}
			},
			initialize: function (attrs, options) {
				this.cache = options.cache;
				
				this.on('sync reset', this._updateStates, this);
			},
			_updateStates: function() {
				var states = _.uniq(this.pluck('state')).sort();
				this.cache.set('state', states);
			}
		});
		$(function() {
			var cache = new Model();
			var countries = new CountryCollection([]);
			var stations = new StationCollection([], {
				cache: cache
			});
			var range = new Model();
			new StationView({
				collection: stations,
				range: range
			});
			new StateView({
				model: cache
			});
			new CountryView({
				collection: countries,
				stations: stations
			});
			new DatepickView({
				model: range
			});
			new MapView({
				collection: stations
			});
			
			countries.fetch();	// once
			
			$(document).on('click', 'button', function(ev) {
				ev.preventDefault();
				var t = new buds.Template('<%=webapp%>/history/#H{station}/#H{col,join}/', {
					join: function(a) {
						a = a || [];
						return _.isString(a)? a : a.join(',');
					}
				});
				var params = buds.Forms.serialize($('form')[0]);
				if (!params['station']) {
					alert('pick a station');
					return false;
				}
				var url = t.inflate(params);
				params = new buds.Params(params);
				url = params.appendTo(url, ['begin', 'end']);
				location.href = url;
				return false;
			});
		});
	}).call(Backbone, lab[4], lab[5], lab[6]);
}).call(w, w[ws.lab[3]]);
</script>
</body>
</html>