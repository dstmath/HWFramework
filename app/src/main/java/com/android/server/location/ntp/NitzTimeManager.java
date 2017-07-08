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
    private static boolean DBG = false;
    private static final long EXPIRT_TIME = 86400000;
    private static final int SUB_NUMS = 0;
    private static final String TAG = "NtpNitzTimeManager";
    private Context mContext;
    private BroadcastReceiver mNitzReceiver;
    private ArrayList<NtpPhoneStateListener> mPhoneStateListenerList;
    private TimeManager mTimeManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.ntp.NitzTimeManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.ntp.NitzTimeManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.ntp.NitzTimeManager.<clinit>():void");
    }

    public NitzTimeManager(Context context) {
        this.mNitzReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    if ("android.intent.action.NETWORK_SET_TIME".equals(intent.getAction())) {
                        NitzTimeManager.this.mTimeManager.setCurrentTime(System.currentTimeMillis(), SystemClock.elapsedRealtime());
                    }
                }
            }
        };
        this.mContext = context;
        this.mTimeManager = new TimeManager(TAG, EXPIRT_TIME);
        registerForTelephonyIntents();
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (telephonyManager != null) {
            this.mPhoneStateListenerList = new ArrayList();
            if (SUB_NUMS != 0) {
                for (int i = SUB_NUMS; i < SUB_NUMS; i++) {
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
        for (NtpPhoneStateListener listener : this.mPhoneStateListenerList) {
            isCdma = !isCdma ? listener.isCdma() : true;
        }
        if (DBG) {
            Log.d(TAG, "isCdma:" + isCdma);
        }
        return isCdma;
    }
}
