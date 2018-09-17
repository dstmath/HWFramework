package java.time.chrono;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.Objects;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.Era;

public final class JapaneseEra implements Era, Serializable {
    static final Era[] ERA_CONFIG = JapaneseChronology.JCAL.getEras();
    static final int ERA_OFFSET = 2;
    public static final JapaneseEra HEISEI = new JapaneseEra(2, LocalDate.of(1989, 1, 8));
    private static final JapaneseEra[] KNOWN_ERAS = new JapaneseEra[ERA_CONFIG.length];
    public static final JapaneseEra MEIJI = new JapaneseEra(-1, LocalDate.of(1868, 1, 1));
    private static final int N_ERA_CONSTANTS = (HEISEI.getValue() + 2);
    public static final JapaneseEra SHOWA = new JapaneseEra(1, LocalDate.of(1926, 12, 25));
    public static final JapaneseEra TAISHO = new JapaneseEra(0, LocalDate.of(1912, 7, 30));
    private static final long serialVersionUID = 1466499369062886794L;
    private final transient int eraValue;
    private final transient LocalDate since;

    static {
        KNOWN_ERAS[0] = MEIJI;
        KNOWN_ERAS[1] = TAISHO;
        KNOWN_ERAS[2] = SHOWA;
        KNOWN_ERAS[3] = HEISEI;
        for (int i = N_ERA_CONSTANTS; i < ERA_CONFIG.length; i++) {
            CalendarDate date = ERA_CONFIG[i].getSinceDate();
            KNOWN_ERAS[i] = new JapaneseEra((i - 2) + 1, LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth()));
        }
    }

    private JapaneseEra(int eraValue, LocalDate since) {
        this.eraValue = eraValue;
        this.since = since;
    }

    Era getPrivateEra() {
        return ERA_CONFIG[ordinal(this.eraValue)];
    }

    public static JapaneseEra of(int japaneseEra) {
        if (japaneseEra >= MEIJI.eraValue && japaneseEra + 2 <= KNOWN_ERAS.length) {
            return KNOWN_ERAS[ordinal(japaneseEra)];
        }
        throw new DateTimeException("Invalid era: " + japaneseEra);
    }

    public static JapaneseEra valueOf(String japaneseEra) {
        Objects.requireNonNull((Object) japaneseEra, "japaneseEra");
        for (JapaneseEra era : KNOWN_ERAS) {
            if (era.getName().equals(japaneseEra)) {
                return era;
            }
        }
        throw new IllegalArgumentException("japaneseEra is invalid");
    }

    public static JapaneseEra[] values() {
        return (JapaneseEra[]) Arrays.copyOf(KNOWN_ERAS, KNOWN_ERAS.length);
    }

    static JapaneseEra from(LocalDate date) {
        if (date.isBefore(JapaneseDate.MEIJI_6_ISODATE)) {
            throw new DateTimeException("JapaneseDate before Meiji 6 are not supported");
        }
        for (int i = KNOWN_ERAS.length - 1; i > 0; i--) {
            JapaneseEra era = KNOWN_ERAS[i];
            if (date.compareTo(era.since) >= 0) {
                return era;
            }
        }
        return null;
    }

    static JapaneseEra toJapaneseEra(Era privateEra) {
        for (int i = ERA_CONFIG.length - 1; i >= 0; i--) {
            if (ERA_CONFIG[i].equals(privateEra)) {
                return KNOWN_ERAS[i];
            }
        }
        return null;
    }

    static Era privateEraFrom(LocalDate isoDate) {
        for (int i = KNOWN_ERAS.length - 1; i > 0; i--) {
            if (isoDate.compareTo(KNOWN_ERAS[i].since) >= 0) {
                return ERA_CONFIG[i];
            }
        }
        return null;
    }

    private static int ordinal(int eraValue) {
        return (eraValue + 2) - 1;
    }

    public int getValue() {
        return this.eraValue;
    }

    public ValueRange range(TemporalField field) {
        if (field == ChronoField.ERA) {
            return JapaneseChronology.INSTANCE.range(ChronoField.ERA);
        }
        return super.range(field);
    }

    String getAbbreviation() {
        int index = ordinal(getValue());
        if (index == 0) {
            return "";
        }
        return ERA_CONFIG[index].getAbbreviation();
    }

    String getName() {
        return ERA_CONFIG[ordinal(getValue())].getName();
    }

    public String toString() {
        return getName();
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace() {
        return new Ser((byte) 5, this);
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeByte(getValue());
    }

    static JapaneseEra readExternal(DataInput in) throws IOException {
        return of(in.readByte());
    }
}
