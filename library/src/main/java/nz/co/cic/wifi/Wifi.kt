package nz.co.cic.wifi

import android.content.Context

/**
 * Created by dipshit on 3/03/17.
 */

class Wifi(private val mContext: Context) {

    var client: Client
    var station: Station

    init {

        this.client = Client(this.mContext)
        this.station = Station(this.mContext)
    }


}

