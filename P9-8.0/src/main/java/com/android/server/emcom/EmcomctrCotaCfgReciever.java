package com.android.server.emcom;

import android.util.Log;

public class EmcomctrCotaCfgReciever extends CotaCfgReciever {
    static final String TAG = "EmcomctrCotaCfgReciever";

    public EmcomctrCotaCfgReciever() {
        super("/emcom/emcomctr");
        Log.d(TAG, "Recieve Emcomctr Cota para upgrade info");
    }
}
