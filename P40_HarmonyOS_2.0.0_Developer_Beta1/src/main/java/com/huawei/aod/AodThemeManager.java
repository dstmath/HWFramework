package com.huawei.aod;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Slog;

public class AodThemeManager {
    private static final String AOD_PACKAGE_NAME = "com.huawei.aod";
    private static final String RES_TYPE_NAME = "drawable";
    private static final String TAG = "AodThemeManager";
    private static final int VERTICAL4_DUAL_INDEX = 4;
    private static final int VERTICAL4_RES_COUNT = 6;
    private static final int VERTICAL4_SINGLE_INDEX = 2;
    private static AodThemeManager sInstance = null;
    private int[] mAnalogDoubleClockIds = new int[6];
    private String[] mAnalogDoubleClockNames = new String[6];
    private int[] mAnalogSingleClockIds = new int[3];
    private String[] mAnalogSingleClockNames = new String[3];
    private String mAnimNum = "";
    private String mAnimationRes = "";
    private int mArtSignatureResNum = 1;
    private Bitmap mBackgroundBitmap = null;
    private Bitmap mBackgroundDualBitmap = null;
    private int mBackgroundResId = 0;
    private String mBackgroundResName = "";
    private String mBackgroundResName1 = "";
    private String mBackgroundResName2 = "";
    private int mBgFgdisplayPos = 0;
    private int mByteCount = 0;
    private int mClockThemeType = 2;
    private int mClockType = 20;
    private Context mContext = null;
    private int mDualByteCount = 0;
    private String[] mDualVertical4Names = new String[4];
    private boolean mIsAnimationable = false;
    private boolean mIsHaveSecondHand = false;
    private boolean mIsParsingFile = true;
    private boolean mIsPreSet = true;
    private boolean mIsProductCustomized = false;
    private boolean mIsResNameSolution = true;
    private boolean mIsVmallCustomized = false;
    private String[] mSingleVertical4Names = new String[2];
    private String mThemeName = "";

    private AodThemeManager() {
    }

    public static synchronized AodThemeManager getInstance() {
        AodThemeManager aodThemeManager;
        synchronized (AodThemeManager.class) {
            if (sInstance == null) {
                sInstance = new AodThemeManager();
            }
            aodThemeManager = sInstance;
        }
        return aodThemeManager;
    }

    public void initPreferenceManager(Context context) {
        this.mContext = context.getApplicationContext();
        if (this.mContext == null) {
            this.mContext = context;
        }
        if (this.mContext == null) {
            Slog.e(TAG, "initPreferenceManager for context invalid");
            return;
        }
        Slog.i(TAG, "initPreferenceManager begin");
        this.mIsParsingFile = true;
        parseThemeInfoFromSharedPreference();
        this.mIsParsingFile = false;
    }

    private void parseThemeInfoFromSharedPreference() {
        if (this.mContext == null) {
            Slog.w(TAG, "parseThemeInfoFromSharedPreference return for mContext invalid");
            return;
        }
        Slog.i(TAG, "parseThemeInfoFromSharedPreference with fileName : " + AodThemeConst.THEME_PREFERENCE);
        SharedPreferences spf = this.mContext.getSharedPreferences(AodThemeConst.THEME_PREFERENCE, 4);
        if (spf == null) {
            Slog.w(TAG, "parseThemeInfoFromSharedPreference return for sharedPreferences invalid");
            return;
        }
        if (spf.getInt(AodThemeConst.THEME_DEFINE_KEY, 0) == 0) {
            this.mIsResNameSolution = false;
        } else {
            this.mIsResNameSolution = true;
        }
        Slog.i(TAG, "parseThemeInfoFromSharedPreference with mIsResNameSolution : " + this.mIsResNameSolution);
        this.mClockType = spf.getInt(AodThemeConst.THEME_STYLE_KEY, 20);
        int i = this.mClockType;
        if (i != 1000) {
            if (i != 1021) {
                switch (i) {
                    default:
                        switch (i) {
                            case 101:
                            case 104:
                                this.mClockThemeType = 5;
                                dealWithBackgroundRes(spf);
                                break;
                            case 102:
                                break;
                            case 103:
                            case 105:
                            case 106:
                            case 107:
                            case 108:
                            case 109:
                            case 110:
                                break;
                            default:
                                this.mClockThemeType = 2;
                                break;
                        }
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                        this.mClockThemeType = 4;
                        dealWithBackgroundRes(spf);
                        break;
                }
            }
            this.mClockThemeType = 4;
            dealWithBackgroundResName(spf);
        } else {
            this.mClockThemeType = 3;
            dealWithAnalogSingleClockRes(spf);
            dealWithAnalogDualClockRes(spf);
        }
        this.mThemeName = spf.getString(AodThemeConst.THEME_STYLE_NAME, "");
        this.mAnimNum = spf.getString(AodThemeConst.ANIMATION_NUM, "");
        this.mIsPreSet = spf.getBoolean(AodThemeConst.THEME_PRELOAD_KEY, true);
        this.mIsProductCustomized = spf.getBoolean(AodThemeConst.PRODUCT_CUSTOMIZED, false);
        this.mIsVmallCustomized = spf.getBoolean(AodThemeConst.VMALL_CUSTOMIZED, false);
        this.mBgFgdisplayPos = spf.getInt(AodThemeConst.VERTICAL_BG_FG_TYPE, 0);
        this.mArtSignatureResNum = spf.getInt(AodThemeConst.ART_SIGNATURE_RES_NUM, 1);
        this.mIsAnimationable = spf.getBoolean(AodThemeConst.IS_ANIMATIONABLE, false);
        if (this.mIsAnimationable) {
            this.mAnimationRes = spf.getString(AodThemeConst.ANIMATION_RES, "");
            this.mByteCount = spf.getInt(AodThemeConst.BYTE_COUNT, 0);
            this.mDualByteCount = spf.getInt(AodThemeConst.BYTE_COUNT_DUAL, 0);
        }
    }

    private void dealWithAnalogSingleClockRes(SharedPreferences sharedPreferences) {
        String[] analogSingleClocks = sharedPreferences.getString(AodThemeConst.THEME_ANALOG_SINGLE_CLOCK_RES_KEY, "").split(",");
        Slog.i(TAG, "parseThemeInfoFromSharedPreference with analogSingleClocks : " + sharedPreferences.getString(AodThemeConst.THEME_ANALOG_SINGLE_CLOCK_RES_KEY, ""));
        if (analogSingleClocks.length >= 3) {
            if (this.mIsResNameSolution) {
                this.mAnalogSingleClockIds[0] = getResIdByResName(analogSingleClocks[1]);
                this.mAnalogSingleClockIds[1] = getResIdByResName(analogSingleClocks[2]);
                String[] strArr = this.mAnalogSingleClockNames;
                strArr[0] = analogSingleClocks[1];
                strArr[1] = analogSingleClocks[2];
            } else {
                this.mAnalogSingleClockIds[0] = getResIdByString(analogSingleClocks[1]);
                this.mAnalogSingleClockIds[1] = getResIdByString(analogSingleClocks[2]);
            }
            dealWithAnalogSingleSecondHand(analogSingleClocks);
        } else {
            int[] iArr = this.mAnalogSingleClockIds;
            iArr[0] = 0;
            iArr[1] = 0;
            iArr[2] = 0;
            String[] strArr2 = this.mAnalogSingleClockNames;
            strArr2[0] = "";
            strArr2[1] = "";
            strArr2[2] = "";
        }
        Slog.i(TAG, "parseThemeInfoFromSharedPreference with mAnalogSingleClockIds : " + this.mAnalogSingleClockIds[0] + "," + this.mAnalogSingleClockIds[1]);
    }

    private void dealWithAnalogSingleSecondHand(String[] analogSingleClocks) {
        if (analogSingleClocks.length == 4) {
            setSecondHandFlag(false);
            if (this.mIsResNameSolution) {
                this.mAnalogSingleClockIds[2] = getResIdByResName(analogSingleClocks[3]);
                this.mAnalogSingleClockNames[2] = analogSingleClocks[3];
                return;
            }
            this.mAnalogSingleClockIds[2] = getResIdByResName(analogSingleClocks[3]);
            return;
        }
        setSecondHandFlag(false);
    }

    private void dealWithAnalogDualClockRes(SharedPreferences sharedPreferences) {
        String[] analogDoubleClocks = sharedPreferences.getString(AodThemeConst.THEME_ANALOG_DOUBLE_CLOCK_RES_KEY, "").split(",");
        Slog.i(TAG, "parseThemeInfoFromSharedPreference with analogDoubleClocks : " + sharedPreferences.getString(AodThemeConst.THEME_ANALOG_DOUBLE_CLOCK_RES_KEY, ""));
        if (analogDoubleClocks.length == 6) {
            dealResForDoubleSize(analogDoubleClocks);
        } else if (analogDoubleClocks.length >= 3) {
            dealResForSingleSize(analogDoubleClocks);
            dealWithAnalogDoubleSecondHand(analogDoubleClocks);
        } else {
            int[] iArr = this.mAnalogDoubleClockIds;
            iArr[0] = 0;
            iArr[1] = 0;
            iArr[2] = 0;
            iArr[3] = 0;
            iArr[4] = 0;
            iArr[5] = 0;
            String[] strArr = this.mAnalogDoubleClockNames;
            strArr[0] = "";
            strArr[1] = "";
            strArr[2] = "";
            strArr[3] = "";
            strArr[4] = "";
            strArr[5] = "";
        }
        Slog.i(TAG, "parseThemeInfoFromSharedPreference with mAnalogDoubleClockIds : " + this.mAnalogDoubleClockIds[0] + "," + this.mAnalogDoubleClockIds[1] + " " + this.mAnalogDoubleClockIds[2] + " " + this.mAnalogDoubleClockIds[3]);
    }

    private void dealWithAnalogDoubleSecondHand(String[] analogDoubleClocks) {
        if (analogDoubleClocks.length == 4) {
            if (this.mIsResNameSolution) {
                int[] iArr = this.mAnalogDoubleClockIds;
                int hour2Index = iArr[2];
                int minute2Index = iArr[3];
                iArr[2] = getResIdByResName(analogDoubleClocks[3]);
                int[] iArr2 = this.mAnalogDoubleClockIds;
                iArr2[3] = hour2Index;
                iArr2[4] = minute2Index;
                String[] strArr = this.mAnalogDoubleClockNames;
                String hour2Str = strArr[2];
                String minute2Str = strArr[3];
                strArr[2] = analogDoubleClocks[3];
                strArr[3] = hour2Str;
                strArr[4] = minute2Str;
            } else {
                this.mAnalogDoubleClockIds[4] = getResIdByString(analogDoubleClocks[3]);
            }
            int[] iArr3 = this.mAnalogDoubleClockIds;
            iArr3[5] = iArr3[2];
            String[] strArr2 = this.mAnalogDoubleClockNames;
            strArr2[5] = strArr2[2];
        }
    }

    private void dealResForDoubleSize(String[] analogDoubleClocks) {
        if (this.mIsResNameSolution) {
            this.mAnalogDoubleClockIds[0] = getResIdByResName(analogDoubleClocks[1]);
            this.mAnalogDoubleClockIds[1] = getResIdByResName(analogDoubleClocks[2]);
            this.mAnalogDoubleClockIds[2] = getResIdByResName(analogDoubleClocks[4]);
            this.mAnalogDoubleClockIds[3] = getResIdByResName(analogDoubleClocks[5]);
            String[] strArr = this.mAnalogDoubleClockNames;
            strArr[0] = analogDoubleClocks[1];
            strArr[1] = analogDoubleClocks[2];
            strArr[2] = analogDoubleClocks[4];
            strArr[3] = analogDoubleClocks[5];
            return;
        }
        this.mAnalogDoubleClockIds[0] = getResIdByString(analogDoubleClocks[1]);
        this.mAnalogDoubleClockIds[1] = getResIdByString(analogDoubleClocks[2]);
        this.mAnalogDoubleClockIds[2] = getResIdByString(analogDoubleClocks[4]);
        this.mAnalogDoubleClockIds[3] = getResIdByString(analogDoubleClocks[5]);
    }

    private void dealResForSingleSize(String[] analogDoubleClocks) {
        if (this.mIsResNameSolution) {
            this.mAnalogDoubleClockIds[0] = getResIdByResName(analogDoubleClocks[1]);
            this.mAnalogDoubleClockIds[1] = getResIdByResName(analogDoubleClocks[2]);
            String[] strArr = this.mAnalogDoubleClockNames;
            strArr[0] = analogDoubleClocks[1];
            strArr[1] = analogDoubleClocks[2];
        } else {
            this.mAnalogDoubleClockIds[0] = getResIdByString(analogDoubleClocks[1]);
            this.mAnalogDoubleClockIds[1] = getResIdByString(analogDoubleClocks[2]);
        }
        int[] iArr = this.mAnalogDoubleClockIds;
        iArr[2] = iArr[0];
        iArr[3] = iArr[1];
        String[] strArr2 = this.mAnalogDoubleClockNames;
        strArr2[2] = strArr2[0];
        strArr2[3] = strArr2[1];
    }

    private void dealWithBackgroundRes(SharedPreferences sharedPreferences) {
        if (this.mIsResNameSolution) {
            this.mBackgroundResName = sharedPreferences.getString(AodThemeConst.THEME_BACKGROUND_KEY, "");
            if (getClockType() == 104) {
                String[] names = this.mBackgroundResName.split(",");
                if (names.length == 6) {
                    System.arraycopy(names, 2, this.mDualVertical4Names, 0, 4);
                    System.arraycopy(names, 0, this.mSingleVertical4Names, 0, 2);
                } else {
                    return;
                }
            } else {
                this.mBackgroundResId = getResIdByResName(this.mBackgroundResName);
            }
        } else {
            this.mBackgroundResId = sharedPreferences.getInt(AodThemeConst.THEME_BACKGROUND_KEY, 0);
        }
        Slog.i(TAG, "dealWithBackgroundRes get mBackgroundResId : " + this.mBackgroundResId);
    }

    private void dealWithBackgroundResName(SharedPreferences sharedPreferences) {
        if (this.mIsResNameSolution) {
            this.mBackgroundResName = sharedPreferences.getString(AodThemeConst.THEME_BACKGROUND_KEY, "");
            if (getClockType() == 102 || getClockType() == 1021) {
                this.mBackgroundResName1 = this.mBackgroundResName.split(",")[0];
                this.mBackgroundResName2 = this.mBackgroundResName.split(",")[1];
            }
        }
        Slog.i(TAG, "dealWithBackgroundResName get mBackgroundResName : " + this.mBackgroundResName);
    }

    public boolean getIsPreSet() {
        return this.mIsPreSet;
    }

    public int getClockThemeType() {
        return this.mClockThemeType;
    }

    public int getVerticalBgFgDisplayPos() {
        return this.mBgFgdisplayPos;
    }

    public int getClockType() {
        return this.mClockType;
    }

    public String getThemeName() {
        return this.mThemeName;
    }

    public String getAnimNum() {
        return this.mAnimNum;
    }

    public int[] getSingleAnalogClockIds() {
        return this.mAnalogSingleClockIds;
    }

    public int[] getDoubleAnalogClockIds() {
        return this.mAnalogDoubleClockIds;
    }

    public String[] getSingleAnalogClockNames() {
        return this.mAnalogSingleClockNames;
    }

    public String[] getDoubleAnalogClockNames() {
        return this.mAnalogDoubleClockNames;
    }

    public int getDigitalBackgroundResId() {
        return this.mBackgroundResId;
    }

    public String getDigitalBackgroundResName() {
        return this.mBackgroundResName;
    }

    public String[] getSingleVertical4ResNames() {
        return this.mSingleVertical4Names;
    }

    public String[] getDualVertical4ResNames() {
        return this.mDualVertical4Names;
    }

    public String getDigitalBackgroundResNames(boolean isDual) {
        return isDual ? this.mBackgroundResName2 : this.mBackgroundResName1;
    }

    public boolean getProductCustomized() {
        return this.mIsProductCustomized;
    }

    public boolean isVmallCustomized() {
        return this.mIsVmallCustomized;
    }

    public void deleteUserSharedPreferences(int userId) {
        if (this.mContext != null) {
            if (userId == 0) {
                Slog.w(TAG, " deleteUserSharedPreferences current is the owner");
                return;
            }
            String fileName = AodThemeConst.THEME_PREFERENCE + userId;
            Slog.i(TAG, "deleteUserSharedPreferences with fileName : " + fileName);
            this.mContext.getSharedPreferences(fileName, 4).edit().clear().commit();
        }
    }

    private int getResIdByResName(String resName) {
        if (this.mContext == null || TextUtils.isEmpty(resName)) {
            return 0;
        }
        return this.mContext.getResources().getIdentifier(resName, RES_TYPE_NAME, AOD_PACKAGE_NAME);
    }

    private int getResIdByString(String stringId) {
        try {
            return Integer.parseInt(stringId);
        } catch (NumberFormatException e) {
            Slog.e(TAG, "getResIdByString NumberFormatException for stringId is : " + stringId);
            return 0;
        }
    }

    public boolean isHaveSecondHand() {
        return this.mIsHaveSecondHand;
    }

    public void setSecondHandFlag(boolean isSecond) {
        this.mIsHaveSecondHand = isSecond;
    }

    public boolean getAnimationable() {
        return this.mIsAnimationable;
    }

    public String getAnimationRes() {
        return this.mAnimationRes;
    }

    public int getByteCount() {
        return this.mByteCount;
    }

    public int getDualByteCount() {
        return this.mDualByteCount;
    }
}
