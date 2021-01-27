package ohos.data.preferences.impl;

import android.content.SharedPreferences;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AndroidPreferencesUtils {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "AndroidPreferencesUtils");

    /* JADX WARNING: Removed duplicated region for block: B:38:0x0097 A[SYNTHETIC, Splitter:B:38:0x0097] */
    public static List<String> readFile(File file) {
        Throwable th;
        ArrayList arrayList = new ArrayList();
        if (!file.getName().endsWith(".xml")) {
            return arrayList;
        }
        BufferedReader bufferedReader = null;
        try {
            BufferedReader bufferedReader2 = new BufferedReader(new FileReader(file));
            try {
                char[] cArr = new char[100];
                int read = bufferedReader2.read(cArr, 0, 100);
                if (read <= 0) {
                    try {
                        bufferedReader2.close();
                    } catch (IOException unused) {
                        HiLog.error(LABEL, "Failed to close source file %{public}s", new Object[]{file.getName()});
                    }
                    return arrayList;
                }
                String[] split = String.valueOf(cArr, 0, read).split("[\r\n]");
                for (String str : split) {
                    if (str != null) {
                        String trim = str.trim();
                        if (!trim.isEmpty()) {
                            arrayList.add(trim);
                        }
                    }
                }
                try {
                    bufferedReader2.close();
                } catch (IOException unused2) {
                    HiLog.error(LABEL, "Failed to close source file %{public}s", new Object[]{file.getName()});
                }
                return arrayList;
            } catch (IOException unused3) {
                bufferedReader = bufferedReader2;
                try {
                    throw new IllegalStateException("Failed to read source file" + file.getName());
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException unused4) {
                            HiLog.error(LABEL, "Failed to close source file %{public}s", new Object[]{file.getName()});
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedReader = bufferedReader2;
                if (bufferedReader != null) {
                }
                throw th;
            }
        } catch (IOException unused5) {
            throw new IllegalStateException("Failed to read source file" + file.getName());
        }
    }

    public static boolean isAndroidFile(List<String> list) {
        if (list != null && list.size() >= 2) {
            String str = list.get(0);
            if (!str.startsWith("<?xml") || !str.endsWith("?>") || !list.get(1).startsWith("<map")) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static boolean isEmptyAndroidFile(List<String> list) {
        if (list == null || list.size() != 2) {
            return false;
        }
        String str = list.get(1);
        return str.startsWith("<map") && str.endsWith("/>");
    }

    public static SharedPreferences getSharedPreferences(File file) {
        try {
            Constructor<?> declaredConstructor = Class.forName("android.app.SharedPreferencesImpl").getDeclaredConstructor(File.class, Integer.TYPE);
            declaredConstructor.setAccessible(true);
            Object newInstance = declaredConstructor.newInstance(file, 0);
            if (newInstance instanceof SharedPreferences) {
                return (SharedPreferences) newInstance;
            }
            HiLog.error(LABEL, "failed to get SharedPreferences when getSharedPreferences", new Object[0]);
            return null;
        } catch (InstantiationException e) {
            HiLog.error(LABEL, "movePreferences has InstantiationException,eMsg:%{public}s", new Object[]{e.getMessage()});
            return null;
        } catch (InvocationTargetException e2) {
            HiLog.error(LABEL, "movePreferences has InvocationTargetException,eMsg:%{public}s", new Object[]{e2.getMessage()});
            return null;
        } catch (NoSuchMethodException e3) {
            HiLog.error(LABEL, "movePreferences has NoSuchMethodException,eMsg:%{public}s", new Object[]{e3.getMessage()});
            return null;
        } catch (IllegalAccessException e4) {
            HiLog.error(LABEL, "movePreferences has IllegalAccessException,eMsg:%{public}s", new Object[]{e4.getMessage()});
            return null;
        } catch (ClassNotFoundException e5) {
            HiLog.error(LABEL, "movePreferences has ClassNotFoundException,eMsg:%{public}s", new Object[]{e5.getMessage()});
            return null;
        }
    }
}
