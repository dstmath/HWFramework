package com.android.commgmt.zrhung;

import android.content.Context;
import android.zrhung.ZrHungData;
import com.android.server.zrhung.IZRHungService;
import com.android.server.zrhung.ZRHungServiceEx;

public class DefaultZrHungService extends ZRHungServiceEx {
    private static final String TAG = "DefaultZrHungService";

    public DefaultZrHungService(Context context) {
        super(context);
    }

    public static IZRHungService getDefaultZrHungService(Context context) {
        return new DefaultZrHungService(context);
    }

    public void onStart() {
    }

    public void onBootPhase(int i) {
    }

    public boolean sendEvent(ZrHungData zrHungData) {
        return false;
    }
}
