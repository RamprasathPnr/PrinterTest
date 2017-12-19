package com.printertest;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.lang.reflect.Field;

public class StartActivity extends AppCompatActivity {

    private Button btnNext;
    private boolean isConnected=false;
    private final String TAG = StartActivity.class.getCanonicalName();
    private TextView txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        btnNext=(Button)findViewById(R.id.btnNext);
        MoveToNext();
        Field[] fields = Build.VERSION_CODES.class.getFields();
        String osName = fields[Build.VERSION.SDK_INT + 1].getName();
        Log.e(TAG,"Android OsName:"+osName);

        txt=(TextView)findViewById(R.id.txt);
        txt.setText(""+osName);

    }

    public void MoveToNext() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   if (isConnected) {
                    Intent intent = new Intent(StartActivity.this, ConnectUSBActivity.class);
                    startActivity(intent);
                    finish();
//                } else {
//                    Toast.makeText(StartActivity.this, "Status not changed", Toast.LENGTH_SHORT).show();
//                    isConnected=true;
//                }
            }
        });

    }
}
