package com.android.server.wm;

import android.content.Context;
import android.util.Flog;
import android.util.Log;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/* access modifiers changed from: package-private */
public final class DividerBarDragEventReport {
    private static final String DRAG_RESULT_KEY = "dragResult";
    private static final String NEW_RATIO_KEY = "newRatio";
    private static final String OLD_RATIO_KEY = "oldRatio";
    private static final String ORIENTATION_KEY = "orientation";
    private static final Map<Integer, String> SPLIT_RATIO_MAP = new HashMap();
    private static final String TAG = "DividerBarDragEventRepo";

    private enum DividerBarDragResult {
        CHANGE_RATIO,
        EXIT_SPLIT,
        CANCEL_DRAG
    }

    static {
        SPLIT_RATIO_MAP.put(0, "1:1");
        SPLIT_RATIO_MAP.put(1, "1:2");
        SPLIT_RATIO_MAP.put(2, "2:1");
    }

    private DividerBarDragEventReport() {
    }

    static void bdReport(Context context, int oldSplitRatio, int newSplitRatio, int orientation) {
        String result;
        boolean isChangeRatio = false;
        if (newSplitRatio == oldSplitRatio) {
            result = DividerBarDragResult.CANCEL_DRAG.name().toLowerCase(Locale.ENGLISH);
        } else if (newSplitRatio == 0 || newSplitRatio == 1 || newSplitRatio == 2) {
            result = DividerBarDragResult.CHANGE_RATIO.name().toLowerCase(Locale.ENGLISH);
            isChangeRatio = true;
        } else if (newSplitRatio == 3 || newSplitRatio == 4) {
            result = DividerBarDragResult.EXIT_SPLIT.name().toLowerCase(Locale.ENGLISH);
        } else {
            return;
        }
        JSONObject eventDict = new JSONObject();
        try {
            eventDict.put(DRAG_RESULT_KEY, result);
            if (isChangeRatio) {
                eventDict.put(ORIENTATION_KEY, orientation);
                eventDict.put(OLD_RATIO_KEY, SPLIT_RATIO_MAP.get(Integer.valueOf(oldSplitRatio)));
                eventDict.put(NEW_RATIO_KEY, SPLIT_RATIO_MAP.get(Integer.valueOf(newSplitRatio)));
            }
        } catch (JSONException e) {
            Log.d(TAG, "bdReport: " + e.getMessage());
        }
        Flog.bdReport(991311031, eventDict);
    }
}
