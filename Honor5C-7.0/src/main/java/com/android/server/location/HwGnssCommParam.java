package com.android.server.location;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.util.Log;

public class HwGnssCommParam {
    public static final String ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE";
    private static final int CONNECTED = 1;
    private static final boolean DEBUG = false;
    private static final int DISABLE = 0;
    private static final int DISCONNECTED = 0;
    private static final int ENABLE = 1;
    private static final String ORIENTATION_LANDSCAPE = "ORIENTATION_LANDSCAPE";
    private static final String ORIENTATION_PORTRAIT = "ORIENTATION_PORTRAIT";
    private static final String ORIENTATION_UNKNOWN = "ORIENTATION_UNKNOWN";
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_DISCONNECTING = 3;
    private static final String TAG = "HwGnssLog_CommParam";
    public static final String USB_CONNECTED = "connected";
    private static final boolean VERBOSE = false;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private int mUsbConnectState;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.HwGnssCommParam.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.HwGnssCommParam.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.HwGnssCommParam.<clinit>():void");
    }

    public HwGnssCommParam(Context context) {
        this.mUsbConnectState = STATE_DISCONNECTED;
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equals(HwGnssCommParam.ACTION_USB_STATE)) {
                    if (intent.getBooleanExtra(HwGnssCommParam.USB_CONNECTED, HwGnssCommParam.DEBUG)) {
                        HwGnssCommParam.this.mUsbConnectState = HwGnssCommParam.STATE_CONNECTING;
                    } else {
                        HwGnssCommParam.this.mUsbConnectState = HwGnssCommParam.STATE_DISCONNECTED;
                    }
                }
            }
        };
        this.mContext = context;
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_USB_STATE);
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    public String getScreenOrientation() {
        int ori = this.mContext.getResources().getConfiguration().orientation;
        String oriRes = ORIENTATION_UNKNOWN;
        if (ori == STATE_CONNECTED) {
            oriRes = ORIENTATION_LANDSCAPE;
        } else if (ori == STATE_CONNECTING) {
            oriRes = ORIENTATION_PORTRAIT;
        }
        if (DEBUG) {
            Log.d(TAG, "screen orientation is : " + oriRes);
        }
        return oriRes;
    }

    public int getBtSwitchState() {
        int btSwitch = STATE_DISCONNECTED;
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Log.d(TAG, "bluetooth adapter is not avaibable!");
        } else if (btAdapter.isEnabled()) {
            btSwitch = STATE_CONNECTING;
        }
        if (DEBUG) {
            Log.d(TAG, "bt switch state is : " + btSwitch);
        }
        return btSwitch;
    }

    public int getBtConnectionState() {
        int btConnectState = STATE_DISCONNECTED;
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Log.d(TAG, "bluetooth adapter is not avaibable!");
        } else if (btAdapter.getConnectionState() == STATE_CONNECTED) {
            btConnectState = STATE_CONNECTING;
        }
        if (DEBUG) {
            Log.d(TAG, "bt connection state is : " + btConnectState);
        }
        return btConnectState;
    }

    public int getNfcSwitchState() {
        int nfcSwitch = STATE_DISCONNECTED;
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
        if (nfcAdapter == null) {
            Log.d(TAG, "nfc adapter is not avaibable!");
        } else if (nfcAdapter.isEnabled()) {
            nfcSwitch = STATE_CONNECTING;
        }
        if (DEBUG) {
            Log.d(TAG, "NFC switch state is : " + nfcSwitch);
        }
        return nfcSwitch;
    }

    public int getUsbConnectState() {
        return this.mUsbConnectState;
    }
}
