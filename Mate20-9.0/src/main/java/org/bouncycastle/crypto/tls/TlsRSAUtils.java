package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.util.Arrays;

public class TlsRSAUtils {
    public static byte[] generateEncryptedPreMasterSecret(TlsContext tlsContext, RSAKeyParameters rSAKeyParameters, OutputStream outputStream) throws IOException {
        byte[] bArr = new byte[48];
        tlsContext.getSecureRandom().nextBytes(bArr);
        TlsUtils.writeVersion(tlsContext.getClientVersion(), bArr, 0);
        PKCS1Encoding pKCS1Encoding = new PKCS1Encoding(new RSABlindedEngine());
        pKCS1Encoding.init(true, new ParametersWithRandom(rSAKeyParameters, tlsContext.getSecureRandom()));
        try {
            byte[] processBlock = pKCS1Encoding.processBlock(bArr, 0, bArr.length);
            if (TlsUtils.isSSL(tlsContext)) {
                outputStream.write(processBlock);
                return bArr;
            }
            TlsUtils.writeOpaque16(processBlock, outputStream);
            return bArr;
        } catch (InvalidCipherTextException e) {
            throw new TlsFatalAlert(80, e);
        }
    }

    public static byte[] safeDecryptPreMasterSecret(TlsContext tlsContext, RSAKeyParameters rSAKeyParameters, byte[] bArr) {
        byte[] bArr2;
        ProtocolVersion clientVersion = tlsContext.getClientVersion();
        byte[] bArr3 = new byte[48];
        tlsContext.getSecureRandom().nextBytes(bArr3);
        try {
            PKCS1Encoding pKCS1Encoding = new PKCS1Encoding((AsymmetricBlockCipher) new RSABlindedEngine(), bArr3);
            pKCS1Encoding.init(false, new ParametersWithRandom(rSAKeyParameters, tlsContext.getSecureRandom()));
            bArr2 = pKCS1Encoding.processBlock(bArr, 0, bArr.length);
        } catch (Exception e) {
            bArr2 = Arrays.clone(bArr3);
        }
        byte majorVersion = (clientVersion.getMajorVersion() ^ (bArr2[0] & 255)) | (clientVersion.getMinorVersion() ^ (bArr2[1] & 255));
        byte b = majorVersion | (majorVersion >> 1);
        byte b2 = b | (b >> 2);
        int i = ~(((b2 | (b2 >> 4)) & 1) - 1);
        for (int i2 = 0; i2 < 48; i2++) {
            bArr2[i2] = (byte) ((bArr2[i2] & (~i)) | (bArr3[i2] & i));
        }
        return bArr2;
    }
}
