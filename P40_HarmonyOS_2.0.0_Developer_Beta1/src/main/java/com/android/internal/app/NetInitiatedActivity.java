package com.android.internal.app;

import android.annotation.UnsupportedAppUsage;
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
import com.android.internal.R;
import com.android.internal.app.AlertController;
import com.android.internal.location.GpsNetInitiatedHandler;

public class NetInitiatedActivity extends AlertActivity implements DialogInterface.OnClickListener {
    private static final boolean DEBUG = true;
    private static final int GPS_NO_RESPONSE_TIME_OUT = 1;
    private static final int NEGATIVE_BUTTON = -2;
    private static final int POSITIVE_BUTTON = -1;
    private static final String TAG = "NetInitiatedActivity";
    private static final boolean VERBOSE = false;
    private int default_response = -1;
    private int default_response_timeout = 6;
    private final Handler mHandler = new Handler() {
        /* class com.android.internal.app.NetInitiatedActivity.AnonymousClass2 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (NetInitiatedActivity.this.notificationId != -1) {
                    NetInitiatedActivity netInitiatedActivity = NetInitiatedActivity.this;
                    netInitiatedActivity.sendUserResponse(netInitiatedActivity.default_response);
                }
                NetInitiatedActivity.this.finish();
            }
        }
    };
    private BroadcastReceiver mNetInitiatedReceiver = new BroadcastReceiver() {
        /* class com.android.internal.app.NetInitiatedActivity.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.d(NetInitiatedActivity.TAG, "NetInitiatedReceiver onReceive: " + intent.getAction());
            if (intent.getAction() == GpsNetInitiatedHandler.ACTION_NI_VERIFY) {
                NetInitiatedActivity.this.handleNIVerify(intent);
            }
        }
    };
    private int notificationId = -1;
    private int timeout = -1;

    /* access modifiers changed from: protected */
    @Override // com.android.internal.app.AlertActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        AlertController.AlertParams p = this.mAlertParams;
        Context context = getApplicationContext();
        p.mTitle = intent.getStringExtra("title");
        p.mMessage = intent.getStringExtra("message");
        p.mPositiveButtonText = String.format(context.getString(R.string.gpsVerifYes), new Object[0]);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = String.format(context.getString(R.string.gpsVerifNo), new Object[0]);
        p.mNegativeButtonListener = this;
        this.notificationId = intent.getIntExtra("notif_id", -1);
        this.timeout = intent.getIntExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_TIMEOUT, this.default_response_timeout);
        this.default_response = intent.getIntExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_DEFAULT_RESPONSE, 1);
        Log.d(TAG, "onCreate() : notificationId: " + this.notificationId + " timeout: " + this.timeout + " default_response:" + this.default_response);
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(1), (long) (this.timeout * 1000));
        setupAlert();
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        registerReceiver(this.mNetInitiatedReceiver, new IntentFilter(GpsNetInitiatedHandler.ACTION_NI_VERIFY));
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        unregisterReceiver(this.mNetInitiatedReceiver);
    }

    @Override // android.content.DialogInterface.OnClickListener
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
    /* access modifiers changed from: public */
    private void sendUserResponse(int response) {
        Log.d(TAG, "sendUserResponse, response: " + response);
        ((LocationManager) getSystemService("location")).sendNiResponse(this.notificationId, response);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void handleNIVerify(Intent intent) {
        this.notificationId = intent.getIntExtra("notif_id", -1);
        Log.d(TAG, "handleNIVerify action: " + intent.getAction());
    }

    private void showNIError() {
        Toast.makeText(this, "NI error", 1).show();
    }
}
