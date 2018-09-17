package com.android.org.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

public class BEROctetString extends ASN1OctetString {
    private static final int MAX_LENGTH = 1000;
    private ASN1OctetString[] octs;

    private static byte[] toBytes(ASN1OctetString[] octs) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        int i = 0;
        while (i != octs.length) {
            try {
                bOut.write(octs[i].getOctets());
                i++;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(octs[i].getClass().getName() + " found in input should only contain DEROctetString");
            } catch (IOException e2) {
                throw new IllegalArgumentException("exception converting octets " + e2.toString());
            }
        }
        return bOut.toByteArray();
    }

    public BEROctetString(byte[] string) {
        super(string);
    }

    public BEROctetString(ASN1OctetString[] octs) {
        super(toBytes(octs));
        this.octs = octs;
    }

    public byte[] getOctets() {
        return this.string;
    }

    public Enumeration getObjects() {
        if (this.octs == null) {
            return generateOcts().elements();
        }
        return new Enumeration() {
            int counter = 0;

            public boolean hasMoreElements() {
                return this.counter < BEROctetString.this.octs.length;
            }

            public Object nextElement() {
                ASN1OctetString[] -get0 = BEROctetString.this.octs;
                int i = this.counter;
                this.counter = i + 1;
                return -get0[i];
            }
        };
    }

    private Vector generateOcts() {
        Vector vec = new Vector();
        for (int i = 0; i < this.string.length; i += MAX_LENGTH) {
            int end;
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

    boolean isConstructed() {
        return true;
    }

    int encodedLength() throws IOException {
        int length = 0;
        Enumeration e = getObjects();
        while (e.hasMoreElements()) {
            length += ((ASN1Encodable) e.nextElement()).toASN1Primitive().encodedLength();
        }
        return (length + 2) + 2;
    }

    public void encode(ASN1OutputStream out) throws IOException {
        out.write(36);
        out.write(128);
        Enumeration e = getObjects();
        while (e.hasMoreElements()) {
            out.writeObject((ASN1Encodable) e.nextElement());
        }
        out.write(0);
        out.write(0);
    }

    static BEROctetString fromSequence(ASN1Sequence seq) {
        ASN1OctetString[] v = new ASN1OctetString[seq.size()];
        Enumeration e = seq.getObjects();
        int index = 0;
        while (e.hasMoreElements()) {
            int index2 = index + 1;
            v[index] = (ASN1OctetString) e.nextElement();
            index = index2;
        }
        return new BEROctetString(v);
    }
}
