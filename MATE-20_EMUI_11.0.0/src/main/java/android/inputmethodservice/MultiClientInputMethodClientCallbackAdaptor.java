package android.inputmethodservice;

import android.graphics.Rect;
import android.inputmethodservice.MultiClientInputMethodServiceDelegate;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.inputmethod.IMultiClientInputMethodSession;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.InputConnectionWrapper;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

final class MultiClientInputMethodClientCallbackAdaptor {
    static final boolean DEBUG = false;
    static final String TAG = MultiClientInputMethodClientCallbackAdaptor.class.getSimpleName();
    @GuardedBy({"mSessionLock"})
    CallbackImpl mCallbackImpl;
    @GuardedBy({"mSessionLock"})
    KeyEvent.DispatcherState mDispatcherState;
    private final AtomicBoolean mFinished = new AtomicBoolean(false);
    @GuardedBy({"mSessionLock"})
    Handler mHandler;
    @GuardedBy({"mSessionLock"})
    InputEventReceiver mInputEventReceiver;
    @GuardedBy({"mSessionLock"})
    InputChannel mReadChannel;
    private final Object mSessionLock = new Object();

    /* access modifiers changed from: package-private */
    public IInputMethodSession.Stub createIInputMethodSession() {
        InputMethodSessionImpl inputMethodSessionImpl;
        synchronized (this.mSessionLock) {
            inputMethodSessionImpl = new InputMethodSessionImpl(this.mSessionLock, this.mCallbackImpl, this.mHandler, this.mFinished);
        }
        return inputMethodSessionImpl;
    }

    /* access modifiers changed from: package-private */
    public IMultiClientInputMethodSession.Stub createIMultiClientInputMethodSession() {
        MultiClientInputMethodSessionImpl multiClientInputMethodSessionImpl;
        synchronized (this.mSessionLock) {
            multiClientInputMethodSessionImpl = new MultiClientInputMethodSessionImpl(this.mSessionLock, this.mCallbackImpl, this.mHandler, this.mFinished);
        }
        return multiClientInputMethodSessionImpl;
    }

    MultiClientInputMethodClientCallbackAdaptor(MultiClientInputMethodServiceDelegate.ClientCallback clientCallback, Looper looper, KeyEvent.DispatcherState dispatcherState, InputChannel readChannel) {
        synchronized (this.mSessionLock) {
            this.mCallbackImpl = new CallbackImpl(this, clientCallback);
            this.mDispatcherState = dispatcherState;
            this.mHandler = new Handler(looper, null, true);
            this.mReadChannel = readChannel;
            this.mInputEventReceiver = new ImeInputEventReceiver(this.mReadChannel, this.mHandler.getLooper(), this.mFinished, this.mDispatcherState, this.mCallbackImpl.mOriginalCallback);
        }
    }

    private static final class KeyEventCallbackAdaptor implements KeyEvent.Callback {
        private final MultiClientInputMethodServiceDelegate.ClientCallback mLocalCallback;

        KeyEventCallbackAdaptor(MultiClientInputMethodServiceDelegate.ClientCallback callback) {
            this.mLocalCallback = callback;
        }

        @Override // android.view.KeyEvent.Callback
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            return this.mLocalCallback.onKeyDown(keyCode, event);
        }

        @Override // android.view.KeyEvent.Callback
        public boolean onKeyLongPress(int keyCode, KeyEvent event) {
            return this.mLocalCallback.onKeyLongPress(keyCode, event);
        }

        @Override // android.view.KeyEvent.Callback
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            return this.mLocalCallback.onKeyUp(keyCode, event);
        }

        @Override // android.view.KeyEvent.Callback
        public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
            return this.mLocalCallback.onKeyMultiple(keyCode, event);
        }
    }

    private static final class ImeInputEventReceiver extends InputEventReceiver {
        private final MultiClientInputMethodServiceDelegate.ClientCallback mClientCallback;
        private final KeyEvent.DispatcherState mDispatcherState;
        private final AtomicBoolean mFinished;
        private final KeyEventCallbackAdaptor mKeyEventCallbackAdaptor;

        ImeInputEventReceiver(InputChannel readChannel, Looper looper, AtomicBoolean finished, KeyEvent.DispatcherState dispatcherState, MultiClientInputMethodServiceDelegate.ClientCallback callback) {
            super(readChannel, looper);
            this.mFinished = finished;
            this.mDispatcherState = dispatcherState;
            this.mClientCallback = callback;
            this.mKeyEventCallbackAdaptor = new KeyEventCallbackAdaptor(callback);
        }

        @Override // android.view.InputEventReceiver
        public void onInputEvent(InputEvent event) {
            boolean handled;
            if (this.mFinished.get()) {
                finishInputEvent(event, false);
                return;
            }
            boolean handled2 = false;
            try {
                if (event instanceof KeyEvent) {
                    handled = ((KeyEvent) event).dispatch(this.mKeyEventCallbackAdaptor, this.mDispatcherState, this.mKeyEventCallbackAdaptor);
                } else {
                    MotionEvent motionEvent = (MotionEvent) event;
                    if (motionEvent.isFromSource(4)) {
                        handled = this.mClientCallback.onTrackballEvent(motionEvent);
                    } else {
                        handled = this.mClientCallback.onGenericMotionEvent(motionEvent);
                    }
                }
            } finally {
                finishInputEvent(event, handled2);
            }
        }
    }

    private static final class InputMethodSessionImpl extends IInputMethodSession.Stub {
        @GuardedBy({"mSessionLock"})
        private CallbackImpl mCallbackImpl;
        @GuardedBy({"mSessionLock"})
        private Handler mHandler;
        private final AtomicBoolean mSessionFinished;
        private final Object mSessionLock;

        InputMethodSessionImpl(Object lock, CallbackImpl callback, Handler handler, AtomicBoolean sessionFinished) {
            this.mSessionLock = lock;
            this.mCallbackImpl = callback;
            this.mHandler = handler;
            this.mSessionFinished = sessionFinished;
        }

        @Override // com.android.internal.view.IInputMethodSession
        public void updateExtractedText(int token, ExtractedText text) {
            MultiClientInputMethodClientCallbackAdaptor.reportNotSupported();
        }

        @Override // com.android.internal.view.IInputMethodSession
        public void updateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
            synchronized (this.mSessionLock) {
                if (this.mCallbackImpl != null) {
                    if (this.mHandler != null) {
                        SomeArgs args = SomeArgs.obtain();
                        args.argi1 = oldSelStart;
                        args.argi2 = oldSelEnd;
                        args.argi3 = newSelStart;
                        args.argi4 = newSelEnd;
                        args.argi5 = candidatesStart;
                        args.argi6 = candidatesEnd;
                        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$zVy_pAXuQfncxA_AL_8DWyVpYXc.INSTANCE, this.mCallbackImpl, args));
                    }
                }
            }
        }

        @Override // com.android.internal.view.IInputMethodSession
        public void viewClicked(boolean focusChanged) {
            MultiClientInputMethodClientCallbackAdaptor.reportNotSupported();
        }

        @Override // com.android.internal.view.IInputMethodSession
        public void updateCursor(Rect newCursor) {
            MultiClientInputMethodClientCallbackAdaptor.reportNotSupported();
        }

        @Override // com.android.internal.view.IInputMethodSession
        public void displayCompletions(CompletionInfo[] completions) {
            synchronized (this.mSessionLock) {
                if (this.mCallbackImpl != null) {
                    if (this.mHandler != null) {
                        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$RawqPImrGiEy8dXqjapbiFcFS9w.INSTANCE, this.mCallbackImpl, completions));
                    }
                }
            }
        }

        @Override // com.android.internal.view.IInputMethodSession
        public void appPrivateCommand(String action, Bundle data) {
            synchronized (this.mSessionLock) {
                if (this.mCallbackImpl != null) {
                    if (this.mHandler != null) {
                        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$nzQNVb4Z0e33hB95nNP1BMA3r4.INSTANCE, this.mCallbackImpl, action, data));
                    }
                }
            }
        }

        @Override // com.android.internal.view.IInputMethodSession
        public void toggleSoftInput(int showFlags, int hideFlags) {
            synchronized (this.mSessionLock) {
                if (this.mCallbackImpl != null) {
                    if (this.mHandler != null) {
                        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$GapYa6Lyify6RwPrgkklzmDV8I.INSTANCE, this.mCallbackImpl, Integer.valueOf(showFlags), Integer.valueOf(hideFlags)));
                    }
                }
            }
        }

        @Override // com.android.internal.view.IInputMethodSession
        public void finishSession() {
            synchronized (this.mSessionLock) {
                if (this.mCallbackImpl != null) {
                    if (this.mHandler != null) {
                        this.mSessionFinished.set(true);
                        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$50K3nJOOPDYkhKRI6jLQ5NjnbLU.INSTANCE, this.mCallbackImpl));
                        this.mCallbackImpl = null;
                        this.mHandler = null;
                    }
                }
            }
        }

        @Override // com.android.internal.view.IInputMethodSession
        public void updateCursorAnchorInfo(CursorAnchorInfo info) {
            synchronized (this.mSessionLock) {
                if (this.mCallbackImpl != null) {
                    if (this.mHandler != null) {
                        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$BAvs3tw1MzE4gOJqYOA5MCJasPE.INSTANCE, this.mCallbackImpl, info));
                    }
                }
            }
        }

        @Override // com.android.internal.view.IInputMethodSession
        public final void notifyImeHidden() {
            MultiClientInputMethodClientCallbackAdaptor.reportNotSupported();
        }
    }

    private static final class MultiClientInputMethodSessionImpl extends IMultiClientInputMethodSession.Stub {
        @GuardedBy({"mSessionLock"})
        private CallbackImpl mCallbackImpl;
        @GuardedBy({"mSessionLock"})
        private Handler mHandler;
        private final AtomicBoolean mSessionFinished;
        private final Object mSessionLock;

        MultiClientInputMethodSessionImpl(Object lock, CallbackImpl callback, Handler handler, AtomicBoolean sessionFinished) {
            this.mSessionLock = lock;
            this.mCallbackImpl = callback;
            this.mHandler = handler;
            this.mSessionFinished = sessionFinished;
        }

        @Override // com.android.internal.inputmethod.IMultiClientInputMethodSession
        public void startInputOrWindowGainedFocus(IInputContext inputContext, int missingMethods, EditorInfo editorInfo, int controlFlags, int softInputMode, int windowHandle) {
            synchronized (this.mSessionLock) {
                if (this.mCallbackImpl != null) {
                    if (this.mHandler != null) {
                        SomeArgs args = SomeArgs.obtain();
                        InputConnectionWrapper inputConnectionWrapper = null;
                        WeakReference<AbstractInputMethodService> fakeIMS = new WeakReference<>(null);
                        if (inputContext != null) {
                            inputConnectionWrapper = new InputConnectionWrapper(fakeIMS, inputContext, missingMethods, this.mSessionFinished);
                        }
                        args.arg1 = inputConnectionWrapper;
                        args.arg2 = editorInfo;
                        args.argi1 = controlFlags;
                        args.argi2 = softInputMode;
                        args.argi3 = windowHandle;
                        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$Xt9K6cDxkSefTfR7zi9nidRFZ8.INSTANCE, this.mCallbackImpl, args));
                    }
                }
            }
        }

        @Override // com.android.internal.inputmethod.IMultiClientInputMethodSession
        public void showSoftInput(int flags, ResultReceiver resultReceiver) {
            synchronized (this.mSessionLock) {
                if (this.mCallbackImpl != null) {
                    if (this.mHandler != null) {
                        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$m1uOlwSmRsg9KSUY6vV9l9ksWc.INSTANCE, this.mCallbackImpl, Integer.valueOf(flags), resultReceiver));
                    }
                }
            }
        }

        @Override // com.android.internal.inputmethod.IMultiClientInputMethodSession
        public void hideSoftInput(int flags, ResultReceiver resultReceiver) {
            synchronized (this.mSessionLock) {
                if (this.mCallbackImpl != null) {
                    if (this.mHandler != null) {
                        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$0tnQSRQlZ73hLobz1ZfjUIoiCl0.INSTANCE, this.mCallbackImpl, Integer.valueOf(flags), resultReceiver));
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class CallbackImpl {
        private final MultiClientInputMethodClientCallbackAdaptor mCallbackAdaptor;
        private boolean mFinished = false;
        private final MultiClientInputMethodServiceDelegate.ClientCallback mOriginalCallback;

        CallbackImpl(MultiClientInputMethodClientCallbackAdaptor callbackAdaptor, MultiClientInputMethodServiceDelegate.ClientCallback callback) {
            this.mCallbackAdaptor = callbackAdaptor;
            this.mOriginalCallback = callback;
        }

        /* access modifiers changed from: package-private */
        public void updateSelection(SomeArgs args) {
            try {
                if (!this.mFinished) {
                    this.mOriginalCallback.onUpdateSelection(args.argi1, args.argi2, args.argi3, args.argi4, args.argi5, args.argi6);
                    args.recycle();
                }
            } finally {
                args.recycle();
            }
        }

        /* access modifiers changed from: package-private */
        public void displayCompletions(CompletionInfo[] completions) {
            if (!this.mFinished) {
                this.mOriginalCallback.onDisplayCompletions(completions);
            }
        }

        /* access modifiers changed from: package-private */
        public void appPrivateCommand(String action, Bundle data) {
            if (!this.mFinished) {
                this.mOriginalCallback.onAppPrivateCommand(action, data);
            }
        }

        /* access modifiers changed from: package-private */
        public void toggleSoftInput(int showFlags, int hideFlags) {
            if (!this.mFinished) {
                this.mOriginalCallback.onToggleSoftInput(showFlags, hideFlags);
            }
        }

        /* access modifiers changed from: package-private */
        public void finishSession() {
            if (!this.mFinished) {
                this.mFinished = true;
                this.mOriginalCallback.onFinishSession();
                synchronized (this.mCallbackAdaptor.mSessionLock) {
                    this.mCallbackAdaptor.mDispatcherState = null;
                    if (this.mCallbackAdaptor.mReadChannel != null) {
                        this.mCallbackAdaptor.mReadChannel.dispose();
                        this.mCallbackAdaptor.mReadChannel = null;
                    }
                    this.mCallbackAdaptor.mInputEventReceiver = null;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void updateCursorAnchorInfo(CursorAnchorInfo info) {
            if (!this.mFinished) {
                this.mOriginalCallback.onUpdateCursorAnchorInfo(info);
            }
        }

        /* access modifiers changed from: package-private */
        public void startInputOrWindowGainedFocus(SomeArgs args) {
            try {
                if (!this.mFinished) {
                    int startInputFlags = args.argi1;
                    int softInputMode = args.argi2;
                    int windowHandle = args.argi3;
                    this.mOriginalCallback.onStartInputOrWindowGainedFocus((InputConnectionWrapper) args.arg1, (EditorInfo) args.arg2, startInputFlags, softInputMode, windowHandle);
                    args.recycle();
                }
            } finally {
                args.recycle();
            }
        }

        /* access modifiers changed from: package-private */
        public void showSoftInput(int flags, ResultReceiver resultReceiver) {
            if (!this.mFinished) {
                this.mOriginalCallback.onShowSoftInput(flags, resultReceiver);
            }
        }

        /* access modifiers changed from: package-private */
        public void hideSoftInput(int flags, ResultReceiver resultReceiver) {
            if (!this.mFinished) {
                this.mOriginalCallback.onHideSoftInput(flags, resultReceiver);
            }
        }
    }

    /* access modifiers changed from: private */
    public static void reportNotSupported() {
    }
}
