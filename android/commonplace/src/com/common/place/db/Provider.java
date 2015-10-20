package com.common.place.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/*
 * PhotoProvider Class
 * - Create "OneShotSms.db" file in data
 * - Create table "Recipient"
 */
public class Provider extends ContentProvider{
    private SQLiteDatabase db;
    private PhotoDatabaseHelper dbHelper;
    
    private static final String DATABASE_NAME     = "commonplace.db";
    private static final int    DATABASE_VERSION  = 2;
    
    private static final String RECIPIENT_TABLE_NAME = "Recipient";
    private static final String GROUP_TABLE_NAME     = "Moim";
    private static final String MEMBER_TABLE_NAME    = "Member";
    
    public static final String AUTHORITY             = "com.common.place.db";
    public static final Uri RECIPIENT_CONTENT_URI    = Uri.parse("content://"+AUTHORITY+"/Provider1");
    public static final Uri GROUP_CONTENT_URI        = Uri.parse("content://"+AUTHORITY+"/Provider2");
    public static final Uri MEMBER_CONTENT_URI       = Uri.parse("content://"+AUTHORITY+"/Provider3");
    
    private static final int PROVIDER1               = 1;
    private static final int PROVIDER2               = 2;
    private static final int PROVIDER3               = 3;
    
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "Provider1", PROVIDER1);
        uriMatcher.addURI(AUTHORITY, "Provider2", PROVIDER2);
        uriMatcher.addURI(AUTHORITY, "Provider3", PROVIDER3);
    }
    
    // fields from internal and external media DB
    // _id is primary key (autoincrement)
    public static final String _ID				   = "_id";
    
    public static final String RECIPIENT		   = "recipient";
    public static final String PHONE_NUMBER        = "phone_number";
    
    public static final String TITLE              = "moim_title";
    public static final String OWNER              = "moim_owner";
    public static final String TIME               = "moim_time";
    public static final String LOCATION_NAME      = "locName";
    public static final String LOCATION_IMAGE_URL = "locImageUrl";
    public static final String LOCATION_LAT       = "locLat";
    public static final String LOCATION_LON       = "locLon";
    public static final String LOCATION_PHONE     = "locPhone";
    public static final String LOCATION_DESC      = "locDesc";
    
    public static final String GROUP_ID           = "groupId";
    public static final String NAME               = "name";
    
    
    private static final String CREATE_RECIPIENT_TABLE 
            = "CREATE TABLE IF NOT EXISTS " + Provider.RECIPIENT_TABLE_NAME +
            "("+_ID+"               integer primary key autoincrement, " +
            " "+RECIPIENT+"         text," +            
            " "+PHONE_NUMBER+"      text NOT NULL)";
    
    private static final String CREATE_GROUP_TABLE 
		    = "CREATE TABLE IF NOT EXISTS " + Provider.GROUP_TABLE_NAME +
		    "("+_ID+"                  integer primary key autoincrement, " +
		    " "+GROUP_ID+"             text NOT NULL," +            
		    " "+TITLE+"                text NOT NULL," +            
		    " "+OWNER+"                text NOT NULL," +            
		    " "+TIME+"                 text NOT NULL," +            
		    " "+LOCATION_NAME+"        text NOT NULL," +            
		    " "+LOCATION_IMAGE_URL+"   text NOT NULL," +            
		    " "+LOCATION_LAT+"         text NOT NULL," +            
		    " "+LOCATION_LON+"         text NOT NULL," +            
		    " "+LOCATION_PHONE+"       text NOT NULL," +            
		    " "+LOCATION_DESC+"        text NOT NULL)";
    
    private static final String CREATE_MEMBER_TABLE 
		    = "CREATE TABLE IF NOT EXISTS " + Provider.MEMBER_TABLE_NAME +
		    "("+_ID+"                  integer primary key autoincrement, " +
		    " "+GROUP_ID+"             text NOT NULL," +            
		    " "+NAME+"                 text NOT NULL," +            
		    " "+PHONE_NUMBER+"         text NOT NULL," +            
		    " "+LOCATION_LAT+"         REAL NOT NULL," +            
		    " "+LOCATION_LON+"         REAL NOT NULL)";
    
    private static class PhotoDatabaseHelper extends SQLiteOpenHelper {

        PhotoDatabaseHelper(Context context) {
            super(context, Provider.DATABASE_NAME, null, Provider.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(Provider.CREATE_RECIPIENT_TABLE);
            db.execSQL(Provider.CREATE_GROUP_TABLE);
            db.execSQL(Provider.CREATE_MEMBER_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
                int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Provider.RECIPIENT_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + Provider.GROUP_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + Provider.MEMBER_TABLE_NAME);
            onCreate(db);
        }
        
    }
    
    @Override
    public int delete(Uri uri, String arg1, String[] arg2) {
        int count = 0;
        db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)){
        case PROVIDER1:
            count = db.delete(Provider.RECIPIENT_TABLE_NAME, arg1, arg2);
            getContext().getContentResolver().notifyChange(uri, null);
            break;
        case PROVIDER2:
        	count = db.delete(Provider.GROUP_TABLE_NAME, arg1, arg2);
        	getContext().getContentResolver().notifyChange(uri, null);
        	break;
        case PROVIDER3:
        	count = db.delete(Provider.MEMBER_TABLE_NAME, arg1, arg2);
        	getContext().getContentResolver().notifyChange(uri, null);
        	break;
        default : throw new SQLException("Failed to delete row into "+uri);
        }
        return count;
    }

    @Override
    public String getType(Uri uri) {
        String returnString = null;
        switch (uriMatcher.match(uri)){
        case PROVIDER1:
            returnString = "content://"+AUTHORITY+"/Provider1";
            break;
        case PROVIDER2:
        	returnString = "content://"+AUTHORITY+"/Provider2";
        	break;
        case PROVIDER3:
        	returnString = "content://"+AUTHORITY+"/Provider3";
        	break;
        default : throw new SQLException("Failed to get Type "+uri);
        }
        return returnString;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        
        // get database to insert records
        db = dbHelper.getWritableDatabase();
        long rowID = 0;
        Uri _uri = null;
        switch (uriMatcher.match(uri)){
        case PROVIDER1:        
            rowID = db.insert(Provider.RECIPIENT_TABLE_NAME, "", values);
            if (rowID>0) {
                _uri = ContentUris.appendId(
                        Provider.RECIPIENT_CONTENT_URI.buildUpon(), rowID).build();
                getContext().getContentResolver().notifyChange(_uri, null);  
            } 
            break;
        case PROVIDER2:        
        	rowID = db.insert(Provider.GROUP_TABLE_NAME, "", values);
        	if (rowID>0) {
        		_uri = ContentUris.appendId(
        				Provider.GROUP_CONTENT_URI.buildUpon(), rowID).build();
        		getContext().getContentResolver().notifyChange(_uri, null);  
        	} 
        	break;
        case PROVIDER3:        
        	rowID = db.insert(Provider.MEMBER_TABLE_NAME, "", values);
        	if (rowID>0) {
        		_uri = ContentUris.appendId(
        				Provider.MEMBER_CONTENT_URI.buildUpon(), rowID).build();
        		getContext().getContentResolver().notifyChange(_uri, null);  
        	} 
        	break;
        default : throw new SQLException("Failed to insert row into " + uri);
        }
        return _uri;  
    }

    @Override
    public boolean onCreate() {
        dbHelper = new PhotoDatabaseHelper(getContext());
        db = dbHelper.getWritableDatabase(); //for DB create..
        return (dbHelper == null)? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, 
            String[] selectionArgs, String sortOrder) {
        Cursor c = null;
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)){
        case PROVIDER1:   
            db = dbHelper.getReadableDatabase();
            sqlBuilder.setTables(Provider.RECIPIENT_TABLE_NAME);
            c = sqlBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            break;
        case PROVIDER2:   
        	db = dbHelper.getReadableDatabase();
        	sqlBuilder.setTables(Provider.GROUP_TABLE_NAME);
        	c = sqlBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        	c.setNotificationUri(getContext().getContentResolver(), uri);
        	break;
        case PROVIDER3:   
        	db = dbHelper.getReadableDatabase();
        	sqlBuilder.setTables(Provider.MEMBER_TABLE_NAME);
        	c = sqlBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        	c.setNotificationUri(getContext().getContentResolver(), uri);
        	break;
        default : throw new SQLException("Failed to query from " + uri);
        }
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, 
            String[] selectionArgs) {
        int count = 0;
        db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)){
        case PROVIDER1:
            count = db.update(Provider.RECIPIENT_TABLE_NAME, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            break;
        case PROVIDER2:
        	count = db.update(Provider.GROUP_TABLE_NAME, values, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	break;
        case PROVIDER3:
        	count = db.update(Provider.MEMBER_TABLE_NAME, values, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	break;
        default : throw new SQLException("Failed to query from " + uri);
        }
        return count;
    }
}
