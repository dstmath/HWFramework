package com.android.internal.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionInspector;
import android.view.inputmethod.InputContentInfo;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.SomeArgs;
import com.android.internal.view.IInputContext.Stub;

public abstract class IInputConnectionWrapper extends Stub {
    private static final boolean DEBUG = false;
    private static final int DO_BEGIN_BATCH_EDIT = 90;
    private static final int DO_CLEAR_META_KEY_STATES = 130;
    private static final int DO_CLOSE_CONNECTION = 150;
    private static final int DO_COMMIT_COMPLETION = 55;
    private static final int DO_COMMIT_CONTENT = 160;
    private static final int DO_COMMIT_CORRECTION = 56;
    private static final int DO_COMMIT_TEXT = 50;
    private static final int DO_DELETE_SURROUNDING_TEXT = 80;
    private static final int DO_DELETE_SURROUNDING_TEXT_IN_CODE_POINTS = 81;
    private static final int DO_END_BATCH_EDIT = 95;
    private static final int DO_FINISH_COMPOSING_TEXT = 65;
    private static final int DO_GET_CURSOR_CAPS_MODE = 30;
    private static final int DO_GET_EXTRACTED_TEXT = 40;
    private static final int DO_GET_SELECTED_TEXT = 25;
    private static final int DO_GET_TEXT_AFTER_CURSOR = 10;
    private static final int DO_GET_TEXT_BEFORE_CURSOR = 20;
    private static final int DO_PERFORM_CONTEXT_MENU_ACTION = 59;
    private static final int DO_PERFORM_EDITOR_ACTION = 58;
    private static final int DO_PERFORM_PRIVATE_COMMAND = 120;
    private static final int DO_REQUEST_UPDATE_CURSOR_ANCHOR_INFO = 140;
    private static final int DO_SEND_KEY_EVENT = 70;
    private static final int DO_SET_COMPOSING_REGION = 63;
    private static final int DO_SET_COMPOSING_TEXT = 60;
    private static final int DO_SET_SELECTION = 57;
    private static final String TAG = "IInputConnectionWrapper";
    @GuardedBy("mLock")
    private boolean mFinished = false;
    private Handler mH;
    @GuardedBy("mLock")
    private InputConnection mInputConnection;
    private Object mLock = new Object();
    private Looper mMainLooper;

    class MyHandler extends Handler {
        MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            IInputConnectionWrapper.this.executeMessage(msg);
        }
    }

    protected abstract boolean isActive();

    protected abstract void onUserAction();

    public IInputConnectionWrapper(Looper mainLooper, InputConnection inputConnection) {
        this.mInputConnection = inputConnection;
        this.mMainLooper = mainLooper;
        this.mH = new MyHandler(this.mMainLooper);
    }

    public InputConnection getInputConnection() {
        InputConnection inputConnection;
        synchronized (this.mLock) {
            inputConnection = this.mInputConnection;
        }
        return inputConnection;
    }

    protected boolean isFinished() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mFinished;
        }
        return z;
    }

    public void getTextAfterCursor(int length, int flags, int seq, IInputContextCallback callback) {
        dispatchMessage(obtainMessageIISC(10, length, flags, seq, callback));
    }

    public void getTextBeforeCursor(int length, int flags, int seq, IInputContextCallback callback) {
        dispatchMessage(obtainMessageIISC(20, length, flags, seq, callback));
    }

    public void getSelectedText(int flags, int seq, IInputContextCallback callback) {
        dispatchMessage(obtainMessageISC(25, flags, seq, callback));
    }

    public void getCursorCapsMode(int reqModes, int seq, IInputContextCallback callback) {
        dispatchMessage(obtainMessageISC(30, reqModes, seq, callback));
    }

    public void getExtractedText(ExtractedTextRequest request, int flags, int seq, IInputContextCallback callback) {
        dispatchMessage(obtainMessageIOSC(40, flags, request, seq, callback));
    }

    public void commitText(CharSequence text, int newCursorPosition) {
        dispatchMessage(obtainMessageIO(50, newCursorPosition, text));
    }

    public void commitCompletion(CompletionInfo text) {
        dispatchMessage(obtainMessageO(55, text));
    }

    public void commitCorrection(CorrectionInfo info) {
        dispatchMessage(obtainMessageO(56, info));
    }

    public void setSelection(int start, int end) {
        dispatchMessage(obtainMessageII(57, start, end));
    }

    public void performEditorAction(int id) {
        dispatchMessage(obtainMessageII(58, id, 0));
    }

    public void performContextMenuAction(int id) {
        dispatchMessage(obtainMessageII(59, id, 0));
    }

    public void setComposingRegion(int start, int end) {
        dispatchMessage(obtainMessageII(63, start, end));
    }

    public void setComposingText(CharSequence text, int newCursorPosition) {
        dispatchMessage(obtainMessageIO(60, newCursorPosition, text));
    }

    public void finishComposingText() {
        dispatchMessage(obtainMessage(65));
    }

    public void sendKeyEvent(KeyEvent event) {
        dispatchMessage(obtainMessageO(70, event));
    }

    public void clearMetaKeyStates(int states) {
        dispatchMessage(obtainMessageII(130, states, 0));
    }

    public void deleteSurroundingText(int beforeLength, int afterLength) {
        dispatchMessage(obtainMessageII(80, beforeLength, afterLength));
    }

    public void deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
        dispatchMessage(obtainMessageII(81, beforeLength, afterLength));
    }

    public void beginBatchEdit() {
        dispatchMessage(obtainMessage(90));
    }

    public void endBatchEdit() {
        dispatchMessage(obtainMessage(95));
    }

    public void performPrivateCommand(String action, Bundle data) {
        dispatchMessage(obtainMessageOO(120, action, data));
    }

    public void requestUpdateCursorAnchorInfo(int cursorUpdateMode, int seq, IInputContextCallback callback) {
        dispatchMessage(obtainMessageISC(140, cursorUpdateMode, seq, callback));
    }

    public void closeConnection() {
        dispatchMessage(obtainMessage(150));
    }

    public void commitContent(InputContentInfo inputContentInfo, int flags, Bundle opts, int seq, IInputContextCallback callback) {
        dispatchMessage(obtainMessageIOOSC(160, flags, inputContentInfo, opts, seq, callback));
    }

    void dispatchMessage(Message msg) {
        if (Looper.myLooper() == this.mMainLooper) {
            executeMessage(msg);
            msg.recycle();
            return;
        }
        this.mH.sendMessage(msg);
    }

    void executeMessage(Message msg) {
        SomeArgs args;
        IInputContextCallback callback;
        int callbackSeq;
        InputConnection ic;
        switch (msg.what) {
            case 10:
                args = msg.obj;
                try {
                    callback = args.arg6;
                    callbackSeq = args.argi6;
                    ic = getInputConnection();
                    if (ic == null || (isActive() ^ 1) != 0) {
                        Log.w(TAG, "getTextAfterCursor on inactive InputConnection");
                        callback.setTextAfterCursor(null, callbackSeq);
                        return;
                    }
                    callback.setTextAfterCursor(ic.getTextAfterCursor(msg.arg1, msg.arg2), callbackSeq);
                    args.recycle();
                    return;
                } catch (RemoteException e) {
                    Log.w(TAG, "Got RemoteException calling setTextAfterCursor", e);
                } finally {
                    args.recycle();
                }
                break;
            case 20:
                args = (SomeArgs) msg.obj;
                try {
                    callback = (IInputContextCallback) args.arg6;
                    callbackSeq = args.argi6;
                    ic = getInputConnection();
                    if (ic == null || (isActive() ^ 1) != 0) {
                        Log.w(TAG, "getTextBeforeCursor on inactive InputConnection");
                        callback.setTextBeforeCursor(null, callbackSeq);
                        return;
                    }
                    callback.setTextBeforeCursor(ic.getTextBeforeCursor(msg.arg1, msg.arg2), callbackSeq);
                    args.recycle();
                    return;
                } catch (RemoteException e2) {
                    Log.w(TAG, "Got RemoteException calling setTextBeforeCursor", e2);
                } finally {
                    args.recycle();
                }
                break;
            case 25:
                args = (SomeArgs) msg.obj;
                try {
                    callback = (IInputContextCallback) args.arg6;
                    callbackSeq = args.argi6;
                    ic = getInputConnection();
                    if (ic == null || (isActive() ^ 1) != 0) {
                        Log.w(TAG, "getSelectedText on inactive InputConnection");
                        callback.setSelectedText(null, callbackSeq);
                        return;
                    }
                    callback.setSelectedText(ic.getSelectedText(msg.arg1), callbackSeq);
                    args.recycle();
                    return;
                } catch (RemoteException e22) {
                    Log.w(TAG, "Got RemoteException calling setSelectedText", e22);
                } finally {
                    args.recycle();
                }
                break;
            case 30:
                args = (SomeArgs) msg.obj;
                try {
                    callback = (IInputContextCallback) args.arg6;
                    callbackSeq = args.argi6;
                    ic = getInputConnection();
                    if (ic == null || (isActive() ^ 1) != 0) {
                        Log.w(TAG, "getCursorCapsMode on inactive InputConnection");
                        callback.setCursorCapsMode(0, callbackSeq);
                        return;
                    }
                    callback.setCursorCapsMode(ic.getCursorCapsMode(msg.arg1), callbackSeq);
                    args.recycle();
                    return;
                } catch (RemoteException e222) {
                    Log.w(TAG, "Got RemoteException calling setCursorCapsMode", e222);
                } finally {
                    args.recycle();
                }
                break;
            case 40:
                args = (SomeArgs) msg.obj;
                try {
                    callback = (IInputContextCallback) args.arg6;
                    callbackSeq = args.argi6;
                    ic = getInputConnection();
                    if (ic == null || (isActive() ^ 1) != 0) {
                        Log.w(TAG, "getExtractedText on inactive InputConnection");
                        callback.setExtractedText(null, callbackSeq);
                        return;
                    }
                    callback.setExtractedText(ic.getExtractedText((ExtractedTextRequest) args.arg1, msg.arg1), callbackSeq);
                    args.recycle();
                    return;
                } catch (RemoteException e2222) {
                    Log.w(TAG, "Got RemoteException calling setExtractedText", e2222);
                } finally {
                    args.recycle();
                }
                break;
            case 50:
                ic = getInputConnection();
                if (ic == null || (isActive() ^ 1) != 0) {
                    Log.w(TAG, "ic" + ic + ", isActive()" + isActive());
                    Log.w(TAG, "commitText on inactive InputConnection");
                    return;
                }
                ic.commitText((CharSequence) msg.obj, msg.arg1);
                onUserAction();
                return;
            case 55:
                ic = getInputConnection();
                if (ic == null || (isActive() ^ 1) != 0) {
                    Log.w(TAG, "commitCompletion on inactive InputConnection");
                    return;
                } else {
                    ic.commitCompletion((CompletionInfo) msg.obj);
                    return;
                }
            case 56:
                ic = getInputConnection();
                if (ic == null || (isActive() ^ 1) != 0) {
                    Log.w(TAG, "commitCorrection on inactive InputConnection");
                    return;
                } else {
                    ic.commitCorrection((CorrectionInfo) msg.obj);
                    return;
                }
            case 57:
                ic = getInputConnection();
                if (ic == null || (isActive() ^ 1) != 0) {
                    Log.w(TAG, "setSelection on inactive InputConnection");
                    return;
                } else {
                    ic.setSelection(msg.arg1, msg.arg2);
                    return;
                }
            case 58:
                ic = getInputConnection();
                if (ic == null || (isActive() ^ 1) != 0) {
                    Log.w(TAG, "performEditorAction on inactive InputConnection");
                    return;
                } else {
                    ic.performEditorAction(msg.arg1);
                    return;
                }
            case 59:
                ic = getInputConnection();
                if (ic == null || (isActive() ^ 1) != 0) {
                    Log.w(TAG, "performContextMenuAction on inactive InputConnection");
                    return;
                } else {
                    ic.performContextMenuAction(msg.arg1);
                    return;
                }
            case 60:
                ic = getInputConnection();
                if (ic == null || (isActive() ^ 1) != 0) {
                    Log.w(TAG, "setComposingText on inactive InputConnection");
                    return;
                }
                ic.setComposingText((CharSequence) msg.obj, msg.arg1);
                onUserAction();
                return;
            case 63:
                ic = getInputConnection();
                if (ic == null || (isActive() ^ 1) != 0) {
                    Log.w(TAG, "setComposingRegion on inactive InputConnection");
                    return;
                } else {
                    ic.setComposingRegion(msg.arg1, msg.arg2);
                    return;
                }
            case 65:
                if (!isFinished()) {
                    ic = getInputConnection();
                    if (ic == null) {
                        Log.w(TAG, "finishComposingText on inactive InputConnection");
                        return;
                    } else {
                        ic.finishComposingText();
                        return;
                    }
                }
                return;
            case 70:
                ic = getInputConnection();
                if (ic == null || (isActive() ^ 1) != 0) {
                    Log.w(TAG, "sendKeyEvent on inactive InputConnection");
                    return;
                }
                ic.sendKeyEvent((KeyEvent) msg.obj);
                onUserAction();
                return;
            case 80:
                ic = getInputConnection();
                if (ic == null || (isActive() ^ 1) != 0) {
                    Log.w(TAG, "deleteSurroundingText on inactive InputConnection");
                    return;
                } else {
                    ic.deleteSurroundingText(msg.arg1, msg.arg2);
                    return;
                }
            case 81:
                ic = getInputConnection();
                if (ic == null || (isActive() ^ 1) != 0) {
                    Log.w(TAG, "deleteSurroundingTextInCodePoints on inactive InputConnection");
                    return;
                } else {
                    ic.deleteSurroundingTextInCodePoints(msg.arg1, msg.arg2);
                    return;
                }
            case 90:
                ic = getInputConnection();
                if (ic == null || (isActive() ^ 1) != 0) {
                    Log.w(TAG, "beginBatchEdit on inactive InputConnection");
                    return;
                } else {
                    ic.beginBatchEdit();
                    return;
                }
            case 95:
                ic = getInputConnection();
                if (ic == null || (isActive() ^ 1) != 0) {
                    Log.w(TAG, "endBatchEdit on inactive InputConnection");
                    return;
                } else {
                    ic.endBatchEdit();
                    return;
                }
            case 120:
                args = (SomeArgs) msg.obj;
                try {
                    String action = args.arg1;
                    Bundle data = args.arg2;
                    ic = getInputConnection();
                    if (ic == null || (isActive() ^ 1) != 0) {
                        Log.w(TAG, "performPrivateCommand on inactive InputConnection");
                        return;
                    }
                    ic.performPrivateCommand(action, data);
                    args.recycle();
                    return;
                } finally {
                    args.recycle();
                }
                break;
            case 130:
                ic = getInputConnection();
                if (ic == null || (isActive() ^ 1) != 0) {
                    Log.w(TAG, "clearMetaKeyStates on inactive InputConnection");
                    return;
                } else {
                    ic.clearMetaKeyStates(msg.arg1);
                    return;
                }
            case 140:
                args = (SomeArgs) msg.obj;
                try {
                    callback = (IInputContextCallback) args.arg6;
                    callbackSeq = args.argi6;
                    ic = getInputConnection();
                    if (ic == null || (isActive() ^ 1) != 0) {
                        Log.w(TAG, "requestCursorAnchorInfo on inactive InputConnection");
                        callback.setRequestUpdateCursorAnchorInfoResult(false, callbackSeq);
                        return;
                    }
                    callback.setRequestUpdateCursorAnchorInfoResult(ic.requestCursorUpdates(msg.arg1), callbackSeq);
                    args.recycle();
                    return;
                } catch (RemoteException e22222) {
                    Log.w(TAG, "Got RemoteException calling requestCursorAnchorInfo", e22222);
                } finally {
                    args.recycle();
                }
                break;
            case 150:
                if (!isFinished()) {
                    try {
                        ic = getInputConnection();
                        if (ic == null) {
                            synchronized (this.mLock) {
                                this.mInputConnection = null;
                                this.mFinished = true;
                            }
                            return;
                        }
                        if ((InputConnectionInspector.getMissingMethodFlags(ic) & 64) == 0) {
                            ic.closeConnection();
                        }
                        synchronized (this.mLock) {
                            this.mInputConnection = null;
                            this.mFinished = true;
                        }
                        return;
                    } catch (Throwable th) {
                        synchronized (this.mLock) {
                            this.mInputConnection = null;
                            this.mFinished = true;
                        }
                    }
                } else {
                    return;
                }
            case 160:
                int flags = msg.arg1;
                args = (SomeArgs) msg.obj;
                try {
                    callback = (IInputContextCallback) args.arg6;
                    callbackSeq = args.argi6;
                    ic = getInputConnection();
                    if (ic == null || (isActive() ^ 1) != 0) {
                        Log.w(TAG, "commitContent on inactive InputConnection");
                        callback.setCommitContentResult(false, callbackSeq);
                        return;
                    }
                    InputContentInfo inputContentInfo = args.arg1;
                    if (inputContentInfo == null || (inputContentInfo.validate() ^ 1) != 0) {
                        Log.w(TAG, "commitContent with invalid inputContentInfo=" + inputContentInfo);
                        callback.setCommitContentResult(false, callbackSeq);
                        args.recycle();
                        return;
                    }
                    callback.setCommitContentResult(ic.commitContent(inputContentInfo, flags, (Bundle) args.arg2), callbackSeq);
                    args.recycle();
                    return;
                } catch (RemoteException e222222) {
                    Log.w(TAG, "Got RemoteException calling commitContent", e222222);
                } finally {
                    args.recycle();
                }
                break;
            default:
                Log.w(TAG, "Unhandled message code: " + msg.what);
                return;
        }
    }

    Message obtainMessage(int what) {
        return this.mH.obtainMessage(what);
    }

    Message obtainMessageII(int what, int arg1, int arg2) {
        return this.mH.obtainMessage(what, arg1, arg2);
    }

    Message obtainMessageO(int what, Object arg1) {
        return this.mH.obtainMessage(what, 0, 0, arg1);
    }

    Message obtainMessageISC(int what, int arg1, int callbackSeq, IInputContextCallback callback) {
        SomeArgs args = SomeArgs.obtain();
        args.arg6 = callback;
        args.argi6 = callbackSeq;
        return this.mH.obtainMessage(what, arg1, 0, args);
    }

    Message obtainMessageIISC(int what, int arg1, int arg2, int callbackSeq, IInputContextCallback callback) {
        SomeArgs args = SomeArgs.obtain();
        args.arg6 = callback;
        args.argi6 = callbackSeq;
        return this.mH.obtainMessage(what, arg1, arg2, args);
    }

    Message obtainMessageIOOSC(int what, int arg1, Object objArg1, Object objArg2, int callbackSeq, IInputContextCallback callback) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = objArg1;
        args.arg2 = objArg2;
        args.arg6 = callback;
        args.argi6 = callbackSeq;
        return this.mH.obtainMessage(what, arg1, 0, args);
    }

    Message obtainMessageIOSC(int what, int arg1, Object arg2, int callbackSeq, IInputContextCallback callback) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = arg2;
        args.arg6 = callback;
        args.argi6 = callbackSeq;
        return this.mH.obtainMessage(what, arg1, 0, args);
    }

    Message obtainMessageIO(int what, int arg1, Object arg2) {
        return this.mH.obtainMessage(what, arg1, 0, arg2);
    }

    Message obtainMessageOO(int what, Object arg1, Object arg2) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = arg1;
        args.arg2 = arg2;
        return this.mH.obtainMessage(what, 0, 0, args);
    }
}
