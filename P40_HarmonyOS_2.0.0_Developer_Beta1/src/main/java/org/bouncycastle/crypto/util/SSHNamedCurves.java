package org.bouncycastle.crypto.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.asn1.sec.SECObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.util.Strings;

public class SSHNamedCurves {
    private static HashMap<ECCurve, String> curveMap = new HashMap<ECCurve, String>() {
        /* class org.bouncycastle.crypto.util.SSHNamedCurves.AnonymousClass3 */

        {
            Enumeration names = CustomNamedCurves.getNames();
            while (names.hasMoreElements()) {
                String str = (String) names.nextElement();
                put(CustomNamedCurves.getByName(str).getCurve(), str);
            }
        }
    };
    private static final Map<String, String> curveNameToSSHName = Collections.unmodifiableMap(new HashMap<String, String>() {
        /* class org.bouncycastle.crypto.util.SSHNamedCurves.AnonymousClass2 */

        {
            String[][] strArr = {new String[]{"secp256r1", "nistp256"}, new String[]{"secp384r1", "nistp384"}, new String[]{"secp521r1", "nistp521"}, new String[]{"sect163k1", "nistk163"}, new String[]{"secp192r1", "nistp192"}, new String[]{"secp224r1", "nistp224"}, new String[]{"sect233k1", "nistk233"}, new String[]{"sect233r1", "nistb233"}, new String[]{"sect283k1", "nistk283"}, new String[]{"sect409k1", "nistk409"}, new String[]{"sect409r1", "nistb409"}, new String[]{"sect571k1", "nistt571"}};
            for (int i = 0; i != strArr.length; i++) {
                String[] strArr2 = strArr[i];
                put(strArr2[0], strArr2[1]);
            }
        }
    });
    private static final Map<String, ASN1ObjectIdentifier> oidMap = Collections.unmodifiableMap(new HashMap<String, ASN1ObjectIdentifier>() {
        /* class org.bouncycastle.crypto.util.SSHNamedCurves.AnonymousClass1 */

        {
            put("nistp256", SECObjectIdentifiers.secp256r1);
            put("nistp384", SECObjectIdentifiers.secp384r1);
            put("nistp521", SECObjectIdentifiers.secp521r1);
            put("nistk163", SECObjectIdentifiers.sect163k1);
            put("nistp192", SECObjectIdentifiers.secp192r1);
            put("nistp224", SECObjectIdentifiers.secp224r1);
            put("nistk233", SECObjectIdentifiers.sect233k1);
            put("nistb233", SECObjectIdentifiers.sect233r1);
            put("nistk283", SECObjectIdentifiers.sect283k1);
            put("nistk409", SECObjectIdentifiers.sect409k1);
            put("nistb409", SECObjectIdentifiers.sect409r1);
            put("nistt571", SECObjectIdentifiers.sect571k1);
        }
    });
    private static final Map<ASN1ObjectIdentifier, String> oidToName = Collections.unmodifiableMap(new HashMap<ASN1ObjectIdentifier, String>() {
        /* class org.bouncycastle.crypto.util.SSHNamedCurves.AnonymousClass4 */

        {
            for (String str : SSHNamedCurves.oidMap.keySet()) {
                put(SSHNamedCurves.oidMap.get(str), str);
            }
        }
    });

    public static ASN1ObjectIdentifier getByName(String str) {
        return oidMap.get(str);
    }

    public static String getName(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return oidToName.get(aSN1ObjectIdentifier);
    }

    public static String getNameForParameters(ECDomainParameters eCDomainParameters) {
        return eCDomainParameters instanceof ECNamedDomainParameters ? getName(((ECNamedDomainParameters) eCDomainParameters).getName()) : getNameForParameters(eCDomainParameters.getCurve());
    }

    public static String getNameForParameters(ECCurve eCCurve) {
        return curveNameToSSHName.get(curveMap.get(eCCurve));
    }

    public static X9ECParameters getParameters(String str) {
        return NISTNamedCurves.getByOID(oidMap.get(Strings.toLowerCase(str)));
    }

    public static X9ECParameters getParameters(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return NISTNamedCurves.getByOID(aSN1ObjectIdentifier);
    }
}
