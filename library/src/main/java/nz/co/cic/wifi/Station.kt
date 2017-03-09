package nz.co.cic.wifi

import android.content.Context
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter


/**
 * Created by dipshit on 3/03/17.
 */

class Station(private val context: Context) {

    private val p2pManager: WifiP2pManager
    private val channel: WifiP2pManager.Channel

    init {
        this.p2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        this.channel = p2pManager.initialize(this.context, Looper.getMainLooper()) {
            //TODO handle disconnect
        }
    }

    fun start(): Flowable<WifiP2pGroup>{
        return Flowable.create({
            subscriber ->
            startRadio().subscribe({}, {
                err ->
                subscriber.onError(err)
            }, {
                getInfo(subscriber)
            })

        }, BackpressureStrategy.BUFFER)
    }

    fun stop(): Flowable<Boolean>{
        return stopRadio()
    }

    fun getInfo(emitter: FlowableEmitter<WifiP2pGroup>) {
        this.recursiveGetInfo(emitter)
    }

    private fun recursiveGetInfo(emitter: FlowableEmitter<WifiP2pGroup>){
        this._getInfo(0, 5, emitter)
    }

    private fun _getInfo(attempt: Int, limit: Int, emitter: FlowableEmitter<WifiP2pGroup>){
        this.p2pManager.requestGroupInfo(channel) { group ->
            if (group != null) {
                emitter.onNext(group)
            }else if(group == null && attempt < limit){
                Thread.sleep(200)
                var tmp_attempt = attempt.plus(1)
                this._getInfo(tmp_attempt, limit, emitter)
            }else {
                emitter.onError(Throwable("No group available"))
            }
        }
    }

    private fun startRadio() : Flowable<Boolean> {
        return Flowable.create({
            subscriber ->
            this.p2pManager.createGroup(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                   subscriber.onComplete()
                }

                override fun onFailure(reason: Int) {
                    if(reason == WifiP2pManager.P2P_UNSUPPORTED){
                        subscriber.onError(Throwable("P2P UNSUPPORTED"))
                    }else if(reason == WifiP2pManager.ERROR){
                        subscriber.onError(Throwable("P2P INTERNAL ERROR"))
                    }else{
                        subscriber.onComplete()
                    }
                }
            })

        }, BackpressureStrategy.BUFFER)

    }

    private fun stopRadio(): Flowable<Boolean>{
        return Flowable.create({
            subscriber ->

            this.p2pManager.removeGroup(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    subscriber.onComplete()
                }

                override fun onFailure(reason: Int) {
                    subscriber.onError(Throwable("Error: " + reason))
                }
            })
        }, BackpressureStrategy.BUFFER)

    }


}
