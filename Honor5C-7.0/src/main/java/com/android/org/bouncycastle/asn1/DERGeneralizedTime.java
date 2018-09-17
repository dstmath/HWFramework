package com.android.org.bouncycastle.asn1;

import java.util.Date;

public class DERGeneralizedTime extends ASN1GeneralizedTime {
    DERGeneralizedTime(byte[] bytes) {
        super(bytes);
    }

    public DERGeneralizedTime(Date time) {
        super(time);
    }

    public DERGeneralizedTime(String time) {
        super(time);
    }
}
