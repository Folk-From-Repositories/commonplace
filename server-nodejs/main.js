var cp = require('child_process');
var server = cp.fork(__dirname + '/app/server');
// var userLocationBroadcastActivator = cp.fork(__dirname + '/app/user-location-broadcast-activator');
// var userLocationBroadcaster = cp.fork(__dirname + '/app/user-location-broadcaster');

server.on('message', function(m) {
    console.log('server message: ' + m);
});
