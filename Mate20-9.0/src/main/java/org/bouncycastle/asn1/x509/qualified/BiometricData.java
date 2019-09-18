package org.bouncycastle.asn1.x509.qualified;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class BiometricData extends ASN1Object {
    private ASN1OctetString biometricDataHash;
    private AlgorithmIdentifier hashAlgorithm;
    private DERIA5String sourceDataUri;
    private TypeOfBiometricData typeOfBiometricData;

    private BiometricData(ASN1Sequence aSN1Sequence) {
        Enumeration objects = aSN1Sequence.getObjects();
        this.typeOfBiometricData = TypeOfBiometricData.getInstance(objects.nextElement());
        this.hashAlgorithm = AlgorithmIdentifier.getInstance(objects.nextElement());
        this.biometricDataHash = ASN1OctetString.getInstance(objects.nextElement());
        if (objects.hasMoreElements()) {
            this.sourceDataUri = DERIA5String.getInstance(objects.nextElement());
        }
    }

    public BiometricData(TypeOfBiometricData typeOfBiometricData2, AlgorithmIdentifier algorithmIdentifier, ASN1OctetString aSN1OctetString) {
        this.typeOfBiometricData = typeOfBiometricData2;
        this.hashAlgorithm = algorithmIdentifier;
        this.biometricDataHash = aSN1OctetString;
        this.sourceDataUri = null;
    }

    public BiometricData(TypeOfBiometricData typeOfBiometricData2, AlgorithmIdentifier algorithmIdentifier, ASN1OctetString aSN1OctetString, DERIA5String dERIA5String) {
        this.typeOfBiometricData = typeOfBiometricData2;
        this.hashAlgorithm = algorithmIdentifier;
        this.biometricDataHash = aSN1OctetString;
        this.sourceDataUri = dERIA5String;
    }

    public static BiometricData getInstance(Object obj) {
        if (obj instanceof BiometricData) {
            return (BiometricData) obj;
        }
        if (obj != null) {
            return new BiometricData(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1OctetString getBiometricDataHash() {
        return this.biometricDataHash;
    }

    public AlgorithmIdentifier getHashAlgorithm() {
        return this.hashAlgorithm;
    }

    public DERIA5String getSourceDataUri() {
        return this.sourceDataUri;
    }

    public TypeOfBiometricData getTypeOfBiometricData() {
        return this.typeOfBiometricData;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.typeOfBiometricData);
        aSN1EncodableVector.add(this.hashAlgorithm);
        aSN1EncodableVector.add(this.biometricDataHash);
        if (this.sourceDataUri != null) {
            aSN1EncodableVector.add(this.sourceDataUri);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
