package android.content.res;

import android.os.FileUtils;
import android.os.Process;
import android.os.SELinux;
import android.util.Slog;
import com.android.internal.os.Zygote;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HwAssetManagerEx {
    private static final String BACKUP_RES_PATH = "/mnt/hep/work/system/lang/";
    private static final String DEFAULT_RES_PATH = "/data/share/SharedRes/";
    private static final int LOCALE_COUNTRY_1_END = 5;
    private static final int LOCALE_COUNTRY_1_START = 3;
    private static final int LOCALE_COUNTRY_2_END = 10;
    private static final int LOCALE_COUNTRY_2_START = 8;
    private static final int LOCALE_LANG_END = 2;
    private static final int LOCALE_LANG_START = 0;
    private static final int LOCALE_SCRIPT_END = 7;
    private static final int LOCALE_SCRIPT_START = 3;
    private static final int MAX_16_BIT = 65536;
    private static final String MIDDLE_LINE_STR = "-";
    private static final int ONE_SECOND = 1000;
    private static final int SHARE_TEXT_ID = 2130837504;
    private static final String TAG = "HwAssetManagerEx";
    private static ArrayList<ApkAssets> sApkAssetsList = new ArrayList<>();
    private static ArrayList<AssetManager> sAssetList = new ArrayList<>();
    private static String sCurLocale = "";
    private static boolean sIsHasSharedRes = false;
    private static boolean sIsUninited = true;
    private static String sLangLocale = "";
    private static long sLastCheck = 0;
    private static String[] sLastCheckResults = new String[0];
    private static Map<Integer, AssetManager> sSharedAsset = new HashMap();

    private HwAssetManagerEx() {
    }

    public static void setSharePemmison() {
        File resRoot = new File(DEFAULT_RES_PATH.trim());
        if (!resRoot.getParentFile().exists()) {
            if (resRoot.getParentFile().mkdir()) {
                FileUtils.setPermissions(resRoot.getParentFile().getAbsolutePath(), 509, Process.myUid(), 1023);
                SELinux.setFileContext(resRoot.getParentFile().getAbsolutePath(), "u:object_r:media_rw_data_file:s0");
            }
        } else if (resRoot.getParentFile().canWrite()) {
            FileUtils.setPermissions(resRoot.getParentFile().getAbsolutePath(), 509, Process.myUid(), 1023);
        }
        if (!resRoot.exists() && resRoot.mkdir()) {
            FileUtils.setPermissions(resRoot.getAbsolutePath(), 509, Process.myUid(), 1023);
        }
    }

    private static boolean makeSharedResource() {
        String script;
        String script2;
        String country;
        File[] filesArray;
        String lang;
        if (sLangLocale.equals(sCurLocale) && sSharedAsset.size() > 0) {
            return true;
        }
        if (!sSharedAsset.isEmpty()) {
            Iterator<AssetManager> it = sAssetList.iterator();
            while (it.hasNext()) {
                it.next().close();
            }
            sAssetList.clear();
            try {
                Iterator<ApkAssets> it2 = sApkAssetsList.iterator();
                while (it2.hasNext()) {
                    it2.next().close();
                }
                sApkAssetsList.clear();
            } catch (Throwable th) {
                Slog.e(TAG, "catch exception for release langpack");
            }
            sSharedAsset.clear();
        }
        String locale = getParentLocale(sCurLocale);
        int len = locale.length();
        boolean z = false;
        if (len == 5) {
            String lang2 = locale.substring(0, 2);
            script = "";
            script2 = locale.substring(3, 5);
            country = lang2;
        } else if (len == 7) {
            String lang3 = locale.substring(0, 2);
            script = locale.substring(3, 7);
            script2 = "";
            country = lang3;
        } else if (len == 10) {
            String lang4 = locale.substring(0, 2);
            script = locale.substring(3, 7);
            script2 = locale.substring(8, 10);
            country = lang4;
        } else {
            script = "";
            script2 = "";
            country = locale;
        }
        File resFolder = new File(DEFAULT_RES_PATH + country + "-" + script2);
        File backupResfolder = new File(BACKUP_RES_PATH + country + "-" + script2);
        if (!resFolder.exists() && !backupResfolder.exists()) {
            resFolder = new File(DEFAULT_RES_PATH + country + "-" + script);
            backupResfolder = new File(BACKUP_RES_PATH + country + "-" + script);
            if (!resFolder.exists() && !backupResfolder.exists()) {
                resFolder = new File(DEFAULT_RES_PATH + country);
                backupResfolder = new File(BACKUP_RES_PATH + country);
                if (!resFolder.exists() && !backupResfolder.exists()) {
                    sIsHasSharedRes = false;
                    return false;
                }
            }
        }
        File[] filesArray2 = resFolder.listFiles();
        if (filesArray2 == null) {
            filesArray = backupResfolder.listFiles();
        } else {
            filesArray = filesArray2;
        }
        if (filesArray != null) {
            int length = filesArray.length;
            int i = 0;
            while (i < length) {
                File fi = filesArray[i];
                if (fi.getName().contains(".lang")) {
                    try {
                        int resId = Integer.parseInt(fi.getName().replace(".lang", ""));
                        AssetManager am = new AssetManager(z);
                        sAssetList.add(am);
                        ApkAssets aa = ApkAssets.loadFromPath(fi.getPath());
                        sApkAssetsList.add(aa);
                        lang = country;
                        try {
                            am.setApkAssets(new ApkAssets[]{aa}, false);
                            sSharedAsset.put(Integer.valueOf(resId), am);
                        } catch (NumberFormatException e) {
                        } catch (IOException e2) {
                            Slog.e(TAG, "catch exception for create langpack");
                            i++;
                            country = lang;
                            z = false;
                        }
                    } catch (NumberFormatException e3) {
                        lang = country;
                        Slog.e(TAG, "MakeSharedResource NumberFormatException.");
                        i++;
                        country = lang;
                        z = false;
                    } catch (IOException e4) {
                        lang = country;
                        Slog.e(TAG, "catch exception for create langpack");
                        i++;
                        country = lang;
                        z = false;
                    }
                } else {
                    lang = country;
                }
                i++;
                country = lang;
                z = false;
            }
        }
        if (sSharedAsset.size() > 0) {
            sLangLocale = sCurLocale;
            sIsHasSharedRes = true;
            return true;
        }
        sIsHasSharedRes = false;
        return false;
    }

    public static boolean hasRes() {
        return sIsHasSharedRes;
    }

    private static String getParentLocale(String locale) {
        String[][] parents = {new String[]{"es-AR", "es-US"}, new String[]{"es-BO", "es-US"}, new String[]{"es-CL", "es-US"}, new String[]{"es-CO", "es-US"}, new String[]{"es-CR", "es-US"}, new String[]{"es-CU", "es-US"}, new String[]{"es-DO", "es-US"}, new String[]{"es-EC", "es-US"}, new String[]{"es-GT", "es-US"}, new String[]{"es-HN", "es-US"}, new String[]{"es-MX", "es-US"}, new String[]{"es-NI", "es-US"}, new String[]{"es-PA", "es-US"}, new String[]{"es-PE", "es-US"}, new String[]{"es-PR", "es-US"}, new String[]{"es-PY", "es-US"}, new String[]{"es-SV", "es-US"}, new String[]{"es-UY", "es-US"}, new String[]{"es-VE", "es-US"}, new String[]{"pt-BR", "pt-BR"}, new String[]{"pt-", "pt-PT"}, new String[]{"en-AU", "en-GB"}, new String[]{"en-BE", "en-GB"}, new String[]{"en-DG", "en-GB"}, new String[]{"en-FK", "en-GB"}, new String[]{"en-GG", "en-GB"}, new String[]{"en-GI", "en-GB"}, new String[]{"en-HK", "en-GB"}, new String[]{"en-IE", "en-GB"}, new String[]{"en-IM", "en-GB"}, new String[]{"en-IN", "en-GB"}, new String[]{"en-IO", "en-GB"}, new String[]{"en-JE", "en-GB"}, new String[]{"en-MO", "en-GB"}, new String[]{"en-MT", "en-GB"}, new String[]{"en-NZ", "en-GB"}, new String[]{"en-PK", "en-GB"}, new String[]{"en-SG", "en-GB"}, new String[]{"en-SH", "en-GB"}, new String[]{"en-VG", "en-GB"}, new String[]{"en-BN", "en-GB"}, new String[]{"en-MY", "en-GB"}, new String[]{"en-PG", "en-GB"}, new String[]{"en-NR", "en-GB"}, new String[]{"en-WS", "en-GB"}, new String[]{"zh-MO", "zh-HK"}, new String[]{"zh-SG", "zh-CN"}, new String[]{"ms-BN", "ms-MY"}, new String[]{"ms-SG", "ms-MY"}, new String[]{"uz-AF", "uz-UZ"}, new String[]{"bo-IN", "bo-CN"}, new String[]{"mn-", "mn-Cyrl"}, new String[]{"my-Qaag", "my-Qaag"}, new String[]{"my-", "my-MM"}, new String[]{"mai-", "mai-Deva-IN"}};
        for (int i = 0; i < parents.length; i++) {
            if (locale.startsWith(parents[i][0])) {
                return parents[i][1];
            }
        }
        return locale;
    }

    public static void setDbidConfig(String locale) {
        if (!sCurLocale.equals(locale)) {
            String cmdLine = Process.getCmdlineForPid(Process.myPid());
            if (!cmdLine.contains(Zygote.PRIMARY_SOCKET_NAME) && !cmdLine.contains("system_server")) {
                synchronized (sSharedAsset) {
                    sCurLocale = locale;
                    makeSharedResource();
                }
            }
        }
    }

    public static CharSequence getTextForDBid(CharSequence dbidCs) {
        CharSequence sharedResult = null;
        if (dbidCs == null || dbidCs.length() <= 5 || dbidCs.charAt(0) != '[' || dbidCs.charAt(3) != '_') {
            return null;
        }
        String dbidStr = dbidCs.toString();
        int dbid = Integer.parseInt(dbidStr.substring(4, dbidStr.indexOf(93)));
        if (dbid > 0) {
            synchronized (sSharedAsset) {
                AssetManager am = sSharedAsset.get(Integer.valueOf(dbid / 65536));
                if (am == null && sIsUninited) {
                    makeSharedResource();
                    sIsUninited = false;
                    am = sSharedAsset.get(Integer.valueOf(dbid / 65536));
                }
                if (am != null) {
                    CharSequence sharedResult2 = am.getResourceText(SHARE_TEXT_ID | (dbid % 65536), true);
                    if (sharedResult2 != null && sharedResult2.length() > 0) {
                        return sharedResult2;
                    }
                    sharedResult = null;
                }
            }
        }
        return sharedResult;
    }

    public static String[] getSharedResList() {
        long now = System.currentTimeMillis();
        if (now - sLastCheck <= 1000) {
            return sLastCheckResults;
        }
        File resFolder = new File(DEFAULT_RES_PATH.trim());
        String[] firsts = null;
        String[] seconds = null;
        if (resFolder.exists()) {
            firsts = resFolder.list();
        }
        File resFolder2 = new File(BACKUP_RES_PATH.trim());
        if (resFolder2.exists()) {
            seconds = resFolder2.list();
        }
        if (firsts == null) {
            firsts = new String[0];
        }
        if (seconds == null) {
            seconds = new String[0];
        }
        String[] results = (String[]) Arrays.copyOf(firsts, firsts.length + seconds.length);
        System.arraycopy(seconds, 0, results, firsts.length, seconds.length);
        sLastCheck = now;
        sLastCheckResults = results;
        return results;
    }

    public static String getCurLocale() {
        return sCurLocale;
    }

    public static String processConfig(AssetManager am, String locale, boolean isFlag) {
        return locale;
    }
}
