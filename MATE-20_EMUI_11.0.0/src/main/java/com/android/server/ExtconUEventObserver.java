package com.android.server;

import android.os.UEventObserver;
import android.util.ArrayMap;
import android.util.Slog;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class ExtconUEventObserver extends UEventObserver {
    private static final boolean LOG = false;
    private static final String SELINUX_POLICIES_NEED_TO_BE_CHANGED = "This probably means the selinux policies need to be changed.";
    private static final String TAG = "ExtconUEventObserver";
    private final Map<String, ExtconInfo> mExtconInfos = new ArrayMap();

    /* access modifiers changed from: protected */
    public abstract void onUEvent(ExtconInfo extconInfo, UEventObserver.UEvent uEvent);

    public final void onUEvent(UEventObserver.UEvent event) {
        ExtconInfo info = this.mExtconInfos.get(event.get("DEVPATH"));
        if (info != null) {
            onUEvent(info, event);
            return;
        }
        Slog.w(TAG, "No match found for DEVPATH of " + event + " in " + this.mExtconInfos);
    }

    public void startObserving(ExtconInfo extconInfo) {
        String devicePath = extconInfo.getDevicePath();
        if (devicePath == null) {
            Slog.wtf(TAG, "Unable to start observing  " + extconInfo.getName() + " because the device path is null. " + SELINUX_POLICIES_NEED_TO_BE_CHANGED);
            return;
        }
        this.mExtconInfos.put(devicePath, extconInfo);
        startObserving("DEVPATH=" + devicePath);
    }

    public static final class ExtconInfo {
        private static final String TAG = "ExtconInfo";
        private final String mName;

        public static List<ExtconInfo> getExtconInfos(String regex) {
            if (!ExtconUEventObserver.extconExists()) {
                return new ArrayList(0);
            }
            Pattern p = regex == null ? null : Pattern.compile(regex);
            File file = new File("/sys/class/extcon");
            File[] files = file.listFiles();
            if (files == null) {
                Slog.wtf(TAG, file + " exists " + file.exists() + " isDir " + file.isDirectory() + " but listFiles returns null. " + ExtconUEventObserver.SELINUX_POLICIES_NEED_TO_BE_CHANGED);
                return new ArrayList(0);
            }
            ArrayList list = new ArrayList(files.length);
            for (File f : files) {
                String name = f.getName();
                if (p == null || p.matcher(name).matches()) {
                    list.add(new ExtconInfo(name));
                }
            }
            return list;
        }

        public ExtconInfo(String name) {
            this.mName = name;
        }

        public String getName() {
            return this.mName;
        }

        public String getDevicePath() {
            try {
                File devPath = new File(String.format(Locale.US, "/sys/class/extcon/%s", this.mName));
                if (!devPath.exists()) {
                    return null;
                }
                String canonicalPath = devPath.getCanonicalPath();
                return canonicalPath.substring(canonicalPath.indexOf("/devices"));
            } catch (IOException e) {
                Slog.e(TAG, "Could not get the extcon device path for " + this.mName, e);
                return null;
            }
        }

        public String getStatePath() {
            return String.format(Locale.US, "/sys/class/extcon/%s/state", this.mName);
        }
    }

    public static boolean namedExtconDirExists(String name) {
        File extconDir = new File("/sys/class/extcon/" + name);
        return extconDir.exists() && extconDir.isDirectory();
    }

    public static boolean extconExists() {
        File extconDir = new File("/sys/class/extcon");
        if (extconDir.exists() && extconDir.isDirectory()) {
            Slog.w(TAG, extconDir + " exists " + extconDir.exists() + " isDir " + extconDir.isDirectory() + " but reporting it does not exist until extcon kernel fix.");
        }
        return false;
    }
}
