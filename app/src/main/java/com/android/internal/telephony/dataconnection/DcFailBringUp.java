package com.android.internal.telephony.dataconnection;

import android.content.Intent;
import android.telephony.Rlog;

public class DcFailBringUp {
    static final String ACTION_FAIL_BRINGUP = "action_fail_bringup";
    static final String COUNTER = "counter";
    private static final boolean DBG = true;
    static final int DEFAULT_COUNTER = 2;
    static final DcFailCause DEFAULT_FAIL_CAUSE = null;
    static final int DEFAULT_SUGGESTED_RETRY_TIME = -1;
    static final String FAIL_CAUSE = "fail_cause";
    static final String INTENT_BASE = null;
    private static final String LOG_TAG = "DcFailBringUp";
    static final String SUGGESTED_RETRY_TIME = "suggested_retry_time";
    int mCounter;
    DcFailCause mFailCause;
    int mSuggestedRetryTime;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.dataconnection.DcFailBringUp.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.dataconnection.DcFailBringUp.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcFailBringUp.<clinit>():void");
    }

    void saveParameters(Intent intent, String s) {
        log(s + ".saveParameters: action=" + intent.getAction());
        this.mCounter = intent.getIntExtra(COUNTER, DEFAULT_COUNTER);
        this.mFailCause = DcFailCause.fromInt(intent.getIntExtra(FAIL_CAUSE, DEFAULT_FAIL_CAUSE.getErrorCode()));
        this.mSuggestedRetryTime = intent.getIntExtra(SUGGESTED_RETRY_TIME, DEFAULT_SUGGESTED_RETRY_TIME);
        log(s + ".saveParameters: " + this);
    }

    public void saveParameters(int counter, int failCause, int suggestedRetryTime) {
        this.mCounter = counter;
        this.mFailCause = DcFailCause.fromInt(failCause);
        this.mSuggestedRetryTime = suggestedRetryTime;
    }

    public String toString() {
        return "{mCounter=" + this.mCounter + " mFailCause=" + this.mFailCause + " mSuggestedRetryTime=" + this.mSuggestedRetryTime + "}";
    }

    private static void log(String s) {
        Rlog.d(LOG_TAG, s);
    }
}
