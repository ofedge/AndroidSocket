package xyz.liuyd.socketclient;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

import xyz.liuyd.socketclient.SocketClientContrat.ClientEntry;

/**
 * Created by silcata on 2016/09/07.
 */
public class MyDBOpenHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "client.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_CLIENT =
            "CREATE TABLE " + ClientEntry.TABLE_NAME + "(" +
                    ClientEntry.COLUMN_NAME_CLIENT_ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    ClientEntry.COLUMN_NAME_PHONE_NUMBER + TEXT_TYPE + COMMA_SEP +
                    ClientEntry.COLUMN_NAME_SMS_CONTENT + TEXT_TYPE + COMMA_SEP +
                    ClientEntry.COLUMN_NAME_SMS_LIMIT + TEXT_TYPE + COMMA_SEP +
                    ClientEntry.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                    ClientEntry.COLUMN_NAME_SMS_SEND + TEXT_TYPE + ")";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ClientEntry.TABLE_NAME;

    public MyDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CLIENT);
        ContentValues values = new ContentValues();
        values.put(ClientEntry.COLUMN_NAME_CLIENT_ID, 1);
        values.put(ClientEntry.COLUMN_NAME_PHONE_NUMBER, "");
        values.put(ClientEntry.COLUMN_NAME_SMS_CONTENT, "");
        values.put(ClientEntry.COLUMN_NAME_SMS_LIMIT, 50);
        values.put(ClientEntry.COLUMN_NAME_DATE, new SimpleDateFormat("yyyyMMdd").format(new Date()));
        values.put(ClientEntry.COLUMN_NAME_SMS_SEND, 0);
        db.insert(ClientEntry.TABLE_NAME, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
