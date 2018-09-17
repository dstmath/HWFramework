package com.android.internal.telephony;

public class MSimConstants {
    public static final int DEFAULT_CARD_INDEX = 0;
    public static final int DEFAULT_SUBSCRIPTION = 0;
    public static final int EVENT_SUBSCRIPTION_ACTIVATED = 500;
    public static final int EVENT_SUBSCRIPTION_DEACTIVATED = 501;
    public static final int MAX_PHONE_COUNT_DS = 2;
    public static final int NUM_SUBSCRIPTIONS = 2;
    public static final int RIL_CARD_MAX_APPS = 8;
    public static final int RIL_MAX_CARDS = 2;
    public static final int SUB1 = 0;
    public static final int SUB2 = 1;
    public static final String SUBSCRIPTION_KEY = "subscription";

    public enum CardUnavailableReason {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.MSimConstants.CardUnavailableReason.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.MSimConstants.CardUnavailableReason.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.MSimConstants.CardUnavailableReason.<clinit>():void");
        }
    }

    public MSimConstants() {
    }
}
