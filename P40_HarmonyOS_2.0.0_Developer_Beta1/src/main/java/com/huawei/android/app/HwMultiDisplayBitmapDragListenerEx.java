package com.huawei.android.app;

import android.graphics.Bitmap;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.view.IHwMultiDisplayBitmapDragStartListener;

public class HwMultiDisplayBitmapDragListenerEx extends IHwMultiDisplayBitmapDragStartListener.Stub {
    private static final String TAG = HwMultiDisplayBitmapDragListenerEx.class.getSimpleName();

    public void onDragStart(Bitmap b) throws RemoteException {
        Log.d(TAG, "onDragStart: drag bitmap.");
    }
}
