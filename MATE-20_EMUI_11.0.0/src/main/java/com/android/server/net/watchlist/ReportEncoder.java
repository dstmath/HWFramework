package com.android.server.net.watchlist;

import android.util.Log;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.HexDump;
import com.android.server.net.watchlist.WatchlistReportDbHelper;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

class ReportEncoder {
    private static final int REPORT_VERSION = 1;
    private static final String TAG = "ReportEncoder";
    private static final int WATCHLIST_HASH_SIZE = 32;

    ReportEncoder() {
    }

    static byte[] encodeWatchlistReport(WatchlistConfig config, byte[] userSecret, List<String> appDigestList, WatchlistReportDbHelper.AggregatedResult aggregatedResult) {
        return serializeReport(config, PrivacyUtils.createDpEncodedReportMap(config.isConfigSecure(), userSecret, appDigestList, aggregatedResult));
    }

    @VisibleForTesting
    static byte[] serializeReport(WatchlistConfig config, Map<String, Boolean> encodedReportMap) {
        byte[] watchlistHash = config.getWatchlistConfigHash();
        if (watchlistHash == null) {
            Log.e(TAG, "No watchlist hash");
            return null;
        } else if (watchlistHash.length != 32) {
            Log.e(TAG, "Unexpected hash length");
            return null;
        } else {
            ByteArrayOutputStream reportOutputStream = new ByteArrayOutputStream();
            ProtoOutputStream proto = new ProtoOutputStream(reportOutputStream);
            proto.write(1120986464257L, 1);
            proto.write(1138166333442L, HexDump.toHexString(watchlistHash));
            for (Map.Entry<String, Boolean> entry : encodedReportMap.entrySet()) {
                String key = entry.getKey();
                HexDump.hexStringToByteArray(key);
                boolean encodedResult = entry.getValue().booleanValue();
                long token = proto.start(2246267895811L);
                proto.write(1138166333441L, key);
                proto.write(1133871366146L, encodedResult);
                proto.end(token);
            }
            proto.flush();
            return reportOutputStream.toByteArray();
        }
    }
}
