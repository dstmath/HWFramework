package com.android.server.emcom;

import android.util.Log;

public class CellularCloudCotaCfgReciever extends CotaCfgReciever {
    static final String TAG = "CellularCloudCotaCfgReciever";

    public CellularCloudCotaCfgReciever() {
        super("/cellular_cloud");
        Log.d(TAG, "Recieve cellular cloud Cota para upgrade info");
    }
}
