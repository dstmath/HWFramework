package org.bouncycastle.pkcs;

import java.io.OutputStream;
import org.bouncycastle.asn1.pkcs.MacData;
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.operator.MacCalculator;

class MacDataGenerator {
    private PKCS12MacCalculatorBuilder builder;

    MacDataGenerator(PKCS12MacCalculatorBuilder pKCS12MacCalculatorBuilder) {
        this.builder = pKCS12MacCalculatorBuilder;
    }

    public MacData build(char[] cArr, byte[] bArr) throws PKCSException {
        try {
            MacCalculator build = this.builder.build(cArr);
            OutputStream outputStream = build.getOutputStream();
            outputStream.write(bArr);
            outputStream.close();
            AlgorithmIdentifier algorithmIdentifier = build.getAlgorithmIdentifier();
            DigestInfo digestInfo = new DigestInfo(this.builder.getDigestAlgorithmIdentifier(), build.getMac());
            PKCS12PBEParams instance = PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters());
            return new MacData(digestInfo, instance.getIV(), instance.getIterations().intValue());
        } catch (Exception e) {
            throw new PKCSException("unable to process data: " + e.getMessage(), e);
        }
    }
}
