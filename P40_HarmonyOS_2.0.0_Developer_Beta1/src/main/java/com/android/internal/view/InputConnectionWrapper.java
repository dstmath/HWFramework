package com.android.internal.view;

import android.annotation.UnsupportedAppUsage;
import android.inputmethodservice.AbstractInputMethodService;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionInspector;
import android.view.inputmethod.InputContentInfo;
import com.android.internal.view.IInputContextCallback;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

public class InputConnectionWrapper implements InputConnection {
    private static final int MAX_WAIT_TIME_MILLIS = 2000;
    private final IInputContext mIInputContext;
    private final WeakReference<AbstractInputMethodService> mInputMethodService;
    private final AtomicBoolean mIsUnbindIssued;
    private final int mMissingMethods;
    private boolean mUseInputMethodService;

    static class InputContextCallback extends IInputContextCallback.Stub {
        private static final String TAG = "InputConnectionWrapper.ICC";
        private static InputContextCallback sInstance = new InputContextCallback();
        private static int sSequenceNumber = 1;
        public boolean mCommitContentResult;
        public int mCursorCapsMode;
        public ExtractedText mExtractedText;
        public boolean mHaveValue;
        public boolean mRequestUpdateCursorAnchorInfoResult;
        public CharSequence mSelectedText;
        public int mSeq;
        public CharSequence mTextAfterCursor;
        public CharSequence mTextBeforeCursor;

        InputContextCallback() {
        }

        /* access modifiers changed from: private */
        @UnsupportedAppUsage
        public static InputContextCallback getInstance() {
            InputContextCallback callback;
            synchronized (InputContextCallback.class) {
                if (sInstance != null) {
                    callback = sInstance;
                    sInstance = null;
                    callback.mHaveValue = false;
                } else {
                    callback = new InputContextCallback();
                }
                int i = sSequenceNumber;
                sSequenceNumber = i + 1;
                callback.mSeq = i;
            }
            return callback;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        @UnsupportedAppUsage
        private void dispose() {
            synchronized (InputContextCallback.class) {
                if (sInstance == null) {
                    this.mTextAfterCursor = null;
                    this.mTextBeforeCursor = null;
                    this.mExtractedText = null;
                    sInstance = this;
                }
            }
        }

        @Override // com.android.internal.view.IInputContextCallback
        public void setTextBeforeCursor(CharSequence textBeforeCursor, int seq) {
            synchronized (this) {
                if (seq == this.mSeq) {
                    this.mTextBeforeCursor = textBeforeCursor;
                    this.mHaveValue = true;
                    notifyAll();
                } else {
                    Log.i(TAG, "Got out-of-sequence callback " + seq + " (expected " + this.mSeq + ") in setTextBeforeCursor, ignoring.");
                }
            }
        }

        @Override // com.android.internal.view.IInputContextCallback
        public void setTextAfterCursor(CharSequence textAfterCursor, int seq) {
            synchronized (this) {
                if (seq == this.mSeq) {
                    this.mTextAfterCursor = textAfterCursor;
                    this.mHaveValue = true;
                    notifyAll();
                } else {
                    Log.i(TAG, "Got out-of-sequence callback " + seq + " (expected " + this.mSeq + ") in setTextAfterCursor, ignoring.");
                }
            }
        }

        @Override // com.android.internal.view.IInputContextCallback
        public void setSelectedText(CharSequence selectedText, int seq) {
            synchronized (this) {
                if (seq == this.mSeq) {
                    this.mSelectedText = selectedText;
                    this.mHaveValue = true;
                    notifyAll();
                } else {
                    Log.i(TAG, "Got out-of-sequence callback " + seq + " (expected " + this.mSeq + ") in setSelectedText, ignoring.");
                }
            }
        }

        @Override // com.android.internal.view.IInputContextCallback
        public void setCursorCapsMode(int capsMode, int seq) {
            synchronized (this) {
                if (seq == this.mSeq) {
                    this.mCursorCapsMode = capsMode;
                    this.mHaveValue = true;
                    notifyAll();
                } else {
                    Log.i(TAG, "Got out-of-sequence callback " + seq + " (expected " + this.mSeq + ") in setCursorCapsMode, ignoring.");
                }
            }
        }

        @Override // com.android.internal.view.IInputContextCallback
        public void setExtractedText(ExtractedText extractedText, int seq) {
            synchronized (this) {
                if (seq == this.mSeq) {
                    this.mExtractedText = extractedText;
                    this.mHaveValue = true;
                    notifyAll();
                } else {
                    Log.i(TAG, "Got out-of-sequence callback " + seq + " (expected " + this.mSeq + ") in setExtractedText, ignoring.");
                }
            }
        }

        @Override // com.android.internal.view.IInputContextCallback
        public void setRequestUpdateCursorAnchorInfoResult(boolean result, int seq) {
            synchronized (this) {
                if (seq == this.mSeq) {
                    this.mRequestUpdateCursorAnchorInfoResult = result;
                    this.mHaveValue = true;
                    notifyAll();
                } else {
                    Log.i(TAG, "Got out-of-sequence callback " + seq + " (expected " + this.mSeq + ") in setCursorAnchorInfoRequestResult, ignoring.");
                }
            }
        }

        @Override // com.android.internal.view.IInputContextCallback
        public void setCommitContentResult(boolean result, int seq) {
            synchronized (this) {
                if (seq == this.mSeq) {
                    this.mCommitContentResult = result;
                    this.mHaveValue = true;
                    notifyAll();
                } else {
                    Log.i(TAG, "Got out-of-sequence callback " + seq + " (expected " + this.mSeq + ") in setCommitContentResult, ignoring.");
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void waitForResultLocked() {
            long endTime = 2000 + SystemClock.uptimeMillis();
            while (!this.mHaveValue) {
                long remainingTime = endTime - SystemClock.uptimeMillis();
                if (remainingTime <= 0) {
                    Log.w(TAG, "Timed out waiting on IInputContextCallback");
                    return;
                }
                try {
                    wait(remainingTime);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public InputConnectionWrapper(WeakReference<AbstractInputMethodService> inputMethodService, IInputContext inputContext, int missingMethods, AtomicBoolean isUnbindIssued) {
        this.mInputMethodService = inputMethodService;
        this.mIInputContext = inputContext;
        this.mMissingMethods = missingMethods;
        this.mIsUnbindIssued = isUnbindIssued;
        this.mUseInputMethodService = true;
    }

    public InputConnectionWrapper(IInputContext inputContext, AtomicBoolean isUnbindIssued) {
        this.mInputMethodService = null;
        this.mIInputContext = inputContext;
        this.mMissingMethods = 0;
        this.mIsUnbindIssued = isUnbindIssued;
        this.mUseInputMethodService = false;
    }

    @Override // android.view.inputmethod.InputConnection
    public CharSequence getTextAfterCursor(int length, int flags) {
        if (this.mIsUnbindIssued.get()) {
            return null;
        }
        CharSequence value = null;
        try {
            InputContextCallback callback = InputContextCallback.getInstance();
            this.mIInputContext.getTextAfterCursor(length, flags, callback.mSeq, callback);
            synchronized (callback) {
                callback.waitForResultLocked();
                if (callback.mHaveValue) {
                    value = callback.mTextAfterCursor;
                }
            }
            callback.dispose();
            return value;
        } catch (RemoteException e) {
            return null;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public CharSequence getTextBeforeCursor(int length, int flags) {
        if (this.mIsUnbindIssued.get()) {
            return null;
        }
        CharSequence value = null;
        try {
            InputContextCallback callback = InputContextCallback.getInstance();
            this.mIInputContext.getTextBeforeCursor(length, flags, callback.mSeq, callback);
            synchronized (callback) {
                callback.waitForResultLocked();
                if (callback.mHaveValue) {
                    value = callback.mTextBeforeCursor;
                }
            }
            callback.dispose();
            return value;
        } catch (RemoteException e) {
            return null;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public CharSequence getSelectedText(int flags) {
        if (this.mIsUnbindIssued.get() || isMethodMissing(1)) {
            return null;
        }
        CharSequence value = null;
        try {
            InputContextCallback callback = InputContextCallback.getInstance();
            this.mIInputContext.getSelectedText(flags, callback.mSeq, callback);
            synchronized (callback) {
                callback.waitForResultLocked();
                if (callback.mHaveValue) {
                    value = callback.mSelectedText;
                }
            }
            callback.dispose();
            return value;
        } catch (RemoteException e) {
            return null;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public int getCursorCapsMode(int reqModes) {
        if (this.mIsUnbindIssued.get()) {
            return 0;
        }
        int value = 0;
        try {
            InputContextCallback callback = InputContextCallback.getInstance();
            this.mIInputContext.getCursorCapsMode(reqModes, callback.mSeq, callback);
            synchronized (callback) {
                callback.waitForResultLocked();
                if (callback.mHaveValue) {
                    value = callback.mCursorCapsMode;
                }
            }
            callback.dispose();
            return value;
        } catch (RemoteException e) {
            return 0;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
        if (this.mIsUnbindIssued.get()) {
            return null;
        }
        ExtractedText value = null;
        try {
            InputContextCallback callback = InputContextCallback.getInstance();
            this.mIInputContext.getExtractedText(request, flags, callback.mSeq, callback);
            synchronized (callback) {
                callback.waitForResultLocked();
                if (callback.mHaveValue) {
                    value = callback.mExtractedText;
                }
            }
            callback.dispose();
            return value;
        } catch (RemoteException e) {
            return null;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean commitText(CharSequence text, int newCursorPosition) {
        try {
            this.mIInputContext.commitText(text, newCursorPosition);
            notifyUserActionIfNecessary();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    private void notifyUserActionIfNecessary() {
        AbstractInputMethodService inputMethodService;
        if (this.mUseInputMethodService && (inputMethodService = this.mInputMethodService.get()) != null) {
            inputMethodService.notifyUserActionIfNecessary();
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean commitCompletion(CompletionInfo text) {
        if (isMethodMissing(4)) {
            return false;
        }
        try {
            this.mIInputContext.commitCompletion(text);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        try {
            this.mIInputContext.commitCorrection(correctionInfo);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean setSelection(int start, int end) {
        try {
            this.mIInputContext.setSelection(start, end);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean performEditorAction(int actionCode) {
        try {
            this.mIInputContext.performEditorAction(actionCode);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean performContextMenuAction(int id) {
        try {
            this.mIInputContext.performContextMenuAction(id);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean setComposingRegion(int start, int end) {
        if (isMethodMissing(2)) {
            return false;
        }
        try {
            this.mIInputContext.setComposingRegion(start, end);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        try {
            this.mIInputContext.setComposingText(text, newCursorPosition);
            notifyUserActionIfNecessary();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean finishComposingText() {
        try {
            this.mIInputContext.finishComposingText();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean beginBatchEdit() {
        try {
            this.mIInputContext.beginBatchEdit();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean endBatchEdit() {
        try {
            this.mIInputContext.endBatchEdit();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean sendKeyEvent(KeyEvent event) {
        try {
            this.mIInputContext.sendKeyEvent(event);
            notifyUserActionIfNecessary();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean clearMetaKeyStates(int states) {
        try {
            this.mIInputContext.clearMetaKeyStates(states);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        try {
            this.mIInputContext.deleteSurroundingText(beforeLength, afterLength);
            notifyUserActionIfNecessary();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
        if (isMethodMissing(16)) {
            return false;
        }
        try {
            this.mIInputContext.deleteSurroundingTextInCodePoints(beforeLength, afterLength);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean reportFullscreenMode(boolean enabled) {
        return false;
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean performPrivateCommand(String action, Bundle data) {
        try {
            this.mIInputContext.performPrivateCommand(action, data);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean requestCursorUpdates(int cursorUpdateMode) {
        if (this.mIsUnbindIssued.get()) {
            return false;
        }
        boolean result = false;
        if (isMethodMissing(8)) {
            return false;
        }
        try {
            InputContextCallback callback = InputContextCallback.getInstance();
            this.mIInputContext.requestUpdateCursorAnchorInfo(cursorUpdateMode, callback.mSeq, callback);
            synchronized (callback) {
                callback.waitForResultLocked();
                if (callback.mHaveValue) {
                    result = callback.mRequestUpdateCursorAnchorInfoResult;
                }
            }
            callback.dispose();
            return result;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.inputmethod.InputConnection
    public Handler getHandler() {
        return null;
    }

    @Override // android.view.inputmethod.InputConnection
    public void closeConnection() {
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean commitContent(InputContentInfo inputContentInfo, int flags, Bundle opts) {
        if (this.mIsUnbindIssued.get()) {
            return false;
        }
        boolean result = false;
        if (isMethodMissing(128)) {
            return false;
        }
        try {
            if (this.mUseInputMethodService && ((flags & 1) != 0)) {
                AbstractInputMethodService inputMethodService = this.mInputMethodService.get();
                if (inputMethodService == null) {
                    return false;
                }
                inputMethodService.exposeContent(inputContentInfo, this);
            }
            InputContextCallback callback = InputContextCallback.getInstance();
            this.mIInputContext.commitContent(inputContentInfo, flags, opts, callback.mSeq, callback);
            synchronized (callback) {
                callback.waitForResultLocked();
                if (callback.mHaveValue) {
                    result = callback.mCommitContentResult;
                }
            }
            callback.dispose();
            return result;
        } catch (RemoteException e) {
            return false;
        }
    }

    private boolean isMethodMissing(int methodFlag) {
        return (this.mMissingMethods & methodFlag) == methodFlag;
    }

    public String toString() {
        return "InputConnectionWrapper{idHash=#" + Integer.toHexString(System.identityHashCode(this)) + " mMissingMethods=" + InputConnectionInspector.getMissingMethodFlagsAsString(this.mMissingMethods) + "}";
    }
}
