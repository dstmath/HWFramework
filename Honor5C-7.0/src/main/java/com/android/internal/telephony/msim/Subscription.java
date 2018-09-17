package com.android.internal.telephony.msim;

import com.huawei.android.util.NoExtAPIException;

public final class Subscription {
    public static final int SUBSCRIPTION_INDEX_INVALID = -1;
    public String appId;
    public String appLabel;
    public String appType;
    public String iccId;
    public int m3gpp2Index;
    public int m3gppIndex;
    public int slotId;
    public int subId;
    public SubscriptionStatus subStatus;

    public enum SubscriptionStatus {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.msim.Subscription.SubscriptionStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.msim.Subscription.SubscriptionStatus.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.msim.Subscription.SubscriptionStatus.<clinit>():void");
        }
    }

    public Subscription() {
        throw new NoExtAPIException("method not supported.");
    }

    public String toString() {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean equals(Subscription sub) {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean isSame(Subscription sub) {
        throw new NoExtAPIException("method not supported.");
    }

    public void clear() {
        throw new NoExtAPIException("method not supported.");
    }

    public Subscription copyFrom(Subscription from) {
        throw new NoExtAPIException("method not supported.");
    }

    public int getAppIndex() {
        throw new NoExtAPIException("method not supported.");
    }

    public int getLength() {
        throw new NoExtAPIException("method not supported.");
    }
}
