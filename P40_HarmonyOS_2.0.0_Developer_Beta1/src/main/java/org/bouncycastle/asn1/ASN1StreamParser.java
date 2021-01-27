package org.bouncycastle.asn1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ASN1StreamParser {
    private final InputStream _in;
    private final int _limit;
    private final byte[][] tmpBuffers;

    public ASN1StreamParser(InputStream inputStream) {
        this(inputStream, StreamUtil.findLimit(inputStream));
    }

    public ASN1StreamParser(InputStream inputStream, int i) {
        this._in = inputStream;
        this._limit = i;
        this.tmpBuffers = new byte[11][];
    }

    public ASN1StreamParser(byte[] bArr) {
        this(new ByteArrayInputStream(bArr), bArr.length);
    }

    private void set00Check(boolean z) {
        InputStream inputStream = this._in;
        if (inputStream instanceof IndefiniteLengthInputStream) {
            ((IndefiniteLengthInputStream) inputStream).setEofOn00(z);
        }
    }

    /* access modifiers changed from: package-private */
    public ASN1Encodable readImplicit(boolean z, int i) throws IOException {
        InputStream inputStream = this._in;
        if (!(inputStream instanceof IndefiniteLengthInputStream)) {
            if (z) {
                if (i == 4) {
                    return new BEROctetStringParser(this);
                }
                if (i == 16) {
                    return new DLSequenceParser(this);
                }
                if (i == 17) {
                    return new DLSetParser(this);
                }
            } else if (i == 4) {
                return new DEROctetStringParser((DefiniteLengthInputStream) inputStream);
            } else {
                if (i == 16) {
                    throw new ASN1Exception("sets must use constructed encoding (see X.690 8.11.1/8.12.1)");
                } else if (i == 17) {
                    throw new ASN1Exception("sequences must use constructed encoding (see X.690 8.9.1/8.10.1)");
                }
            }
            throw new ASN1Exception("implicit tagging not implemented");
        } else if (z) {
            return readIndef(i);
        } else {
            throw new IOException("indefinite-length primitive encoding encountered");
        }
    }

    /* access modifiers changed from: package-private */
    public ASN1Encodable readIndef(int i) throws IOException {
        if (i == 4) {
            return new BEROctetStringParser(this);
        }
        if (i == 8) {
            return new DERExternalParser(this);
        }
        if (i == 16) {
            return new BERSequenceParser(this);
        }
        if (i == 17) {
            return new BERSetParser(this);
        }
        throw new ASN1Exception("unknown BER object encountered: 0x" + Integer.toHexString(i));
    }

    public ASN1Encodable readObject() throws IOException {
        int read = this._in.read();
        if (read == -1) {
            return null;
        }
        boolean z = false;
        set00Check(false);
        int readTagNumber = ASN1InputStream.readTagNumber(this._in, read);
        boolean z2 = (read & 32) != 0;
        InputStream inputStream = this._in;
        int i = this._limit;
        if (readTagNumber == 4 || readTagNumber == 16 || readTagNumber == 17 || readTagNumber == 8) {
            z = true;
        }
        int readLength = ASN1InputStream.readLength(inputStream, i, z);
        if (readLength >= 0) {
            DefiniteLengthInputStream definiteLengthInputStream = new DefiniteLengthInputStream(this._in, readLength, this._limit);
            if ((read & 64) != 0) {
                return new DLApplicationSpecific(z2, readTagNumber, definiteLengthInputStream.toByteArray());
            }
            if ((read & 128) != 0) {
                return new BERTaggedObjectParser(z2, readTagNumber, new ASN1StreamParser(definiteLengthInputStream));
            }
            if (z2) {
                if (readTagNumber == 4) {
                    return new BEROctetStringParser(new ASN1StreamParser(definiteLengthInputStream));
                }
                if (readTagNumber == 8) {
                    return new DERExternalParser(new ASN1StreamParser(definiteLengthInputStream));
                }
                if (readTagNumber == 16) {
                    return new DLSequenceParser(new ASN1StreamParser(definiteLengthInputStream));
                }
                if (readTagNumber == 17) {
                    return new DLSetParser(new ASN1StreamParser(definiteLengthInputStream));
                }
                throw new IOException("unknown tag " + readTagNumber + " encountered");
            } else if (readTagNumber == 4) {
                return new DEROctetStringParser(definiteLengthInputStream);
            } else {
                try {
                    return ASN1InputStream.createPrimitiveDERObject(readTagNumber, definiteLengthInputStream, this.tmpBuffers);
                } catch (IllegalArgumentException e) {
                    throw new ASN1Exception("corrupted stream detected", e);
                }
            }
        } else if (z2) {
            ASN1StreamParser aSN1StreamParser = new ASN1StreamParser(new IndefiniteLengthInputStream(this._in, this._limit), this._limit);
            return (read & 64) != 0 ? new BERApplicationSpecificParser(readTagNumber, aSN1StreamParser) : (read & 128) != 0 ? new BERTaggedObjectParser(true, readTagNumber, aSN1StreamParser) : aSN1StreamParser.readIndef(readTagNumber);
        } else {
            throw new IOException("indefinite-length primitive encoding encountered");
        }
    }

    /* access modifiers changed from: package-private */
    public ASN1Primitive readTaggedObject(boolean z, int i) throws IOException {
        if (!z) {
            return new DLTaggedObject(false, i, new DEROctetString(((DefiniteLengthInputStream) this._in).toByteArray()));
        }
        ASN1EncodableVector readVector = readVector();
        return this._in instanceof IndefiniteLengthInputStream ? readVector.size() == 1 ? new BERTaggedObject(true, i, readVector.get(0)) : new BERTaggedObject(false, i, BERFactory.createSequence(readVector)) : readVector.size() == 1 ? new DLTaggedObject(true, i, readVector.get(0)) : new DLTaggedObject(false, i, DLFactory.createSequence(readVector));
    }

    /* access modifiers changed from: package-private */
    public ASN1EncodableVector readVector() throws IOException {
        ASN1Encodable readObject = readObject();
        if (readObject == null) {
            return new ASN1EncodableVector(0);
        }
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        do {
            aSN1EncodableVector.add(readObject instanceof InMemoryRepresentable ? ((InMemoryRepresentable) readObject).getLoadedObject() : readObject.toASN1Primitive());
            readObject = readObject();
        } while (readObject != null);
        return aSN1EncodableVector;
    }
}
