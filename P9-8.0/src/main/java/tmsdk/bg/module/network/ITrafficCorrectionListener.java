package tmsdk.bg.module.network;

import java.util.ArrayList;

public abstract class ITrafficCorrectionListener {
    public static final int Correct_Fail = -1;
    public static final int Correct_Null = -2;
    public static final int Correct_Succeed = 0;
    public static final int TC_Traffic4G = 3;
    public static final int TC_TrafficCommon = 1;
    public static final int TC_TrafficFree = 2;
    public static final int TSC_LeftKByte = 257;
    public static final int TSC_TotalKBytes = 259;
    public static final int TSC_UsedKBytes = 258;

    public void onCorrectionResult(int i, int i2) {
    }

    public void onDetailInfoNotify(ArrayList<DetailCategoryInfo> arrayList) {
    }

    public abstract void onError(int i, int i2);

    public abstract void onNeedSmsCorrection(int i, String str, String str2);

    public void onProfileNotify(int i, ProfileInfo profileInfo) {
    }

    public abstract void onTrafficInfoNotify(int i, int i2, int i3, int i4);
}
