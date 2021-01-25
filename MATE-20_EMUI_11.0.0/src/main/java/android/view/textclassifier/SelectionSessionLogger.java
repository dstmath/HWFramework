package android.view.textclassifier;

import android.content.Context;
import android.metrics.LogMaker;
import android.telephony.SmsManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.Preconditions;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

public final class SelectionSessionLogger {
    static final String CLASSIFIER_ID = "androidtc";
    private static final int ENTITY_TYPE = 1254;
    private static final int EVENT_END = 1251;
    private static final int EVENT_START = 1250;
    private static final int INDEX = 1120;
    private static final String LOG_TAG = "SelectionSessionLogger";
    private static final int MODEL_NAME = 1256;
    private static final int PREV_EVENT_DELTA = 1118;
    private static final int SESSION_ID = 1119;
    private static final int SMART_END = 1253;
    private static final int SMART_START = 1252;
    private static final int START_EVENT_DELTA = 1117;
    private static final String UNKNOWN = "unknown";
    private static final int WIDGET_TYPE = 1255;
    private static final int WIDGET_VERSION = 1262;
    private static final String ZERO = "0";
    private final MetricsLogger mMetricsLogger;

    public SelectionSessionLogger() {
        this.mMetricsLogger = new MetricsLogger();
    }

    @VisibleForTesting
    public SelectionSessionLogger(MetricsLogger metricsLogger) {
        this.mMetricsLogger = (MetricsLogger) Preconditions.checkNotNull(metricsLogger);
    }

    public void writeEvent(SelectionEvent event) {
        Preconditions.checkNotNull(event);
        LogMaker log = new LogMaker(1100).setType(getLogType(event)).setSubtype(getLogSubType(event)).setPackageName(event.getPackageName()).addTaggedData(1117, Long.valueOf(event.getDurationSinceSessionStart())).addTaggedData(1118, Long.valueOf(event.getDurationSincePreviousEvent())).addTaggedData(1120, Integer.valueOf(event.getEventIndex())).addTaggedData(1255, event.getWidgetType()).addTaggedData(1262, event.getWidgetVersion()).addTaggedData(1254, event.getEntityType()).addTaggedData(1250, Integer.valueOf(event.getStart())).addTaggedData(1251, Integer.valueOf(event.getEnd()));
        if (isPlatformLocalTextClassifierSmartSelection(event.getResultId())) {
            log.addTaggedData(1256, SignatureParser.getModelName(event.getResultId())).addTaggedData(1252, Integer.valueOf(event.getSmartStart())).addTaggedData(1253, Integer.valueOf(event.getSmartEnd()));
        }
        if (event.getSessionId() != null) {
            log.addTaggedData(1119, event.getSessionId().flattenToString());
        }
        this.mMetricsLogger.write(log);
        debugLog(log);
    }

    private static int getLogType(SelectionEvent event) {
        int eventType = event.getEventType();
        if (eventType == 1) {
            return 1101;
        }
        if (eventType == 2) {
            return 1102;
        }
        if (eventType == 3) {
            return 1105;
        }
        if (eventType == 4) {
            return 1106;
        }
        if (eventType == 5) {
            return 1107;
        }
        if (eventType == 200) {
            return 1103;
        }
        if (eventType == 201) {
            return 1104;
        }
        switch (eventType) {
            case 100:
                return 1108;
            case 101:
                return 1109;
            case 102:
                return 1110;
            case 103:
                return 1111;
            case 104:
                return 1112;
            case 105:
                return 1113;
            case 106:
                return 1114;
            case 107:
                return 1115;
            case 108:
                return 1116;
            default:
                return 0;
        }
    }

    private static int getLogSubType(SelectionEvent event) {
        int invocationMethod = event.getInvocationMethod();
        if (invocationMethod == 1) {
            return 1;
        }
        if (invocationMethod != 2) {
            return 0;
        }
        return 2;
    }

    private static String getLogTypeString(int logType) {
        switch (logType) {
            case 1101:
                return "SELECTION_STARTED";
            case 1102:
                return "SELECTION_MODIFIED";
            case 1103:
                return "SELECT_ALL";
            case 1104:
                return "RESET";
            case 1105:
                return "SMART_SELECTION_SINGLE";
            case 1106:
                return "SMART_SELECTION_MULTI";
            case 1107:
                return "AUTO_SELECTION";
            case 1108:
                return "OVERTYPE";
            case 1109:
                return "COPY";
            case 1110:
                return "PASTE";
            case 1111:
                return "CUT";
            case 1112:
                return "SHARE";
            case 1113:
                return "SMART_SHARE";
            case 1114:
                return "DRAG";
            case 1115:
                return "ABANDON";
            case 1116:
                return "OTHER";
            default:
                return "unknown";
        }
    }

    private static String getLogSubTypeString(int logSubType) {
        if (logSubType == 1) {
            return "MANUAL";
        }
        if (logSubType != 2) {
            return "unknown";
        }
        return "LINK";
    }

    static boolean isPlatformLocalTextClassifierSmartSelection(String signature) {
        return "androidtc".equals(SignatureParser.getClassifierId(signature));
    }

    private static void debugLog(LogMaker log) {
        String widget;
        if (Log.ENABLE_FULL_LOGGING) {
            String widgetType = Objects.toString(log.getTaggedData(1255), "unknown");
            String widgetVersion = Objects.toString(log.getTaggedData(1262), "");
            if (widgetVersion.isEmpty()) {
                widget = widgetType;
            } else {
                widget = widgetType + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + widgetVersion;
            }
            int index = Integer.parseInt(Objects.toString(log.getTaggedData(1120), "0"));
            if (log.getType() == 1101) {
                String sessionId = Objects.toString(log.getTaggedData(1119), "");
                Log.d(LOG_TAG, String.format("New selection session: %s (%s)", widget, sessionId.substring(sessionId.lastIndexOf(NativeLibraryHelper.CLEAR_ABI_OVERRIDE) + 1)));
            }
            String model = Objects.toString(log.getTaggedData(1256), "unknown");
            String entity = Objects.toString(log.getTaggedData(1254), "unknown");
            Log.v(LOG_TAG, String.format(Locale.US, "%2d: %s/%s/%s, range=%d,%d - smart_range=%d,%d (%s/%s)", Integer.valueOf(index), getLogTypeString(log.getType()), getLogSubTypeString(log.getSubtype()), entity, Integer.valueOf(Integer.parseInt(Objects.toString(log.getTaggedData(1250), "0"))), Integer.valueOf(Integer.parseInt(Objects.toString(log.getTaggedData(1251), "0"))), Integer.valueOf(Integer.parseInt(Objects.toString(log.getTaggedData(1252), "0"))), Integer.valueOf(Integer.parseInt(Objects.toString(log.getTaggedData(1253), "0"))), widget, model));
        }
    }

    public static BreakIterator getTokenIterator(Locale locale) {
        return BreakIterator.getWordInstance((Locale) Preconditions.checkNotNull(locale));
    }

    public static String createId(String text, int start, int end, Context context, int modelVersion, List<Locale> locales) {
        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(locales);
        StringJoiner localesJoiner = new StringJoiner(SmsManager.REGEX_PREFIX_DELIMITER);
        for (Locale locale : locales) {
            localesJoiner.add(locale.toLanguageTag());
        }
        return SignatureParser.createSignature("androidtc", String.format(Locale.US, "%s_v%d", localesJoiner.toString(), Integer.valueOf(modelVersion)), Objects.hash(text, Integer.valueOf(start), Integer.valueOf(end), context.getPackageName()));
    }

    @VisibleForTesting
    public static final class SignatureParser {
        static String createSignature(String classifierId, String modelName, int hash) {
            return String.format(Locale.US, "%s|%s|%d", classifierId, modelName, Integer.valueOf(hash));
        }

        static String getClassifierId(String signature) {
            int end;
            if (signature != null && (end = signature.indexOf("|")) >= 0) {
                return signature.substring(0, end);
            }
            return "";
        }

        static String getModelName(String signature) {
            if (signature == null) {
                return "";
            }
            int start = signature.indexOf("|") + 1;
            int end = signature.indexOf("|", start);
            if (start < 1 || end < start) {
                return "";
            }
            return signature.substring(start, end);
        }

        static int getHash(String signature) {
            int index2;
            if (signature != null && (index2 = signature.indexOf("|", signature.indexOf("|"))) > 0) {
                return Integer.parseInt(signature.substring(index2));
            }
            return 0;
        }
    }
}
