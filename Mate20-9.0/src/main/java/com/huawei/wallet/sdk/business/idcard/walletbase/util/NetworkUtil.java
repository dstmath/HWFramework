package com.huawei.wallet.sdk.business.idcard.walletbase.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLSocket;

public class NetworkUtil {
    private static final String[] UN_SAFE_ALGORITHMS = {"TEA", "SHA0", "MD2", "MD4", "RIPEMD", "aNULL", "eNULL", "RC4", "DES", "DESX", "DES40", "RC2", "MD5", "ANON", "NULL", "TLS_EMPTY_RENEGOTIATION_INFO_SCSV"};

    public static String[] setEnableSafeCipherSuites(SSLSocket sslsock) {
        if (sslsock == null) {
            return new String[0];
        }
        String[] ENABLED_CIPHERS = sslsock.getEnabledCipherSuites();
        List<String> ENABLED_CIPHERS_List = new ArrayList<>();
        Object obj = "";
        for (String string : ENABLED_CIPHERS) {
            boolean isSafeAlgorithm = true;
            String upperCaseStr = string.toUpperCase(Locale.ENGLISH);
            String[] strArr = UN_SAFE_ALGORITHMS;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (upperCaseStr.contains(strArr[i])) {
                    isSafeAlgorithm = false;
                    break;
                } else {
                    i++;
                }
            }
            if (isSafeAlgorithm) {
                ENABLED_CIPHERS_List.add(string);
            }
        }
        String[] SAFE_ENABLED_CIPHERS = (String[]) ENABLED_CIPHERS_List.toArray(new String[ENABLED_CIPHERS_List.size()]);
        sslsock.setEnabledCipherSuites(SAFE_ENABLED_CIPHERS);
        return SAFE_ENABLED_CIPHERS;
    }
}
