package nz.co.cic.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Handler
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter

/**
 * Created by dipshit on 3/03/17.
 */

class Client(private val context: Context) : BroadcastReceiver() {
    private val wifiManager: WifiManager

    private var connectionObserver: FlowableEmitter<Boolean>? = null
    private var connectionSsid: String? = null

    private var disconnectObserver : FlowableEmitter<Boolean>? = null

    init {
        this.wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        enableWifi(wifiManager)
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        context.registerReceiver(this, intentFilter)
    }


    fun enableWifi(wifiManager: WifiManager) {
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }
    }

    fun connect(ssid: String, pass: String): Flowable<Boolean>{
        return Flowable.create({
            subscriber ->
            this.connectionObserver = subscriber

            this.connectionSsid = ssid
            val connectConfig = getConfiguration(ssid, pass)
            val i = wifiManager.configuredNetworks.iterator()

            while (i.hasNext()) {
                val wifiConfiguration = i.next()
                if (wifiConfiguration.SSID.equals(connectConfig.SSID)) {
                    wifiManager.enableNetwork(wifiConfiguration.networkId, true)
                }
            }

            val netId = wifiManager.addNetwork(connectConfig)
            wifiManager.enableNetwork(netId, true)

            Handler().postDelayed({
                subscriber.onError(Throwable("timeout"))
            }, TIMEOUT.toLong())

        }, BackpressureStrategy.BUFFER)

    }

    fun disconnect(): Flowable<Boolean>{
        return Flowable.create({
            subscriber ->
            this.disconnectObserver = subscriber
            this.wifiManager.disconnect()

            Handler().postDelayed({
                subscriber.onError(Throwable("timeout"))
            }, TIMEOUT.toLong())

        }, BackpressureStrategy.BUFFER)

    }

    fun getConnection() : WifiInfo {
        val info = this.wifiManager.connectionInfo
        return info
    }

    private fun getConfiguration(ssid: String, pass: String): WifiConfiguration {
        val wifiConfiguration = WifiConfiguration()
        wifiConfiguration.SSID = "\"" + ssid + "\""
        wifiConfiguration.preSharedKey = "\"" + pass + "\""
        return wifiConfiguration
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action === WifiManager.NETWORK_STATE_CHANGED_ACTION) {
            val ni = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
            val info = wifiManager.connectionInfo

            val state = ni.state.toString()
            val ssid = info.ssid

            if (ni.state == NetworkInfo.State.CONNECTED) {
                this.connectionObserver!!.onComplete()
            }

            if (ni.state == NetworkInfo.State.DISCONNECTED) {
                this.disconnectObserver!!.onComplete()
            }
        }
    }

    companion object {

        var TIMEOUT = 15000
    }
}
