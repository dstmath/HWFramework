package com.huawei.motiondetection.motionrelay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import com.huawei.motiondetection.MRLog;

class RelayReceiver extends BroadcastReceiver {
    private static final String TAG = "RelayReceiver";
    private Handler mHandler = null;
    private Message mMsg = null;

    protected RelayReceiver(Handler handler) {
        this.mHandler = handler;
    }

    /* access modifiers changed from: protected */
    public void destroy() {
        this.mHandler = null;
        this.mMsg = null;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        MRLog.d(TAG, "onReceive intent.getAction: " + intent.getAction());
        try {
            this.mMsg = Message.obtain();
            this.mMsg.what = 1;
            this.mMsg.obj = intent;
            this.mHandler.sendMessage(this.mMsg);
        } catch (Exception e) {
            MRLog.w(TAG, "onReceive Exception");
        }
    }
}
