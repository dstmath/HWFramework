package org.bouncycastle.cert.crmf;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.cmp.CMPObjectIdentifiers;
import org.bouncycastle.asn1.cmp.PBMParameter;
import org.bouncycastle.asn1.iana.IANAObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.MacCalculator;
import org.bouncycastle.operator.RuntimeOperatorException;
import org.bouncycastle.util.Strings;

public class PKMACBuilder {
    /* access modifiers changed from: private */
    public PKMACValuesCalculator calculator;
    private int iterationCount;
    private AlgorithmIdentifier mac;
    private int maxIterations;
    private AlgorithmIdentifier owf;
    private PBMParameter parameters;
    private SecureRandom random;
    private int saltLength;

    private PKMACBuilder(AlgorithmIdentifier algorithmIdentifier, int i, AlgorithmIdentifier algorithmIdentifier2, PKMACValuesCalculator pKMACValuesCalculator) {
        this.saltLength = 20;
        this.owf = algorithmIdentifier;
        this.iterationCount = i;
        this.mac = algorithmIdentifier2;
        this.calculator = pKMACValuesCalculator;
    }

    public PKMACBuilder(PKMACValuesCalculator pKMACValuesCalculator) {
        this(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1), 1000, new AlgorithmIdentifier(IANAObjectIdentifiers.hmacSHA1, DERNull.INSTANCE), pKMACValuesCalculator);
    }

    public PKMACBuilder(PKMACValuesCalculator pKMACValuesCalculator, int i) {
        this.saltLength = 20;
        this.maxIterations = i;
        this.calculator = pKMACValuesCalculator;
    }

    private void checkIterationCountCeiling(int i) {
        if (this.maxIterations > 0 && i > this.maxIterations) {
            throw new IllegalArgumentException("iteration count exceeds limit (" + i + " > " + this.maxIterations + ")");
        }
    }

    private MacCalculator genCalculator(final PBMParameter pBMParameter, char[] cArr) throws CRMFException {
        byte[] uTF8ByteArray = Strings.toUTF8ByteArray(cArr);
        byte[] octets = pBMParameter.getSalt().getOctets();
        final byte[] bArr = new byte[(uTF8ByteArray.length + octets.length)];
        System.arraycopy(uTF8ByteArray, 0, bArr, 0, uTF8ByteArray.length);
        System.arraycopy(octets, 0, bArr, uTF8ByteArray.length, octets.length);
        this.calculator.setup(pBMParameter.getOwf(), pBMParameter.getMac());
        int intValue = pBMParameter.getIterationCount().getValue().intValue();
        do {
            bArr = this.calculator.calculateDigest(bArr);
            intValue--;
        } while (intValue > 0);
        return new MacCalculator() {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();

            public AlgorithmIdentifier getAlgorithmIdentifier() {
                return new AlgorithmIdentifier(CMPObjectIdentifiers.passwordBasedMac, pBMParameter);
            }

            public GenericKey getKey() {
                return new GenericKey(getAlgorithmIdentifier(), bArr);
            }

            public byte[] getMac() {
                try {
                    return PKMACBuilder.this.calculator.calculateMac(bArr, this.bOut.toByteArray());
                } catch (CRMFException e) {
                    throw new RuntimeOperatorException("exception calculating mac: " + e.getMessage(), e);
                }
            }

            public OutputStream getOutputStream() {
                return this.bOut;
            }
        };
    }

    public MacCalculator build(char[] cArr) throws CRMFException {
        if (this.parameters != null) {
            return genCalculator(this.parameters, cArr);
        }
        byte[] bArr = new byte[this.saltLength];
        if (this.random == null) {
            this.random = new SecureRandom();
        }
        this.random.nextBytes(bArr);
        return genCalculator(new PBMParameter(bArr, this.owf, this.iterationCount, this.mac), cArr);
    }

    public PKMACBuilder setIterationCount(int i) {
        if (i >= 100) {
            checkIterationCountCeiling(i);
            this.iterationCount = i;
            return this;
        }
        throw new IllegalArgumentException("iteration count must be at least 100");
    }

    public PKMACBuilder setParameters(PBMParameter pBMParameter) {
        checkIterationCountCeiling(pBMParameter.getIterationCount().getValue().intValue());
        this.parameters = pBMParameter;
        return this;
    }

    public PKMACBuilder setSaltLength(int i) {
        if (i >= 8) {
            this.saltLength = i;
            return this;
        }
        throw new IllegalArgumentException("salt length must be at least 8 bytes");
    }

    public PKMACBuilder setSecureRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
        return this;
    }
}
