var cp = require('child_process');
var server = cp.fork(__dirname + '/app/server');
var userLocationBroadcastActivator = cp.fork(__dirname + '/app/cron-tasks/user-location-broadcast-activator');
var userLocationBroadcaster = cp.fork(__dirname + '/app/cron-tasks/user-location-broadcaster');

server.on('message', function(m) {
    console.log('server message: ' + m);
});


process.on('SIGINT', function(err) {
    killChilds();
});

function killChilds() {
	server.kill('SIGINT');
	userLocationBroadcaster.kill('SIGINT');
	userLocationBroadcastActivator.kill('SIGINT');
}
