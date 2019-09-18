package android.view.textclassifier;

import android.view.textclassifier.SelectionSessionLogger;
import android.view.textclassifier.TextClassification;
import android.view.textclassifier.TextLinks;
import android.view.textclassifier.TextSelection;
import com.android.internal.util.Preconditions;

final class TextClassificationSession implements TextClassifier {
    static final boolean DEBUG_LOG_ENABLED = true;
    private static final String LOG_TAG = "TextClassificationSession";
    private final TextClassificationContext mClassificationContext;
    private final TextClassifier mDelegate;
    private boolean mDestroyed;
    private final SelectionEventHelper mEventHelper = new SelectionEventHelper(this.mSessionId, this.mClassificationContext);
    private final TextClassificationSessionId mSessionId = new TextClassificationSessionId();

    private static final class SelectionEventHelper {
        private final TextClassificationContext mContext;
        private int mInvocationMethod = 0;
        private SelectionEvent mPrevEvent;
        private final TextClassificationSessionId mSessionId;
        private SelectionEvent mSmartEvent;
        private SelectionEvent mStartEvent;

        SelectionEventHelper(TextClassificationSessionId sessionId, TextClassificationContext context) {
            this.mSessionId = (TextClassificationSessionId) Preconditions.checkNotNull(sessionId);
            this.mContext = (TextClassificationContext) Preconditions.checkNotNull(context);
        }

        /* access modifiers changed from: package-private */
        public boolean sanitizeEvent(SelectionEvent event) {
            updateInvocationMethod(event);
            modifyAutoSelectionEventType(event);
            boolean z = false;
            if (event.getEventType() == 1 || this.mStartEvent != null) {
                long now = System.currentTimeMillis();
                switch (event.getEventType()) {
                    case 1:
                        if (event.getAbsoluteEnd() == event.getAbsoluteStart() + 1) {
                            z = true;
                        }
                        Preconditions.checkArgument(z);
                        event.setSessionId(this.mSessionId);
                        this.mStartEvent = event;
                        break;
                    case 2:
                    case 5:
                        if (this.mPrevEvent != null && this.mPrevEvent.getAbsoluteStart() == event.getAbsoluteStart() && this.mPrevEvent.getAbsoluteEnd() == event.getAbsoluteEnd()) {
                            return false;
                        }
                    case 3:
                    case 4:
                        this.mSmartEvent = event;
                        break;
                }
                event.setEventTime(now);
                if (this.mStartEvent != null) {
                    event.setSessionId(this.mStartEvent.getSessionId()).setDurationSinceSessionStart(now - this.mStartEvent.getEventTime()).setStart(event.getAbsoluteStart() - this.mStartEvent.getAbsoluteStart()).setEnd(event.getAbsoluteEnd() - this.mStartEvent.getAbsoluteStart());
                }
                if (this.mSmartEvent != null) {
                    event.setResultId(this.mSmartEvent.getResultId()).setSmartStart(this.mSmartEvent.getAbsoluteStart() - this.mStartEvent.getAbsoluteStart()).setSmartEnd(this.mSmartEvent.getAbsoluteEnd() - this.mStartEvent.getAbsoluteStart());
                }
                if (this.mPrevEvent != null) {
                    event.setDurationSincePreviousEvent(now - this.mPrevEvent.getEventTime()).setEventIndex(this.mPrevEvent.getEventIndex() + 1);
                }
                this.mPrevEvent = event;
                return true;
            }
            Log.d(TextClassificationSession.LOG_TAG, "Selection session not yet started. Ignoring event");
            return false;
        }

        /* access modifiers changed from: package-private */
        public void endSession() {
            this.mPrevEvent = null;
            this.mSmartEvent = null;
            this.mStartEvent = null;
        }

        private void updateInvocationMethod(SelectionEvent event) {
            event.setTextClassificationSessionContext(this.mContext);
            if (event.getInvocationMethod() == 0) {
                event.setInvocationMethod(this.mInvocationMethod);
            } else {
                this.mInvocationMethod = event.getInvocationMethod();
            }
        }

        private void modifyAutoSelectionEventType(SelectionEvent event) {
            switch (event.getEventType()) {
                case 3:
                case 4:
                case 5:
                    if (!isPlatformLocalTextClassifierSmartSelection(event.getResultId())) {
                        event.setEventType(5);
                    } else if (event.getAbsoluteEnd() - event.getAbsoluteStart() > 1) {
                        event.setEventType(4);
                    } else {
                        event.setEventType(3);
                    }
                    return;
                default:
                    return;
            }
        }

        private static boolean isPlatformLocalTextClassifierSmartSelection(String signature) {
            return TextClassifier.DEFAULT_LOG_TAG.equals(SelectionSessionLogger.SignatureParser.getClassifierId(signature));
        }
    }

    TextClassificationSession(TextClassificationContext context, TextClassifier delegate) {
        this.mClassificationContext = (TextClassificationContext) Preconditions.checkNotNull(context);
        this.mDelegate = (TextClassifier) Preconditions.checkNotNull(delegate);
        initializeRemoteSession();
    }

    public TextSelection suggestSelection(TextSelection.Request request) {
        checkDestroyed();
        return this.mDelegate.suggestSelection(request);
    }

    private void initializeRemoteSession() {
        if (this.mDelegate instanceof SystemTextClassifier) {
            ((SystemTextClassifier) this.mDelegate).initializeRemoteSession(this.mClassificationContext, this.mSessionId);
        }
    }

    public TextClassification classifyText(TextClassification.Request request) {
        checkDestroyed();
        return this.mDelegate.classifyText(request);
    }

    public TextLinks generateLinks(TextLinks.Request request) {
        checkDestroyed();
        return this.mDelegate.generateLinks(request);
    }

    public void onSelectionEvent(SelectionEvent event) {
        checkDestroyed();
        Preconditions.checkNotNull(event);
        if (this.mEventHelper.sanitizeEvent(event)) {
            this.mDelegate.onSelectionEvent(event);
        }
    }

    public void destroy() {
        this.mEventHelper.endSession();
        this.mDelegate.destroy();
        this.mDestroyed = true;
    }

    public boolean isDestroyed() {
        return this.mDestroyed;
    }

    private void checkDestroyed() {
        if (this.mDestroyed) {
            throw new IllegalStateException("This TextClassification session has been destroyed");
        }
    }
}
