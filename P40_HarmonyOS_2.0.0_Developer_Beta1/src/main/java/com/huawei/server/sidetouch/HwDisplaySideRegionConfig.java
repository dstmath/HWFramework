package com.huawei.server.sidetouch;

import android.content.pm.PackageInfo;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Xml;
import com.huawei.android.os.EnvironmentEx;
import com.huawei.server.pm.PackageManagerServiceEx;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwDisplaySideRegionConfig extends DefaultHwDisplaySideRegionConfig {
    private static final String CLOUD_WHITE_LIST_FILE_DIR = "/data/cota/para/xml/HwExtDisplay/displayside";
    private static final String DEFAULT_WHITE_LIST = "display_side_region_whitelist.xml";
    private static final String PRESET_WHITE_LIST_FILE_DIR = "xml/HwExtDisplay/displayside/";
    private static final String TAG = "HwDisplaySideRegionConfig";
    private static final String XML_ACCEPT_VOLUMEKEY_APP_NAME = "send_volumekey_app";
    private static final String XML_ATTRIBUTE_NAME = "name";
    private static final String XML_COMPRESS_DISPLAY_APP_NAME = "compress_display_app";
    private static final String XML_TARGET_NAME = "target";
    private static final String XML_VERSION_NAME = "version";
    private static final String XML_WHITE_LIST_NAME = "display_side_region_whitelist";
    private static HwDisplaySideRegionConfig mInstance;
    private ArrayMap<String, String> mAppsInWhiteList;
    private ArrayMap<String, String> mCompressAppsInWhiteList;
    private List<String> mExtendAppsInUserSettings;
    private PackageManagerServiceEx mPmsEx;
    private HashMap<String, Integer> mSendVolumeKeyAppsInWhiteList;
    private List<String> oldAppsInWhiteList;

    private HwDisplaySideRegionConfig() {
        init();
    }

    private void init() {
        Log.d(TAG, "do init");
        this.mCompressAppsInWhiteList = new ArrayMap<>();
        this.mExtendAppsInUserSettings = new LinkedList();
        this.mSendVolumeKeyAppsInWhiteList = new HashMap<>();
        this.oldAppsInWhiteList = new ArrayList();
        this.mAppsInWhiteList = new ArrayMap<>();
    }

    private void loadWhiteList() {
        File whitelistFile = null;
        try {
            File backupFile = new File(EnvironmentEx.getDataSystemDirectory(), DEFAULT_WHITE_LIST);
            File presetFile = HwCfgFilePolicy.getCfgFile("xml/HwExtDisplay/displayside/display_side_region_whitelist.xml", 0);
            whitelistFile = findNewerFile(backupFile, presetFile);
            if (whitelistFile == presetFile) {
                Log.d(TAG, "loadWhiteList use presetFile");
            } else {
                Log.d(TAG, "loadWhiteList use backupFile");
            }
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "loadWhiteList NoClassDefFoundError");
        } catch (Exception e2) {
            Log.e(TAG, "loadWhiteList unkown Exception : " + e2.getClass());
        }
        parseWhiteList(whitelistFile);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00aa, code lost:
        if (com.huawei.server.sidetouch.HwDisplaySideRegionConfig.XML_WHITE_LIST_NAME.equals(r5.getName()) != false) goto L_0x00b6;
     */
    private void parseWhiteList(File whiteList) {
        StringBuilder sb;
        String installedVersionName;
        if (whiteList == null || !whiteList.exists()) {
            Log.e(TAG, "parseWhiteList whiteList is not exist");
            return;
        }
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(whiteList);
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(inputStream, null);
            int xmlEventType = xmlParser.next();
            while (xmlEventType != 1) {
                if (xmlEventType == 2 && XML_COMPRESS_DISPLAY_APP_NAME.equals(xmlParser.getName())) {
                    String compressApp = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_NAME);
                    String versionName = xmlParser.getAttributeValue(null, XML_VERSION_NAME);
                    if (!this.mAppsInWhiteList.containsKey(compressApp)) {
                        this.mAppsInWhiteList.put(compressApp, versionName);
                    }
                } else if (xmlEventType == 2 && XML_ACCEPT_VOLUMEKEY_APP_NAME.equals(xmlParser.getName())) {
                    String specialApp = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_NAME);
                    String versionName2 = xmlParser.getAttributeValue(null, XML_VERSION_NAME);
                    Integer target = Integer.valueOf(parseTarget(xmlParser.getAttributeValue(null, XML_TARGET_NAME)));
                    if (versionName2 != null && (installedVersionName = getInstalledPackageVersion(specialApp)) != null && compareVersion(installedVersionName, versionName2) >= 0) {
                        xmlEventType = xmlParser.next();
                    } else if (!(specialApp == null || this.mSendVolumeKeyAppsInWhiteList == null || this.mSendVolumeKeyAppsInWhiteList.containsKey(specialApp))) {
                        this.mSendVolumeKeyAppsInWhiteList.put(specialApp, target);
                    }
                } else if (xmlEventType == 3) {
                }
                xmlEventType = xmlParser.next();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e = e;
                sb = new StringBuilder();
            }
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "parseWhiteList whitelist not found!");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            } else {
                return;
            }
        } catch (XmlPullParserException e4) {
            Log.e(TAG, "parseWhiteList: " + e4.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            } else {
                return;
            }
        } catch (IOException e6) {
            Log.e(TAG, "parseWhiteList: " + e6.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e7) {
                    e = e7;
                    sb = new StringBuilder();
                }
            } else {
                return;
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e8) {
                    Log.e(TAG, "parseWhiteList: IO Exception while closing stream" + e8.getMessage());
                }
                Log.d(TAG, "parseWhiteList end.");
            }
            throw th;
        }
        Log.d(TAG, "parseWhiteList end.");
        sb.append("parseWhiteList: IO Exception while closing stream");
        sb.append(e.getMessage());
        Log.e(TAG, sb.toString());
        Log.d(TAG, "parseWhiteList end.");
    }

    private void initExtendAppsInUserSettings() {
        PackageManagerServiceEx packageManagerServiceEx = this.mPmsEx;
        if (packageManagerServiceEx != null) {
            this.mExtendAppsInUserSettings.addAll(packageManagerServiceEx.getAppsUseSideList());
        }
    }

    public static synchronized HwDisplaySideRegionConfig getInstance() {
        HwDisplaySideRegionConfig hwDisplaySideRegionConfig;
        synchronized (HwDisplaySideRegionConfig.class) {
            if (mInstance == null) {
                mInstance = new HwDisplaySideRegionConfig();
            }
            hwDisplaySideRegionConfig = mInstance;
        }
        return hwDisplaySideRegionConfig;
    }

    public void updateExtendApp(String packageName, boolean isExtend) {
        if (isExtend) {
            if (!this.mExtendAppsInUserSettings.contains(packageName)) {
                this.mExtendAppsInUserSettings.add(packageName);
            }
        } else if (this.mExtendAppsInUserSettings.contains(packageName)) {
            this.mExtendAppsInUserSettings.remove(packageName);
        }
    }

    public boolean isExtendApp(String packageName) {
        return this.mExtendAppsInUserSettings.contains(packageName);
    }

    public boolean isAppInWhiteList(String packageName) {
        return this.mCompressAppsInWhiteList.containsKey(packageName);
    }

    public String getAppVersionInWhiteList(String packageName) {
        if (this.mAppsInWhiteList.containsKey(packageName)) {
            return this.mAppsInWhiteList.get(packageName);
        }
        return HwSideStatusManager.AUDIO_STATE_NONE;
    }

    public void updateWhitelistByOuc() {
        Log.d(TAG, "updateWhitelistByOuc");
        this.oldAppsInWhiteList.clear();
        this.oldAppsInWhiteList.addAll(this.mAppsInWhiteList.keySet());
        this.mCompressAppsInWhiteList.clear();
        this.mExtendAppsInUserSettings.clear();
        this.mAppsInWhiteList.clear();
        HashMap<String, Integer> hashMap = this.mSendVolumeKeyAppsInWhiteList;
        if (hashMap != null) {
            hashMap.clear();
        }
        updateWhitelist();
    }

    /* access modifiers changed from: package-private */
    public void systemReady() {
        this.mPmsEx = new PackageManagerServiceEx();
        updateWhitelist();
    }

    /* access modifiers changed from: package-private */
    public boolean isAppShouldSendVolumeKey(String packageName, int productMode) {
        Integer configTarget;
        HashMap<String, Integer> hashMap = this.mSendVolumeKeyAppsInWhiteList;
        if (hashMap == null || (configTarget = hashMap.get(packageName)) == null || (configTarget.intValue() & productMode) != productMode) {
            return false;
        }
        return true;
    }

    private void updateWhitelist() {
        String installedVersionName;
        Log.d(TAG, "start to updateWhitelist");
        if (this.oldAppsInWhiteList.size() == 0) {
            Log.d(TAG, "oldAppsInWhiteList is empty.");
            loadWhiteList();
            this.oldAppsInWhiteList.addAll(this.mAppsInWhiteList.keySet());
        }
        loadCloudFile();
        ArrayMap<String, String> extendApps = new ArrayMap<>();
        Set<String> pkgNames = new HashSet<>();
        pkgNames.addAll(this.mAppsInWhiteList.keySet());
        pkgNames.addAll(this.oldAppsInWhiteList);
        for (String pkgName : pkgNames) {
            if (!this.mAppsInWhiteList.containsKey(pkgName)) {
                Log.d(TAG, "package is not in white list, pkgName: " + pkgName);
                extendApps.put(pkgName, HwSideStatusManager.AUDIO_STATE_NONE);
            } else {
                String versionName = this.mAppsInWhiteList.get(pkgName);
                if (versionName == null || (installedVersionName = getInstalledPackageVersion(pkgName)) == null || compareVersion(installedVersionName, versionName) < 0) {
                    this.mCompressAppsInWhiteList.put(pkgName, versionName);
                } else {
                    extendApps.put(pkgName, versionName);
                }
            }
        }
        PackageManagerServiceEx packageManagerServiceEx = this.mPmsEx;
        if (packageManagerServiceEx != null) {
            packageManagerServiceEx.updateAppsUseSideWhitelist(this.mCompressAppsInWhiteList, extendApps);
        }
        initExtendAppsInUserSettings();
        Log.d(TAG, "end to updateWhitelist");
    }

    private void loadCloudFile() {
        File cloudFile = new File(CLOUD_WHITE_LIST_FILE_DIR, DEFAULT_WHITE_LIST);
        if (!cloudFile.exists()) {
            Log.d(TAG, "cloudFile is not exists");
        } else if (findNewerFile(new File(EnvironmentEx.getDataSystemDirectory(), DEFAULT_WHITE_LIST), cloudFile).equals(cloudFile)) {
            Log.d(TAG, "cloudFile is updated, update backupFile.");
            File backupFile = createFileForBackup(DEFAULT_WHITE_LIST);
            if (backupFile != null) {
                parseConfigsToTargetFile(backupFile, cloudFile);
                this.mAppsInWhiteList.clear();
                this.mSendVolumeKeyAppsInWhiteList.clear();
                loadWhiteList();
            }
        }
    }

    public int compareVersion(String version1, String version2) {
        int minLength;
        if (version1 == null || version2 == null) {
            Log.d(TAG, "version is null");
            return 0;
        }
        String[] versionArray1 = version1.split("\\.");
        String[] versionArray2 = version2.split("\\.");
        if (versionArray1.length >= versionArray2.length) {
            minLength = versionArray2.length;
        } else {
            minLength = versionArray1.length;
        }
        int diff = 0;
        for (int idx = 0; idx < minLength; idx++) {
            int length = versionArray1[idx].length() - versionArray2[idx].length();
            diff = length;
            if (length != 0) {
                break;
            }
            int compareTo = versionArray1[idx].compareTo(versionArray2[idx]);
            diff = compareTo;
            if (compareTo != 0) {
                break;
            }
        }
        return diff != 0 ? diff : versionArray1.length - versionArray2.length;
    }

    private String getInstalledPackageVersion(String pkgName) {
        PackageInfo pInfo;
        PackageManagerServiceEx packageManagerServiceEx = this.mPmsEx;
        if (packageManagerServiceEx == null || (pInfo = packageManagerServiceEx.getPackageInfo(pkgName, 16384, 0)) == null) {
            return null;
        }
        return pInfo.versionName;
    }

    private static File createFileForBackup(String fileName) {
        File file = new File(EnvironmentEx.getDataSystemDirectory(), fileName);
        if (!file.exists() || file.delete()) {
            try {
                if (!file.createNewFile()) {
                    Log.e(TAG, "createFileForWrite createNewFile error!");
                    return null;
                }
                file.setReadable(true, false);
                return file;
            } catch (IOException ioException) {
                Log.e(TAG, "ioException: " + ioException);
                return null;
            }
        } else {
            Log.e(TAG, "delete file error!");
            return null;
        }
    }

    private void parseConfigsToTargetFile(File targetFile, File srcFile) {
        FileInputStream inputStream = null;
        BufferedReader reader = null;
        FileOutputStream outputStream = null;
        InputStreamReader inputStreamReader = null;
        StringBuilder targetStringBuilder = new StringBuilder();
        boolean recordStarted = true;
        try {
            inputStream = new FileInputStream(srcFile);
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            reader = new BufferedReader(inputStreamReader);
            while (true) {
                String tempLineString = reader.readLine();
                if (tempLineString == null) {
                    break;
                }
                String tempLineString2 = tempLineString.trim();
                if (recordStarted) {
                    targetStringBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                    recordStarted = false;
                } else {
                    targetStringBuilder.append("\n");
                    targetStringBuilder.append(tempLineString2);
                }
            }
            outputStream = new FileOutputStream(targetFile);
            byte[] outputString = targetStringBuilder.toString().getBytes("utf-8");
            outputStream.write(outputString, 0, outputString.length);
        } catch (IOException e) {
            deleteAbnormalXml(targetFile);
            Log.e(TAG, "parseConfigsToTargetFile IOException :" + e);
        } catch (RuntimeException e2) {
            deleteAbnormalXml(targetFile);
            Log.e(TAG, "parseConfigsToTargetFile RuntimeException :" + e2);
        } catch (Throwable th) {
            closeBufferedReader(null);
            closeInputStreamReader(null);
            closeFileOutputStream(null);
            closeInputStream(null);
            throw th;
        }
        closeBufferedReader(reader);
        closeInputStreamReader(inputStreamReader);
        closeFileOutputStream(outputStream);
        closeInputStream(inputStream);
    }

    private void deleteAbnormalXml(File file) {
        if (file.exists() && !file.delete()) {
            Log.e(TAG, "delete abnormal xml error!");
        }
    }

    private void closeBufferedReader(BufferedReader bufferedReader) {
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                Log.e(TAG, "closeBufferedReader error!");
            }
        }
    }

    private void closeInputStreamReader(InputStreamReader inputStreamReader) {
        if (inputStreamReader != null) {
            try {
                inputStreamReader.close();
            } catch (IOException e) {
                Log.e(TAG, "closeInputStreamReader error!");
            }
        }
    }

    private void closeFileOutputStream(FileOutputStream fileOutputStream) {
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "closeFileOutputStream error!");
            }
        }
    }

    private void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "closeInputStream error!");
            }
        }
    }

    private static File findNewerFile(File fileA, File fileB) {
        if (fileA == null || !fileA.exists()) {
            return fileB;
        }
        if (fileB == null || !fileB.exists() || fileA.lastModified() >= fileB.lastModified()) {
            return fileA;
        }
        return fileB;
    }

    private int parseTarget(String target) {
        if (target == null) {
            return 3;
        }
        try {
            return Integer.parseInt(target);
        } catch (NumberFormatException e) {
            Log.e(TAG, "invalid target:" + target);
            return 3;
        }
    }
}
