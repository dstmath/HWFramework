package android.telephony;

public class AccessNetworkUtils {
    public static final int INVALID_BAND = -1;

    private AccessNetworkUtils() {
    }

    public static int getDuplexModeForEutranBand(int band) {
        if (band == -1 || band >= 68) {
            return 0;
        }
        if (band >= 65) {
            return 1;
        }
        if (band >= 47) {
            return 0;
        }
        if (band >= 33) {
            return 2;
        }
        if (band >= 1) {
            return 1;
        }
        return 0;
    }

    public static int getOperatingBandForEarfcn(int earfcn) {
        if (earfcn > 67535 || earfcn >= 67366) {
            return -1;
        }
        if (earfcn >= 66436) {
            return 66;
        }
        if (earfcn >= 65536) {
            return 65;
        }
        if (earfcn > 54339) {
            return -1;
        }
        if (earfcn >= 46790) {
            return 46;
        }
        if (earfcn >= 46590) {
            return 45;
        }
        if (earfcn >= 45590) {
            return 44;
        }
        if (earfcn >= 43590) {
            return 43;
        }
        if (earfcn >= 41590) {
            return 42;
        }
        if (earfcn >= 39650) {
            return 41;
        }
        if (earfcn >= 38650) {
            return 40;
        }
        if (earfcn >= 38250) {
            return 39;
        }
        if (earfcn >= 37750) {
            return 38;
        }
        if (earfcn >= 37550) {
            return 37;
        }
        if (earfcn >= 36950) {
            return 36;
        }
        if (earfcn >= 36350) {
            return 35;
        }
        if (earfcn >= 36200) {
            return 34;
        }
        if (earfcn >= 36000) {
            return 33;
        }
        if (earfcn > 10359 || earfcn >= 9920) {
            return -1;
        }
        if (earfcn >= 9870) {
            return 31;
        }
        if (earfcn >= 9770) {
            return 30;
        }
        if (earfcn >= 9660) {
            return -1;
        }
        if (earfcn >= 9210) {
            return 28;
        }
        if (earfcn >= 9040) {
            return 27;
        }
        if (earfcn >= 8690) {
            return 26;
        }
        if (earfcn >= 8040) {
            return 25;
        }
        if (earfcn >= 7700) {
            return 24;
        }
        if (earfcn >= 7500) {
            return 23;
        }
        if (earfcn >= 6600) {
            return 22;
        }
        if (earfcn >= 6450) {
            return 21;
        }
        if (earfcn >= 6150) {
            return 20;
        }
        if (earfcn >= 6000) {
            return 19;
        }
        if (earfcn >= 5850) {
            return 18;
        }
        if (earfcn >= 5730) {
            return 17;
        }
        if (earfcn > 5379) {
            return -1;
        }
        if (earfcn >= 5280) {
            return 14;
        }
        if (earfcn >= 5180) {
            return 13;
        }
        if (earfcn >= 5010) {
            return 12;
        }
        if (earfcn >= 4750) {
            return 11;
        }
        if (earfcn >= 4150) {
            return 10;
        }
        if (earfcn >= 3800) {
            return 9;
        }
        if (earfcn >= 3450) {
            return 8;
        }
        if (earfcn >= 2750) {
            return 7;
        }
        if (earfcn >= 2650) {
            return 6;
        }
        if (earfcn >= 2400) {
            return 5;
        }
        if (earfcn >= 1950) {
            return 4;
        }
        if (earfcn >= 1200) {
            return 3;
        }
        if (earfcn >= 600) {
            return 2;
        }
        if (earfcn >= 0) {
            return 1;
        }
        return -1;
    }
}
