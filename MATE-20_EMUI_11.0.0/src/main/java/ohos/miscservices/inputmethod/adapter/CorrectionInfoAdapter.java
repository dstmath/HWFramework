package ohos.miscservices.inputmethod.adapter;

import android.view.inputmethod.CorrectionInfo;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.CorrectionText;

public class CorrectionInfoAdapter {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "CorrectionInfoAdapter");

    private CorrectionInfoAdapter() {
    }

    public static CorrectionInfo convertToCorrectionInfo(CorrectionText correctionText) {
        if (correctionText != null) {
            return new CorrectionInfo(correctionText.getStartOffset(), correctionText.getOriginalText(), correctionText.getCorrectedText());
        }
        HiLog.error(TAG, "correctionText is null.", new Object[0]);
        return null;
    }

    public static CorrectionText convertToCorrectionText(CorrectionInfo correctionInfo) {
        String str;
        String str2 = null;
        if (correctionInfo == null) {
            HiLog.error(TAG, "correctionInfo is null.", new Object[0]);
            return null;
        }
        if (correctionInfo.getOldText() == null) {
            str = null;
        } else {
            str = correctionInfo.getOldText().toString();
        }
        if (correctionInfo.getNewText() != null) {
            str2 = correctionInfo.getNewText().toString();
        }
        return new CorrectionText(correctionInfo.getOffset(), str, str2);
    }
}
