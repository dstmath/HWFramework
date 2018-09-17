package android.telephony;

public class HwCustSignalStrength {
    public int getGsmSignalStrength(int mGsmSignalStrength) {
        return mGsmSignalStrength;
    }

    public int getGsmDbm(int mGsmSignalStrength) {
        return 0;
    }

    public boolean isDocomo() {
        return false;
    }
}
