package ohos.data.preferences.impl;

import android.content.SharedPreferences;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import ohos.app.Context;
import ohos.data.preferences.Preferences;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class PreferencesController {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "PreferencesController");
    private static volatile PreferencesController sInstance = null;
    private final Object mLock = new Object();
    private Map<File, PreferencesImpl> mSettingPrefsCache = new HashMap();

    private PreferencesController() {
    }

    public static PreferencesController getInstance() {
        if (sInstance == null) {
            synchronized (PreferencesController.class) {
                if (sInstance == null) {
                    sInstance = new PreferencesController();
                }
            }
        }
        return sInstance;
    }

    public Preferences getPreferences(Context context, String str) {
        return getPreferences(getPreferencesFile(context, str));
    }

    public boolean deletePreferences(Context context, String str) {
        return deletePreferences(getPreferencesFile(context, str));
    }

    private boolean deletePreferences(File file) {
        File makeBackupFile = PreferencesImpl.makeBackupFile(file);
        File makeBrokenFile = PreferencesImpl.makeBrokenFile(file);
        synchronized (this.mLock) {
            if (this.mSettingPrefsCache != null) {
                this.mSettingPrefsCache.remove(file);
            }
        }
        boolean z = true;
        if (file.exists() && !file.delete()) {
            z = false;
        }
        if (makeBackupFile.exists() && !makeBackupFile.delete()) {
            z = false;
        }
        if (!makeBrokenFile.exists() || makeBrokenFile.delete()) {
            return z;
        }
        return false;
    }

    public void removePreferencesFromCache(Context context, String str) {
        File preferencesFile = getPreferencesFile(context, str);
        synchronized (this.mLock) {
            if (this.mSettingPrefsCache != null) {
                this.mSettingPrefsCache.remove(preferencesFile);
            }
        }
    }

    private File getPreferencesFile(Context context, String str) {
        if (str == null || str.isEmpty() || str.indexOf(File.separatorChar) != -1) {
            throw new IllegalArgumentException("name is invalid.");
        } else if (context != null) {
            File preferencesDir = context.getPreferencesDir();
            if (preferencesDir != null) {
                return new File(preferencesDir, str);
            }
            throw new IllegalArgumentException("preferences path can not be null.");
        } else {
            throw new IllegalArgumentException("context can not be null.");
        }
    }

    private Preferences getPreferences(File file) {
        PreferencesImpl preferencesImpl;
        synchronized (this.mLock) {
            if (this.mSettingPrefsCache == null) {
                this.mSettingPrefsCache = new HashMap();
            }
            preferencesImpl = this.mSettingPrefsCache.get(file);
            if (preferencesImpl == null) {
                if (file.exists()) {
                    if (!file.canRead()) {
                        throw new IllegalArgumentException("no permission to read file " + file.getName());
                    }
                }
                preferencesImpl = new PreferencesImpl(file);
                this.mSettingPrefsCache.put(file, preferencesImpl);
            }
        }
        return preferencesImpl;
    }

    public boolean movePreferences(Context context, String str, Context context2, String str2) {
        File sourceFile = getSourceFile(context, str);
        if (!sourceFile.exists()) {
            return true;
        }
        List<String> readFile = AndroidPreferencesUtils.readFile(sourceFile);
        File preferencesFile = getPreferencesFile(context2, str2);
        if (AndroidPreferencesUtils.isAndroidFile(readFile)) {
            return moveAndroidFile(sourceFile, preferencesFile, readFile);
        }
        if (preferencesFile.exists()) {
            deletePreferences(preferencesFile);
        }
        try {
            Files.copy(sourceFile.toPath(), preferencesFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            PreferencesXmlUtils.limitFilePermission(preferencesFile);
            deletePreferences(sourceFile);
            return true;
        } catch (IOException unused) {
            throw new IllegalStateException("Failed to copy sourceName to targetName");
        }
    }

    private File getSourceFile(Context context, String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("sourceName can not be empty.");
        } else if (str.indexOf(File.separatorChar) < 0) {
            if (context != null) {
                File preferencesDir = context.getPreferencesDir();
                if (preferencesDir != null) {
                    return new File(preferencesDir, str);
                }
                throw new IllegalArgumentException("preferences path can not be null.");
            }
            throw new IllegalArgumentException("sourceContext can not be null.");
        } else if (str.charAt(0) == File.separatorChar) {
            try {
                return new File(str).getCanonicalFile();
            } catch (IOException unused) {
                throw new IllegalArgumentException("source file is invalid.");
            }
        } else {
            throw new IllegalArgumentException("sourceName is relative path.");
        }
    }

    private boolean moveAndroidFile(File file, File file2, List<String> list) {
        SharedPreferences sharedPreferences = AndroidPreferencesUtils.getSharedPreferences(file);
        if (sharedPreferences != null) {
            Map<String, ?> all = sharedPreferences.getAll();
            if ((all == null || all.size() == 0) && !AndroidPreferencesUtils.isEmptyAndroidFile(list)) {
                return false;
            }
            deletePreferences(file2);
            Preferences preferences = getPreferences(file2);
            moveAndroidFileInner(file, all, preferences);
            if (!preferences.flushSync()) {
                HiLog.error(LABEL, "failed to flushSync when movePreferences", new Object[0]);
                return false;
            } else if (file.delete()) {
                return true;
            } else {
                HiLog.error(LABEL, "failed to delete srcCanonicalFile when flushSync", new Object[0]);
                return true;
            }
        } else {
            throw new IllegalStateException("failed to get SharedPreferences instance of file " + file.getName() + " when movePreferences");
        }
    }

    private void moveAndroidFileInner(File file, Map<String, ?> map, Preferences preferences) {
        preferences.clear();
        if (map != null) {
            map.forEach(new BiConsumer(file) {
                /* class ohos.data.preferences.impl.$$Lambda$PreferencesController$IGmaPzzWsR8QDG_bdNv_HDA14 */
                private final /* synthetic */ File f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    PreferencesController.lambda$moveAndroidFileInner$0(Preferences.this, this.f$1, (String) obj, obj2);
                }
            });
        }
    }

    static /* synthetic */ void lambda$moveAndroidFileInner$0(Preferences preferences, File file, String str, Object obj) {
        if (obj instanceof String) {
            preferences.putString(str, (String) obj);
        } else if (obj instanceof Integer) {
            preferences.putInt(str, ((Integer) obj).intValue());
        } else if (obj instanceof Long) {
            preferences.putLong(str, ((Long) obj).longValue());
        } else if (obj instanceof Boolean) {
            preferences.putBoolean(str, ((Boolean) obj).booleanValue());
        } else if (obj instanceof Float) {
            preferences.putFloat(str, ((Float) obj).floatValue());
        } else if (obj instanceof Set) {
            HashSet hashSet = new HashSet();
            for (Object obj2 : (Set) obj) {
                if (obj2 instanceof String) {
                    hashSet.add((String) obj2);
                }
            }
            preferences.putStringSet(str, hashSet);
        } else {
            HiLog.error(LABEL, "invalid type of value in file :%{public}s", new Object[]{file.getName()});
        }
    }
}
