package org.bouncycastle.crypto.parsers;

import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.crypto.KeyParser;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.util.io.Streams;

public class ECIESPublicKeyParser implements KeyParser {
    private ECDomainParameters ecParams;

    public ECIESPublicKeyParser(ECDomainParameters eCDomainParameters) {
        this.ecParams = eCDomainParameters;
    }

    @Override // org.bouncycastle.crypto.KeyParser
    public AsymmetricKeyParameter readKey(InputStream inputStream) throws IOException {
        byte[] bArr;
        int read = inputStream.read();
        if (read != 0) {
            if (read == 2 || read == 3) {
                bArr = new byte[(((this.ecParams.getCurve().getFieldSize() + 7) / 8) + 1)];
            } else if (read == 4 || read == 6 || read == 7) {
                bArr = new byte[((((this.ecParams.getCurve().getFieldSize() + 7) / 8) * 2) + 1)];
            } else {
                throw new IOException("Sender's public key has invalid point encoding 0x" + Integer.toString(read, 16));
            }
            bArr[0] = (byte) read;
            Streams.readFully(inputStream, bArr, 1, bArr.length - 1);
            return new ECPublicKeyParameters(this.ecParams.getCurve().decodePoint(bArr), this.ecParams);
        }
        throw new IOException("Sender's public key invalid.");
    }
}
