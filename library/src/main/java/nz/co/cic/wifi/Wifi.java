package nz.co.cic.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;

import java.util.Iterator;

/**
 * Created by dipshit on 3/03/17.
 */

public class Wifi {

    private Context mContext;

    public Client client;
    public Station station;

    public Wifi(Context context){
        this.mContext = context;

        this.client = new Client(this.mContext);
        this.station = new Station(this.mContext);
    }


}

