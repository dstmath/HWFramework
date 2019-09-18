package android.view.textclassifier;

import android.content.Context;
import android.metrics.LogMaker;
import android.util.FrameworkTagConstant;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.Preconditions;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

public final class SelectionSessionLogger {
    static final String CLASSIFIER_ID = "androidtc";
    private static final boolean DEBUG_LOG_ENABLED = false;
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

    @VisibleForTesting
    public static final class SignatureParser {
        static String createSignature(String classifierId, String modelName, int hash) {
            return String.format(Locale.US, "%s|%s|%d", new Object[]{classifierId, modelName, Integer.valueOf(hash)});
        }

        static String getClassifierId(String signature) {
            if (signature == null) {
                return "";
            }
            int end = signature.indexOf("|");
            if (end >= 0) {
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
            if (signature == null) {
                return 0;
            }
            int index2 = signature.indexOf("|", signature.indexOf("|"));
            if (index2 > 0) {
                return Integer.parseInt(signature.substring(index2));
            }
            return 0;
        }
    }

    public SelectionSessionLogger() {
        this.mMetricsLogger = new MetricsLogger();
    }

    @VisibleForTesting
    public SelectionSessionLogger(MetricsLogger metricsLogger) {
        this.mMetricsLogger = (MetricsLogger) Preconditions.checkNotNull(metricsLogger);
    }

    public void writeEvent(SelectionEvent event) {
        Preconditions.checkNotNull(event);
        LogMaker log = new LogMaker(1100).setType(getLogType(event)).setSubtype(getLogSubType(event)).setPackageName(event.getPackageName()).addTaggedData(START_EVENT_DELTA, Long.valueOf(event.getDurationSinceSessionStart())).addTaggedData(PREV_EVENT_DELTA, Long.valueOf(event.getDurationSincePreviousEvent())).addTaggedData(INDEX, Integer.valueOf(event.getEventIndex())).addTaggedData(WIDGET_TYPE, event.getWidgetType()).addTaggedData(WIDGET_VERSION, event.getWidgetVersion()).addTaggedData(MODEL_NAME, SignatureParser.getModelName(event.getResultId())).addTaggedData(ENTITY_TYPE, event.getEntityType()).addTaggedData(SMART_START, Integer.valueOf(event.getSmartStart())).addTaggedData(SMART_END, Integer.valueOf(event.getSmartEnd())).addTaggedData(EVENT_START, Integer.valueOf(event.getStart())).addTaggedData(EVENT_END, Integer.valueOf(event.getEnd()));
        if (event.getSessionId() != null) {
            log.addTaggedData(SESSION_ID, event.getSessionId().flattenToString());
        }
        this.mMetricsLogger.write(log);
        debugLog(log);
    }

    private static int getLogType(SelectionEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case 1:
                return FrameworkTagConstant.HWTAG_SENSOR_HUB;
            case 2:
                return FrameworkTagConstant.HWTAG_SENSOR_GESTURE;
            case 3:
                return 1105;
            case 4:
                return 1106;
            case 5:
                return 1107;
            default:
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
                        switch (eventType) {
                            case 200:
                                return 1103;
                            case 201:
                                return 1104;
                            default:
                                return 0;
                        }
                }
        }
    }

    private static int getLogSubType(SelectionEvent event) {
        switch (event.getInvocationMethod()) {
            case 1:
                return 1;
            case 2:
                return 2;
            default:
                return 0;
        }
    }

    private static String getLogTypeString(int logType) {
        switch (logType) {
            case FrameworkTagConstant.HWTAG_SENSOR_HUB:
                return "SELECTION_STARTED";
            case FrameworkTagConstant.HWTAG_SENSOR_GESTURE:
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
        switch (logSubType) {
            case 1:
                return "MANUAL";
            case 2:
                return "LINK";
            default:
                return "unknown";
        }
    }

    private static void debugLog(LogMaker log) {
    }

    public static BreakIterator getTokenIterator(Locale locale) {
        return BreakIterator.getWordInstance((Locale) Preconditions.checkNotNull(locale));
    }

    public static String createId(String text, int start, int end, Context context, int modelVersion, List<Locale> locales) {
        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(locales);
        StringJoiner localesJoiner = new StringJoiner(",");
        for (Locale locale : locales) {
            localesJoiner.add(locale.toLanguageTag());
        }
        return SignatureParser.createSignature("androidtc", String.format(Locale.US, "%s_v%d", new Object[]{localesJoiner.toString(), Integer.valueOf(modelVersion)}), Objects.hash(new Object[]{text, Integer.valueOf(start), Integer.valueOf(end), context.getPackageName()}));
    }
}
