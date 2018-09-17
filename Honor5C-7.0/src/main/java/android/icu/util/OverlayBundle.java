package android.icu.util;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Deprecated
public class OverlayBundle extends ResourceBundle {
    private String[] baseNames;
    private ResourceBundle[] bundles;
    private Locale locale;

    @Deprecated
    public OverlayBundle(String[] baseNames, Locale locale) {
        this.baseNames = baseNames;
        this.locale = locale;
        this.bundles = new ResourceBundle[baseNames.length];
    }

    @Deprecated
    protected Object handleGetObject(String key) throws MissingResourceException {
        Object o = null;
        for (int i = 0; i < this.bundles.length; i++) {
            load(i);
            try {
                o = this.bundles[i].getObject(key);
            } catch (MissingResourceException e) {
                if (i == this.bundles.length - 1) {
                    throw e;
                }
            }
            if (o != null) {
                break;
            }
        }
        return o;
    }

    @Deprecated
    public Enumeration<String> getKeys() {
        int i = this.bundles.length - 1;
        load(i);
        return this.bundles[i].getKeys();
    }

    private void load(int i) throws MissingResourceException {
        if (this.bundles[i] == null) {
            boolean tryWildcard = false;
            try {
                this.bundles[i] = ResourceBundle.getBundle(this.baseNames[i], this.locale);
                if (!this.bundles[i].getLocale().equals(this.locale)) {
                    if (!(this.locale.getCountry().length() == 0 || i == this.bundles.length - 1)) {
                        tryWildcard = true;
                    }
                    if (tryWildcard) {
                        try {
                            this.bundles[i] = ResourceBundle.getBundle(this.baseNames[i], new Locale("xx", this.locale.getCountry(), this.locale.getVariant()));
                        } catch (MissingResourceException e) {
                            if (this.bundles[i] == null) {
                                throw e;
                            }
                        }
                    }
                }
            } catch (MissingResourceException e2) {
                if (i == this.bundles.length - 1) {
                    throw e2;
                }
                tryWildcard = true;
            }
        }
    }
}
