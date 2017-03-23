package com.example.adrienmorel.rss;

import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.easy.adri.EasyActivity;
import com.easy.adri.Useful;

import java.io.IOException;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends EasyActivity {

    TextView mLogTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLogTextView = (TextView) findViewById(R.id.logtv);
        mLogTextView.setMovementMethod(new ScrollingMovementMethod());

        try {
            new Router(this);
        } catch (IOException e) {
            log(e.toString());
        }

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        log("Ip is: " + ip);
    }

    public void log(final String text) {
        Log.d("MainActivity", text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLogTextView.setText(mLogTextView.getText() + "\n" + text);
            }
        });
    }
}
