package com.android.server.wm;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AtomicFile;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* access modifiers changed from: package-private */
public class AppWarnings {
    private static final String CONFIG_FILE_NAME = "packages-warnings.xml";
    public static final int FLAG_HIDE_COMPILE_SDK = 2;
    public static final int FLAG_HIDE_DEPRECATED_SDK = 4;
    public static final int FLAG_HIDE_DISPLAY_SIZE = 1;
    private static final String TAG = "AppWarnings";
    private HashSet<ComponentName> mAlwaysShowUnsupportedCompileSdkWarningActivities = new HashSet<>();
    private final ActivityTaskManagerService mAtm;
    private final AtomicFile mConfigFile;
    private DeprecatedTargetSdkVersionDialog mDeprecatedTargetSdkVersionDialog;
    private final ConfigHandler mHandler;
    private final HashMap<String, Integer> mPackageFlags = new HashMap<>();
    private final Context mUiContext;
    private final UiHandler mUiHandler;
    private UnsupportedCompileSdkDialog mUnsupportedCompileSdkDialog;
    private UnsupportedDisplaySizeDialog mUnsupportedDisplaySizeDialog;

    /* access modifiers changed from: package-private */
    public void alwaysShowUnsupportedCompileSdkWarning(ComponentName activity) {
        this.mAlwaysShowUnsupportedCompileSdkWarningActivities.add(activity);
    }

    public AppWarnings(ActivityTaskManagerService atm, Context uiContext, Handler handler, Handler uiHandler, File systemDir) {
        this.mAtm = atm;
        this.mUiContext = uiContext;
        this.mHandler = new ConfigHandler(handler.getLooper());
        this.mUiHandler = new UiHandler(uiHandler.getLooper());
        this.mConfigFile = new AtomicFile(new File(systemDir, CONFIG_FILE_NAME), "warnings-config");
        readConfigFromFileAmsThread();
    }

    public void showUnsupportedDisplaySizeDialogIfNeeded(ActivityRecord r) {
        Configuration globalConfig = this.mAtm.getGlobalConfiguration();
        if (globalConfig.densityDpi != DisplayMetrics.DENSITY_DEVICE_STABLE && r.appInfo.requiresSmallestWidthDp > globalConfig.smallestScreenWidthDp) {
            this.mUiHandler.showUnsupportedDisplaySizeDialog(r);
        }
    }

    public void showUnsupportedCompileSdkDialogIfNeeded(ActivityRecord r) {
        if (r.appInfo.compileSdkVersion != 0 && r.appInfo.compileSdkVersionCodename != null && this.mAlwaysShowUnsupportedCompileSdkWarningActivities.contains(r.mActivityComponent)) {
            int compileSdk = r.appInfo.compileSdkVersion;
            int platformSdk = Build.VERSION.SDK_INT;
            boolean isCompileSdkPreview = !"REL".equals(r.appInfo.compileSdkVersionCodename);
            boolean isPlatformSdkPreview = !"REL".equals(Build.VERSION.CODENAME);
            if ((isCompileSdkPreview && compileSdk < platformSdk) || ((isPlatformSdkPreview && platformSdk < compileSdk) || (isCompileSdkPreview && isPlatformSdkPreview && platformSdk == compileSdk && !Build.VERSION.CODENAME.equals(r.appInfo.compileSdkVersionCodename)))) {
                this.mUiHandler.showUnsupportedCompileSdkDialog(r);
            }
        }
    }

    public void showDeprecatedTargetDialogIfNeeded(ActivityRecord r) {
        if (r.appInfo.targetSdkVersion < Build.VERSION.MIN_SUPPORTED_TARGET_SDK_INT) {
            this.mUiHandler.showDeprecatedTargetDialog(r);
        }
    }

    public void onStartActivity(ActivityRecord r) {
        showUnsupportedCompileSdkDialogIfNeeded(r);
        showUnsupportedDisplaySizeDialogIfNeeded(r);
        showDeprecatedTargetDialogIfNeeded(r);
    }

    public void onResumeActivity(ActivityRecord r) {
        showUnsupportedDisplaySizeDialogIfNeeded(r);
    }

    public void onPackageDataCleared(String name) {
        removePackageAndHideDialogs(name);
    }

    public void onPackageUninstalled(String name) {
        removePackageAndHideDialogs(name);
    }

    public void onDensityChanged() {
        this.mUiHandler.hideUnsupportedDisplaySizeDialog();
    }

    private void removePackageAndHideDialogs(String name) {
        this.mUiHandler.hideDialogsForPackage(name);
        synchronized (this.mPackageFlags) {
            this.mPackageFlags.remove(name);
            this.mHandler.scheduleWrite();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideUnsupportedDisplaySizeDialogUiThread() {
        UnsupportedDisplaySizeDialog unsupportedDisplaySizeDialog = this.mUnsupportedDisplaySizeDialog;
        if (unsupportedDisplaySizeDialog != null) {
            unsupportedDisplaySizeDialog.dismiss();
            this.mUnsupportedDisplaySizeDialog = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showUnsupportedDisplaySizeDialogUiThread(ActivityRecord ar) {
        UnsupportedDisplaySizeDialog unsupportedDisplaySizeDialog = this.mUnsupportedDisplaySizeDialog;
        if (unsupportedDisplaySizeDialog != null) {
            unsupportedDisplaySizeDialog.dismiss();
            this.mUnsupportedDisplaySizeDialog = null;
        }
        if (ar != null && !hasPackageFlag(ar.packageName, 1)) {
            this.mUnsupportedDisplaySizeDialog = new UnsupportedDisplaySizeDialog(this, this.mUiContext, ar.info.applicationInfo);
            this.mUnsupportedDisplaySizeDialog.show();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showUnsupportedCompileSdkDialogUiThread(ActivityRecord ar) {
        UnsupportedCompileSdkDialog unsupportedCompileSdkDialog = this.mUnsupportedCompileSdkDialog;
        if (unsupportedCompileSdkDialog != null) {
            unsupportedCompileSdkDialog.dismiss();
            this.mUnsupportedCompileSdkDialog = null;
        }
        if (ar != null && !hasPackageFlag(ar.packageName, 2)) {
            this.mUnsupportedCompileSdkDialog = new UnsupportedCompileSdkDialog(this, this.mUiContext, ar.info.applicationInfo);
            this.mUnsupportedCompileSdkDialog.show();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showDeprecatedTargetSdkDialogUiThread(ActivityRecord ar) {
        DeprecatedTargetSdkVersionDialog deprecatedTargetSdkVersionDialog = this.mDeprecatedTargetSdkVersionDialog;
        if (deprecatedTargetSdkVersionDialog != null) {
            deprecatedTargetSdkVersionDialog.dismiss();
            this.mDeprecatedTargetSdkVersionDialog = null;
        }
        if (ar != null && !hasPackageFlag(ar.packageName, 4)) {
            this.mDeprecatedTargetSdkVersionDialog = new DeprecatedTargetSdkVersionDialog(this, this.mUiContext, ar.info.applicationInfo);
            this.mDeprecatedTargetSdkVersionDialog.show();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideDialogsForPackageUiThread(String name) {
        UnsupportedDisplaySizeDialog unsupportedDisplaySizeDialog = this.mUnsupportedDisplaySizeDialog;
        if (unsupportedDisplaySizeDialog != null && (name == null || name.equals(unsupportedDisplaySizeDialog.getPackageName()))) {
            this.mUnsupportedDisplaySizeDialog.dismiss();
            this.mUnsupportedDisplaySizeDialog = null;
        }
        UnsupportedCompileSdkDialog unsupportedCompileSdkDialog = this.mUnsupportedCompileSdkDialog;
        if (unsupportedCompileSdkDialog != null && (name == null || name.equals(unsupportedCompileSdkDialog.getPackageName()))) {
            this.mUnsupportedCompileSdkDialog.dismiss();
            this.mUnsupportedCompileSdkDialog = null;
        }
        DeprecatedTargetSdkVersionDialog deprecatedTargetSdkVersionDialog = this.mDeprecatedTargetSdkVersionDialog;
        if (deprecatedTargetSdkVersionDialog == null) {
            return;
        }
        if (name == null || name.equals(deprecatedTargetSdkVersionDialog.getPackageName())) {
            this.mDeprecatedTargetSdkVersionDialog.dismiss();
            this.mDeprecatedTargetSdkVersionDialog = null;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasPackageFlag(String name, int flag) {
        return (getPackageFlags(name) & flag) == flag;
    }

    /* access modifiers changed from: package-private */
    public void setPackageFlag(String name, int flag, boolean enabled) {
        synchronized (this.mPackageFlags) {
            int curFlags = getPackageFlags(name);
            int newFlags = enabled ? curFlags | flag : (~flag) & curFlags;
            if (curFlags != newFlags) {
                if (newFlags != 0) {
                    this.mPackageFlags.put(name, Integer.valueOf(newFlags));
                } else {
                    this.mPackageFlags.remove(name);
                }
                this.mHandler.scheduleWrite();
            }
        }
    }

    private int getPackageFlags(String name) {
        int intValue;
        synchronized (this.mPackageFlags) {
            intValue = this.mPackageFlags.getOrDefault(name, 0).intValue();
        }
        return intValue;
    }

    /* access modifiers changed from: private */
    public final class UiHandler extends Handler {
        private static final int MSG_HIDE_DIALOGS_FOR_PACKAGE = 4;
        private static final int MSG_HIDE_UNSUPPORTED_DISPLAY_SIZE_DIALOG = 2;
        private static final int MSG_SHOW_DEPRECATED_TARGET_SDK_DIALOG = 5;
        private static final int MSG_SHOW_UNSUPPORTED_COMPILE_SDK_DIALOG = 3;
        private static final int MSG_SHOW_UNSUPPORTED_DISPLAY_SIZE_DIALOG = 1;

        public UiHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                AppWarnings.this.showUnsupportedDisplaySizeDialogUiThread((ActivityRecord) msg.obj);
            } else if (i == 2) {
                AppWarnings.this.hideUnsupportedDisplaySizeDialogUiThread();
            } else if (i == 3) {
                AppWarnings.this.showUnsupportedCompileSdkDialogUiThread((ActivityRecord) msg.obj);
            } else if (i == 4) {
                AppWarnings.this.hideDialogsForPackageUiThread((String) msg.obj);
            } else if (i == 5) {
                AppWarnings.this.showDeprecatedTargetSdkDialogUiThread((ActivityRecord) msg.obj);
            }
        }

        public void showUnsupportedDisplaySizeDialog(ActivityRecord r) {
            removeMessages(1);
            obtainMessage(1, r).sendToTarget();
        }

        public void hideUnsupportedDisplaySizeDialog() {
            removeMessages(2);
            sendEmptyMessage(2);
        }

        public void showUnsupportedCompileSdkDialog(ActivityRecord r) {
            removeMessages(3);
            obtainMessage(3, r).sendToTarget();
        }

        public void showDeprecatedTargetDialog(ActivityRecord r) {
            removeMessages(5);
            obtainMessage(5, r).sendToTarget();
        }

        public void hideDialogsForPackage(String name) {
            obtainMessage(4, name).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public final class ConfigHandler extends Handler {
        private static final int DELAY_MSG_WRITE = 10000;
        private static final int MSG_WRITE = 1;

        public ConfigHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                AppWarnings.this.writeConfigToFileAmsThread();
            }
        }

        public void scheduleWrite() {
            removeMessages(1);
            sendEmptyMessageDelayed(1, 10000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeConfigToFileAmsThread() {
        HashMap<String, Integer> packageFlags;
        synchronized (this.mPackageFlags) {
            packageFlags = new HashMap<>(this.mPackageFlags);
        }
        try {
            FileOutputStream fos = this.mConfigFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            out.startTag(null, "packages");
            for (Map.Entry<String, Integer> entry : packageFlags.entrySet()) {
                String pkg = entry.getKey();
                int mode = entry.getValue().intValue();
                if (mode != 0) {
                    out.startTag(null, "package");
                    out.attribute(null, "name", pkg);
                    out.attribute(null, "flags", Integer.toString(mode));
                    out.endTag(null, "package");
                }
            }
            out.endTag(null, "packages");
            out.endDocument();
            this.mConfigFile.finishWrite(fos);
        } catch (IOException e1) {
            Slog.w(TAG, "Error writing package metadata", e1);
            if (0 != 0) {
                this.mConfigFile.failWrite(null);
            }
        }
    }

    private void readConfigFromFileAmsThread() {
        String name;
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = this.mConfigFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis2, StandardCharsets.UTF_8.name());
            int eventType = parser.getEventType();
            while (eventType != 2 && eventType != 1) {
                eventType = parser.next();
            }
            if (eventType != 1) {
                if ("packages".equals(parser.getName())) {
                    int eventType2 = parser.next();
                    do {
                        if (eventType2 == 2) {
                            String tagName = parser.getName();
                            if (parser.getDepth() == 2 && "package".equals(tagName) && (name = parser.getAttributeValue(null, "name")) != null) {
                                String flags = parser.getAttributeValue(null, "flags");
                                int flagsInt = 0;
                                if (flags != null) {
                                    try {
                                        flagsInt = Integer.parseInt(flags);
                                    } catch (NumberFormatException e) {
                                    }
                                }
                                this.mPackageFlags.put(name, Integer.valueOf(flagsInt));
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
            Slog.w(TAG, "Error reading package metadata", e4);
            if (0 != 0) {
                fis.close();
            }
        } catch (IOException e5) {
            if (0 != 0) {
                Slog.w(TAG, "Error reading package metadata", e5);
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
}
