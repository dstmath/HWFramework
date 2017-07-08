package com.android.internal.telephony.imsphone;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;

public class HwCustImsPhoneCallTrackerImpl extends HwCustImsPhoneCallTracker {
    private static final String BOARD_PLATFORM_TAG = "ro.board.platform";
    private static final boolean IS_VDF = false;
    private static final String PLATFORM_QUALCOMM = "msm";
    private Context mContext;
    private TelephonyManager mTelephonyManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.imsphone.HwCustImsPhoneCallTrackerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.imsphone.HwCustImsPhoneCallTrackerImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.HwCustImsPhoneCallTrackerImpl.<clinit>():void");
    }

    public HwCustImsPhoneCallTrackerImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public boolean checkImsRegistered() {
        if (IS_VDF && isQcomPlatform() && this.mContext != null) {
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
            if (this.mTelephonyManager != null) {
                return this.mTelephonyManager.isImsRegistered();
            }
        }
        return true;
    }

    private boolean isQcomPlatform() {
        return SystemProperties.get(BOARD_PLATFORM_TAG, "").startsWith(PLATFORM_QUALCOMM);
    }
}
