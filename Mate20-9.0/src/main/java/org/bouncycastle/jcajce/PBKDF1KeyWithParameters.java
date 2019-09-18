package org.bouncycastle.jcajce;

import javax.crypto.interfaces.PBEKey;
import org.bouncycastle.crypto.CharToByteConverter;
import org.bouncycastle.util.Arrays;

public class PBKDF1KeyWithParameters extends PBKDF1Key implements PBEKey {
    private final int iterationCount;
    private final byte[] salt;

    public PBKDF1KeyWithParameters(char[] cArr, CharToByteConverter charToByteConverter, byte[] bArr, int i) {
        super(cArr, charToByteConverter);
        this.salt = Arrays.clone(bArr);
        this.iterationCount = i;
    }

    public int getIterationCount() {
        return this.iterationCount;
    }

    public byte[] getSalt() {
        return this.salt;
    }
}
