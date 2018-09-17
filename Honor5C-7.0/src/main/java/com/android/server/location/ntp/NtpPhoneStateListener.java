package com.android.server.location.ntp;

import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.util.Log;

public class NtpPhoneStateListener extends PhoneStateListener {
    private static boolean DBG = false;
    private static final String TAG = "NtpPhoneStateListener";
    private boolean mIsCdma;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.ntp.NtpPhoneStateListener.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.ntp.NtpPhoneStateListener.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.ntp.NtpPhoneStateListener.<clinit>():void");
    }

    public NtpPhoneStateListener(int subId) {
        super(subId);
        this.mIsCdma = false;
        if (DBG) {
            Log.d(TAG, "NtpPhoneStateListener create subId:" + subId);
        }
    }

    public void onServiceStateChanged(ServiceState state) {
        if (state != null) {
            this.mIsCdma = ServiceState.isCdma(state.getRilVoiceRadioTechnology());
            if (DBG) {
                Log.d(TAG, "onServiceStateChanged subId:" + this.mSubId + " isCdma=" + this.mIsCdma);
            }
        }
    }

    public boolean isCdma() {
        return this.mIsCdma;
    }
}
