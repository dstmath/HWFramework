package android.widget;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.os.LocaleList;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.LogException;
import android.view.ActionMode;
import android.view.textclassifier.TextClassification;
import android.view.textclassifier.TextClassifier;
import android.view.textclassifier.TextSelection;
import android.widget.-$Lambda$tTszxdFZ0V9nXhnBpPsqeBMO0fw.AnonymousClass1;
import android.widget.-$Lambda$tTszxdFZ0V9nXhnBpPsqeBMO0fw.AnonymousClass2;
import android.widget.-$Lambda$tTszxdFZ0V9nXhnBpPsqeBMO0fw.AnonymousClass3;
import android.widget.-$Lambda$tTszxdFZ0V9nXhnBpPsqeBMO0fw.AnonymousClass4;
import android.widget.-$Lambda$tTszxdFZ0V9nXhnBpPsqeBMO0fw.AnonymousClass5;
import com.android.internal.util.Preconditions;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class SelectionActionModeHelper {
    private static final int TIMEOUT_DURATION = 200;
    private final Editor mEditor;
    private final SelectionTracker mSelectionTracker;
    private TextClassification mTextClassification;
    private AsyncTask mTextClassificationAsyncTask;
    private final TextClassificationHelper mTextClassificationHelper;

    private static final class SelectionResult {
        private final TextClassification mClassification;
        private final int mEnd;
        private final int mStart;

        SelectionResult(int start, int end, TextClassification classification) {
            this.mStart = start;
            this.mEnd = end;
            this.mClassification = (TextClassification) Preconditions.checkNotNull(classification);
        }
    }

    private static final class SelectionTracker {
        private static final String LOG_EVENT_MULTI_SELECTION = "textClassifier_multiSelection";
        private static final String LOG_EVENT_MULTI_SELECTION_ACTION = "textClassifier_multiSelection_action";
        private static final String LOG_EVENT_MULTI_SELECTION_MODIFIED = "textClassifier_multiSelection_modified";
        private static final String LOG_EVENT_MULTI_SELECTION_RESET = "textClassifier_multiSelection_reset";
        private static final String LOG_EVENT_SINGLE_SELECTION = "textClassifier_singleSelection";
        private static final String LOG_EVENT_SINGLE_SELECTION_ACTION = "textClassifier_singleSelection_action";
        private static final String LOG_EVENT_SINGLE_SELECTION_MODIFIED = "textClassifier_singleSelection_modified";
        private final TextClassifier mClassifier;
        private boolean mClassifierSelection;
        private boolean mMultiSelection;
        private int mOriginalEnd;
        private int mOriginalStart;
        private int mSelectionEnd;
        private int mSelectionStart;

        SelectionTracker(TextClassifier classifier) {
            this.mClassifier = classifier;
        }

        public void setOriginalSelection(int selectionStart, int selectionEnd) {
            this.mOriginalStart = selectionStart;
            this.mOriginalEnd = selectionEnd;
            resetSelectionFlags();
        }

        public void onSelectionStarted(int selectionStart, int selectionEnd, String logTag) {
            boolean z = true;
            this.mClassifierSelection = logTag.isEmpty() ^ 1;
            this.mSelectionStart = selectionStart;
            this.mSelectionEnd = selectionEnd;
            if (this.mSelectionStart == this.mOriginalStart && this.mSelectionEnd == this.mOriginalEnd) {
                z = false;
            }
            this.mMultiSelection = z;
            if (this.mMultiSelection) {
                this.mClassifier.logEvent(logTag, LOG_EVENT_MULTI_SELECTION);
            } else if (this.mClassifierSelection) {
                this.mClassifier.logEvent(logTag, LOG_EVENT_SINGLE_SELECTION);
            }
        }

        public void onSelectionUpdated(int selectionStart, int selectionEnd, String logTag) {
            boolean selectionChanged = (selectionStart == this.mSelectionStart && selectionEnd == this.mSelectionEnd) ? false : true;
            if (selectionChanged) {
                if (this.mMultiSelection) {
                    this.mClassifier.logEvent(logTag, LOG_EVENT_MULTI_SELECTION_MODIFIED);
                } else if (this.mClassifierSelection) {
                    this.mClassifier.logEvent(logTag, LOG_EVENT_SINGLE_SELECTION_MODIFIED);
                }
                resetSelectionFlags();
            }
        }

        public void onSelectionDestroyed() {
            resetSelectionFlags();
        }

        public void onSelectionAction(String logTag) {
            if (this.mMultiSelection) {
                this.mClassifier.logEvent(logTag, LOG_EVENT_MULTI_SELECTION_ACTION);
            } else if (this.mClassifierSelection) {
                this.mClassifier.logEvent(logTag, LOG_EVENT_SINGLE_SELECTION_ACTION);
            }
        }

        public boolean resetSelection(int textIndex, Editor editor, String logTag) {
            CharSequence text = editor.getTextView().getText();
            if (!this.mMultiSelection || textIndex < this.mSelectionStart || textIndex > this.mSelectionEnd || !(text instanceof Spannable)) {
                return false;
            }
            resetSelectionFlags();
            this.mClassifier.logEvent(logTag, LOG_EVENT_MULTI_SELECTION_RESET);
            return editor.selectCurrentWord();
        }

        private void resetSelectionFlags() {
            this.mMultiSelection = false;
            this.mClassifierSelection = false;
        }
    }

    private static final class TextClassificationAsyncTask extends AsyncTask<Void, Void, SelectionResult> {
        private final String mOriginalText;
        private final Consumer<SelectionResult> mSelectionResultCallback;
        private final Supplier<SelectionResult> mSelectionResultSupplier;
        private final TextView mTextView;
        private final int mTimeOutDuration;

        TextClassificationAsyncTask(TextView textView, int timeOut, Supplier<SelectionResult> selectionResultSupplier, Consumer<SelectionResult> selectionResultCallback) {
            Handler handler = null;
            if (textView != null) {
                handler = textView.getHandler();
            }
            super(handler);
            this.mTextView = (TextView) Preconditions.checkNotNull(textView);
            this.mTimeOutDuration = timeOut;
            this.mSelectionResultSupplier = (Supplier) Preconditions.checkNotNull(selectionResultSupplier);
            this.mSelectionResultCallback = (Consumer) Preconditions.checkNotNull(selectionResultCallback);
            this.mOriginalText = this.mTextView.getText().toString();
        }

        protected SelectionResult doInBackground(Void... params) {
            Runnable onTimeOut = new -$Lambda$tTszxdFZ0V9nXhnBpPsqeBMO0fw(this);
            this.mTextView.postDelayed(onTimeOut, (long) this.mTimeOutDuration);
            SelectionResult result = (SelectionResult) this.mSelectionResultSupplier.get();
            this.mTextView.removeCallbacks(onTimeOut);
            return result;
        }

        protected void onPostExecute(SelectionResult result) {
            Object result2;
            if (!TextUtils.equals(this.mOriginalText, this.mTextView.getText())) {
                result2 = null;
            }
            this.mSelectionResultCallback.accept(result2);
        }

        private void onTimeOut() {
            if (getStatus() == Status.RUNNING) {
                onPostExecute(null);
            }
            cancel(true);
        }
    }

    private static final class TextClassificationHelper {
        private static final int TRIM_DELTA = 120;
        private LocaleList mLastClassificationLocales;
        private SelectionResult mLastClassificationResult;
        private int mLastClassificationSelectionEnd;
        private int mLastClassificationSelectionStart;
        private CharSequence mLastClassificationText;
        private LocaleList mLocales;
        private int mRelativeEnd;
        private int mRelativeStart;
        private int mSelectionEnd;
        private int mSelectionStart;
        private String mSelectionTag = LogException.NO_VALUE;
        private String mText;
        private TextClassifier mTextClassifier;
        private int mTrimStart;
        private CharSequence mTrimmedText;

        TextClassificationHelper(TextClassifier textClassifier, CharSequence text, int selectionStart, int selectionEnd, LocaleList locales) {
            reset(textClassifier, text, selectionStart, selectionEnd, true, locales);
        }

        public void reset(TextClassifier textClassifier, CharSequence text, int selectionStart, int selectionEnd, boolean resetSelectionTag, LocaleList locales) {
            this.mTextClassifier = (TextClassifier) Preconditions.checkNotNull(textClassifier);
            this.mText = ((CharSequence) Preconditions.checkNotNull(text)).toString();
            this.mLastClassificationText = null;
            Preconditions.checkArgument(selectionEnd > selectionStart);
            this.mSelectionStart = selectionStart;
            this.mSelectionEnd = selectionEnd;
            this.mLocales = locales;
            if (resetSelectionTag) {
                this.mSelectionTag = LogException.NO_VALUE;
            }
        }

        /* renamed from: classifyText */
        public SelectionResult -android_widget_SelectionActionModeHelper-mthref-3() {
            if (!(Objects.equals(this.mText, this.mLastClassificationText) && this.mSelectionStart == this.mLastClassificationSelectionStart && this.mSelectionEnd == this.mLastClassificationSelectionEnd && (Objects.equals(this.mLocales, this.mLastClassificationLocales) ^ 1) == 0)) {
                this.mLastClassificationText = this.mText;
                this.mLastClassificationSelectionStart = this.mSelectionStart;
                this.mLastClassificationSelectionEnd = this.mSelectionEnd;
                this.mLastClassificationLocales = this.mLocales;
                trimText();
                this.mLastClassificationResult = new SelectionResult(this.mSelectionStart, this.mSelectionEnd, this.mTextClassifier.classifyText(this.mTrimmedText, this.mRelativeStart, this.mRelativeEnd, this.mLocales));
            }
            return this.mLastClassificationResult;
        }

        /* renamed from: suggestSelection */
        public SelectionResult -android_widget_SelectionActionModeHelper-mthref-0() {
            trimText();
            TextSelection sel = this.mTextClassifier.suggestSelection(this.mTrimmedText, this.mRelativeStart, this.mRelativeEnd, this.mLocales);
            this.mSelectionStart = Math.max(0, sel.getSelectionStartIndex() + this.mTrimStart);
            this.mSelectionEnd = Math.min(this.mText.length(), sel.getSelectionEndIndex() + this.mTrimStart);
            this.mSelectionTag = sel.getSourceClassifier();
            return -android_widget_SelectionActionModeHelper-mthref-3();
        }

        String getSelectionTag() {
            return this.mSelectionTag;
        }

        private void trimText() {
            this.mTrimStart = Math.max(0, this.mSelectionStart - 120);
            this.mTrimmedText = this.mText.subSequence(this.mTrimStart, Math.min(this.mText.length(), this.mSelectionEnd + 120));
            this.mRelativeStart = this.mSelectionStart - this.mTrimStart;
            this.mRelativeEnd = this.mSelectionEnd - this.mTrimStart;
        }
    }

    SelectionActionModeHelper(Editor editor) {
        this.mEditor = (Editor) Preconditions.checkNotNull(editor);
        TextView textView = this.mEditor.getTextView();
        this.mTextClassificationHelper = new TextClassificationHelper(textView.getTextClassifier(), textView.getText(), 0, 1, textView.getTextLocales());
        this.mSelectionTracker = new SelectionTracker(textView.getTextClassifier());
    }

    public void startActionModeAsync(boolean adjustSelection) {
        cancelAsyncTask();
        if (skipTextClassification()) {
            startActionMode(null);
            return;
        }
        Supplier anonymousClass4;
        resetTextClassificationHelper(true);
        TextView tv = this.mEditor.getTextView();
        TextClassificationHelper textClassificationHelper;
        if (adjustSelection) {
            textClassificationHelper = this.mTextClassificationHelper;
            textClassificationHelper.getClass();
            anonymousClass4 = new AnonymousClass4(textClassificationHelper);
        } else {
            textClassificationHelper = this.mTextClassificationHelper;
            textClassificationHelper.getClass();
            anonymousClass4 = new AnonymousClass5(textClassificationHelper);
        }
        this.mTextClassificationAsyncTask = new TextClassificationAsyncTask(tv, 200, anonymousClass4, new AnonymousClass2(this)).execute(new Void[0]);
    }

    public void startActionMode() {
        startActionMode(null);
    }

    public void invalidateActionModeAsync() {
        cancelAsyncTask();
        if (skipTextClassification()) {
            invalidateActionMode(null);
            return;
        }
        resetTextClassificationHelper(false);
        TextView textView = this.mEditor.getTextView();
        TextClassificationHelper textClassificationHelper = this.mTextClassificationHelper;
        textClassificationHelper.getClass();
        this.mTextClassificationAsyncTask = new TextClassificationAsyncTask(textView, 200, new AnonymousClass3(textClassificationHelper), new AnonymousClass1(this)).execute(new Void[0]);
    }

    public void onSelectionAction() {
        this.mSelectionTracker.onSelectionAction(this.mTextClassificationHelper.getSelectionTag());
    }

    public boolean resetSelection(int textIndex) {
        if (!this.mSelectionTracker.resetSelection(textIndex, this.mEditor, this.mTextClassificationHelper.getSelectionTag())) {
            return false;
        }
        invalidateActionModeAsync();
        return true;
    }

    public TextClassification getTextClassification() {
        return this.mTextClassification;
    }

    public void onDestroyActionMode() {
        this.mSelectionTracker.onSelectionDestroyed();
        cancelAsyncTask();
    }

    private void cancelAsyncTask() {
        if (this.mTextClassificationAsyncTask != null) {
            this.mTextClassificationAsyncTask.cancel(true);
            this.mTextClassificationAsyncTask = null;
        }
        this.mTextClassification = null;
    }

    private boolean skipTextClassification() {
        boolean password;
        TextView textView = this.mEditor.getTextView();
        boolean noOpTextClassifier = textView.getTextClassifier() == TextClassifier.NO_OP;
        boolean noSelection = textView.getSelectionEnd() == textView.getSelectionStart();
        if (textView.hasPasswordTransformationMethod()) {
            password = true;
        } else {
            password = TextView.isPasswordInputType(textView.getInputType());
        }
        if (noOpTextClassifier || noSelection) {
            return true;
        }
        return password;
    }

    private boolean hasSelection() {
        TextView textView = this.mEditor.getTextView();
        return textView.getSelectionEnd() > textView.getSelectionStart();
    }

    private void startActionMode(SelectionResult result) {
        TextView textView = this.mEditor.getTextView();
        CharSequence text = textView.getText();
        this.mSelectionTracker.setOriginalSelection(textView.getSelectionStart(), textView.getSelectionEnd());
        if (result == null || !(text instanceof Spannable)) {
            this.mTextClassification = null;
        } else {
            Selection.setSelection((Spannable) text, result.mStart, result.mEnd);
            this.mTextClassification = result.mClassification;
        }
        if (this.mEditor.startSelectionActionModeInternal()) {
            SelectionModifierCursorController controller = this.mEditor.getSelectionController();
            if (controller != null) {
                controller.show();
            }
            if (result != null) {
                this.mSelectionTracker.onSelectionStarted(result.mStart, result.mEnd, this.mTextClassificationHelper.getSelectionTag());
            }
        }
        this.mEditor.setRestartActionModeOnNextRefresh(false);
        this.mTextClassificationAsyncTask = null;
    }

    private void invalidateActionMode(SelectionResult result) {
        TextClassification -get0;
        if (result != null) {
            -get0 = result.mClassification;
        } else {
            -get0 = null;
        }
        this.mTextClassification = -get0;
        ActionMode actionMode = this.mEditor.getTextActionMode();
        if (actionMode != null) {
            actionMode.invalidate();
        }
        TextView textView = this.mEditor.getTextView();
        this.mSelectionTracker.onSelectionUpdated(textView.getSelectionStart(), textView.getSelectionEnd(), this.mTextClassificationHelper.getSelectionTag());
        this.mTextClassificationAsyncTask = null;
    }

    private void resetTextClassificationHelper(boolean resetSelectionTag) {
        TextView textView = this.mEditor.getTextView();
        this.mTextClassificationHelper.reset(textView.getTextClassifier(), textView.getText(), textView.getSelectionStart(), textView.getSelectionEnd(), resetSelectionTag, textView.getTextLocales());
    }
}
