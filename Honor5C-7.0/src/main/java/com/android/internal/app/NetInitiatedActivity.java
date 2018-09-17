package com.android.internal.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.R;
import com.android.internal.app.AlertController.AlertParams;
import com.android.internal.location.GpsNetInitiatedHandler;
import com.android.internal.telephony.RILConstants;

public class NetInitiatedActivity extends AlertActivity implements OnClickListener {
    private static boolean DEBUG = false;
    private static final int GPS_NO_RESPONSE_TIME_OUT = 1;
    private static final int NEGATIVE_BUTTON = -2;
    private static final int POSITIVE_BUTTON = -1;
    private static final String TAG = "NetInitiatedActivity";
    private static final boolean VERBOSE = false;
    private int default_response;
    private int default_response_timeout;
    private final Handler mHandler;
    private BroadcastReceiver mNetInitiatedReceiver;
    private int notificationId;
    private int timeout;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.NetInitiatedActivity.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.app.NetInitiatedActivity.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.NetInitiatedActivity.<clinit>():void");
    }

    public NetInitiatedActivity() {
        this.notificationId = POSITIVE_BUTTON;
        this.timeout = POSITIVE_BUTTON;
        this.default_response = POSITIVE_BUTTON;
        this.default_response_timeout = 6;
        this.mNetInitiatedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (NetInitiatedActivity.DEBUG) {
                    Log.d(NetInitiatedActivity.TAG, "NetInitiatedReceiver onReceive: " + intent.getAction());
                }
                if (intent.getAction() == GpsNetInitiatedHandler.ACTION_NI_VERIFY) {
                    NetInitiatedActivity.this.handleNIVerify(intent);
                }
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case NetInitiatedActivity.GPS_NO_RESPONSE_TIME_OUT /*1*/:
                        if (NetInitiatedActivity.this.notificationId != NetInitiatedActivity.POSITIVE_BUTTON) {
                            NetInitiatedActivity.this.sendUserResponse(NetInitiatedActivity.this.default_response);
                        }
                        NetInitiatedActivity.this.finish();
                    default:
                }
            }
        };
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        AlertParams p = this.mAlertParams;
        Context context = getApplicationContext();
        p.mTitle = intent.getStringExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_TITLE);
        p.mMessage = intent.getStringExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_MESSAGE);
        p.mPositiveButtonText = String.format(context.getString(R.string.gpsVerifYes), new Object[0]);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = String.format(context.getString(R.string.gpsVerifNo), new Object[0]);
        p.mNegativeButtonListener = this;
        this.notificationId = intent.getIntExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_NOTIF_ID, POSITIVE_BUTTON);
        this.timeout = intent.getIntExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_TIMEOUT, this.default_response_timeout);
        this.default_response = intent.getIntExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_DEFAULT_RESPONSE, GPS_NO_RESPONSE_TIME_OUT);
        if (DEBUG) {
            Log.d(TAG, "onCreate() : notificationId: " + this.notificationId + " timeout: " + this.timeout + " default_response:" + this.default_response);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(GPS_NO_RESPONSE_TIME_OUT), (long) (this.timeout * RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED));
        setupAlert();
    }

    protected void onResume() {
        super.onResume();
        if (DEBUG) {
            Log.d(TAG, "onResume");
        }
        registerReceiver(this.mNetInitiatedReceiver, new IntentFilter(GpsNetInitiatedHandler.ACTION_NI_VERIFY));
    }

    protected void onPause() {
        super.onPause();
        if (DEBUG) {
            Log.d(TAG, "onPause");
        }
        unregisterReceiver(this.mNetInitiatedReceiver);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == POSITIVE_BUTTON) {
            sendUserResponse(GPS_NO_RESPONSE_TIME_OUT);
        }
        if (which == NEGATIVE_BUTTON) {
            sendUserResponse(2);
        }
        finish();
        this.notificationId = POSITIVE_BUTTON;
    }

    private void sendUserResponse(int response) {
        if (DEBUG) {
            Log.d(TAG, "sendUserResponse, response: " + response);
        }
        ((LocationManager) getSystemService("location")).sendNiResponse(this.notificationId, response);
    }

    private void handleNIVerify(Intent intent) {
        this.notificationId = intent.getIntExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_NOTIF_ID, POSITIVE_BUTTON);
        if (DEBUG) {
            Log.d(TAG, "handleNIVerify action: " + intent.getAction());
        }
    }

    private void showNIError() {
        Toast.makeText((Context) this, (CharSequence) "NI error", (int) GPS_NO_RESPONSE_TIME_OUT).show();
    }
}
