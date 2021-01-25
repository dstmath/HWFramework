package com.android.server.broadcastradio.hal1;

import android.util.Slog;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.Set;

class Convert {
    private static final String TAG = "BroadcastRadioService.Convert";

    Convert() {
    }

    static String[][] stringMapToNative(Map<String, String> map) {
        if (map == null) {
            Slog.v(TAG, "map is null, returning zero-elements array");
            return (String[][]) Array.newInstance(String.class, 0, 0);
        }
        Set<Map.Entry<String, String>> entries = map.entrySet();
        String[][] arr = (String[][]) Array.newInstance(String.class, entries.size(), 2);
        int i = 0;
        for (Map.Entry<String, String> entry : entries) {
            arr[i][0] = entry.getKey();
            arr[i][1] = entry.getValue();
            i++;
        }
        Slog.v(TAG, "converted " + i + " element(s)");
        return arr;
    }
}
