package com.android.server.notch;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManagerInternal;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.WindowState;
import com.android.server.LocalServices;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwNotchScreenWhiteConfig {
    private static final String CURRENT_ROM_VERSION = SystemProperties.get("ro.build.version.incremental", "B001");
    public static final String DISPLAY_NOTCH_STATUS = "display_notch_status";
    private static final int NOTCH_SCREEN_TYPE = 0;
    private static final String NOTCH_SCREEN_WHITE_LIST = "notch_screen_whitelist.xml";
    private static final String TAG = "HwNotchScreenWhiteConfig";
    private static final String XML_ATTRIBUTE_NAME = "name";
    private static final String XML_NONE_NOTCH_NAME = "none_notch_app";
    private static final String XML_NONE_NOTCH_NAME_HIDE = "none_notch_app_hide";
    private static final String XML_NONE_NOTCH_NAME_WITH_STATUSBAR = "none_notch_app_with_statusbar";
    private static final String XML_NOTCH_APP_NAME = "notch_app";
    private static final String XML_NOTCH_APP_NAME_HIDE = "notch_app_hide";
    private static final String XML_NOTCH_SYSTEM_APP = "system_app";
    private static final String XML_Notch_WhITE_LIST = "notchscreen_whitelist";
    private static HwNotchScreenWhiteConfig hwNotchScreenWhiteConfig;
    private static final Integer[] typeSupport = new Integer[]{Integer.valueOf(2009), Integer.valueOf(2010), Integer.valueOf(2023)};
    private Map<String, Boolean> mInstalledAppVersionCode;
    private Map<String, Integer> mWhiteAppVersionCode;
    private List<String> noneNotchAppInfos;
    private List<String> noneNotchAppWithStatusbarInfos;
    private List<String> noneNotchHideAppInfos;
    private List<String> notchAppHideInfos;
    private List<String> notchAppInfos;
    private List<String> systemAppInfos;

    public static abstract class NotchSwitchListener {
        final ContentObserver mObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange, Uri uri) {
                NotchSwitchListener.this.onChange();
            }
        };

        public abstract void onChange();
    }

    private static class WhitelistUpdateThread extends Thread {
        Context mContext = null;
        String mFileName = null;

        protected WhitelistUpdateThread(Context context, String fileName) {
            super("config update thread");
            this.mContext = context;
            this.mFileName = fileName;
        }

        public void run() {
            if (this.mFileName != null) {
                try {
                    FileDescriptor fileDescriptor = this.mContext.getContentResolver().openFileDescriptor(Uri.parse(this.mFileName), "data/system").getFileDescriptor();
                    File targetFileTemp = createFileForWrite();
                    if (!(targetFileTemp == null || fileDescriptor == null)) {
                        parseConfigsToTargetFile(targetFileTemp, fileDescriptor);
                    }
                } catch (FileNotFoundException e) {
                    Log.e(HwNotchScreenWhiteConfig.TAG, "FileNotFoundException:" + e);
                }
            }
        }

        public static File createFileForWrite() {
            File file = new File(Environment.getDataSystemDirectory(), "notch_screen_whitelist_" + HwNotchScreenWhiteConfig.CURRENT_ROM_VERSION + ".xml");
            if (!file.exists() || file.delete()) {
                try {
                    if (file.createNewFile()) {
                        file.setReadable(true, false);
                        return file;
                    }
                    Log.e(HwNotchScreenWhiteConfig.TAG, "createFileForWrite createNewFile error!");
                    return null;
                } catch (IOException ioException) {
                    Log.e(HwNotchScreenWhiteConfig.TAG, "ioException: " + ioException);
                    return null;
                }
            }
            Log.e(HwNotchScreenWhiteConfig.TAG, "delete file error!");
            return null;
        }

        private void parseConfigsToTargetFile(File targetFile, FileDescriptor fileDescriptor) {
            IOException e;
            Throwable th;
            RuntimeException e2;
            BufferedReader reader = null;
            FileOutputStream outputStream = null;
            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            StringBuilder targetStringBuilder = new StringBuilder();
            boolean recordStarted = true;
            try {
                InputStreamReader inputStreamReader2;
                InputStream inputStream2 = new FileInputStream(fileDescriptor);
                try {
                    inputStreamReader2 = new InputStreamReader(inputStream2, "utf-8");
                } catch (IOException e3) {
                    e = e3;
                    inputStream = inputStream2;
                    try {
                        deleteAbnormalXml(targetFile);
                        Log.e(HwNotchScreenWhiteConfig.TAG, "parseConfigsToTargetFile IOException :" + e);
                        closeBufferedReader(reader);
                        closeInputStreamReader(inputStreamReader);
                        closeInputStream(inputStream);
                        closeFileOutputStream(outputStream);
                        HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
                    } catch (Throwable th2) {
                        th = th2;
                        closeBufferedReader(reader);
                        closeInputStreamReader(inputStreamReader);
                        closeInputStream(inputStream);
                        closeFileOutputStream(outputStream);
                        HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
                        throw th;
                    }
                } catch (RuntimeException e4) {
                    e2 = e4;
                    inputStream = inputStream2;
                    deleteAbnormalXml(targetFile);
                    Log.e(HwNotchScreenWhiteConfig.TAG, "parseConfigsToTargetFile RuntimeException :" + e2);
                    closeBufferedReader(reader);
                    closeInputStreamReader(inputStreamReader);
                    closeInputStream(inputStream);
                    closeFileOutputStream(outputStream);
                    HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
                } catch (Throwable th3) {
                    th = th3;
                    inputStream = inputStream2;
                    closeBufferedReader(reader);
                    closeInputStreamReader(inputStreamReader);
                    closeInputStream(inputStream);
                    closeFileOutputStream(outputStream);
                    HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
                    throw th;
                }
                try {
                    BufferedReader reader2 = new BufferedReader(inputStreamReader2);
                    while (true) {
                        try {
                            String tempLineString = reader2.readLine();
                            if (tempLineString != null) {
                                tempLineString = tempLineString.trim();
                                if (recordStarted) {
                                    targetStringBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                                    recordStarted = false;
                                } else {
                                    targetStringBuilder.append("\n").append(tempLineString);
                                }
                            } else {
                                FileOutputStream outputStream2 = new FileOutputStream(targetFile);
                                try {
                                    byte[] outputString = targetStringBuilder.toString().getBytes("utf-8");
                                    outputStream2.write(outputString, 0, outputString.length);
                                    closeBufferedReader(reader2);
                                    closeInputStreamReader(inputStreamReader2);
                                    closeInputStream(inputStream2);
                                    closeFileOutputStream(outputStream2);
                                    HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
                                    return;
                                } catch (IOException e5) {
                                    e = e5;
                                    inputStreamReader = inputStreamReader2;
                                    inputStream = inputStream2;
                                    outputStream = outputStream2;
                                    reader = reader2;
                                    deleteAbnormalXml(targetFile);
                                    Log.e(HwNotchScreenWhiteConfig.TAG, "parseConfigsToTargetFile IOException :" + e);
                                    closeBufferedReader(reader);
                                    closeInputStreamReader(inputStreamReader);
                                    closeInputStream(inputStream);
                                    closeFileOutputStream(outputStream);
                                    HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
                                } catch (RuntimeException e6) {
                                    e2 = e6;
                                    inputStreamReader = inputStreamReader2;
                                    inputStream = inputStream2;
                                    outputStream = outputStream2;
                                    reader = reader2;
                                    deleteAbnormalXml(targetFile);
                                    Log.e(HwNotchScreenWhiteConfig.TAG, "parseConfigsToTargetFile RuntimeException :" + e2);
                                    closeBufferedReader(reader);
                                    closeInputStreamReader(inputStreamReader);
                                    closeInputStream(inputStream);
                                    closeFileOutputStream(outputStream);
                                    HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
                                } catch (Throwable th4) {
                                    th = th4;
                                    inputStreamReader = inputStreamReader2;
                                    inputStream = inputStream2;
                                    outputStream = outputStream2;
                                    reader = reader2;
                                    closeBufferedReader(reader);
                                    closeInputStreamReader(inputStreamReader);
                                    closeInputStream(inputStream);
                                    closeFileOutputStream(outputStream);
                                    HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
                                    throw th;
                                }
                            }
                        } catch (IOException e7) {
                            e = e7;
                            inputStreamReader = inputStreamReader2;
                            inputStream = inputStream2;
                            reader = reader2;
                            deleteAbnormalXml(targetFile);
                            Log.e(HwNotchScreenWhiteConfig.TAG, "parseConfigsToTargetFile IOException :" + e);
                            closeBufferedReader(reader);
                            closeInputStreamReader(inputStreamReader);
                            closeInputStream(inputStream);
                            closeFileOutputStream(outputStream);
                            HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
                        } catch (RuntimeException e8) {
                            e2 = e8;
                            inputStreamReader = inputStreamReader2;
                            inputStream = inputStream2;
                            reader = reader2;
                            deleteAbnormalXml(targetFile);
                            Log.e(HwNotchScreenWhiteConfig.TAG, "parseConfigsToTargetFile RuntimeException :" + e2);
                            closeBufferedReader(reader);
                            closeInputStreamReader(inputStreamReader);
                            closeInputStream(inputStream);
                            closeFileOutputStream(outputStream);
                            HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
                        } catch (Throwable th5) {
                            th = th5;
                            inputStreamReader = inputStreamReader2;
                            inputStream = inputStream2;
                            reader = reader2;
                            closeBufferedReader(reader);
                            closeInputStreamReader(inputStreamReader);
                            closeInputStream(inputStream);
                            closeFileOutputStream(outputStream);
                            HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
                            throw th;
                        }
                    }
                } catch (IOException e9) {
                    e = e9;
                    inputStreamReader = inputStreamReader2;
                    inputStream = inputStream2;
                    deleteAbnormalXml(targetFile);
                    Log.e(HwNotchScreenWhiteConfig.TAG, "parseConfigsToTargetFile IOException :" + e);
                    closeBufferedReader(reader);
                    closeInputStreamReader(inputStreamReader);
                    closeInputStream(inputStream);
                    closeFileOutputStream(outputStream);
                    HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
                } catch (RuntimeException e10) {
                    e2 = e10;
                    inputStreamReader = inputStreamReader2;
                    inputStream = inputStream2;
                    deleteAbnormalXml(targetFile);
                    Log.e(HwNotchScreenWhiteConfig.TAG, "parseConfigsToTargetFile RuntimeException :" + e2);
                    closeBufferedReader(reader);
                    closeInputStreamReader(inputStreamReader);
                    closeInputStream(inputStream);
                    closeFileOutputStream(outputStream);
                    HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
                } catch (Throwable th6) {
                    th = th6;
                    inputStreamReader = inputStreamReader2;
                    inputStream = inputStream2;
                    closeBufferedReader(reader);
                    closeInputStreamReader(inputStreamReader);
                    closeInputStream(inputStream);
                    closeFileOutputStream(outputStream);
                    HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
                    throw th;
                }
            } catch (IOException e11) {
                e = e11;
                deleteAbnormalXml(targetFile);
                Log.e(HwNotchScreenWhiteConfig.TAG, "parseConfigsToTargetFile IOException :" + e);
                closeBufferedReader(reader);
                closeInputStreamReader(inputStreamReader);
                closeInputStream(inputStream);
                closeFileOutputStream(outputStream);
                HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
            } catch (RuntimeException e12) {
                e2 = e12;
                deleteAbnormalXml(targetFile);
                Log.e(HwNotchScreenWhiteConfig.TAG, "parseConfigsToTargetFile RuntimeException :" + e2);
                closeBufferedReader(reader);
                closeInputStreamReader(inputStreamReader);
                closeInputStream(inputStream);
                closeFileOutputStream(outputStream);
                HwNotchScreenWhiteConfig.getInstance().updateWhiteListData();
            }
        }

        private void deleteAbnormalXml(File file) {
            try {
                if (file.exists() && !file.delete()) {
                    Log.e(HwNotchScreenWhiteConfig.TAG, "delete abnormal xml error!");
                }
            } catch (Exception e) {
                Log.e(HwNotchScreenWhiteConfig.TAG, "Delete the abnormal xml Fail!!!");
            }
        }

        private void closeBufferedReader(BufferedReader bufferedReader) {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(HwNotchScreenWhiteConfig.TAG, "closeBufferedReader error!");
                }
            }
        }

        private void closeInputStreamReader(InputStreamReader inputStreamReader) {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    Log.e(HwNotchScreenWhiteConfig.TAG, "closeInputStreamReader error!");
                }
            }
        }

        private void closeInputStream(InputStream inputStream) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(HwNotchScreenWhiteConfig.TAG, "closeInputStream error!");
                }
            }
        }

        private void closeFileOutputStream(FileOutputStream fileOutputStream) {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.e(HwNotchScreenWhiteConfig.TAG, "closeFileOutputStream error!");
                }
            }
        }
    }

    public static HwNotchScreenWhiteConfig getInstance() {
        if (hwNotchScreenWhiteConfig == null) {
            hwNotchScreenWhiteConfig = new HwNotchScreenWhiteConfig();
        }
        return hwNotchScreenWhiteConfig;
    }

    private HwNotchScreenWhiteConfig() {
        initData();
    }

    public boolean notchSupportWindow(LayoutParams attrs) {
        return Arrays.asList(typeSupport).contains(Integer.valueOf(attrs.type));
    }

    public void updateWhiteListData() {
        initData();
    }

    private void initData() {
        this.notchAppInfos = new ArrayList();
        this.noneNotchAppInfos = new ArrayList();
        this.notchAppHideInfos = new ArrayList();
        this.noneNotchHideAppInfos = new ArrayList();
        this.noneNotchAppWithStatusbarInfos = new ArrayList();
        this.systemAppInfos = new ArrayList();
        this.mWhiteAppVersionCode = new HashMap();
        this.mInstalledAppVersionCode = new HashMap();
        loadNotchScreenWhiteList();
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0065 A:{Catch:{ FileNotFoundException -> 0x00dd, XmlPullParserException -> 0x0122, IOException -> 0x0177, all -> 0x01ce }} */
    /* JADX WARNING: Removed duplicated region for block: B:106:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x024b A:{SYNTHETIC, Splitter: B:90:0x024b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadNotchScreenWhiteList() {
        InputStream inputStream = null;
        File notchScreenFile = null;
        try {
            File newFile = new File(Environment.getDataSystemDirectory(), "notch_screen_whitelist_" + CURRENT_ROM_VERSION + ".xml");
            if (newFile.exists()) {
                notchScreenFile = newFile;
                Log.i(TAG, "update new file:" + newFile);
            } else {
                notchScreenFile = HwCfgFilePolicy.getCfgFile("xml/notch_screen_whitelist.xml", 0);
            }
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        } catch (Exception e2) {
            Log.d(TAG, "HwCfgFilePolicy get notch_screen_whitelist exception");
        }
        if (notchScreenFile != null) {
            try {
                if (notchScreenFile.exists()) {
                    inputStream = new FileInputStream(notchScreenFile);
                    if (inputStream != null) {
                        XmlPullParser xmlParser = Xml.newPullParser();
                        xmlParser.setInput(inputStream, null);
                        for (int xmlEventType = xmlParser.next(); xmlEventType != 1; xmlEventType = xmlParser.next()) {
                            if (xmlEventType == 2 && XML_NOTCH_APP_NAME.equals(xmlParser.getName())) {
                                String notchApp = xmlParser.getAttributeValue(null, "name");
                                addNotchAppInfo(notchApp);
                                getVersionCode(xmlParser, notchApp);
                            } else {
                                if (xmlEventType == 2) {
                                    if (XML_NOTCH_APP_NAME_HIDE.equals(xmlParser.getName())) {
                                        String notchAppHide = xmlParser.getAttributeValue(null, "name");
                                        addNotchAppHideInfo(notchAppHide);
                                        getVersionCode(xmlParser, notchAppHide);
                                    }
                                }
                                if (xmlEventType == 2) {
                                    if (XML_NONE_NOTCH_NAME.equals(xmlParser.getName())) {
                                        String noneNotchApp = xmlParser.getAttributeValue(null, "name");
                                        addNoneNotchAppInfo(noneNotchApp);
                                        getVersionCode(xmlParser, noneNotchApp);
                                    }
                                }
                                if (xmlEventType == 2) {
                                    if (XML_NONE_NOTCH_NAME_WITH_STATUSBAR.equals(xmlParser.getName())) {
                                        String noneNotchAppWithStatusbar = xmlParser.getAttributeValue(null, "name");
                                        addNoneNotchAppWithStatusbarInfo(noneNotchAppWithStatusbar);
                                        getVersionCode(xmlParser, noneNotchAppWithStatusbar);
                                    }
                                }
                                if (xmlEventType == 2) {
                                    if (XML_NONE_NOTCH_NAME_HIDE.equals(xmlParser.getName())) {
                                        String noneNotchAppHide = xmlParser.getAttributeValue(null, "name");
                                        addNoneNotchAppHideInfo(noneNotchAppHide);
                                        getVersionCode(xmlParser, noneNotchAppHide);
                                    }
                                }
                                if (xmlEventType != 2 || !XML_NOTCH_SYSTEM_APP.equals(xmlParser.getName())) {
                                    if (xmlEventType == 3 && XML_Notch_WhITE_LIST.equals(xmlParser.getName())) {
                                        break;
                                    }
                                } else {
                                    addSystemAppInfo(xmlParser.getAttributeValue(null, "name"));
                                }
                            }
                        }
                    }
                    if (inputStream == null) {
                        try {
                            inputStream.close();
                            return;
                        } catch (IOException e3) {
                            Log.e(TAG, "load notch screen config: IO Exception while closing stream", e3);
                            return;
                        }
                    }
                    return;
                }
            } catch (FileNotFoundException e4) {
                Log.e(TAG, "load notch screen config: ", e4);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                        return;
                    } catch (IOException e32) {
                        Log.e(TAG, "load notch screen config: IO Exception while closing stream", e32);
                        return;
                    }
                }
                return;
            } catch (XmlPullParserException e5) {
                Log.e(TAG, "load notch screen config: ", e5);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                        return;
                    } catch (IOException e322) {
                        Log.e(TAG, "load notch screen config: IO Exception while closing stream", e322);
                        return;
                    }
                }
                return;
            } catch (IOException e3222) {
                Log.e(TAG, "load notch screen config: ", e3222);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                        return;
                    } catch (IOException e32222) {
                        Log.e(TAG, "load notch screen config: IO Exception while closing stream", e32222);
                        return;
                    }
                }
                return;
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e322222) {
                        Log.e(TAG, "load notch screen config: IO Exception while closing stream", e322222);
                    }
                }
            }
        }
        Slog.w(TAG, "notch_screen_whitelist.xml is not exist");
        if (inputStream != null) {
        }
        if (inputStream == null) {
        }
    }

    private void getVersionCode(XmlPullParser xmlParser, String className) {
        String code = xmlParser.getAttributeValue(null, "versionCode");
        if (!TextUtils.isEmpty(code) && !TextUtils.isEmpty(className)) {
            String packageName = className.split("/")[0].trim();
            int versionCode = 0;
            try {
                versionCode = Integer.parseInt(code);
            } catch (NumberFormatException e) {
                Log.e(TAG, "NumberFormatException: ", e);
            }
            if (!this.mWhiteAppVersionCode.containsKey(packageName)) {
                this.mWhiteAppVersionCode.put(packageName, Integer.valueOf(versionCode));
            }
            if (versionCode != 0 && getVersionCodeForPackage(packageName) >= versionCode) {
                this.mInstalledAppVersionCode.put(packageName, Boolean.valueOf(true));
            }
        }
    }

    private int getVersionCodeForPackage(String packageName) {
        PackageInfo pkgInfo = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getPackageInfo(packageName, 786432, Binder.getCallingUid(), ActivityManager.getCurrentUser());
        if (pkgInfo != null) {
            return pkgInfo.versionCode;
        }
        return 0;
    }

    private boolean isAdaptationNotchScreen(String packageName) {
        if (packageName != null && this.mInstalledAppVersionCode.containsKey(packageName)) {
            return ((Boolean) this.mInstalledAppVersionCode.get(packageName)).booleanValue();
        }
        return false;
    }

    public void updateVersionCodeInNoch(String packageName, String flag, int updateVersionCode) {
        if (packageName != null && flag != null && (this.mWhiteAppVersionCode.containsKey(packageName) ^ 1) == 0) {
            if ("add".equals(flag)) {
                int whiteAppVersionCode = ((Integer) this.mWhiteAppVersionCode.get(packageName)).intValue();
                if (whiteAppVersionCode != 0 && updateVersionCode >= whiteAppVersionCode) {
                    this.mInstalledAppVersionCode.put(packageName, Boolean.valueOf(true));
                }
            } else if ("removed".equals(flag) && this.mInstalledAppVersionCode.containsKey(packageName)) {
                this.mInstalledAppVersionCode.remove(packageName);
            }
        }
    }

    public boolean isNotchAppInfo(WindowState win) {
        String appInfo = win.toString();
        int size = this.notchAppInfos.size();
        int i = 0;
        while (i < size) {
            if (appInfo != null && appInfo.contains((CharSequence) this.notchAppInfos.get(i))) {
                return isAdaptationNotchScreen(win.getOwningPackage()) ^ 1;
            }
            i++;
        }
        return false;
    }

    public boolean isNoneNotchAppInfo(WindowState win) {
        String appInfo = win.toString();
        int size = this.noneNotchAppInfos.size();
        int i = 0;
        while (i < size) {
            if (appInfo != null && appInfo.contains((CharSequence) this.noneNotchAppInfos.get(i))) {
                return isAdaptationNotchScreen(win.getOwningPackage()) ^ 1;
            }
            i++;
        }
        return false;
    }

    public boolean isNotchAppHideInfo(WindowState win) {
        String appInfo = win.toString();
        int size = this.notchAppHideInfos.size();
        int i = 0;
        while (i < size) {
            if (appInfo != null && appInfo.contains((CharSequence) this.notchAppHideInfos.get(i))) {
                return isAdaptationNotchScreen(win.getOwningPackage()) ^ 1;
            }
            i++;
        }
        return false;
    }

    public boolean isNoneNotchAppHideInfo(WindowState win) {
        String appInfo = win.toString();
        int size = this.noneNotchHideAppInfos.size();
        int i = 0;
        while (i < size) {
            if (appInfo != null && appInfo.contains((CharSequence) this.noneNotchHideAppInfos.get(i))) {
                return isAdaptationNotchScreen(win.getOwningPackage()) ^ 1;
            }
            i++;
        }
        return false;
    }

    public boolean isNoneNotchAppWithStatusbarInfo(WindowState win) {
        String appInfo = win.toString();
        int size = this.noneNotchAppWithStatusbarInfos.size();
        int i = 0;
        while (i < size) {
            if (appInfo != null && appInfo.contains((CharSequence) this.noneNotchAppWithStatusbarInfos.get(i))) {
                return isAdaptationNotchScreen(win.getOwningPackage()) ^ 1;
            }
            i++;
        }
        return false;
    }

    public boolean isSystemAppInfo(String systemInfo) {
        int size = this.systemAppInfos.size();
        int i = 0;
        while (i < size) {
            if (systemInfo != null && systemInfo.contains((CharSequence) this.systemAppInfos.get(i))) {
                return true;
            }
            i++;
        }
        return false;
    }

    private void addNotchAppInfo(String notchApp) {
        if (!this.notchAppInfos.contains(notchApp)) {
            this.notchAppInfos.add(notchApp);
        }
    }

    private void addNoneNotchAppInfo(String noneNotchApp) {
        if (!this.noneNotchAppInfos.contains(noneNotchApp)) {
            this.noneNotchAppInfos.add(noneNotchApp);
        }
    }

    private void addNotchAppHideInfo(String notchAppHide) {
        if (!this.notchAppHideInfos.contains(notchAppHide)) {
            this.notchAppHideInfos.add(notchAppHide);
        }
    }

    private void addNoneNotchAppHideInfo(String noneNotchAppHide) {
        if (!this.noneNotchHideAppInfos.contains(noneNotchAppHide)) {
            this.noneNotchHideAppInfos.add(noneNotchAppHide);
        }
    }

    private void addNoneNotchAppWithStatusbarInfo(String noneNotchAppWithStatusbar) {
        if (!this.noneNotchAppWithStatusbarInfos.contains(noneNotchAppWithStatusbar)) {
            this.noneNotchAppWithStatusbarInfos.add(noneNotchAppWithStatusbar);
        }
    }

    private void addSystemAppInfo(String systemApp) {
        if (!this.systemAppInfos.contains(systemApp)) {
            this.systemAppInfos.add(systemApp);
        }
    }

    public static void registerNotchSwitchListener(Context context, NotchSwitchListener listener) {
        context.getContentResolver().registerContentObserver(Secure.getUriFor(DISPLAY_NOTCH_STATUS), true, listener.mObserver, -1);
    }

    public static void unregisterNotchSwitchListener(Context context, NotchSwitchListener listener) {
        context.getContentResolver().unregisterContentObserver(listener.mObserver);
    }

    public void updateWhitelistByHot(Context context, String fileName) {
        new WhitelistUpdateThread(context, fileName).start();
    }
}
