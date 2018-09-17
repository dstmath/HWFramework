package com.android.internal.telephony;

public class RoamingBroker {
    public static final String PreviousIccId = "persist.radio.previousiccid";
    public static final String PreviousOperator = "persist.radio.previousopcode";
    public static final String RBActivated = "gsm.RBActivated";
    private huawei.com.android.internal.telephony.RoamingBroker mRoamingBroker;

    private static class HelperHolder {
        private static RoamingBroker mInstance;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.RoamingBroker.HelperHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.RoamingBroker.HelperHolder.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RoamingBroker.HelperHolder.<clinit>():void");
        }

        private HelperHolder() {
        }
    }

    /* synthetic */ RoamingBroker(huawei.com.android.internal.telephony.RoamingBroker roamingBroker, RoamingBroker roamingBroker2) {
        this(roamingBroker);
    }

    private RoamingBroker(huawei.com.android.internal.telephony.RoamingBroker roamingBroker) {
        this.mRoamingBroker = roamingBroker;
    }

    public static RoamingBroker getDefault() {
        return HelperHolder.mInstance;
    }

    public static boolean isRoamingBrokerActivated() {
        return huawei.com.android.internal.telephony.RoamingBroker.isRoamingBrokerActivated();
    }

    public static String updateSelectionForRoamingBroker(String selection) {
        return huawei.com.android.internal.telephony.RoamingBroker.updateSelectionForRoamingBroker(selection);
    }

    public void setOperator(String operatorCode) {
        this.mRoamingBroker.setOperator(operatorCode);
    }

    public void setIccId(String IccId) {
        this.mRoamingBroker.setIccId(IccId);
    }

    public static String getRBOperatorNumeric() {
        return huawei.com.android.internal.telephony.RoamingBroker.getRBOperatorNumeric();
    }

    public static boolean isRoamingBrokerActivated(int slotId) {
        return huawei.com.android.internal.telephony.RoamingBroker.getDefault(Integer.valueOf(slotId)).isRoamingBrokerActivated(Integer.valueOf(slotId));
    }

    public static String updateSelectionForRoamingBroker(String selection, int slotId) {
        return huawei.com.android.internal.telephony.RoamingBroker.getDefault(Integer.valueOf(slotId)).updateSelectionForRoamingBroker(selection, slotId);
    }
}
