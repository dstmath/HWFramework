package org.bouncycastle.cms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Generator;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetStringParser;
import org.bouncycastle.asn1.ASN1SequenceParser;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1SetParser;
import org.bouncycastle.asn1.ASN1StreamParser;
import org.bouncycastle.asn1.BERSequenceGenerator;
import org.bouncycastle.asn1.BERSetParser;
import org.bouncycastle.asn1.BERTaggedObject;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.cms.ContentInfoParser;
import org.bouncycastle.asn1.cms.SignedDataParser;
import org.bouncycastle.asn1.cms.SignerInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.io.Streams;

public class CMSSignedDataParser extends CMSContentInfoParser {
    private static final CMSSignedHelper HELPER = CMSSignedHelper.INSTANCE;
    private ASN1Set _certSet;
    private ASN1Set _crlSet;
    private boolean _isCertCrlParsed;
    private CMSTypedStream _signedContent;
    private ASN1ObjectIdentifier _signedContentType;
    private SignedDataParser _signedData;
    private SignerInformationStore _signerInfoStore;
    private Set<AlgorithmIdentifier> digestAlgorithms;
    private Map digests;

    public CMSSignedDataParser(DigestCalculatorProvider digestCalculatorProvider, InputStream inputStream) throws CMSException {
        this(digestCalculatorProvider, (CMSTypedStream) null, inputStream);
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0088 A[Catch:{ IOException -> 0x0097 }] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x008f A[Catch:{ IOException -> 0x0097 }] */
    public CMSSignedDataParser(DigestCalculatorProvider digestCalculatorProvider, CMSTypedStream cMSTypedStream, InputStream inputStream) throws CMSException {
        super(inputStream);
        CMSTypedStream cMSTypedStream2;
        try {
            this._signedContent = cMSTypedStream;
            this._signedData = SignedDataParser.getInstance(this._contentInfo.getContent(16));
            this.digests = new HashMap();
            ASN1SetParser digestAlgorithms2 = this._signedData.getDigestAlgorithms();
            HashSet hashSet = new HashSet();
            while (true) {
                ASN1Encodable readObject = digestAlgorithms2.readObject();
                if (readObject == null) {
                    break;
                }
                AlgorithmIdentifier instance = AlgorithmIdentifier.getInstance(readObject);
                hashSet.add(instance);
                try {
                    DigestCalculator digestCalculator = digestCalculatorProvider.get(instance);
                    if (digestCalculator != null) {
                        this.digests.put(instance.getAlgorithm(), digestCalculator);
                    }
                } catch (OperatorCreationException e) {
                }
            }
            this.digestAlgorithms = Collections.unmodifiableSet(hashSet);
            ContentInfoParser encapContentInfo = this._signedData.getEncapContentInfo();
            ASN1Encodable content = encapContentInfo.getContent(4);
            if (content instanceof ASN1OctetStringParser) {
                CMSTypedStream cMSTypedStream3 = new CMSTypedStream(encapContentInfo.getContentType(), ((ASN1OctetStringParser) content).getOctetStream());
                cMSTypedStream2 = cMSTypedStream3;
                if (this._signedContent != null) {
                    cMSTypedStream3.drain();
                    this._signedContentType = cMSTypedStream == null ? encapContentInfo.getContentType() : this._signedContent.getContentType();
                }
            } else {
                if (content != null) {
                    PKCS7TypedStream pKCS7TypedStream = new PKCS7TypedStream(encapContentInfo.getContentType(), content);
                    if (this._signedContent == null) {
                        cMSTypedStream2 = pKCS7TypedStream;
                    } else {
                        pKCS7TypedStream.drain();
                    }
                }
                this._signedContentType = cMSTypedStream == null ? encapContentInfo.getContentType() : this._signedContent.getContentType();
            }
            this._signedContent = cMSTypedStream2;
            this._signedContentType = cMSTypedStream == null ? encapContentInfo.getContentType() : this._signedContent.getContentType();
        } catch (IOException e2) {
            throw new CMSException("io exception: " + e2.getMessage(), e2);
        }
    }

    public CMSSignedDataParser(DigestCalculatorProvider digestCalculatorProvider, CMSTypedStream cMSTypedStream, byte[] bArr) throws CMSException {
        this(digestCalculatorProvider, cMSTypedStream, (InputStream) new ByteArrayInputStream(bArr));
    }

    public CMSSignedDataParser(DigestCalculatorProvider digestCalculatorProvider, byte[] bArr) throws CMSException {
        this(digestCalculatorProvider, (InputStream) new ByteArrayInputStream(bArr));
    }

    private static ASN1Set getASN1Set(ASN1SetParser aSN1SetParser) {
        if (aSN1SetParser == null) {
            return null;
        }
        return ASN1Set.getInstance(aSN1SetParser.toASN1Primitive());
    }

    private static void pipeEncapsulatedOctetString(ContentInfoParser contentInfoParser, OutputStream outputStream) throws IOException {
        ASN1OctetStringParser aSN1OctetStringParser = (ASN1OctetStringParser) contentInfoParser.getContent(4);
        if (aSN1OctetStringParser != null) {
            pipeOctetString(aSN1OctetStringParser, outputStream);
        }
    }

    private static void pipeOctetString(ASN1OctetStringParser aSN1OctetStringParser, OutputStream outputStream) throws IOException {
        OutputStream createBEROctetOutputStream = CMSUtils.createBEROctetOutputStream(outputStream, 0, true, 0);
        Streams.pipeAll(aSN1OctetStringParser.getOctetStream(), createBEROctetOutputStream);
        createBEROctetOutputStream.close();
    }

    private void populateCertCrlSets() throws CMSException {
        if (!this._isCertCrlParsed) {
            this._isCertCrlParsed = true;
            try {
                this._certSet = getASN1Set(this._signedData.getCertificates());
                this._crlSet = getASN1Set(this._signedData.getCrls());
            } catch (IOException e) {
                throw new CMSException("problem parsing cert/crl sets", e);
            }
        }
    }

    public static OutputStream replaceCertificatesAndCRLs(InputStream inputStream, Store store, Store store2, Store store3, OutputStream outputStream) throws CMSException, IOException {
        SignedDataParser instance = SignedDataParser.getInstance(new ContentInfoParser((ASN1SequenceParser) new ASN1StreamParser(inputStream).readObject()).getContent(16));
        BERSequenceGenerator bERSequenceGenerator = new BERSequenceGenerator(outputStream);
        bERSequenceGenerator.addObject(CMSObjectIdentifiers.signedData);
        BERSequenceGenerator bERSequenceGenerator2 = new BERSequenceGenerator(bERSequenceGenerator.getRawOutputStream(), 0, true);
        bERSequenceGenerator2.addObject(instance.getVersion());
        bERSequenceGenerator2.getRawOutputStream().write(instance.getDigestAlgorithms().toASN1Primitive().getEncoded());
        ContentInfoParser encapContentInfo = instance.getEncapContentInfo();
        BERSequenceGenerator bERSequenceGenerator3 = new BERSequenceGenerator(bERSequenceGenerator2.getRawOutputStream());
        bERSequenceGenerator3.addObject(encapContentInfo.getContentType());
        pipeEncapsulatedOctetString(encapContentInfo, bERSequenceGenerator3.getRawOutputStream());
        bERSequenceGenerator3.close();
        getASN1Set(instance.getCertificates());
        getASN1Set(instance.getCrls());
        if (!(store == null && store3 == null)) {
            ArrayList arrayList = new ArrayList();
            if (store != null) {
                arrayList.addAll(CMSUtils.getCertificatesFromStore(store));
            }
            if (store3 != null) {
                arrayList.addAll(CMSUtils.getAttributeCertificatesFromStore(store3));
            }
            ASN1Set createBerSetFromList = CMSUtils.createBerSetFromList(arrayList);
            if (createBerSetFromList.size() > 0) {
                bERSequenceGenerator2.getRawOutputStream().write(new DERTaggedObject(false, 0, createBerSetFromList).getEncoded());
            }
        }
        if (store2 != null) {
            ASN1Set createBerSetFromList2 = CMSUtils.createBerSetFromList(CMSUtils.getCRLsFromStore(store2));
            if (createBerSetFromList2.size() > 0) {
                bERSequenceGenerator2.getRawOutputStream().write(new DERTaggedObject(false, 1, createBerSetFromList2).getEncoded());
            }
        }
        bERSequenceGenerator2.getRawOutputStream().write(instance.getSignerInfos().toASN1Primitive().getEncoded());
        bERSequenceGenerator2.close();
        bERSequenceGenerator.close();
        return outputStream;
    }

    public static OutputStream replaceSigners(InputStream inputStream, SignerInformationStore signerInformationStore, OutputStream outputStream) throws CMSException, IOException {
        SignedDataParser instance = SignedDataParser.getInstance(new ContentInfoParser((ASN1SequenceParser) new ASN1StreamParser(inputStream).readObject()).getContent(16));
        BERSequenceGenerator bERSequenceGenerator = new BERSequenceGenerator(outputStream);
        bERSequenceGenerator.addObject(CMSObjectIdentifiers.signedData);
        BERSequenceGenerator bERSequenceGenerator2 = new BERSequenceGenerator(bERSequenceGenerator.getRawOutputStream(), 0, true);
        bERSequenceGenerator2.addObject(instance.getVersion());
        instance.getDigestAlgorithms().toASN1Primitive();
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        for (SignerInformation digestAlgorithmID : signerInformationStore.getSigners()) {
            aSN1EncodableVector.add(CMSSignedHelper.INSTANCE.fixAlgID(digestAlgorithmID.getDigestAlgorithmID()));
        }
        bERSequenceGenerator2.getRawOutputStream().write(new DERSet(aSN1EncodableVector).getEncoded());
        ContentInfoParser encapContentInfo = instance.getEncapContentInfo();
        BERSequenceGenerator bERSequenceGenerator3 = new BERSequenceGenerator(bERSequenceGenerator2.getRawOutputStream());
        bERSequenceGenerator3.addObject(encapContentInfo.getContentType());
        pipeEncapsulatedOctetString(encapContentInfo, bERSequenceGenerator3.getRawOutputStream());
        bERSequenceGenerator3.close();
        writeSetToGeneratorTagged(bERSequenceGenerator2, instance.getCertificates(), 0);
        writeSetToGeneratorTagged(bERSequenceGenerator2, instance.getCrls(), 1);
        ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
        for (SignerInformation aSN1Structure : signerInformationStore.getSigners()) {
            aSN1EncodableVector2.add(aSN1Structure.toASN1Structure());
        }
        bERSequenceGenerator2.getRawOutputStream().write(new DERSet(aSN1EncodableVector2).getEncoded());
        bERSequenceGenerator2.close();
        bERSequenceGenerator.close();
        return outputStream;
    }

    private static void writeSetToGeneratorTagged(ASN1Generator aSN1Generator, ASN1SetParser aSN1SetParser, int i) throws IOException {
        OutputStream rawOutputStream;
        byte[] encoded;
        ASN1Set aSN1Set = getASN1Set(aSN1SetParser);
        if (aSN1Set != null) {
            if (aSN1SetParser instanceof BERSetParser) {
                rawOutputStream = aSN1Generator.getRawOutputStream();
                encoded = new BERTaggedObject(false, i, aSN1Set).getEncoded();
            } else {
                rawOutputStream = aSN1Generator.getRawOutputStream();
                encoded = new DERTaggedObject(false, i, aSN1Set).getEncoded();
            }
            rawOutputStream.write(encoded);
        }
    }

    public Store getAttributeCertificates() throws CMSException {
        populateCertCrlSets();
        return HELPER.getAttributeCertificates(this._certSet);
    }

    public Store getCRLs() throws CMSException {
        populateCertCrlSets();
        return HELPER.getCRLs(this._crlSet);
    }

    public Store getCertificates() throws CMSException {
        populateCertCrlSets();
        return HELPER.getCertificates(this._certSet);
    }

    public Set<AlgorithmIdentifier> getDigestAlgorithmIDs() {
        return this.digestAlgorithms;
    }

    public Store getOtherRevocationInfo(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws CMSException {
        populateCertCrlSets();
        return HELPER.getOtherRevocationInfo(aSN1ObjectIdentifier, this._crlSet);
    }

    public CMSTypedStream getSignedContent() {
        if (this._signedContent == null) {
            return null;
        }
        return new CMSTypedStream(this._signedContent.getContentType(), CMSUtils.attachDigestsToInputStream(this.digests.values(), this._signedContent.getContentStream()));
    }

    public String getSignedContentTypeOID() {
        return this._signedContentType.getId();
    }

    public SignerInformationStore getSignerInfos() throws CMSException {
        if (this._signerInfoStore == null) {
            populateCertCrlSets();
            ArrayList arrayList = new ArrayList();
            HashMap hashMap = new HashMap();
            for (Object next : this.digests.keySet()) {
                hashMap.put(next, ((DigestCalculator) this.digests.get(next)).getDigest());
            }
            try {
                ASN1SetParser signerInfos = this._signedData.getSignerInfos();
                while (true) {
                    ASN1Encodable readObject = signerInfos.readObject();
                    if (readObject == null) {
                        break;
                    }
                    SignerInfo instance = SignerInfo.getInstance(readObject.toASN1Primitive());
                    arrayList.add(new SignerInformation(instance, this._signedContentType, null, (byte[]) hashMap.get(instance.getDigestAlgorithm().getAlgorithm())));
                }
                this._signerInfoStore = new SignerInformationStore((Collection<SignerInformation>) arrayList);
            } catch (IOException e) {
                throw new CMSException("io exception: " + e.getMessage(), e);
            }
        }
        return this._signerInfoStore;
    }

    public int getVersion() {
        return this._signedData.getVersion().getValue().intValue();
    }
}
