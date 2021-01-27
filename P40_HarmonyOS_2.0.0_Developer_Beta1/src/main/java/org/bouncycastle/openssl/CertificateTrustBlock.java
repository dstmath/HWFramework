package org.bouncycastle.openssl;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;

public class CertificateTrustBlock {
    private String alias;
    private ASN1Sequence prohibitions;
    private ASN1Sequence uses;

    public CertificateTrustBlock(String str, Set<ASN1ObjectIdentifier> set) {
        this(str, set, null);
    }

    public CertificateTrustBlock(String str, Set<ASN1ObjectIdentifier> set, Set<ASN1ObjectIdentifier> set2) {
        this.alias = str;
        this.uses = toSequence(set);
        this.prohibitions = toSequence(set2);
    }

    public CertificateTrustBlock(Set<ASN1ObjectIdentifier> set) {
        this(null, set, null);
    }

    CertificateTrustBlock(byte[] bArr) {
        Enumeration objects = ASN1Sequence.getInstance(bArr).getObjects();
        while (objects.hasMoreElements()) {
            ASN1Encodable aSN1Encodable = (ASN1Encodable) objects.nextElement();
            if (aSN1Encodable instanceof ASN1Sequence) {
                this.uses = ASN1Sequence.getInstance(aSN1Encodable);
            } else if (aSN1Encodable instanceof ASN1TaggedObject) {
                this.prohibitions = ASN1Sequence.getInstance((ASN1TaggedObject) aSN1Encodable, false);
            } else if (aSN1Encodable instanceof DERUTF8String) {
                this.alias = DERUTF8String.getInstance(aSN1Encodable).getString();
            }
        }
    }

    private ASN1Sequence toSequence(Set<ASN1ObjectIdentifier> set) {
        if (set == null || set.isEmpty()) {
            return null;
        }
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        for (ASN1ObjectIdentifier aSN1ObjectIdentifier : set) {
            aSN1EncodableVector.add(aSN1ObjectIdentifier);
        }
        return new DERSequence(aSN1EncodableVector);
    }

    private Set<ASN1ObjectIdentifier> toSet(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence == null) {
            return Collections.EMPTY_SET;
        }
        HashSet hashSet = new HashSet(aSN1Sequence.size());
        Enumeration objects = aSN1Sequence.getObjects();
        while (objects.hasMoreElements()) {
            hashSet.add(ASN1ObjectIdentifier.getInstance(objects.nextElement()));
        }
        return hashSet;
    }

    public String getAlias() {
        return this.alias;
    }

    public Set<ASN1ObjectIdentifier> getProhibitions() {
        return toSet(this.prohibitions);
    }

    public Set<ASN1ObjectIdentifier> getUses() {
        return toSet(this.uses);
    }

    /* access modifiers changed from: package-private */
    public ASN1Sequence toASN1Sequence() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        ASN1Sequence aSN1Sequence = this.uses;
        if (aSN1Sequence != null) {
            aSN1EncodableVector.add(aSN1Sequence);
        }
        ASN1Sequence aSN1Sequence2 = this.prohibitions;
        if (aSN1Sequence2 != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, aSN1Sequence2));
        }
        String str = this.alias;
        if (str != null) {
            aSN1EncodableVector.add(new DERUTF8String(str));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
