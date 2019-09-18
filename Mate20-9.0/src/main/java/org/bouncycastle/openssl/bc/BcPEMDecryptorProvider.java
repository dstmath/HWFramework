package org.bouncycastle.openssl.bc;

import org.bouncycastle.openssl.PEMDecryptor;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PasswordException;

public class BcPEMDecryptorProvider implements PEMDecryptorProvider {
    /* access modifiers changed from: private */
    public final char[] password;

    public BcPEMDecryptorProvider(char[] cArr) {
        this.password = cArr;
    }

    public PEMDecryptor get(final String str) {
        return new PEMDecryptor() {
            public byte[] decrypt(byte[] bArr, byte[] bArr2) throws PEMException {
                if (BcPEMDecryptorProvider.this.password != null) {
                    return PEMUtilities.crypt(false, bArr, BcPEMDecryptorProvider.this.password, str, bArr2);
                }
                throw new PasswordException("Password is null, but a password is required");
            }
        };
    }
}
