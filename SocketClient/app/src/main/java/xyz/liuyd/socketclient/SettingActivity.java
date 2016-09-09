package xyz.liuyd.socketclient;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import xyz.liuyd.socketclient.SocketClientContrat.ClientEntry;

public class SettingActivity extends AppCompatActivity {

    private SQLiteDatabase db;

    private String clientId;

    private EditText mPhoneNumber;
    private EditText mSmsContent;
    private EditText mSmsLimit;
    private TextView mSmsSend;
    private Button mSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mPhoneNumber = (EditText) findViewById(R.id.phoneNumberText);
        mSmsContent = (EditText) findViewById(R.id.smsContentText);
        mSmsLimit = (EditText) findViewById(R.id.smsLimitText);
        mSmsSend = (TextView) findViewById(R.id.smsSend);
        mSaveButton = (Button) findViewById(R.id.saveSetting);

        MyDBOpenHelper dbHelper = new MyDBOpenHelper(getApplicationContext());
        db = dbHelper.getReadableDatabase();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle("一般设置");

        Cursor cursor = db.query(ClientEntry.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()){
            clientId = cursor.getString(cursor.getColumnIndex(ClientEntry.COLUMN_NAME_CLIENT_ID));
            String phoneNumber = cursor.getString(cursor.getColumnIndex(ClientEntry.COLUMN_NAME_PHONE_NUMBER));
            if (!TextUtils.isEmpty(phoneNumber)){
                mPhoneNumber.setText(phoneNumber);
            }
            String smsContent = cursor.getString(cursor.getColumnIndex(ClientEntry.COLUMN_NAME_SMS_CONTENT));
            if (!TextUtils.isEmpty(smsContent)) {
                mSmsContent.setText(smsContent);
            }
            String smsLimit = cursor.getString(cursor.getColumnIndex(ClientEntry.COLUMN_NAME_SMS_LIMIT));
            if (!TextUtils.isEmpty(smsLimit)){
                mSmsLimit.setText(smsLimit);
            }
            int smsSend = cursor.getInt(cursor.getColumnIndex(ClientEntry.COLUMN_NAME_SMS_SEND));
            mSmsSend.setText("今日已发送短信: " + smsSend);
        }

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = mPhoneNumber.getText().toString();
                String smsLimit = mSmsLimit.getText().toString();
                String smsContent = mSmsContent.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(getApplicationContext(), "分机号码必须填写!", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(smsContent)) {
                    Toast.makeText(getApplicationContext(), "短信内容必须填写!", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(smsLimit)){
                    Toast.makeText(getApplicationContext(), "每日短信上限必须填写", Toast.LENGTH_LONG).show();
                }else {
                    ContentValues values = new ContentValues();
                    values.put(ClientEntry.COLUMN_NAME_PHONE_NUMBER, phoneNumber);
                    values.put(ClientEntry.COLUMN_NAME_SMS_CONTENT, smsContent);
                    values.put(ClientEntry.COLUMN_NAME_SMS_LIMIT, smsLimit);
                    db.update(ClientEntry.TABLE_NAME, values, ClientEntry.COLUMN_NAME_CLIENT_ID + " = ?", new String[]{clientId});
                    Toast.makeText(getApplicationContext(), "保存成功!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
            Intent intent = new Intent();
            intent.setClass(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            Intent intent = new Intent();
            intent.setClass(this, MainActivity.class);
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }

}
