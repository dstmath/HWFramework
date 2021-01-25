package com.android.commands.monkey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class MonkeyUtils {
    private static final Date DATE = new Date();
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ");
    private static PackageFilter sFilter;

    private MonkeyUtils() {
    }

    public static synchronized String toCalendarTime(long time) {
        String format;
        synchronized (MonkeyUtils.class) {
            DATE.setTime(time);
            format = DATE_FORMATTER.format(DATE);
        }
        return format;
    }

    public static PackageFilter getPackageFilter() {
        if (sFilter == null) {
            sFilter = new PackageFilter();
        }
        return sFilter;
    }

    public static class PackageFilter {
        private Set<String> mInvalidPackages;
        private Set<String> mValidPackages;

        private PackageFilter() {
            this.mValidPackages = new HashSet();
            this.mInvalidPackages = new HashSet();
        }

        public void addValidPackages(Set<String> validPackages) {
            this.mValidPackages.addAll(validPackages);
        }

        public void addInvalidPackages(Set<String> invalidPackages) {
            this.mInvalidPackages.addAll(invalidPackages);
        }

        public boolean hasValidPackages() {
            return this.mValidPackages.size() > 0;
        }

        public boolean isPackageValid(String pkg) {
            return this.mValidPackages.contains(pkg);
        }

        public boolean isPackageInvalid(String pkg) {
            return this.mInvalidPackages.contains(pkg);
        }

        public boolean checkEnteringPackage(String pkg) {
            if (this.mInvalidPackages.size() > 0) {
                if (this.mInvalidPackages.contains(pkg)) {
                    return false;
                }
                return true;
            } else if (this.mValidPackages.size() <= 0 || this.mValidPackages.contains(pkg)) {
                return true;
            } else {
                return false;
            }
        }

        public void dump() {
            if (this.mValidPackages.size() > 0) {
                Iterator<String> it = this.mValidPackages.iterator();
                while (it.hasNext()) {
                    Logger logger = Logger.out;
                    logger.println(":AllowPackage: " + it.next());
                }
            }
            if (this.mInvalidPackages.size() > 0) {
                Iterator<String> it2 = this.mInvalidPackages.iterator();
                while (it2.hasNext()) {
                    Logger logger2 = Logger.out;
                    logger2.println(":DisallowPackage: " + it2.next());
                }
            }
        }
    }
}
