(function($) {
	
	/*
	$(window).load(function() {
		var clock = $('<p class="clock">WebSocket Clock: <span>subscribing...</span></p>').prependTo('footer');
		setTimeout(function() {
			$.atmosphere.subscribe(
				location.protocol + '//' + location.host + '/websocket/clock', function callback(response) {
				if (response.status == 200 && response.responseBody && response.state != 'connected' && response.state != 'closed') {
					$('span', clock).html(response.responseBody);
				}
			}, $.atmosphere.request = {
				logLevel: 'none',
				transport: 'websocket'
			});
		}, 200);
	});
	*/
	
	$(':header').each(function() {
		if (!$.support.opacity) {
			$(this).css({
				filter: 'progid:DXImageTransform.Microsoft.AlphaImageLoader(src=\"\",sizingMethod=\"crop\")',
				zoom: 1
			});
		}
	});
	
	$(function() {
		$('#options, #view').bind('beforeSend', function(e) {
			if (window.console) {
				window.console.log(e);
			}
		}).bind('complete', function(e) {
			if (window.console) {
				window.console.log(e);
			}
		});
	});

})(jQuery);