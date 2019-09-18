package com.android.internal.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.app.AlertController;
import com.android.internal.location.GpsNetInitiatedHandler;

public class NetInitiatedActivity extends AlertActivity implements DialogInterface.OnClickListener {
    /* access modifiers changed from: private */
    public static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int GPS_NO_RESPONSE_TIME_OUT = 1;
    private static final int NEGATIVE_BUTTON = -2;
    private static final int POSITIVE_BUTTON = -1;
    private static final String TAG = "NetInitiatedActivity";
    private static final boolean VERBOSE = false;
    /* access modifiers changed from: private */
    public int default_response = -1;
    private int default_response_timeout = 6;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (NetInitiatedActivity.this.notificationId != -1) {
                    NetInitiatedActivity.this.sendUserResponse(NetInitiatedActivity.this.default_response);
                }
                NetInitiatedActivity.this.finish();
            }
        }
    };
    private BroadcastReceiver mNetInitiatedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (NetInitiatedActivity.DEBUG) {
                Log.d(NetInitiatedActivity.TAG, "NetInitiatedReceiver onReceive: " + intent.getAction());
            }
            if (intent.getAction() == GpsNetInitiatedHandler.ACTION_NI_VERIFY) {
                NetInitiatedActivity.this.handleNIVerify(intent);
            }
        }
    };
    /* access modifiers changed from: private */
    public int notificationId = -1;
    private int timeout = -1;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        AlertController.AlertParams p = this.mAlertParams;
        Context context = getApplicationContext();
        p.mTitle = intent.getStringExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_TITLE);
        p.mMessage = intent.getStringExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_MESSAGE);
        p.mPositiveButtonText = String.format(context.getString(17040146), new Object[0]);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = String.format(context.getString(17040145), new Object[0]);
        p.mNegativeButtonListener = this;
        this.notificationId = intent.getIntExtra("notif_id", -1);
        this.timeout = intent.getIntExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_TIMEOUT, this.default_response_timeout);
        this.default_response = intent.getIntExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_DEFAULT_RESPONSE, 1);
        if (DEBUG) {
            Log.d(TAG, "onCreate() : notificationId: " + this.notificationId + " timeout: " + this.timeout + " default_response:" + this.default_response);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), (long) (this.timeout * 1000));
        setupAlert();
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        if (DEBUG) {
            Log.d(TAG, "onResume");
        }
        registerReceiver(this.mNetInitiatedReceiver, new IntentFilter(GpsNetInitiatedHandler.ACTION_NI_VERIFY));
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        if (DEBUG) {
            Log.d(TAG, "onPause");
        }
        unregisterReceiver(this.mNetInitiatedReceiver);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            sendUserResponse(1);
        }
        if (which == -2) {
            sendUserResponse(2);
        }
        finish();
        this.notificationId = -1;
    }

    /* access modifiers changed from: private */
    public void sendUserResponse(int response) {
        if (DEBUG) {
            Log.d(TAG, "sendUserResponse, response: " + response);
        }
        ((LocationManager) getSystemService("location")).sendNiResponse(this.notificationId, response);
    }

    /* access modifiers changed from: private */
    public void handleNIVerify(Intent intent) {
        this.notificationId = intent.getIntExtra("notif_id", -1);
        if (DEBUG) {
            Log.d(TAG, "handleNIVerify action: " + intent.getAction());
        }
    }

    private void showNIError() {
        Toast.makeText(this, "NI error", 1).show();
    }
}
