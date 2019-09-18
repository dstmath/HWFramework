package com.android.server.am;

import android.app.AppGlobals;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.AbsLocationManagerService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.Settings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class CompatModePackages {
    public static final int COMPAT_FLAG_DONT_ASK = 1;
    public static final int COMPAT_FLAG_ENABLED = 2;
    private static final int MSG_WRITE = 300;
    private static final String TAG = "ActivityManager";
    private static final String TAG_CONFIGURATION = ("ActivityManager" + ActivityManagerDebugConfig.POSTFIX_CONFIGURATION);
    private final AtomicFile mFile;
    private final CompatHandler mHandler;
    private final HashMap<String, Integer> mPackages = new HashMap<>();
    private final ActivityManagerService mService;

    private final class CompatHandler extends Handler {
        public CompatHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 300) {
                CompatModePackages.this.saveCompatModes();
            }
        }
    }

    public CompatModePackages(ActivityManagerService service, File systemDir, Handler handler) {
        this.mService = service;
        this.mFile = new AtomicFile(new File(systemDir, "packages-compat.xml"), "compat-mode");
        this.mHandler = new CompatHandler(handler.getLooper());
        FileInputStream fis = null;
        try {
            fis = this.mFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, StandardCharsets.UTF_8.name());
            int eventType = parser.getEventType();
            while (eventType != 2 && eventType != 1) {
                eventType = parser.next();
            }
            if (eventType == 1) {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                    }
                }
                return;
            }
            if ("compat-packages".equals(parser.getName())) {
                int eventType2 = parser.next();
                do {
                    if (eventType2 == 2) {
                        String tagName = parser.getName();
                        if (parser.getDepth() == 2 && AbsLocationManagerService.DEL_PKG.equals(tagName)) {
                            String pkg = parser.getAttributeValue(null, Settings.ATTR_NAME);
                            if (pkg != null) {
                                String mode = parser.getAttributeValue(null, "mode");
                                int modeInt = 0;
                                if (mode != null) {
                                    try {
                                        modeInt = Integer.parseInt(mode);
                                    } catch (NumberFormatException e2) {
                                    }
                                }
                                this.mPackages.put(pkg, Integer.valueOf(modeInt));
                            }
                        }
                    }
                    eventType2 = parser.next();
                } while (eventType2 != 1);
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e3) {
                }
            }
        } catch (XmlPullParserException e4) {
            Slog.w("ActivityManager", "Error reading compat-packages", e4);
            if (fis != null) {
                fis.close();
            }
        } catch (IOException e5) {
            if (fis != null) {
                Slog.w("ActivityManager", "Error reading compat-packages", e5);
            }
            if (fis != null) {
                fis.close();
            }
        } catch (Throwable th) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e6) {
                }
            }
            throw th;
        }
    }

    public HashMap<String, Integer> getPackages() {
        return this.mPackages;
    }

    private int getPackageFlags(String packageName) {
        Integer flags = this.mPackages.get(packageName);
        if (flags != null) {
            return flags.intValue();
        }
        return 0;
    }

    public void handlePackageDataClearedLocked(String packageName) {
        removePackage(packageName);
    }

    public void handlePackageUninstalledLocked(String packageName) {
        removePackage(packageName);
    }

    private void removePackage(String packageName) {
        if (this.mPackages.containsKey(packageName)) {
            this.mPackages.remove(packageName);
            scheduleWrite();
        }
    }

    public void handlePackageAddedLocked(String packageName, boolean updated) {
        ApplicationInfo ai = null;
        boolean mayCompat = false;
        try {
            ai = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, 0);
        } catch (RemoteException e) {
        }
        if (ai != null) {
            CompatibilityInfo ci = compatibilityInfoForPackageLocked(ai);
            if (!ci.alwaysSupportsScreen() && !ci.neverSupportsScreen()) {
                mayCompat = true;
            }
            if (updated && !mayCompat && this.mPackages.containsKey(packageName)) {
                this.mPackages.remove(packageName);
                scheduleWrite();
            }
        }
    }

    private void scheduleWrite() {
        this.mHandler.removeMessages(300);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(300), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
    }

    public CompatibilityInfo compatibilityInfoForPackageLocked(ApplicationInfo ai) {
        Configuration globalConfig = this.mService.getGlobalConfiguration();
        return new CompatibilityInfo(ai, globalConfig.screenLayout, globalConfig.smallestScreenWidthDp, (getPackageFlags(ai.packageName) & 2) != 0);
    }

    public int computeCompatModeLocked(ApplicationInfo ai) {
        int i = 0;
        boolean enabled = (getPackageFlags(ai.packageName) & 2) != 0;
        Configuration globalConfig = this.mService.getGlobalConfiguration();
        CompatibilityInfo info = new CompatibilityInfo(ai, globalConfig.screenLayout, globalConfig.smallestScreenWidthDp, enabled);
        if (info.alwaysSupportsScreen()) {
            return -2;
        }
        if (info.neverSupportsScreen()) {
            return -1;
        }
        if (enabled) {
            i = 1;
        }
        return i;
    }

    public boolean getFrontActivityAskCompatModeLocked() {
        ActivityRecord r = this.mService.getFocusedStack().topRunningActivityLocked();
        if (r == null) {
            return false;
        }
        return getPackageAskCompatModeLocked(r.packageName);
    }

    public boolean getPackageAskCompatModeLocked(String packageName) {
        return (getPackageFlags(packageName) & 1) == 0;
    }

    public void setFrontActivityAskCompatModeLocked(boolean ask) {
        ActivityRecord r = this.mService.getFocusedStack().topRunningActivityLocked();
        if (r != null) {
            setPackageAskCompatModeLocked(r.packageName, ask);
        }
    }

    public void setPackageAskCompatModeLocked(String packageName, boolean ask) {
        setPackageFlagLocked(packageName, 1, ask);
    }

    private void setPackageFlagLocked(String packageName, int flag, boolean set) {
        int curFlags = getPackageFlags(packageName);
        int newFlags = set ? (~flag) & curFlags : curFlags | flag;
        if (curFlags != newFlags) {
            if (newFlags != 0) {
                this.mPackages.put(packageName, Integer.valueOf(newFlags));
            } else {
                this.mPackages.remove(packageName);
            }
            scheduleWrite();
        }
    }

    public int getFrontActivityScreenCompatModeLocked() {
        ActivityRecord r = this.mService.getFocusedStack().topRunningActivityLocked();
        if (r == null) {
            return -3;
        }
        return computeCompatModeLocked(r.info.applicationInfo);
    }

    public void setFrontActivityScreenCompatModeLocked(int mode) {
        ActivityRecord r = this.mService.getFocusedStack().topRunningActivityLocked();
        if (r == null) {
            Slog.w("ActivityManager", "setFrontActivityScreenCompatMode failed: no top activity");
        } else {
            setPackageScreenCompatModeLocked(r.info.applicationInfo, mode);
        }
    }

    public int getPackageScreenCompatModeLocked(String packageName) {
        ApplicationInfo ai = null;
        try {
            ai = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, UserHandle.getCallingUserId());
        } catch (RemoteException e) {
        }
        if (ai == null) {
            return -3;
        }
        return computeCompatModeLocked(ai);
    }

    public void setPackageScreenCompatModeLocked(String packageName, int mode) {
        ApplicationInfo ai = null;
        try {
            ai = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, 0);
        } catch (RemoteException e) {
        }
        if (ai == null) {
            Slog.w("ActivityManager", "setPackageScreenCompatMode failed: unknown package " + packageName);
            return;
        }
        setPackageScreenCompatModeLocked(ai, mode);
    }

    private void setPackageScreenCompatModeLocked(ApplicationInfo ai, int mode) {
        boolean enable;
        int newFlags;
        String packageName = ai.packageName;
        int curFlags = getPackageFlags(packageName);
        switch (mode) {
            case 0:
                enable = false;
                break;
            case 1:
                enable = true;
                break;
            case 2:
                if ((curFlags & 2) != 0) {
                    enable = false;
                    break;
                } else {
                    enable = true;
                    break;
                }
            default:
                Slog.w("ActivityManager", "Unknown screen compat mode req #" + mode + "; ignoring");
                return;
        }
        int newFlags2 = curFlags;
        if (enable) {
            newFlags = newFlags2 | 2;
        } else {
            newFlags = newFlags2 & -3;
        }
        CompatibilityInfo ci = compatibilityInfoForPackageLocked(ai);
        if (ci.alwaysSupportsScreen()) {
            Slog.w("ActivityManager", "Ignoring compat mode change of " + packageName + "; compatibility never needed");
            newFlags = 0;
        }
        if (ci.neverSupportsScreen()) {
            Slog.w("ActivityManager", "Ignoring compat mode change of " + packageName + "; compatibility always needed");
            newFlags = 0;
        }
        if (newFlags != curFlags) {
            if (newFlags != 0) {
                this.mPackages.put(packageName, Integer.valueOf(newFlags));
            } else {
                this.mPackages.remove(packageName);
            }
            CompatibilityInfo ci2 = compatibilityInfoForPackageLocked(ai);
            scheduleWrite();
            ActivityStack stack = this.mService.getFocusedStack();
            ActivityRecord starting = stack.restartPackage(packageName);
            int i = this.mService.mLruProcesses.size() - 1;
            while (true) {
                int i2 = i;
                if (i2 >= 0) {
                    ProcessRecord app = this.mService.mLruProcesses.get(i2);
                    if (app.pkgList.containsKey(packageName)) {
                        try {
                            if (app.thread != null) {
                                if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                    Slog.v(TAG_CONFIGURATION, "Sending to proc " + app.processName + " new compat " + ci2);
                                }
                                app.thread.updatePackageCompatibilityInfo(packageName, ci2);
                            }
                        } catch (Exception e) {
                        }
                    }
                    i = i2 - 1;
                } else if (starting != null) {
                    starting.ensureActivityConfiguration(0, false);
                    stack.ensureActivitiesVisibleLocked(starting, 0, false);
                }
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v0, resolved type: android.content.pm.ApplicationInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v1, resolved type: android.content.pm.ApplicationInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v2, resolved type: android.content.pm.ApplicationInfo} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00e5  */
    /* JADX WARNING: Removed duplicated region for block: B:53:? A[RETURN, SYNTHETIC] */
    public void saveCompatModes() {
        HashMap<String, Integer> pkgs;
        HashMap<String, Integer> pkgs2;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                pkgs = new HashMap<>(this.mPackages);
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        String str = null;
        FileOutputStream fos = null;
        try {
            fos = this.mFile.startWrite();
            FastXmlSerializer fastXmlSerializer = new FastXmlSerializer();
            fastXmlSerializer.setOutput(fos, StandardCharsets.UTF_8.name());
            fastXmlSerializer.startDocument(null, true);
            fastXmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            fastXmlSerializer.startTag(null, "compat-packages");
            IPackageManager pm = AppGlobals.getPackageManager();
            Configuration globalConfig = this.mService.getGlobalConfiguration();
            int screenLayout = globalConfig.screenLayout;
            int smallestScreenWidthDp = globalConfig.smallestScreenWidthDp;
            Iterator<Map.Entry<String, Integer>> it = pkgs.entrySet().iterator();
            while (true) {
                Iterator<Map.Entry<String, Integer>> it2 = it;
                if (it2.hasNext()) {
                    Map.Entry next = it2.next();
                    String pkg = (String) next.getKey();
                    int mode = ((Integer) next.getValue()).intValue();
                    if (mode != 0) {
                        ApplicationInfo ai = str;
                        try {
                            ai = pm.getApplicationInfo(pkg, 0, 0);
                        } catch (RemoteException e) {
                        } catch (IOException e2) {
                            e1 = e2;
                            HashMap<String, Integer> hashMap = pkgs;
                            Slog.w("ActivityManager", "Error writing compat packages", e1);
                            if (fos != null) {
                            }
                        }
                        if (ai != 0) {
                            CompatibilityInfo info = new CompatibilityInfo(ai, screenLayout, smallestScreenWidthDp, false);
                            if (!info.alwaysSupportsScreen()) {
                                if (!info.neverSupportsScreen()) {
                                    fastXmlSerializer.startTag(str, AbsLocationManagerService.DEL_PKG);
                                    fastXmlSerializer.attribute(str, Settings.ATTR_NAME, pkg);
                                    pkgs2 = pkgs;
                                    try {
                                        fastXmlSerializer.attribute(null, "mode", Integer.toString(mode));
                                        fastXmlSerializer.endTag(null, AbsLocationManagerService.DEL_PKG);
                                        it = it2;
                                        pkgs = pkgs2;
                                        str = null;
                                    } catch (IOException e3) {
                                        e1 = e3;
                                        Slog.w("ActivityManager", "Error writing compat packages", e1);
                                        if (fos != null) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                    pkgs2 = pkgs;
                    it = it2;
                    pkgs = pkgs2;
                    str = null;
                } else {
                    fastXmlSerializer.endTag(null, "compat-packages");
                    fastXmlSerializer.endDocument();
                    this.mFile.finishWrite(fos);
                    return;
                }
            }
        } catch (IOException e4) {
            e1 = e4;
            HashMap<String, Integer> hashMap2 = pkgs;
            Slog.w("ActivityManager", "Error writing compat packages", e1);
            if (fos != null) {
                this.mFile.failWrite(fos);
            }
        }
    }
}
