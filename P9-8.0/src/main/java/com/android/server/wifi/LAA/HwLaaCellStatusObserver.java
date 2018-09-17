package com.android.server.wifi.LAA;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.Global;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import com.android.server.wifi.HwQoE.HwQoEJNIAdapter;

public class HwLaaCellStatusObserver {
    private static final String TAG = "LAA_HwLaaCellStatusObserver";
    private BroadcastReceiver mBroadcastReceiver;
    private ContentResolver mContentResolver;
    private Context mContext;
    private int mCurrentPhoneServiceState = -1;
    private Handler mHwLaaControllerHandler;
    private HwQoEJNIAdapter mHwQoEJNIAdapter = HwQoEJNIAdapter.getInstance();
    private IntentFilter mIntentFilter;
    private boolean mIsMobileDataEnabled;
    private int mLaaDetailedState = -1;
    private LaaPhoneStateListener mLaaPhoneStateListener;
    private boolean mPhoneServicePowerOff;
    private TelephonyManager mTelephonyManager;

    private class LaaPhoneStateListener extends PhoneStateListener {
        /* synthetic */ LaaPhoneStateListener(HwLaaCellStatusObserver this$0, LaaPhoneStateListener -this1) {
            this();
        }

        private LaaPhoneStateListener() {
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            HwLaaCellStatusObserver.this.mCurrentPhoneServiceState = serviceState.getState();
            HwLaaUtils.logD(HwLaaCellStatusObserver.TAG, "ServiceStateChanged CurrentPhoneServiceState: " + HwLaaCellStatusObserver.this.mCurrentPhoneServiceState);
            if (serviceState.getState() == 0) {
                if (HwLaaCellStatusObserver.this.mPhoneServicePowerOff) {
                    HwLaaCellStatusObserver.this.notificateServicePowerOn();
                }
                HwLaaCellStatusObserver.this.mPhoneServicePowerOff = false;
            } else if (3 == serviceState.getState()) {
                HwLaaCellStatusObserver.this.mPhoneServicePowerOff = true;
            }
        }
    }

    public HwLaaCellStatusObserver(Context context, Handler handler) {
        this.mContext = context;
        this.mHwLaaControllerHandler = handler;
        this.mContentResolver = context.getContentResolver();
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mIsMobileDataEnabled = HwLaaUtils.getSettingsGlobalBoolean(this.mContentResolver, "mobile_data", false);
        registerBroadcastReceiver();
        registerForMobileDataChanges();
    }

    private void registerBroadcastReceiver() {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    ServiceState serviceState = HwLaaCellStatusObserver.this.mTelephonyManager.getServiceState();
                    if (serviceState != null) {
                        HwLaaCellStatusObserver.this.mCurrentPhoneServiceState = serviceState.getState();
                    }
                    HwLaaUtils.logD(HwLaaCellStatusObserver.TAG, "Boot completed, CurrentPhoneServiceState: " + HwLaaCellStatusObserver.this.mCurrentPhoneServiceState);
                    HwLaaCellStatusObserver.this.mLaaPhoneStateListener = new LaaPhoneStateListener(HwLaaCellStatusObserver.this, null);
                    HwLaaCellStatusObserver.this.mTelephonyManager.listen(HwLaaCellStatusObserver.this.mLaaPhoneStateListener, 1);
                } else if (HwLaaUtils.LAA_STATE_CHANGED_ACTION.equals(action)) {
                    HwLaaCellStatusObserver.this.mLaaDetailedState = intent.getIntExtra(HwLaaUtils.EXTRA_LAA_STATE, -1);
                    HwLaaUtils.logD(HwLaaCellStatusObserver.TAG, "LAA STATE CHANGE, LaaDetailedState: " + HwLaaCellStatusObserver.this.mLaaDetailedState);
                }
            }
        };
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mIntentFilter.addAction(HwLaaUtils.LAA_STATE_CHANGED_ACTION);
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
    }

    public boolean isPermitSendLaaCmd() {
        if (this.mCurrentPhoneServiceState == 0 || this.mCurrentPhoneServiceState == 2 || this.mCurrentPhoneServiceState == 1) {
            return true;
        }
        return false;
    }

    public synchronized boolean getMobileDataEnabled() {
        return this.mIsMobileDataEnabled;
    }

    public synchronized int getLaaDetailedState() {
        return this.mLaaDetailedState;
    }

    private synchronized void notificateServicePowerOn() {
        this.mHwLaaControllerHandler.sendEmptyMessage(2);
    }

    private void registerForMobileDataChanges() {
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("mobile_data"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                HwLaaCellStatusObserver.this.mIsMobileDataEnabled = HwLaaUtils.getSettingsGlobalBoolean(HwLaaCellStatusObserver.this.mContentResolver, "mobile_data", false);
                HwLaaCellStatusObserver.this.mHwLaaControllerHandler.sendEmptyMessage(4);
                HwLaaUtils.logD(HwLaaCellStatusObserver.TAG, "MobileData has changed,isMobileDataEnabled = " + HwLaaCellStatusObserver.this.mIsMobileDataEnabled);
            }
        });
    }
}
