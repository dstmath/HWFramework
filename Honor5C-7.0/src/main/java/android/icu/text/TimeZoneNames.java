package android.icu.text;

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
    private static Cache TZNAMES_CACHE = null;
    private static final Factory TZNAMES_FACTORY = null;
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
        private Cache() {
        }

        protected TimeZoneNames createInstance(String key, ULocale data) {
            return TimeZoneNames.TZNAMES_FACTORY.getTimeZoneNames(data);
        }
    }

    private static class DefaultTimeZoneNames extends TimeZoneNames {
        public static final DefaultTimeZoneNames INSTANCE = null;
        private static final long serialVersionUID = -995672072494349071L;

        public static class FactoryImpl extends Factory {
            public TimeZoneNames getTimeZoneNames(ULocale locale) {
                return DefaultTimeZoneNames.INSTANCE;
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.TimeZoneNames.DefaultTimeZoneNames.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.TimeZoneNames.DefaultTimeZoneNames.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.TimeZoneNames.DefaultTimeZoneNames.<clinit>():void");
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
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.TimeZoneNames.NameType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.TimeZoneNames.NameType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.TimeZoneNames.NameType.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.TimeZoneNames.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.TimeZoneNames.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.TimeZoneNames.<clinit>():void");
    }

    public abstract Set<String> getAvailableMetaZoneIDs();

    public abstract Set<String> getAvailableMetaZoneIDs(String str);

    public abstract String getMetaZoneDisplayName(String str, NameType nameType);

    public abstract String getMetaZoneID(String str, long j);

    public abstract String getReferenceZoneID(String str, String str2);

    public abstract String getTimeZoneDisplayName(String str, NameType nameType);

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
