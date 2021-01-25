package com.android.server.net.watchlist;

import android.privacy.DifferentialPrivacyEncoder;
import android.privacy.internal.longitudinalreporting.LongitudinalReportingConfig;
import android.privacy.internal.longitudinalreporting.LongitudinalReportingEncoder;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.net.watchlist.WatchlistReportDbHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PrivacyUtils {
    private static final boolean DEBUG = false;
    private static final String ENCODER_ID_PREFIX = "watchlist_encoder:";
    private static final double PROB_F = 0.469d;
    private static final double PROB_P = 0.28d;
    private static final double PROB_Q = 1.0d;
    private static final String TAG = "PrivacyUtils";

    private PrivacyUtils() {
    }

    @VisibleForTesting
    static DifferentialPrivacyEncoder createInsecureDPEncoderForTest(String appDigest) {
        return LongitudinalReportingEncoder.createInsecureEncoderForTest(createLongitudinalReportingConfig(appDigest));
    }

    @VisibleForTesting
    static DifferentialPrivacyEncoder createSecureDPEncoder(byte[] userSecret, String appDigest) {
        return LongitudinalReportingEncoder.createEncoder(createLongitudinalReportingConfig(appDigest), userSecret);
    }

    private static LongitudinalReportingConfig createLongitudinalReportingConfig(String appDigest) {
        return new LongitudinalReportingConfig(ENCODER_ID_PREFIX + appDigest, (double) PROB_F, (double) PROB_P, (double) PROB_Q);
    }

    @VisibleForTesting
    static Map<String, Boolean> createDpEncodedReportMap(boolean isSecure, byte[] userSecret, List<String> appDigestList, WatchlistReportDbHelper.AggregatedResult aggregatedResult) {
        DifferentialPrivacyEncoder encoder;
        int appDigestListSize = appDigestList.size();
        HashMap<String, Boolean> resultMap = new HashMap<>(appDigestListSize);
        for (int i = 0; i < appDigestListSize; i++) {
            String appDigest = appDigestList.get(i);
            if (isSecure) {
                encoder = createSecureDPEncoder(userSecret, appDigest);
            } else {
                encoder = createInsecureDPEncoderForTest(appDigest);
            }
            boolean encodedVisitedWatchlist = false;
            if ((encoder.encodeBoolean(aggregatedResult.appDigestList.contains(appDigest))[0] & 1) == 1) {
                encodedVisitedWatchlist = true;
            }
            resultMap.put(appDigest, Boolean.valueOf(encodedVisitedWatchlist));
        }
        return resultMap;
    }
}
