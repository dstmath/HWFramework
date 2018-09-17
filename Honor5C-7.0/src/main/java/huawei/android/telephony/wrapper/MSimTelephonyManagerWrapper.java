package huawei.android.telephony.wrapper;

public interface MSimTelephonyManagerWrapper {
    int getCurrentPhoneType(int i);

    int getDefaultSubscription();

    int getMmsAutoSetDataSubscription();

    int getNetworkType(int i);

    int getPhoneCount();

    int getPreferredDataSubscription();

    String getVoiceMailNumber(int i);

    boolean hasIccCard(int i);

    boolean isMultiSimEnabled();

    boolean isNetworkRoaming(int i);

    boolean setMmsAutoSetDataSubscription(int i);

    boolean setPreferredDataSubscription(int i);
}
