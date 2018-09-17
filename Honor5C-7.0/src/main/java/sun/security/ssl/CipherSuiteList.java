package sun.security.ssl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.net.ssl.SSLException;
import sun.util.calendar.BaseCalendar;

final class CipherSuiteList {
    private static final /* synthetic */ int[] -sun-security-ssl-CipherSuite$KeyExchangeSwitchesValues = null;
    private final Collection<CipherSuite> cipherSuites;
    private volatile Boolean containsEC;
    private String[] suiteNames;

    private static /* synthetic */ int[] -getsun-security-ssl-CipherSuite$KeyExchangeSwitchesValues() {
        if (-sun-security-ssl-CipherSuite$KeyExchangeSwitchesValues != null) {
            return -sun-security-ssl-CipherSuite$KeyExchangeSwitchesValues;
        }
        int[] iArr = new int[KeyExchange.values().length];
        try {
            iArr[KeyExchange.K_DHE_DSS.ordinal()] = 6;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[KeyExchange.K_DHE_RSA.ordinal()] = 7;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[KeyExchange.K_DH_ANON.ordinal()] = 8;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[KeyExchange.K_DH_DSS.ordinal()] = 9;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[KeyExchange.K_DH_RSA.ordinal()] = 10;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[KeyExchange.K_ECDHE_ECDSA.ordinal()] = 1;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[KeyExchange.K_ECDHE_RSA.ordinal()] = 2;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[KeyExchange.K_ECDH_ANON.ordinal()] = 3;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[KeyExchange.K_ECDH_ECDSA.ordinal()] = 4;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[KeyExchange.K_ECDH_RSA.ordinal()] = 5;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[KeyExchange.K_KRB5.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[KeyExchange.K_KRB5_EXPORT.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[KeyExchange.K_NULL.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[KeyExchange.K_RSA.ordinal()] = 14;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[KeyExchange.K_RSA_EXPORT.ordinal()] = 15;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[KeyExchange.K_SCSV.ordinal()] = 16;
        } catch (NoSuchFieldError e16) {
        }
        -sun-security-ssl-CipherSuite$KeyExchangeSwitchesValues = iArr;
        return iArr;
    }

    CipherSuiteList(Collection<CipherSuite> cipherSuites) {
        this.cipherSuites = cipherSuites;
    }

    CipherSuiteList(CipherSuite suite) {
        this.cipherSuites = new ArrayList(1);
        this.cipherSuites.add(suite);
    }

    CipherSuiteList(String[] names) {
        if (names == null) {
            throw new IllegalArgumentException("CipherSuites may not be null");
        }
        this.cipherSuites = new ArrayList(names.length);
        boolean refreshed = false;
        for (String suiteName : names) {
            CipherSuite suite = CipherSuite.valueOf(suiteName);
            if (!suite.isAvailable()) {
                if (!refreshed) {
                    clearAvailableCache();
                    refreshed = true;
                }
                if (!suite.isAvailable()) {
                    throw new IllegalArgumentException("Cannot support " + suiteName + " with currently installed providers");
                }
            }
            this.cipherSuites.add(suite);
        }
    }

    CipherSuiteList(HandshakeInStream in) throws IOException {
        byte[] bytes = in.getBytes16();
        if ((bytes.length & 1) != 0) {
            throw new SSLException("Invalid ClientHello message");
        }
        this.cipherSuites = new ArrayList(bytes.length >> 1);
        for (int i = 0; i < bytes.length; i += 2) {
            this.cipherSuites.add(CipherSuite.valueOf(bytes[i], bytes[i + 1]));
        }
    }

    boolean contains(CipherSuite suite) {
        return this.cipherSuites.contains(suite);
    }

    boolean containsEC() {
        if (this.containsEC == null) {
            for (CipherSuite c : this.cipherSuites) {
                switch (-getsun-security-ssl-CipherSuite$KeyExchangeSwitchesValues()[c.keyExchange.ordinal()]) {
                    case BaseCalendar.SUNDAY /*1*/:
                    case BaseCalendar.MONDAY /*2*/:
                    case BaseCalendar.TUESDAY /*3*/:
                    case BaseCalendar.WEDNESDAY /*4*/:
                    case BaseCalendar.THURSDAY /*5*/:
                        this.containsEC = Boolean.valueOf(true);
                        return true;
                    default:
                }
            }
            this.containsEC = Boolean.valueOf(false);
        }
        return this.containsEC.booleanValue();
    }

    Iterator<CipherSuite> iterator() {
        return this.cipherSuites.iterator();
    }

    Collection<CipherSuite> collection() {
        return this.cipherSuites;
    }

    int size() {
        return this.cipherSuites.size();
    }

    synchronized String[] toStringArray() {
        if (this.suiteNames == null) {
            this.suiteNames = new String[this.cipherSuites.size()];
            int i = 0;
            for (CipherSuite c : this.cipherSuites) {
                int i2 = i + 1;
                this.suiteNames[i] = c.name;
                i = i2;
            }
        }
        return (String[]) this.suiteNames.clone();
    }

    public String toString() {
        return this.cipherSuites.toString();
    }

    void send(HandshakeOutStream s) throws IOException {
        byte[] suiteBytes = new byte[(this.cipherSuites.size() * 2)];
        int i = 0;
        for (CipherSuite c : this.cipherSuites) {
            suiteBytes[i] = (byte) (c.id >> 8);
            suiteBytes[i + 1] = (byte) c.id;
            i += 2;
        }
        s.putBytes16(suiteBytes);
    }

    static synchronized void clearAvailableCache() {
        synchronized (CipherSuiteList.class) {
            BulkCipher.clearAvailableCache();
            JsseJce.clearEcAvailable();
        }
    }
}
