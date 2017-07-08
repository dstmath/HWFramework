package com.android.server;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.ITrustedUIService.Stub;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.am.ProcessList;

public class TrustedUIService extends Stub {
    private static final String TAG = "TrustedUIService";
    private static boolean mTUIStatus;
    private final Context mContext;
    private TUIEventListener mListener;
    private final PhoneStateListener mPhoneStateListener;
    private final TelephonyManager mTelephonyManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.TrustedUIService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.TrustedUIService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.TrustedUIService.<clinit>():void");
    }

    private native int nativeSendTUICmd(int i, int i2);

    private native void nativeSendTUIExitCmd();

    private native void nativeTUILibraryDeInit();

    private native boolean nativeTUILibraryInit();

    public TrustedUIService(Context context) {
        this.mContext = context;
        this.mListener = new TUIEventListener(this, context);
        new Thread(this.mListener, TUIEventListener.class.getName()).start();
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == 1) {
                    Log.d(TrustedUIService.TAG, " PhoneStateListener: CALL_STATE_RINGING, mTUIStatus: " + TrustedUIService.mTUIStatus);
                    if (TrustedUIService.mTUIStatus) {
                        TrustedUIService.this.sendTUIExitCmd();
                    }
                }
            }
        };
        this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
    }

    public void setTrustedUIStatus(boolean status) {
        Log.d(TAG, " setTrustedUIStatus: " + status);
        mTUIStatus = status;
    }

    public boolean getTrustedUIStatus() {
        Log.d(TAG, " getTrustedUIStatus: " + mTUIStatus);
        if (Binder.getCallingUid() == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            return mTUIStatus;
        }
        throw new SecurityException("getTrustedUIStatus should only be called by TrustedUIService");
    }

    public void sendTUIExitCmd() {
        if (Binder.getCallingUid() != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            throw new SecurityException("sendTUIExitCmd should only be called by TrustedUIService");
        }
        nativeSendTUIExitCmd();
        this.mContext.sendBroadcast(new Intent("com.huawei.secime.HIDE_WINDOW"));
    }

    public int sendTUICmd(int event_type, int value) {
        if (Binder.getCallingUid() != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
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
