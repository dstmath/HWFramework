package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.DERVisibleString;

public class DisplayText extends ASN1Object implements ASN1Choice {
    public static final int CONTENT_TYPE_BMPSTRING = 1;
    public static final int CONTENT_TYPE_IA5STRING = 0;
    public static final int CONTENT_TYPE_UTF8STRING = 2;
    public static final int CONTENT_TYPE_VISIBLESTRING = 3;
    public static final int DISPLAY_TEXT_MAXIMUM_SIZE = 200;
    int contentType;
    ASN1String contents;

    public DisplayText(int i, String str) {
        ASN1String dERIA5String;
        str = str.length() > 200 ? str.substring(0, DISPLAY_TEXT_MAXIMUM_SIZE) : str;
        this.contentType = i;
        switch (i) {
            case 0:
                dERIA5String = new DERIA5String(str);
                break;
            case 1:
                dERIA5String = new DERBMPString(str);
                break;
            case 2:
                dERIA5String = new DERUTF8String(str);
                break;
            case 3:
                dERIA5String = new DERVisibleString(str);
                break;
            default:
                dERIA5String = new DERUTF8String(str);
                break;
        }
        this.contents = dERIA5String;
    }

    public DisplayText(String str) {
        str = str.length() > 200 ? str.substring(0, DISPLAY_TEXT_MAXIMUM_SIZE) : str;
        this.contentType = 2;
        this.contents = new DERUTF8String(str);
    }

    private DisplayText(ASN1String aSN1String) {
        int i;
        this.contents = aSN1String;
        if (aSN1String instanceof DERUTF8String) {
            i = 2;
        } else if (aSN1String instanceof DERBMPString) {
            i = 1;
        } else if (aSN1String instanceof DERIA5String) {
            i = 0;
        } else if (aSN1String instanceof DERVisibleString) {
            i = 3;
        } else {
            throw new IllegalArgumentException("unknown STRING type in DisplayText");
        }
        this.contentType = i;
    }

    public static DisplayText getInstance(Object obj) {
        if (obj instanceof ASN1String) {
            return new DisplayText((ASN1String) obj);
        }
        if (obj == null || (obj instanceof DisplayText)) {
            return (DisplayText) obj;
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    public static DisplayText getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(aSN1TaggedObject.getObject());
    }

    public String getString() {
        return this.contents.getString();
    }

    public ASN1Primitive toASN1Primitive() {
        return (ASN1Primitive) this.contents;
    }
}
