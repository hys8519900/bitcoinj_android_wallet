package test.example.wg.testbitcoinj;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BitcoinjService extends Service {

    @Override
    public  void onCreate() {
        Log.v("service", "onCreate()");
    }

    @Override
    public void onDestroy() {
        Log.v("service", "onDestroy()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("service", "onStartCommand()" + startId + ": " + intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
