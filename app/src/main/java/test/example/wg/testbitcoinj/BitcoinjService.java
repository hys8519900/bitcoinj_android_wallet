package test.example.wg.testbitcoinj;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import com.google.bitcoin.core.*;
import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.TestNet3Params;

import java.io.File;
import java.util.Date;

public class BitcoinjService extends Service {

    @Override
    public  void onCreate() {
        Log.v("service", "onCreate()");
        Date time = Utils.now();
        Log.v("bitcoinj", time.toString());
    }

    @Override
    public void onDestroy() {
        Log.v("service", "onDestroy()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("service", "onStartCommand() id:" + startId + ": " + intent);

        new Thread(new Runnable() {
            @Override
            public void run() {
                //NetworkParameters params = TestNet3Params.get();
                //WalletAppKit kit = new WalletAppKit(params, new File(Environment.getExternalStorageDirectory().toString()), "walletappkit-example");
                //kit.startAndWait();
                Log.v("Sdcard ",Environment.getExternalStorageDirectory().toString());

            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
