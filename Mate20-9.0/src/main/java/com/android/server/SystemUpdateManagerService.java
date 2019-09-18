package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.ISystemUpdateManager;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SystemUpdateManagerService extends ISystemUpdateManager.Stub {
    private static final String INFO_FILE = "system-update-info.xml";
    private static final int INFO_FILE_VERSION = 0;
    private static final String KEY_BOOT_COUNT = "boot-count";
    private static final String KEY_INFO_BUNDLE = "info-bundle";
    private static final String KEY_UID = "uid";
    private static final String KEY_VERSION = "version";
    private static final String TAG = "SystemUpdateManagerService";
    private static final String TAG_INFO = "info";
    private static final int UID_UNKNOWN = -1;
    private final Context mContext;
    private final AtomicFile mFile;
    private int mLastStatus = 0;
    private int mLastUid = -1;
    private final Object mLock = new Object();

    public SystemUpdateManagerService(Context context) {
        this.mContext = context;
        this.mFile = new AtomicFile(new File(Environment.getDataSystemDirectory(), INFO_FILE));
        synchronized (this.mLock) {
            loadSystemUpdateInfoLocked();
        }
    }

    public void updateSystemUpdateInfo(PersistableBundle infoBundle) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.RECOVERY", TAG);
        int status = infoBundle.getInt("status", 0);
        if (status == 0) {
            Slog.w(TAG, "Invalid status info. Ignored");
            return;
        }
        int uid = Binder.getCallingUid();
        if (this.mLastUid == -1 || this.mLastUid == uid || status != 1) {
            synchronized (this.mLock) {
                saveSystemUpdateInfoLocked(infoBundle, uid);
            }
        } else {
            Slog.i(TAG, "Inactive updater reporting IDLE status. Ignored");
        }
    }

    public Bundle retrieveSystemUpdateInfo() {
        Bundle loadSystemUpdateInfoLocked;
        if (this.mContext.checkCallingOrSelfPermission("android.permission.READ_SYSTEM_UPDATE_INFO") == -1 && this.mContext.checkCallingOrSelfPermission("android.permission.RECOVERY") == -1) {
            throw new SecurityException("Can't read system update info. Requiring READ_SYSTEM_UPDATE_INFO or RECOVERY permission.");
        }
        synchronized (this.mLock) {
            loadSystemUpdateInfoLocked = loadSystemUpdateInfoLocked();
        }
        return loadSystemUpdateInfoLocked;
    }

    private Bundle loadSystemUpdateInfoLocked() {
        FileInputStream fis;
        PersistableBundle loadedBundle = null;
        try {
            fis = this.mFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, StandardCharsets.UTF_8.name());
            loadedBundle = readInfoFileLocked(parser);
            if (fis != null) {
                fis.close();
            }
        } catch (FileNotFoundException e) {
            Slog.i(TAG, "No existing info file " + this.mFile.getBaseFile());
        } catch (XmlPullParserException e2) {
            Slog.e(TAG, "Failed to parse the info file:", e2);
        } catch (IOException e3) {
            Slog.e(TAG, "Failed to read the info file:", e3);
        } catch (Throwable th) {
            r0.addSuppressed(th);
        }
        if (loadedBundle == null) {
            return removeInfoFileAndGetDefaultInfoBundleLocked();
        }
        if (loadedBundle.getInt(KEY_VERSION, -1) == -1) {
            Slog.w(TAG, "Invalid info file (invalid version). Ignored");
            return removeInfoFileAndGetDefaultInfoBundleLocked();
        }
        int lastUid = loadedBundle.getInt("uid", -1);
        if (lastUid == -1) {
            Slog.w(TAG, "Invalid info file (invalid UID). Ignored");
            return removeInfoFileAndGetDefaultInfoBundleLocked();
        }
        int lastBootCount = loadedBundle.getInt(KEY_BOOT_COUNT, -1);
        if (lastBootCount == -1 || lastBootCount != getBootCount()) {
            Slog.w(TAG, "Outdated info file. Ignored");
            return removeInfoFileAndGetDefaultInfoBundleLocked();
        }
        PersistableBundle infoBundle = loadedBundle.getPersistableBundle(KEY_INFO_BUNDLE);
        if (infoBundle == null) {
            Slog.w(TAG, "Invalid info file (missing info). Ignored");
            return removeInfoFileAndGetDefaultInfoBundleLocked();
        }
        int lastStatus = infoBundle.getInt("status", 0);
        if (lastStatus == 0) {
            Slog.w(TAG, "Invalid info file (invalid status). Ignored");
            return removeInfoFileAndGetDefaultInfoBundleLocked();
        }
        this.mLastStatus = lastStatus;
        this.mLastUid = lastUid;
        return new Bundle(infoBundle);
        throw th;
    }

    private void saveSystemUpdateInfoLocked(PersistableBundle infoBundle, int uid) {
        PersistableBundle outBundle = new PersistableBundle();
        outBundle.putPersistableBundle(KEY_INFO_BUNDLE, infoBundle);
        outBundle.putInt(KEY_VERSION, 0);
        outBundle.putInt("uid", uid);
        outBundle.putInt(KEY_BOOT_COUNT, getBootCount());
        if (writeInfoFileLocked(outBundle)) {
            this.mLastUid = uid;
            this.mLastStatus = infoBundle.getInt("status");
        }
    }

    private PersistableBundle readInfoFileLocked(XmlPullParser parser) throws XmlPullParserException, IOException {
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return null;
            }
            if (type == 2 && TAG_INFO.equals(parser.getName())) {
                return PersistableBundle.restoreFromXml(parser);
            }
        }
    }

    private boolean writeInfoFileLocked(PersistableBundle outBundle) {
        FileOutputStream fos = null;
        try {
            fos = this.mFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            out.startTag(null, TAG_INFO);
            outBundle.saveToXml(out);
            out.endTag(null, TAG_INFO);
            out.endDocument();
            this.mFile.finishWrite(fos);
            return true;
        } catch (IOException | XmlPullParserException e) {
            Slog.e(TAG, "Failed to save the info file:", e);
            if (fos != null) {
                this.mFile.failWrite(fos);
            }
            return false;
        }
    }

    private Bundle removeInfoFileAndGetDefaultInfoBundleLocked() {
        if (this.mFile.exists()) {
            Slog.i(TAG, "Removing info file");
            this.mFile.delete();
        }
        this.mLastStatus = 0;
        this.mLastUid = -1;
        Bundle infoBundle = new Bundle();
        infoBundle.putInt("status", 0);
        return infoBundle;
    }

    private int getBootCount() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "boot_count", 0);
    }
}
