package org.bouncycastle.pqc.crypto.util;

import java.io.IOException;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.isara.IsaraObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import org.bouncycastle.pqc.asn1.SPHINCS256KeyParams;
import org.bouncycastle.pqc.asn1.XMSSKeyParams;
import org.bouncycastle.pqc.asn1.XMSSMTKeyParams;
import org.bouncycastle.pqc.asn1.XMSSMTPublicKey;
import org.bouncycastle.pqc.asn1.XMSSPublicKey;
import org.bouncycastle.pqc.crypto.lms.Composer;
import org.bouncycastle.pqc.crypto.lms.HSSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.newhope.NHPublicKeyParameters;
import org.bouncycastle.pqc.crypto.qtesla.QTESLAPublicKeyParameters;
import org.bouncycastle.pqc.crypto.sphincs.SPHINCSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTPublicKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSPublicKeyParameters;

public class SubjectPublicKeyInfoFactory {
    private SubjectPublicKeyInfoFactory() {
    }

    public static SubjectPublicKeyInfo createSubjectPublicKeyInfo(AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        if (asymmetricKeyParameter instanceof QTESLAPublicKeyParameters) {
            QTESLAPublicKeyParameters qTESLAPublicKeyParameters = (QTESLAPublicKeyParameters) asymmetricKeyParameter;
            return new SubjectPublicKeyInfo(Utils.qTeslaLookupAlgID(qTESLAPublicKeyParameters.getSecurityCategory()), qTESLAPublicKeyParameters.getPublicData());
        } else if (asymmetricKeyParameter instanceof SPHINCSPublicKeyParameters) {
            SPHINCSPublicKeyParameters sPHINCSPublicKeyParameters = (SPHINCSPublicKeyParameters) asymmetricKeyParameter;
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(PQCObjectIdentifiers.sphincs256, new SPHINCS256KeyParams(Utils.sphincs256LookupTreeAlgID(sPHINCSPublicKeyParameters.getTreeDigest()))), sPHINCSPublicKeyParameters.getKeyData());
        } else if (asymmetricKeyParameter instanceof NHPublicKeyParameters) {
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(PQCObjectIdentifiers.newHope), ((NHPublicKeyParameters) asymmetricKeyParameter).getPubData());
        } else {
            if (asymmetricKeyParameter instanceof LMSPublicKeyParameters) {
                return new SubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_alg_hss_lms_hashsig), new DEROctetString(Composer.compose().u32str(1).bytes((LMSPublicKeyParameters) asymmetricKeyParameter).build()));
            } else if (asymmetricKeyParameter instanceof HSSPublicKeyParameters) {
                HSSPublicKeyParameters hSSPublicKeyParameters = (HSSPublicKeyParameters) asymmetricKeyParameter;
                return new SubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_alg_hss_lms_hashsig), new DEROctetString(Composer.compose().u32str(hSSPublicKeyParameters.getL()).bytes(hSSPublicKeyParameters.getLMSPublicKey()).build()));
            } else if (asymmetricKeyParameter instanceof XMSSPublicKeyParameters) {
                XMSSPublicKeyParameters xMSSPublicKeyParameters = (XMSSPublicKeyParameters) asymmetricKeyParameter;
                byte[] publicSeed = xMSSPublicKeyParameters.getPublicSeed();
                byte[] root = xMSSPublicKeyParameters.getRoot();
                byte[] encoded = xMSSPublicKeyParameters.getEncoded();
                return encoded.length > publicSeed.length + root.length ? new SubjectPublicKeyInfo(new AlgorithmIdentifier(IsaraObjectIdentifiers.id_alg_xmss), new DEROctetString(encoded)) : new SubjectPublicKeyInfo(new AlgorithmIdentifier(PQCObjectIdentifiers.xmss, new XMSSKeyParams(xMSSPublicKeyParameters.getParameters().getHeight(), Utils.xmssLookupTreeAlgID(xMSSPublicKeyParameters.getTreeDigest()))), new XMSSPublicKey(publicSeed, root));
            } else if (asymmetricKeyParameter instanceof XMSSMTPublicKeyParameters) {
                XMSSMTPublicKeyParameters xMSSMTPublicKeyParameters = (XMSSMTPublicKeyParameters) asymmetricKeyParameter;
                byte[] publicSeed2 = xMSSMTPublicKeyParameters.getPublicSeed();
                byte[] root2 = xMSSMTPublicKeyParameters.getRoot();
                byte[] encoded2 = xMSSMTPublicKeyParameters.getEncoded();
                return encoded2.length > publicSeed2.length + root2.length ? new SubjectPublicKeyInfo(new AlgorithmIdentifier(IsaraObjectIdentifiers.id_alg_xmssmt), new DEROctetString(encoded2)) : new SubjectPublicKeyInfo(new AlgorithmIdentifier(PQCObjectIdentifiers.xmss_mt, new XMSSMTKeyParams(xMSSMTPublicKeyParameters.getParameters().getHeight(), xMSSMTPublicKeyParameters.getParameters().getLayers(), Utils.xmssLookupTreeAlgID(xMSSMTPublicKeyParameters.getTreeDigest()))), new XMSSMTPublicKey(xMSSMTPublicKeyParameters.getPublicSeed(), xMSSMTPublicKeyParameters.getRoot()));
            } else {
                throw new IOException("key parameters not recognized");
            }
        }
    }
}
