package org.bouncycastle.asn1.cmc;

import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;

public class CMCFailInfo extends ASN1Object {
    public static final CMCFailInfo authDataFail = new CMCFailInfo(new ASN1Integer(13));
    public static final CMCFailInfo badAlg = new CMCFailInfo(new ASN1Integer(0));
    public static final CMCFailInfo badCertId = new CMCFailInfo(new ASN1Integer(4));
    public static final CMCFailInfo badIdentity = new CMCFailInfo(new ASN1Integer(7));
    public static final CMCFailInfo badMessageCheck = new CMCFailInfo(new ASN1Integer(1));
    public static final CMCFailInfo badRequest = new CMCFailInfo(new ASN1Integer(2));
    public static final CMCFailInfo badTime = new CMCFailInfo(new ASN1Integer(3));
    public static final CMCFailInfo internalCAError = new CMCFailInfo(new ASN1Integer(11));
    public static final CMCFailInfo mustArchiveKeys = new CMCFailInfo(new ASN1Integer(6));
    public static final CMCFailInfo noKeyReuse = new CMCFailInfo(new ASN1Integer(10));
    public static final CMCFailInfo popFailed = new CMCFailInfo(new ASN1Integer(9));
    public static final CMCFailInfo popRequired = new CMCFailInfo(new ASN1Integer(8));
    private static Map range = new HashMap();
    public static final CMCFailInfo tryLater = new CMCFailInfo(new ASN1Integer(12));
    public static final CMCFailInfo unsupportedExt = new CMCFailInfo(new ASN1Integer(5));
    private final ASN1Integer value;

    static {
        Map map = range;
        CMCFailInfo cMCFailInfo = badAlg;
        map.put(cMCFailInfo.value, cMCFailInfo);
        Map map2 = range;
        CMCFailInfo cMCFailInfo2 = badMessageCheck;
        map2.put(cMCFailInfo2.value, cMCFailInfo2);
        Map map3 = range;
        CMCFailInfo cMCFailInfo3 = badRequest;
        map3.put(cMCFailInfo3.value, cMCFailInfo3);
        Map map4 = range;
        CMCFailInfo cMCFailInfo4 = badTime;
        map4.put(cMCFailInfo4.value, cMCFailInfo4);
        Map map5 = range;
        CMCFailInfo cMCFailInfo5 = badCertId;
        map5.put(cMCFailInfo5.value, cMCFailInfo5);
        Map map6 = range;
        CMCFailInfo cMCFailInfo6 = popRequired;
        map6.put(cMCFailInfo6.value, cMCFailInfo6);
        Map map7 = range;
        CMCFailInfo cMCFailInfo7 = unsupportedExt;
        map7.put(cMCFailInfo7.value, cMCFailInfo7);
        Map map8 = range;
        CMCFailInfo cMCFailInfo8 = mustArchiveKeys;
        map8.put(cMCFailInfo8.value, cMCFailInfo8);
        Map map9 = range;
        CMCFailInfo cMCFailInfo9 = badIdentity;
        map9.put(cMCFailInfo9.value, cMCFailInfo9);
        Map map10 = range;
        CMCFailInfo cMCFailInfo10 = popRequired;
        map10.put(cMCFailInfo10.value, cMCFailInfo10);
        Map map11 = range;
        CMCFailInfo cMCFailInfo11 = popFailed;
        map11.put(cMCFailInfo11.value, cMCFailInfo11);
        Map map12 = range;
        CMCFailInfo cMCFailInfo12 = badCertId;
        map12.put(cMCFailInfo12.value, cMCFailInfo12);
        Map map13 = range;
        CMCFailInfo cMCFailInfo13 = popRequired;
        map13.put(cMCFailInfo13.value, cMCFailInfo13);
        Map map14 = range;
        CMCFailInfo cMCFailInfo14 = noKeyReuse;
        map14.put(cMCFailInfo14.value, cMCFailInfo14);
        Map map15 = range;
        CMCFailInfo cMCFailInfo15 = internalCAError;
        map15.put(cMCFailInfo15.value, cMCFailInfo15);
        Map map16 = range;
        CMCFailInfo cMCFailInfo16 = tryLater;
        map16.put(cMCFailInfo16.value, cMCFailInfo16);
        Map map17 = range;
        CMCFailInfo cMCFailInfo17 = authDataFail;
        map17.put(cMCFailInfo17.value, cMCFailInfo17);
    }

    private CMCFailInfo(ASN1Integer aSN1Integer) {
        this.value = aSN1Integer;
    }

    public static CMCFailInfo getInstance(Object obj) {
        if (obj instanceof CMCFailInfo) {
            return (CMCFailInfo) obj;
        }
        if (obj == null) {
            return null;
        }
        CMCFailInfo cMCFailInfo = (CMCFailInfo) range.get(ASN1Integer.getInstance(obj));
        if (cMCFailInfo != null) {
            return cMCFailInfo;
        }
        throw new IllegalArgumentException("unknown object in getInstance(): " + obj.getClass().getName());
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        return this.value;
    }
}
