package com.android.internal.app;

import android.content.Context;
import android.provider.Settings.Global;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.LogException;
import com.android.internal.content.NativeLibraryHelper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Set;

public class LocaleStore {
    private static boolean sFullyInitialized = false;
    private static final HashMap<String, LocaleInfo> sLocaleCache = new HashMap();

    public static class LocaleInfo {
        private static final int SUGGESTION_TYPE_CFG = 2;
        private static final int SUGGESTION_TYPE_NONE = 0;
        private static final int SUGGESTION_TYPE_SIM = 1;
        private String mFullCountryNameNative;
        private String mFullNameNative;
        private final String mId;
        private boolean mIsChecked;
        private boolean mIsPseudo;
        private boolean mIsTranslated;
        private String mLangScriptKey;
        private final Locale mLocale;
        private final Locale mParent;
        private int mSuggestionFlags;

        private LocaleInfo(Locale locale) {
            this.mLocale = locale;
            this.mId = locale.toLanguageTag();
            this.mParent = getParent(locale);
            this.mIsChecked = false;
            this.mSuggestionFlags = 0;
            this.mIsTranslated = false;
            this.mIsPseudo = false;
        }

        private LocaleInfo(String localeId) {
            this(Locale.forLanguageTag(localeId));
        }

        private static Locale getParent(Locale locale) {
            if (locale.getCountry().isEmpty()) {
                return null;
            }
            Builder localeBuilder = new Builder().setLanguageTag("en");
            try {
                localeBuilder = new Builder().setLocale(locale).setRegion(LogException.NO_VALUE);
            } catch (IllformedLocaleException e) {
                Log.e("LocaleStore", "Error locale: " + locale.toLanguageTag());
            }
            return localeBuilder.build();
        }

        public String toString() {
            return this.mId;
        }

        public Locale getLocale() {
            return this.mLocale;
        }

        public Locale getParent() {
            return this.mParent;
        }

        public String getId() {
            return this.mId;
        }

        public boolean isTranslated() {
            return this.mIsTranslated;
        }

        public void setTranslated(boolean isTranslated) {
            this.mIsTranslated = isTranslated;
        }

        boolean isSuggested() {
            boolean z = false;
            if (!this.mIsTranslated) {
                return false;
            }
            if (this.mSuggestionFlags != 0) {
                z = true;
            }
            return z;
        }

        private boolean isSuggestionOfType(int suggestionMask) {
            boolean z = false;
            if (!this.mIsTranslated) {
                return false;
            }
            if ((this.mSuggestionFlags & suggestionMask) == suggestionMask) {
                z = true;
            }
            return z;
        }

        public String getFullNameNative() {
            if (this.mFullNameNative == null) {
                this.mFullNameNative = LocaleHelper.getDisplayName(this.mLocale, this.mLocale, true);
            }
            return this.mFullNameNative;
        }

        String getFullCountryNameNative() {
            if (this.mFullCountryNameNative == null) {
                this.mFullCountryNameNative = LocaleHelper.getDisplayCountry(this.mLocale, this.mLocale);
            }
            return this.mFullCountryNameNative;
        }

        String getFullCountryNameInUiLanguage() {
            return LocaleHelper.getDisplayCountry(this.mLocale);
        }

        public String getFullNameInUiLanguage() {
            return LocaleHelper.getDisplayName(this.mLocale, true);
        }

        private String getLangScriptKey() {
            if (this.mLangScriptKey == null) {
                String toLanguageTag;
                Locale parentWithScript = getParent(LocaleHelper.addLikelySubtags(this.mLocale));
                if (parentWithScript == null) {
                    toLanguageTag = this.mLocale.toLanguageTag();
                } else {
                    toLanguageTag = parentWithScript.toLanguageTag();
                }
                this.mLangScriptKey = toLanguageTag;
            }
            return this.mLangScriptKey;
        }

        String getLabel(boolean countryMode) {
            if (countryMode) {
                return getFullCountryNameNative();
            }
            return getFullNameNative();
        }

        String getContentDescription(boolean countryMode) {
            if (countryMode) {
                return getFullCountryNameInUiLanguage();
            }
            return getFullNameInUiLanguage();
        }

        public boolean getChecked() {
            return this.mIsChecked;
        }

        public void setChecked(boolean checked) {
            this.mIsChecked = checked;
        }
    }

    private static Set<String> getSimCountries(Context context) {
        Set<String> result = new HashSet();
        TelephonyManager tm = TelephonyManager.from(context);
        if (tm != null) {
            String iso = tm.getSimCountryIso().toUpperCase(Locale.US);
            if (!iso.isEmpty()) {
                result.add(iso);
                if ("MM".equals(iso)) {
                    result.add("ZG");
                }
            }
            iso = tm.getNetworkCountryIso().toUpperCase(Locale.US);
            if (!iso.isEmpty()) {
                result.add(iso);
                if ("MM".equals(iso)) {
                    result.add("ZG");
                }
            }
        }
        return result;
    }

    public static void updateSimCountries(Context context) {
        Set<String> simCountries = getSimCountries(context);
        for (LocaleInfo li : sLocaleCache.values()) {
            if (simCountries.contains(li.getLocale().getCountry())) {
                li.mSuggestionFlags = li.mSuggestionFlags | 1;
            }
        }
    }

    private static void addSuggestedLocalesForRegion(Locale locale) {
        if (locale != null) {
            String country = locale.getCountry();
            if (!country.isEmpty()) {
                for (LocaleInfo li : sLocaleCache.values()) {
                    if (country.equals(li.getLocale().getCountry())) {
                        li.mSuggestionFlags = li.mSuggestionFlags | 1;
                    }
                    if ("ZG".equals(country) && country.equals(li.getLocale().getCountry())) {
                        li.mSuggestionFlags = li.mSuggestionFlags | 1;
                    }
                }
            }
        }
    }

    public static void fillCache(Context context) {
        if (!sFullyInitialized) {
            LocaleInfo li;
            Set<String> simCountries = getSimCountries(context);
            String[] supportedLocales = LocalePicker.getSupportedLocales(context);
            for (String localeId : supportedLocales) {
                if (localeId.isEmpty()) {
                    throw new IllformedLocaleException("Bad locale entry in locale_config.xml");
                }
                li = new LocaleInfo(localeId, null);
                if (simCountries.contains(li.getLocale().getCountry())) {
                    li.mSuggestionFlags = li.mSuggestionFlags | 1;
                }
                sLocaleCache.put(li.getId(), li);
                Locale parent = li.getParent();
                if (parent != null) {
                    String parentId = parent.toLanguageTag();
                    if (!sLocaleCache.containsKey(parentId)) {
                        sLocaleCache.put(parentId, new LocaleInfo(parent, null));
                    }
                }
            }
            boolean isInDeveloperMode = Global.getInt(context.getContentResolver(), "development_settings_enabled", 0) != 0;
            for (String localeId2 : LocalePicker.getPseudoLocales()) {
                li = getLocaleInfo(Locale.forLanguageTag(localeId2));
                if (isInDeveloperMode && Arrays.asList(supportedLocales).contains(localeId2)) {
                    li.setTranslated(true);
                    li.mIsPseudo = true;
                    li.mSuggestionFlags = li.mSuggestionFlags | 1;
                } else {
                    sLocaleCache.remove(li.getId());
                }
            }
            HashSet<String> localizedLocales = new HashSet();
            for (String localeId22 : LocalePicker.getSystemAssetLocales()) {
                li = new LocaleInfo(localeId22, null);
                String country = li.getLocale().getCountry();
                if (!country.isEmpty()) {
                    LocaleInfo cachedLocale = null;
                    if (sLocaleCache.containsKey(li.getId())) {
                        cachedLocale = (LocaleInfo) sLocaleCache.get(li.getId());
                    } else {
                        String langScriptCtry = li.getLangScriptKey() + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + country;
                        if (sLocaleCache.containsKey(langScriptCtry)) {
                            cachedLocale = (LocaleInfo) sLocaleCache.get(langScriptCtry);
                        }
                    }
                    if (cachedLocale != null) {
                        cachedLocale.mSuggestionFlags = cachedLocale.mSuggestionFlags | 2;
                    }
                }
                localizedLocales.add(li.getLangScriptKey());
            }
            for (LocaleInfo li2 : sLocaleCache.values()) {
                li2.setTranslated(localizedLocales.contains(li2.getLangScriptKey()));
            }
            addSuggestedLocalesForRegion(Locale.getDefault());
            sFullyInitialized = true;
        }
    }

    private static int getLevel(Set<String> ignorables, LocaleInfo li, boolean translatedOnly) {
        if (ignorables.contains(li.getId())) {
            return 0;
        }
        if (li.mIsPseudo) {
            return 2;
        }
        return ((!translatedOnly || (li.isTranslated() ^ 1) == 0) && li.getParent() != null) ? 2 : 0;
    }

    public static Set<LocaleInfo> getLevelLocales(Context context, Set<String> ignorables, LocaleInfo parent, boolean translatedOnly) {
        fillCache(context);
        String parentId = parent == null ? null : parent.getId();
        HashSet<LocaleInfo> result = new HashSet();
        HashMap<String, LocaleInfo> localCache = new HashMap();
        localCache.putAll(sLocaleCache);
        for (LocaleInfo li : localCache.values()) {
            if (getLevel(ignorables, li, translatedOnly) == 2) {
                if (parent != null) {
                    if (parentId.equals(li.getParent().toLanguageTag())) {
                        result.add(li);
                    }
                } else if (li.isSuggestionOfType(1)) {
                    result.add(li);
                } else {
                    result.add(getLocaleInfo(li.getParent()));
                }
            }
        }
        return result;
    }

    public static LocaleInfo getLocaleInfo(Locale locale) {
        String id = locale.toLanguageTag();
        if (sLocaleCache.containsKey(id)) {
            return (LocaleInfo) sLocaleCache.get(id);
        }
        LocaleInfo result = new LocaleInfo(locale, null);
        sLocaleCache.put(id, result);
        return result;
    }
}
