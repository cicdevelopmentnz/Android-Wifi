package nz.co.cic.wifi;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

/**
 * Created by dipshit on 3/03/17.
 */

public class Station {

    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel channel;
    private Context context;

    public Station(Context c){
        this.context = c;
        this.p2pManager = (WifiP2pManager) c.getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = p2pManager.initialize(this.context, Looper.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                //TODO handle disconnect
            }
        });
    }

    public void start(final Deferred deferred){
        Deferred deferred1 = new DeferredObject();
        Promise p = deferred1.promise();
        p.done(new DoneCallback() {
            @Override
            public void onDone(Object result) {
                getInfo(deferred);
            }
        }).fail(new FailCallback() {
            @Override
            public void onFail(Object result) {
                deferred.reject(result);
            }
        });

        startRadio(deferred1);
    }

    public void stop(Deferred deferred){
        stopRadio(deferred);
    }

    public void getInfo(final Deferred deferred){
        this.p2pManager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if(group != null){
                    deferred.resolve(group);
                }else{
                    deferred.reject("No group available");
                }
            }
        });
    }

    private void startRadio(final Deferred deferred){
        this.p2pManager.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                deferred.resolve(null);
            }
            @Override
            public void onFailure(int reason) {
                deferred.reject(reason);
            }
        });
    }

    private void stopRadio(final Deferred deferred){
        this.p2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                deferred.resolve(null);
            }

            @Override
            public void onFailure(int reason) {
                deferred.reject(reason);
            }
        });
    }


}
