package android.service.autofill.augmented;

import android.content.ComponentName;
import android.metrics.LogMaker;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;
import java.io.PrintStream;

public final class Helper {
    private static final MetricsLogger sMetricsLogger = new MetricsLogger();

    public static void logResponse(int type, String servicePackageName, ComponentName componentName, int mSessionId, long durationMs) {
        LogMaker log = new LogMaker((int) MetricsProto.MetricsEvent.AUTOFILL_AUGMENTED_RESPONSE).setType(type).setComponentName(componentName).addTaggedData(MetricsProto.MetricsEvent.FIELD_AUTOFILL_SESSION_ID, Integer.valueOf(mSessionId)).addTaggedData(MetricsProto.MetricsEvent.FIELD_AUTOFILL_SERVICE, servicePackageName).addTaggedData(MetricsProto.MetricsEvent.FIELD_AUTOFILL_DURATION, Long.valueOf(durationMs));
        PrintStream printStream = System.out;
        printStream.println("LOGGGO: " + log.getEntries());
        sMetricsLogger.write(log);
    }

    private Helper() {
        throw new UnsupportedOperationException("contains only static methods");
    }
}
