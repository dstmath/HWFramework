package com.huawei.g11n.tmr.phonenumber;

public class SearchPhoneNumber {
    private static volatile AbstractPhoneNumberMatcher instance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.g11n.tmr.phonenumber.SearchPhoneNumber.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.g11n.tmr.phonenumber.SearchPhoneNumber.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.g11n.tmr.phonenumber.SearchPhoneNumber.<clinit>():void");
    }

    private static synchronized AbstractPhoneNumberMatcher getInstance(String str) {
        AbstractPhoneNumberMatcher abstractPhoneNumberMatcher;
        synchronized (SearchPhoneNumber.class) {
            if (instance == null) {
                instance = new PhoneNumberMatcher(str);
            } else if (!instance.getCountry().equals(str.trim())) {
                instance = new PhoneNumberMatcher(str);
            }
            abstractPhoneNumberMatcher = instance;
        }
        return abstractPhoneNumberMatcher;
    }

    public static int[] getMatchedPhoneNumber(String str, String str2) {
        return getInstance(str2).getMatchedPhoneNumber(str, str2);
    }
}
