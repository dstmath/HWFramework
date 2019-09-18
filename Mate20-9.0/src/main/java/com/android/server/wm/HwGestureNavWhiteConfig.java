package com.android.server.wm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.gesture.GestureNavConst;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwGestureNavWhiteConfig {
    /* access modifiers changed from: private */
    public static final String CURRENT_ROM_VERSION = SystemProperties.get("ro.build.version.incremental", "B001");
    private static final String TAG = "HwGestureNav";
    private static final String WHITE_LIST = "GestureNav_whitelist.xml";
    private static final String XML_ATTRIBUTE_ACTION = "action";
    private static final String XML_ATTRIBUTE_NAME = "name";
    private static final String XML_VERSION = "version";
    private static final String XML_WINDOW = "window";
    private static final String XML_WhITE_LIST = "whitelist";
    private static HwGestureNavWhiteConfig hwGestureNavWhiteConfig;
    final boolean DEBUG;
    final int FLAG_FULLSCREEN = 1024;
    final int FLAG_GESTNAV_SLIDER_ONE;
    Context mContext;
    WindowManagerPolicy.WindowState mCurrentWin;
    WindowManagerPolicy.WindowState mNewWin;
    WindowManagerService mService;
    private List<GestureNavAttr> mWindows;

    private class GestureNavAttr {
        boolean action = false;
        String name = null;

        GestureNavAttr() {
        }

        public String toString() {
            return "name:" + this.name + " action:" + this.action;
        }
    }

    private static class WhitelistReadThread extends Thread {
        protected WhitelistReadThread() {
            super("HwGestureNavWhiteConfig update thread");
        }

        public void run() {
            HwGestureNavWhiteConfig.getInstance().initList();
        }
    }

    private class WhitelistUpdateThread extends Thread {
        Context mContext = null;
        String mFileName = null;

        protected WhitelistUpdateThread(Context context, String fileName) {
            super("config update thread");
            this.mContext = context;
            this.mFileName = fileName;
        }

        public void run() {
            if (this.mFileName != null) {
                FileInputStream stream = HwGestureNavWhiteConfig.this.getStreamFromPath(this.mContext, this.mFileName);
                String target = Environment.getDataSystemDirectory() + "/" + HwGestureNavWhiteConfig.CURRENT_ROM_VERSION + "-" + HwGestureNavWhiteConfig.WHITE_LIST;
                if (stream != null) {
                    try {
                        Slog.d(HwGestureNavWhiteConfig.TAG, "target " + target);
                        boolean unused = HwGestureNavWhiteConfig.this.copyFile(stream, target);
                        HwGestureNavWhiteConfig.this.updateconfig();
                    } finally {
                        IoUtils.closeQuietly(stream);
                    }
                }
            }
        }
    }

    public static synchronized HwGestureNavWhiteConfig getInstance() {
        HwGestureNavWhiteConfig hwGestureNavWhiteConfig2;
        synchronized (HwGestureNavWhiteConfig.class) {
            if (hwGestureNavWhiteConfig == null) {
                hwGestureNavWhiteConfig = new HwGestureNavWhiteConfig();
                Slog.d(TAG, "getInstance " + hwGestureNavWhiteConfig);
            }
            hwGestureNavWhiteConfig2 = hwGestureNavWhiteConfig;
        }
        return hwGestureNavWhiteConfig2;
    }

    public void initWmsServer(WindowManagerService service, Context context) {
        Slog.d(TAG, "initWmsServer " + this);
        this.mService = service;
        this.mContext = context;
    }

    public void updateconfig() {
        unInitList();
        initList();
    }

    public void updatewindow(WindowManagerPolicy.WindowState win) {
        this.mNewWin = win;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00b7, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ec, code lost:
        return false;
     */
    public synchronized boolean isEnable() {
        this.mCurrentWin = this.mNewWin;
        boolean z = false;
        if (this.mCurrentWin != null) {
            if (this.mService != null) {
                int rotation = this.mService.getDefaultDisplayContentLocked().getRotation();
                if (rotation != 0) {
                    if (rotation != 2) {
                        GestureNavAttr whitename = findInList();
                        if (whitename != null) {
                            return whitename.action;
                        }
                        int LastSystemUiFlags = 0;
                        if (this.mService.mPolicy instanceof HwPhoneWindowManager) {
                            LastSystemUiFlags = this.mService.mPolicy.getLastSystemUiFlags();
                        }
                        if (this.DEBUG) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("win:return ");
                            sb.append((this.mCurrentWin.getAttrs().flags & 1024) != 0);
                            sb.append(" ");
                            sb.append((LastSystemUiFlags & 4) != 0);
                            sb.append(" win:");
                            sb.append(this.mCurrentWin);
                            sb.append(" extra ");
                            sb.append(checkgestnavflags(1));
                            Slog.d(TAG, sb.toString());
                        }
                        if (((this.mCurrentWin.getAttrs().flags & 1024) != 0 || ((LastSystemUiFlags & 4) != 0 && !this.mCurrentWin.toString().contains(GestureNavConst.STATUSBAR_WINDOW))) && !checkgestnavflags(1)) {
                            z = true;
                        }
                    }
                }
                Slog.d(TAG, "rotation is " + rotation);
                return false;
            }
        }
        if (this.mService == null) {
            Slog.d(TAG, "mService == null" + this);
        }
    }

    private boolean checkgestnavflags(int flags) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        String pckName = this.mCurrentWin.getAttrs().packageName;
        if (this.DEBUG) {
            Slog.d(TAG, "pckName" + pckName);
        }
        try {
            ApplicationInfo info = pm.getApplicationInfo(pckName, 0);
            if (info == null || (info.gestnav_extra_flags & flags) != flags) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            Slog.e(TAG, "not found app" + pckName + "exception=" + ex.toString() + "mCurrentWin " + this.mCurrentWin);
            return false;
        }
    }

    private HwGestureNavWhiteConfig() {
        boolean z = true;
        this.FLAG_GESTNAV_SLIDER_ONE = 1;
        if (!SystemProperties.getBoolean("ro.debuggable", false) && !SystemProperties.getBoolean("persist.sys.huawei.debug.on", false)) {
            z = false;
        }
        this.DEBUG = z;
        this.mWindows = new ArrayList();
        new WhitelistReadThread().start();
    }

    /* access modifiers changed from: private */
    public void initList() {
        long now = System.nanoTime();
        loadconfig();
        Slog.d(TAG, "load config use:" + (System.nanoTime() - now));
    }

    private GestureNavAttr findInList() {
        GestureNavAttr object = findInList(this.mWindows);
        if (object != null) {
            return object;
        }
        return null;
    }

    private String getKeyString(String key) {
        if (key == null) {
            return null;
        }
        if (key.substring(key.length() - 1).equals("*")) {
            return " " + key.substring(0, key.length() - 1);
        }
        return " " + key + "}";
    }

    private GestureNavAttr findInList(List<GestureNavAttr> list) {
        String wininfo = this.mCurrentWin.toString();
        int size = list.size();
        if (this.DEBUG) {
            Slog.d(TAG, "win:" + wininfo + " " + list);
        }
        for (int i = 0; i < size; i++) {
            if (wininfo.contains(list.get(i).name)) {
                return list.get(i);
            }
        }
        return null;
    }

    private void unInitList() {
        this.mWindows.clear();
    }

    private void addWindowToList(GestureNavAttr window) {
        if (this.DEBUG) {
            Slog.d(TAG, "add name:" + window.name + " action " + window.action);
        }
        if (!this.mWindows.contains(window)) {
            this.mWindows.add(window);
        }
    }

    private void loadconfig() {
        File configfile = new File(Environment.getDataSystemDirectory() + "/" + CURRENT_ROM_VERSION + "-" + WHITE_LIST);
        if (!configfile.exists()) {
            configfile = HwCfgFilePolicy.getCfgFile("xml/GestureNav_whitelist.xml", 0);
            Slog.d(TAG, "load defalut config...");
        } else {
            Slog.d(TAG, "load config:" + filePath);
        }
        loadconfig(configfile);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0027 A[Catch:{ FileNotFoundException -> 0x001b, XmlPullParserException -> 0x0018, IOException -> 0x0015, all -> 0x0012 }] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00ea A[SYNTHETIC, Splitter:B:42:0x00ea] */
    /* JADX WARNING: Removed duplicated region for block: B:72:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    private void loadconfig(File configfile) {
        InputStream inputStream = null;
        if (configfile != null) {
            try {
                if (configfile.exists()) {
                    inputStream = new FileInputStream(configfile);
                    if (inputStream != null) {
                        XmlPullParser xmlParser = Xml.newPullParser();
                        xmlParser.setInput(inputStream, null);
                        int xmlEventType = xmlParser.next();
                        while (true) {
                            if (xmlEventType == 1) {
                                break;
                            }
                            if (this.DEBUG) {
                                Slog.d(TAG, "xmlname " + xmlParser.getName());
                                Slog.d(TAG, "EventType " + xmlEventType);
                            }
                            if (xmlEventType == 2 && XML_WINDOW.equals(xmlParser.getName())) {
                                GestureNavAttr window = new GestureNavAttr();
                                window.name = getKeyString(xmlParser.getAttributeValue(null, "name"));
                                String value = xmlParser.getAttributeValue(null, "action");
                                window.action = true;
                                if (value.equals("false")) {
                                    window.action = false;
                                }
                                addWindowToList(window);
                            } else if (xmlEventType != 2 || !XML_VERSION.equals(xmlParser.getName())) {
                                if (xmlEventType == 3 && XML_WhITE_LIST.equals(xmlParser.getName())) {
                                    break;
                                }
                            } else {
                                String name = xmlParser.getAttributeValue(null, "name");
                                Log.d(TAG, "whitelist version :" + name);
                            }
                            xmlEventType = xmlParser.next();
                        }
                    }
                    if (inputStream == null) {
                        try {
                            inputStream.close();
                            return;
                        } catch (IOException e) {
                            Log.e(TAG, "load GestureNav config: IO Exception while closing stream", e);
                            return;
                        }
                    } else {
                        return;
                    }
                }
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "load GestureNav FileNotFoundException: ", e2);
                if (inputStream != null) {
                    inputStream.close();
                    return;
                }
                return;
            } catch (XmlPullParserException e3) {
                Log.e(TAG, "load GestureNav XmlPullParserException: ", e3);
                if (inputStream != null) {
                    inputStream.close();
                    return;
                }
                return;
            } catch (IOException e4) {
                Log.e(TAG, "load GestureNav IOException: ", e4);
                if (inputStream != null) {
                    inputStream.close();
                    return;
                }
                return;
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "load GestureNav config: IO Exception while closing stream", e5);
                    }
                }
                throw th;
            }
        }
        Slog.w(TAG, "GestureNav_whitelist.xml is not exist");
        if (inputStream != null) {
        }
        if (inputStream == null) {
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0067 A[SYNTHETIC, Splitter:B:26:0x0067] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0074 A[SYNTHETIC, Splitter:B:31:0x0074] */
    public boolean copyFile(FileInputStream srcStream, String filePath) {
        boolean result;
        if (srcStream == null || filePath == null) {
            return false;
        }
        File dest = new File(filePath);
        if (dest.exists()) {
            dest.delete();
        }
        try {
            dest.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "IOException:" + e);
        }
        FileChannel srcChannel = null;
        FileChannel dstChannel = null;
        try {
            srcChannel = srcStream.getChannel();
            FileChannel dstChannel2 = new FileOutputStream(dest).getChannel();
            try {
                srcChannel.transferTo(0, srcChannel.size(), dstChannel2);
                result = true;
                dstChannel = dstChannel2;
            } catch (FileNotFoundException e2) {
                e = e2;
                dstChannel = dstChannel2;
                result = false;
                e.printStackTrace();
                if (srcChannel != null) {
                }
                if (dstChannel != null) {
                }
                return result;
            } catch (IOException e3) {
                e = e3;
                dstChannel = dstChannel2;
                result = false;
                e.printStackTrace();
                if (srcChannel != null) {
                }
                if (dstChannel != null) {
                }
                return result;
            }
        } catch (FileNotFoundException e4) {
            e = e4;
            result = false;
            e.printStackTrace();
            if (srcChannel != null) {
            }
            if (dstChannel != null) {
            }
            return result;
        } catch (IOException e5) {
            e = e5;
            result = false;
            e.printStackTrace();
            if (srcChannel != null) {
            }
            if (dstChannel != null) {
            }
            return result;
        }
        if (srcChannel != null) {
            try {
                srcChannel.close();
            } catch (IOException e6) {
                result = false;
                e6.printStackTrace();
            }
        }
        if (dstChannel != null) {
            try {
                dstChannel.close();
            } catch (IOException e7) {
                result = false;
                e7.printStackTrace();
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    public FileInputStream getStreamFromPath(Context Context, String fileName) {
        ParcelFileDescriptor parcelFileDesc = null;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(fileName));
            if (parcelFileDesc != null) {
                try {
                    parcelFileDesc.close();
                } catch (IOException e) {
                    Log.e(TAG, "parcelFileDesc error!");
                }
            }
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "FileNotFoundException:" + e2);
            if (parcelFileDesc != null) {
                parcelFileDesc.close();
            }
        } catch (Throwable th) {
            if (parcelFileDesc != null) {
                try {
                    parcelFileDesc.close();
                } catch (IOException e3) {
                    Log.e(TAG, "parcelFileDesc error!");
                }
            }
            throw th;
        }
        return inputStream;
    }

    public void updateWhitelistByHot(Context context, String fileName) {
        new WhitelistUpdateThread(context, fileName).start();
    }
}
