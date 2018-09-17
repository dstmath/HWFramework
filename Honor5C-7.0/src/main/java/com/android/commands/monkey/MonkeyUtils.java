package com.android.commands.monkey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public abstract class MonkeyUtils {
    private static final Date DATE = null;
    private static final SimpleDateFormat DATE_FORMATTER = null;
    private static PackageFilter sFilter;

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
            } else if (this.mValidPackages.size() > 0 && !this.mValidPackages.contains(pkg)) {
                return false;
            }
            return true;
        }

        public void dump() {
            if (this.mValidPackages.size() > 0) {
                for (String str : this.mValidPackages) {
                    System.out.println(":AllowPackage: " + str);
                }
            }
            if (this.mInvalidPackages.size() > 0) {
                for (String str2 : this.mInvalidPackages) {
                    System.out.println(":DisallowPackage: " + str2);
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.commands.monkey.MonkeyUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.commands.monkey.MonkeyUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.commands.monkey.MonkeyUtils.<clinit>():void");
    }

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
}
