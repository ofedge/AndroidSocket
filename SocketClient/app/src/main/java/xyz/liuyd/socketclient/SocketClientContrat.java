package xyz.liuyd.socketclient;

import android.provider.BaseColumns;

/**
 * Created by silcata on 2016/09/07.
 */
public final class SocketClientContrat {

    public SocketClientContrat(){}

    public static abstract class ClientEntry implements BaseColumns {
        public static final String TABLE_NAME = "clientinfo";
        public static final String COLUMN_NAME_CLIENT_ID = "id";
        public static final String COLUMN_NAME_PHONE_NUMBER = "phonenumber";
        public static final String COLUMN_NAME_SMS_CONTENT = "smsContent";
        public static final String COLUMN_NAME_SMS_LIMIT = "smslimit";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_SMS_SEND = "smssend";
    }

}
