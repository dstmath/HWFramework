package java.time.chrono;

import java.time.DateTimeException;

public enum IsoEra implements Era {
    BCE,
    CE;

    public static IsoEra of(int isoEra) {
        switch (isoEra) {
            case 0:
                return BCE;
            case 1:
                return CE;
            default:
                throw new DateTimeException("Invalid era: " + isoEra);
        }
    }

    public int getValue() {
        return ordinal();
    }
}
