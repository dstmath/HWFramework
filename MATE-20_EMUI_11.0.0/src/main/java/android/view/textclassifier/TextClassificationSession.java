package android.view.textclassifier;

import android.view.textclassifier.TextClassification;
import android.view.textclassifier.TextLinks;
import android.view.textclassifier.TextSelection;
import com.android.internal.util.Preconditions;

/* access modifiers changed from: package-private */
public final class TextClassificationSession implements TextClassifier {
    private static final String LOG_TAG = "TextClassificationSession";
    private final TextClassificationContext mClassificationContext;
    private final TextClassifier mDelegate;
    private boolean mDestroyed;
    private final SelectionEventHelper mEventHelper = new SelectionEventHelper(this.mSessionId, this.mClassificationContext);
    private final TextClassificationSessionId mSessionId = new TextClassificationSessionId();

    TextClassificationSession(TextClassificationContext context, TextClassifier delegate) {
        this.mClassificationContext = (TextClassificationContext) Preconditions.checkNotNull(context);
        this.mDelegate = (TextClassifier) Preconditions.checkNotNull(delegate);
        initializeRemoteSession();
    }

    @Override // android.view.textclassifier.TextClassifier
    public TextSelection suggestSelection(TextSelection.Request request) {
        checkDestroyed();
        return this.mDelegate.suggestSelection(request);
    }

    private void initializeRemoteSession() {
        TextClassifier textClassifier = this.mDelegate;
        if (textClassifier instanceof SystemTextClassifier) {
            ((SystemTextClassifier) textClassifier).initializeRemoteSession(this.mClassificationContext, this.mSessionId);
        }
    }

    @Override // android.view.textclassifier.TextClassifier
    public TextClassification classifyText(TextClassification.Request request) {
        checkDestroyed();
        return this.mDelegate.classifyText(request);
    }

    @Override // android.view.textclassifier.TextClassifier
    public TextLinks generateLinks(TextLinks.Request request) {
        checkDestroyed();
        return this.mDelegate.generateLinks(request);
    }

    @Override // android.view.textclassifier.TextClassifier
    public void onSelectionEvent(SelectionEvent event) {
        try {
            if (this.mEventHelper.sanitizeEvent(event)) {
                this.mDelegate.onSelectionEvent(event);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error reporting text classifier selection event", e);
        }
    }

    @Override // android.view.textclassifier.TextClassifier
    public void onTextClassifierEvent(TextClassifierEvent event) {
        try {
            event.mHiddenTempSessionId = this.mSessionId;
            this.mDelegate.onTextClassifierEvent(event);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error reporting text classifier event", e);
        }
    }

    @Override // android.view.textclassifier.TextClassifier
    public void destroy() {
        this.mEventHelper.endSession();
        this.mDelegate.destroy();
        this.mDestroyed = true;
    }

    @Override // android.view.textclassifier.TextClassifier
    public boolean isDestroyed() {
        return this.mDestroyed;
    }

    private void checkDestroyed() {
        if (this.mDestroyed) {
            throw new IllegalStateException("This TextClassification session has been destroyed");
        }
    }

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
                int eventType = event.getEventType();
                if (eventType == 1) {
                    if (event.getAbsoluteEnd() == event.getAbsoluteStart() + 1) {
                        z = true;
                    }
                    Preconditions.checkArgument(z);
                    event.setSessionId(this.mSessionId);
                    this.mStartEvent = event;
                } else if (eventType == 2) {
                    SelectionEvent selectionEvent = this.mPrevEvent;
                    if (selectionEvent != null && selectionEvent.getAbsoluteStart() == event.getAbsoluteStart() && this.mPrevEvent.getAbsoluteEnd() == event.getAbsoluteEnd()) {
                        return false;
                    }
                } else if (eventType == 3 || eventType == 4 || eventType == 5) {
                    this.mSmartEvent = event;
                }
                event.setEventTime(now);
                SelectionEvent selectionEvent2 = this.mStartEvent;
                if (selectionEvent2 != null) {
                    event.setSessionId(selectionEvent2.getSessionId()).setDurationSinceSessionStart(now - this.mStartEvent.getEventTime()).setStart(event.getAbsoluteStart() - this.mStartEvent.getAbsoluteStart()).setEnd(event.getAbsoluteEnd() - this.mStartEvent.getAbsoluteStart());
                }
                SelectionEvent selectionEvent3 = this.mSmartEvent;
                if (selectionEvent3 != null) {
                    event.setResultId(selectionEvent3.getResultId()).setSmartStart(this.mSmartEvent.getAbsoluteStart() - this.mStartEvent.getAbsoluteStart()).setSmartEnd(this.mSmartEvent.getAbsoluteEnd() - this.mStartEvent.getAbsoluteStart());
                }
                SelectionEvent selectionEvent4 = this.mPrevEvent;
                if (selectionEvent4 != null) {
                    event.setDurationSincePreviousEvent(now - selectionEvent4.getEventTime()).setEventIndex(this.mPrevEvent.getEventIndex() + 1);
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
            int eventType = event.getEventType();
            if (eventType != 3 && eventType != 4 && eventType != 5) {
                return;
            }
            if (!SelectionSessionLogger.isPlatformLocalTextClassifierSmartSelection(event.getResultId())) {
                event.setEventType(5);
            } else if (event.getAbsoluteEnd() - event.getAbsoluteStart() > 1) {
                event.setEventType(4);
            } else {
                event.setEventType(3);
            }
        }
    }
}
