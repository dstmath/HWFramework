package android.util.apk;

import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;

public class ApkSignatureVerifierEx {
    public static PackageParserEx.SigningDetailsEx unsafeGetCertsWithoutVerification(String apkPath, int minSignatureSchemeVersion) throws PackageParserEx.PackageParserExceptionEx {
        try {
            PackageParser.SigningDetails signingDetails = ApkSignatureVerifier.unsafeGetCertsWithoutVerification(apkPath, minSignatureSchemeVersion);
            PackageParserEx.SigningDetailsEx detailsEx = new PackageParserEx.SigningDetailsEx();
            detailsEx.setSigningDetails(signingDetails);
            return detailsEx;
        } catch (PackageParser.PackageParserException e) {
            throw new PackageParserEx.PackageParserExceptionEx(e);
        }
    }
}
