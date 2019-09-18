package android.security;

import java.security.KeyPair;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AttestedKeyPair {
    private final Certificate[] mAttestationRecord;
    private final KeyPair mKeyPair;

    public AttestedKeyPair(KeyPair keyPair, Certificate[] attestationRecord) {
        this.mKeyPair = keyPair;
        this.mAttestationRecord = attestationRecord;
    }

    public KeyPair getKeyPair() {
        return this.mKeyPair;
    }

    public List<Certificate> getAttestationRecord() {
        if (this.mAttestationRecord == null) {
            return new ArrayList();
        }
        return Arrays.asList(this.mAttestationRecord);
    }
}
