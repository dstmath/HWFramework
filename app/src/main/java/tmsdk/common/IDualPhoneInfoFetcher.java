package tmsdk.common;

/* compiled from: Unknown */
public interface IDualPhoneInfoFetcher {
    public static final int FIRST_SIM_INDEX = 0;
    public static final int SECOND_SIM_INDEX = 1;

    String getIMSI(int i);
}
