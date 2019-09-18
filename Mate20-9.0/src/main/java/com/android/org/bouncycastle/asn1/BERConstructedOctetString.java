package com.android.org.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

public class BERConstructedOctetString extends BEROctetString {
    private static final int MAX_LENGTH = 1000;
    private Vector octs;

    private static byte[] toBytes(Vector octs2) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        int i = 0;
        while (i != octs2.size()) {
            try {
                bOut.write(((DEROctetString) octs2.elementAt(i)).getOctets());
                i++;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(octs2.elementAt(i).getClass().getName() + " found in input should only contain DEROctetString");
            } catch (IOException e2) {
                throw new IllegalArgumentException("exception converting octets " + e2.toString());
            }
        }
        return bOut.toByteArray();
    }

    public BERConstructedOctetString(byte[] string) {
        super(string);
    }

    public BERConstructedOctetString(Vector octs2) {
        super(toBytes(octs2));
        this.octs = octs2;
    }

    public BERConstructedOctetString(ASN1Primitive obj) {
        super(toByteArray(obj));
    }

    private static byte[] toByteArray(ASN1Primitive obj) {
        try {
            return obj.getEncoded();
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to encode object");
        }
    }

    public BERConstructedOctetString(ASN1Encodable obj) {
        this(obj.toASN1Primitive());
    }

    public byte[] getOctets() {
        return this.string;
    }

    public Enumeration getObjects() {
        if (this.octs == null) {
            return generateOcts().elements();
        }
        return this.octs.elements();
    }

    private Vector generateOcts() {
        int end;
        Vector vec = new Vector();
        for (int i = 0; i < this.string.length; i += MAX_LENGTH) {
            if (i + MAX_LENGTH > this.string.length) {
                end = this.string.length;
            } else {
                end = i + MAX_LENGTH;
            }
            byte[] nStr = new byte[(end - i)];
            System.arraycopy(this.string, i, nStr, 0, nStr.length);
            vec.addElement(new DEROctetString(nStr));
        }
        return vec;
    }

    public static BEROctetString fromSequence(ASN1Sequence seq) {
        Vector v = new Vector();
        Enumeration e = seq.getObjects();
        while (e.hasMoreElements()) {
            v.addElement(e.nextElement());
        }
        return new BERConstructedOctetString(v);
    }
}
