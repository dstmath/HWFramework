package ohos.global.configuration;

import java.util.Locale;

public class Configuration {
    private static final int DEFAULT_VALUE = 1;
    public static final int DIMEN_UNDEFINED = -1;
    public static final int DIRECTION_HORIZONTAL = 1;
    public static final int DIRECTION_UNDEFINED = -1;
    public static final int DIRECTION_VERTICAL = 0;
    public static final float SCALE_UNDEFINED = -1.0f;
    public int direction;
    public float fontRatio;
    public boolean isLayoutRTL;
    private LocaleProfile localeProfile;

    public Configuration() {
        this.localeProfile = new LocaleProfile(new Locale[]{Locale.getDefault()});
        this.direction = -1;
        this.fontRatio = 1.0f;
        this.isLayoutRTL = false;
    }

    public Configuration(Configuration configuration) {
        if (configuration == null) {
            this.direction = -1;
            this.fontRatio = -1.0f;
            this.isLayoutRTL = false;
        } else {
            LocaleProfile localeProfile2 = configuration.localeProfile;
            if (localeProfile2 != null) {
                this.localeProfile = new LocaleProfile(LocaleProfile.cloneNonNullLocales(localeProfile2.getLocales()));
            }
            this.direction = configuration.direction;
            this.fontRatio = configuration.fontRatio;
            this.isLayoutRTL = configuration.isLayoutRTL;
        }
        checkLocaleProfile();
    }

    public LocaleProfile getLocaleProfile() {
        return this.localeProfile;
    }

    public void setLocaleProfile(LocaleProfile localeProfile2) {
        this.localeProfile = localeProfile2;
        checkLocaleProfile();
    }

    public Locale getFirstLocale() {
        return getLocaleProfile().getLocales()[0];
    }

    private void checkLocaleProfile() {
        Locale locale = Locale.getDefault();
        LocaleProfile localeProfile2 = this.localeProfile;
        if (localeProfile2 == null || localeProfile2.getLocales() == null || this.localeProfile.getLocales().length <= 0) {
            this.localeProfile = new LocaleProfile(new Locale[]{locale});
        } else if (this.localeProfile.getLocales()[0] != null) {
            Locale locale2 = this.localeProfile.getLocales()[0];
        } else {
            this.localeProfile.getLocales()[0] = locale;
        }
    }
}
