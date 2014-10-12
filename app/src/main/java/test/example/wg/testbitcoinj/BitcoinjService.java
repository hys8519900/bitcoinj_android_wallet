package test.example.wg.testbitcoinj;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class BitcoinjService extends Service {
    private static Thread thread;

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

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                NetworkParameters params = TestNet3Params.get();
                WalletAppKit kit = new WalletAppKit(params, new File("/mnt/sdcard-ext/"), "walletappkit-example");
                kit.startAsync();
                kit.awaitRunning();
                Log.v("Sdcard ",Environment.getExternalStorageDirectory().toString());
                /*
                File file = new File("mnt/sdcard-ext/testwritefile");
                if(!file.exists())
                {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                */

            }
        });
        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
