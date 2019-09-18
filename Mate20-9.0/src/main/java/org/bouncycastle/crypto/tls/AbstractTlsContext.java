package org.bouncycastle.crypto.tls;

import java.security.SecureRandom;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.prng.DigestRandomGenerator;
import org.bouncycastle.crypto.prng.RandomGenerator;
import org.bouncycastle.util.Times;

abstract class AbstractTlsContext implements TlsContext {
    private static long counter = Times.nanoTime();
    private ProtocolVersion clientVersion = null;
    private RandomGenerator nonceRandom;
    private SecureRandom secureRandom;
    private SecurityParameters securityParameters;
    private ProtocolVersion serverVersion = null;
    private TlsSession session = null;
    private Object userObject = null;

    AbstractTlsContext(SecureRandom secureRandom2, SecurityParameters securityParameters2) {
        Digest createHash = TlsUtils.createHash(4);
        byte[] bArr = new byte[createHash.getDigestSize()];
        secureRandom2.nextBytes(bArr);
        this.nonceRandom = new DigestRandomGenerator(createHash);
        this.nonceRandom.addSeedMaterial(nextCounterValue());
        this.nonceRandom.addSeedMaterial(Times.nanoTime());
        this.nonceRandom.addSeedMaterial(bArr);
        this.secureRandom = secureRandom2;
        this.securityParameters = securityParameters2;
    }

    private static synchronized long nextCounterValue() {
        long j;
        synchronized (AbstractTlsContext.class) {
            j = counter + 1;
            counter = j;
        }
        return j;
    }

    public byte[] exportKeyingMaterial(String str, byte[] bArr, int i) {
        if (bArr == null || TlsUtils.isValidUint16(bArr.length)) {
            SecurityParameters securityParameters2 = getSecurityParameters();
            byte[] clientRandom = securityParameters2.getClientRandom();
            byte[] serverRandom = securityParameters2.getServerRandom();
            int length = clientRandom.length + serverRandom.length;
            if (bArr != null) {
                length += 2 + bArr.length;
            }
            byte[] bArr2 = new byte[length];
            System.arraycopy(clientRandom, 0, bArr2, 0, clientRandom.length);
            int length2 = clientRandom.length + 0;
            System.arraycopy(serverRandom, 0, bArr2, length2, serverRandom.length);
            int length3 = length2 + serverRandom.length;
            if (bArr != null) {
                TlsUtils.writeUint16(bArr.length, bArr2, length3);
                int i2 = length3 + 2;
                System.arraycopy(bArr, 0, bArr2, i2, bArr.length);
                length3 = i2 + bArr.length;
            }
            if (length3 == length) {
                return TlsUtils.PRF(this, securityParameters2.getMasterSecret(), str, bArr2, i);
            }
            throw new IllegalStateException("error in calculation of seed for export");
        }
        throw new IllegalArgumentException("'context_value' must have length less than 2^16 (or be null)");
    }

    public ProtocolVersion getClientVersion() {
        return this.clientVersion;
    }

    public RandomGenerator getNonceRandomGenerator() {
        return this.nonceRandom;
    }

    public TlsSession getResumableSession() {
        return this.session;
    }

    public SecureRandom getSecureRandom() {
        return this.secureRandom;
    }

    public SecurityParameters getSecurityParameters() {
        return this.securityParameters;
    }

    public ProtocolVersion getServerVersion() {
        return this.serverVersion;
    }

    public Object getUserObject() {
        return this.userObject;
    }

    /* access modifiers changed from: package-private */
    public void setClientVersion(ProtocolVersion protocolVersion) {
        this.clientVersion = protocolVersion;
    }

    /* access modifiers changed from: package-private */
    public void setResumableSession(TlsSession tlsSession) {
        this.session = tlsSession;
    }

    /* access modifiers changed from: package-private */
    public void setServerVersion(ProtocolVersion protocolVersion) {
        this.serverVersion = protocolVersion;
    }

    public void setUserObject(Object obj) {
        this.userObject = obj;
    }
}
