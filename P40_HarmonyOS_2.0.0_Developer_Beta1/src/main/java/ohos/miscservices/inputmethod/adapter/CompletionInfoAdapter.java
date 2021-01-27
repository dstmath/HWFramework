package ohos.miscservices.inputmethod.adapter;

import android.view.inputmethod.CompletionInfo;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.RecommendationInfo;

public class CompletionInfoAdapter {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "CompletionInfoAdapter");

    private CompletionInfoAdapter() {
    }

    public static CompletionInfo convertToCompletionInfo(RecommendationInfo recommendationInfo) {
        if (recommendationInfo != null) {
            return new CompletionInfo(recommendationInfo.getId(), recommendationInfo.getOffset(), recommendationInfo.getTextContent(), recommendationInfo.getReference());
        }
        HiLog.error(TAG, "recommendationInfo is null.", new Object[0]);
        return null;
    }

    public static RecommendationInfo convertToRecommendationInfo(CompletionInfo completionInfo) {
        String str;
        String str2 = null;
        if (completionInfo == null) {
            HiLog.error(TAG, "completionInfo is null.", new Object[0]);
            return null;
        }
        if (completionInfo.getText() == null) {
            str = null;
        } else {
            str = completionInfo.getText().toString();
        }
        if (completionInfo.getLabel() != null) {
            str2 = completionInfo.getLabel().toString();
        }
        return new RecommendationInfo(completionInfo.getPosition(), str, completionInfo.getId(), str2);
    }
}
