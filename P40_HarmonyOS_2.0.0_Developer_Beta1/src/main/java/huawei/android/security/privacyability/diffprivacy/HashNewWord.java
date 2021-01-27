package huawei.android.security.privacyability.diffprivacy;

import android.text.TextUtils;
import android.util.Log;
import com.huawei.hwpartsecurity.BuildConfig;
import java.security.NoSuchAlgorithmException;

/* access modifiers changed from: package-private */
public class HashNewWord {
    private static final int BINARY = 2;
    private static final int HEX = 16;
    private static final int HEX_BYTE = 4;
    private static final String LOG_TAG = "LocalWordFilter";
    private final int bloomBits;
    private final int hashNum;

    public HashNewWord(int hashNum2, int bloomBits2) {
        this.hashNum = hashNum2;
        this.bloomBits = bloomBits2;
    }

    public int getStrpIndex(String prestring, int index) throws NoSuchAlgorithmException {
        HashUtil hashUtil = new HashUtil();
        int res = hashUtil.fastHash(this.hashNum, index, 0, "%~" + prestring + "^$", this.bloomBits);
        if (res >= 0) {
            return getSparseHash(res);
        }
        Log.e(LOG_TAG, "Gen strP Hash int error.");
        return res;
    }

    private int getSparseHash(int hashValue) {
        return hashValue % this.bloomBits;
    }

    public String convertReporttoHex(String report, int numBits) {
        if (TextUtils.isEmpty(report)) {
            return BuildConfig.FLAVOR;
        }
        StringBuilder builder = new StringBuilder();
        if (numBits > 4) {
            try {
                int reminder = numBits % 4;
                int numberOfHex = numBits / 4;
                for (int eachHex = 0; eachHex < numberOfHex; eachHex++) {
                    builder.append(Integer.toString(Integer.parseInt(report.substring(eachHex * 4, (eachHex + 1) * 4), 2), 16));
                }
                if (reminder > 0) {
                    builder.append(Integer.toString(Integer.parseInt(report.substring(numberOfHex * 4, numBits), 2), 16));
                }
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, "Integer parse of parameter is wrong.");
                return BuildConfig.FLAVOR;
            }
        } else {
            builder.append(Integer.toString(Integer.parseInt(report, 2), 16));
        }
        return builder.toString();
    }
}
