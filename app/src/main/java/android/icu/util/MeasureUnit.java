package android.icu.util;

import android.icu.impl.Pair;
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
    public static final MeasureUnit ACRE = null;
    public static final MeasureUnit ACRE_FOOT = null;
    public static final MeasureUnit AMPERE = null;
    public static final MeasureUnit ARC_MINUTE = null;
    public static final MeasureUnit ARC_SECOND = null;
    static final UnicodeSet ASCII = null;
    static final UnicodeSet ASCII_HYPHEN_DIGITS = null;
    public static final MeasureUnit ASTRONOMICAL_UNIT = null;
    public static final MeasureUnit BIT = null;
    public static final MeasureUnit BUSHEL = null;
    public static final MeasureUnit BYTE = null;
    public static final MeasureUnit CALORIE = null;
    public static final MeasureUnit CARAT = null;
    public static final MeasureUnit CELSIUS = null;
    public static final MeasureUnit CENTILITER = null;
    public static final MeasureUnit CENTIMETER = null;
    public static final MeasureUnit CENTURY = null;
    public static final MeasureUnit CUBIC_CENTIMETER = null;
    public static final MeasureUnit CUBIC_FOOT = null;
    public static final MeasureUnit CUBIC_INCH = null;
    public static final MeasureUnit CUBIC_KILOMETER = null;
    public static final MeasureUnit CUBIC_METER = null;
    public static final MeasureUnit CUBIC_MILE = null;
    public static final MeasureUnit CUBIC_YARD = null;
    public static final MeasureUnit CUP = null;
    public static final MeasureUnit CUP_METRIC = null;
    static Factory CURRENCY_FACTORY = null;
    public static final TimeUnit DAY = null;
    public static final MeasureUnit DECILITER = null;
    public static final MeasureUnit DECIMETER = null;
    public static final MeasureUnit DEGREE = null;
    public static final MeasureUnit FAHRENHEIT = null;
    public static final MeasureUnit FATHOM = null;
    public static final MeasureUnit FLUID_OUNCE = null;
    public static final MeasureUnit FOODCALORIE = null;
    public static final MeasureUnit FOOT = null;
    public static final MeasureUnit FURLONG = null;
    public static final MeasureUnit GALLON = null;
    public static final MeasureUnit GENERIC_TEMPERATURE = null;
    public static final MeasureUnit GIGABIT = null;
    public static final MeasureUnit GIGABYTE = null;
    public static final MeasureUnit GIGAHERTZ = null;
    public static final MeasureUnit GIGAWATT = null;
    public static final MeasureUnit GRAM = null;
    public static final MeasureUnit G_FORCE = null;
    public static final MeasureUnit HECTARE = null;
    public static final MeasureUnit HECTOLITER = null;
    public static final MeasureUnit HECTOPASCAL = null;
    public static final MeasureUnit HERTZ = null;
    public static final MeasureUnit HORSEPOWER = null;
    public static final TimeUnit HOUR = null;
    public static final MeasureUnit INCH = null;
    public static final MeasureUnit INCH_HG = null;
    public static final MeasureUnit JOULE = null;
    public static final MeasureUnit KARAT = null;
    public static final MeasureUnit KELVIN = null;
    public static final MeasureUnit KILOBIT = null;
    public static final MeasureUnit KILOBYTE = null;
    public static final MeasureUnit KILOCALORIE = null;
    public static final MeasureUnit KILOGRAM = null;
    public static final MeasureUnit KILOHERTZ = null;
    public static final MeasureUnit KILOJOULE = null;
    public static final MeasureUnit KILOMETER = null;
    public static final MeasureUnit KILOMETER_PER_HOUR = null;
    public static final MeasureUnit KILOWATT = null;
    public static final MeasureUnit KILOWATT_HOUR = null;
    public static final MeasureUnit KNOT = null;
    public static final MeasureUnit LIGHT_YEAR = null;
    public static final MeasureUnit LITER = null;
    public static final MeasureUnit LITER_PER_100KILOMETERS = null;
    public static final MeasureUnit LITER_PER_KILOMETER = null;
    public static final MeasureUnit LUX = null;
    public static final MeasureUnit MEGABIT = null;
    public static final MeasureUnit MEGABYTE = null;
    public static final MeasureUnit MEGAHERTZ = null;
    public static final MeasureUnit MEGALITER = null;
    public static final MeasureUnit MEGAWATT = null;
    public static final MeasureUnit METER = null;
    public static final MeasureUnit METER_PER_SECOND = null;
    public static final MeasureUnit METER_PER_SECOND_SQUARED = null;
    public static final MeasureUnit METRIC_TON = null;
    public static final MeasureUnit MICROGRAM = null;
    public static final MeasureUnit MICROMETER = null;
    public static final MeasureUnit MICROSECOND = null;
    public static final MeasureUnit MILE = null;
    public static final MeasureUnit MILE_PER_GALLON = null;
    public static final MeasureUnit MILE_PER_HOUR = null;
    public static final MeasureUnit MILE_SCANDINAVIAN = null;
    public static final MeasureUnit MILLIAMPERE = null;
    public static final MeasureUnit MILLIBAR = null;
    public static final MeasureUnit MILLIGRAM = null;
    public static final MeasureUnit MILLILITER = null;
    public static final MeasureUnit MILLIMETER = null;
    public static final MeasureUnit MILLIMETER_OF_MERCURY = null;
    public static final MeasureUnit MILLISECOND = null;
    public static final MeasureUnit MILLIWATT = null;
    public static final TimeUnit MINUTE = null;
    public static final TimeUnit MONTH = null;
    public static final MeasureUnit NANOMETER = null;
    public static final MeasureUnit NANOSECOND = null;
    public static final MeasureUnit NAUTICAL_MILE = null;
    public static final MeasureUnit OHM = null;
    public static final MeasureUnit OUNCE = null;
    public static final MeasureUnit OUNCE_TROY = null;
    public static final MeasureUnit PARSEC = null;
    public static final MeasureUnit PICOMETER = null;
    public static final MeasureUnit PINT = null;
    public static final MeasureUnit PINT_METRIC = null;
    public static final MeasureUnit POUND = null;
    public static final MeasureUnit POUND_PER_SQUARE_INCH = null;
    public static final MeasureUnit QUART = null;
    public static final MeasureUnit RADIAN = null;
    public static final MeasureUnit REVOLUTION_ANGLE = null;
    public static final TimeUnit SECOND = null;
    public static final MeasureUnit SQUARE_CENTIMETER = null;
    public static final MeasureUnit SQUARE_FOOT = null;
    public static final MeasureUnit SQUARE_INCH = null;
    public static final MeasureUnit SQUARE_KILOMETER = null;
    public static final MeasureUnit SQUARE_METER = null;
    public static final MeasureUnit SQUARE_MILE = null;
    public static final MeasureUnit SQUARE_YARD = null;
    public static final MeasureUnit STONE = null;
    public static final MeasureUnit TABLESPOON = null;
    public static final MeasureUnit TEASPOON = null;
    public static final MeasureUnit TERABIT = null;
    public static final MeasureUnit TERABYTE = null;
    static Factory TIMEUNIT_FACTORY = null;
    public static final MeasureUnit TON = null;
    private static Factory UNIT_FACTORY = null;
    public static final MeasureUnit VOLT = null;
    public static final MeasureUnit WATT = null;
    public static final TimeUnit WEEK = null;
    public static final MeasureUnit YARD = null;
    public static final TimeUnit YEAR = null;
    private static final Map<String, Map<String, MeasureUnit>> cache = null;
    private static final long serialVersionUID = -1839973855554750484L;
    private static final String[] unitKeys = null;
    private static HashMap<Pair<MeasureUnit, MeasureUnit>, MeasureUnit> unitPerUnitToSingleUnit;
    @Deprecated
    protected final String subType;
    @Deprecated
    protected final String type;

    @Deprecated
    protected interface Factory {
        @Deprecated
        MeasureUnit create(String str, String str2);
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.MeasureUnit.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.MeasureUnit.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.MeasureUnit.<clinit>():void");
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
            unmodifiableSet = Collections.unmodifiableSet(cache.keySet());
        }
        return unmodifiableSet;
    }

    public static synchronized Set<MeasureUnit> getAvailable(String type) {
        Set<MeasureUnit> emptySet;
        synchronized (MeasureUnit.class) {
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
        } else if ("currency".equals(type) || (ASCII.containsAll(type) && ASCII_HYPHEN_DIGITS.containsAll(subType))) {
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
