package cz.kotik.ledcontrol;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.Time;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "cz.kotik.ledcontrol.MESSAGE";

    EditText editTxt;
    Time timeOut;
    String IP;
    boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTxt = (EditText) findViewById(R.id.editText2);
        timeOut = new Time(10);
        connected = false;
    }

    private class Connection extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Socket socket;

            try {
                socket = new Socket();
                SocketAddress socketAddress = new InetSocketAddress(IP, 80);
                socket.connect(socketAddress, 4000);
                if (socket.isConnected()) {
                    connected = true;
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void connect(View view) {

        IP = editTxt.getText().toString();

        findViewById(R.id.textView9).setVisibility(View.INVISIBLE);
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new Connection().execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);
                        if (connected) {
                            findViewById(R.id.textView9).setVisibility(View.INVISIBLE);
                            Intent intent = new Intent(MainActivity.this, LEDState.class);
                            intent.putExtra(EXTRA_MESSAGE, editTxt.getText().toString());
                            startActivity(intent);
                        } else {
                            findViewById(R.id.textView9).setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }).start();
    }
}
