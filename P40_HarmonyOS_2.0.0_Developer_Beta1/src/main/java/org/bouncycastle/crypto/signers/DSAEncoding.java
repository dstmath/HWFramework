package org.bouncycastle.crypto.signers;

import java.io.IOException;
import java.math.BigInteger;

public interface DSAEncoding {
    BigInteger[] decode(BigInteger bigInteger, byte[] bArr) throws IOException;

    byte[] encode(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3) throws IOException;
}
