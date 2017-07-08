package com.android.server.am;

import android.app.AppGlobals;
import android.common.HwFrameworkFactory;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.res.CompatibilityInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.AbsLocationManagerService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.wm.WindowState;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class CompatModePackages {
    public static final int COMPAT_FLAG_DISABLED_DEFAULT = 2048;
    public static final int COMPAT_FLAG_DONT_ASK = 1;
    public static final int COMPAT_FLAG_ENABLED = 2;
    public static final int COMPAT_FLAG_ENABLED_DEFAULT = 1024;
    private static final int MSG_WRITE = 300;
    private static final String TAG = null;
    private static final String TAG_CONFIGURATION = null;
    public static final int UNSUPPORTED_ZOOM_FLAG_DONT_NOTIFY = 4;
    private static String[] mCompatApps;
    private static String[] mCompatGames;
    private HwCustCompatModePackages mCust;
    private final AtomicFile mFile;
    private final CompatHandler mHandler;
    private boolean mIsInitCustList;
    private final HashMap<String, Integer> mPackages;
    private final HashMap<String, Integer> mPackagesCompatModeHash;
    private final ActivityManagerService mService;

    private final class CompatHandler extends Handler {
        public CompatHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CompatModePackages.MSG_WRITE /*300*/:
                    CompatModePackages.this.saveCompatModes();
                default:
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.CompatModePackages.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.CompatModePackages.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.CompatModePackages.<clinit>():void");
    }

    public void loadCompatModeAppList() {
        if (!this.mIsInitCustList) {
            Slog.i("SDR", "APS: SDR: CompatModePackages.loadCompatModeAppList.");
            getCustAppList(9998);
            this.mIsInitCustList = true;
        }
    }

    private void getCustAppList(int type) {
        String[] custPkgList = HwFrameworkFactory.getHwNsdImpl().getCustAppList(type);
        if (custPkgList != null) {
            int length = custPkgList.length;
            for (int i = 0; i < length; i += COMPAT_FLAG_DONT_ASK) {
                this.mPackages.put(custPkgList[i], Integer.valueOf(1027));
            }
            Slog.i("SDR", "APS: SDR: CompatModePackages.getCustAppList. mPackages size = " + this.mPackages.size());
        }
    }

    public CompatModePackages(ActivityManagerService service, File systemDir, Handler handler) {
        this.mPackages = new HashMap();
        this.mPackagesCompatModeHash = new HashMap();
        this.mCust = (HwCustCompatModePackages) HwCustUtils.createObj(HwCustCompatModePackages.class, new Object[0]);
        this.mIsInitCustList = false;
        this.mService = service;
        this.mFile = new AtomicFile(new File(systemDir, "packages-compat.xml"));
        this.mHandler = new CompatHandler(handler.getLooper());
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = this.mFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fileInputStream, StandardCharsets.UTF_8.name());
            int eventType = parser.getEventType();
            while (eventType != COMPAT_FLAG_ENABLED && eventType != COMPAT_FLAG_DONT_ASK) {
                eventType = parser.next();
            }
            if (eventType == COMPAT_FLAG_DONT_ASK) {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                    }
                }
                addCompatList();
                return;
            }
            if ("compat-packages".equals(parser.getName())) {
                eventType = parser.next();
                do {
                    if (eventType == COMPAT_FLAG_ENABLED) {
                        String tagName = parser.getName();
                        if (parser.getDepth() == COMPAT_FLAG_ENABLED && AbsLocationManagerService.DEL_PKG.equals(tagName)) {
                            String pkg = parser.getAttributeValue(null, "name");
                            if (pkg != null) {
                                String mode = parser.getAttributeValue(null, "mode");
                                int modeInt = 0;
                                if (mode != null) {
                                    try {
                                        modeInt = Integer.parseInt(mode);
                                    } catch (NumberFormatException e2) {
                                    }
                                }
                                if (this.mCust == null || !this.mCust.isLowPowerDisplayMode() || (modeInt & COMPAT_FLAG_ENABLED_DEFAULT) == 0) {
                                    this.mPackages.put(pkg, Integer.valueOf(modeInt));
                                }
                            }
                        }
                    }
                    eventType = parser.next();
                } while (eventType != COMPAT_FLAG_DONT_ASK);
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e3) {
                }
            }
            addCompatList();
        } catch (XmlPullParserException e4) {
            Slog.w(TAG, "Error reading compat-packages", e4);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e5) {
                }
            }
            addCompatList();
        } catch (IOException e6) {
            if (fileInputStream != null) {
                Slog.w(TAG, "Error reading compat-packages", e6);
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e7) {
                }
            }
            addCompatList();
        } catch (Throwable th) {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e8) {
                }
            }
            addCompatList();
        }
    }

    private void addCompatList() {
        if ((this.mCust == null || !this.mCust.isLowPowerDisplayMode()) && SystemProperties.getInt("ro.config.compatibility_enable", 0) == COMPAT_FLAG_DONT_ASK) {
            int i;
            String[] strArr = mCompatGames;
            int length = strArr.length;
            for (i = 0; i < length; i += COMPAT_FLAG_DONT_ASK) {
                addCompatForCheck(strArr[i], false);
            }
            strArr = mCompatApps;
            length = strArr.length;
            for (i = 0; i < length; i += COMPAT_FLAG_DONT_ASK) {
                addCompatForCheck(strArr[i], false);
            }
        }
    }

    private void addCompatForCheck(String PackageName, boolean NeedCheck) {
        int i = 0;
        if (this.mCust == null || !this.mCust.isLowPowerDisplayMode()) {
            boolean CompatModeHasChanged;
            boolean CompatDefaultDisable = getDisabledPackageFlags(PackageName) != 0;
            if (getPackageFlags(PackageName) != 0) {
                CompatModeHasChanged = true;
            } else {
                CompatModeHasChanged = false;
            }
            if (!CompatModeHasChanged && !CompatDefaultDisable) {
                if (NeedCheck) {
                    String[] strArr = mCompatGames;
                    int length = strArr.length;
                    for (int i2 = 0; i2 < length; i2 += COMPAT_FLAG_DONT_ASK) {
                        if (PackageName.matches(strArr[i2])) {
                            this.mPackages.put(PackageName, Integer.valueOf(1027));
                            return;
                        }
                    }
                    String[] strArr2 = mCompatApps;
                    int length2 = strArr2.length;
                    while (i < length2) {
                        if (PackageName.matches(strArr2[i])) {
                            this.mPackages.put(PackageName, Integer.valueOf(1027));
                            return;
                        }
                        i += COMPAT_FLAG_DONT_ASK;
                    }
                    this.mPackagesCompatModeHash.put(PackageName, Integer.valueOf(COMPAT_FLAG_DISABLED_DEFAULT));
                    return;
                }
                this.mPackages.put(PackageName, Integer.valueOf(1027));
            }
        }
    }

    private int getDisabledPackageFlags(String packageName) {
        Integer flags = (Integer) this.mPackagesCompatModeHash.get(packageName);
        return flags != null ? flags.intValue() : 0;
    }

    public HashMap<String, Integer> getPackages() {
        return this.mPackages;
    }

    private int getPackageFlags(String packageName) {
        int i = 0;
        if ((DumpState.DUMP_VERSION & SystemProperties.getInt("sys.aps.support", 0)) == 0) {
            return 0;
        }
        Integer flags = (Integer) this.mPackages.get(packageName);
        if (flags != null) {
            i = flags.intValue();
        }
        return i;
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
        try {
            ai = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, 0);
        } catch (RemoteException e) {
        }
        if (ai != null) {
            CompatibilityInfo ci = compatibilityInfoForPackageLocked(ai);
            boolean mayCompat = !ci.alwaysSupportsScreen() ? !ci.neverSupportsScreen() : false;
            if (updated && !mayCompat && this.mPackages.containsKey(packageName)) {
                this.mPackages.remove(packageName);
                scheduleWrite();
            }
        }
    }

    private void scheduleWrite() {
        this.mHandler.removeMessages(MSG_WRITE);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_WRITE), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
    }

    public CompatibilityInfo compatibilityInfoForPackageLocked(ApplicationInfo ai) {
        boolean z = true;
        if (SystemProperties.getInt("ro.config.compatibility_enable", 0) == COMPAT_FLAG_DONT_ASK) {
            addCompatForCheck(ai.packageName, true);
        }
        int i = this.mService.mConfiguration.screenLayout;
        int i2 = this.mService.mConfiguration.smallestScreenWidthDp;
        if ((getPackageFlags(ai.packageName) & COMPAT_FLAG_ENABLED) == 0) {
            z = false;
        }
        return new CompatibilityInfo(ai, i, i2, z);
    }

    public int computeCompatModeLocked(ApplicationInfo ai) {
        int i = 0;
        boolean enabled = (getPackageFlags(ai.packageName) & COMPAT_FLAG_ENABLED) != 0;
        CompatibilityInfo info = new CompatibilityInfo(ai, this.mService.mConfiguration.screenLayout, this.mService.mConfiguration.smallestScreenWidthDp, enabled);
        if (info.alwaysSupportsScreen()) {
            return -2;
        }
        if (info.neverSupportsScreen()) {
            return -1;
        }
        if (enabled) {
            i = COMPAT_FLAG_DONT_ASK;
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
        return (getPackageFlags(packageName) & COMPAT_FLAG_DONT_ASK) == 0;
    }

    public boolean getPackageNotifyUnsupportedZoomLocked(String packageName) {
        return (getPackageFlags(packageName) & UNSUPPORTED_ZOOM_FLAG_DONT_NOTIFY) == 0;
    }

    public void setFrontActivityAskCompatModeLocked(boolean ask) {
        ActivityRecord r = this.mService.getFocusedStack().topRunningActivityLocked();
        if (r != null) {
            setPackageAskCompatModeLocked(r.packageName, ask);
        }
    }

    public void setPackageAskCompatModeLocked(String packageName, boolean ask) {
        int curFlags = getPackageFlags(packageName);
        int newFlags = ask ? curFlags & -2 : curFlags | COMPAT_FLAG_DONT_ASK;
        if (curFlags != newFlags) {
            if (newFlags != 0) {
                this.mPackages.put(packageName, Integer.valueOf(newFlags));
            } else {
                this.mPackages.remove(packageName);
            }
            scheduleWrite();
        }
    }

    public void setPackageNotifyUnsupportedZoomLocked(String packageName, boolean notify) {
        int newFlags;
        int curFlags = getPackageFlags(packageName);
        if (notify) {
            newFlags = curFlags & -5;
        } else {
            newFlags = curFlags | UNSUPPORTED_ZOOM_FLAG_DONT_NOTIFY;
        }
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
            Slog.w(TAG, "setFrontActivityScreenCompatMode failed: no top activity");
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
            Slog.w(TAG, "setPackageScreenCompatMode failed: unknown package " + packageName);
            if (this.mPackages.keySet().contains(packageName)) {
                this.mPackages.remove(packageName);
                scheduleWrite();
            }
            return;
        }
        setPackageScreenCompatModeLocked(ai, mode);
    }

    private void setPackageScreenCompatModeLocked(ApplicationInfo ai, int mode) {
        boolean enable;
        String packageName = ai.packageName;
        int curFlags = getPackageFlags(packageName);
        switch (mode) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                enable = false;
                break;
            case COMPAT_FLAG_DONT_ASK /*1*/:
                enable = true;
                break;
            case COMPAT_FLAG_ENABLED /*2*/:
                if ((curFlags & COMPAT_FLAG_ENABLED) != 0) {
                    enable = false;
                    break;
                } else {
                    enable = true;
                    break;
                }
            default:
                Slog.w(TAG, "Unknown screen compat mode req #" + mode + "; ignoring");
                return;
        }
        int newFlags = curFlags;
        if (enable) {
            newFlags = curFlags | COMPAT_FLAG_ENABLED;
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
            scheduleWrite();
            this.mService.forceStopPackage(packageName, UserHandle.myUserId());
        }
    }

    void saveCompatModes() {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                HashMap<String, Integer> pkgs = new HashMap(this.mPackages);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = this.mFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fileOutputStream, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            out.startTag(null, "compat-packages");
            IPackageManager pm = AppGlobals.getPackageManager();
            int screenLayout = this.mService.mConfiguration.screenLayout;
            int smallestScreenWidthDp = this.mService.mConfiguration.smallestScreenWidthDp;
            for (Entry<String, Integer> entry : pkgs.entrySet()) {
                String pkg = (String) entry.getKey();
                int mode = ((Integer) entry.getValue()).intValue();
                if (mode != 0) {
                    ApplicationInfo ai = null;
                    try {
                        ai = pm.getApplicationInfo(pkg, 0, 0);
                    } catch (RemoteException e) {
                    }
                    if (ai != null) {
                        CompatibilityInfo info = new CompatibilityInfo(ai, screenLayout, smallestScreenWidthDp, false);
                        if (!(info.alwaysSupportsScreen() || info.neverSupportsScreen())) {
                            out.startTag(null, AbsLocationManagerService.DEL_PKG);
                            out.attribute(null, "name", pkg);
                            out.attribute(null, "mode", Integer.toString(mode));
                            out.endTag(null, AbsLocationManagerService.DEL_PKG);
                        }
                    } else {
                        continue;
                    }
                }
            }
            out.endTag(null, "compat-packages");
            out.endDocument();
            this.mFile.finishWrite(fileOutputStream);
        } catch (IOException e1) {
            Slog.w(TAG, "Error writing compat packages", e1);
            if (fileOutputStream != null) {
                this.mFile.failWrite(fileOutputStream);
            }
        }
    }
}
