package com.android.server.pm;

import android.os.Build;
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
import java.util.Iterator;
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
    private static final boolean HW_DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int MAX_BOOT_TIMES = 10;
    private static final int OEMINFO_ENABLE_RETREAD = 163;
    private static final int OEMINFO_ENABLE_RETREAD_SIZE = 40;
    private static final String STATUS = "status";
    private static final String SYSTEM_CUST_STATUS_PRO = "ro.odm.sys.wp";
    private static final String TAG = "AntiMalDataManager";
    private static final String TAG_ITEM = "item";
    private boolean mAntiMalDataExist;
    private Status mCurAntiMalStatus = new Status();
    private ArrayList<AntiMalApkInfo> mCurApkInfoList = new ArrayList<>();
    private ArrayList<AntiMalComponentInfo> mCurComponentList = new ArrayList<>();
    private AntiMalCounter mCurCounter = new AntiMalCounter();
    private long mDeviceFirstUseTime;
    private Status mOldAntiMalStatus;
    private ArrayList<AntiMalApkInfo> mOldApkInfoList;
    private ArrayList<AntiMalComponentInfo> mOldComponentList = new ArrayList<>();
    private AntiMalCounter mOldCounter;
    private boolean mOtaBoot;

    private static class AntiMalCounter {
        int mAddCnt;
        int mBootCnt;
        int mDeleteCnt;
        int mModifiedCnt;

        public AntiMalCounter() {
        }

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
            return this.mDeleteCnt + this.mModifiedCnt + this.mAddCnt + this.mBootCnt;
        }

        /* access modifiers changed from: package-private */
        public boolean hasAbnormalApks() {
            return (this.mDeleteCnt + this.mModifiedCnt) + this.mAddCnt > 0;
        }
    }

    private static class Status {
        int mCustSysStatus;
        String mDeviceFirstUseTimeStr;
        int mFastbootStatus;
        int mRootStatus;
        int mSeLinuxStatus;
        String mSecPatchVer;
        int mVerfybootStatus;

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
            return "Root Status : " + this.mRootStatus + " Fastboot Status : " + this.mFastbootStatus + " Verifyboot Status : " + this.mVerfybootStatus + " SeLinux Status : " + this.mSeLinuxStatus + " Cust System : " + this.mCustSysStatus + " FirstUseTime : " + this.mDeviceFirstUseTimeStr + " SecPatch Version : " + this.mSecPatchVer;
        }

        public int hashCode() {
            return this.mRootStatus + this.mFastbootStatus + this.mVerfybootStatus + this.mSeLinuxStatus + this.mCustSysStatus + this.mDeviceFirstUseTimeStr.hashCode() + this.mSecPatchVer.hashCode();
        }
    }

    public AntiMalDataManager(boolean isOtaBoot) {
        this.mOtaBoot = isOtaBoot;
        readOldAntiMalData();
        getDeviceFirstUseTime();
        getCurrentStatus();
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

    /* access modifiers changed from: package-private */
    public AntiMalComponentInfo getComponentByApkPath(String apkPath) {
        if (TextUtils.isEmpty(apkPath)) {
            return null;
        }
        int listSize = this.mCurComponentList.size();
        for (int i = 0; i < listSize; i++) {
            if (apkPath.contains(this.mCurComponentList.get(i).mName)) {
                return this.mCurComponentList.get(i);
            }
        }
        return null;
    }

    public ArrayList<AntiMalApkInfo> getOldApkInfoList() {
        return this.mOldApkInfoList;
    }

    private boolean isDataValid() {
        getCurCounter();
        return this.mCurCounter.hasAbnormalApks() && !antiMalDataEquals();
    }

    private boolean isOldAntiMalResultNormal() {
        Iterator<AntiMalComponentInfo> it = this.mOldComponentList.iterator();
        while (it.hasNext()) {
            AntiMalComponentInfo tmpComp = it.next();
            if (HW_DEBUG) {
                Log.d(TAG, "isOldAntiMalResultNormal tmpComp info : " + tmpComp);
            }
            if (!tmpComp.isNormal()) {
                return false;
            }
        }
        return true;
    }

    private boolean deviceStatusOK() {
        Log.i(TAG, "deviceStatusOK mDeviceFirstUseTime = " + formatTime(this.mDeviceFirstUseTime) + " mBootCnt = " + this.mCurCounter.mBootCnt);
        return this.mDeviceFirstUseTime == 0 && this.mCurCounter.mBootCnt <= 10;
    }

    public boolean needReport() {
        return deviceStatusOK() && isDataValid();
    }

    public boolean needScanIllegalApks() {
        return this.mOtaBoot || deviceStatusOK() || !this.mAntiMalDataExist || !isOldAntiMalResultNormal();
    }

    private boolean apkInfoListEquals() {
        boolean z = false;
        if (this.mOldApkInfoList == null) {
            if (HW_DEBUG) {
                Log.d(TAG, "apkInfoListEquals mOldApkInfoList is NULL!");
            }
            if (this.mCurApkInfoList.size() == 0) {
                z = true;
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
        return apkCntEqual && listEqual;
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
                Iterator<AntiMalApkInfo> it = this.mCurApkInfoList.iterator();
                while (it.hasNext()) {
                    AntiMalApkInfo ai = it.next();
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
        int maskSysStatus = SystemProperties.getInt("persist.sys.root.status", 0);
        int i = 1;
        this.mCurAntiMalStatus.mRootStatus = maskSysStatus > 0 ? 1 : 0;
        this.mCurAntiMalStatus.mVerfybootStatus = (maskSysStatus & 128) > 0 ? 1 : 0;
        int seLinuxMask = maskSysStatus & 8;
        Status status = this.mCurAntiMalStatus;
        if (seLinuxMask <= 0) {
            i = 0;
        }
        status.mSeLinuxStatus = i;
        this.mCurAntiMalStatus.mFastbootStatus = getCurFastbootStatus();
        this.mCurAntiMalStatus.mCustSysStatus = SystemProperties.getBoolean(SYSTEM_CUST_STATUS_PRO, false);
        this.mCurAntiMalStatus.mDeviceFirstUseTimeStr = formatTime(this.mDeviceFirstUseTime);
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
        String patch = Build.VERSION.SECURITY_PATCH;
        if (HW_DEBUG) {
            Log.d(TAG, "getSecurePatchVersion patch = " + patch);
        }
        if (!TextUtils.isEmpty(patch)) {
            return formatData(patch);
        }
        return null;
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
        Log.e(TAG, "mDeviceFirstUseTime = " + formatTime(this.mDeviceFirstUseTime));
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
        antimalData.putString(HwSecDiagnoseConstant.ANTIMAL_USED_TIME, this.mCurAntiMalStatus.mDeviceFirstUseTimeStr);
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

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0046 A[Catch:{ IOException -> 0x00a8, XmlPullParserException -> 0x008f, Exception -> 0x0076, all -> 0x0074 }] */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0052 A[SYNTHETIC, Splitter:B:14:0x0052] */
    private void readOldAntiMalData() {
        int type;
        long start = System.currentTimeMillis();
        File antimalFile = Environment.buildPath(Environment.getDataDirectory(), new String[]{"system", ANTIMAL_DATA_FILE});
        if (verifyAntimalFile(antimalFile)) {
            FileInputStream str = null;
            try {
                str = new FileInputStream(antimalFile);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(str, StandardCharsets.UTF_8.name());
                while (true) {
                    int next = parser.next();
                    type = next;
                    if (next != 2 && type != 1) {
                        Log.e(TAG, "readOldAntiMalData");
                    } else if (type == 2) {
                        Log.e(TAG, "readAntiMalData NO start tag!");
                        IoUtils.closeQuietly(str);
                        return;
                    } else {
                        int outerDepth = parser.getDepth();
                        while (true) {
                            int next2 = parser.next();
                            int type2 = next2;
                            if (next2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                                break;
                            } else if (type2 != 3) {
                                if (type2 != 4) {
                                    readOldDataByTag(parser, parser.getName());
                                }
                            }
                        }
                        IoUtils.closeQuietly(str);
                        if (HW_DEBUG) {
                            long end = System.currentTimeMillis();
                            Log.d(TAG, "readOldAntiMalData time = " + (end - start));
                        }
                        return;
                    }
                }
                if (type == 2) {
                }
            } catch (IOException e) {
                Log.e(TAG, "readAntiMalData IOException e: " + e);
            } catch (XmlPullParserException e2) {
                Log.e(TAG, "readAntiMalData XmlPullParserException: " + e2);
            } catch (Exception e3) {
                Log.e(TAG, "readAntiMalData Other exception :" + e3);
            } catch (Throwable th) {
                IoUtils.closeQuietly(str);
                throw th;
            }
        }
    }

    private boolean verifyAntimalFile(File antimalFile) {
        if (antimalFile == null || !antimalFile.exists()) {
            Log.e(TAG, "readOldAntiMalData AntiMalData.xml File not exist!");
            this.mAntiMalDataExist = false;
            setBootCnt();
            return false;
        }
        this.mAntiMalDataExist = true;
        return true;
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
            int next = parser.next();
            int type = next;
            if (next == 1) {
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
                    this.mOldAntiMalStatus.mDeviceFirstUseTimeStr = parser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_USED_TIME);
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
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !TAG_ITEM.equals(parser.getName()))) {
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

    private void setBootCnt() {
        AntiMalCounter antiMalCounter = this.mCurCounter;
        int i = 1;
        if (this.mOldCounter != null) {
            AntiMalCounter antiMalCounter2 = this.mOldCounter;
            i = 1 + antiMalCounter2.mBootCnt;
            antiMalCounter2.mBootCnt = i;
        }
        antiMalCounter.mBootCnt = i;
    }

    private void readOldAntiMalApks(XmlPullParser parser) throws XmlPullParserException, IOException {
        XmlPullParser xmlPullParser = parser;
        int outerDepth = parser.getDepth();
        this.mOldApkInfoList = new ArrayList<>();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(TAG_ITEM))) {
                String typeStr = xmlPullParser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_APK_TYPE);
                String packageName = xmlPullParser.getAttributeValue(null, "PackageName");
                String apkName = xmlPullParser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_APK_NAME);
                String apkPath = xmlPullParser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_APK_PATH);
                String lastTime = xmlPullParser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_APK_LAST_MODIFY);
                String str = packageName;
                String str2 = apkPath;
                String str3 = apkName;
                String str4 = lastTime;
                AntiMalApkInfo api = new AntiMalApkInfo(str, str2, str3, stringToInt(typeStr), str4, null, stringToInt(xmlPullParser.getAttributeValue(null, HwSecDiagnoseConstant.ANTIMAL_APK_VERSION)));
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
            int next = parser.next();
            int type = next;
            if (next == 1) {
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

    public void writeAntiMalData() {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(Environment.buildPath(Environment.getDataDirectory(), new String[]{"system", ANTIMAL_DATA_FILE}), false);
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            out.startTag(null, "antimal");
            writeStatus(out);
            writeCounter(out);
            writeApkInfoList(out);
            writeComponentInfoList(out);
            out.endTag(null, "antimal");
            out.endDocument();
            fos.flush();
        } catch (IOException e) {
            Log.e(TAG, "writeAntiMalData IOException: " + e);
        } catch (Exception e2) {
            Log.e(TAG, "writeAntiMalData Other exception: " + e2);
        } catch (Throwable th) {
            IoUtils.closeQuietly(fos);
            throw th;
        }
        IoUtils.closeQuietly(fos);
    }

    private void writeStatus(XmlSerializer out) throws IOException, IllegalArgumentException, IllegalStateException {
        out.startTag(null, STATUS);
        out.startTag(null, TAG_ITEM);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.mCurAntiMalStatus.mRootStatus);
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_ROOT_STATE, stringBuffer.toString());
        StringBuffer stringBuffer2 = new StringBuffer();
        stringBuffer2.append(this.mCurAntiMalStatus.mFastbootStatus);
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_FASTBOOT_STATE, stringBuffer2.toString());
        StringBuffer stringBuffer3 = new StringBuffer();
        stringBuffer3.append(this.mCurAntiMalStatus.mVerfybootStatus);
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_SYSTEM_STATE, stringBuffer3.toString());
        StringBuffer stringBuffer4 = new StringBuffer();
        stringBuffer4.append(this.mCurAntiMalStatus.mSeLinuxStatus);
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_SELINUX_STATE, stringBuffer4.toString());
        StringBuffer stringBuffer5 = new StringBuffer();
        stringBuffer5.append(this.mCurAntiMalStatus.mCustSysStatus);
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_SYSTEM_CUST_STATE, stringBuffer5.toString());
        if (!TextUtils.isEmpty(this.mCurAntiMalStatus.mSecPatchVer)) {
            out.attribute(null, "SecVer", this.mCurAntiMalStatus.mSecPatchVer);
        }
        if (!TextUtils.isEmpty(this.mCurAntiMalStatus.mDeviceFirstUseTimeStr)) {
            out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_USED_TIME, this.mCurAntiMalStatus.mDeviceFirstUseTimeStr);
        }
        out.endTag(null, TAG_ITEM);
        out.endTag(null, STATUS);
    }

    private void writeCounter(XmlSerializer out) throws IOException, IllegalArgumentException, IllegalStateException {
        out.startTag(null, COUNTER);
        out.startTag(null, TAG_ITEM);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.mCurCounter.mBootCnt);
        out.attribute(null, BOOT_TIMES, stringBuffer.toString());
        StringBuffer stringBuffer2 = new StringBuffer();
        stringBuffer2.append(this.mCurCounter.mAddCnt);
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_MAL_COUNT, stringBuffer2.toString());
        StringBuffer stringBuffer3 = new StringBuffer();
        stringBuffer3.append(this.mCurCounter.mDeleteCnt);
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_DELETE_COUNT, stringBuffer3.toString());
        StringBuffer stringBuffer4 = new StringBuffer();
        stringBuffer4.append(this.mCurCounter.mModifiedCnt);
        out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_TAMPER_COUNT, stringBuffer4.toString());
        out.endTag(null, TAG_ITEM);
        out.endTag(null, COUNTER);
    }

    private void writeApkInfoList(XmlSerializer out) throws IOException, IllegalArgumentException, IllegalStateException {
        out.startTag(null, APPS);
        int listSize = this.mCurApkInfoList.size();
        for (int i = 0; i < listSize; i++) {
            if (HW_DEBUG) {
                Log.d(TAG, "writeAntiMalData AntiMalApkInfo : " + this.mCurApkInfoList.get(i));
            }
            if (this.mCurApkInfoList.get(i) != null) {
                out.startTag(null, TAG_ITEM);
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(this.mCurApkInfoList.get(i).mType);
                out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, stringBuffer.toString());
                out.attribute(null, "PackageName", this.mCurApkInfoList.get(i).mPackageName);
                out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_APK_NAME, this.mCurApkInfoList.get(i).mApkName);
                out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_APK_PATH, this.mCurApkInfoList.get(i).mPath);
                out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_APK_LAST_MODIFY, this.mCurApkInfoList.get(i).mLastModifyTime);
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append(this.mCurApkInfoList.get(i).mVersion);
                out.attribute(null, HwSecDiagnoseConstant.ANTIMAL_APK_VERSION, stringBuffer2.toString());
                out.endTag(null, TAG_ITEM);
            }
        }
        out.endTag(null, APPS);
    }

    private void writeComponentInfoList(XmlSerializer out) throws IOException, IllegalArgumentException, IllegalStateException {
        out.startTag(null, COMPONENT);
        int listSize = this.mCurComponentList.size();
        for (int i = 0; i < listSize; i++) {
            if (HW_DEBUG) {
                Log.d(TAG, "writeComponentInfoList AntiMalComponentInfo : " + this.mCurComponentList.get(i));
            }
            if (this.mCurComponentList.get(i) != null) {
                out.startTag(null, TAG_ITEM);
                out.attribute(null, "name", this.mCurComponentList.get(i).mName);
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(this.mCurComponentList.get(i).mVerifyStatus);
                out.attribute(null, AntiMalComponentInfo.VERIFY_STATUS, stringBuffer.toString());
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append(this.mCurComponentList.get(i).mAntimalTypeMask);
                out.attribute(null, AntiMalComponentInfo.ANTIMAL_TYPE_MASK, stringBuffer2.toString());
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
                Log.e(TAG, "formatData ParseException!" + e);
            }
        }
        return patch;
    }

    private String getCurrentTime() {
        return formatTime(System.currentTimeMillis());
    }
}
