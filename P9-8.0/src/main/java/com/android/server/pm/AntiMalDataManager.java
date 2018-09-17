package com.android.server.pm;

import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.security.deviceusage.HwOEMInfoAdapter;
import com.android.server.security.securitydiagnose.AntiMalApkInfo;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class AntiMalDataManager {
    private static final String ANTIMAL_DATA_FILE = "AntiMalData.xml";
    private static final String APPS = "apps";
    private static final String BOOT_TIMES = "BootTimes";
    private static final String COMPONENT = "component";
    private static final String COUNTER = "counter";
    private static final String FASTBOOT_STATUS = "ro.boot.flash.locked";
    private static final boolean HW_DEBUG;
    private static final int MAX_BOOT_TIMES = 10;
    private static final int OEMINFO_ENABLE_RETREAD = 163;
    private static final int OEMINFO_ENABLE_RETREAD_SIZE = 40;
    private static final String STATUS = "status";
    private static final String SYSTEM_CUST_STATUS_PRO = "ro.sys.wp";
    private static final String TAG = "AntiMalDataManager";
    private static final String TAG_ITEM = "item";
    private Status mCurAntiMalStatus = new Status();
    private ArrayList<AntiMalApkInfo> mCurApkInfoList = new ArrayList();
    private ArrayList<AntiMalComponentInfo> mCurComponentList = new ArrayList();
    private AntiMalCounter mCurCounter = new AntiMalCounter();
    private long mDeviceFirstUseTime;
    private Status mOldAntiMalStatus;
    private ArrayList<AntiMalApkInfo> mOldApkInfoList;
    private ArrayList<AntiMalComponentInfo> mOldComponentList = new ArrayList();
    private AntiMalCounter mOldCounter;
    private boolean mOtaBoot;

    private static class AntiMalCounter {
        int mAddCnt;
        int mBootCnt;
        int mDeleteCnt;
        int mModifiedCnt;

        public AntiMalCounter(int deleteCnt, int addCnt, int modifyCnt, int bootCnt) {
            this.mDeleteCnt = deleteCnt;
            this.mModifiedCnt = modifyCnt;
            this.mAddCnt = addCnt;
            this.mBootCnt = bootCnt;
        }

        public boolean equals(Object in) {
            boolean z = false;
            if (in == null || !(in instanceof AntiMalCounter)) {
                return false;
            }
            AntiMalCounter other = (AntiMalCounter) in;
            if (this.mAddCnt == other.mAddCnt && this.mDeleteCnt == other.mDeleteCnt && this.mModifiedCnt == other.mModifiedCnt) {
                z = true;
            }
            return z;
        }

        public String toString() {
            return "Delete : " + this.mDeleteCnt + " Modify : " + this.mModifiedCnt + " Add : " + this.mAddCnt + " Boot time : " + this.mBootCnt;
        }

        public int hashCode() {
            return super.hashCode();
        }

        boolean hasAbnormalApks() {
            return (this.mDeleteCnt + this.mModifiedCnt) + this.mAddCnt > 0;
        }
    }

    private static class Status {
        int mCustSysStatus;
        String mDeviceFirstUseTime;
        int mFastbootStatus;
        int mRootStatus;
        int mSeLinuxStatus;
        String mSecPatchVer;
        int mVerfybootStatus;

        /* synthetic */ Status(Status -this0) {
            this();
        }

        private Status() {
        }

        public boolean equals(Object in) {
            boolean z = false;
            if (in == null || !(in instanceof Status)) {
                return false;
            }
            Status other = (Status) in;
            if (this.mRootStatus == other.mRootStatus && this.mFastbootStatus == other.mFastbootStatus && this.mVerfybootStatus == other.mVerfybootStatus && this.mSeLinuxStatus == other.mSeLinuxStatus && this.mCustSysStatus == other.mCustSysStatus) {
                z = true;
            }
            return z;
        }

        public String toString() {
            return "Root Status : " + this.mRootStatus + " Fastboot Status : " + this.mFastbootStatus + " Verifyboot Status : " + this.mVerfybootStatus + " SeLinux Status : " + this.mSeLinuxStatus + " Cust System : " + this.mCustSysStatus + " FirstUseTime : " + this.mDeviceFirstUseTime + " SecPatch Version : " + this.mSecPatchVer;
        }

        public int hashCode() {
            return super.hashCode();
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HW_DEBUG = isLoggable;
    }

    public void addAntiMalApkInfo(AntiMalApkInfo apkInfo) {
        if (apkInfo != null) {
            this.mCurApkInfoList.add(apkInfo);
        }
    }

    public void addComponentInfo(AntiMalComponentInfo componentInfo) {
        if (componentInfo != null) {
            this.mCurComponentList.add(componentInfo);
        }
    }

    public Bundle getAntimalComponentInfo() {
        Bundle bundle = new Bundle();
        if (this.mCurComponentList.size() > 0) {
            bundle.putParcelableArrayList(HwSecDiagnoseConstant.COMPONENT_LIST, this.mCurComponentList);
        } else {
            bundle.putParcelableArrayList(HwSecDiagnoseConstant.COMPONENT_LIST, this.mOldComponentList);
        }
        return bundle;
    }

    AntiMalComponentInfo getComponentByApkPath(String apkPath) {
        if (TextUtils.isEmpty(apkPath)) {
            return null;
        }
        int list_size = this.mCurComponentList.size();
        for (int i = 0; i < list_size; i++) {
            if (apkPath.contains(((AntiMalComponentInfo) this.mCurComponentList.get(i)).mName)) {
                return (AntiMalComponentInfo) this.mCurComponentList.get(i);
            }
        }
        return null;
    }

    public ArrayList<AntiMalApkInfo> getOldApkInfoList() {
        return this.mOldApkInfoList;
    }

    public AntiMalDataManager(boolean isOtaBoot) {
        this.mOtaBoot = isOtaBoot;
        readOldAntiMalData();
        getDeviceFirstUseTime();
        getCurrentStatus();
    }

    private boolean isDataValid() {
        getCurCounter();
        return this.mCurCounter.hasAbnormalApks() ? antiMalDataEquals() ^ 1 : false;
    }

    private boolean deviceStatusOK() {
        return this.mDeviceFirstUseTime <= 0 && this.mCurCounter.mBootCnt <= 10;
    }

    public boolean needReport() {
        return deviceStatusOK() ? isDataValid() : false;
    }

    public boolean needScanIllegalApks() {
        return !this.mOtaBoot ? deviceStatusOK() : true;
    }

    private boolean apkInfoListEquals() {
        boolean z = true;
        if (this.mOldApkInfoList == null) {
            if (HW_DEBUG) {
                Log.d(TAG, "apkInfoListEquals mOldApkInfoList is NULL!");
            }
            if (this.mCurApkInfoList.size() != 0) {
                z = false;
            }
            return z;
        } else if (this.mOldApkInfoList.size() != this.mCurApkInfoList.size()) {
            if (HW_DEBUG) {
                Log.d(TAG, "apkInfoListEquals size not equal!");
            }
            return false;
        } else if (this.mCurApkInfoList.size() == 0) {
            if (HW_DEBUG) {
                Log.d(TAG, "apkInfoListEquals size is 0");
            }
            return true;
        } else {
            AntiMalApkInfo[] sampleArry = new AntiMalApkInfo[this.mCurApkInfoList.size()];
            AntiMalApkInfo[] curApkInfoArry = (AntiMalApkInfo[]) this.mCurApkInfoList.toArray(sampleArry);
            AntiMalApkInfo[] oldApkInfoArry = (AntiMalApkInfo[]) this.mOldApkInfoList.toArray(sampleArry);
            Arrays.sort(curApkInfoArry);
            Arrays.sort(oldApkInfoArry);
            return Arrays.equals(oldApkInfoArry, curApkInfoArry);
        }
    }

    private boolean antiMalDataEquals() {
        boolean apkCntEqual = this.mCurCounter.equals(this.mOldCounter);
        boolean listEqual = apkInfoListEquals();
        if (HW_DEBUG) {
            Log.d(TAG, " apkCntEqual = " + apkCntEqual + " listEqual = " + listEqual);
        }
        return apkCntEqual ? listEqual : false;
    }

    private int stringToInt(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(str);
    }

    private void getCurCounter() {
        int addCnt = 0;
        int modifyCnt = 0;
        int deleteCnt = 0;
        if (this.mCurApkInfoList.size() != 0) {
            synchronized (this.mCurApkInfoList) {
                for (AntiMalApkInfo ai : this.mCurApkInfoList) {
                    if (ai != null) {
                        switch (ai.mType) {
                            case 1:
                                addCnt++;
                                break;
                            case 2:
                                modifyCnt++;
                                break;
                            case 3:
                                deleteCnt++;
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            this.mCurCounter.mAddCnt = addCnt;
            this.mCurCounter.mDeleteCnt = deleteCnt;
            this.mCurCounter.mModifiedCnt = modifyCnt;
            if (HW_DEBUG) {
                Log.d(TAG, "getCurCounter = " + this.mCurCounter);
            }
        }
    }

    private void getCurrentStatus() {
        int i;
        int i2 = 1;
        int maskSysStatus = SystemProperties.getInt("persist.sys.root.status", 0);
        Status status = this.mCurAntiMalStatus;
        if (maskSysStatus > 0) {
            i = 1;
        } else {
            i = 0;
        }
        status.mRootStatus = i;
        int verStatusMask = maskSysStatus & 128;
        status = this.mCurAntiMalStatus;
        if (verStatusMask > 0) {
            i = 1;
        } else {
            i = 0;
        }
        status.mVerfybootStatus = i;
        int seLinuxMask = maskSysStatus & 8;
        status = this.mCurAntiMalStatus;
        if (seLinuxMask > 0) {
            i = 1;
        } else {
            i = 0;
        }
        status.mSeLinuxStatus = i;
        this.mCurAntiMalStatus.mFastbootStatus = getCurFastbootStatus();
        boolean writable = SystemProperties.getBoolean(SYSTEM_CUST_STATUS_PRO, false);
        Status status2 = this.mCurAntiMalStatus;
        if (!writable) {
            i2 = 0;
        }
        status2.mCustSysStatus = i2;
        this.mCurAntiMalStatus.mDeviceFirstUseTime = formatTime(this.mDeviceFirstUseTime);
        this.mCurAntiMalStatus.mSecPatchVer = getSecurePatchVersion();
        if (HW_DEBUG) {
            Log.d(TAG, "getCurrentStatus AntiMalStatus = " + this.mCurAntiMalStatus);
        }
    }

    private int getCurFastbootStatus() {
        int status = SystemProperties.getInt(FASTBOOT_STATUS, 1);
        if (HW_DEBUG) {
            Log.d(TAG, "getCurFastbootStatus fastboot status = " + status);
        }
        return status;
    }

    private String getSecurePatchVersion() {
        String patch = VERSION.SECURITY_PATCH;
        if (HW_DEBUG) {
            Log.d(TAG, "getSecurePatchVersion patch = " + patch);
        }
        if (TextUtils.isEmpty(patch)) {
            return null;
        }
        return formatData(patch);
    }

    private void getDeviceFirstUseTime() {
        byte[] renewData = HwOEMInfoAdapter.getByteArrayFromOeminfo(OEMINFO_ENABLE_RETREAD, 40);
        if (renewData == null || renewData.length < 40) {
            Log.d(TAG, "getDeviceFirstUseTime OEMINFO error!");
            return;
        }
        ByteBuffer dataBuffer = ByteBuffer.allocate(40);
        dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
        dataBuffer.clear();
        dataBuffer.put(renewData);
        this.mDeviceFirstUseTime = dataBuffer.getLong(32);
        if (HW_DEBUG) {
            Log.d(TAG, "mDeviceFirstUseTime = " + formatTime(this.mDeviceFirstUseTime));
        }
    }

    public Bundle collectData() {
        getCurCounter();
        Bundle antimalData = new Bundle();
        antimalData.putString(HwSecDiagnoseConstant.ANTIMAL_TIME, getCurrentTime());
        antimalData.putInt(HwSecDiagnoseConstant.ANTIMAL_ROOT_STATE, this.mCurAntiMalStatus.mRootStatus);
        antimalData.putInt(HwSecDiagnoseConstant.ANTIMAL_FASTBOOT_STATE, this.mCurAntiMalStatus.mFastbootStatus);
        antimalData.putInt(HwSecDiagnoseConstant.ANTIMAL_SYSTEM_STATE, this.mCurAntiMalStatus.mVerfybootStatus);
        antimalData.putInt(HwSecDiagnoseConstant.ANTIMAL_SELINUX_STATE, this.mCurAntiMalStatus.mSeLinuxStatus);
        antimalData.putInt(HwSecDiagnoseConstant.ANTIMAL_SYSTEM_CUST_STATE, this.mCurAntiMalStatus.mCustSysStatus);
        antimalData.putString(HwSecDiagnoseConstant.ANTIMAL_USED_TIME, this.mCurAntiMalStatus.mDeviceFirstUseTime);
        antimalData.putString("SecVer", this.mCurAntiMalStatus.mSecPatchVer);
        antimalData.putInt(HwSecDiagnoseConstant.ANTIMAL_MAL_COUNT, this.mCurCounter.mAddCnt);
        antimalData.putInt(HwSecDiagnoseConstant.ANTIMAL_DELETE_COUNT, this.mCurCounter.mDeleteCnt);
        antimalData.putInt(HwSecDiagnoseConstant.ANTIMAL_TAMPER_COUNT, this.mCurCounter.mModifiedCnt);
        antimalData.putString("SecVer", null);
        if (this.mCurApkInfoList.size() > 0) {
            antimalData.putParcelableArrayList(HwSecDiagnoseConstant.ANTIMAL_APK_LIST, this.mCurApkInfoList);
        }
        return antimalData;
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x00bf  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00bf  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00bf  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00bf  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00bf  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00bf  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readOldAntiMalData() {
        IOException e;
        Throwable th;
        XmlPullParserException e2;
        Object str;
        Exception e3;
        long start = System.currentTimeMillis();
        File antimalFile = Environment.buildPath(Environment.getDataDirectory(), new String[]{"system", ANTIMAL_DATA_FILE});
        if (verifyAntimalFile(antimalFile)) {
            AutoCloseable str2 = null;
            try {
                FileInputStream str3 = new FileInputStream(antimalFile);
                try {
                    int type;
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(str3, StandardCharsets.UTF_8.name());
                    do {
                        type = parser.next();
                        if (type == 2) {
                            break;
                        }
                    } while (type != 1);
                    if (type != 2) {
                        Log.e(TAG, "readAntiMalData NO start tag!");
                        IoUtils.closeQuietly(str3);
                        return;
                    }
                    int outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                            IoUtils.closeQuietly(str3);
                        } else if (!(type == 3 || type == 4)) {
                            readOldDataByTag(parser, parser.getName());
                        }
                    }
                    IoUtils.closeQuietly(str3);
                    if (HW_DEBUG) {
                        Log.d(TAG, "readOldAntiMalData time = " + (System.currentTimeMillis() - start));
                    }
                } catch (IOException e4) {
                    e = e4;
                    str2 = str3;
                    try {
                        Log.e(TAG, "readAntiMalData IOException e: " + e);
                        printStackTraceForDebug(e);
                        IoUtils.closeQuietly(str2);
                        if (HW_DEBUG) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(str2);
                        throw th;
                    }
                } catch (XmlPullParserException e5) {
                    e2 = e5;
                    str2 = str3;
                    Log.e(TAG, "readAntiMalData XmlPullParserException: " + e2);
                    printStackTraceForDebug(e2);
                    IoUtils.closeQuietly(str2);
                    if (HW_DEBUG) {
                    }
                } catch (Exception e6) {
                    e3 = e6;
                    str2 = str3;
                    Log.e(TAG, "readAntiMalData Other exception :" + e3);
                    printStackTraceForDebug(e3);
                    IoUtils.closeQuietly(str2);
                    if (HW_DEBUG) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    str2 = str3;
                    IoUtils.closeQuietly(str2);
                    throw th;
                }
            } catch (IOException e7) {
                e = e7;
                Log.e(TAG, "readAntiMalData IOException e: " + e);
                printStackTraceForDebug(e);
                IoUtils.closeQuietly(str2);
                if (HW_DEBUG) {
                }
            } catch (XmlPullParserException e8) {
                e2 = e8;
                Log.e(TAG, "readAntiMalData XmlPullParserException: " + e2);
                printStackTraceForDebug(e2);
                IoUtils.closeQuietly(str2);
                if (HW_DEBUG) {
                }
            } catch (Exception e9) {
                e3 = e9;
                Log.e(TAG, "readAntiMalData Other exception :" + e3);
                printStackTraceForDebug(e3);
                IoUtils.closeQuietly(str2);
                if (HW_DEBUG) {
                }
            }
        }
    }

    private boolean verifyAntimalFile(File antimalFile) {
        if (antimalFile != null && (antimalFile.exists() ^ 1) == 0) {
            return true;
        }
        if (HW_DEBUG) {
            Log.d(TAG, "readOldAntiMalData File not exist!");
        }
        setBootCnt();
        return false;
    }

    private void printStackTraceForDebug(Exception e) {
        if (HW_DEBUG) {
            e.printStackTrace();
        }
    }

    private void readOldDataByTag(XmlPullParser parser, String tagName) throws XmlPullParserException, IOException {
        if (STATUS.equals(tagName)) {
            readOldStatus(parser);
        } else if (COUNTER.equals(tagName)) {
            readOldCounter(parser);
        } else if (APPS.equals(tagName)) {
            readOldAntiMalApks(parser);
        } else if (COMPONENT.equals(tagName)) {
            readOldComponentInfo(parser);
        }
    }

    private void readOldStatus(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    this.mOldAntiMalStatus = new Status();
                    String rootStatusStr = parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_ROOT_STATE);
                    this.mOldAntiMalStatus.mRootStatus = stringToInt(rootStatusStr);
                    String fastbootStatusStr = parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_FASTBOOT_STATE);
                    this.mOldAntiMalStatus.mFastbootStatus = stringToInt(fastbootStatusStr);
                    String systemStatusStr = parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_SYSTEM_STATE);
                    this.mOldAntiMalStatus.mVerfybootStatus = stringToInt(systemStatusStr);
                    String seLinuxStatusStr = parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_SELINUX_STATE);
                    this.mOldAntiMalStatus.mSeLinuxStatus = stringToInt(seLinuxStatusStr);
                    String custSystemStatusVer = parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_SYSTEM_CUST_STATE);
                    this.mOldAntiMalStatus.mCustSysStatus = stringToInt(custSystemStatusVer);
                    this.mOldAntiMalStatus.mDeviceFirstUseTime = parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_USED_TIME);
                    this.mOldAntiMalStatus.mSecPatchVer = parser.getAttributeValue(null, "SecVer");
                    if (HW_DEBUG) {
                        Log.d(TAG, "readStatus = " + this.mOldAntiMalStatus);
                    }
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    private void readOldCounter(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (TAG_ITEM.equals(parser.getName())) {
                    this.mOldCounter = new AntiMalCounter();
                    String malCntStr = parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_MAL_COUNT);
                    this.mOldCounter.mAddCnt = stringToInt(malCntStr);
                    String deleteCntStr = parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_DELETE_COUNT);
                    this.mOldCounter.mDeleteCnt = stringToInt(deleteCntStr);
                    String modifyCntStr = parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_TAMPER_COUNT);
                    this.mOldCounter.mModifiedCnt = stringToInt(modifyCntStr);
                    String bootCntStr = parser.getAttributeValue(null, BOOT_TIMES);
                    this.mOldCounter.mBootCnt = stringToInt(bootCntStr);
                    setBootCnt();
                    if (HW_DEBUG) {
                        Log.d(TAG, "readCounter = " + this.mOldCounter);
                    }
                }
            }
        }
    }

    private void setBootCnt() {
        int i;
        AntiMalCounter antiMalCounter = this.mCurCounter;
        if (this.mOldCounter != null) {
            AntiMalCounter antiMalCounter2 = this.mOldCounter;
            i = antiMalCounter2.mBootCnt + 1;
            antiMalCounter2.mBootCnt = i;
        } else {
            i = 1;
        }
        antiMalCounter.mBootCnt = i;
    }

    private void readOldAntiMalApks(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        this.mOldApkInfoList = new ArrayList();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(TAG_ITEM))) {
                String typeStr = parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_APK_TYPE);
                String packageName = parser.getAttributeValue(null, "PackageName");
                String apkName = parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_APK_NAME);
                AntiMalApkInfo api = new AntiMalApkInfo(packageName, parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_APK_PATH), apkName, stringToInt(typeStr), parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_APK_LAST_MODIFY), null, stringToInt(parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_APK_VERSION)));
                this.mOldApkInfoList.add(api);
                if (HW_DEBUG) {
                    Log.d(TAG, "readAntiMalApks : AntiMalApkInfo : " + api);
                }
            }
        }
    }

    private void readOldComponentInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(TAG_ITEM))) {
                String name = parser.getAttributeValue(null, "name");
                String verifyStatus = parser.getAttributeValue(null, AntiMalComponentInfo.VERIFY_STATUS);
                String antimalTypeMask = parser.getAttributeValue(null, AntiMalComponentInfo.ANTIMAL_TYPE_MASK);
                if (!TextUtils.isEmpty(name)) {
                    AntiMalComponentInfo acpi = new AntiMalComponentInfo(name, stringToInt(verifyStatus), stringToInt(antimalTypeMask));
                    this.mOldComponentList.add(acpi);
                    if (HW_DEBUG) {
                        Log.d(TAG, "readOldComponentInfo AntiMalComponentInfo : " + acpi);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x009f A:{Catch:{ all -> 0x00a6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0079 A:{Catch:{ all -> 0x00a6 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void writeAntiMalData() {
        IOException e;
        Object fos;
        Exception e2;
        Throwable th;
        AutoCloseable fos2 = null;
        try {
            FileOutputStream fos3 = new FileOutputStream(Environment.buildPath(Environment.getDataDirectory(), new String[]{"system", ANTIMAL_DATA_FILE}), false);
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(fos3, StandardCharsets.UTF_8.name());
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, "antimal");
                writeStatus(out);
                writeCounter(out);
                writeApkInfoList(out);
                writeComponentInfoList(out);
                out.endTag(null, "antimal");
                out.endDocument();
                fos3.flush();
                IoUtils.closeQuietly(fos3);
                FileOutputStream fileOutputStream = fos3;
            } catch (IOException e3) {
                e = e3;
                fos2 = fos3;
                Log.e(TAG, "writeAntiMalData IOException: " + e);
                if (HW_DEBUG) {
                }
                IoUtils.closeQuietly(fos2);
            } catch (Exception e4) {
                e2 = e4;
                fos2 = fos3;
                try {
                    Log.e(TAG, "writeAntiMalData Other exception: " + e2);
                    if (HW_DEBUG) {
                    }
                    IoUtils.closeQuietly(fos2);
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(fos2);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fos2 = fos3;
                IoUtils.closeQuietly(fos2);
                throw th;
            }
        } catch (IOException e5) {
            e = e5;
            Log.e(TAG, "writeAntiMalData IOException: " + e);
            if (HW_DEBUG) {
                e.printStackTrace();
            }
            IoUtils.closeQuietly(fos2);
        } catch (Exception e6) {
            e2 = e6;
            Log.e(TAG, "writeAntiMalData Other exception: " + e2);
            if (HW_DEBUG) {
                e2.printStackTrace();
            }
            IoUtils.closeQuietly(fos2);
        }
    }

    private void writeStatus(XmlSerializer out) throws IOException, IllegalArgumentException, IllegalStateException {
        out.startTag(null, STATUS);
        out.startTag(null, TAG_ITEM);
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_ROOT_STATE, new StringBuffer().append(this.mCurAntiMalStatus.mRootStatus).toString());
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_FASTBOOT_STATE, new StringBuffer().append(this.mCurAntiMalStatus.mFastbootStatus).toString());
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_SYSTEM_STATE, new StringBuffer().append(this.mCurAntiMalStatus.mVerfybootStatus).toString());
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_SELINUX_STATE, new StringBuffer().append(this.mCurAntiMalStatus.mSeLinuxStatus).toString());
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_SYSTEM_CUST_STATE, new StringBuffer().append(this.mCurAntiMalStatus.mCustSysStatus).toString());
        if (!TextUtils.isEmpty(this.mCurAntiMalStatus.mSecPatchVer)) {
            out.attribute(null, "SecVer", this.mCurAntiMalStatus.mSecPatchVer);
        }
        if (!TextUtils.isEmpty(this.mCurAntiMalStatus.mDeviceFirstUseTime)) {
            out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_USED_TIME, this.mCurAntiMalStatus.mDeviceFirstUseTime);
        }
        out.endTag(null, TAG_ITEM);
        out.endTag(null, STATUS);
    }

    private void writeCounter(XmlSerializer out) throws IOException, IllegalArgumentException, IllegalStateException {
        out.startTag(null, COUNTER);
        out.startTag(null, TAG_ITEM);
        out.attribute(null, BOOT_TIMES, new StringBuffer().append(this.mCurCounter.mBootCnt).toString());
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_MAL_COUNT, new StringBuffer().append(this.mCurCounter.mAddCnt).toString());
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_DELETE_COUNT, new StringBuffer().append(this.mCurCounter.mDeleteCnt).toString());
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_TAMPER_COUNT, new StringBuffer().append(this.mCurCounter.mModifiedCnt).toString());
        out.endTag(null, TAG_ITEM);
        out.endTag(null, COUNTER);
    }

    private void writeApkInfoList(XmlSerializer out) throws IOException, IllegalArgumentException, IllegalStateException {
        out.startTag(null, APPS);
        int list_size = this.mCurApkInfoList.size();
        for (int i = 0; i < list_size; i++) {
            if (HW_DEBUG) {
                Log.d(TAG, "writeAntiMalData AntiMalApkInfo : " + this.mCurApkInfoList.get(i));
            }
            if (this.mCurApkInfoList.get(i) != null) {
                out.startTag(null, TAG_ITEM);
                out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, new StringBuffer().append(((AntiMalApkInfo) this.mCurApkInfoList.get(i)).mType).toString());
                out.attribute(null, "PackageName", ((AntiMalApkInfo) this.mCurApkInfoList.get(i)).mPackageName);
                out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_APK_NAME, ((AntiMalApkInfo) this.mCurApkInfoList.get(i)).mApkName);
                out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_APK_PATH, ((AntiMalApkInfo) this.mCurApkInfoList.get(i)).mPath);
                out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_APK_LAST_MODIFY, ((AntiMalApkInfo) this.mCurApkInfoList.get(i)).mLastModifyTime);
                out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_APK_VERSION, new StringBuffer().append(((AntiMalApkInfo) this.mCurApkInfoList.get(i)).mVersion).toString());
                out.endTag(null, TAG_ITEM);
            }
        }
        out.endTag(null, APPS);
    }

    private void writeComponentInfoList(XmlSerializer out) throws IOException, IllegalArgumentException, IllegalStateException {
        out.startTag(null, COMPONENT);
        int list_size = this.mCurComponentList.size();
        for (int i = 0; i < list_size; i++) {
            if (HW_DEBUG) {
                Log.d(TAG, "writeComponentInfoList AntiMalComponentInfo : " + this.mCurComponentList.get(i));
            }
            if (this.mCurComponentList.get(i) != null) {
                out.startTag(null, TAG_ITEM);
                out.attribute(null, "name", ((AntiMalComponentInfo) this.mCurComponentList.get(i)).mName);
                out.attribute(null, AntiMalComponentInfo.VERIFY_STATUS, new StringBuffer().append(((AntiMalComponentInfo) this.mCurComponentList.get(i)).mVerifyStatus).toString());
                out.attribute(null, AntiMalComponentInfo.ANTIMAL_TYPE_MASK, new StringBuffer().append(((AntiMalComponentInfo) this.mCurComponentList.get(i)).mAntimalTypeMask).toString());
                out.endTag(null, TAG_ITEM);
            }
        }
        out.endTag(null, COMPONENT);
    }

    private String formatTime(long minSecond) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(minSecond));
    }

    private String formatData(String date) {
        if (TextUtils.isEmpty(date)) {
            return null;
        }
        String patch = null;
        try {
            patch = DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy"), new SimpleDateFormat("yyyy-MM-dd").parse(date)).toString();
        } catch (Exception e) {
            Log.e(TAG, "formatData ParseException!");
            if (HW_DEBUG) {
                e.printStackTrace();
            }
        }
        return patch;
    }

    private String getCurrentTime() {
        return formatTime(System.currentTimeMillis());
    }
}
