package ohos.global.icu.impl.duration.impl;

import java.util.ArrayList;

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
        public static final String[] names = {"M", "F", "N"};
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

    public static DataRecord read(String str, RecordReader recordReader) {
        if (recordReader.open("DataRecord")) {
            DataRecord dataRecord = new DataRecord();
            dataRecord.pl = recordReader.namedIndex("pl", EPluralization.names);
            dataRecord.pluralNames = recordReader.stringTable("pluralName");
            dataRecord.genders = recordReader.namedIndexArray("gender", EGender.names);
            dataRecord.singularNames = recordReader.stringArray("singularName");
            dataRecord.halfNames = recordReader.stringArray("halfName");
            dataRecord.numberNames = recordReader.stringArray("numberName");
            dataRecord.mediumNames = recordReader.stringArray("mediumName");
            dataRecord.shortNames = recordReader.stringArray("shortName");
            dataRecord.measures = recordReader.stringArray("measure");
            dataRecord.rqdSuffixes = recordReader.stringArray("rqdSuffix");
            dataRecord.optSuffixes = recordReader.stringArray("optSuffix");
            dataRecord.halves = recordReader.stringArray("halves");
            dataRecord.halfPlacements = recordReader.namedIndexArray("halfPlacement", EHalfPlacement.names);
            dataRecord.halfSupport = recordReader.namedIndexArray("halfSupport", EHalfSupport.names);
            dataRecord.fifteenMinutes = recordReader.string("fifteenMinutes");
            dataRecord.fiveMinutes = recordReader.string("fiveMinutes");
            dataRecord.requiresDigitSeparator = recordReader.bool("requiresDigitSeparator");
            dataRecord.digitPrefix = recordReader.string("digitPrefix");
            dataRecord.countSep = recordReader.string("countSep");
            dataRecord.shortUnitSep = recordReader.string("shortUnitSep");
            dataRecord.unitSep = recordReader.stringArray("unitSep");
            dataRecord.unitSepRequiresDP = recordReader.boolArray("unitSepRequiresDP");
            dataRecord.requiresSkipMarker = recordReader.boolArray("requiresSkipMarker");
            dataRecord.numberSystem = recordReader.namedIndex("numberSystem", ENumberSystem.names);
            dataRecord.zero = recordReader.character("zero");
            dataRecord.decimalSep = recordReader.character("decimalSep");
            dataRecord.omitSingularCount = recordReader.bool("omitSingularCount");
            dataRecord.omitDualCount = recordReader.bool("omitDualCount");
            dataRecord.zeroHandling = recordReader.namedIndex("zeroHandling", EZeroHandling.names);
            dataRecord.decimalHandling = recordReader.namedIndex("decimalHandling", EDecimalHandling.names);
            dataRecord.fractionHandling = recordReader.namedIndex("fractionHandling", EFractionHandling.names);
            dataRecord.skippedUnitMarker = recordReader.string("skippedUnitMarker");
            dataRecord.allowZero = recordReader.bool("allowZero");
            dataRecord.weeksAloneOnly = recordReader.bool("weeksAloneOnly");
            dataRecord.useMilliseconds = recordReader.namedIndex("useMilliseconds", EMilliSupport.names);
            if (recordReader.open("ScopeDataList")) {
                ArrayList arrayList = new ArrayList();
                while (true) {
                    ScopeData read = ScopeData.read(recordReader);
                    if (read == null) {
                        break;
                    }
                    arrayList.add(read);
                }
                if (recordReader.close()) {
                    dataRecord.scopeData = (ScopeData[]) arrayList.toArray(new ScopeData[arrayList.size()]);
                }
            }
            if (recordReader.close()) {
                return dataRecord;
            }
            throw new InternalError("null data read while reading " + str);
        }
        throw new InternalError("did not find DataRecord while reading " + str);
    }

    public void write(RecordWriter recordWriter) {
        recordWriter.open("DataRecord");
        recordWriter.namedIndex("pl", EPluralization.names, this.pl);
        recordWriter.stringTable("pluralName", this.pluralNames);
        recordWriter.namedIndexArray("gender", EGender.names, this.genders);
        recordWriter.stringArray("singularName", this.singularNames);
        recordWriter.stringArray("halfName", this.halfNames);
        recordWriter.stringArray("numberName", this.numberNames);
        recordWriter.stringArray("mediumName", this.mediumNames);
        recordWriter.stringArray("shortName", this.shortNames);
        recordWriter.stringArray("measure", this.measures);
        recordWriter.stringArray("rqdSuffix", this.rqdSuffixes);
        recordWriter.stringArray("optSuffix", this.optSuffixes);
        recordWriter.stringArray("halves", this.halves);
        recordWriter.namedIndexArray("halfPlacement", EHalfPlacement.names, this.halfPlacements);
        recordWriter.namedIndexArray("halfSupport", EHalfSupport.names, this.halfSupport);
        recordWriter.string("fifteenMinutes", this.fifteenMinutes);
        recordWriter.string("fiveMinutes", this.fiveMinutes);
        recordWriter.bool("requiresDigitSeparator", this.requiresDigitSeparator);
        recordWriter.string("digitPrefix", this.digitPrefix);
        recordWriter.string("countSep", this.countSep);
        recordWriter.string("shortUnitSep", this.shortUnitSep);
        recordWriter.stringArray("unitSep", this.unitSep);
        recordWriter.boolArray("unitSepRequiresDP", this.unitSepRequiresDP);
        recordWriter.boolArray("requiresSkipMarker", this.requiresSkipMarker);
        recordWriter.namedIndex("numberSystem", ENumberSystem.names, this.numberSystem);
        recordWriter.character("zero", this.zero);
        recordWriter.character("decimalSep", this.decimalSep);
        recordWriter.bool("omitSingularCount", this.omitSingularCount);
        recordWriter.bool("omitDualCount", this.omitDualCount);
        recordWriter.namedIndex("zeroHandling", EZeroHandling.names, this.zeroHandling);
        recordWriter.namedIndex("decimalHandling", EDecimalHandling.names, this.decimalHandling);
        recordWriter.namedIndex("fractionHandling", EFractionHandling.names, this.fractionHandling);
        recordWriter.string("skippedUnitMarker", this.skippedUnitMarker);
        recordWriter.bool("allowZero", this.allowZero);
        recordWriter.bool("weeksAloneOnly", this.weeksAloneOnly);
        recordWriter.namedIndex("useMilliseconds", EMilliSupport.names, this.useMilliseconds);
        if (this.scopeData != null) {
            recordWriter.open("ScopeDataList");
            int i = 0;
            while (true) {
                ScopeData[] scopeDataArr = this.scopeData;
                if (i >= scopeDataArr.length) {
                    break;
                }
                scopeDataArr[i].write(recordWriter);
                i++;
            }
            recordWriter.close();
        }
        recordWriter.close();
    }

    public static class ScopeData {
        String prefix;
        boolean requiresDigitPrefix;
        String suffix;

        public void write(RecordWriter recordWriter) {
            recordWriter.open("ScopeData");
            recordWriter.string("prefix", this.prefix);
            recordWriter.bool("requiresDigitPrefix", this.requiresDigitPrefix);
            recordWriter.string("suffix", this.suffix);
            recordWriter.close();
        }

        public static ScopeData read(RecordReader recordReader) {
            if (!recordReader.open("ScopeData")) {
                return null;
            }
            ScopeData scopeData = new ScopeData();
            scopeData.prefix = recordReader.string("prefix");
            scopeData.requiresDigitPrefix = recordReader.bool("requiresDigitPrefix");
            scopeData.suffix = recordReader.string("suffix");
            if (recordReader.close()) {
                return scopeData;
            }
            return null;
        }
    }
}
