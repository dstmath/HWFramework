package org.bouncycastle.crypto.tls;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ocsp.ResponderID;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.util.io.Streams;

public class OCSPStatusRequest {
    protected Extensions requestExtensions;
    protected Vector responderIDList;

    public OCSPStatusRequest(Vector vector, Extensions extensions) {
        this.responderIDList = vector;
        this.requestExtensions = extensions;
    }

    public static OCSPStatusRequest parse(InputStream inputStream) throws IOException {
        Vector vector = new Vector();
        int readUint16 = TlsUtils.readUint16(inputStream);
        if (readUint16 > 0) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(TlsUtils.readFully(readUint16, inputStream));
            do {
                vector.addElement(ResponderID.getInstance(TlsUtils.readDERObject(TlsUtils.readOpaque16(byteArrayInputStream))));
            } while (byteArrayInputStream.available() > 0);
        }
        Extensions extensions = null;
        int readUint162 = TlsUtils.readUint16(inputStream);
        if (readUint162 > 0) {
            extensions = Extensions.getInstance(TlsUtils.readDERObject(TlsUtils.readFully(readUint162, inputStream)));
        }
        return new OCSPStatusRequest(vector, extensions);
    }

    public void encode(OutputStream outputStream) throws IOException {
        if (this.responderIDList == null || this.responderIDList.isEmpty()) {
            TlsUtils.writeUint16(0, outputStream);
        } else {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            for (int i = 0; i < this.responderIDList.size(); i++) {
                TlsUtils.writeOpaque16(((ResponderID) this.responderIDList.elementAt(i)).getEncoded(ASN1Encoding.DER), byteArrayOutputStream);
            }
            TlsUtils.checkUint16(byteArrayOutputStream.size());
            TlsUtils.writeUint16(byteArrayOutputStream.size(), outputStream);
            Streams.writeBufTo(byteArrayOutputStream, outputStream);
        }
        if (this.requestExtensions == null) {
            TlsUtils.writeUint16(0, outputStream);
            return;
        }
        byte[] encoded = this.requestExtensions.getEncoded(ASN1Encoding.DER);
        TlsUtils.checkUint16(encoded.length);
        TlsUtils.writeUint16(encoded.length, outputStream);
        outputStream.write(encoded);
    }

    public Extensions getRequestExtensions() {
        return this.requestExtensions;
    }

    public Vector getResponderIDList() {
        return this.responderIDList;
    }
}
