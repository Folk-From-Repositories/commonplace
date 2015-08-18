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
    private SQLiteDatabase smsDB;
    private PhotoDatabaseHelper dbHelper;
    
    private static final String ONE_SHOT_DATABASE_NAME     = "commonplace.db";
    private static final int    ONE_SHOT_DATABASE_VERSION  = 1;
    
    private static final String ONE_SHOT_TABLE_NAME              = "Recipient";
    
    public static final String AUTHORITY                = "com.common.place.db";
    public static final Uri CONTENT_URI        = Uri.parse("content://"+AUTHORITY+"/Provider1");
    
    private static final int PROVIDER1                  = 1;
    
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "Provider1", PROVIDER1);
    }
    
    // fields from internal and external media DB
    // _id is primary key (autoincrement)
    public static final String _ID				   = "_id";
    public static final String RECIPIENT		   = "recipient";
    public static final String PHONE_NUMBER        = "phone_number";
        
    private static final String ONE_SHOT_DATABASE_CREATE 
            = "CREATE TABLE IF NOT EXISTS " + Provider.ONE_SHOT_TABLE_NAME +
            "("+_ID+"               integer primary key autoincrement, " +
            " "+RECIPIENT+"         text," +            
            " "+PHONE_NUMBER+"      text NOT NULL)";
    
    private static class PhotoDatabaseHelper extends SQLiteOpenHelper {

        PhotoDatabaseHelper(Context context) {
            super(context, Provider.ONE_SHOT_DATABASE_NAME, null, Provider.ONE_SHOT_DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(Provider.ONE_SHOT_DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
                int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Provider.ONE_SHOT_TABLE_NAME);
            onCreate(db);
        }
        
    }
    
    @Override
    public int delete(Uri uri, String arg1, String[] arg2) {
        int count = 0;
        smsDB = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)){
        case PROVIDER1:
            count = smsDB.delete(Provider.ONE_SHOT_TABLE_NAME, arg1, arg2);
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
        default : throw new SQLException("Failed to get Type "+uri);
        }
        return returnString;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        
        // get database to insert records
        smsDB = dbHelper.getWritableDatabase();
        long rowID = 0;
        Uri _uri = null;
        switch (uriMatcher.match(uri)){
        case PROVIDER1:        
            rowID = smsDB.insert(Provider.ONE_SHOT_TABLE_NAME, "", values);
            if (rowID>0) {
                _uri = ContentUris.appendId(
                        Provider.CONTENT_URI.buildUpon(), rowID).build();
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
        smsDB = dbHelper.getWritableDatabase(); //for DB create..
        return (dbHelper == null)? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, 
            String[] selectionArgs, String sortOrder) {
        Cursor c = null;
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)){
        case PROVIDER1:   
            smsDB = dbHelper.getReadableDatabase();
            sqlBuilder.setTables(Provider.ONE_SHOT_TABLE_NAME);
            c = sqlBuilder.query(smsDB, projection, selection, selectionArgs, null, null, sortOrder);
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
        smsDB = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)){
        case PROVIDER1:
            count = smsDB.update(Provider.ONE_SHOT_TABLE_NAME, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            break;
        default : throw new SQLException("Failed to query from " + uri);
        }
        return count;
    }
}
