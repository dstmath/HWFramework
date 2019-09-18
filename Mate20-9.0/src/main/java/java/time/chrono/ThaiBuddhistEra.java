package java.time.chrono;

import java.time.DateTimeException;

public enum ThaiBuddhistEra implements Era {
    BEFORE_BE,
    BE;

    public static ThaiBuddhistEra of(int thaiBuddhistEra) {
        switch (thaiBuddhistEra) {
            case 0:
                return BEFORE_BE;
            case 1:
                return BE;
            default:
                throw new DateTimeException("Invalid era: " + thaiBuddhistEra);
        }
    }

    public int getValue() {
        return ordinal();
    }
}
