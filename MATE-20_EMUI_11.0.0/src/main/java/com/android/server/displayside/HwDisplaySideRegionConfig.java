package com.android.server.displayside;

import android.content.pm.PackageInfo;
import android.os.Environment;
import android.os.ServiceManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.pm.PackageManagerService;
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

public class HwDisplaySideRegionConfig {
    private static final String CLOUD_WHITE_LIST_FILE_DIR = "/data/cota/para/xml/HwExtDisplay/displayside";
    private static final String COMPATIBLE_PRESET_WHITE_LIST_FILE_DIR = "xml/";
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
    private PackageManagerService mPMS;
    private HashMap<String, Integer> mSendVolumeKeyAppsInWhiteList;
    private List<String> oldAppsInWhiteList;

    private HwDisplaySideRegionConfig() {
        init();
    }

    private void init() {
        Slog.d(TAG, "do init");
        this.mCompressAppsInWhiteList = new ArrayMap<>();
        this.mExtendAppsInUserSettings = new LinkedList();
        this.mSendVolumeKeyAppsInWhiteList = new HashMap<>();
        this.oldAppsInWhiteList = new ArrayList();
        this.mAppsInWhiteList = new ArrayMap<>();
        this.mPMS = ServiceManager.getService("package");
        if (this.mPMS == null) {
            Slog.e(TAG, "init failed, mPMS is null");
        } else {
            updateWhitelist();
        }
    }

    private boolean isFirstBootOrUpgrade() {
        return this.mPMS.isFirstBoot() || this.mPMS.isDeviceUpgrading();
    }

    private void loadWhiteList() {
        File whitelistFile = null;
        try {
            File backupFile = new File(Environment.getDataSystemDirectory(), DEFAULT_WHITE_LIST);
            File presetFile = HwCfgFilePolicy.getCfgFile("xml/HwExtDisplay/displayside/display_side_region_whitelist.xml", 0);
            whitelistFile = findNewerFile(backupFile, presetFile);
            if (whitelistFile == presetFile) {
                Slog.d(TAG, "loadWhiteList use presetFile");
            } else {
                Slog.d(TAG, "loadWhiteList use backupFile");
            }
        } catch (NoClassDefFoundError e) {
            Slog.e(TAG, "loadWhiteList NoClassDefFoundError");
        } catch (Exception e2) {
            Slog.e(TAG, "loadWhiteList unkown Exception : " + e2.getClass());
        }
        parseWhiteList(whitelistFile);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00ad, code lost:
        if (com.android.server.displayside.HwDisplaySideRegionConfig.XML_WHITE_LIST_NAME.equals(r5.getName()) != false) goto L_0x00b9;
     */
    private void parseWhiteList(File whiteList) {
        StringBuilder sb;
        String installedVersionName;
        if (whiteList == null || !whiteList.exists()) {
            Slog.e(TAG, "parseWhiteList whiteList is not exist");
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
                    String versionName = xmlParser.getAttributeValue(null, "version");
                    if (!this.mAppsInWhiteList.containsKey(compressApp)) {
                        this.mAppsInWhiteList.put(compressApp, versionName);
                    }
                } else if (xmlEventType == 2 && XML_ACCEPT_VOLUMEKEY_APP_NAME.equals(xmlParser.getName())) {
                    String specialApp = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_NAME);
                    String versionName2 = xmlParser.getAttributeValue(null, "version");
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
            Slog.e(TAG, "parseWhiteList whitelist not found!");
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
            Slog.e(TAG, "parseWhiteList: " + e4.getMessage());
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
            Slog.e(TAG, "parseWhiteList: " + e6.getMessage());
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
                    Slog.e(TAG, "parseWhiteList: IO Exception while closing stream" + e8.getMessage());
                }
                Slog.d(TAG, "parseWhiteList end.");
            }
            throw th;
        }
        Slog.d(TAG, "parseWhiteList end.");
        sb.append("parseWhiteList: IO Exception while closing stream");
        sb.append(e.getMessage());
        Slog.e(TAG, sb.toString());
        Slog.d(TAG, "parseWhiteList end.");
    }

    private void initExtendAppsInUserSettings() {
        this.mExtendAppsInUserSettings.addAll(this.mPMS.getAppsUseSideList());
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
        return "0";
    }

    public boolean isAppShouldSendVolumeKey(String packageName, int productMode) {
        Integer configTarget;
        HashMap<String, Integer> hashMap = this.mSendVolumeKeyAppsInWhiteList;
        if (hashMap == null || (configTarget = hashMap.get(packageName)) == null || (configTarget.intValue() & productMode) != productMode) {
            return false;
        }
        return true;
    }

    public void updateWhitelistByOuc() {
        Slog.d(TAG, "updateWhitelistByOuc");
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

    private void updateWhitelist() {
        String installedVersionName;
        Slog.d(TAG, "start to updateWhitelist");
        if (this.oldAppsInWhiteList.size() == 0) {
            Slog.d(TAG, "oldAppsInWhiteList is empty.");
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
                Slog.d(TAG, "package is not in white list, pkgName: " + pkgName);
                extendApps.put(pkgName, "0");
            } else {
                String versionName = this.mAppsInWhiteList.get(pkgName);
                if (versionName == null || (installedVersionName = getInstalledPackageVersion(pkgName)) == null || compareVersion(installedVersionName, versionName) < 0) {
                    this.mCompressAppsInWhiteList.put(pkgName, versionName);
                } else {
                    extendApps.put(pkgName, versionName);
                }
            }
        }
        this.mPMS.updateAppsUseSideWhitelist(this.mCompressAppsInWhiteList, extendApps);
        initExtendAppsInUserSettings();
        Slog.d(TAG, "end to updateWhitelist");
    }

    private void loadCloudFile() {
        File cloudFile = new File(CLOUD_WHITE_LIST_FILE_DIR, DEFAULT_WHITE_LIST);
        if (!cloudFile.exists()) {
            Slog.d(TAG, "cloudFile is not exists");
        } else if (findNewerFile(new File(Environment.getDataSystemDirectory(), DEFAULT_WHITE_LIST), cloudFile).equals(cloudFile)) {
            Slog.d(TAG, "cloudFile is updated, update backupFile.");
            File backupFile = createFileForBackup(DEFAULT_WHITE_LIST);
            if (backupFile != null) {
                parseConfigsToTargetFile(backupFile, cloudFile);
                this.mAppsInWhiteList.clear();
                this.mSendVolumeKeyAppsInWhiteList.clear();
                loadWhiteList();
            }
        }
    }

    public String getInstalledPackageVersion(String pkgName) {
        PackageInfo pInfo = this.mPMS.getPackageInfo(pkgName, 16384, 0);
        if (pInfo != null) {
            return pInfo.versionName;
        }
        return null;
    }

    public int compareVersion(String version1, String version2) {
        int minLength;
        if (version1 == null || version2 == null) {
            Slog.d(TAG, "version is null");
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

    private static File createFileForBackup(String fileName) {
        File file = new File(Environment.getDataSystemDirectory(), fileName);
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
