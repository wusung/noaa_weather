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
<title>Wiki API: query page count by title & time range</title>
<link href="<%=webapp%>/css/jquery/ui.css?ver=<%=verStyle%>" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=true"></script>
<script type="text/javascript" src="<%=webapp%>/js/lib/json2+date+jquery+underscore+backbone+gmaps+buds+../plugin/jquery/ui.js?ver=<%=verScript%>"></script>
<script type="text/javascript"><wu:minify type="js">
var a0 = 'windowzdocumentzjQueryzBackbonezModelzCollectionzViewzweather';
var System = {
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
<h1>Wiki API: query page count by title & time range</h1>
<form>
<fieldset>
<legend>title</legend>
<input type="text" name="title" value="" />
</fieldset>
<fieldset>
<legend>time range</legend>
<input type="text" name="begin_time" value="" />
<input type="text" name="end_time" value="" />
</fieldset>
<fieldset>
<legend>sample rate</legend>
<label><input type="radio" name="sample_rate" value="h" />1 Hour</label>
<label><input type="radio" name="sample_rate" value="d" checked />1 Day</label>
<label><input type="radio" name="sample_rate" value="w" />1 Week</label>
<label><input type="radio" name="sample_rate" value="m" />1 Month</label>
<!-- label><input type="radio" name="sample_rate" value="y" />1 Year</label -->
</fieldset>
<button type="button">make query</button>
</form>
<script type="text/javascript"><wu:minify type="js" charset="UTF-8">
var w = this, ws = System;(function(Backbone) {
	var lab = ws.lab;
	(function(_Model, _Collection, _View) {
		var Model = this[_Model].extend({
			idAttribute: '_id',
		}),
			Collection = this[_Collection].extend({
				model: Model
			}),
			View = this[_View];
		var DatepickView = View.extend({
			initialize: function() {
				var today = new Date().toString('yyyy-MM-dd');
				var $begin = this.$begin = $('input:text[name=begin_time]').datepicker({
			        changeYear: true,
			        changeMonth: true,
			        yearRange: "2008:+0",
					dateFormat: 'yy-mm-dd',
					defaultDate: today,
					showButtonPanel: true,
					onClose: function(d) {
						$end.datepicker('option', 'minDate', d);
					}
				}).val(today);
				var $end = this.$end = $('input:text[name=end_time]').datepicker({
			        changeYear: true,
			        changeMonth: true,
			        yearRange: "2008:+0",
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
				console.log(begin);
				console.log(end);
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
		$(function() {
			new DatepickView({
				model: new Model()
			});
			
			$(document).on('click', 'button', function(ev) {
				ev.preventDefault();
				var t = new buds.Template('<%=webapp%>/wikistat');
				var params = buds.Forms.serialize($('form')[0]);
				if (!params['title']) {
					alert('no title');
					return false;
				}
				var url = t.inflate(params);
				//console.log(JSON.stringify(params));
				params = new buds.Params(params);
				url = params.appendTo(url, ['title', 'begin_time', 'end_time', 'sample_rate']);
				//console.log(url);
				location.href = url;
				return false;
			});
		});
	}).call(Backbone, lab[4], lab[5], lab[6]);
}).call(w, w[ws.lab[3]]);
</wu:minify></script>
</body>
</html>