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
import android.widget.Toast;

import org.w3c.dom.Text;

import xyz.liuyd.socketclient.SocketClientContrat.ClientEntry;

public class ServerActivity extends AppCompatActivity {

    private EditText mHost;
    private EditText mPort;
    private Button mSaveButton;

    private SQLiteDatabase db;

    private String clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle("服务器设置");

        MyDBOpenHelper dbHelper = new MyDBOpenHelper(getApplicationContext());
        db = dbHelper.getReadableDatabase();

        mHost = (EditText) findViewById(R.id.socketHost);
        mPort = (EditText) findViewById(R.id.socketPort);
        mSaveButton = (Button) findViewById(R.id.saveServer);

        Cursor cursor = db.query(ClientEntry.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()){
            clientId = cursor.getString(cursor.getColumnIndex(ClientEntry.COLUMN_NAME_CLIENT_ID));
            String host = cursor.getString(cursor.getColumnIndex(ClientEntry.COLUMN_NAME_HOST));
            String port = cursor.getString(cursor.getColumnIndex(ClientEntry.COLUMN_NAME_PORT));
            mHost.setText(host);
            mPort.setText(port);
        }

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String host = mHost.getText().toString();
                String port = mPort.getText().toString();
                if (TextUtils.isEmpty(host)){
                    Toast.makeText(getApplicationContext(), "服务器不能为空", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(port)) {
                    Toast.makeText(getApplicationContext(), "端口不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    ContentValues values = new ContentValues();
                    values.put(ClientEntry.COLUMN_NAME_HOST, host);
                    values.put(ClientEntry.COLUMN_NAME_PORT, port);
                    db.update(ClientEntry.TABLE_NAME, values, ClientEntry.COLUMN_NAME_CLIENT_ID + " = ?", new String[]{clientId});
                    Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
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
