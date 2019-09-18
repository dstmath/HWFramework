package com.android.server;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.LocaleList;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.internal.app.LocalePicker;
import java.util.ArrayList;
import java.util.IllformedLocaleException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class UpdateConfig {
    private static final String TAG = "UpdateConfig";
    private static boolean isChange = false;

    public static void updateLocalesWhenOTA(Context context) {
        if (context != null) {
            PackageManager mPackageManager = context.getPackageManager();
            if (mPackageManager != null && mPackageManager.isUpgrade()) {
                LocaleList defaultsList = updateLocale(context, LocalePicker.getLocales());
                String prop = SystemProperties.get("ril.operator.numeric");
                if (prop.length() >= 3 && "414".equals(prop.substring(0, 3))) {
                    defaultsList = updateLocale(defaultsList);
                }
                if (isChange) {
                    LocalePicker.updateLocales(defaultsList);
                }
            }
        }
    }

    public static void updateLocalesWhenOTAEX(Context context, int preSdkVersion) {
        if (context != null) {
            PackageManager mPackageManager = context.getPackageManager();
            if (mPackageManager != null && mPackageManager.isUpgrade()) {
                LocaleList defaultsList = updateLocale(context, LocalePicker.getLocales());
                String prop = SystemProperties.get("ril.operator.numeric");
                if (preSdkVersion != -1 && preSdkVersion < 28 && prop.length() >= 3 && "414".equals(prop.substring(0, 3))) {
                    defaultsList = updateLocale(defaultsList);
                }
                if (isChange) {
                    LocalePicker.updateLocales(defaultsList);
                }
            }
        }
    }

    private static LocaleList updateLocale(LocaleList instance) {
        LocaleList result;
        LocaleList defaultsList = instance;
        if (defaultsList == null) {
            return instance;
        }
        List<Locale> nList = new ArrayList<>();
        int size = defaultsList.size();
        boolean isChangeLocal = false;
        for (int i = 0; i < size; i++) {
            Locale locale = defaultsList.get(i);
            if (i != 0 || (!locale.getLanguage().equals("en") && !locale.getLanguage().equals("zh"))) {
                nList.add(locale);
            } else {
                Locale.Builder localeBuilder = new Locale.Builder().setLanguageTag("en");
                try {
                    localeBuilder = new Locale.Builder().setLocale(locale).setRegion("ZG");
                } catch (IllformedLocaleException e) {
                    Slog.e(TAG, "Error locale: " + locale.toLanguageTag());
                }
                nList.add(localeBuilder.build());
                isChangeLocal = true;
            }
        }
        if (isChangeLocal) {
            List<Locale> rList = new ArrayList<>();
            for (int i2 = 0; i2 < size; i2++) {
                Locale tmpL = nList.get(i2);
                if (!rList.contains(tmpL)) {
                    rList.add(tmpL);
                }
            }
            result = new LocaleList((Locale[]) rList.toArray(new Locale[0]));
            isChange = true;
        } else {
            result = instance;
        }
        return result;
    }

    private static LocaleList updateLocale(Context context, LocaleList instance) {
        LocaleList result;
        LocaleList defaultsList = instance;
        if (defaultsList == null) {
            return instance;
        }
        List<Locale> nList = new ArrayList<>();
        int size = defaultsList.size();
        boolean isChangeLocal = false;
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= size) {
                break;
            }
            boolean isContinues = false;
            Locale locale = defaultsList.get(i2);
            String scriptName = locale.getScript();
            if (scriptName != null && !scriptName.isEmpty()) {
                String langName = locale.getLanguage();
                if ((langName.equals("en") || langName.equals("es") || langName.equals("pt")) && scriptName.equalsIgnoreCase("Latn")) {
                    Locale.Builder languageTag = new Locale.Builder().setLanguageTag("en");
                    Locale nLocale = null;
                    boolean nLocaleValid = true;
                    try {
                        nLocale = new Locale.Builder().setLocale(locale).setScript("").build();
                    } catch (IllformedLocaleException e) {
                        StringBuilder sb = new StringBuilder();
                        IllformedLocaleException illformedLocaleException = e;
                        sb.append("Error locale: ");
                        sb.append(locale.toLanguageTag());
                        Log.e("UpdateConfigy", sb.toString());
                        nLocaleValid = false;
                    }
                    if (nLocaleValid) {
                        locale = nLocale;
                        isChangeLocal = true;
                    }
                }
            }
            Iterator<Locale> it = nList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    Context context2 = context;
                    break;
                }
                if (langEqual(context, locale, it.next())) {
                    isContinues = true;
                    isChangeLocal = true;
                    break;
                }
            }
            if (!isContinues) {
                nList.add(locale);
            }
            i = i2 + 1;
        }
        Context context3 = context;
        if (isChangeLocal) {
            result = new LocaleList((Locale[]) nList.toArray(new Locale[0]));
            isChange = true;
        } else {
            result = instance;
        }
        return result;
    }

    private static boolean langEqual(Context context, Locale locale, Locale nLocale) {
        if (!locale.getLanguage().equals(nLocale.getLanguage()) || !HwFrameworkFactory.getHwLocaleStoreEx().getDialectsName(context, locale.toLanguageTag()).equals(HwFrameworkFactory.getHwLocaleStoreEx().getDialectsName(context, nLocale.toLanguageTag()))) {
            return false;
        }
        return true;
    }
}
