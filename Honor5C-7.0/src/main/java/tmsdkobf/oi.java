package tmsdkobf;

/* compiled from: Unknown */
public class oi {
    public static final int bV(int i) {
        return i % 20;
    }

    public static final int bW(int i) {
        return (i % 10000) - bV(i);
    }

    public static final int bX(int i) {
        return ((i % 1000000) - bW(i)) - bV(i);
    }

    public static int bY(int i) {
        switch (i) {
            case -170000:
                return i - 5;
            case -160000:
                return i - 6;
            case -150000:
                return i - 3;
            case -140000:
                return i - 3;
            case -130000:
                return i - 4;
            case -120000:
                return i - 3;
            case -110000:
                return i - 3;
            case -100000:
                return i - 3;
            case -90000:
                return i - 3;
            case -80000:
                return i - 3;
            case -70000:
                return i - 3;
            case -60000:
                return i - 3;
            case -50000:
                return i - 4;
            case -40000:
                return i - 3;
            case -30000:
                return i - 3;
            case -20000:
                return i - 3;
            case -10000:
                return i - 3;
            default:
                return i;
        }
    }
}
