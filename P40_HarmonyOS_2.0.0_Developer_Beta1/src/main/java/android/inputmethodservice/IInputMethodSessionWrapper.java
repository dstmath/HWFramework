package android.inputmethodservice;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.HwLogUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseLongArray;
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
    private static final int DO_FINISH_SESSION = 110;
    private static final int DO_NOTIFY_IME_HIDDEN = 120;
    private static final int DO_TOGGLE_SOFT_INPUT = 105;
    private static final int DO_UPDATE_CURSOR = 95;
    private static final int DO_UPDATE_CURSOR_ANCHOR_INFO = 99;
    private static final int DO_UPDATE_EXTRACTED_TEXT = 67;
    private static final int DO_UPDATE_SELECTION = 90;
    private static final int DO_VIEW_CLICKED = 115;
    private static final String TAG = "InputMethodWrapper";
    @UnsupportedAppUsage
    HandlerCaller mCaller;
    InputChannel mChannel;
    InputMethodSession mInputMethodSession;
    ImeInputEventReceiver mReceiver;

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

    @Override // com.android.internal.os.HandlerCaller.Callback
    public void executeMessage(Message msg) {
        if (this.mInputMethodSession == null) {
            int i = msg.what;
            if (i == 90 || i == 100) {
                ((SomeArgs) msg.obj).recycle();
                return;
            }
            return;
        }
        int i2 = msg.what;
        if (i2 == 65) {
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
        } else if (i2 == 115) {
            InputMethodSession inputMethodSession = this.mInputMethodSession;
            boolean z = true;
            if (msg.arg1 != 1) {
                z = false;
            }
            inputMethodSession.viewClicked(z);
        } else if (i2 == 120) {
            this.mInputMethodSession.notifyImeHidden();
        } else if (i2 == 99) {
            this.mInputMethodSession.updateCursorAnchorInfo((CursorAnchorInfo) msg.obj);
        } else if (i2 != 100) {
            Log.w(TAG, "Unhandled message code: " + msg.what);
        } else {
            SomeArgs args2 = (SomeArgs) msg.obj;
            this.mInputMethodSession.appPrivateCommand((String) args2.arg1, (Bundle) args2.arg2);
            args2.recycle();
        }
    }

    private void doFinishSession() {
        this.mInputMethodSession = null;
        ImeInputEventReceiver imeInputEventReceiver = this.mReceiver;
        if (imeInputEventReceiver != null) {
            imeInputEventReceiver.dispose();
            this.mReceiver = null;
        }
        InputChannel inputChannel = this.mChannel;
        if (inputChannel != null) {
            inputChannel.dispose();
            this.mChannel = null;
        }
    }

    @Override // com.android.internal.view.IInputMethodSession
    public void displayCompletions(CompletionInfo[] completions) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageO(65, completions));
    }

    @Override // com.android.internal.view.IInputMethodSession
    public void updateExtractedText(int token, ExtractedText text) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageIO(67, token, text));
    }

    @Override // com.android.internal.view.IInputMethodSession
    public void updateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageIIIIII(90, oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd));
    }

    @Override // com.android.internal.view.IInputMethodSession
    public void viewClicked(boolean focusChanged) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageI(115, focusChanged ? 1 : 0));
    }

    @Override // com.android.internal.view.IInputMethodSession
    public void notifyImeHidden() {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessage(120));
    }

    @Override // com.android.internal.view.IInputMethodSession
    public void updateCursor(Rect newCursor) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageO(95, newCursor));
    }

    @Override // com.android.internal.view.IInputMethodSession
    public void updateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageO(99, cursorAnchorInfo));
    }

    @Override // com.android.internal.view.IInputMethodSession
    public void appPrivateCommand(String action, Bundle data) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageOO(100, action, data));
    }

    @Override // com.android.internal.view.IInputMethodSession
    public void toggleSoftInput(int showFlags, int hideFlags) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageII(105, showFlags, hideFlags));
    }

    @Override // com.android.internal.view.IInputMethodSession
    public void finishSession() {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessage(110));
    }

    /* access modifiers changed from: private */
    public final class ImeInputEventReceiver extends InputEventReceiver implements InputMethodSession.EventCallback {
        private final SparseArray<InputEvent> mPendingEvents = new SparseArray<>();
        private final SparseLongArray mStartTimeMap = new SparseLongArray();

        public ImeInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        @Override // android.view.InputEventReceiver
        public void onInputEvent(InputEvent event) {
            if (IInputMethodSessionWrapper.this.mInputMethodSession == null) {
                finishInputEvent(event, false);
                return;
            }
            int seq = event.getSequenceNumber();
            this.mPendingEvents.put(seq, event);
            if (HwLogUtils.isDebugVersion()) {
                this.mStartTimeMap.put(seq, SystemClock.uptimeMillis());
            }
            if (event instanceof KeyEvent) {
                IInputMethodSessionWrapper.this.mInputMethodSession.dispatchKeyEvent(seq, (KeyEvent) event, this);
                return;
            }
            MotionEvent motionEvent = (MotionEvent) event;
            if (motionEvent.isFromSource(4)) {
                IInputMethodSessionWrapper.this.mInputMethodSession.dispatchTrackballEvent(seq, motionEvent, this);
            } else {
                IInputMethodSessionWrapper.this.mInputMethodSession.dispatchGenericMotionEvent(seq, motionEvent, this);
            }
        }

        @Override // android.view.inputmethod.InputMethodSession.EventCallback
        public void finishedEvent(int seq, boolean handled) {
            int index;
            int index2 = this.mPendingEvents.indexOfKey(seq);
            InputEvent event = null;
            if (index2 >= 0) {
                event = this.mPendingEvents.valueAt(index2);
                this.mPendingEvents.removeAt(index2);
                finishInputEvent(event, handled);
            }
            if (HwLogUtils.isDebugVersion() && (index = this.mStartTimeMap.indexOfKey(seq)) >= 0 && event != null) {
                HwLogUtils.checkTime(this.mStartTimeMap.valueAt(index), "finishedEvent", event);
            }
        }
    }
}
