package org.bouncycastle.asn1;

import java.io.IOException;
import java.io.InputStream;

/* access modifiers changed from: package-private */
public class ConstructedOctetStream extends InputStream {
    private InputStream _currentStream;
    private boolean _first = true;
    private final ASN1StreamParser _parser;

    ConstructedOctetStream(ASN1StreamParser aSN1StreamParser) {
        this._parser = aSN1StreamParser;
    }

    private ASN1OctetStringParser getNextParser() throws IOException {
        ASN1Encodable readObject = this._parser.readObject();
        if (readObject == null) {
            return null;
        }
        if (readObject instanceof ASN1OctetStringParser) {
            return (ASN1OctetStringParser) readObject;
        }
        throw new IOException("unknown object encountered: " + readObject.getClass());
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x001e  */
    @Override // java.io.InputStream
    public int read() throws IOException {
        if (this._currentStream == null) {
            if (!this._first || (r0 = getNextParser()) == null) {
                return -1;
            }
            this._first = false;
            this._currentStream = r0.getOctetStream();
        }
        int read = this._currentStream.read();
        if (read < 0) {
            ASN1OctetStringParser aSN1OctetStringParser = getNextParser();
            if (aSN1OctetStringParser == null) {
                this._currentStream = null;
                return -1;
            }
            this._currentStream = aSN1OctetStringParser.getOctetStream();
            int read2 = this._currentStream.read();
            if (read2 < 0) {
            }
        }
        return read2;
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x002a A[EDGE_INSN: B:20:0x002a->B:15:0x002a ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0029 A[SYNTHETIC] */
    @Override // java.io.InputStream
    public int read(byte[] r6, int r7, int r8) throws java.io.IOException {
        /*
            r5 = this;
            java.io.InputStream r0 = r5._currentStream
            r1 = 0
            r2 = -1
            if (r0 != 0) goto L_0x001a
            boolean r0 = r5._first
            if (r0 != 0) goto L_0x000b
            return r2
        L_0x000b:
            org.bouncycastle.asn1.ASN1OctetStringParser r0 = r5.getNextParser()
            if (r0 != 0) goto L_0x0012
            return r2
        L_0x0012:
            r5._first = r1
        L_0x0014:
            java.io.InputStream r0 = r0.getOctetStream()
            r5._currentStream = r0
        L_0x001a:
            java.io.InputStream r0 = r5._currentStream
            int r3 = r7 + r1
            int r4 = r8 - r1
            int r0 = r0.read(r6, r3, r4)
            if (r0 < 0) goto L_0x002a
            int r1 = r1 + r0
            if (r1 != r8) goto L_0x001a
            return r1
        L_0x002a:
            org.bouncycastle.asn1.ASN1OctetStringParser r0 = r5.getNextParser()
            if (r0 != 0) goto L_0x0014
            r6 = 0
            r5._currentStream = r6
            r6 = 1
            if (r1 >= r6) goto L_0x0037
            r1 = r2
        L_0x0037:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: org.bouncycastle.asn1.ConstructedOctetStream.read(byte[], int, int):int");
    }
}
