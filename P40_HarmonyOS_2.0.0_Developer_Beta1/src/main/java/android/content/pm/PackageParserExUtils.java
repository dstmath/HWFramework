package android.content.pm;

import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;
import android.util.ArraySet;
import com.huawei.android.content.pm.PackageUserStateEx;
import com.huawei.android.content.pm.SignatureEx;
import java.security.PublicKey;

public class PackageParserExUtils {
    public static final SignatureEx[] getSignatures(PackageParserEx.SigningDetailsEx detailsEx) {
        if (detailsEx == null || detailsEx.getSigningDetails() == null) {
            return null;
        }
        Signature[] signatures = detailsEx.getSigningDetails().signatures;
        SignatureEx[] signatureExes = new SignatureEx[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            signatureExes[i] = new SignatureEx();
            signatureExes[i].setSignature(signatures[i]);
        }
        return signatureExes;
    }

    public static final SignatureEx[] getPastSigningCertificates(PackageParserEx.SigningDetailsEx detailsEx) {
        Signature[] signatures;
        if (detailsEx == null || (signatures = detailsEx.getSigningDetails().pastSigningCertificates) == null) {
            return null;
        }
        SignatureEx[] signatureExes = new SignatureEx[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            signatureExes[i] = new SignatureEx();
            signatureExes[i].setSignature(signatures[i]);
        }
        return signatureExes;
    }

    public static PackageParserEx.SigningDetailsEx newSigningDetails(SignatureEx[] signatures, int signatureSchemeVersion, ArraySet<PublicKey> keys, SignatureEx[] pastSigningCertificates) {
        PackageParser.SigningDetails signingDetails = new PackageParser.SigningDetails(toSignatures(signatures), signatureSchemeVersion, keys, toSignatures(pastSigningCertificates));
        PackageParserEx.SigningDetailsEx signingDetailsEx = new PackageParserEx.SigningDetailsEx();
        signingDetailsEx.setSigningDetails(signingDetails);
        return signingDetailsEx;
    }

    private static Signature[] toSignatures(SignatureEx[] signaturexs) {
        if (signaturexs == null) {
            return null;
        }
        Signature[] signatures = new Signature[signaturexs.length];
        for (int i = 0; i < signaturexs.length; i++) {
            signatures[i] = signaturexs[i].getSignature();
        }
        return signatures;
    }

    public static ApplicationInfo generateApplicationInfo(ApplicationInfo ai, int flags, PackageUserStateEx state, int userId) {
        if (state == null) {
            return null;
        }
        return PackageParser.generateApplicationInfo(ai, flags, state.getPackageUserState(), userId);
    }
}
