package com.huawei.android.provider;

import android.net.Uri;
import android.telephony.MSimTelephonyConstants;
import java.util.HashMap;

public final class IccProviderUtilsEx {
    private static HashMap<String, Integer> indexColumn;
    private static HashMap<String, String> simAnr;
    private static HashMap<String, Uri> simProviderUri;
    private static HashMap<String, Uri> usimProviderUri;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.provider.IccProviderUtilsEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.provider.IccProviderUtilsEx.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.provider.IccProviderUtilsEx.<clinit>():void");
    }

    public static final HashMap getIndexColumn() {
        indexColumn.put("INDEX_NAME_COLUMN", Integer.valueOf(0));
        indexColumn.put("INDEX_NUMBER_COLUMN", Integer.valueOf(1));
        indexColumn.put("INDEX_EMAILS_COLUMN", Integer.valueOf(2));
        indexColumn.put("INDEX_EFID_COLUMN", Integer.valueOf(3));
        indexColumn.put("INDEX_SIM_INDEX_COLUMN", Integer.valueOf(4));
        indexColumn.put("INDEX_ANRS_COLUMN", Integer.valueOf(5));
        return indexColumn;
    }

    public static final HashMap getSimProviderUri() {
        simProviderUri.put("sFirstSimProviderUri", Uri.parse("content://icc/adn/subId/0"));
        simProviderUri.put("sSecondSimProviderUri", Uri.parse("content://icc/adn/subId/1"));
        return simProviderUri;
    }

    public static final HashMap getUSimProviderUri() {
        usimProviderUri.put("sSingleUSimProviderUri", null);
        usimProviderUri.put("sFirstUSimProviderUri", null);
        usimProviderUri.put("sSecondUSimProviderUri", null);
        return usimProviderUri;
    }

    public static final HashMap getSimAnr() {
        simAnr.put("SIM_ANR", MSimTelephonyConstants.SIM_PHONEBOOK_COLIMN_NAME_ANR);
        simAnr.put("SIM_NEW_ANR", MSimTelephonyConstants.SIM_PHONEBOOK_COLIMN_NAME_NEW_ANR);
        return simAnr;
    }
}
