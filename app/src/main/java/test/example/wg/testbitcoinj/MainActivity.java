package test.example.wg.testbitcoinj;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;



public class MainActivity extends Activity {
    //private static final Logger log = LoggerFactory.getLogger(MainActivity.class);
    public static Handler handler;

    //Service for Binder
    BitcoinjService bitcoinjService;
    boolean mBound = false;
    //ServiceConnection for Binder
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BitcoinjService.LocalBinder binder = (BitcoinjService.LocalBinder)iBinder;
            bitcoinjService = binder.getService();
            mBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //bind Service
        Intent intent = new Intent(this, BitcoinjService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

        //Msg handler
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case BitcoinjService.MSG_KITREADY:
                        String str = msg.getData().getString("address");
                        if(str == null)
                        {
                            Log.v("msg data", "empty");
                        }
                        else
                        {
                            Log.v("msg data", str);
                        }

                        changeAddressTextView(msg.getData().getString("address"));

                        ((TextView)findViewById(R.id.text_balance)).setText(BitcoinjService.getBalance());
                        break;

                    case BitcoinjService.MSG_FRESHUI:
                        changeAddressTextView();
                        ((TextView)findViewById(R.id.text_balance)).setText(BitcoinjService.getBalance());
                }
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void start_service(View view) {
        Intent startIntent = new Intent(this, BitcoinjService.class);
        startService(startIntent);
    }

    public void stop_service(View view) {
        //unbind service
        unbindService(mConnection);

        Intent stopIntent = new Intent(this, BitcoinjService.class);
        stopService(stopIntent);
    }

    public void testbutton(View view) {
        //test service binded
        TextView textView = (TextView)findViewById(R.id.text_address);
        textView.setText("" + bitcoinjService.getRandomNumber());


        //changeAddressTextView();
    }

    public void changeAddressTextView() {
        TextView textView = (TextView)findViewById(R.id.text_address);
        textView.setText(BitcoinjService.getCurrentRecvAddress());
    }

    public void changeAddressTextView(String address) {
        TextView textView = (TextView)findViewById(R.id.text_address);
        textView.setText(address);
    }
}
