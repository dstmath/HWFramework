package android.webkit;

import android.content.Context;
import android.util.Log;

class LegacyErrorStrings {
    private static final String LOGTAG = "Http";

    private LegacyErrorStrings() {
    }

    static String getString(int errorCode, Context context) {
        return context.getText(getResource(errorCode)).toString();
    }

    private static int getResource(int errorCode) {
        switch (errorCode) {
            case -15:
                return 17040187;
            case -14:
                return 17040180;
            case -13:
                return 17040179;
            case -12:
                return 33685722;
            case -11:
                return 17040178;
            case -10:
                return 17039368;
            case -9:
                return 17040185;
            case -8:
                return 17040186;
            case -7:
                return 17040181;
            case -6:
                return 17040177;
            case -5:
                return 17040184;
            case -4:
                return 17040176;
            case -3:
                return 17040188;
            case -2:
                return 17040182;
            case -1:
                return 17040175;
            case 0:
                return 17040183;
            default:
                Log.w(LOGTAG, "Using generic message for unknown error code: " + errorCode);
                return 17040175;
        }
    }
}
