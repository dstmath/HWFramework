package com.android.internal.telephony;

import android.text.TextUtils;
import android.util.Log;
import java.util.Locale;

public class HwCustMccTableImpl extends HwCustMccTable {
    private static final boolean IS_CUST_LOCALE_CONFIG = false;
    private static final String LOG_TAG = "MccTable";
    private static final String[] MEXICO_MCC = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwCustMccTableImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwCustMccTableImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwCustMccTableImpl.<clinit>():void");
    }

    public Locale getCustSpecialLocaleConfig(String imsi) {
        if (!IS_CUST_LOCALE_CONFIG || TextUtils.isEmpty(imsi)) {
            return null;
        }
        for (String startsWith : MEXICO_MCC) {
            if (imsi.startsWith(startsWith)) {
                Log.d(LOG_TAG, "Mexico special locale config, set default language to es_mx");
                return new Locale("es", "mx");
            }
        }
        return null;
    }
}
