package android.icu.util;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.Pair;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.Sink;
import android.icu.impl.UResource.Table;
import android.icu.impl.UResource.Value;
import android.icu.impl.locale.LanguageTag;
import android.icu.text.UnicodeSet;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MeasureUnit implements Serializable {
    public static final MeasureUnit ACRE = internalGetInstance("area", "acre");
    public static final MeasureUnit ACRE_FOOT = internalGetInstance("volume", "acre-foot");
    public static final MeasureUnit AMPERE = internalGetInstance("electric", "ampere");
    public static final MeasureUnit ARC_MINUTE = internalGetInstance("angle", "arc-minute");
    public static final MeasureUnit ARC_SECOND = internalGetInstance("angle", "arc-second");
    static final UnicodeSet ASCII = new UnicodeSet(97, 122).freeze();
    static final UnicodeSet ASCII_HYPHEN_DIGITS = new UnicodeSet(45, 45, 48, 57, 97, 122).freeze();
    public static final MeasureUnit ASTRONOMICAL_UNIT = internalGetInstance("length", "astronomical-unit");
    public static final MeasureUnit BIT = internalGetInstance("digital", "bit");
    public static final MeasureUnit BUSHEL = internalGetInstance("volume", "bushel");
    public static final MeasureUnit BYTE = internalGetInstance("digital", "byte");
    public static final MeasureUnit CALORIE = internalGetInstance("energy", "calorie");
    public static final MeasureUnit CARAT = internalGetInstance("mass", "carat");
    public static final MeasureUnit CELSIUS = internalGetInstance("temperature", "celsius");
    public static final MeasureUnit CENTILITER = internalGetInstance("volume", "centiliter");
    public static final MeasureUnit CENTIMETER = internalGetInstance("length", "centimeter");
    public static final MeasureUnit CENTURY = internalGetInstance("duration", "century");
    public static final MeasureUnit CUBIC_CENTIMETER = internalGetInstance("volume", "cubic-centimeter");
    public static final MeasureUnit CUBIC_FOOT = internalGetInstance("volume", "cubic-foot");
    public static final MeasureUnit CUBIC_INCH = internalGetInstance("volume", "cubic-inch");
    public static final MeasureUnit CUBIC_KILOMETER = internalGetInstance("volume", "cubic-kilometer");
    public static final MeasureUnit CUBIC_METER = internalGetInstance("volume", "cubic-meter");
    public static final MeasureUnit CUBIC_MILE = internalGetInstance("volume", "cubic-mile");
    public static final MeasureUnit CUBIC_YARD = internalGetInstance("volume", "cubic-yard");
    public static final MeasureUnit CUP = internalGetInstance("volume", "cup");
    public static final MeasureUnit CUP_METRIC = internalGetInstance("volume", "cup-metric");
    static Factory CURRENCY_FACTORY = new Factory() {
        public MeasureUnit create(String unusedType, String subType) {
            return new Currency(subType);
        }
    };
    public static final TimeUnit DAY = ((TimeUnit) internalGetInstance("duration", "day"));
    public static final MeasureUnit DECILITER = internalGetInstance("volume", "deciliter");
    public static final MeasureUnit DECIMETER = internalGetInstance("length", "decimeter");
    public static final MeasureUnit DEGREE = internalGetInstance("angle", "degree");
    public static final MeasureUnit EAST = internalGetInstance("coordinate", "east");
    public static final MeasureUnit FAHRENHEIT = internalGetInstance("temperature", "fahrenheit");
    public static final MeasureUnit FATHOM = internalGetInstance("length", "fathom");
    public static final MeasureUnit FLUID_OUNCE = internalGetInstance("volume", "fluid-ounce");
    public static final MeasureUnit FOODCALORIE = internalGetInstance("energy", "foodcalorie");
    public static final MeasureUnit FOOT = internalGetInstance("length", "foot");
    public static final MeasureUnit FURLONG = internalGetInstance("length", "furlong");
    public static final MeasureUnit GALLON = internalGetInstance("volume", "gallon");
    public static final MeasureUnit GALLON_IMPERIAL = internalGetInstance("volume", "gallon-imperial");
    public static final MeasureUnit GENERIC_TEMPERATURE = internalGetInstance("temperature", "generic");
    public static final MeasureUnit GIGABIT = internalGetInstance("digital", "gigabit");
    public static final MeasureUnit GIGABYTE = internalGetInstance("digital", "gigabyte");
    public static final MeasureUnit GIGAHERTZ = internalGetInstance("frequency", "gigahertz");
    public static final MeasureUnit GIGAWATT = internalGetInstance("power", "gigawatt");
    public static final MeasureUnit GRAM = internalGetInstance("mass", "gram");
    public static final MeasureUnit G_FORCE = internalGetInstance("acceleration", "g-force");
    public static final MeasureUnit HECTARE = internalGetInstance("area", "hectare");
    public static final MeasureUnit HECTOLITER = internalGetInstance("volume", "hectoliter");
    public static final MeasureUnit HECTOPASCAL = internalGetInstance("pressure", "hectopascal");
    public static final MeasureUnit HERTZ = internalGetInstance("frequency", "hertz");
    public static final MeasureUnit HORSEPOWER = internalGetInstance("power", "horsepower");
    public static final TimeUnit HOUR = ((TimeUnit) internalGetInstance("duration", "hour"));
    public static final MeasureUnit INCH = internalGetInstance("length", "inch");
    public static final MeasureUnit INCH_HG = internalGetInstance("pressure", "inch-hg");
    public static final MeasureUnit JOULE = internalGetInstance("energy", "joule");
    public static final MeasureUnit KARAT = internalGetInstance("concentr", "karat");
    public static final MeasureUnit KELVIN = internalGetInstance("temperature", "kelvin");
    public static final MeasureUnit KILOBIT = internalGetInstance("digital", "kilobit");
    public static final MeasureUnit KILOBYTE = internalGetInstance("digital", "kilobyte");
    public static final MeasureUnit KILOCALORIE = internalGetInstance("energy", "kilocalorie");
    public static final MeasureUnit KILOGRAM = internalGetInstance("mass", "kilogram");
    public static final MeasureUnit KILOHERTZ = internalGetInstance("frequency", "kilohertz");
    public static final MeasureUnit KILOJOULE = internalGetInstance("energy", "kilojoule");
    public static final MeasureUnit KILOMETER = internalGetInstance("length", "kilometer");
    public static final MeasureUnit KILOMETER_PER_HOUR = internalGetInstance("speed", "kilometer-per-hour");
    public static final MeasureUnit KILOWATT = internalGetInstance("power", "kilowatt");
    public static final MeasureUnit KILOWATT_HOUR = internalGetInstance("energy", "kilowatt-hour");
    public static final MeasureUnit KNOT = internalGetInstance("speed", "knot");
    public static final MeasureUnit LIGHT_YEAR = internalGetInstance("length", "light-year");
    public static final MeasureUnit LITER = internalGetInstance("volume", "liter");
    public static final MeasureUnit LITER_PER_100KILOMETERS = internalGetInstance("consumption", "liter-per-100kilometers");
    public static final MeasureUnit LITER_PER_KILOMETER = internalGetInstance("consumption", "liter-per-kilometer");
    public static final MeasureUnit LUX = internalGetInstance("light", "lux");
    public static final MeasureUnit MEGABIT = internalGetInstance("digital", "megabit");
    public static final MeasureUnit MEGABYTE = internalGetInstance("digital", "megabyte");
    public static final MeasureUnit MEGAHERTZ = internalGetInstance("frequency", "megahertz");
    public static final MeasureUnit MEGALITER = internalGetInstance("volume", "megaliter");
    public static final MeasureUnit MEGAWATT = internalGetInstance("power", "megawatt");
    public static final MeasureUnit METER = internalGetInstance("length", "meter");
    public static final MeasureUnit METER_PER_SECOND = internalGetInstance("speed", "meter-per-second");
    public static final MeasureUnit METER_PER_SECOND_SQUARED = internalGetInstance("acceleration", "meter-per-second-squared");
    public static final MeasureUnit METRIC_TON = internalGetInstance("mass", "metric-ton");
    public static final MeasureUnit MICROGRAM = internalGetInstance("mass", "microgram");
    public static final MeasureUnit MICROMETER = internalGetInstance("length", "micrometer");
    public static final MeasureUnit MICROSECOND = internalGetInstance("duration", "microsecond");
    public static final MeasureUnit MILE = internalGetInstance("length", "mile");
    public static final MeasureUnit MILE_PER_GALLON = internalGetInstance("consumption", "mile-per-gallon");
    public static final MeasureUnit MILE_PER_GALLON_IMPERIAL = internalGetInstance("consumption", "mile-per-gallon-imperial");
    public static final MeasureUnit MILE_PER_HOUR = internalGetInstance("speed", "mile-per-hour");
    public static final MeasureUnit MILE_SCANDINAVIAN = internalGetInstance("length", "mile-scandinavian");
    public static final MeasureUnit MILLIAMPERE = internalGetInstance("electric", "milliampere");
    public static final MeasureUnit MILLIBAR = internalGetInstance("pressure", "millibar");
    public static final MeasureUnit MILLIGRAM = internalGetInstance("mass", "milligram");
    public static final MeasureUnit MILLIGRAM_PER_DECILITER = internalGetInstance("concentr", "milligram-per-deciliter");
    public static final MeasureUnit MILLILITER = internalGetInstance("volume", "milliliter");
    public static final MeasureUnit MILLIMETER = internalGetInstance("length", "millimeter");
    public static final MeasureUnit MILLIMETER_OF_MERCURY = internalGetInstance("pressure", "millimeter-of-mercury");
    public static final MeasureUnit MILLIMOLE_PER_LITER = internalGetInstance("concentr", "millimole-per-liter");
    public static final MeasureUnit MILLISECOND = internalGetInstance("duration", "millisecond");
    public static final MeasureUnit MILLIWATT = internalGetInstance("power", "milliwatt");
    public static final TimeUnit MINUTE = ((TimeUnit) internalGetInstance("duration", "minute"));
    public static final TimeUnit MONTH = ((TimeUnit) internalGetInstance("duration", "month"));
    public static final MeasureUnit NANOMETER = internalGetInstance("length", "nanometer");
    public static final MeasureUnit NANOSECOND = internalGetInstance("duration", "nanosecond");
    public static final MeasureUnit NAUTICAL_MILE = internalGetInstance("length", "nautical-mile");
    public static final MeasureUnit NORTH = internalGetInstance("coordinate", "north");
    public static final MeasureUnit OHM = internalGetInstance("electric", "ohm");
    public static final MeasureUnit OUNCE = internalGetInstance("mass", "ounce");
    public static final MeasureUnit OUNCE_TROY = internalGetInstance("mass", "ounce-troy");
    public static final MeasureUnit PARSEC = internalGetInstance("length", "parsec");
    public static final MeasureUnit PART_PER_MILLION = internalGetInstance("concentr", "part-per-million");
    public static final MeasureUnit PICOMETER = internalGetInstance("length", "picometer");
    public static final MeasureUnit PINT = internalGetInstance("volume", "pint");
    public static final MeasureUnit PINT_METRIC = internalGetInstance("volume", "pint-metric");
    public static final MeasureUnit POUND = internalGetInstance("mass", "pound");
    public static final MeasureUnit POUND_PER_SQUARE_INCH = internalGetInstance("pressure", "pound-per-square-inch");
    public static final MeasureUnit QUART = internalGetInstance("volume", "quart");
    public static final MeasureUnit RADIAN = internalGetInstance("angle", "radian");
    public static final MeasureUnit REVOLUTION_ANGLE = internalGetInstance("angle", "revolution");
    public static final TimeUnit SECOND = ((TimeUnit) internalGetInstance("duration", "second"));
    public static final MeasureUnit SOUTH = internalGetInstance("coordinate", "south");
    public static final MeasureUnit SQUARE_CENTIMETER = internalGetInstance("area", "square-centimeter");
    public static final MeasureUnit SQUARE_FOOT = internalGetInstance("area", "square-foot");
    public static final MeasureUnit SQUARE_INCH = internalGetInstance("area", "square-inch");
    public static final MeasureUnit SQUARE_KILOMETER = internalGetInstance("area", "square-kilometer");
    public static final MeasureUnit SQUARE_METER = internalGetInstance("area", "square-meter");
    public static final MeasureUnit SQUARE_MILE = internalGetInstance("area", "square-mile");
    public static final MeasureUnit SQUARE_YARD = internalGetInstance("area", "square-yard");
    public static final MeasureUnit STONE = internalGetInstance("mass", "stone");
    public static final MeasureUnit TABLESPOON = internalGetInstance("volume", "tablespoon");
    public static final MeasureUnit TEASPOON = internalGetInstance("volume", "teaspoon");
    public static final MeasureUnit TERABIT = internalGetInstance("digital", "terabit");
    public static final MeasureUnit TERABYTE = internalGetInstance("digital", "terabyte");
    static Factory TIMEUNIT_FACTORY = new Factory() {
        public MeasureUnit create(String type, String subType) {
            return new TimeUnit(type, subType);
        }
    };
    public static final MeasureUnit TON = internalGetInstance("mass", "ton");
    private static Factory UNIT_FACTORY = new Factory() {
        public MeasureUnit create(String type, String subType) {
            return new MeasureUnit(type, subType);
        }
    };
    public static final MeasureUnit VOLT = internalGetInstance("electric", "volt");
    public static final MeasureUnit WATT = internalGetInstance("power", "watt");
    public static final TimeUnit WEEK = ((TimeUnit) internalGetInstance("duration", "week"));
    public static final MeasureUnit WEST = internalGetInstance("coordinate", "west");
    public static final MeasureUnit YARD = internalGetInstance("length", "yard");
    public static final TimeUnit YEAR = ((TimeUnit) internalGetInstance("duration", "year"));
    private static final Map<String, Map<String, MeasureUnit>> cache = new HashMap();
    private static boolean cacheIsPopulated = false;
    private static final long serialVersionUID = -1839973855554750484L;
    private static HashMap<Pair<MeasureUnit, MeasureUnit>, MeasureUnit> unitPerUnitToSingleUnit = new HashMap();
    @Deprecated
    protected final String subType;
    @Deprecated
    protected final String type;

    @Deprecated
    protected interface Factory {
        @Deprecated
        MeasureUnit create(String str, String str2);
    }

    private static final class CurrencyNumericCodeSink extends Sink {
        /* synthetic */ CurrencyNumericCodeSink(CurrencyNumericCodeSink -this0) {
            this();
        }

        private CurrencyNumericCodeSink() {
        }

        public void put(Key key, Value value, boolean noFallback) {
            Table codesTable = value.getTable();
            for (int i1 = 0; codesTable.getKeyAndValue(i1, key, value); i1++) {
                MeasureUnit.internalGetInstance("currency", key.toString());
            }
        }
    }

    static final class MeasureUnitProxy implements Externalizable {
        private static final long serialVersionUID = -3910681415330989598L;
        private String subType;
        private String type;

        public MeasureUnitProxy(String type, String subType) {
            this.type = type;
            this.subType = subType;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeByte(0);
            out.writeUTF(this.type);
            out.writeUTF(this.subType);
            out.writeShort(0);
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            in.readByte();
            this.type = in.readUTF();
            this.subType = in.readUTF();
            int extra = in.readShort();
            if (extra > 0) {
                in.read(new byte[extra], 0, extra);
            }
        }

        private Object readResolve() throws ObjectStreamException {
            return MeasureUnit.internalGetInstance(this.type, this.subType);
        }
    }

    private static final class MeasureUnitSink extends Sink {
        /* synthetic */ MeasureUnitSink(MeasureUnitSink -this0) {
            this();
        }

        private MeasureUnitSink() {
        }

        public void put(Key key, Value value, boolean noFallback) {
            Table unitTypesTable = value.getTable();
            for (int i2 = 0; unitTypesTable.getKeyAndValue(i2, key, value); i2++) {
                if (!key.contentEquals("compound")) {
                    String unitType = key.toString();
                    Table unitNamesTable = value.getTable();
                    for (int i3 = 0; unitNamesTable.getKeyAndValue(i3, key, value); i3++) {
                        MeasureUnit.internalGetInstance(unitType, key.toString());
                    }
                }
            }
        }
    }

    static {
        unitPerUnitToSingleUnit.put(Pair.of(LITER, KILOMETER), LITER_PER_KILOMETER);
        unitPerUnitToSingleUnit.put(Pair.of(POUND, SQUARE_INCH), POUND_PER_SQUARE_INCH);
        unitPerUnitToSingleUnit.put(Pair.of(MILE, HOUR), MILE_PER_HOUR);
        unitPerUnitToSingleUnit.put(Pair.of(MILLIGRAM, DECILITER), MILLIGRAM_PER_DECILITER);
        unitPerUnitToSingleUnit.put(Pair.of(MILE, GALLON_IMPERIAL), MILE_PER_GALLON_IMPERIAL);
        unitPerUnitToSingleUnit.put(Pair.of(KILOMETER, HOUR), KILOMETER_PER_HOUR);
        unitPerUnitToSingleUnit.put(Pair.of(MILE, GALLON), MILE_PER_GALLON);
        unitPerUnitToSingleUnit.put(Pair.of(METER, SECOND), METER_PER_SECOND);
    }

    @Deprecated
    protected MeasureUnit(String type, String subType) {
        this.type = type;
        this.subType = subType;
    }

    public String getType() {
        return this.type;
    }

    public String getSubtype() {
        return this.subType;
    }

    public int hashCode() {
        return (this.type.hashCode() * 31) + this.subType.hashCode();
    }

    public boolean equals(Object rhs) {
        boolean z = false;
        if (rhs == this) {
            return true;
        }
        if (!(rhs instanceof MeasureUnit)) {
            return false;
        }
        MeasureUnit c = (MeasureUnit) rhs;
        if (this.type.equals(c.type)) {
            z = this.subType.equals(c.subType);
        }
        return z;
    }

    public String toString() {
        return this.type + LanguageTag.SEP + this.subType;
    }

    public static synchronized Set<String> getAvailableTypes() {
        Set<String> unmodifiableSet;
        synchronized (MeasureUnit.class) {
            populateCache();
            unmodifiableSet = Collections.unmodifiableSet(cache.keySet());
        }
        return unmodifiableSet;
    }

    public static synchronized Set<MeasureUnit> getAvailable(String type) {
        Set<MeasureUnit> emptySet;
        synchronized (MeasureUnit.class) {
            populateCache();
            Map<String, MeasureUnit> units = (Map) cache.get(type);
            if (units == null) {
                emptySet = Collections.emptySet();
            } else {
                emptySet = Collections.unmodifiableSet(new HashSet(units.values()));
            }
        }
        return emptySet;
    }

    public static synchronized Set<MeasureUnit> getAvailable() {
        Set<MeasureUnit> unmodifiableSet;
        synchronized (MeasureUnit.class) {
            Set<MeasureUnit> result = new HashSet();
            for (String type : new HashSet(getAvailableTypes())) {
                for (MeasureUnit unit : getAvailable(type)) {
                    result.add(unit);
                }
            }
            unmodifiableSet = Collections.unmodifiableSet(result);
        }
        return unmodifiableSet;
    }

    @Deprecated
    public static MeasureUnit internalGetInstance(String type, String subType) {
        if (type == null || subType == null) {
            throw new NullPointerException("Type and subType must be non-null");
        } else if ("currency".equals(type) || (ASCII.containsAll(type) && (ASCII_HYPHEN_DIGITS.containsAll(subType) ^ 1) == 0)) {
            Factory factory;
            if ("currency".equals(type)) {
                factory = CURRENCY_FACTORY;
            } else if ("duration".equals(type)) {
                factory = TIMEUNIT_FACTORY;
            } else {
                factory = UNIT_FACTORY;
            }
            return addUnit(type, subType, factory);
        } else {
            throw new IllegalArgumentException("The type or subType are invalid.");
        }
    }

    @Deprecated
    public static MeasureUnit resolveUnitPerUnit(MeasureUnit unit, MeasureUnit perUnit) {
        return (MeasureUnit) unitPerUnitToSingleUnit.get(Pair.of(unit, perUnit));
    }

    private static void populateCache() {
        if (!cacheIsPopulated) {
            cacheIsPopulated = true;
            ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_UNIT_BASE_NAME, "en")).getAllItemsWithFallback("units", new MeasureUnitSink());
            ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "currencyNumericCodes", ICUResourceBundle.ICU_DATA_CLASS_LOADER)).getAllItemsWithFallback("codeMap", new CurrencyNumericCodeSink());
        }
    }

    @Deprecated
    protected static synchronized MeasureUnit addUnit(String type, String unitName, Factory factory) {
        MeasureUnit unit;
        synchronized (MeasureUnit.class) {
            Map<String, MeasureUnit> tmp = (Map) cache.get(type);
            if (tmp == null) {
                Map map = cache;
                tmp = new HashMap();
                map.put(type, tmp);
            } else {
                type = ((MeasureUnit) ((Entry) tmp.entrySet().iterator().next()).getValue()).type;
            }
            unit = (MeasureUnit) tmp.get(unitName);
            if (unit == null) {
                unit = factory.create(type, unitName);
                tmp.put(unitName, unit);
            }
        }
        return unit;
    }

    private Object writeReplace() throws ObjectStreamException {
        return new MeasureUnitProxy(this.type, this.subType);
    }
}
