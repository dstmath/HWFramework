package android.telephony;

import android.database.Cursor;

public class CallerInfoHW {
    public static final int MIN_MATCH = 7;
    private static final String TAG = "CallerInfo";
    private static final CallerInfoHW sInstance = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.CallerInfoHW.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.CallerInfoHW.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.CallerInfoHW.<clinit>():void");
    }

    public static synchronized CallerInfoHW getInstance() {
        CallerInfoHW callerInfoHW;
        synchronized (CallerInfoHW.class) {
            callerInfoHW = sInstance;
        }
        return callerInfoHW;
    }

    public String getCountryIsoFromDbNumber(String number) {
        return huawei.android.telephony.CallerInfoHW.getInstance().getCountryIsoFromDbNumber(number);
    }

    public int getIntlPrefixAndCCLen(String number) {
        return huawei.android.telephony.CallerInfoHW.getInstance().getIntlPrefixAndCCLen(number);
    }

    public boolean compareNums(String num1, String netIso1, String num2, String netIso2) {
        return huawei.android.telephony.CallerInfoHW.getInstance().compareNums(num1, netIso1, num2, netIso2);
    }

    public boolean compareNums(String num1, String num2) {
        return huawei.android.telephony.CallerInfoHW.getInstance().compareNums(num1, num2);
    }

    public int getCallerIndex(Cursor cursor, String compNum) {
        return huawei.android.telephony.CallerInfoHW.getInstance().getCallerIndex(cursor, compNum);
    }

    public int getCallerIndex(Cursor cursor, String compNum, String columnName) {
        return huawei.android.telephony.CallerInfoHW.getInstance().getCallerIndex(cursor, compNum, columnName);
    }

    public int getCallerIndex(Cursor cursor, String compNum, String columnName, String countryIso) {
        return huawei.android.telephony.CallerInfoHW.getInstance().getCallerIndex(cursor, compNum, columnName, countryIso);
    }
}
