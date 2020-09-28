package android.view.textclassifier.logging;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.metrics.LogMaker;
import android.util.Log;
import android.view.textclassifier.TextClassification;
import android.view.textclassifier.TextClassifier;
import android.view.textclassifier.TextSelection;
import com.android.internal.content.NativeLibraryHelper;
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
    private final MetricsLogger mMetricsLogger;
    private String mModelName;
    private int mOrigStart;
    private final int[] mPrevIndices;
    private String mSessionId;
    private long mSessionStartTime;
    private final int[] mSmartIndices;
    private boolean mSmartSelectionTriggered;
    private final int mWidgetType;
    private final String mWidgetVersion;

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

    @UnsupportedAppUsage
    public SmartSelectionEventTracker(Context context, int widgetType) {
        this.mMetricsLogger = new MetricsLogger();
        this.mSmartIndices = new int[2];
        this.mPrevIndices = new int[2];
        this.mWidgetType = widgetType;
        this.mWidgetVersion = null;
        this.mContext = (Context) Preconditions.checkNotNull(context);
    }

    public SmartSelectionEventTracker(Context context, int widgetType, String widgetVersion) {
        this.mMetricsLogger = new MetricsLogger();
        this.mSmartIndices = new int[2];
        this.mPrevIndices = new int[2];
        this.mWidgetType = widgetType;
        this.mWidgetVersion = widgetVersion;
        this.mContext = (Context) Preconditions.checkNotNull(context);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002b, code lost:
        if (r0 != 5) goto L_0x007b;
     */
    @UnsupportedAppUsage
    public void logEvent(SelectionEvent event) {
        Preconditions.checkNotNull(event);
        boolean z = true;
        if (event.mEventType == 1 || this.mSessionId != null) {
            long now = System.currentTimeMillis();
            int i = event.mEventType;
            if (i != 1) {
                if (i != 2) {
                    if (i == 3 || i == 4) {
                        this.mSmartSelectionTriggered = true;
                        this.mModelName = getModelName(event);
                        this.mSmartIndices[0] = event.mStart;
                        this.mSmartIndices[1] = event.mEnd;
                    }
                }
                if (this.mPrevIndices[0] == event.mStart && this.mPrevIndices[1] == event.mEnd) {
                    return;
                }
            } else {
                this.mSessionId = startNewSession();
                if (event.mEnd != event.mStart + 1) {
                    z = false;
                }
                Preconditions.checkArgument(z);
                this.mOrigStart = event.mStart;
                this.mSessionStartTime = now;
            }
            writeEvent(event, now);
            if (event.isTerminal()) {
                endSession();
                return;
            }
            return;
        }
        Log.d(LOG_TAG, "Selection session not yet started. Ignoring event");
    }

    private void writeEvent(SelectionEvent event, long now) {
        long j = this.mLastEventTime;
        long prevEventDelta = 0;
        if (j != 0) {
            prevEventDelta = now - j;
        }
        LogMaker log = new LogMaker(1100).setType(getLogType(event)).setSubtype(1).setPackageName(this.mContext.getPackageName()).addTaggedData(1117, Long.valueOf(now - this.mSessionStartTime)).addTaggedData(1118, Long.valueOf(prevEventDelta)).addTaggedData(1120, Integer.valueOf(this.mIndex)).addTaggedData(1255, getWidgetTypeName()).addTaggedData(1262, this.mWidgetVersion).addTaggedData(1256, this.mModelName).addTaggedData(1254, event.mEntityType).addTaggedData(1252, Integer.valueOf(getSmartRangeDelta(this.mSmartIndices[0]))).addTaggedData(1253, Integer.valueOf(getSmartRangeDelta(this.mSmartIndices[1]))).addTaggedData(1250, Integer.valueOf(getRangeDelta(event.mStart))).addTaggedData(1251, Integer.valueOf(getRangeDelta(event.mEnd))).addTaggedData(1119, this.mSessionId);
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
        iArr[1] = 0;
        iArr[0] = 0;
        int[] iArr2 = this.mPrevIndices;
        iArr2[1] = 0;
        iArr2[0] = 0;
        this.mIndex = 0;
        this.mSessionStartTime = 0;
        this.mLastEventTime = 0;
        this.mSmartSelectionTriggered = false;
        this.mModelName = getModelName(null);
        this.mSessionId = null;
    }

    private static int getLogType(SelectionEvent event) {
        int i = event.mEventType;
        if (i == 1) {
            return 1101;
        }
        if (i == 2) {
            return 1102;
        }
        if (i == 3) {
            return 1105;
        }
        if (i == 4) {
            return 1106;
        }
        if (i == 5) {
            return 1107;
        }
        if (i == 200) {
            return 1103;
        }
        if (i == 201) {
            return 1104;
        }
        switch (i) {
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
        Log.d(LOG_TAG, String.format("%2d: %s/%s, range=%d,%d - smart_range=%d,%d (%s/%s)", Integer.valueOf(index), getLogTypeString(log.getType()), entity, Integer.valueOf(Integer.parseInt(Objects.toString(log.getTaggedData(1250), "0"))), Integer.valueOf(Integer.parseInt(Objects.toString(log.getTaggedData(1251), "0"))), Integer.valueOf(Integer.parseInt(Objects.toString(log.getTaggedData(1252), "0"))), Integer.valueOf(Integer.parseInt(Objects.toString(log.getTaggedData(1253), "0"))), widget, model));
    }

    public static final class SelectionEvent {
        private static final String NO_VERSION_TAG = "";
        public static final int OUT_OF_BOUNDS = Integer.MAX_VALUE;
        public static final int OUT_OF_BOUNDS_NEGATIVE = Integer.MIN_VALUE;
        private final int mEnd;
        private final String mEntityType;
        private int mEventType;
        private final int mStart;
        private final String mVersionTag;

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

        @UnsupportedAppUsage
        public static SelectionEvent selectionStarted(int start) {
            return new SelectionEvent(start, start + 1, 1, "", "");
        }

        @UnsupportedAppUsage
        public static SelectionEvent selectionModified(int start, int end) {
            return new SelectionEvent(start, end, 2, "", "");
        }

        @UnsupportedAppUsage
        public static SelectionEvent selectionModified(int start, int end, TextClassification classification) {
            String entityType;
            if (classification.getEntityCount() > 0) {
                entityType = classification.getEntity(0);
            } else {
                entityType = "";
            }
            return new SelectionEvent(start, end, 2, entityType, getVersionInfo(classification.getId()));
        }

        @UnsupportedAppUsage
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
            if (selection.getEntityCount() > 0) {
                entityType = selection.getEntity(0);
            } else {
                entityType = "";
            }
            return new SelectionEvent(start, end, eventType, entityType, getVersionInfo(selection.getId()));
        }

        @UnsupportedAppUsage
        public static SelectionEvent selectionAction(int start, int end, int actionType) {
            return new SelectionEvent(start, end, actionType, "", "");
        }

        @UnsupportedAppUsage
        public static SelectionEvent selectionAction(int start, int end, int actionType, TextClassification classification) {
            String entityType;
            if (classification.getEntityCount() > 0) {
                entityType = classification.getEntity(0);
            } else {
                entityType = "";
            }
            return new SelectionEvent(start, end, actionType, entityType, getVersionInfo(classification.getId()));
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
        /* access modifiers changed from: public */
        private boolean isTerminal() {
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
}
