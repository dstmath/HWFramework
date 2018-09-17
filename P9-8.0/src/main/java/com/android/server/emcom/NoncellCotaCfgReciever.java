package com.android.server.emcom;

import android.util.Log;

public class NoncellCotaCfgReciever extends CotaCfgReciever {
    static final String TAG = "NoncellCotaCfgReciever";

    public NoncellCotaCfgReciever() {
        super("/emcom/noncell");
        Log.d(TAG, "Recieve Noncell Cota para upgrade info");
    }
}
