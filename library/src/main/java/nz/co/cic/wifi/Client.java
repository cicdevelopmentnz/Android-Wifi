package nz.co.cic.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;

import org.jdeferred.Deferred;

import java.util.Iterator;

/**
 * Created by dipshit on 3/03/17.
 */

public class Client extends BroadcastReceiver{

    public static int TIMEOUT = 15000;

    private Context context;
    private WifiManager wifiManager;

    private Deferred connectionPromise;
    private String connectionSsid;

    private Deferred disconnectPromise;

    public Client(Context c){
        this.context = c;
        this.wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);

        enableWifi(wifiManager);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        c.registerReceiver(this, intentFilter);
    }


    public void enableWifi(WifiManager wifiManager){
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
    }

    public void connect(String ssid, String pass, Deferred deferred){
        this.connectionSsid = ssid;
        this.connectionPromise = deferred;
        final WifiConfiguration connectConfig = getConfiguration(ssid, pass);
        Iterator i = wifiManager.getConfiguredNetworks().iterator();

        while (i.hasNext()){
            WifiConfiguration wifiConfiguration = (WifiConfiguration) i.next();
            if(wifiConfiguration.SSID.equalsIgnoreCase(connectConfig.SSID)){
                wifiManager.enableNetwork(wifiConfiguration.networkId, true);
                return;
            }
        }

        int netId = wifiManager.addNetwork(connectConfig);
        wifiManager.enableNetwork(netId, true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!connectionPromise.isResolved()){
                    connectionPromise.reject("timeout");
                }
            }
        }, TIMEOUT);
    }

    public void disconnect(Deferred deferred){
        this.disconnectPromise = deferred;
        this.wifiManager.disconnect();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!disconnectPromise.isResolved()){
                    disconnectPromise.reject("timeout");
                }
            }
        }, TIMEOUT);
    }

    public void getConnection(Deferred deferred){
        WifiInfo info = this.wifiManager.getConnectionInfo();
        deferred.resolve(info);
    }

    private WifiConfiguration getConfiguration(String ssid, String pass){
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + ssid + "\"";
        wifiConfiguration.preSharedKey = "\"" + pass + "\"";
        return wifiConfiguration;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == WifiManager.NETWORK_STATE_CHANGED_ACTION){
            NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            WifiInfo info = wifiManager.getConnectionInfo();

            String state = ni.getState().toString();
            String ssid = info.getSSID();

            if(ni.getState()  == NetworkInfo.State.CONNECTED && ssid.indexOf(this.connectionSsid) > -1 && !this.connectionPromise.isResolved()){
                this.connectionPromise.resolve(info.getRssi());
            }

            if(ni.getState() == NetworkInfo.State.DISCONNECTED && !this.disconnectPromise.isResolved()){
                this.disconnectPromise.resolve(info.getRssi());
            }
        }
    }
}
