package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.ITrustedUIService.Stub;
import android.telephony.TelephonyManager;
import android.util.Log;

public class TrustedUIService extends Stub {
    private static final String PHONE_OUTGOING_ACTION = "android.intent.action.NEW_OUTGOING_CALL";
    private static final String PHONE_STATE_ACTION = "android.intent.action.PHONE_STATE";
    private static final String TAG = "TrustedUIService";
    private static boolean mTUIStatus = false;
    private final Context mContext;
    private TUIEventListener mListener;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TrustedUIService.TAG, " Broadcast Receiver: " + action);
            if (action != null && (action.equals(TrustedUIService.PHONE_OUTGOING_ACTION) ^ 1) != 0) {
                TrustedUIService.this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
                if (TrustedUIService.this.mTelephonyManager.getCallState() == 1) {
                    Log.d(TrustedUIService.TAG, "Phone incoming status action received, mTUIStatus: " + TrustedUIService.mTUIStatus);
                    if (TrustedUIService.mTUIStatus) {
                        TrustedUIService.this.sendTUIExitCmd();
                    }
                }
            }
        }
    };
    private TelephonyManager mTelephonyManager;

    private native int nativeSendTUICmd(int i, int i2);

    private native void nativeSendTUIExitCmd();

    private native void nativeTUILibraryDeInit();

    private native boolean nativeTUILibraryInit();

    public TrustedUIService(Context context) {
        this.mContext = context;
        this.mListener = new TUIEventListener(this, context);
        new Thread(this.mListener, TUIEventListener.class.getName()).start();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PHONE_STATE_ACTION);
        filter.addAction(PHONE_OUTGOING_ACTION);
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    public void setTrustedUIStatus(boolean status) {
        Log.d(TAG, " setTrustedUIStatus: " + status);
        mTUIStatus = status;
    }

    public boolean getTrustedUIStatus() {
        Log.d(TAG, " getTrustedUIStatus: " + mTUIStatus);
        if (Binder.getCallingUid() == 1000) {
            return mTUIStatus;
        }
        throw new SecurityException("getTrustedUIStatus should only be called by TrustedUIService");
    }

    public void sendTUIExitCmd() {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("sendTUIExitCmd should only be called by TrustedUIService");
        }
        nativeSendTUIExitCmd();
    }

    public int sendTUICmd(int event_type, int value) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("sendTUICmd should only be called by TrustedUIService");
        }
        int ret = nativeSendTUICmd(event_type, value);
        Log.d(TAG, " sendTUICmd: event_type=" + event_type + " value=" + value + " ret=" + ret);
        return ret;
    }

    public boolean TUIServiceLibraryInit() {
        return nativeTUILibraryInit();
    }
}
