package cz.kotik.ledcontrol;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LEDState extends AppCompatActivity {

    TextView ip, ledState, upTime;

    Button switchButton;

    HttpURLConnection urlConnection;

    String urlAddress, ipAddress, state;

    boolean LedState;

    int hrs, mins, secs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledstate);

        Intent intent = getIntent();
        ipAddress = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        ip = (TextView) findViewById(R.id.textView13);
        ledState = (TextView) findViewById(R.id.textView8);
        upTime = (TextView) findViewById(R.id.textView6);

        switchButton = (Button) findViewById(R.id.button2);

        ip.setText(ipAddress);

        urlAddress = "http://" + ipAddress;

        LedState = false;

        new Connection().execute();
    }

    private class Connection extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {

                URL url = new URL(urlAddress);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(3000);
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                readStream(in);

            } catch (Exception e) {
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(LEDState.this, "Disconnected from " + ipAddress,
                                Toast.LENGTH_LONG).show();
                    }
                });

                finish();
            } finally {
                urlConnection.disconnect();
            }
            return null;
        }
    }

    public void readStream(InputStream in) {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = total.toString();
        Log.d("LEDSTATE: ", result);

        try {
            JSONObject reader = new JSONObject(result);

            hrs = Integer.parseInt(reader.getString("hours"));
            mins = Integer.parseInt(reader.getString("minutes"));
            secs = Integer.parseInt(reader.getString("seconds"));
            state = reader.getString("state");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        runOnUiThread(new Runnable() {
            public void run() {
                upTime.setText(String.format("%02d", hrs) + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs));
                if (state.equals("ON"))
                    ledState.setTextColor(Color.GREEN);
                else
                    ledState.setTextColor(Color.RED);
                ledState.setText(state);
            }
        });

    }

    public void changeLedState(View view) {
        if (LedState) {
            LedState = false;
            urlAddress = "http://" + ip.getText().toString() + "/?led=0";
            switchButton.setText("Turn ON");
            if (state.equals("OFF"))
                ledState.setTextColor(Color.RED);
            ledState.setText(state);
            upTime.setText(String.format("%02d", hrs) + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs));
            new Connection().execute();
        } else {
            LedState = true;
            urlAddress = "http://" + ip.getText().toString() + "/?led=1";
            switchButton.setText("Turn OFF");
            if (state.equals("ON"))
                ledState.setTextColor(Color.GREEN);
            ledState.setText(state);
            upTime.setText(String.format("%02d", hrs) + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs));
            new Connection().execute();
        }
    }
}
