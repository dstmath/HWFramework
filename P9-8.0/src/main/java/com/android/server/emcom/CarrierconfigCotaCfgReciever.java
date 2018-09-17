package com.android.server.emcom;

import android.util.Log;

public class CarrierconfigCotaCfgReciever extends CotaCfgReciever {
    static final String TAG = "CarrierconfigCotaCfgReciever";

    public CarrierconfigCotaCfgReciever() {
        super("/emcom/carrierconfig");
        Log.d(TAG, "Recieve Carrier Config Cota para upgrade info");
    }
}
