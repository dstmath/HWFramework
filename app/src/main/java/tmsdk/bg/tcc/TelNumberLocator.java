package tmsdk.bg.tcc;

import android.content.Context;
import java.util.ArrayList;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.tcc.NumMarkerConsts;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdkobf.ms;

/* compiled from: Unknown */
public class TelNumberLocator {
    private static final String YELLOW_PAGE_NAME = "yd.sdb";
    private static TelNumberLocator mInstance;
    private Context mContext;
    private long object;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.bg.tcc.TelNumberLocator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.bg.tcc.TelNumberLocator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.bg.tcc.TelNumberLocator.<clinit>():void");
    }

    protected TelNumberLocator(Context context) {
        this.mContext = context;
        this.object = newObject();
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
            case NumMarkerConsts.DIFF_UPDATE_GET_BIN_DIFF_ERR /*-4*/:
                throw new OutOfMemoryError();
            case SpaceManager.ERROR_CODE_OK /*0*/:
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
        return patch(this.object, ms.a(this.mContext, UpdateConfig.LOCATION_NAME, null), str, str2);
    }

    public void reload() {
        try {
            String a = ms.a(this.mContext, UpdateConfig.LOCATION_NAME, null);
            String a2 = ms.a(this.mContext, YELLOW_PAGE_NAME, null);
            if (a != null || a2 != null) {
                throwIfError(init(this.object, a, a2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
