package com.android.internal.telephony;

public class RoamingBroker {
    public static final String PreviousIccId = "persist.radio.previousiccid";
    public static final String PreviousOperator = "persist.radio.previousopcode";
    public static final String RBActivated = "gsm.RBActivated";
    private huawei.com.android.internal.telephony.RoamingBroker mRoamingBroker;

    private RoamingBroker(huawei.com.android.internal.telephony.RoamingBroker roamingBroker) {
        this.mRoamingBroker = roamingBroker;
    }

    public static RoamingBroker getDefault() {
        return HelperHolder.INSTANCE;
    }

    public static boolean isRoamingBrokerActivated() {
        return huawei.com.android.internal.telephony.RoamingBroker.isRoamingBrokerActivated();
    }

    public static String updateSelectionForRoamingBroker(String selection) {
        return huawei.com.android.internal.telephony.RoamingBroker.updateSelectionForRoamingBroker(selection);
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

    public void setOperator(String operatorCode) {
        this.mRoamingBroker.setOperator(operatorCode);
    }

    public void setIccId(String IccId) {
        this.mRoamingBroker.setIccId(IccId);
    }

    private static class HelperHolder {
        private static final RoamingBroker INSTANCE = new RoamingBroker(huawei.com.android.internal.telephony.RoamingBroker.getDefault());

        private HelperHolder() {
        }
    }
}
