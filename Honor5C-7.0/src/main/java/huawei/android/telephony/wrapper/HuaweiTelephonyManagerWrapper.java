package huawei.android.telephony.wrapper;

public interface HuaweiTelephonyManagerWrapper {
    public static final int CT_NATIONAL_ROAMING_CARD = 41;
    public static final int CU_DUAL_MODE_CARD = 42;
    public static final int DUAL_MODE_CG_CARD = 40;
    public static final int DUAL_MODE_UG_CARD = 50;
    public static final int SINGLE_MODE_RUIM_CARD = 30;
    public static final int SINGLE_MODE_SIM_CARD = 10;
    public static final int SINGLE_MODE_USIM_CARD = 20;
    public static final int UNKNOWN_CARD = -1;

    int getCardType(int i);

    int getDualCardMode();

    int getSubidFromSlotId(int i);
}
