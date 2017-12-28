package net.phn1.patrick.onlinestatus;

import android.content.Context;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
//import android.view.MenuItem;
import android.net.ConnectivityManager;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    public TextView ipText;
    public boolean isConnected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView conn = (TextView) findViewById(R.id.connection);
        conn.setText(getNetworkInfo());
        ipText = (TextView) findViewById(R.id.ip);
        updateIP();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Status refreshed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                TextView mainTextView = (TextView) findViewById(R.id.connection);
                mainTextView.setText(getNetworkInfo());
                updateIP();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
    public String getNetworkInfo() {
        String networkStatus = "Not connected :(";
        boolean isWiFi;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = (activeNetwork != null) && activeNetwork.isConnected();
        if (isConnected) {
            networkStatus = "Connected via cell data";
            isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            if (isWiFi) {
                networkStatus = "Connected via wifi";
            }

        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        String lastChecked = sdf.format(new Date());
        networkStatus = networkStatus + "\n\nLast checked: " + lastChecked;
        return networkStatus;
    }
    public void updateIP() {
        GetIP ip = new GetIP();
        ip.start();
        ipText.setText(ip.ip);
        try {
            ip.join();
            ipText.setText(ip.ip);
        } catch (InterruptedException e) {
            ipText.setText("Error getting IP address");
        }
    }
    public class GetIP extends Thread {
        public String ip = "Getting IP";
        public void run() {
            if (isConnected) {
                HttpsURLConnection connection;
                try {
                    URL url = new URL("https://api.ipify.org");
                    connection = (HttpsURLConnection) url.openConnection();
                    connection.setConnectTimeout(3000);
                    connection.setReadTimeout(3000);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    int responseCode = connection.getResponseCode();
                    if (responseCode != HttpsURLConnection.HTTP_OK) {
                        ip = "HTTP error code: " + responseCode;
                    } else {
                        String encoding = "UTF-8";
                        String body = IOUtils.toString(connection.getInputStream(), encoding);
                        ip = "IP: " + body;
                    }
                } catch (SocketTimeoutException e) {
                    ip = "Timeout getting IP.";
                } catch (IOException e) {
                    ip = "\nUnexpected error when getting IP";
                }
            } else {
                ip = "";
            }
        }
    }
}
