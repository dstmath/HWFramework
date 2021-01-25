package com.android.server;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.LocaleList;
import android.util.Slog;
import com.android.internal.app.LocalePicker;
import java.util.ArrayList;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Locale;

public class UpdateConfig {
    private static final String BURMESE = "my";
    private static final String ENGLISH = "en";
    private static final String MYANMAR = "MM";
    private static final String QAAG = "Qaag";
    private static final String TAG = "UpdateConfig";
    private static final int UNAVAILABLE_VERSION = -1;
    private static final String ZAWGYI = "ZG";

    public static void updateLocalesWhenOTA(Context context) {
        PackageManager packageManager;
        if (context != null && (packageManager = context.getPackageManager()) != null && packageManager.isUpgrade()) {
            updateLocale();
        }
    }

    public static void updateLocalesWhenOTAEX(Context context, int preSdkVersion) {
        PackageManager packageManager;
        if (context != null && preSdkVersion != -1 && (packageManager = context.getPackageManager()) != null && packageManager.isUpgrade()) {
            updateLocale();
        }
    }

    private static void updateLocale() {
        LocaleList defaultsList = LocalePicker.getLocales();
        if (defaultsList != null) {
            List<Locale> newList = new ArrayList<>();
            boolean isChange = false;
            int size = defaultsList.size();
            for (int i = 0; i < size; i++) {
                Locale locale = defaultsList.get(i);
                if ((ENGLISH.equals(locale.getLanguage()) || BURMESE.equals(locale.getLanguage())) && ZAWGYI.equals(locale.getCountry())) {
                    new Locale.Builder();
                    try {
                        locale = new Locale.Builder().setLocale(locale).setScript(QAAG).setRegion(MYANMAR).build();
                        isChange = true;
                    } catch (IllformedLocaleException e) {
                        Slog.e(TAG, "Error locale: " + locale.toLanguageTag());
                    }
                    newList.add(locale);
                } else {
                    newList.add(locale);
                }
            }
            if (isChange) {
                List<Locale> repeatList = new ArrayList<>();
                for (int i2 = 0; i2 < size; i2++) {
                    Locale tempLocale = newList.get(i2);
                    if (!repeatList.contains(tempLocale)) {
                        repeatList.add(tempLocale);
                    }
                }
                LocalePicker.updateLocales(new LocaleList((Locale[]) repeatList.toArray(new Locale[0])));
            }
        }
    }
}
