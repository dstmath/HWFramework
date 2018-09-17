package com.android.internal.view;

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
import com.android.internal.view.IInputContextCallback.Stub;

public class InputConnectionWrapper implements InputConnection {
    private static final int MAX_WAIT_TIME_MILLIS = 2000;
    private final IInputContext mIInputContext;
    private final int mMissingMethods;

    static class InputContextCallback extends Stub {
        private static final String TAG = "InputConnectionWrapper.ICC";
        private static InputContextCallback sInstance;
        private static int sSequenceNumber;
        public int mCursorCapsMode;
        public ExtractedText mExtractedText;
        public boolean mHaveValue;
        public boolean mRequestUpdateCursorAnchorInfoResult;
        public CharSequence mSelectedText;
        public int mSeq;
        public CharSequence mTextAfterCursor;
        public CharSequence mTextBeforeCursor;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.view.InputConnectionWrapper.InputContextCallback.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.view.InputConnectionWrapper.InputContextCallback.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.view.InputConnectionWrapper.InputContextCallback.<clinit>():void");
        }

        InputContextCallback() {
        }

        private static InputContextCallback getInstance() {
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

        void waitForResultLocked() {
            long endTime = SystemClock.uptimeMillis() + 2000;
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

    public InputConnectionWrapper(IInputContext inputContext, int missingMethods) {
        this.mIInputContext = inputContext;
        this.mMissingMethods = missingMethods;
    }

    public CharSequence getTextAfterCursor(int length, int flags) {
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

    public CharSequence getTextBeforeCursor(int length, int flags) {
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

    public CharSequence getSelectedText(int flags) {
        if (isMethodMissing(1)) {
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

    public int getCursorCapsMode(int reqModes) {
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

    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
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

    public boolean commitText(CharSequence text, int newCursorPosition) {
        try {
            this.mIInputContext.commitText(text, newCursorPosition);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

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

    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        try {
            this.mIInputContext.commitCorrection(correctionInfo);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean setSelection(int start, int end) {
        try {
            this.mIInputContext.setSelection(start, end);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean performEditorAction(int actionCode) {
        try {
            this.mIInputContext.performEditorAction(actionCode);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean performContextMenuAction(int id) {
        try {
            this.mIInputContext.performContextMenuAction(id);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

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

    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        try {
            this.mIInputContext.setComposingText(text, newCursorPosition);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean finishComposingText() {
        try {
            this.mIInputContext.finishComposingText();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean beginBatchEdit() {
        try {
            this.mIInputContext.beginBatchEdit();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean endBatchEdit() {
        try {
            this.mIInputContext.endBatchEdit();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean sendKeyEvent(KeyEvent event) {
        try {
            this.mIInputContext.sendKeyEvent(event);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean clearMetaKeyStates(int states) {
        try {
            this.mIInputContext.clearMetaKeyStates(states);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        try {
            this.mIInputContext.deleteSurroundingText(beforeLength, afterLength);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

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

    public boolean reportFullscreenMode(boolean enabled) {
        try {
            this.mIInputContext.reportFullscreenMode(enabled);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean performPrivateCommand(String action, Bundle data) {
        try {
            this.mIInputContext.performPrivateCommand(action, data);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean requestCursorUpdates(int cursorUpdateMode) {
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

    public Handler getHandler() {
        return null;
    }

    public void closeConnection() {
    }

    private boolean isMethodMissing(int methodFlag) {
        return (this.mMissingMethods & methodFlag) == methodFlag;
    }

    public String toString() {
        return "InputConnectionWrapper{idHash=#" + Integer.toHexString(System.identityHashCode(this)) + " mMissingMethods=" + InputConnectionInspector.getMissingMethodFlagsAsString(this.mMissingMethods) + "}";
    }
}
