package android.view.textclassifier.logging;

import android.content.Context;
import android.metrics.LogMaker;
import android.util.FrameworkTagConstant;
import android.util.Log;
import android.view.textclassifier.TextClassification;
import android.view.textclassifier.TextClassifier;
import android.view.textclassifier.TextSelection;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;
import java.util.UUID;

public final class SmartSelectionEventTracker {
    private static final String CUSTOM_EDITTEXT = "customedit";
    private static final String CUSTOM_TEXTVIEW = "customview";
    private static final String CUSTOM_UNSELECTABLE_TEXTVIEW = "nosel-customview";
    private static final boolean DEBUG_LOG_ENABLED = true;
    private static final String EDITTEXT = "edittext";
    private static final String EDIT_WEBVIEW = "edit-webview";
    private static final int ENTITY_TYPE = 1254;
    private static final int EVENT_END = 1251;
    private static final int EVENT_START = 1250;
    private static final int INDEX = 1120;
    private static final String LOG_TAG = "SmartSelectEventTracker";
    private static final int MODEL_NAME = 1256;
    private static final int PREV_EVENT_DELTA = 1118;
    private static final int SESSION_ID = 1119;
    private static final int SMART_END = 1253;
    private static final int SMART_START = 1252;
    private static final int START_EVENT_DELTA = 1117;
    private static final String TEXTVIEW = "textview";
    private static final String UNKNOWN = "unknown";
    private static final String UNSELECTABLE_TEXTVIEW = "nosel-textview";
    private static final String WEBVIEW = "webview";
    private static final int WIDGET_TYPE = 1255;
    private static final int WIDGET_VERSION = 1262;
    private static final String ZERO = "0";
    private final Context mContext;
    private int mIndex;
    private long mLastEventTime;
    private final MetricsLogger mMetricsLogger = new MetricsLogger();
    private String mModelName;
    private int mOrigStart;
    private final int[] mPrevIndices = new int[2];
    private String mSessionId;
    private long mSessionStartTime;
    private final int[] mSmartIndices = new int[2];
    private boolean mSmartSelectionTriggered;
    private final int mWidgetType;
    private final String mWidgetVersion;

    public static final class SelectionEvent {
        private static final String NO_VERSION_TAG = "";
        public static final int OUT_OF_BOUNDS = Integer.MAX_VALUE;
        public static final int OUT_OF_BOUNDS_NEGATIVE = Integer.MIN_VALUE;
        /* access modifiers changed from: private */
        public final int mEnd;
        /* access modifiers changed from: private */
        public final String mEntityType;
        /* access modifiers changed from: private */
        public int mEventType;
        /* access modifiers changed from: private */
        public final int mStart;
        /* access modifiers changed from: private */
        public final String mVersionTag;

        @Retention(RetentionPolicy.SOURCE)
        public @interface ActionType {
            public static final int ABANDON = 107;
            public static final int COPY = 101;
            public static final int CUT = 103;
            public static final int DRAG = 106;
            public static final int OTHER = 108;
            public static final int OVERTYPE = 100;
            public static final int PASTE = 102;
            public static final int RESET = 201;
            public static final int SELECT_ALL = 200;
            public static final int SHARE = 104;
            public static final int SMART_SHARE = 105;
        }

        @Retention(RetentionPolicy.SOURCE)
        private @interface EventType {
            public static final int AUTO_SELECTION = 5;
            public static final int SELECTION_MODIFIED = 2;
            public static final int SELECTION_STARTED = 1;
            public static final int SMART_SELECTION_MULTI = 4;
            public static final int SMART_SELECTION_SINGLE = 3;
        }

        private SelectionEvent(int start, int end, int eventType, String entityType, String versionTag) {
            Preconditions.checkArgument(end >= start, "end cannot be less than start");
            this.mStart = start;
            this.mEnd = end;
            this.mEventType = eventType;
            this.mEntityType = (String) Preconditions.checkNotNull(entityType);
            this.mVersionTag = (String) Preconditions.checkNotNull(versionTag);
        }

        public static SelectionEvent selectionStarted(int start) {
            SelectionEvent selectionEvent = new SelectionEvent(start, start + 1, 1, "", "");
            return selectionEvent;
        }

        public static SelectionEvent selectionModified(int start, int end) {
            SelectionEvent selectionEvent = new SelectionEvent(start, end, 2, "", "");
            return selectionEvent;
        }

        public static SelectionEvent selectionModified(int start, int end, TextClassification classification) {
            String entityType;
            if (classification.getEntityCount() > 0) {
                entityType = classification.getEntity(0);
            } else {
                entityType = "";
            }
            SelectionEvent selectionEvent = new SelectionEvent(start, end, 2, entityType, getVersionInfo(classification.getId()));
            return selectionEvent;
        }

        public static SelectionEvent selectionModified(int start, int end, TextSelection selection) {
            int eventType;
            String entityType;
            if (!getSourceClassifier(selection.getId()).equals(TextClassifier.DEFAULT_LOG_TAG)) {
                eventType = 5;
            } else if (end - start > 1) {
                eventType = 4;
            } else {
                eventType = 3;
            }
            int eventType2 = eventType;
            if (selection.getEntityCount() > 0) {
                entityType = selection.getEntity(0);
            } else {
                entityType = "";
            }
            SelectionEvent selectionEvent = new SelectionEvent(start, end, eventType2, entityType, getVersionInfo(selection.getId()));
            return selectionEvent;
        }

        public static SelectionEvent selectionAction(int start, int end, int actionType) {
            SelectionEvent selectionEvent = new SelectionEvent(start, end, actionType, "", "");
            return selectionEvent;
        }

        public static SelectionEvent selectionAction(int start, int end, int actionType, TextClassification classification) {
            String entityType;
            if (classification.getEntityCount() > 0) {
                entityType = classification.getEntity(0);
            } else {
                entityType = "";
            }
            SelectionEvent selectionEvent = new SelectionEvent(start, end, actionType, entityType, getVersionInfo(classification.getId()));
            return selectionEvent;
        }

        private static String getVersionInfo(String signature) {
            int start = signature.indexOf("|");
            int end = signature.indexOf("|", start);
            if (start < 0 || end < start) {
                return "";
            }
            return signature.substring(start, end);
        }

        private static String getSourceClassifier(String signature) {
            int end = signature.indexOf("|");
            if (end >= 0) {
                return signature.substring(0, end);
            }
            return "";
        }

        /* access modifiers changed from: private */
        public boolean isTerminal() {
            switch (this.mEventType) {
                case 100:
                case 101:
                case 102:
                case 103:
                case 104:
                case 105:
                case 106:
                case 107:
                case 108:
                    return true;
                default:
                    return false;
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface WidgetType {
        public static final int CUSTOM_EDITTEXT = 7;
        public static final int CUSTOM_TEXTVIEW = 6;
        public static final int CUSTOM_UNSELECTABLE_TEXTVIEW = 8;
        public static final int EDITTEXT = 3;
        public static final int EDIT_WEBVIEW = 4;
        public static final int TEXTVIEW = 1;
        public static final int UNSELECTABLE_TEXTVIEW = 5;
        public static final int UNSPECIFIED = 0;
        public static final int WEBVIEW = 2;
    }

    public SmartSelectionEventTracker(Context context, int widgetType) {
        this.mWidgetType = widgetType;
        this.mWidgetVersion = null;
        this.mContext = (Context) Preconditions.checkNotNull(context);
    }

    public SmartSelectionEventTracker(Context context, int widgetType, String widgetVersion) {
        this.mWidgetType = widgetType;
        this.mWidgetVersion = widgetVersion;
        this.mContext = (Context) Preconditions.checkNotNull(context);
    }

    public void logEvent(SelectionEvent event) {
        Preconditions.checkNotNull(event);
        boolean z = true;
        if (event.mEventType == 1 || this.mSessionId != null) {
            long now = System.currentTimeMillis();
            switch (event.mEventType) {
                case 1:
                    this.mSessionId = startNewSession();
                    if (event.mEnd != event.mStart + 1) {
                        z = false;
                    }
                    Preconditions.checkArgument(z);
                    this.mOrigStart = event.mStart;
                    this.mSessionStartTime = now;
                    break;
                case 2:
                case 5:
                    if (this.mPrevIndices[0] == event.mStart && this.mPrevIndices[1] == event.mEnd) {
                        return;
                    }
                case 3:
                case 4:
                    this.mSmartSelectionTriggered = true;
                    this.mModelName = getModelName(event);
                    this.mSmartIndices[0] = event.mStart;
                    this.mSmartIndices[1] = event.mEnd;
                    break;
            }
            writeEvent(event, now);
            if (event.isTerminal()) {
                endSession();
            }
            return;
        }
        Log.d(LOG_TAG, "Selection session not yet started. Ignoring event");
    }

    private void writeEvent(SelectionEvent event, long now) {
        long prevEventDelta = 0;
        if (this.mLastEventTime != 0) {
            prevEventDelta = now - this.mLastEventTime;
        }
        LogMaker log = new LogMaker(1100).setType(getLogType(event)).setSubtype(1).setPackageName(this.mContext.getPackageName()).addTaggedData(START_EVENT_DELTA, Long.valueOf(now - this.mSessionStartTime)).addTaggedData(PREV_EVENT_DELTA, Long.valueOf(prevEventDelta)).addTaggedData(INDEX, Integer.valueOf(this.mIndex)).addTaggedData(WIDGET_TYPE, getWidgetTypeName()).addTaggedData(WIDGET_VERSION, this.mWidgetVersion).addTaggedData(MODEL_NAME, this.mModelName).addTaggedData(ENTITY_TYPE, event.mEntityType).addTaggedData(SMART_START, Integer.valueOf(getSmartRangeDelta(this.mSmartIndices[0]))).addTaggedData(SMART_END, Integer.valueOf(getSmartRangeDelta(this.mSmartIndices[1]))).addTaggedData(EVENT_START, Integer.valueOf(getRangeDelta(event.mStart))).addTaggedData(EVENT_END, Integer.valueOf(getRangeDelta(event.mEnd))).addTaggedData(SESSION_ID, this.mSessionId);
        this.mMetricsLogger.write(log);
        debugLog(log);
        this.mLastEventTime = now;
        this.mPrevIndices[0] = event.mStart;
        this.mPrevIndices[1] = event.mEnd;
        this.mIndex++;
    }

    private String startNewSession() {
        endSession();
        this.mSessionId = createSessionId();
        return this.mSessionId;
    }

    private void endSession() {
        this.mOrigStart = 0;
        int[] iArr = this.mSmartIndices;
        this.mSmartIndices[1] = 0;
        iArr[0] = 0;
        int[] iArr2 = this.mPrevIndices;
        this.mPrevIndices[1] = 0;
        iArr2[0] = 0;
        this.mIndex = 0;
        this.mSessionStartTime = 0;
        this.mLastEventTime = 0;
        this.mSmartSelectionTriggered = false;
        this.mModelName = getModelName(null);
        this.mSessionId = null;
    }

    private static int getLogType(SelectionEvent event) {
        int access$000 = event.mEventType;
        switch (access$000) {
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
                switch (access$000) {
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
                        switch (access$000) {
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

    private int getRangeDelta(int offset) {
        return offset - this.mOrigStart;
    }

    private int getSmartRangeDelta(int offset) {
        if (this.mSmartSelectionTriggered) {
            return getRangeDelta(offset);
        }
        return 0;
    }

    private String getWidgetTypeName() {
        switch (this.mWidgetType) {
            case 1:
                return "textview";
            case 2:
                return "webview";
            case 3:
                return "edittext";
            case 4:
                return "edit-webview";
            case 5:
                return "nosel-textview";
            case 6:
                return "customview";
            case 7:
                return "customedit";
            case 8:
                return "nosel-customview";
            default:
                return "unknown";
        }
    }

    private String getModelName(SelectionEvent event) {
        if (event == null) {
            return "";
        }
        return Objects.toString(event.mVersionTag, "");
    }

    private static String createSessionId() {
        return UUID.randomUUID().toString();
    }

    private static void debugLog(LogMaker log) {
        String widget;
        LogMaker logMaker = log;
        String widgetType = Objects.toString(logMaker.getTaggedData(WIDGET_TYPE), "unknown");
        if (Objects.toString(logMaker.getTaggedData(WIDGET_VERSION), "").isEmpty()) {
            widget = widgetType;
        } else {
            widget = widgetType + "-" + widgetVersion;
        }
        int index = Integer.parseInt(Objects.toString(logMaker.getTaggedData(INDEX), "0"));
        if (log.getType() == 1101) {
            String sessionId = Objects.toString(logMaker.getTaggedData(SESSION_ID), "");
            Log.d(LOG_TAG, String.format("New selection session: %s (%s)", new Object[]{widget, sessionId.substring(sessionId.lastIndexOf("-") + 1)}));
        }
        String model = Objects.toString(logMaker.getTaggedData(MODEL_NAME), "unknown");
        String entity = Objects.toString(logMaker.getTaggedData(ENTITY_TYPE), "unknown");
        Log.d(LOG_TAG, String.format("%2d: %s/%s, range=%d,%d - smart_range=%d,%d (%s/%s)", new Object[]{Integer.valueOf(index), getLogTypeString(log.getType()), entity, Integer.valueOf(Integer.parseInt(Objects.toString(logMaker.getTaggedData(EVENT_START), "0"))), Integer.valueOf(Integer.parseInt(Objects.toString(logMaker.getTaggedData(EVENT_END), "0"))), Integer.valueOf(Integer.parseInt(Objects.toString(logMaker.getTaggedData(SMART_START), "0"))), Integer.valueOf(Integer.parseInt(Objects.toString(logMaker.getTaggedData(SMART_END), "0"))), widget, model}));
    }
}
