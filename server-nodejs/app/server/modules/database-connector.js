var mysql 		= require('mysql');
var dbInfo 		= require(__dirname + '/../conf/database.json');

/* establish the database connection */
var connection;
var tag = '[database-connector.js] ';

function makeConnection() {
	connection = mysql.createConnection(dbInfo);

	connection.connect(function(err) {
		if (err) {
			console.error(tag + 'Error when connecting to db:', err);
			setTimeout(makeConnection, 3000);
		} else {
			console.log(tag + 'Establish db connection.')
		}
	});

	connection.on('error', function(err) {
		console.error(tag + 'Database error', err);

		if (err.code === 'PROTOCOL_CONNECTION_LOST') {
			makeConnection();
		} else {
			throw err;
		}
	});
}

makeConnection();

exports.connection = connection;