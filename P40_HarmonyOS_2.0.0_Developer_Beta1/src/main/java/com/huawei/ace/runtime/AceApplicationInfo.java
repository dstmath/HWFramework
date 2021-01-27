package com.huawei.ace.runtime;

import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import ohos.dmsdp.sdk.DMSDPConfig;
import ohos.global.icu.impl.locale.LanguageTag;

public final class AceApplicationInfo {
    private static final int GRAY_THRESH_HOLD = 225;
    private static final AceApplicationInfo INSTANCE = new AceApplicationInfo();
    private static final String LOG_TAG = "AceApplicationInfo";
    private String languageTag;
    private IAceLocaleChanged localeChangedCallback;
    private IAceLocaleFallback localeFallbackCallback;
    private String packageName;
    private int pid;

    public static int getGrayThreshHold() {
        return 225;
    }

    private native void nativeInitialize(AceApplicationInfo aceApplicationInfo);

    private native void nativeLocaleChanged(String str, String str2, String str3, String str4);

    private native void nativeSetPackageInfo(String str, boolean z, boolean z2);

    public static int toGray(int i, int i2, int i3) {
        return (((i * 38) + (i2 * 75)) + (i3 * 15)) >> 7;
    }

    public static AceApplicationInfo getInstance() {
        return INSTANCE;
    }

    public AceApplicationInfo() {
        nativeInitialize(this);
    }

    public void setPackageInfo(String str, boolean z, boolean z2) {
        this.packageName = str;
        nativeSetPackageInfo(str, z, z2);
    }

    public void setPid(int i) {
        this.pid = i;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public int getPid() {
        return this.pid;
    }

    public String getCountryOrRegion() {
        return Locale.getDefault().getCountry();
    }

    public String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public String getScript() {
        return Locale.getDefault().getScript();
    }

    public String getKeywordsAndValues() {
        Set<String> unicodeLocaleKeys = Locale.getDefault().getUnicodeLocaleKeys();
        if (unicodeLocaleKeys.isEmpty()) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (String str : unicodeLocaleKeys) {
            stringBuffer.append(str);
            stringBuffer.append("=");
            stringBuffer.append(Locale.getDefault().getUnicodeLocaleType(str));
            stringBuffer.append(DMSDPConfig.LIST_TO_STRING_SPLIT);
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        return stringBuffer.toString();
    }

    public String getLanguageTag() {
        return this.languageTag;
    }

    public void changeLocale(String str, String str2) {
        if (str == null || str2 == null) {
            ALog.e(LOG_TAG, "the language or country is null.");
            return;
        }
        Locale.setDefault(new Locale(str.toLowerCase(Locale.ENGLISH), str2.toUpperCase(Locale.ENGLISH)));
        setLocale();
        IAceLocaleChanged iAceLocaleChanged = this.localeChangedCallback;
        if (iAceLocaleChanged != null) {
            iAceLocaleChanged.onLocaleChanged();
        } else {
            ALog.e(LOG_TAG, "localeChangedCallback is null");
        }
    }

    public void setLocaleChanged(IAceLocaleChanged iAceLocaleChanged) {
        this.localeChangedCallback = iAceLocaleChanged;
    }

    public void setLocaleFallback(IAceLocaleFallback iAceLocaleFallback) {
        this.localeFallbackCallback = iAceLocaleFallback;
    }

    private String getLocalesFallback(String str, String[] strArr) {
        IAceLocaleFallback iAceLocaleFallback = this.localeFallbackCallback;
        return iAceLocaleFallback != null ? iAceLocaleFallback.onLocaleFallback(str, strArr) : "";
    }

    public void setLocale() {
        CompletableFuture.runAsync(new Runnable() {
            /* class com.huawei.ace.runtime.$$Lambda$AceApplicationInfo$AJgJB_6mqWajD49WCZ_uiY8OEmQ */

            @Override // java.lang.Runnable
            public final void run() {
                AceApplicationInfo.this.lambda$setLocale$0$AceApplicationInfo();
            }
        });
    }

    public /* synthetic */ void lambda$setLocale$0$AceApplicationInfo() {
        StringJoiner stringJoiner = new StringJoiner(LanguageTag.SEP);
        stringJoiner.add(getLanguage());
        stringJoiner.add(getScript());
        stringJoiner.add(getCountryOrRegion());
        stringJoiner.add(getKeywordsAndValues());
        this.languageTag = stringJoiner.toString();
        nativeLocaleChanged(getLanguage(), getCountryOrRegion(), getScript(), getKeywordsAndValues());
    }
}
