package android.icu.text;

import android.icu.impl.ICUConfig;
import android.icu.impl.SoftCache;
import android.icu.impl.TZDBTimeZoneNames;
import android.icu.impl.TimeZoneNamesImpl;
import android.icu.util.ULocale;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public abstract class TimeZoneNames implements Serializable {
    private static final String DEFAULT_FACTORY_CLASS = "android.icu.impl.TimeZoneNamesFactoryImpl";
    private static final String FACTORY_NAME_PROP = "android.icu.text.TimeZoneNames.Factory.impl";
    private static Cache TZNAMES_CACHE = new Cache();
    private static final Factory TZNAMES_FACTORY;
    private static final long serialVersionUID = -9180227029248969153L;

    @Deprecated
    public static abstract class Factory {
        @Deprecated
        public abstract TimeZoneNames getTimeZoneNames(ULocale uLocale);

        @Deprecated
        protected Factory() {
        }
    }

    private static class Cache extends SoftCache<String, TimeZoneNames, ULocale> {
        /* synthetic */ Cache(Cache -this0) {
            this();
        }

        private Cache() {
        }

        protected TimeZoneNames createInstance(String key, ULocale data) {
            return TimeZoneNames.TZNAMES_FACTORY.getTimeZoneNames(data);
        }
    }

    private static class DefaultTimeZoneNames extends TimeZoneNames {
        public static final DefaultTimeZoneNames INSTANCE = new DefaultTimeZoneNames();
        private static final long serialVersionUID = -995672072494349071L;

        public static class FactoryImpl extends Factory {
            public TimeZoneNames getTimeZoneNames(ULocale locale) {
                return DefaultTimeZoneNames.INSTANCE;
            }
        }

        private DefaultTimeZoneNames() {
        }

        public Set<String> getAvailableMetaZoneIDs() {
            return Collections.emptySet();
        }

        public Set<String> getAvailableMetaZoneIDs(String tzID) {
            return Collections.emptySet();
        }

        public String getMetaZoneID(String tzID, long date) {
            return null;
        }

        public String getReferenceZoneID(String mzID, String region) {
            return null;
        }

        public String getMetaZoneDisplayName(String mzID, NameType type) {
            return null;
        }

        public String getTimeZoneDisplayName(String tzID, NameType type) {
            return null;
        }

        public Collection<MatchInfo> find(CharSequence text, int start, EnumSet<NameType> enumSet) {
            return Collections.emptyList();
        }
    }

    public static class MatchInfo {
        private int _matchLength;
        private String _mzID;
        private NameType _nameType;
        private String _tzID;

        public MatchInfo(NameType nameType, String tzID, String mzID, int matchLength) {
            if (nameType == null) {
                throw new IllegalArgumentException("nameType is null");
            } else if (tzID == null && mzID == null) {
                throw new IllegalArgumentException("Either tzID or mzID must be available");
            } else if (matchLength <= 0) {
                throw new IllegalArgumentException("matchLength must be positive value");
            } else {
                this._nameType = nameType;
                this._tzID = tzID;
                this._mzID = mzID;
                this._matchLength = matchLength;
            }
        }

        public String tzID() {
            return this._tzID;
        }

        public String mzID() {
            return this._mzID;
        }

        public NameType nameType() {
            return this._nameType;
        }

        public int matchLength() {
            return this._matchLength;
        }
    }

    public enum NameType {
        LONG_GENERIC,
        LONG_STANDARD,
        LONG_DAYLIGHT,
        SHORT_GENERIC,
        SHORT_STANDARD,
        SHORT_DAYLIGHT,
        EXEMPLAR_LOCATION
    }

    public abstract Set<String> getAvailableMetaZoneIDs();

    public abstract Set<String> getAvailableMetaZoneIDs(String str);

    public abstract String getMetaZoneDisplayName(String str, NameType nameType);

    public abstract String getMetaZoneID(String str, long j);

    public abstract String getReferenceZoneID(String str, String str2);

    public abstract String getTimeZoneDisplayName(String str, NameType nameType);

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0021  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static {
        Factory factory = null;
        String classname = ICUConfig.get(FACTORY_NAME_PROP, DEFAULT_FACTORY_CLASS);
        while (true) {
            try {
                factory = (Factory) Class.forName(classname).newInstance();
                break;
            } catch (ClassNotFoundException e) {
            } catch (IllegalAccessException e2) {
            } catch (InstantiationException e3) {
            }
            classname = DEFAULT_FACTORY_CLASS;
        }
        if (factory == null) {
            factory = new FactoryImpl();
        }
        TZNAMES_FACTORY = factory;
        return;
        if (classname.equals(DEFAULT_FACTORY_CLASS)) {
            if (factory == null) {
            }
            TZNAMES_FACTORY = factory;
            return;
        }
        classname = DEFAULT_FACTORY_CLASS;
    }

    public static TimeZoneNames getInstance(ULocale locale) {
        return (TimeZoneNames) TZNAMES_CACHE.getInstance(locale.getBaseName(), locale);
    }

    public static TimeZoneNames getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    public static TimeZoneNames getTZDBInstance(ULocale locale) {
        return new TZDBTimeZoneNames(locale);
    }

    public final String getDisplayName(String tzID, NameType type, long date) {
        String name = getTimeZoneDisplayName(tzID, type);
        if (name == null) {
            return getMetaZoneDisplayName(getMetaZoneID(tzID, date), type);
        }
        return name;
    }

    public String getExemplarLocationName(String tzID) {
        return TimeZoneNamesImpl.getDefaultExemplarLocationName(tzID);
    }

    public Collection<MatchInfo> find(CharSequence text, int start, EnumSet<NameType> enumSet) {
        throw new UnsupportedOperationException("The method is not implemented in TimeZoneNames base class.");
    }

    @Deprecated
    public void loadAllDisplayNames() {
    }

    @Deprecated
    public void getDisplayNames(String tzID, NameType[] types, long date, String[] dest, int destOffset) {
        if (tzID != null && tzID.length() != 0) {
            String mzID = null;
            for (int i = 0; i < types.length; i++) {
                NameType type = types[i];
                String name = getTimeZoneDisplayName(tzID, type);
                if (name == null) {
                    if (mzID == null) {
                        mzID = getMetaZoneID(tzID, date);
                    }
                    name = getMetaZoneDisplayName(mzID, type);
                }
                dest[destOffset + i] = name;
            }
        }
    }

    protected TimeZoneNames() {
    }
}
