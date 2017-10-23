package com.example.popularmovieapp.database;

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
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Created by My Love on 23/10/2017.
 */

public class DataProvider extends ContentProvider {
    public static final String PROVIDER_NAME = "com.example.popularmovieapp.database.DataProvider";
    public static final String URL = "content://" + PROVIDER_NAME + "/movies";
    public static final Uri CONTENT_URI = Uri.parse(URL);


    private static HashMap<String, String> STUDENTS_PROJECTION_MAP;

    static final int MOVIES = 1;
    static final int MOVIES_ID = 2;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "movies", MOVIES);
        uriMatcher.addURI(PROVIDER_NAME, "movies/#", MOVIES_ID);
    }

    /**
     * Database specific constant declarations
     */

    private SQLiteDatabase db;

    static final String DATABASE_NAME = "Movies";
    static final String MOViES_TABLE_NAME = "movies";
    static final int DATABASE_VERSION = 1;
    public static final String COLUMN_ID = "id";
    public static final String MOVIE_ID = "movie_id";
    public static final String MOVIE_JSONOBJECT = "movie_data";
    static final String CREATE_DB_TABLE =
            "create table "
                    + MOViES_TABLE_NAME + "(" + COLUMN_ID
                    + " integer primary key autoincrement, " + MOVIE_ID
                    + " integer unique, " + MOVIE_JSONOBJECT + " text not null" + ");";  //create table

    /**
     * Helper class that actually creates and manages
     * the provider's underlying data repository.
     */

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  MOViES_TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */

        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /**
         * Add a new student record
         */
        long rowID = db.insert(	MOViES_TABLE_NAME, "", values);

        /**
         * If record is added successfully
         */
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
                        String selection,String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(MOViES_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case MOVIES:
                qb.setProjectionMap(STUDENTS_PROJECTION_MAP);
                break;

            case MOVIES_ID:
                qb.appendWhere( COLUMN_ID+ "=" + uri.getPathSegments().get(1));
                break;

            default:
        }

        if (sortOrder == null || sortOrder == ""){
            /**
             * By default sort on student names
             */
            //sortOrder = NAME;
        }

        Cursor c = qb.query(db,	projection,	selection,
                selectionArgs,null, null, sortOrder);
        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case MOVIES:
                count = db.delete(MOViES_TABLE_NAME, selection, selectionArgs);
                break;

            case MOVIES_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( MOViES_TABLE_NAME,   COLUMN_ID +" = " + id +
                        (!TextUtils.isEmpty(selection) ? "AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case MOVIES:
                count = db.update(MOViES_TABLE_NAME, values, selection, selectionArgs);
                break;

            case MOVIES_ID:
                count = db.update(MOViES_TABLE_NAME, values,COLUMN_ID + " = " + uri.getPathSegments().get(1) +(!TextUtils.isEmpty(selection) ?
                        "AND (" +selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            /**
             * Get all student records
             */
            case MOVIES:
                return "vnd.android.cursor.dir/vnd.example.movies";
            /**
             * Get a particular student
             */
            case MOVIES_ID:
                return "vnd.android.cursor.item/vnd.example.movies";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}
