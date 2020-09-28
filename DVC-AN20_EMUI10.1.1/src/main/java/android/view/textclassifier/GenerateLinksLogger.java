package android.view.textclassifier;

import android.metrics.LogMaker;
import android.util.ArrayMap;
import android.view.textclassifier.TextLinks;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.Preconditions;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public final class GenerateLinksLogger {
    private static final String LOG_TAG = "GenerateLinksLogger";
    private static final String ZERO = "0";
    private final MetricsLogger mMetricsLogger;
    private final Random mRng;
    private final int mSampleRate;

    public GenerateLinksLogger(int sampleRate) {
        this.mSampleRate = sampleRate;
        this.mRng = new Random(System.nanoTime());
        this.mMetricsLogger = new MetricsLogger();
    }

    @VisibleForTesting
    public GenerateLinksLogger(int sampleRate, MetricsLogger metricsLogger) {
        this.mSampleRate = sampleRate;
        this.mRng = new Random(System.nanoTime());
        this.mMetricsLogger = metricsLogger;
    }

    public void logGenerateLinks(CharSequence text, TextLinks links, String callingPackageName, long latencyMs) {
        String entityType;
        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(links);
        Preconditions.checkNotNull(callingPackageName);
        if (shouldLog()) {
            LinkifyStats totalStats = new LinkifyStats();
            Map<String, LinkifyStats> perEntityTypeStats = new ArrayMap<>();
            for (TextLinks.TextLink link : links.getLinks()) {
                if (link.getEntityCount() != 0 && (entityType = link.getEntity(0)) != null && !"other".equals(entityType) && !"".equals(entityType)) {
                    totalStats.countLink(link);
                    perEntityTypeStats.computeIfAbsent(entityType, $$Lambda$GenerateLinksLogger$vmbT_h7MLlbrIm0lJJwAeHQhXk.INSTANCE).countLink(link);
                }
            }
            String callId = UUID.randomUUID().toString();
            writeStats(callId, callingPackageName, null, totalStats, text, latencyMs);
            for (Map.Entry<String, LinkifyStats> entry : perEntityTypeStats.entrySet()) {
                writeStats(callId, callingPackageName, entry.getKey(), entry.getValue(), text, latencyMs);
            }
        }
    }

    static /* synthetic */ LinkifyStats lambda$logGenerateLinks$0(String k) {
        return new LinkifyStats();
    }

    private boolean shouldLog() {
        int i = this.mSampleRate;
        if (i > 1 && this.mRng.nextInt(i) != 0) {
            return false;
        }
        return true;
    }

    private void writeStats(String callId, String callingPackageName, String entityType, LinkifyStats stats, CharSequence text, long latencyMs) {
        LogMaker log = new LogMaker(1313).setPackageName(callingPackageName).addTaggedData(1319, callId).addTaggedData(1316, Integer.valueOf(stats.mNumLinks)).addTaggedData(1317, Integer.valueOf(stats.mNumLinksTextLength)).addTaggedData(1315, Integer.valueOf(text.length())).addTaggedData(1314, Long.valueOf(latencyMs));
        if (entityType != null) {
            log.addTaggedData(1318, entityType);
        }
        this.mMetricsLogger.write(log);
        debugLog(log);
    }

    private static void debugLog(LogMaker log) {
        if (Log.ENABLE_FULL_LOGGING) {
            String callId = Objects.toString(log.getTaggedData(1319), "");
            String entityType = Objects.toString(log.getTaggedData(1318), "ANY_ENTITY");
            int numLinks = Integer.parseInt(Objects.toString(log.getTaggedData(1316), "0"));
            int linkLength = Integer.parseInt(Objects.toString(log.getTaggedData(1317), "0"));
            int textLength = Integer.parseInt(Objects.toString(log.getTaggedData(1315), "0"));
            int latencyMs = Integer.parseInt(Objects.toString(log.getTaggedData(1314), "0"));
            Log.v(LOG_TAG, String.format(Locale.US, "%s:%s %d links (%d/%d chars) %dms %s", callId, entityType, Integer.valueOf(numLinks), Integer.valueOf(linkLength), Integer.valueOf(textLength), Integer.valueOf(latencyMs), log.getPackageName()));
        }
    }

    /* access modifiers changed from: private */
    public static final class LinkifyStats {
        int mNumLinks;
        int mNumLinksTextLength;

        private LinkifyStats() {
        }

        /* access modifiers changed from: package-private */
        public void countLink(TextLinks.TextLink link) {
            this.mNumLinks++;
            this.mNumLinksTextLength += link.getEnd() - link.getStart();
        }
    }
}
