package com.huawei.okhttp3;

import com.huawei.android.util.JlogConstantsEx;
import com.huawei.lcagent.client.MetricConstant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class CipherSuite {
    private static final Map<String, CipherSuite> INSTANCES = new TreeMap(ORDER_BY_NAME);
    static final Comparator<String> ORDER_BY_NAME = new Comparator<String>() {
        public int compare(String a, String b) {
            int i = 4;
            int limit = Math.min(a.length(), b.length());
            while (true) {
                int i2 = -1;
                if (i < limit) {
                    char charA = a.charAt(i);
                    char charB = b.charAt(i);
                    if (charA != charB) {
                        if (charA >= charB) {
                            i2 = 1;
                        }
                        return i2;
                    }
                    i++;
                } else {
                    int lengthA = a.length();
                    int lengthB = b.length();
                    if (lengthA == lengthB) {
                        return 0;
                    }
                    if (lengthA >= lengthB) {
                        i2 = 1;
                    }
                    return i2;
                }
            }
        }
    };
    public static final CipherSuite TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA = of("SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", 19);
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_128_CBC_SHA = of("TLS_DHE_DSS_WITH_AES_128_CBC_SHA", 50);
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_128_CBC_SHA256 = of("TLS_DHE_DSS_WITH_AES_128_CBC_SHA256", 64);
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_128_GCM_SHA256 = of("TLS_DHE_DSS_WITH_AES_128_GCM_SHA256", 162);
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_256_CBC_SHA = of("TLS_DHE_DSS_WITH_AES_256_CBC_SHA", 56);
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_256_CBC_SHA256 = of("TLS_DHE_DSS_WITH_AES_256_CBC_SHA256", 106);
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_256_GCM_SHA384 = of("TLS_DHE_DSS_WITH_AES_256_GCM_SHA384", 163);
    public static final CipherSuite TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA = of("TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA", 68);
    public static final CipherSuite TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA = of("TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA", 135);
    public static final CipherSuite TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA = of("SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", 22);
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_128_CBC_SHA = of("TLS_DHE_RSA_WITH_AES_128_CBC_SHA", 51);
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_128_CBC_SHA256 = of("TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", 103);
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_128_GCM_SHA256 = of("TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", 158);
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_256_CBC_SHA = of("TLS_DHE_RSA_WITH_AES_256_CBC_SHA", 57);
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_256_CBC_SHA256 = of("TLS_DHE_RSA_WITH_AES_256_CBC_SHA256", MetricConstant.BLUETOOTH_METRIC_ID_EX);
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_256_GCM_SHA384 = of("TLS_DHE_RSA_WITH_AES_256_GCM_SHA384", 159);
    public static final CipherSuite TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA = of("TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA", 69);
    public static final CipherSuite TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA = of("TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA", 136);
    public static final CipherSuite TLS_DH_anon_WITH_AES_128_CBC_SHA256 = of("TLS_DH_anon_WITH_AES_128_CBC_SHA256", MetricConstant.GPS_METRIC_ID_EX);
    public static final CipherSuite TLS_DH_anon_WITH_AES_128_GCM_SHA256 = of("TLS_DH_anon_WITH_AES_128_GCM_SHA256", 166);
    public static final CipherSuite TLS_DH_anon_WITH_AES_256_CBC_SHA256 = of("TLS_DH_anon_WITH_AES_256_CBC_SHA256", 109);
    public static final CipherSuite TLS_DH_anon_WITH_AES_256_GCM_SHA384 = of("TLS_DH_anon_WITH_AES_256_GCM_SHA384", 167);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA = of("TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA", 49160);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA = of("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", 49161);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256 = of("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", 49187);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 = of("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", 49195);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA = of("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA", 49162);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384 = of("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", 49188);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 = of("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", 49196);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256 = of("TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256", 52393);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_NULL_SHA = of("TLS_ECDHE_ECDSA_WITH_NULL_SHA", 49158);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_RC4_128_SHA = of("TLS_ECDHE_ECDSA_WITH_RC4_128_SHA", 49159);
    public static final CipherSuite TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA = of("TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA", 49205);
    public static final CipherSuite TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA = of("TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA", 49206);
    public static final CipherSuite TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256 = of("TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256", 52396);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA = of("TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", 49170);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA = of("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", 49171);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256 = of("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", 49191);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 = of("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", 49199);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA = of("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", 49172);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384 = of("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", 49192);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 = of("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", 49200);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256 = of("TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256", 52392);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_NULL_SHA = of("TLS_ECDHE_RSA_WITH_NULL_SHA", 49168);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_RC4_128_SHA = of("TLS_ECDHE_RSA_WITH_RC4_128_SHA", 49169);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA = of("TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA", 49155);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA = of("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", 49156);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256 = of("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256", 49189);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256 = of("TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256", 49197);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA = of("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA", 49157);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384 = of("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384", 49190);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384 = of("TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384", 49198);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_NULL_SHA = of("TLS_ECDH_ECDSA_WITH_NULL_SHA", 49153);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_RC4_128_SHA = of("TLS_ECDH_ECDSA_WITH_RC4_128_SHA", 49154);
    public static final CipherSuite TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA = of("TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA", 49165);
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_128_CBC_SHA = of("TLS_ECDH_RSA_WITH_AES_128_CBC_SHA", 49166);
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256 = of("TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256", 49193);
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256 = of("TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256", 49201);
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_256_CBC_SHA = of("TLS_ECDH_RSA_WITH_AES_256_CBC_SHA", 49167);
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384 = of("TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384", 49194);
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384 = of("TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384", 49202);
    public static final CipherSuite TLS_ECDH_RSA_WITH_NULL_SHA = of("TLS_ECDH_RSA_WITH_NULL_SHA", 49163);
    public static final CipherSuite TLS_ECDH_RSA_WITH_RC4_128_SHA = of("TLS_ECDH_RSA_WITH_RC4_128_SHA", 49164);
    public static final CipherSuite TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA = of("TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA", 49175);
    public static final CipherSuite TLS_ECDH_anon_WITH_AES_128_CBC_SHA = of("TLS_ECDH_anon_WITH_AES_128_CBC_SHA", 49176);
    public static final CipherSuite TLS_ECDH_anon_WITH_AES_256_CBC_SHA = of("TLS_ECDH_anon_WITH_AES_256_CBC_SHA", 49177);
    public static final CipherSuite TLS_ECDH_anon_WITH_NULL_SHA = of("TLS_ECDH_anon_WITH_NULL_SHA", 49173);
    public static final CipherSuite TLS_ECDH_anon_WITH_RC4_128_SHA = of("TLS_ECDH_anon_WITH_RC4_128_SHA", 49174);
    public static final CipherSuite TLS_EMPTY_RENEGOTIATION_INFO_SCSV = of("TLS_EMPTY_RENEGOTIATION_INFO_SCSV", 255);
    public static final CipherSuite TLS_FALLBACK_SCSV = of("TLS_FALLBACK_SCSV", 22016);
    public static final CipherSuite TLS_KRB5_WITH_3DES_EDE_CBC_MD5 = of("TLS_KRB5_WITH_3DES_EDE_CBC_MD5", 35);
    public static final CipherSuite TLS_KRB5_WITH_3DES_EDE_CBC_SHA = of("TLS_KRB5_WITH_3DES_EDE_CBC_SHA", 31);
    public static final CipherSuite TLS_KRB5_WITH_RC4_128_MD5 = of("TLS_KRB5_WITH_RC4_128_MD5", 36);
    public static final CipherSuite TLS_KRB5_WITH_RC4_128_SHA = of("TLS_KRB5_WITH_RC4_128_SHA", 32);
    public static final CipherSuite TLS_PSK_WITH_3DES_EDE_CBC_SHA = of("TLS_PSK_WITH_3DES_EDE_CBC_SHA", JlogConstantsEx.JLID_MMS_MESSAGE_SEARCH);
    public static final CipherSuite TLS_PSK_WITH_AES_128_CBC_SHA = of("TLS_PSK_WITH_AES_128_CBC_SHA", 140);
    public static final CipherSuite TLS_PSK_WITH_AES_256_CBC_SHA = of("TLS_PSK_WITH_AES_256_CBC_SHA", JlogConstantsEx.JLID_MMS_MATCHED_CONTACTS_SEARCH);
    public static final CipherSuite TLS_PSK_WITH_RC4_128_SHA = of("TLS_PSK_WITH_RC4_128_SHA", JlogConstantsEx.JLID_MMS_MESSAGES_DELETE);
    public static final CipherSuite TLS_RSA_WITH_3DES_EDE_CBC_SHA = of("SSL_RSA_WITH_3DES_EDE_CBC_SHA", 10);
    public static final CipherSuite TLS_RSA_WITH_AES_128_CBC_SHA = of("TLS_RSA_WITH_AES_128_CBC_SHA", 47);
    public static final CipherSuite TLS_RSA_WITH_AES_128_CBC_SHA256 = of("TLS_RSA_WITH_AES_128_CBC_SHA256", 60);
    public static final CipherSuite TLS_RSA_WITH_AES_128_GCM_SHA256 = of("TLS_RSA_WITH_AES_128_GCM_SHA256", 156);
    public static final CipherSuite TLS_RSA_WITH_AES_256_CBC_SHA = of("TLS_RSA_WITH_AES_256_CBC_SHA", 53);
    public static final CipherSuite TLS_RSA_WITH_AES_256_CBC_SHA256 = of("TLS_RSA_WITH_AES_256_CBC_SHA256", 61);
    public static final CipherSuite TLS_RSA_WITH_AES_256_GCM_SHA384 = of("TLS_RSA_WITH_AES_256_GCM_SHA384", 157);
    public static final CipherSuite TLS_RSA_WITH_CAMELLIA_128_CBC_SHA = of("TLS_RSA_WITH_CAMELLIA_128_CBC_SHA", 65);
    public static final CipherSuite TLS_RSA_WITH_CAMELLIA_256_CBC_SHA = of("TLS_RSA_WITH_CAMELLIA_256_CBC_SHA", JlogConstantsEx.JLID_EDIT_CONTACT_END);
    public static final CipherSuite TLS_RSA_WITH_NULL_SHA256 = of("TLS_RSA_WITH_NULL_SHA256", 59);
    public static final CipherSuite TLS_RSA_WITH_RC4_128_MD5 = of("SSL_RSA_WITH_RC4_128_MD5", 4);
    public static final CipherSuite TLS_RSA_WITH_RC4_128_SHA = of("SSL_RSA_WITH_RC4_128_SHA", 5);
    public static final CipherSuite TLS_RSA_WITH_SEED_CBC_SHA = of("TLS_RSA_WITH_SEED_CBC_SHA", 150);
    final String javaName;

    public static synchronized CipherSuite forJavaName(String javaName2) {
        CipherSuite result;
        synchronized (CipherSuite.class) {
            result = INSTANCES.get(javaName2);
            if (result == null) {
                result = new CipherSuite(javaName2);
                INSTANCES.put(javaName2, result);
            }
        }
        return result;
    }

    static List<CipherSuite> forJavaNames(String... cipherSuites) {
        List<CipherSuite> result = new ArrayList<>(cipherSuites.length);
        for (String cipherSuite : cipherSuites) {
            result.add(forJavaName(cipherSuite));
        }
        return Collections.unmodifiableList(result);
    }

    private CipherSuite(String javaName2) {
        if (javaName2 != null) {
            this.javaName = javaName2;
            return;
        }
        throw new NullPointerException();
    }

    private static CipherSuite of(String javaName2, int value) {
        return forJavaName(javaName2);
    }

    public String javaName() {
        return this.javaName;
    }

    public String toString() {
        return this.javaName;
    }
}
