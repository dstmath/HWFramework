package com.android.server.location.ntp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.ArrayList;

public class NitzTimeManager {
    private static boolean DBG = true;
    private static final long EXPIRT_TIME = 86400000;
    private static final int SUB_NUMS = TelephonyManager.getDefault().getPhoneCount();
    private static final String TAG = "NtpNitzTimeManager";
    private Context mContext;
    private BroadcastReceiver mNitzReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if ("android.intent.action.NETWORK_SET_TIME".equals(intent.getAction())) {
                    NitzTimeManager.this.mTimeManager.setCurrentTime(System.currentTimeMillis(), SystemClock.elapsedRealtime());
                }
            }
        }
    };
    private ArrayList<NtpPhoneStateListener> mPhoneStateListenerList;
    private TimeManager mTimeManager;

    public NitzTimeManager(Context context) {
        this.mContext = context;
        this.mTimeManager = new TimeManager(TAG, 86400000);
        registerForTelephonyIntents();
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (telephonyManager != null) {
            this.mPhoneStateListenerList = new ArrayList();
            if (SUB_NUMS != 0) {
                for (int i = 0; i < SUB_NUMS; i++) {
                    NtpPhoneStateListener mobilePhoneStateListener = new NtpPhoneStateListener(i);
                    telephonyManager.listen(mobilePhoneStateListener, 1);
                    this.mPhoneStateListenerList.add(mobilePhoneStateListener);
                }
            }
        }
    }

    private void registerForTelephonyIntents() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.NETWORK_SET_TIME");
        this.mContext.registerReceiver(this.mNitzReceiver, intentFilter);
    }

    public long getNitzTime() {
        return this.mTimeManager.getCurrentTime();
    }

    public boolean isCdma() {
        boolean isCdma = false;
        int size = this.mPhoneStateListenerList.size();
        for (int i = 0; i < size; i++) {
            isCdma = !isCdma ? ((NtpPhoneStateListener) this.mPhoneStateListenerList.get(i)).isCdma() : true;
        }
        if (DBG) {
            Log.d(TAG, "isCdma:" + isCdma);
        }
        return isCdma;
    }
}
