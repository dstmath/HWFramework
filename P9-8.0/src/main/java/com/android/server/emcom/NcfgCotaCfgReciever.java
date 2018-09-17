package com.android.server.emcom;

import android.util.Log;

public class NcfgCotaCfgReciever extends CotaCfgReciever {
    static final String TAG = "NcfgCotaCfgReciever";

    public NcfgCotaCfgReciever() {
        super("/ncfg");
        Log.d(TAG, "Recieve Ncfg Cota para upgrade info");
    }
}
