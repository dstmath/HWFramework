package org.bouncycastle.crypto.tls;

import org.bouncycastle.util.Arrays;

public class SecurityParameters {
    int cipherSuite = -1;
    byte[] clientRandom = null;
    short compressionAlgorithm = 0;
    boolean encryptThenMAC = false;
    int entity = -1;
    boolean extendedMasterSecret = false;
    byte[] masterSecret = null;
    short maxFragmentLength = -1;
    int prfAlgorithm = -1;
    byte[] pskIdentity = null;
    byte[] serverRandom = null;
    byte[] sessionHash = null;
    byte[] srpIdentity = null;
    boolean truncatedHMac = false;
    int verifyDataLength = -1;

    /* access modifiers changed from: package-private */
    public void clear() {
        if (this.masterSecret != null) {
            Arrays.fill(this.masterSecret, (byte) 0);
            this.masterSecret = null;
        }
    }

    public int getCipherSuite() {
        return this.cipherSuite;
    }

    public byte[] getClientRandom() {
        return this.clientRandom;
    }

    public short getCompressionAlgorithm() {
        return this.compressionAlgorithm;
    }

    public int getEntity() {
        return this.entity;
    }

    public byte[] getMasterSecret() {
        return this.masterSecret;
    }

    public byte[] getPSKIdentity() {
        return this.pskIdentity;
    }

    public int getPrfAlgorithm() {
        return this.prfAlgorithm;
    }

    public byte[] getPskIdentity() {
        return this.pskIdentity;
    }

    public byte[] getSRPIdentity() {
        return this.srpIdentity;
    }

    public byte[] getServerRandom() {
        return this.serverRandom;
    }

    public byte[] getSessionHash() {
        return this.sessionHash;
    }

    public int getVerifyDataLength() {
        return this.verifyDataLength;
    }
}
