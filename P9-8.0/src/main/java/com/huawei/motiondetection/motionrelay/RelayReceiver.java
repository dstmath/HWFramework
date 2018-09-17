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

    protected void destroy() {
        this.mHandler = null;
        this.mMsg = null;
    }

    public void onReceive(Context context, Intent intent) {
        MRLog.d(TAG, "onReceive intent.getAction: " + intent.getAction());
        try {
            this.mMsg = Message.obtain();
            this.mMsg.what = 1;
            this.mMsg.obj = intent;
            this.mHandler.sendMessage(this.mMsg);
        } catch (Exception ex) {
            MRLog.w(TAG, ex.getMessage());
        }
    }
}
