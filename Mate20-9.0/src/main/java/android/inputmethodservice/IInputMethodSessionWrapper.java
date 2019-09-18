package android.inputmethodservice;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.InputMethodSession;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;
import com.android.internal.view.IInputMethodSession;

class IInputMethodSessionWrapper extends IInputMethodSession.Stub implements HandlerCaller.Callback {
    private static final int DO_APP_PRIVATE_COMMAND = 100;
    private static final int DO_DISPLAY_COMPLETIONS = 65;
    private static final int DO_FINISH_INPUT = 60;
    private static final int DO_FINISH_SESSION = 110;
    private static final int DO_TOGGLE_SOFT_INPUT = 105;
    private static final int DO_UPDATE_CURSOR = 95;
    private static final int DO_UPDATE_CURSOR_ANCHOR_INFO = 99;
    private static final int DO_UPDATE_EXTRACTED_TEXT = 67;
    private static final int DO_UPDATE_SELECTION = 90;
    private static final int DO_VIEW_CLICKED = 115;
    private static final String TAG = "InputMethodWrapper";
    HandlerCaller mCaller;
    InputChannel mChannel;
    InputMethodSession mInputMethodSession;
    ImeInputEventReceiver mReceiver;

    private final class ImeInputEventReceiver extends InputEventReceiver implements InputMethodSession.EventCallback {
        private final SparseArray<InputEvent> mPendingEvents = new SparseArray<>();

        public ImeInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent event, int displayId) {
            if (IInputMethodSessionWrapper.this.mInputMethodSession == null) {
                finishInputEvent(event, false);
                return;
            }
            int seq = event.getSequenceNumber();
            this.mPendingEvents.put(seq, event);
            if (event instanceof KeyEvent) {
                IInputMethodSessionWrapper.this.mInputMethodSession.dispatchKeyEvent(seq, (KeyEvent) event, this);
            } else {
                MotionEvent motionEvent = (MotionEvent) event;
                if (motionEvent.isFromSource(4)) {
                    IInputMethodSessionWrapper.this.mInputMethodSession.dispatchTrackballEvent(seq, motionEvent, this);
                } else {
                    IInputMethodSessionWrapper.this.mInputMethodSession.dispatchGenericMotionEvent(seq, motionEvent, this);
                }
            }
        }

        public void finishedEvent(int seq, boolean handled) {
            int index = this.mPendingEvents.indexOfKey(seq);
            if (index >= 0) {
                this.mPendingEvents.removeAt(index);
                finishInputEvent(this.mPendingEvents.valueAt(index), handled);
            }
        }
    }

    public IInputMethodSessionWrapper(Context context, InputMethodSession inputMethodSession, InputChannel channel) {
        this.mCaller = new HandlerCaller(context, null, this, true);
        this.mInputMethodSession = inputMethodSession;
        this.mChannel = channel;
        if (channel != null) {
            this.mReceiver = new ImeInputEventReceiver(channel, context.getMainLooper());
        }
    }

    public InputMethodSession getInternalInputMethodSession() {
        return this.mInputMethodSession;
    }

    public void executeMessage(Message msg) {
        if (this.mInputMethodSession == null) {
            int i = msg.what;
            if (i == 90 || i == 100) {
                ((SomeArgs) msg.obj).recycle();
            }
            return;
        }
        int i2 = msg.what;
        if (i2 == 60) {
            this.mInputMethodSession.finishInput();
        } else if (i2 == 65) {
            this.mInputMethodSession.displayCompletions((CompletionInfo[]) msg.obj);
        } else if (i2 == 67) {
            this.mInputMethodSession.updateExtractedText(msg.arg1, (ExtractedText) msg.obj);
        } else if (i2 == 90) {
            SomeArgs args = (SomeArgs) msg.obj;
            this.mInputMethodSession.updateSelection(args.argi1, args.argi2, args.argi3, args.argi4, args.argi5, args.argi6);
            args.recycle();
        } else if (i2 == 95) {
            this.mInputMethodSession.updateCursor((Rect) msg.obj);
        } else if (i2 == 105) {
            this.mInputMethodSession.toggleSoftInput(msg.arg1, msg.arg2);
        } else if (i2 == 110) {
            doFinishSession();
        } else if (i2 != 115) {
            switch (i2) {
                case 99:
                    this.mInputMethodSession.updateCursorAnchorInfo((CursorAnchorInfo) msg.obj);
                    return;
                case 100:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    this.mInputMethodSession.appPrivateCommand((String) args2.arg1, (Bundle) args2.arg2);
                    args2.recycle();
                    return;
                default:
                    Log.w(TAG, "Unhandled message code: " + msg.what);
                    return;
            }
        } else {
            InputMethodSession inputMethodSession = this.mInputMethodSession;
            boolean z = true;
            if (msg.arg1 != 1) {
                z = false;
            }
            inputMethodSession.viewClicked(z);
        }
    }

    private void doFinishSession() {
        this.mInputMethodSession = null;
        if (this.mReceiver != null) {
            this.mReceiver.dispose();
            this.mReceiver = null;
        }
        if (this.mChannel != null) {
            this.mChannel.dispose();
            this.mChannel = null;
        }
    }

    public void finishInput() {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessage(60));
    }

    public void displayCompletions(CompletionInfo[] completions) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(65, completions));
    }

    public void updateExtractedText(int token, ExtractedText text) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageIO(67, token, text));
    }

    public void updateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageIIIIII(90, oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd));
    }

    public void viewClicked(boolean focusChanged) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageI(115, focusChanged));
    }

    public void updateCursor(Rect newCursor) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(95, newCursor));
    }

    public void updateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(99, cursorAnchorInfo));
    }

    public void appPrivateCommand(String action, Bundle data) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageOO(100, action, data));
    }

    public void toggleSoftInput(int showFlags, int hideFlags) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageII(105, showFlags, hideFlags));
    }

    public void finishSession() {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessage(110));
    }
}
