package test.example.wg.testbitcoinj;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Message;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DownloadListener;
import org.bitcoinj.core.GetDataMessage;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerEventListener;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

public class BitcoinjService extends Service {
    private static Thread thread;
    private static WalletAppKit kit;

    private static final Logger log = LoggerFactory.getLogger(MainActivity.class);

    //Service Binder
    private final IBinder mBinder = new LocalBinder();

    public static final int MSG_KITREADY = 1;
    public static final int MSG_FRESHUI = 2;

    //new Binder have method getSerivce()
    public class LocalBinder extends Binder {
        BitcoinjService getService() {
            return BitcoinjService.this;
        }
    }

    //Random for test Binder Service
    private final Random mGenerator = new Random();

    private static NetworkParameters params;


    @Override
    public  void onCreate() {
        Log.v("service", "onCreate()");
        Date time = Utils.now();
        Log.v("bitcoinj", time.toString());
    }

    @Override
    public void onDestroy() {
        Log.v("Service", "onDestroy()");
        kit.stopAsync();
        kit.awaitTerminated();

        if(!kit.isRunning())
        {
            log.info("kit is not Running");
        }

        thread.interrupt();
        if(thread.isInterrupted()) {
            log.info("thread is interrrupted()");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("service", "onStartCommand() id:" + startId + ": " + intent);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                params = TestNet3Params.get();
                kit = new WalletAppKit(params, new File("/mnt/sdcard-ext/"), "walletappkit-example")
                {
                    @Override
                    protected void onSetupCompleted()
                    {
                        //set false to use kit before network and block sync
                        kit.setBlockingStartup(false);
                        kit.setDownloadListener(new DownloadListener(){
                            @Override
                            protected void startDownload(int blocks)
                            {
                                Log.v("MyDownloadListener", "startDownload");
                            }

                            @Override
                            protected void progress(double pct, int blockSoFar, Date date)
                            {
                                Log.v("onSetupCompleted progress called: ", pct + "%  " + date.toString());

                            }
                        });
                    }
                };

                kit.startAsync();
                kit.awaitRunning();


                kit.wallet().addEventListener(new AbstractWalletEventListener() {
                    @Override
                    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                        super.onCoinsReceived(wallet, tx, prevBalance, newBalance);

                        Coin value = tx.getValueSentToMe(wallet);
                        Log.i("AbstractWalletEventListener: ","Received tx for " + value.toFriendlyString() + ": " + tx);
                        Log.i("AbstractWalletEventListener: ","Transaction will be forwarded after it confirms.");

                        Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<Transaction>() {
                            @Override
                            public void onSuccess(@Nullable Transaction result) {
                                Log.i("Futures Confidence depth onSuccess: ", "depth 1" + result.toString());

                                //send message to Fresh UI
                                FreshUImessage();
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                throw new RuntimeException(t);
                            }
                        });
                    }

                    @Override
                    public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                        super.onCoinsSent(wallet, tx, prevBalance, newBalance);

                        Coin valuesent = tx.getValueSentFromMe(wallet);
                        Log.i("AbstractWalletEventListener: ","Sent tx for " + valuesent.toFriendlyString() + ": " + tx);
                        Log.i("AbstractWalletEventListener: ","Transaction will be forwarded after it confirms.");

                        Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<Transaction>() {
                            @Override
                            public void onSuccess(@Nullable Transaction result) {
                                Log.i("Futures Confidence depth onSuccess: ", "depth 1" + result.toString());

                                //send message to Fresh UI
                                FreshUImessage();
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                throw new RuntimeException(t);
                            }
                        });
                    }
                });

                //show receive address
                Address sendToAddress = kit.wallet().currentReceiveAddress();
                Log.i("Wallet: ", " Send coins to: " + sendToAddress);
                //show wallet
                log.info(kit.wallet().toString());
                //Log.i("Wallet: ", " Waiting for coins to arrive. Press Ctrl-C to quit.");

                //send Message to fresh UI
                Message message = new Message();
                message.what = MSG_KITREADY;
                //set Message data
                Bundle bundle = new Bundle();
                bundle.putString("address", kit.wallet().currentReceiveAddress().toString());
                message.setData(bundle);
                MainActivity.handler.sendMessage(message);

                try{
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException ingored)
                {
                    log.info("InterruptedException called");
                }

                log.info("thead end");
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
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    //method for test Bind Service
    public int getRandomNumber() {
        return mGenerator.nextInt(100);
    }

    //static UI get Address
    public static String getCurrentRecvAddress() {
        if(kit!=null && kit.isRunning())
        {
            return kit.wallet().currentReceiveAddress().toString();
        }

        return null;
    }

    //static UI get Balance
    public static String getBalance() {
        if(kit != null && kit.isRunning())
        {
            return kit.wallet().getBalance().toFriendlyString();
        }

        return null;
    }


    //static UI get pending Balance
    public static String getPending() {
        if(kit != null && kit.isRunning())
        {
            return kit.wallet().getBalance().toFriendlyString();
        }

        return null;
    }

    public void FreshUImessage() {
        if(kit != null && kit.isRunning()) {
            Message message = new Message();
            message.what = MSG_FRESHUI;
            MainActivity.handler.sendMessage(message);
        }
    }

    //simple send coins without check broadcast success
    public static void simpleSendToAddress(String coins, String address)
    {
        Address sendaddress = null;
        try {
            sendaddress = new Address(params, address);
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }
        try {
            final Wallet.SendResult sendResult =  kit.wallet().sendCoins(kit.peerGroup(), sendaddress, Coin.parseCoin(coins));
        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
        }

    }
}
