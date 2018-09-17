package tmsdk.bg.tcc;

import android.content.Context;
import java.util.ArrayList;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.update.UpdateConfig;
import tmsdkobf.lu;

public class TelNumberLocator {
    private static final String YELLOW_PAGE_NAME = "yd.sdb";
    private static TelNumberLocator mInstance = null;
    private Context mContext;
    private long object = newObject();

    static {
        TMSDKContext.registerNatives(3, TelNumberLocator.class);
    }

    protected TelNumberLocator(Context context) {
        this.mContext = context;
        if (0 == this.object) {
            throw new OutOfMemoryError();
        }
        reload();
    }

    private static native void deleteObject(long j);

    private static native int[] getAreaCode(long j);

    private static native int getAreaCodeLocation(long j, int i, StringBuffer stringBuffer);

    private static native int getCityNameList(long j, String str, ArrayList<String> arrayList);

    private static native int[] getCountryCode(long j);

    private static native int getCountryCodeLocation(long j, int i, StringBuffer stringBuffer);

    public static synchronized TelNumberLocator getDefault(Context context) {
        TelNumberLocator telNumberLocator;
        synchronized (TelNumberLocator.class) {
            if (mInstance == null) {
                mInstance = new TelNumberLocator(context);
            }
            telNumberLocator = mInstance;
        }
        return telNumberLocator;
    }

    private static native int getDetailYellowPages(long j, ArrayList<Integer> arrayList, ArrayList<String> arrayList2, ArrayList<String> arrayList3, ArrayList<String> arrayList4);

    private static native int getLocation(long j, StringBuffer stringBuffer, StringBuffer stringBuffer2, StringBuffer stringBuffer3, String str, boolean z);

    private static native int getMobileNumLocation(long j, int i, StringBuffer stringBuffer);

    private static native int getProvinceNameList(long j, ArrayList<String> arrayList);

    private static native int getYellowPages(long j, ArrayList<String> arrayList, ArrayList<String> arrayList2);

    private static native int init(long j, String str, String str2);

    private static native long newObject();

    private static native int patch(long j, String str, String str2, String str3);

    private void throwIfError(int i) {
        switch (i) {
            case -4:
                throw new OutOfMemoryError();
            case 0:
                return;
            default:
                throw new TelNumberLocatorException(i);
        }
    }

    protected void finalize() {
        if (this.object != 0) {
            deleteObject(this.object);
        }
        this.object = 0;
    }

    public int[] getAreaCode() {
        return getAreaCode(this.object);
    }

    public String getAreaCodeLocation(int i) {
        StringBuffer stringBuffer = new StringBuffer();
        int areaCodeLocation = getAreaCodeLocation(this.object, i, stringBuffer);
        if (areaCodeLocation < 0) {
            if (areaCodeLocation == -1) {
                return "";
            }
            throwIfError(areaCodeLocation);
        }
        return stringBuffer.toString();
    }

    public ArrayList<String> getCityNameList(String str) {
        ArrayList<String> arrayList = new ArrayList();
        throwIfError(getCityNameList(this.object, str, arrayList));
        return arrayList;
    }

    public int[] getCountryCode() {
        return getCountryCode(this.object);
    }

    public String getCountryCodeLocation(int i) {
        StringBuffer stringBuffer = new StringBuffer();
        int countryCodeLocation = getCountryCodeLocation(this.object, i, stringBuffer);
        if (countryCodeLocation < 0) {
            if (countryCodeLocation == -1) {
                return "";
            }
            throwIfError(countryCodeLocation);
        }
        return stringBuffer.toString();
    }

    public boolean getDetailYellowPages(ArrayList<Integer> arrayList, ArrayList<String> arrayList2, ArrayList<String> arrayList3, ArrayList<String> arrayList4) {
        if (arrayList2 == null || arrayList3 == null) {
            return false;
        }
        throwIfError(getDetailYellowPages(this.object, arrayList, arrayList2, arrayList3, arrayList4));
        return true;
    }

    public void getLocation(StringBuffer stringBuffer, StringBuffer stringBuffer2, StringBuffer stringBuffer3, String str, boolean z) {
        int location = getLocation(this.object, stringBuffer, stringBuffer2, stringBuffer3, str, z);
        if (location < 0) {
            if (location != -1) {
                throwIfError(location);
                return;
            }
            stringBuffer.replace(0, stringBuffer.length(), "");
            stringBuffer2.replace(0, stringBuffer2.length(), "");
            stringBuffer3.replace(0, stringBuffer3.length(), "");
        }
    }

    public String getMobileNumLocation(int i) {
        StringBuffer stringBuffer = new StringBuffer();
        int mobileNumLocation = getMobileNumLocation(this.object, i, stringBuffer);
        if (mobileNumLocation < 0) {
            if (mobileNumLocation == -1) {
                return "";
            }
            throwIfError(mobileNumLocation);
        }
        return stringBuffer.toString();
    }

    public ArrayList<String> getProvinceNameList() {
        ArrayList<String> arrayList = new ArrayList();
        throwIfError(getProvinceNameList(this.object, arrayList));
        return arrayList;
    }

    public boolean getYellowPages(ArrayList<String> arrayList, ArrayList<String> arrayList2) {
        if (arrayList == null || arrayList2 == null) {
            return false;
        }
        throwIfError(getYellowPages(this.object, arrayList, arrayList2));
        return true;
    }

    public int patchLocation(String str, String str2) {
        return patch(this.object, lu.b(this.mContext, UpdateConfig.LOCATION_NAME, null), str, str2);
    }

    public void reload() {
        try {
            String b = lu.b(this.mContext, UpdateConfig.LOCATION_NAME, null);
            String b2 = lu.b(this.mContext, YELLOW_PAGE_NAME, null);
            if (b != null || b2 != null) {
                throwIfError(init(this.object, b, b2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
