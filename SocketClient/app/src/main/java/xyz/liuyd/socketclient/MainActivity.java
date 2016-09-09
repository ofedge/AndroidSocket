package xyz.liuyd.socketclient;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import xyz.liuyd.socketclient.SocketClientContrat.ClientEntry;

public class MainActivity extends AppCompatActivity {

    private Button mConnectBtn;
    private Button mDisconnectBtn;
    private TextView mTextView;

    private Socket socket;
    private String socketMsg;
    private InputStream is;
    private OutputStream os;

    private boolean threadFlag; // whether the thread is running
    private boolean connFlag = false; // whether the socket is connected
    private boolean settingFlag = false; // whether the phone number is correct

    private Handler handler;
    private String targetNumber; // number received from socket and prepare to send SMS;

    private SQLiteDatabase db;

    private String clientId;
    private String phoneNumber;
    private int smsLimit;
    private int smsSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyDBOpenHelper dbHelper = new MyDBOpenHelper(getApplicationContext());
        db = dbHelper.getReadableDatabase();

        handler = new Handler() {
            public void handleMessage(Message message){
                switch(message.what){
                    case 1:
                        if (mTextView != null){
                            mTextView.setText(mTextView.getText() + "\n" + socketMsg);
                        }
                        break;
                }
            }
        };

        mConnectBtn = (Button) findViewById(R.id.connect_server);
        mDisconnectBtn = (Button) findViewById(R.id.disconnect_server);
        mTextView = (TextView) findViewById(R.id.server_info);
        mTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        mConnectBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Cursor cursor = db.query(ClientEntry.TABLE_NAME, null, null, null, null, null, null);
                if (cursor.moveToFirst()){
                    phoneNumber = cursor.getString(cursor.getColumnIndex(ClientEntry.COLUMN_NAME_PHONE_NUMBER));
                    if (TextUtils.isEmpty(phoneNumber)){ // if the phone number is null
                        settingFlag = false;
                    } else {
                        settingFlag = true;
                        clientId = cursor.getString(cursor.getColumnIndex(ClientEntry.COLUMN_NAME_CLIENT_ID));
                        smsLimit = cursor.getInt(cursor.getColumnIndex(ClientEntry.COLUMN_NAME_SMS_LIMIT));
                        String date = cursor.getString(cursor.getColumnIndex(ClientEntry.COLUMN_NAME_DATE));
                        if (!new SimpleDateFormat("yyyyMMdd").format(new Date()).equals(date)){ // if the statistic data is not today, reset it!
                            ContentValues values = new ContentValues();
                            values.put(ClientEntry.COLUMN_NAME_DATE, new SimpleDateFormat("yyyyMMdd").format(new Date()));
                            values.put(ClientEntry.COLUMN_NAME_SMS_SEND, 0);
                            db.update(ClientEntry.TABLE_NAME, values, ClientEntry.COLUMN_NAME_CLIENT_ID + " = ?", new String[]{clientId});
                            smsSend = 0;
                        } else {
                            smsSend = cursor.getInt(cursor.getColumnIndex(ClientEntry.COLUMN_NAME_SMS_SEND));
                        }
                    }
                }
                if (!settingFlag) {
                    socketMsg = "Your phone number is required to continue";
                    handler.sendEmptyMessage(1);
                } else if (smsSend >= smsLimit) {
                    socketMsg = "Your SMS send reached your SMS limit!";
                    handler.sendEmptyMessage(1);
                } else if (!connFlag){
                    threadFlag = true;
                    mTextView.setText("Connecting...");
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            try {
                                socket = new Socket("104.128.233.118", 9999);
                                connFlag = true;
                                socketMsg = "Connection build! SMS send: " + smsSend + ", SMS limit: " + smsLimit;
                                handler.sendEmptyMessage(1);
                                is = socket.getInputStream();
                                os = socket.getOutputStream();
                                os.write(ByteArrayUtils.intTo4String(phoneNumber.length())); // send head
                                os.flush();
                                os.write(phoneNumber.getBytes()); // send body
                                os.flush();
                                while (threadFlag) {
                                    while(is.available() > 0){
                                        byte[] header = new byte[4];
                                        is.read(header, 0, 4);
                                        int length = Integer.valueOf(new String(header));
                                        byte[] body = new byte[length];
                                        is.read(body, 0, length);
                                        targetNumber = new String(body);
                                        socketMsg = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date())  + " >> " + targetNumber;
                                        handler.sendEmptyMessage(1);
                                        try{
                                            SmsManager smsManager = SmsManager.getDefault();
                                            smsManager.sendTextMessage(targetNumber, null, "msg send from app", null, null);
                                            smsSend++;
                                            ContentValues values = new ContentValues();
                                            values.put(ClientEntry.COLUMN_NAME_SMS_SEND, smsSend);
                                            db.update(ClientEntry.TABLE_NAME, values, ClientEntry.COLUMN_NAME_CLIENT_ID + " = ?", new String[]{clientId});
                                            if (smsSend >= smsLimit){
                                                socketMsg = "Your SMS send reach your SMS limit, connection closed";
                                                handler.sendEmptyMessage(1);
                                                closeConnection();
                                            }
                                            socketMsg = "Send message to " + targetNumber + " successfully!, SMS send: " + smsSend + ", SMS limit: " + smsLimit;
                                            handler.sendEmptyMessage(1);
                                        } catch (Exception e){
                                            Log.e("MainActivity", e.getMessage());
                                            socketMsg = "Error sending message to " + targetNumber + ", reason: " + e.getMessage();
                                            handler.sendEmptyMessage(1);
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                Log.e("MainActivity", e.getMessage());
                                socketMsg = "Connection error: " + e.getMessage();
                                handler.sendEmptyMessage(1);
                            } catch (Exception e){
                                Log.e("MainActivity", e.getMessage());
                                socketMsg = "Connection error: " + e.getMessage();
                                handler.sendEmptyMessage(1);
                            }
                        }
                    }).start();
                }
            }
        });

        mDisconnectBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                closeConnection();
            }
        });
    }

    /**
     * close socket connection
     */
    private void closeConnection(){
        try {
            if (socket != null){
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.write(ByteArrayUtils.intTo4String(1));
                out.flush();
                out.write("0".getBytes());
                out.flush();
                out.close();
                socket.close();
            }
            threadFlag = false;
            if (is != null) {
                is.close();
            }
            connFlag = false;
            socketMsg = "Connection closed!";
            handler.sendEmptyMessage(1);
        } catch (IOException e) {
            Log.e("MainActivity", e.getMessage());
            socketMsg = "Close error: " + e.getMessage();
            handler.sendEmptyMessage(1);
        } catch (Exception e){
            Log.e("MainActivity", e.getMessage());
            socketMsg = "Close error: " + e.getMessage();
            handler.sendEmptyMessage(1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (socket != null){
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.write("0".getBytes());
                out.flush();
                out.close();
                socket.close();

            }
            threadFlag = false;
            if (is != null) {
                is.close();
            }
            connFlag = false;
        } catch (IOException e) {
            Log.e("MainActivity", e.getMessage());
        } catch (Exception e){
            Log.e("MainActivity", e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.action_settings){
            if (connFlag) {
                Toast.makeText(getApplicationContext(), "You must disconnect before enter setting.", Toast.LENGTH_SHORT).show();
                return false;
            }
            Intent intent = new Intent();
            intent.setClass(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
