package org.bouncycastle.mime.smime;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.util.Strings;

class SMimeUtils {
    private static final Map RFC3851_MICALGS;
    private static final Map RFC5751_MICALGS;
    private static final Map STANDARD_MICALGS = RFC5751_MICALGS;
    private static final Map forMic;
    private static final byte[] nl = new byte[2];

    static {
        byte[] bArr = nl;
        bArr[0] = 13;
        bArr[1] = 10;
        HashMap hashMap = new HashMap();
        hashMap.put(CMSAlgorithm.MD5, "md5");
        hashMap.put(CMSAlgorithm.SHA1, "sha-1");
        hashMap.put(CMSAlgorithm.SHA224, "sha-224");
        hashMap.put(CMSAlgorithm.SHA256, "sha-256");
        hashMap.put(CMSAlgorithm.SHA384, "sha-384");
        hashMap.put(CMSAlgorithm.SHA512, "sha-512");
        hashMap.put(CMSAlgorithm.GOST3411, "gostr3411-94");
        hashMap.put(CMSAlgorithm.GOST3411_2012_256, "gostr3411-2012-256");
        hashMap.put(CMSAlgorithm.GOST3411_2012_512, "gostr3411-2012-512");
        RFC5751_MICALGS = Collections.unmodifiableMap(hashMap);
        HashMap hashMap2 = new HashMap();
        hashMap2.put(CMSAlgorithm.MD5, "md5");
        hashMap2.put(CMSAlgorithm.SHA1, "sha1");
        hashMap2.put(CMSAlgorithm.SHA224, "sha224");
        hashMap2.put(CMSAlgorithm.SHA256, "sha256");
        hashMap2.put(CMSAlgorithm.SHA384, "sha384");
        hashMap2.put(CMSAlgorithm.SHA512, "sha512");
        hashMap2.put(CMSAlgorithm.GOST3411, "gostr3411-94");
        hashMap2.put(CMSAlgorithm.GOST3411_2012_256, "gostr3411-2012-256");
        hashMap2.put(CMSAlgorithm.GOST3411_2012_512, "gostr3411-2012-512");
        RFC3851_MICALGS = Collections.unmodifiableMap(hashMap2);
        TreeMap treeMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        for (Object obj : STANDARD_MICALGS.keySet()) {
            treeMap.put(STANDARD_MICALGS.get(obj).toString(), (ASN1ObjectIdentifier) obj);
        }
        for (Object obj2 : RFC3851_MICALGS.keySet()) {
            treeMap.put(RFC3851_MICALGS.get(obj2).toString(), (ASN1ObjectIdentifier) obj2);
        }
        forMic = Collections.unmodifiableMap(treeMap);
    }

    SMimeUtils() {
    }

    static InputStream autoBuffer(InputStream inputStream) {
        return inputStream instanceof FileInputStream ? new BufferedInputStream(inputStream) : inputStream;
    }

    static OutputStream autoBuffer(OutputStream outputStream) {
        return outputStream instanceof FileOutputStream ? new BufferedOutputStream(outputStream) : outputStream;
    }

    static OutputStream createUnclosable(OutputStream outputStream) {
        return new FilterOutputStream(outputStream) {
            /* class org.bouncycastle.mime.smime.SMimeUtils.AnonymousClass1 */

            @Override // java.io.FilterOutputStream, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
            public void close() throws IOException {
            }

            @Override // java.io.FilterOutputStream, java.io.OutputStream
            public void write(byte[] bArr, int i, int i2) throws IOException {
                if (bArr != null) {
                    int i3 = i2 + i;
                    if ((i | i2 | (bArr.length - i3) | i3) >= 0) {
                        this.out.write(bArr, i, i2);
                        return;
                    }
                    throw new IndexOutOfBoundsException();
                }
                throw new NullPointerException();
            }
        };
    }

    static ASN1ObjectIdentifier getDigestOID(String str) {
        ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) forMic.get(Strings.toLowerCase(str));
        if (aSN1ObjectIdentifier != null) {
            return aSN1ObjectIdentifier;
        }
        throw new IllegalArgumentException("unknown micalg passed: " + str);
    }

    static String getParameter(String str, List<String> list) {
        for (String str2 : list) {
            if (str2.startsWith(str)) {
                return str2;
            }
        }
        return null;
    }

    static String lessQuotes(String str) {
        return (str == null || str.length() <= 1 || str.charAt(0) != '\"' || str.charAt(str.length() - 1) != '\"') ? str : str.substring(1, str.length() - 1);
    }
}
