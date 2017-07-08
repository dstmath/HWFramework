package android.content.res;

import android.os.Parcel;

public interface IHwConfiguration {
    public static final int CLEARMODE_NO = 0;
    public static final int CLEARMODE_YES = 1;
    public static final int CLEAR_MODE = 4;
    public static final int SIMPLEUIMODE_DRAWER = 4;
    public static final int SIMPLEUIMODE_NO = 1;
    public static final int SIMPLEUIMODE_UNDEFINED = 0;
    public static final int SIMPLEUIMODE_YES = 2;
    public static final int SIMPLEUI_MODE = 2;
    public static final int THEME_MODE = 1;
    public static final int USERID_MODE = 3;

    int compareTo(IHwConfiguration iHwConfiguration);

    int diff(IHwConfiguration iHwConfiguration);

    boolean equals(IHwConfiguration iHwConfiguration);

    boolean equals(Object obj);

    int getConfigItem(int i);

    int hashCode();

    @Deprecated
    void makeDefault();

    void readFromParcel(Parcel parcel);

    void setConfigItem(int i, int i2);

    void setDensityDPI(int i);

    void setTo(IHwConfiguration iHwConfiguration);

    void setToDefaults();

    String toString();

    int updateFrom(IHwConfiguration iHwConfiguration);

    void writeToParcel(Parcel parcel, int i);
}
