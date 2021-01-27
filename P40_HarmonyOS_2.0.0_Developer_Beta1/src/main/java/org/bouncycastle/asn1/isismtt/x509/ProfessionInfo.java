package org.bouncycastle.asn1.isismtt.x509;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x500.DirectoryString;

public class ProfessionInfo extends ASN1Object {
    public static final ASN1ObjectIdentifier Notar = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".9");
    public static final ASN1ObjectIdentifier Notariatsverwalter = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".13");
    public static final ASN1ObjectIdentifier Notariatsverwalterin = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".12");
    public static final ASN1ObjectIdentifier Notarin = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".8");
    public static final ASN1ObjectIdentifier Notarvertreter = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".11");
    public static final ASN1ObjectIdentifier Notarvertreterin = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".10");
    public static final ASN1ObjectIdentifier Patentanwalt = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".19");
    public static final ASN1ObjectIdentifier Patentanwltin = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".18");
    public static final ASN1ObjectIdentifier Rechtsanwalt = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".2");
    public static final ASN1ObjectIdentifier Rechtsanwltin = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".1");
    public static final ASN1ObjectIdentifier Rechtsbeistand = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".3");
    public static final ASN1ObjectIdentifier Steuerberater = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".5");
    public static final ASN1ObjectIdentifier Steuerberaterin = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".4");
    public static final ASN1ObjectIdentifier Steuerbevollmchtigte = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".6");
    public static final ASN1ObjectIdentifier Steuerbevollmchtigter = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".7");
    public static final ASN1ObjectIdentifier VereidigteBuchprferin = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".16");
    public static final ASN1ObjectIdentifier VereidigterBuchprfer = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".17");
    public static final ASN1ObjectIdentifier Wirtschaftsprfer = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".15");
    public static final ASN1ObjectIdentifier Wirtschaftsprferin = new ASN1ObjectIdentifier(NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".14");
    private ASN1OctetString addProfessionInfo;
    private NamingAuthority namingAuthority;
    private ASN1Sequence professionItems;
    private ASN1Sequence professionOIDs;
    private String registrationNumber;

    private ProfessionInfo(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() <= 5) {
            Enumeration objects = aSN1Sequence.getObjects();
            ASN1Encodable aSN1Encodable = (ASN1Encodable) objects.nextElement();
            if (aSN1Encodable instanceof ASN1TaggedObject) {
                ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) aSN1Encodable;
                if (aSN1TaggedObject.getTagNo() == 0) {
                    this.namingAuthority = NamingAuthority.getInstance(aSN1TaggedObject, true);
                    aSN1Encodable = (ASN1Encodable) objects.nextElement();
                } else {
                    throw new IllegalArgumentException("Bad tag number: " + aSN1TaggedObject.getTagNo());
                }
            }
            this.professionItems = ASN1Sequence.getInstance(aSN1Encodable);
            if (objects.hasMoreElements()) {
                ASN1Encodable aSN1Encodable2 = (ASN1Encodable) objects.nextElement();
                if (aSN1Encodable2 instanceof ASN1Sequence) {
                    this.professionOIDs = ASN1Sequence.getInstance(aSN1Encodable2);
                } else if (aSN1Encodable2 instanceof DERPrintableString) {
                    this.registrationNumber = DERPrintableString.getInstance(aSN1Encodable2).getString();
                } else if (aSN1Encodable2 instanceof ASN1OctetString) {
                    this.addProfessionInfo = ASN1OctetString.getInstance(aSN1Encodable2);
                } else {
                    throw new IllegalArgumentException("Bad object encountered: " + aSN1Encodable2.getClass());
                }
            }
            if (objects.hasMoreElements()) {
                ASN1Encodable aSN1Encodable3 = (ASN1Encodable) objects.nextElement();
                if (aSN1Encodable3 instanceof DERPrintableString) {
                    this.registrationNumber = DERPrintableString.getInstance(aSN1Encodable3).getString();
                } else if (aSN1Encodable3 instanceof DEROctetString) {
                    this.addProfessionInfo = (DEROctetString) aSN1Encodable3;
                } else {
                    throw new IllegalArgumentException("Bad object encountered: " + aSN1Encodable3.getClass());
                }
            }
            if (objects.hasMoreElements()) {
                ASN1Encodable aSN1Encodable4 = (ASN1Encodable) objects.nextElement();
                if (aSN1Encodable4 instanceof DEROctetString) {
                    this.addProfessionInfo = (DEROctetString) aSN1Encodable4;
                    return;
                }
                throw new IllegalArgumentException("Bad object encountered: " + aSN1Encodable4.getClass());
            }
            return;
        }
        throw new IllegalArgumentException("Bad sequence size: " + aSN1Sequence.size());
    }

    public ProfessionInfo(NamingAuthority namingAuthority2, DirectoryString[] directoryStringArr, ASN1ObjectIdentifier[] aSN1ObjectIdentifierArr, String str, ASN1OctetString aSN1OctetString) {
        this.namingAuthority = namingAuthority2;
        this.professionItems = new DERSequence(directoryStringArr);
        if (aSN1ObjectIdentifierArr != null) {
            this.professionOIDs = new DERSequence(aSN1ObjectIdentifierArr);
        }
        this.registrationNumber = str;
        this.addProfessionInfo = aSN1OctetString;
    }

    public static ProfessionInfo getInstance(Object obj) {
        if (obj == null || (obj instanceof ProfessionInfo)) {
            return (ProfessionInfo) obj;
        }
        if (obj instanceof ASN1Sequence) {
            return new ProfessionInfo((ASN1Sequence) obj);
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    public ASN1OctetString getAddProfessionInfo() {
        return this.addProfessionInfo;
    }

    public NamingAuthority getNamingAuthority() {
        return this.namingAuthority;
    }

    public DirectoryString[] getProfessionItems() {
        DirectoryString[] directoryStringArr = new DirectoryString[this.professionItems.size()];
        Enumeration objects = this.professionItems.getObjects();
        int i = 0;
        while (objects.hasMoreElements()) {
            directoryStringArr[i] = DirectoryString.getInstance(objects.nextElement());
            i++;
        }
        return directoryStringArr;
    }

    public ASN1ObjectIdentifier[] getProfessionOIDs() {
        ASN1Sequence aSN1Sequence = this.professionOIDs;
        int i = 0;
        if (aSN1Sequence == null) {
            return new ASN1ObjectIdentifier[0];
        }
        ASN1ObjectIdentifier[] aSN1ObjectIdentifierArr = new ASN1ObjectIdentifier[aSN1Sequence.size()];
        Enumeration objects = this.professionOIDs.getObjects();
        while (objects.hasMoreElements()) {
            aSN1ObjectIdentifierArr[i] = ASN1ObjectIdentifier.getInstance(objects.nextElement());
            i++;
        }
        return aSN1ObjectIdentifierArr;
    }

    public String getRegistrationNumber() {
        return this.registrationNumber;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(5);
        NamingAuthority namingAuthority2 = this.namingAuthority;
        if (namingAuthority2 != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 0, namingAuthority2));
        }
        aSN1EncodableVector.add(this.professionItems);
        ASN1Sequence aSN1Sequence = this.professionOIDs;
        if (aSN1Sequence != null) {
            aSN1EncodableVector.add(aSN1Sequence);
        }
        String str = this.registrationNumber;
        if (str != null) {
            aSN1EncodableVector.add(new DERPrintableString(str, true));
        }
        ASN1OctetString aSN1OctetString = this.addProfessionInfo;
        if (aSN1OctetString != null) {
            aSN1EncodableVector.add(aSN1OctetString);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
