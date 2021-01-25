package ohos.data.preferences.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Set;
import ohos.data.preferences.PreferencesFileReadException;
import ohos.data.preferences.PreferencesFileWriteException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class PreferencesXmlUtils {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "PreferencesXmlUtils");

    private static native void nativeInit();

    private static native HashMap<String, Object> nativeReadSettingXml(String str) throws PreferencesFileReadException;

    private static native boolean nativeWriteSettingXml(HashMap<String, Object> hashMap, String str) throws PreferencesFileWriteException;

    static {
        try {
            System.loadLibrary("preferences_jni.z");
            HiLog.debug(LABEL, "preferences_jni.z loaded success.", new Object[0]);
            nativeInit();
        } catch (SecurityException | UnsatisfiedLinkError e) {
            HiLog.error(LABEL, "preferences_jni.z loaded fail:%{public}s", new Object[]{e.getMessage()});
        }
    }

    private PreferencesXmlUtils() {
    }

    public static final HashMap<String, Object> readSettingXml(String str) throws PreferencesFileReadException {
        return nativeReadSettingXml(str);
    }

    public static final boolean writeSettingXml(HashMap<String, Object> hashMap, String str) throws PreferencesFileWriteException {
        return nativeWriteSettingXml(hashMap, str);
    }

    public static void limitFilePermission(File file) {
        if (file != null) {
            try {
                Set<PosixFilePermission> posixFilePermissions = Files.getPosixFilePermissions(file.toPath(), new LinkOption[0]);
                if (posixFilePermissions.removeAll(PosixFilePermissions.fromString("--x--xrwx"))) {
                    Files.setPosixFilePermissions(file.toPath(), posixFilePermissions);
                }
            } catch (IOException unused) {
                HiLog.error(LABEL, "an I/O error occurs when limiting permissions of file:%{public}s", new Object[]{file.getName()});
            }
        }
    }
}
