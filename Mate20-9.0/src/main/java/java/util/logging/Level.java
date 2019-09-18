package java.util.logging;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Level implements Serializable {
    public static final Level ALL = new Level("ALL", Integer.MIN_VALUE, defaultBundle);
    public static final Level CONFIG = new Level("CONFIG", 700, defaultBundle);
    public static final Level FINE = new Level("FINE", 500, defaultBundle);
    public static final Level FINER = new Level("FINER", HttpURLConnection.HTTP_BAD_REQUEST, defaultBundle);
    public static final Level FINEST = new Level("FINEST", HttpURLConnection.HTTP_MULT_CHOICE, defaultBundle);
    public static final Level INFO = new Level("INFO", 800, defaultBundle);
    public static final Level OFF = new Level("OFF", Integer.MAX_VALUE, defaultBundle);
    public static final Level SEVERE = new Level("SEVERE", 1000, defaultBundle);
    public static final Level WARNING = new Level("WARNING", 900, defaultBundle);
    private static final String defaultBundle = "sun.util.logging.resources.logging";
    private static final long serialVersionUID = -8176160795706313070L;
    private transient Locale cachedLocale;
    private transient String localizedLevelName;
    /* access modifiers changed from: private */
    public final String name;
    /* access modifiers changed from: private */
    public final String resourceBundleName;
    /* access modifiers changed from: private */
    public final int value;

    static final class KnownLevel {
        private static Map<Integer, List<KnownLevel>> intToLevels = new HashMap();
        private static Map<String, List<KnownLevel>> nameToLevels = new HashMap();
        final Level levelObject;
        final Level mirroredLevel;

        KnownLevel(Level l) {
            this.levelObject = l;
            if (l.getClass() == Level.class) {
                this.mirroredLevel = l;
                return;
            }
            Level level = new Level(l.name, l.value, l.resourceBundleName, false);
            this.mirroredLevel = level;
        }

        static synchronized void add(Level l) {
            synchronized (KnownLevel.class) {
                KnownLevel o = new KnownLevel(l);
                List<KnownLevel> list = nameToLevels.get(l.name);
                if (list == null) {
                    list = new ArrayList<>();
                    nameToLevels.put(l.name, list);
                }
                list.add(o);
                List<KnownLevel> list2 = intToLevels.get(Integer.valueOf(l.value));
                if (list2 == null) {
                    list2 = new ArrayList<>();
                    intToLevels.put(Integer.valueOf(l.value), list2);
                }
                list2.add(o);
            }
        }

        static synchronized KnownLevel findByName(String name) {
            synchronized (KnownLevel.class) {
                List<KnownLevel> list = nameToLevels.get(name);
                if (list == null) {
                    return null;
                }
                KnownLevel knownLevel = list.get(0);
                return knownLevel;
            }
        }

        static synchronized KnownLevel findByValue(int value) {
            synchronized (KnownLevel.class) {
                List<KnownLevel> list = intToLevels.get(Integer.valueOf(value));
                if (list == null) {
                    return null;
                }
                KnownLevel knownLevel = list.get(0);
                return knownLevel;
            }
        }

        static synchronized KnownLevel findByLocalizedLevelName(String name) {
            synchronized (KnownLevel.class) {
                for (List<KnownLevel> levels : nameToLevels.values()) {
                    Iterator<KnownLevel> it = levels.iterator();
                    while (true) {
                        if (it.hasNext()) {
                            KnownLevel l = it.next();
                            if (name.equals(l.levelObject.getLocalizedLevelName())) {
                                return l;
                            }
                        }
                    }
                }
                return null;
            }
        }

        static synchronized KnownLevel matches(Level l) {
            synchronized (KnownLevel.class) {
                List<KnownLevel> list = nameToLevels.get(l.name);
                if (list != null) {
                    for (KnownLevel level : list) {
                        Level other = level.mirroredLevel;
                        Class<?> cls = level.levelObject.getClass();
                        if (l.value == other.value && ((l.resourceBundleName == other.resourceBundleName || (l.resourceBundleName != null && l.resourceBundleName.equals(other.resourceBundleName))) && cls == l.getClass())) {
                            return level;
                        }
                    }
                }
                return null;
            }
        }
    }

    protected Level(String name2, int value2) {
        this(name2, value2, null);
    }

    protected Level(String name2, int value2, String resourceBundleName2) {
        this(name2, value2, resourceBundleName2, true);
    }

    private Level(String name2, int value2, String resourceBundleName2, boolean visible) {
        if (name2 != null) {
            this.name = name2;
            this.value = value2;
            this.resourceBundleName = resourceBundleName2;
            this.localizedLevelName = resourceBundleName2 == null ? name2 : null;
            this.cachedLocale = null;
            if (visible) {
                KnownLevel.add(this);
                return;
            }
            return;
        }
        throw new NullPointerException();
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

    /* access modifiers changed from: package-private */
    public final String getLevelName() {
        return this.name;
    }

    private String computeLocalizedLevelName(Locale newLocale) {
        ResourceBundle rb = ResourceBundle.getBundle(this.resourceBundleName, newLocale, Thread.currentThread().getContextClassLoader());
        String localizedName = rb.getString(this.name);
        if (!defaultBundle.equals(this.resourceBundleName)) {
            return localizedName;
        }
        Locale rbLocale = rb.getLocale();
        Locale locale = (Locale.ROOT.equals(rbLocale) || this.name.equals(localizedName.toUpperCase(Locale.ROOT))) ? Locale.ROOT : rbLocale;
        return Locale.ROOT.equals(locale) ? this.name : localizedName.toUpperCase(locale);
    }

    /* access modifiers changed from: package-private */
    public final String getCachedLocalizedLevelName() {
        if (this.localizedLevelName != null && this.cachedLocale != null && this.cachedLocale.equals(Locale.getDefault())) {
            return this.localizedLevelName;
        }
        if (this.resourceBundleName == null) {
            return this.name;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final synchronized String getLocalizedLevelName() {
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

    static Level findLevel(String name2) {
        if (name2 != null) {
            KnownLevel level = KnownLevel.findByName(name2);
            if (level != null) {
                return level.mirroredLevel;
            }
            try {
                int x = Integer.parseInt(name2);
                KnownLevel level2 = KnownLevel.findByValue(x);
                if (level2 == null) {
                    new Level(name2, x);
                    level2 = KnownLevel.findByValue(x);
                }
                return level2.mirroredLevel;
            } catch (NumberFormatException e) {
                KnownLevel level3 = KnownLevel.findByLocalizedLevelName(name2);
                if (level3 != null) {
                    return level3.mirroredLevel;
                }
                return null;
            }
        } else {
            throw new NullPointerException();
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

    public static synchronized Level parse(String name2) throws IllegalArgumentException {
        synchronized (Level.class) {
            name2.length();
            KnownLevel level = KnownLevel.findByName(name2);
            if (level != null) {
                Level level2 = level.levelObject;
                return level2;
            }
            try {
                int x = Integer.parseInt(name2);
                KnownLevel level3 = KnownLevel.findByValue(x);
                if (level3 == null) {
                    new Level(name2, x);
                    level3 = KnownLevel.findByValue(x);
                }
                Level levelObject = level3.levelObject;
                return levelObject;
            } catch (NumberFormatException e) {
                KnownLevel level4 = KnownLevel.findByLocalizedLevelName(name2);
                if (level4 != null) {
                    return level4.levelObject;
                }
                throw new IllegalArgumentException("Bad level \"" + name2 + "\"");
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
