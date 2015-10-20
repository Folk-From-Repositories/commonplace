
/**
	* Common Place Web Service
	* Copyright (c) 2015 CommonPlace
**/

var http = require('http');
var express = require('express');
var session = require('express-session');
var bodyParser = require('body-parser');
var errorHandler = require('errorhandler');
var cookieParser = require('cookie-parser');
var FileStore = require('session-file-store')(session);
var packageInfo = require('../package.json');
var app = express();

app.set('port', packageInfo.port.web || 3000);
app.set('views', __dirname + '/server/views');
app.set('view engine', 'jade');
app.use(cookieParser());
app.use(session({
	secret: 'faeb4453e5d14fe6f6d04637f78077c76c73d1b4',
	proxy: true,
	resave: true,
	saveUninitialized: true,
	store: new FileStore
	})
);
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(require('stylus').middleware({ src: __dirname + '/public' }));
app.use(express.static(__dirname + '/public'));

require(__dirname + '/server/routes')(app);

if (app.get('env') == 'development') app.use(errorHandler());

http.createServer(app).listen(app.get('port'), function(){
	console.log('Express server listening on port ' + app.get('port'));
});

/**
 * Primary export.
 */
module.exports = app;

/**
 * Export the configured port.
 */
module.exports.PORT = packageInfo.port.hippie || 7891;

