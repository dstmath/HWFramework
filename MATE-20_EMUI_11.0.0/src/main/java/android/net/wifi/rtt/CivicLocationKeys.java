package android.net.wifi.rtt;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CivicLocationKeys {
    public static final int ADDITIONAL_CODE = 32;
    public static final int APT = 26;
    public static final int BOROUGH = 4;
    public static final int BRANCH_ROAD_NAME = 36;
    public static final int BUILDING = 25;
    public static final int CITY = 3;
    public static final int COUNTY = 2;
    public static final int DESK = 33;
    public static final int FLOOR = 27;
    public static final int GROUP_OF_STREETS = 6;
    public static final int HNO = 19;
    public static final int HNS = 20;
    public static final int LANGUAGE = 0;
    public static final int LMK = 21;
    public static final int LOC = 22;
    public static final int NAM = 23;
    public static final int NEIGHBORHOOD = 5;
    public static final int PCN = 30;
    public static final int POD = 17;
    public static final int POSTAL_CODE = 24;
    public static final int PO_BOX = 31;
    public static final int PRD = 16;
    public static final int PRIMARY_ROAD_NAME = 34;
    public static final int ROAD_SECTION = 35;
    public static final int ROOM = 28;
    public static final int SCRIPT = 128;
    public static final int STATE = 1;
    public static final int STREET_NAME_POST_MODIFIER = 39;
    public static final int STREET_NAME_PRE_MODIFIER = 38;
    public static final int STS = 18;
    public static final int SUBBRANCH_ROAD_NAME = 37;
    public static final int TYPE_OF_PLACE = 29;

    @Retention(RetentionPolicy.SOURCE)
    public @interface CivicLocationKeysType {
    }

    private CivicLocationKeys() {
    }
}
