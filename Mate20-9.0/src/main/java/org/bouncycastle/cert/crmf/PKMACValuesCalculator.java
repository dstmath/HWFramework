package org.bouncycastle.cert.crmf;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public interface PKMACValuesCalculator {
    byte[] calculateDigest(byte[] bArr) throws CRMFException;

    byte[] calculateMac(byte[] bArr, byte[] bArr2) throws CRMFException;

    void setup(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2) throws CRMFException;
}
