package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Telephony.Sms.Intents;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;

public class SmsStorageMonitor extends Handler {
    private static final int EVENT_ICC_FULL = 1;
    private static final int EVENT_RADIO_ON = 3;
    private static final int EVENT_REPORT_MEMORY_STATUS_DONE = 2;
    private static final String TAG = "SmsStorageMonitor";
    private static final int WAKE_LOCK_TIMEOUT = 5000;
    final CommandsInterface mCi;
    private final Context mContext;
    Phone mPhone;
    private boolean mReportMemoryStatusPending;
    private final BroadcastReceiver mResultReceiver;
    boolean mStorageAvailable;
    private WakeLock mWakeLock;

    public SmsStorageMonitor(Phone phone) {
        this.mStorageAvailable = true;
        this.mResultReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.DEVICE_STORAGE_FULL")) {
                    SmsStorageMonitor.this.mStorageAvailable = false;
                    SmsStorageMonitor.this.mCi.reportSmsMemoryStatus(false, SmsStorageMonitor.this.obtainMessage(SmsStorageMonitor.EVENT_REPORT_MEMORY_STATUS_DONE));
                } else if (intent.getAction().equals("android.intent.action.DEVICE_STORAGE_NOT_FULL")) {
                    SmsStorageMonitor.this.mStorageAvailable = true;
                    SmsStorageMonitor.this.mCi.reportSmsMemoryStatus(true, SmsStorageMonitor.this.obtainMessage(SmsStorageMonitor.EVENT_REPORT_MEMORY_STATUS_DONE));
                }
            }
        };
        this.mPhone = phone;
        this.mContext = phone.getContext();
        this.mCi = phone.mCi;
        createWakelock();
        this.mCi.setOnIccSmsFull(this, EVENT_ICC_FULL, null);
        this.mCi.registerForOn(this, EVENT_RADIO_ON, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.DEVICE_STORAGE_FULL");
        filter.addAction("android.intent.action.DEVICE_STORAGE_NOT_FULL");
        this.mContext.registerReceiver(this.mResultReceiver, filter);
    }

    public void dispose() {
        this.mCi.unSetOnIccSmsFull(this);
        this.mCi.unregisterForOn(this);
        this.mContext.unregisterReceiver(this.mResultReceiver);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_ICC_FULL /*1*/:
                handleIccFull();
            case EVENT_REPORT_MEMORY_STATUS_DONE /*2*/:
                if (msg.obj.exception != null) {
                    this.mReportMemoryStatusPending = true;
                    Rlog.v(TAG, "Memory status report to modem pending : mStorageAvailable = " + this.mStorageAvailable);
                    return;
                }
                this.mReportMemoryStatusPending = false;
            case EVENT_RADIO_ON /*3*/:
                if (this.mReportMemoryStatusPending) {
                    Rlog.v(TAG, "Sending pending memory status report : mStorageAvailable = " + this.mStorageAvailable);
                    this.mCi.reportSmsMemoryStatus(this.mStorageAvailable, obtainMessage(EVENT_REPORT_MEMORY_STATUS_DONE));
                }
            default:
        }
    }

    private void createWakelock() {
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(EVENT_ICC_FULL, TAG);
        this.mWakeLock.setReferenceCounted(true);
    }

    private void handleIccFull() {
        Intent intent = new Intent(Intents.SIM_FULL_ACTION);
        this.mWakeLock.acquire(5000);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        this.mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
    }

    public boolean isStorageAvailable() {
        return this.mStorageAvailable;
    }
}
