package com.android.server.wifi.wifipro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

public class WiFiProPortalController implements IWifiProPortalCallBack {
    private static final boolean BUG = true;
    private static final int MSG_BROWSER_REQUEST_RECEIVED = 1;
    private static final int MSG_SMS_RECEIVED = 0;
    private static final String SECRET_SMS_RECEIVED_INTENT_ACTION = "android.provider.Telephony.INTERCEPTION_SMS_RECEIVED";
    private static final String SMS_RECEIVED_INTENT_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "_AutoLogin";
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private ParsercodeHandler mHandler = new ParsercodeHandler(this, null);
    private IntentFilter mIntentFilter;
    private boolean mIsAutoFillSmsEnabled;
    private boolean mIsReceiverRegistered;
    private PortalAutoFillManager mPortalAutoFillManager;
    private WiFiProAuthCodeParser mWiFiProAuthCodeParser;

    private class ParsercodeHandler extends Handler {
        /* synthetic */ ParsercodeHandler(WiFiProPortalController this$0, ParsercodeHandler -this1) {
            this();
        }

        private ParsercodeHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    synchronized (this) {
                        String[] authSms = WiFiProPortalController.this.mWiFiProAuthCodeParser.obtainAuthSms(msg.obj);
                        if (authSms.length == 2) {
                            String sms_num = authSms[0];
                            String sms_body = authSms[1];
                            if (WiFiProPortalController.this.mWiFiProAuthCodeParser.isPortalAuthSms(sms_num)) {
                                String auth_code = WiFiProPortalController.this.mWiFiProAuthCodeParser.parsePortalAuthCode(sms_body);
                                if (WiFiProPortalController.this.mIsAutoFillSmsEnabled) {
                                    WiFiProPortalController.this.sendPortalAuthCode(auth_code);
                                }
                            } else {
                                Log.e(WiFiProPortalController.TAG, "sms_num is not portal auth server,sms_num = " + sms_num);
                            }
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public WiFiProPortalController(Context context, TelephonyManager tel, WifiProConfigurationManager config, SampleCollectionManager sample) {
        this.mContext = context;
        this.mWiFiProAuthCodeParser = new WiFiProAuthCodeParser(config);
        this.mPortalAutoFillManager = new PortalAutoFillManager(this.mContext, tel, this, sample);
        this.mIsReceiverRegistered = false;
        this.mIsAutoFillSmsEnabled = false;
        Log.d(TAG, "WiFiProPortalController init Complete! ");
    }

    private void registerBroadcastReceiver() {
        if (!this.mIsReceiverRegistered) {
            this.mBroadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (WiFiProPortalController.SMS_RECEIVED_INTENT_ACTION.equals(action) || WiFiProPortalController.SECRET_SMS_RECEIVED_INTENT_ACTION.equals(action)) {
                        WiFiProPortalController.this.mHandler.sendMessage(WiFiProPortalController.this.mHandler.obtainMessage(0, intent));
                    }
                }
            };
            this.mIntentFilter = new IntentFilter();
            this.mIntentFilter.addAction(SMS_RECEIVED_INTENT_ACTION);
            this.mIntentFilter.addAction(SECRET_SMS_RECEIVED_INTENT_ACTION);
            this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
            this.mIsReceiverRegistered = true;
        }
    }

    private void unRegisterBroadcastReceiver() {
        if (this.mIsReceiverRegistered && this.mBroadcastReceiver != null) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mBroadcastReceiver = null;
            this.mIsReceiverRegistered = false;
        }
    }

    private void sendPortalAuthCode(String code) {
        if (code == null) {
            Log.e(TAG, "Parser code is null");
        } else {
            this.mPortalAutoFillManager.notifyParsedPassword(code);
        }
    }

    public void onStartReceiveSms() {
        logD("onStartReceiveSms, mIsAutoFillSmsEnabled = " + this.mIsAutoFillSmsEnabled + ", mIsReceiverRegistered = " + this.mIsReceiverRegistered);
        this.mIsAutoFillSmsEnabled = true;
        registerBroadcastReceiver();
    }

    public void onStopReceiveSms() {
        logD("onStopReceiveSms, mIsAutoFillSmsEnabled = " + this.mIsAutoFillSmsEnabled + ", mIsReceiverRegistered = " + this.mIsReceiverRegistered);
        this.mIsAutoFillSmsEnabled = false;
        unRegisterBroadcastReceiver();
    }

    public void notifyPortalAuthenStatus(boolean portal, boolean success) {
        this.mPortalAutoFillManager.notifyPortalAuthenStatus(portal, success);
    }

    public void notifyPortalConnected(String ssid, String bssid) {
        this.mPortalAutoFillManager.notifyPortalConnected(ssid, bssid);
    }

    public void handleWifiProStatusChanged(boolean enabled, boolean portal) {
        this.mPortalAutoFillManager.handleWifiProStatusChanged(enabled, portal);
    }

    public void handleWifiDisconnected(boolean portal) {
        this.mPortalAutoFillManager.handleWifiDisconnected(portal);
    }

    private void logD(String log) {
        Log.d(TAG, log);
    }
}
