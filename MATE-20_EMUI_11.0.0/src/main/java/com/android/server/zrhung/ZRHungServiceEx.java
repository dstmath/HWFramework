package com.android.server.zrhung;

import android.content.Context;
import android.util.Log;
import android.zrhung.ZrHungData;

public class ZRHungServiceEx implements IZRHungService {
    private static final String TAG = "ZRHungServiceEx";

    public ZRHungServiceEx(Context context) {
    }

    @Override // com.android.server.zrhung.IZRHungService
    public void onStart() {
        Log.i(TAG, "ZRHungService onStart!");
    }

    @Override // com.android.server.zrhung.IZRHungService
    public void onBootPhase(int phase) {
        Log.i(TAG, "ZRHungService onBootPhase!");
    }

    @Override // com.android.server.zrhung.IZRHungService
    public boolean sendEvent(ZrHungData args) {
        return false;
    }
}
