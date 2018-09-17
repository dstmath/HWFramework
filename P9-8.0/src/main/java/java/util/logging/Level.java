package java.util.logging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import sun.util.logging.PlatformLogger;

public class Level implements Serializable {
    public static final Level ALL = new Level("ALL", Integer.MIN_VALUE, defaultBundle);
    public static final Level CONFIG = new Level("CONFIG", PlatformLogger.CONFIG, defaultBundle);
    public static final Level FINE = new Level("FINE", 500, defaultBundle);
    public static final Level FINER = new Level("FINER", 400, defaultBundle);
    public static final Level FINEST = new Level("FINEST", 300, defaultBundle);
    public static final Level INFO = new Level("INFO", PlatformLogger.INFO, defaultBundle);
    public static final Level OFF = new Level("OFF", Integer.MAX_VALUE, defaultBundle);
    public static final Level SEVERE = new Level("SEVERE", 1000, defaultBundle);
    public static final Level WARNING = new Level("WARNING", PlatformLogger.WARNING, defaultBundle);
    private static final String defaultBundle = "sun.util.logging.resources.logging";
    private static final long serialVersionUID = -8176160795706313070L;
    private transient Locale cachedLocale;
    private transient String localizedLevelName;
    private final String name;
    private final String resourceBundleName;
    private final int value;

    static final class KnownLevel {
        private static Map<Integer, List<KnownLevel>> intToLevels = new HashMap();
        private static Map<String, List<KnownLevel>> nameToLevels = new HashMap();
        final Level levelObject;
        final Level mirroredLevel;

        KnownLevel(Level l) {
            this.levelObject = l;
            if (l.getClass() == Level.class) {
                this.mirroredLevel = l;
            } else {
                this.mirroredLevel = new Level(l.name, l.value, l.resourceBundleName, false, null);
            }
        }

        static synchronized void add(Level l) {
            synchronized (KnownLevel.class) {
                KnownLevel o = new KnownLevel(l);
                List<KnownLevel> list = (List) nameToLevels.get(l.name);
                if (list == null) {
                    list = new ArrayList();
                    nameToLevels.put(l.name, list);
                }
                list.add(o);
                list = (List) intToLevels.get(Integer.valueOf(l.value));
                if (list == null) {
                    list = new ArrayList();
                    intToLevels.put(Integer.valueOf(l.value), list);
                }
                list.add(o);
            }
        }

        static synchronized KnownLevel findByName(String name) {
            synchronized (KnownLevel.class) {
                List<KnownLevel> list = (List) nameToLevels.get(name);
                if (list != null) {
                    KnownLevel knownLevel = (KnownLevel) list.get(0);
                    return knownLevel;
                }
                return null;
            }
        }

        static synchronized KnownLevel findByValue(int value) {
            synchronized (KnownLevel.class) {
                List<KnownLevel> list = (List) intToLevels.get(Integer.valueOf(value));
                if (list != null) {
                    KnownLevel knownLevel = (KnownLevel) list.get(0);
                    return knownLevel;
                }
                return null;
            }
        }

        static synchronized KnownLevel findByLocalizedLevelName(String name) {
            synchronized (KnownLevel.class) {
                for (List<KnownLevel> levels : nameToLevels.values()) {
                    for (KnownLevel l : levels) {
                        if (name.equals(l.levelObject.getLocalizedLevelName())) {
                            return l;
                        }
                    }
                }
                return null;
            }
        }

        /* JADX WARNING: Missing block: B:21:0x005b, code:
            return null;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        static synchronized KnownLevel matches(Level l) {
            synchronized (KnownLevel.class) {
                List<KnownLevel> list = (List) nameToLevels.get(l.name);
                if (list != null) {
                    for (KnownLevel level : list) {
                        Level other = level.mirroredLevel;
                        Class<? extends Level> type = level.levelObject.getClass();
                        if (l.value == other.value && ((l.resourceBundleName == other.resourceBundleName || (l.resourceBundleName != null && l.resourceBundleName.equals(other.resourceBundleName))) && type == l.getClass())) {
                            return level;
                        }
                    }
                }
            }
        }
    }

    /* synthetic */ Level(String name, int value, String resourceBundleName, boolean visible, Level -this4) {
        this(name, value, resourceBundleName, visible);
    }

    protected Level(String name, int value) {
        this(name, value, null);
    }

    protected Level(String name, int value, String resourceBundleName) {
        this(name, value, resourceBundleName, true);
    }

    private Level(String name, int value, String resourceBundleName, boolean visible) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.value = value;
        this.resourceBundleName = resourceBundleName;
        if (resourceBundleName != null) {
            name = null;
        }
        this.localizedLevelName = name;
        this.cachedLocale = null;
        if (visible) {
            KnownLevel.add(this);
        }
    }

    public String getResourceBundleName() {
        return this.resourceBundleName;
    }

    public String getName() {
        return this.name;
    }

    public String getLocalizedName() {
        return getLocalizedLevelName();
    }

    final String getLevelName() {
        return this.name;
    }

    private String computeLocalizedLevelName(Locale newLocale) {
        ResourceBundle rb = ResourceBundle.getBundle(this.resourceBundleName, newLocale, Thread.currentThread().getContextClassLoader());
        String localizedName = rb.getString(this.name);
        if (!defaultBundle.equals(this.resourceBundleName)) {
            return localizedName;
        }
        String str;
        Locale rbLocale = rb.getLocale();
        Locale locale = (Locale.ROOT.equals(rbLocale) || this.name.equals(localizedName.toUpperCase(Locale.ROOT))) ? Locale.ROOT : rbLocale;
        if (Locale.ROOT.equals(locale)) {
            str = this.name;
        } else {
            str = localizedName.toUpperCase(locale);
        }
        return str;
    }

    final String getCachedLocalizedLevelName() {
        if (this.localizedLevelName != null && this.cachedLocale != null && this.cachedLocale.equals(Locale.getDefault())) {
            return this.localizedLevelName;
        }
        if (this.resourceBundleName == null) {
            return this.name;
        }
        return null;
    }

    final synchronized String getLocalizedLevelName() {
        String cachedLocalizedName = getCachedLocalizedLevelName();
        if (cachedLocalizedName != null) {
            return cachedLocalizedName;
        }
        Locale newLocale = Locale.getDefault();
        try {
            this.localizedLevelName = computeLocalizedLevelName(newLocale);
        } catch (Exception e) {
            this.localizedLevelName = this.name;
        }
        this.cachedLocale = newLocale;
        return this.localizedLevelName;
    }

    static Level findLevel(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        KnownLevel level = KnownLevel.findByName(name);
        if (level != null) {
            return level.mirroredLevel;
        }
        try {
            int x = Integer.parseInt(name);
            level = KnownLevel.findByValue(x);
            if (level == null) {
                Level levelObject = new Level(name, x);
                level = KnownLevel.findByValue(x);
            }
            return level.mirroredLevel;
        } catch (NumberFormatException e) {
            level = KnownLevel.findByLocalizedLevelName(name);
            if (level != null) {
                return level.mirroredLevel;
            }
            return null;
        }
    }

    public final String toString() {
        return this.name;
    }

    public final int intValue() {
        return this.value;
    }

    private Object readResolve() {
        KnownLevel o = KnownLevel.matches(this);
        if (o != null) {
            return o.levelObject;
        }
        return new Level(this.name, this.value, this.resourceBundleName);
    }

    public static synchronized Level parse(String name) throws IllegalArgumentException {
        synchronized (Level.class) {
            name.length();
            KnownLevel level = KnownLevel.findByName(name);
            Level level2;
            if (level != null) {
                level2 = level.levelObject;
                return level2;
            }
            try {
                int x = Integer.parseInt(name);
                level = KnownLevel.findByValue(x);
                if (level == null) {
                    Level levelObject = new Level(name, x);
                    level = KnownLevel.findByValue(x);
                }
                level2 = level.levelObject;
                return level2;
            } catch (NumberFormatException e) {
                level = KnownLevel.findByLocalizedLevelName(name);
                if (level != null) {
                    return level.levelObject;
                }
                throw new IllegalArgumentException("Bad level \"" + name + "\"");
            }
        }
    }

    public boolean equals(Object ox) {
        boolean z = false;
        try {
            if (((Level) ox).value == this.value) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            return false;
        }
    }

    public int hashCode() {
        return this.value;
    }
}
