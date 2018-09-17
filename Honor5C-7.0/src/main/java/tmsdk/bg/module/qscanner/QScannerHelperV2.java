package tmsdk.bg.module.qscanner;

import android.content.Context;
import tmsdkobf.ka;

/* compiled from: Unknown */
public class QScannerHelperV2 {
    public static ICertCheckerV2 createDefaultCertChecker(Context context) {
        return new ka(context);
    }
}
