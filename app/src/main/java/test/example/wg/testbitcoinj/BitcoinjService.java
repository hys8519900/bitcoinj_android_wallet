package test.example.wg.testbitcoinj;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.GetDataMessage;
import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerEventListener;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class BitcoinjService extends Service {
    private static Thread thread;
    private static WalletAppKit kit;

    @Override
    public  void onCreate() {
        Log.v("service", "onCreate()");
        Date time = Utils.now();
        Log.v("bitcoinj", time.toString());
    }

    @Override
    public void onDestroy() {
        Log.v("service", "onDestroy()");
        kit.setAutoStop(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("service", "onStartCommand() id:" + startId + ": " + intent);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                NetworkParameters params = TestNet3Params.get();
                kit = new WalletAppKit(params, new File("/mnt/sdcard-ext/"), "walletappkit-example");
                kit.startAsync();
                kit.awaitRunning();

                Log.e("kit add EventListener","kit add EventListener");
                kit.peerGroup().addEventListener(new PeerEventListener() {
                    @Override
                    public void onBlocksDownloaded(Peer peer, Block block, int blocksLeft) {
                        Log.d("peerGroup", "onBlocksDownloaded()");
                    }

                    @Override
                    public void onChainDownloadStarted(Peer peer, int blocksLeft) {
                        Log.d("peerGroup", "onChainDownloadStarted()");
                    }

                    @Override
                    public void onPeerConnected(Peer peer, int peerCount) {
                        Log.d("peerGroup", "onPeerConnected()");
                    }

                    @Override
                    public void onPeerDisconnected(Peer peer, int peerCount) {
                        Log.d("peerGroup", "onPeerDisconnected()");
                    }

                    @Override
                    public Message onPreMessageReceived(Peer peer, Message m) {
                        Log.d("peerGroup", "onPreMessageReceived()");
                        return null;
                    }

                    @Override
                    public void onTransaction(Peer peer, Transaction t) {
                        Log.d("peerGroup", "onTransaction()");
                    }

                    @Nullable
                    @Override
                    public List<Message> getData(Peer peer, GetDataMessage m) {
                        return null;
                    }
                });

                Address sendToAddress = kit.wallet().currentReceiveAddress();
                Log.e(" Send coins to: " + sendToAddress, " ");
                Log.e(" Waiting for coins to arrive. Press Ctrl-C to quit.", " ");

                try{
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException ingored)
                {

                }

                //Log.v("Sdcard ",Environment.getExternalStorageDirectory().toString());
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
