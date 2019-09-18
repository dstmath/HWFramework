package android.icu.impl.duration.impl;

import android.icu.text.DateFormat;
import android.icu.text.PluralRules;
import java.util.ArrayList;
import java.util.List;

public class DataRecord {
    boolean allowZero;
    String countSep;
    byte decimalHandling;
    char decimalSep;
    String digitPrefix;
    String fifteenMinutes;
    String fiveMinutes;
    byte fractionHandling;
    byte[] genders;
    String[] halfNames;
    byte[] halfPlacements;
    byte[] halfSupport;
    String[] halves;
    String[] measures;
    String[] mediumNames;
    String[] numberNames;
    byte numberSystem;
    boolean omitDualCount;
    boolean omitSingularCount;
    String[] optSuffixes;
    byte pl;
    String[][] pluralNames;
    boolean requiresDigitSeparator;
    boolean[] requiresSkipMarker;
    String[] rqdSuffixes;
    ScopeData[] scopeData;
    String[] shortNames;
    String shortUnitSep;
    String[] singularNames;
    String skippedUnitMarker;
    String[] unitSep;
    boolean[] unitSepRequiresDP;
    byte useMilliseconds;
    boolean weeksAloneOnly;
    char zero;
    byte zeroHandling;

    public interface ECountVariant {
        public static final byte DECIMAL1 = 3;
        public static final byte DECIMAL2 = 4;
        public static final byte DECIMAL3 = 5;
        public static final byte HALF_FRACTION = 2;
        public static final byte INTEGER = 0;
        public static final byte INTEGER_CUSTOM = 1;
        public static final String[] names = {"INTEGER", "INTEGER_CUSTOM", "HALF_FRACTION", "DECIMAL1", "DECIMAL2", "DECIMAL3"};
    }

    public interface EDecimalHandling {
        public static final byte DPAUCAL = 3;
        public static final byte DPLURAL = 0;
        public static final byte DSINGULAR = 1;
        public static final byte DSINGULAR_SUBONE = 2;
        public static final String[] names = {"DPLURAL", "DSINGULAR", "DSINGULAR_SUBONE", "DPAUCAL"};
    }

    public interface EFractionHandling {
        public static final byte FPAUCAL = 3;
        public static final byte FPLURAL = 0;
        public static final byte FSINGULAR_PLURAL = 1;
        public static final byte FSINGULAR_PLURAL_ANDAHALF = 2;
        public static final String[] names = {"FPLURAL", "FSINGULAR_PLURAL", "FSINGULAR_PLURAL_ANDAHALF", "FPAUCAL"};
    }

    public interface EGender {
        public static final byte F = 1;
        public static final byte M = 0;
        public static final byte N = 2;
        public static final String[] names = {DateFormat.NUM_MONTH, "F", "N"};
    }

    public interface EHalfPlacement {
        public static final byte AFTER_FIRST = 1;
        public static final byte LAST = 2;
        public static final byte PREFIX = 0;
        public static final String[] names = {"PREFIX", "AFTER_FIRST", "LAST"};
    }

    public interface EHalfSupport {
        public static final byte NO = 1;
        public static final byte ONE_PLUS = 2;
        public static final byte YES = 0;
        public static final String[] names = {"YES", "NO", "ONE_PLUS"};
    }

    public interface EMilliSupport {
        public static final byte NO = 1;
        public static final byte WITH_SECONDS = 2;
        public static final byte YES = 0;
        public static final String[] names = {"YES", "NO", "WITH_SECONDS"};
    }

    public interface ENumberSystem {
        public static final byte CHINESE_SIMPLIFIED = 2;
        public static final byte CHINESE_TRADITIONAL = 1;
        public static final byte DEFAULT = 0;
        public static final byte KOREAN = 3;
        public static final String[] names = {"DEFAULT", "CHINESE_TRADITIONAL", "CHINESE_SIMPLIFIED", "KOREAN"};
    }

    public interface EPluralization {
        public static final byte ARABIC = 5;
        public static final byte DUAL = 2;
        public static final byte HEBREW = 4;
        public static final byte NONE = 0;
        public static final byte PAUCAL = 3;
        public static final byte PLURAL = 1;
        public static final String[] names = {"NONE", "PLURAL", "DUAL", "PAUCAL", "HEBREW", "ARABIC"};
    }

    public interface ESeparatorVariant {
        public static final byte FULL = 2;
        public static final byte NONE = 0;
        public static final byte SHORT = 1;
        public static final String[] names = {"NONE", "SHORT", "FULL"};
    }

    public interface ETimeDirection {
        public static final byte FUTURE = 2;
        public static final byte NODIRECTION = 0;
        public static final byte PAST = 1;
        public static final String[] names = {"NODIRECTION", "PAST", "FUTURE"};
    }

    public interface ETimeLimit {
        public static final byte LT = 1;
        public static final byte MT = 2;
        public static final byte NOLIMIT = 0;
        public static final String[] names = {"NOLIMIT", "LT", "MT"};
    }

    public interface EUnitVariant {
        public static final byte MEDIUM = 1;
        public static final byte PLURALIZED = 0;
        public static final byte SHORT = 2;
        public static final String[] names = {"PLURALIZED", "MEDIUM", "SHORT"};
    }

    public interface EZeroHandling {
        public static final byte ZPLURAL = 0;
        public static final byte ZSINGULAR = 1;
        public static final String[] names = {"ZPLURAL", "ZSINGULAR"};
    }

    public static class ScopeData {
        String prefix;
        boolean requiresDigitPrefix;
        String suffix;

        public void write(RecordWriter out) {
            out.open("ScopeData");
            out.string("prefix", this.prefix);
            out.bool("requiresDigitPrefix", this.requiresDigitPrefix);
            out.string("suffix", this.suffix);
            out.close();
        }

        public static ScopeData read(RecordReader in) {
            if (in.open("ScopeData")) {
                ScopeData scope = new ScopeData();
                scope.prefix = in.string("prefix");
                scope.requiresDigitPrefix = in.bool("requiresDigitPrefix");
                scope.suffix = in.string("suffix");
                if (in.close()) {
                    return scope;
                }
            }
            return null;
        }
    }

    public static DataRecord read(String ln, RecordReader in) {
        if (in.open("DataRecord")) {
            DataRecord record = new DataRecord();
            record.pl = in.namedIndex("pl", EPluralization.names);
            record.pluralNames = in.stringTable("pluralName");
            record.genders = in.namedIndexArray("gender", EGender.names);
            record.singularNames = in.stringArray("singularName");
            record.halfNames = in.stringArray("halfName");
            record.numberNames = in.stringArray("numberName");
            record.mediumNames = in.stringArray("mediumName");
            record.shortNames = in.stringArray("shortName");
            record.measures = in.stringArray("measure");
            record.rqdSuffixes = in.stringArray("rqdSuffix");
            record.optSuffixes = in.stringArray("optSuffix");
            record.halves = in.stringArray("halves");
            record.halfPlacements = in.namedIndexArray("halfPlacement", EHalfPlacement.names);
            record.halfSupport = in.namedIndexArray("halfSupport", EHalfSupport.names);
            record.fifteenMinutes = in.string("fifteenMinutes");
            record.fiveMinutes = in.string("fiveMinutes");
            record.requiresDigitSeparator = in.bool("requiresDigitSeparator");
            record.digitPrefix = in.string("digitPrefix");
            record.countSep = in.string("countSep");
            record.shortUnitSep = in.string("shortUnitSep");
            record.unitSep = in.stringArray("unitSep");
            record.unitSepRequiresDP = in.boolArray("unitSepRequiresDP");
            record.requiresSkipMarker = in.boolArray("requiresSkipMarker");
            record.numberSystem = in.namedIndex("numberSystem", ENumberSystem.names);
            record.zero = in.character(PluralRules.KEYWORD_ZERO);
            record.decimalSep = in.character("decimalSep");
            record.omitSingularCount = in.bool("omitSingularCount");
            record.omitDualCount = in.bool("omitDualCount");
            record.zeroHandling = in.namedIndex("zeroHandling", EZeroHandling.names);
            record.decimalHandling = in.namedIndex("decimalHandling", EDecimalHandling.names);
            record.fractionHandling = in.namedIndex("fractionHandling", EFractionHandling.names);
            record.skippedUnitMarker = in.string("skippedUnitMarker");
            record.allowZero = in.bool("allowZero");
            record.weeksAloneOnly = in.bool("weeksAloneOnly");
            record.useMilliseconds = in.namedIndex("useMilliseconds", EMilliSupport.names);
            if (in.open("ScopeDataList")) {
                List<ScopeData> list = new ArrayList<>();
                while (true) {
                    ScopeData read = ScopeData.read(in);
                    ScopeData data = read;
                    if (read == null) {
                        break;
                    }
                    list.add(data);
                }
                if (in.close()) {
                    record.scopeData = (ScopeData[]) list.toArray(new ScopeData[list.size()]);
                }
            }
            if (in.close()) {
                return record;
            }
            throw new InternalError("null data read while reading " + ln);
        }
        throw new InternalError("did not find DataRecord while reading " + ln);
    }

    public void write(RecordWriter out) {
        out.open("DataRecord");
        out.namedIndex("pl", EPluralization.names, this.pl);
        out.stringTable("pluralName", this.pluralNames);
        out.namedIndexArray("gender", EGender.names, this.genders);
        out.stringArray("singularName", this.singularNames);
        out.stringArray("halfName", this.halfNames);
        out.stringArray("numberName", this.numberNames);
        out.stringArray("mediumName", this.mediumNames);
        out.stringArray("shortName", this.shortNames);
        out.stringArray("measure", this.measures);
        out.stringArray("rqdSuffix", this.rqdSuffixes);
        out.stringArray("optSuffix", this.optSuffixes);
        out.stringArray("halves", this.halves);
        out.namedIndexArray("halfPlacement", EHalfPlacement.names, this.halfPlacements);
        out.namedIndexArray("halfSupport", EHalfSupport.names, this.halfSupport);
        out.string("fifteenMinutes", this.fifteenMinutes);
        out.string("fiveMinutes", this.fiveMinutes);
        out.bool("requiresDigitSeparator", this.requiresDigitSeparator);
        out.string("digitPrefix", this.digitPrefix);
        out.string("countSep", this.countSep);
        out.string("shortUnitSep", this.shortUnitSep);
        out.stringArray("unitSep", this.unitSep);
        out.boolArray("unitSepRequiresDP", this.unitSepRequiresDP);
        out.boolArray("requiresSkipMarker", this.requiresSkipMarker);
        out.namedIndex("numberSystem", ENumberSystem.names, this.numberSystem);
        out.character(PluralRules.KEYWORD_ZERO, this.zero);
        out.character("decimalSep", this.decimalSep);
        out.bool("omitSingularCount", this.omitSingularCount);
        out.bool("omitDualCount", this.omitDualCount);
        out.namedIndex("zeroHandling", EZeroHandling.names, this.zeroHandling);
        out.namedIndex("decimalHandling", EDecimalHandling.names, this.decimalHandling);
        out.namedIndex("fractionHandling", EFractionHandling.names, this.fractionHandling);
        out.string("skippedUnitMarker", this.skippedUnitMarker);
        out.bool("allowZero", this.allowZero);
        out.bool("weeksAloneOnly", this.weeksAloneOnly);
        out.namedIndex("useMilliseconds", EMilliSupport.names, this.useMilliseconds);
        if (this.scopeData != null) {
            out.open("ScopeDataList");
            for (ScopeData write : this.scopeData) {
                write.write(out);
            }
            out.close();
        }
        out.close();
    }
}
