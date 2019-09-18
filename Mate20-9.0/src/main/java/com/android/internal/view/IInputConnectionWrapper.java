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
import com.android.internal.view.IInputContext;

public abstract class IInputConnectionWrapper extends IInputContext.Stub {
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

    /* access modifiers changed from: protected */
    public abstract boolean isActive();

    /* access modifiers changed from: protected */
    public abstract void onUserAction();

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

    /* access modifiers changed from: protected */
    public boolean isFinished() {
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

    /* access modifiers changed from: package-private */
    public void dispatchMessage(Message msg) {
        if (Looper.myLooper() == this.mMainLooper) {
            executeMessage(msg);
            msg.recycle();
            return;
        }
        this.mH.sendMessage(msg);
    }

    /* access modifiers changed from: package-private */
    public void executeMessage(Message msg) {
        int i = msg.what;
        switch (i) {
            case 55:
                InputConnection ic = getInputConnection();
                if (ic == null || !isActive()) {
                    Log.w(TAG, "commitCompletion on inactive InputConnection");
                    return;
                } else {
                    ic.commitCompletion((CompletionInfo) msg.obj);
                    return;
                }
            case 56:
                InputConnection ic2 = getInputConnection();
                if (ic2 == null || !isActive()) {
                    Log.w(TAG, "commitCorrection on inactive InputConnection");
                    return;
                } else {
                    ic2.commitCorrection((CorrectionInfo) msg.obj);
                    return;
                }
            case 57:
                InputConnection ic3 = getInputConnection();
                if (ic3 == null || !isActive()) {
                    Log.w(TAG, "setSelection on inactive InputConnection");
                    return;
                } else {
                    ic3.setSelection(msg.arg1, msg.arg2);
                    return;
                }
            case 58:
                InputConnection ic4 = getInputConnection();
                if (ic4 == null || !isActive()) {
                    Log.w(TAG, "performEditorAction on inactive InputConnection");
                    return;
                } else {
                    ic4.performEditorAction(msg.arg1);
                    return;
                }
            case 59:
                InputConnection ic5 = getInputConnection();
                if (ic5 == null || !isActive()) {
                    Log.w(TAG, "performContextMenuAction on inactive InputConnection");
                    return;
                } else {
                    ic5.performContextMenuAction(msg.arg1);
                    return;
                }
            case 60:
                InputConnection ic6 = getInputConnection();
                if (ic6 == null || !isActive()) {
                    Log.w(TAG, "setComposingText on inactive InputConnection");
                    return;
                }
                ic6.setComposingText((CharSequence) msg.obj, msg.arg1);
                onUserAction();
                return;
            default:
                switch (i) {
                    case 80:
                        InputConnection ic7 = getInputConnection();
                        if (ic7 == null || !isActive()) {
                            Log.w(TAG, "deleteSurroundingText on inactive InputConnection");
                            return;
                        } else {
                            ic7.deleteSurroundingText(msg.arg1, msg.arg2);
                            return;
                        }
                    case 81:
                        InputConnection ic8 = getInputConnection();
                        if (ic8 == null || !isActive()) {
                            Log.w(TAG, "deleteSurroundingTextInCodePoints on inactive InputConnection");
                            return;
                        } else {
                            ic8.deleteSurroundingTextInCodePoints(msg.arg1, msg.arg2);
                            return;
                        }
                    default:
                        switch (i) {
                            case 10:
                                SomeArgs args = (SomeArgs) msg.obj;
                                try {
                                    IInputContextCallback callback = (IInputContextCallback) args.arg6;
                                    int callbackSeq = args.argi6;
                                    InputConnection ic9 = getInputConnection();
                                    if (ic9 != null) {
                                        if (isActive()) {
                                            callback.setTextAfterCursor(ic9.getTextAfterCursor(msg.arg1, msg.arg2), callbackSeq);
                                            args.recycle();
                                            return;
                                        }
                                    }
                                    Log.w(TAG, "getTextAfterCursor on inactive InputConnection");
                                    callback.setTextAfterCursor(null, callbackSeq);
                                    args.recycle();
                                    return;
                                } catch (RemoteException e) {
                                    Log.w(TAG, "Got RemoteException calling setTextAfterCursor", e);
                                } catch (Throwable th) {
                                    args.recycle();
                                    throw th;
                                }
                            case 20:
                                SomeArgs args2 = (SomeArgs) msg.obj;
                                try {
                                    IInputContextCallback callback2 = (IInputContextCallback) args2.arg6;
                                    int callbackSeq2 = args2.argi6;
                                    InputConnection ic10 = getInputConnection();
                                    if (ic10 != null) {
                                        if (isActive()) {
                                            callback2.setTextBeforeCursor(ic10.getTextBeforeCursor(msg.arg1, msg.arg2), callbackSeq2);
                                            args2.recycle();
                                            return;
                                        }
                                    }
                                    Log.w(TAG, "getTextBeforeCursor on inactive InputConnection");
                                    callback2.setTextBeforeCursor(null, callbackSeq2);
                                    args2.recycle();
                                    return;
                                } catch (RemoteException e2) {
                                    Log.w(TAG, "Got RemoteException calling setTextBeforeCursor", e2);
                                } catch (Throwable th2) {
                                    args2.recycle();
                                    throw th2;
                                }
                            case 25:
                                SomeArgs args3 = (SomeArgs) msg.obj;
                                try {
                                    IInputContextCallback callback3 = (IInputContextCallback) args3.arg6;
                                    int callbackSeq3 = args3.argi6;
                                    InputConnection ic11 = getInputConnection();
                                    if (ic11 != null) {
                                        if (isActive()) {
                                            callback3.setSelectedText(ic11.getSelectedText(msg.arg1), callbackSeq3);
                                            args3.recycle();
                                            return;
                                        }
                                    }
                                    Log.w(TAG, "getSelectedText on inactive InputConnection");
                                    callback3.setSelectedText(null, callbackSeq3);
                                    args3.recycle();
                                    return;
                                } catch (RemoteException e3) {
                                    Log.w(TAG, "Got RemoteException calling setSelectedText", e3);
                                } catch (Throwable th3) {
                                    args3.recycle();
                                    throw th3;
                                }
                            case 30:
                                SomeArgs args4 = (SomeArgs) msg.obj;
                                try {
                                    IInputContextCallback callback4 = (IInputContextCallback) args4.arg6;
                                    int callbackSeq4 = args4.argi6;
                                    InputConnection ic12 = getInputConnection();
                                    if (ic12 != null) {
                                        if (isActive()) {
                                            callback4.setCursorCapsMode(ic12.getCursorCapsMode(msg.arg1), callbackSeq4);
                                            args4.recycle();
                                            return;
                                        }
                                    }
                                    Log.w(TAG, "getCursorCapsMode on inactive InputConnection");
                                    callback4.setCursorCapsMode(0, callbackSeq4);
                                    args4.recycle();
                                    return;
                                } catch (RemoteException e4) {
                                    Log.w(TAG, "Got RemoteException calling setCursorCapsMode", e4);
                                } catch (Throwable th4) {
                                    args4.recycle();
                                    throw th4;
                                }
                            case 40:
                                SomeArgs args5 = (SomeArgs) msg.obj;
                                try {
                                    IInputContextCallback callback5 = (IInputContextCallback) args5.arg6;
                                    int callbackSeq5 = args5.argi6;
                                    InputConnection ic13 = getInputConnection();
                                    if (ic13 != null) {
                                        if (isActive()) {
                                            callback5.setExtractedText(ic13.getExtractedText((ExtractedTextRequest) args5.arg1, msg.arg1), callbackSeq5);
                                            args5.recycle();
                                            return;
                                        }
                                    }
                                    Log.w(TAG, "getExtractedText on inactive InputConnection");
                                    callback5.setExtractedText(null, callbackSeq5);
                                    args5.recycle();
                                    return;
                                } catch (RemoteException e5) {
                                    Log.w(TAG, "Got RemoteException calling setExtractedText", e5);
                                } catch (Throwable th5) {
                                    args5.recycle();
                                    throw th5;
                                }
                            case 50:
                                InputConnection ic14 = getInputConnection();
                                if (ic14 == null || !isActive()) {
                                    Log.w(TAG, "ic" + ic14 + ", isActive()" + isActive());
                                    Log.w(TAG, "commitText on inactive InputConnection");
                                    return;
                                }
                                ic14.commitText((CharSequence) msg.obj, msg.arg1);
                                onUserAction();
                                return;
                            case 63:
                                InputConnection ic15 = getInputConnection();
                                if (ic15 == null || !isActive()) {
                                    Log.w(TAG, "setComposingRegion on inactive InputConnection");
                                    return;
                                } else {
                                    ic15.setComposingRegion(msg.arg1, msg.arg2);
                                    return;
                                }
                            case 65:
                                if (!isFinished()) {
                                    InputConnection ic16 = getInputConnection();
                                    if (ic16 == null) {
                                        Log.w(TAG, "finishComposingText on inactive InputConnection");
                                        return;
                                    } else {
                                        ic16.finishComposingText();
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            case 70:
                                InputConnection ic17 = getInputConnection();
                                if (ic17 == null || !isActive()) {
                                    Log.w(TAG, "sendKeyEvent on inactive InputConnection");
                                    return;
                                }
                                ic17.sendKeyEvent((KeyEvent) msg.obj);
                                onUserAction();
                                return;
                            case 90:
                                InputConnection ic18 = getInputConnection();
                                if (ic18 == null || !isActive()) {
                                    Log.w(TAG, "beginBatchEdit on inactive InputConnection");
                                    return;
                                } else {
                                    ic18.beginBatchEdit();
                                    return;
                                }
                            case 95:
                                InputConnection ic19 = getInputConnection();
                                if (ic19 == null || !isActive()) {
                                    Log.w(TAG, "endBatchEdit on inactive InputConnection");
                                    return;
                                } else {
                                    ic19.endBatchEdit();
                                    return;
                                }
                            case 120:
                                SomeArgs args6 = (SomeArgs) msg.obj;
                                try {
                                    String action = (String) args6.arg1;
                                    Bundle data = (Bundle) args6.arg2;
                                    InputConnection ic20 = getInputConnection();
                                    if (ic20 != null) {
                                        if (isActive()) {
                                            ic20.performPrivateCommand(action, data);
                                            args6.recycle();
                                            return;
                                        }
                                    }
                                    Log.w(TAG, "performPrivateCommand on inactive InputConnection");
                                    return;
                                } finally {
                                    args6.recycle();
                                }
                            case 130:
                                InputConnection ic21 = getInputConnection();
                                if (ic21 == null || !isActive()) {
                                    Log.w(TAG, "clearMetaKeyStates on inactive InputConnection");
                                    return;
                                } else {
                                    ic21.clearMetaKeyStates(msg.arg1);
                                    return;
                                }
                            case 140:
                                SomeArgs args7 = (SomeArgs) msg.obj;
                                try {
                                    IInputContextCallback callback6 = (IInputContextCallback) args7.arg6;
                                    int callbackSeq6 = args7.argi6;
                                    InputConnection ic22 = getInputConnection();
                                    if (ic22 != null) {
                                        if (isActive()) {
                                            callback6.setRequestUpdateCursorAnchorInfoResult(ic22.requestCursorUpdates(msg.arg1), callbackSeq6);
                                            args7.recycle();
                                            return;
                                        }
                                    }
                                    Log.w(TAG, "requestCursorAnchorInfo on inactive InputConnection");
                                    callback6.setRequestUpdateCursorAnchorInfoResult(false, callbackSeq6);
                                    args7.recycle();
                                    return;
                                } catch (RemoteException e6) {
                                    Log.w(TAG, "Got RemoteException calling requestCursorAnchorInfo", e6);
                                } catch (Throwable th6) {
                                    args7.recycle();
                                    throw th6;
                                }
                            case 150:
                                if (isFinished() == 0) {
                                    try {
                                        InputConnection ic23 = getInputConnection();
                                        if (ic23 == null) {
                                            synchronized (this.mLock) {
                                                this.mInputConnection = null;
                                                this.mFinished = true;
                                            }
                                            return;
                                        }
                                        if ((InputConnectionInspector.getMissingMethodFlags(ic23) & 64) == 0) {
                                            ic23.closeConnection();
                                        }
                                        synchronized (this.mLock) {
                                            this.mInputConnection = null;
                                            this.mFinished = true;
                                        }
                                        return;
                                    } catch (Throwable th7) {
                                        synchronized (this.mLock) {
                                            this.mInputConnection = null;
                                            this.mFinished = true;
                                            throw th7;
                                        }
                                    }
                                } else {
                                    return;
                                }
                            case 160:
                                int flags = msg.arg1;
                                SomeArgs args8 = (SomeArgs) msg.obj;
                                try {
                                    IInputContextCallback callback7 = (IInputContextCallback) args8.arg6;
                                    int callbackSeq7 = args8.argi6;
                                    InputConnection ic24 = getInputConnection();
                                    if (ic24 != null) {
                                        if (isActive()) {
                                            InputContentInfo inputContentInfo = (InputContentInfo) args8.arg1;
                                            if (inputContentInfo != null) {
                                                if (inputContentInfo.validate()) {
                                                    callback7.setCommitContentResult(ic24.commitContent(inputContentInfo, flags, (Bundle) args8.arg2), callbackSeq7);
                                                    args8.recycle();
                                                    return;
                                                }
                                            }
                                            Log.w(TAG, "commitContent with invalid inputContentInfo=" + inputContentInfo);
                                            callback7.setCommitContentResult(false, callbackSeq7);
                                            args8.recycle();
                                            return;
                                        }
                                    }
                                    Log.w(TAG, "commitContent on inactive InputConnection");
                                    callback7.setCommitContentResult(false, callbackSeq7);
                                    args8.recycle();
                                    return;
                                } catch (RemoteException e7) {
                                    Log.w(TAG, "Got RemoteException calling commitContent", e7);
                                } catch (Throwable th8) {
                                    args8.recycle();
                                    throw th8;
                                }
                            default:
                                Log.w(TAG, "Unhandled message code: " + msg.what);
                                return;
                        }
                        break;
                }
        }
    }

    /* access modifiers changed from: package-private */
    public Message obtainMessage(int what) {
        return this.mH.obtainMessage(what);
    }

    /* access modifiers changed from: package-private */
    public Message obtainMessageII(int what, int arg1, int arg2) {
        return this.mH.obtainMessage(what, arg1, arg2);
    }

    /* access modifiers changed from: package-private */
    public Message obtainMessageO(int what, Object arg1) {
        return this.mH.obtainMessage(what, 0, 0, arg1);
    }

    /* access modifiers changed from: package-private */
    public Message obtainMessageISC(int what, int arg1, int callbackSeq, IInputContextCallback callback) {
        SomeArgs args = SomeArgs.obtain();
        args.arg6 = callback;
        args.argi6 = callbackSeq;
        return this.mH.obtainMessage(what, arg1, 0, args);
    }

    /* access modifiers changed from: package-private */
    public Message obtainMessageIISC(int what, int arg1, int arg2, int callbackSeq, IInputContextCallback callback) {
        SomeArgs args = SomeArgs.obtain();
        args.arg6 = callback;
        args.argi6 = callbackSeq;
        return this.mH.obtainMessage(what, arg1, arg2, args);
    }

    /* access modifiers changed from: package-private */
    public Message obtainMessageIOOSC(int what, int arg1, Object objArg1, Object objArg2, int callbackSeq, IInputContextCallback callback) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = objArg1;
        args.arg2 = objArg2;
        args.arg6 = callback;
        args.argi6 = callbackSeq;
        return this.mH.obtainMessage(what, arg1, 0, args);
    }

    /* access modifiers changed from: package-private */
    public Message obtainMessageIOSC(int what, int arg1, Object arg2, int callbackSeq, IInputContextCallback callback) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = arg2;
        args.arg6 = callback;
        args.argi6 = callbackSeq;
        return this.mH.obtainMessage(what, arg1, 0, args);
    }

    /* access modifiers changed from: package-private */
    public Message obtainMessageIO(int what, int arg1, Object arg2) {
        return this.mH.obtainMessage(what, arg1, 0, arg2);
    }

    /* access modifiers changed from: package-private */
    public Message obtainMessageOO(int what, Object arg1, Object arg2) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = arg1;
        args.arg2 = arg2;
        return this.mH.obtainMessage(what, 0, 0, args);
    }
}
