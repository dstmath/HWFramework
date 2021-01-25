package com.android.server.wm;

import android.app.AppGlobals;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class CompatModePackages {
    private static final int COMPAT_FLAG_DONT_ASK = 1;
    private static final int COMPAT_FLAG_ENABLED = 2;
    private static final int MSG_WRITE = 300;
    private static final String TAG = "ActivityTaskManager";
    private static final String TAG_CONFIGURATION = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_CONFIGURATION);
    private final AtomicFile mFile;
    private final CompatHandler mHandler;
    private final HashMap<String, Integer> mPackages = new HashMap<>();
    private final ActivityTaskManagerService mService;

    /* access modifiers changed from: private */
    public final class CompatHandler extends Handler {
        public CompatHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == CompatModePackages.MSG_WRITE) {
                CompatModePackages.this.saveCompatModes();
            }
        }
    }

    public CompatModePackages(ActivityTaskManagerService service, File systemDir, Handler handler) {
        String pkg;
        this.mService = service;
        this.mFile = new AtomicFile(new File(systemDir, "packages-compat.xml"), "compat-mode");
        this.mHandler = new CompatHandler(handler.getLooper());
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = this.mFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis2, StandardCharsets.UTF_8.name());
            int eventType = parser.getEventType();
            while (eventType != 2 && eventType != 1) {
                eventType = parser.next();
            }
            if (eventType != 1) {
                if ("compat-packages".equals(parser.getName())) {
                    int eventType2 = parser.next();
                    do {
                        if (eventType2 == 2) {
                            String tagName = parser.getName();
                            if (parser.getDepth() == 2 && "pkg".equals(tagName) && (pkg = parser.getAttributeValue(null, "name")) != null) {
                                String mode = parser.getAttributeValue(null, "mode");
                                int modeInt = 0;
                                if (mode != null) {
                                    try {
                                        modeInt = Integer.parseInt(mode);
                                    } catch (NumberFormatException e) {
                                    }
                                }
                                this.mPackages.put(pkg, Integer.valueOf(modeInt));
                            }
                        }
                        eventType2 = parser.next();
                    } while (eventType2 != 1);
                }
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (IOException e2) {
                    }
                }
            } else if (fis2 != null) {
                try {
                    fis2.close();
                } catch (IOException e3) {
                }
            }
        } catch (XmlPullParserException e4) {
            Slog.w(TAG, "Error reading compat-packages", e4);
            if (0 != 0) {
                fis.close();
            }
        } catch (IOException e5) {
            if (0 != 0) {
                Slog.w(TAG, "Error reading compat-packages", e5);
            }
            if (0 != 0) {
                fis.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
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
        this.mHandler.removeMessages(MSG_WRITE);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_WRITE), 10000);
    }

    public CompatibilityInfo compatibilityInfoForPackageLocked(ApplicationInfo ai) {
        Configuration globalConfig = this.mService.getGlobalConfiguration();
        return new CompatibilityInfo(ai, globalConfig.screenLayout, globalConfig.smallestScreenWidthDp, (getPackageFlags(ai.packageName) & 2) != 0);
    }

    public int computeCompatModeLocked(ApplicationInfo ai) {
        boolean enabled = (getPackageFlags(ai.packageName) & 2) != 0;
        Configuration globalConfig = this.mService.getGlobalConfiguration();
        CompatibilityInfo info = new CompatibilityInfo(ai, globalConfig.screenLayout, globalConfig.smallestScreenWidthDp, enabled);
        if (info.alwaysSupportsScreen()) {
            return -2;
        }
        if (info.neverSupportsScreen()) {
            return -1;
        }
        return enabled ? 1 : 0;
    }

    public boolean getPackageAskCompatModeLocked(String packageName) {
        return (getPackageFlags(packageName) & 1) == 0;
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

    public int getPackageScreenCompatModeLocked(String packageName) {
        ApplicationInfo ai = null;
        try {
            ai = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, 0);
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
            Slog.w(TAG, "setPackageScreenCompatMode failed: unknown package " + packageName);
            return;
        }
        setPackageScreenCompatModeLocked(ai, mode);
    }

    /* access modifiers changed from: package-private */
    public void setPackageScreenCompatModeLocked(ApplicationInfo ai, int mode) {
        boolean enable;
        int newFlags;
        String packageName = ai.packageName;
        int curFlags = getPackageFlags(packageName);
        if (mode == 0) {
            enable = false;
        } else if (mode == 1) {
            enable = true;
        } else if (mode != 2) {
            Slog.w(TAG, "Unknown screen compat mode req #" + mode + "; ignoring");
            return;
        } else {
            enable = (curFlags & 2) == 0;
        }
        if (enable) {
            newFlags = 2 | curFlags;
        } else {
            newFlags = curFlags & -3;
        }
        CompatibilityInfo ci = compatibilityInfoForPackageLocked(ai);
        if (ci.alwaysSupportsScreen()) {
            Slog.w(TAG, "Ignoring compat mode change of " + packageName + "; compatibility never needed");
            newFlags = 0;
        }
        if (ci.neverSupportsScreen()) {
            Slog.w(TAG, "Ignoring compat mode change of " + packageName + "; compatibility always needed");
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
            ActivityStack stack = this.mService.getTopDisplayFocusedStack();
            ActivityRecord starting = stack.restartPackage(packageName);
            SparseArray<WindowProcessController> pidMap = this.mService.mProcessMap.getPidMap();
            for (int i = pidMap.size() - 1; i >= 0; i--) {
                WindowProcessController app = pidMap.valueAt(i);
                if (app.mPkgList.contains(packageName)) {
                    try {
                        if (app.hasThread()) {
                            if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                                Slog.v(TAG_CONFIGURATION, "Sending to proc " + app.mName + " new compat " + ci2);
                            }
                            app.getThread().updatePackageCompatibilityInfo(packageName, ci2);
                        }
                    } catch (Exception e) {
                    }
                }
            }
            if (starting != null) {
                starting.ensureActivityConfiguration(0, false);
                stack.ensureActivitiesVisibleLocked(starting, 0, false);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00de  */
    /* JADX WARNING: Removed duplicated region for block: B:47:? A[RETURN, SYNTHETIC] */
    private void saveCompatModes() {
        HashMap<String, Integer> pkgs;
        IOException e1;
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                pkgs = new HashMap<>(this.mPackages);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        FileOutputStream fos = null;
        try {
            fos = this.mFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            String str = null;
            out.startDocument(null, true);
            out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            out.startTag(null, "compat-packages");
            IPackageManager pm = AppGlobals.getPackageManager();
            Configuration globalConfig = this.mService.getGlobalConfiguration();
            int screenLayout = globalConfig.screenLayout;
            int smallestScreenWidthDp = globalConfig.smallestScreenWidthDp;
            for (Map.Entry<String, Integer> entry : pkgs.entrySet()) {
                String pkg = entry.getKey();
                int mode = entry.getValue().intValue();
                if (mode != 0) {
                    ApplicationInfo ai = null;
                    try {
                        ai = pm.getApplicationInfo(pkg, 0, 0);
                    } catch (RemoteException e) {
                    } catch (IOException e2) {
                        e1 = e2;
                        Slog.w(TAG, "Error writing compat packages", e1);
                        if (fos != null) {
                        }
                    }
                    if (ai != null) {
                        CompatibilityInfo info = new CompatibilityInfo(ai, screenLayout, smallestScreenWidthDp, false);
                        if (!info.alwaysSupportsScreen() && !info.neverSupportsScreen()) {
                            out.startTag(str, "pkg");
                            out.attribute(str, "name", pkg);
                            try {
                                out.attribute(null, "mode", Integer.toString(mode));
                                out.endTag(null, "pkg");
                                pkgs = pkgs;
                                str = null;
                            } catch (IOException e3) {
                                e1 = e3;
                                Slog.w(TAG, "Error writing compat packages", e1);
                                if (fos != null) {
                                }
                            }
                        }
                    }
                }
            }
            out.endTag(null, "compat-packages");
            out.endDocument();
            this.mFile.finishWrite(fos);
        } catch (IOException e4) {
            e1 = e4;
            Slog.w(TAG, "Error writing compat packages", e1);
            if (fos != null) {
                this.mFile.failWrite(fos);
            }
        }
    }
}
