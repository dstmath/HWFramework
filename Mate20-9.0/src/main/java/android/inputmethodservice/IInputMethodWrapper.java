package android.inputmethodservice;

import android.Manifest;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.InputChannel;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodSession;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethod;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.IInputSessionCallback;
import com.android.internal.view.InputConnectionWrapper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class IInputMethodWrapper extends IInputMethod.Stub implements HandlerCaller.Callback {
    private static final int DO_ATTACH_TOKEN = 10;
    private static final int DO_CHANGE_INPUTMETHOD_SUBTYPE = 80;
    private static final int DO_CREATE_SESSION = 40;
    private static final int DO_DUMP = 1;
    private static final int DO_HIDE_SOFT_INPUT = 70;
    private static final int DO_REVOKE_SESSION = 50;
    private static final int DO_SET_INPUT_CONTEXT = 20;
    private static final int DO_SET_SESSION_ENABLED = 45;
    private static final int DO_SHOW_SOFT_INPUT = 60;
    private static final int DO_START_INPUT = 32;
    private static final int DO_UNSET_INPUT_CONTEXT = 30;
    private static final String TAG = "InputMethodWrapper";
    final HandlerCaller mCaller;
    final Context mContext;
    final WeakReference<InputMethod> mInputMethod;
    AtomicBoolean mIsUnbindIssued = null;
    final WeakReference<AbstractInputMethodService> mTarget;
    final int mTargetSdkVersion;

    static final class InputMethodSessionCallbackWrapper implements InputMethod.SessionCallback {
        final IInputSessionCallback mCb;
        final InputChannel mChannel;
        final Context mContext;

        InputMethodSessionCallbackWrapper(Context context, InputChannel channel, IInputSessionCallback cb) {
            this.mContext = context;
            this.mChannel = channel;
            this.mCb = cb;
        }

        public void sessionCreated(InputMethodSession session) {
            if (session != null) {
                try {
                    this.mCb.sessionCreated(new IInputMethodSessionWrapper(this.mContext, session, this.mChannel));
                } catch (RemoteException e) {
                }
            } else {
                if (this.mChannel != null) {
                    this.mChannel.dispose();
                }
                this.mCb.sessionCreated(null);
            }
        }
    }

    public IInputMethodWrapper(AbstractInputMethodService context, InputMethod inputMethod) {
        this.mTarget = new WeakReference<>(context);
        this.mContext = context.getApplicationContext();
        this.mCaller = new HandlerCaller(this.mContext, null, this, true);
        this.mInputMethod = new WeakReference<>(inputMethod);
        this.mTargetSdkVersion = context.getApplicationInfo().targetSdkVersion;
    }

    public void executeMessage(Message msg) {
        InputConnection ic;
        InputMethod inputMethod = (InputMethod) this.mInputMethod.get();
        boolean restarting = true;
        if (inputMethod != null || msg.what == 1) {
            switch (msg.what) {
                case 1:
                    AbstractInputMethodService target = (AbstractInputMethodService) this.mTarget.get();
                    if (target != null) {
                        SomeArgs args = (SomeArgs) msg.obj;
                        try {
                            target.dump((FileDescriptor) args.arg1, (PrintWriter) args.arg2, (String[]) args.arg3);
                        } catch (RuntimeException e) {
                            ((PrintWriter) args.arg2).println("Exception: " + e);
                        }
                        synchronized (args.arg4) {
                            ((CountDownLatch) args.arg4).countDown();
                        }
                        args.recycle();
                        return;
                    }
                    return;
                case 10:
                    inputMethod.attachToken((IBinder) msg.obj);
                    return;
                case 20:
                    inputMethod.bindInput((InputBinding) msg.obj);
                    return;
                case 30:
                    inputMethod.unbindInput();
                    return;
                case 32:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    int missingMethods = msg.arg1;
                    if (msg.arg2 == 0) {
                        restarting = false;
                    }
                    IBinder startInputToken = (IBinder) args2.arg1;
                    IInputContext inputContext = (IInputContext) args2.arg2;
                    EditorInfo info = (EditorInfo) args2.arg3;
                    AtomicBoolean isUnbindIssued = (AtomicBoolean) args2.arg4;
                    if (inputContext != null) {
                        ic = new InputConnectionWrapper(this.mTarget, inputContext, missingMethods, isUnbindIssued);
                    } else {
                        ic = null;
                    }
                    info.makeCompatible(this.mTargetSdkVersion);
                    inputMethod.dispatchStartInputWithToken(ic, info, restarting, startInputToken);
                    args2.recycle();
                    return;
                case 40:
                    SomeArgs args3 = (SomeArgs) msg.obj;
                    inputMethod.createSession(new InputMethodSessionCallbackWrapper(this.mContext, (InputChannel) args3.arg1, (IInputSessionCallback) args3.arg2));
                    args3.recycle();
                    return;
                case 45:
                    InputMethodSession inputMethodSession = (InputMethodSession) msg.obj;
                    if (msg.arg1 == 0) {
                        restarting = false;
                    }
                    inputMethod.setSessionEnabled(inputMethodSession, restarting);
                    return;
                case 50:
                    inputMethod.revokeSession((InputMethodSession) msg.obj);
                    return;
                case 60:
                    inputMethod.showSoftInput(msg.arg1, (ResultReceiver) msg.obj);
                    return;
                case 70:
                    inputMethod.hideSoftInput(msg.arg1, (ResultReceiver) msg.obj);
                    return;
                case 80:
                    inputMethod.changeInputMethodSubtype((InputMethodSubtype) msg.obj);
                    return;
                default:
                    Log.w(TAG, "Unhandled message code: " + msg.what);
                    return;
            }
        } else {
            Log.w(TAG, "Input method reference was null, ignoring message: " + msg.what);
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        AbstractInputMethodService target = (AbstractInputMethodService) this.mTarget.get();
        if (target != null) {
            if (target.checkCallingOrSelfPermission(Manifest.permission.DUMP) != 0) {
                fout.println("Permission Denial: can't dump InputMethodManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            CountDownLatch latch = new CountDownLatch(1);
            this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageOOOO(1, fd, fout, args, latch));
            try {
                if (!latch.await(5, TimeUnit.SECONDS)) {
                    fout.println("Timeout waiting for dump");
                }
            } catch (InterruptedException e) {
                fout.println("Interrupted waiting for dump");
            }
        }
    }

    public void attachToken(IBinder token) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(10, token));
    }

    public void bindInput(InputBinding binding) {
        if (this.mIsUnbindIssued != null) {
            Log.e(TAG, "bindInput must be paired with unbindInput.");
        }
        this.mIsUnbindIssued = new AtomicBoolean();
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(20, new InputBinding(new InputConnectionWrapper(this.mTarget, IInputContext.Stub.asInterface(binding.getConnectionToken()), 0, this.mIsUnbindIssued), binding)));
    }

    public void unbindInput() {
        if (this.mIsUnbindIssued != null) {
            this.mIsUnbindIssued.set(true);
            this.mIsUnbindIssued = null;
        } else {
            Log.e(TAG, "unbindInput must be paired with bindInput.");
        }
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessage(30));
    }

    public void startInput(IBinder startInputToken, IInputContext inputContext, int missingMethods, EditorInfo attribute, boolean restarting) {
        if (this.mIsUnbindIssued == null) {
            Log.e(TAG, "startInput must be called after bindInput.");
            this.mIsUnbindIssued = new AtomicBoolean();
        }
        HandlerCaller handlerCaller = this.mCaller;
        HandlerCaller handlerCaller2 = this.mCaller;
        AtomicBoolean atomicBoolean = this.mIsUnbindIssued;
        handlerCaller.executeOrSendMessage(handlerCaller2.obtainMessageIIOOOO(32, missingMethods, restarting ? 1 : 0, startInputToken, inputContext, attribute, atomicBoolean));
    }

    public void createSession(InputChannel channel, IInputSessionCallback callback) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageOO(40, channel, callback));
    }

    public void setSessionEnabled(IInputMethodSession session, boolean enabled) {
        try {
            InputMethodSession ls = ((IInputMethodSessionWrapper) session).getInternalInputMethodSession();
            if (ls == null) {
                Log.w(TAG, "Session is already finished: " + session);
                return;
            }
            this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageIO(45, enabled, ls));
        } catch (ClassCastException e) {
            Log.w(TAG, "Incoming session not of correct type: " + session, e);
        }
    }

    public void revokeSession(IInputMethodSession session) {
        try {
            InputMethodSession ls = ((IInputMethodSessionWrapper) session).getInternalInputMethodSession();
            if (ls == null) {
                Log.w(TAG, "Session is already finished: " + session);
                return;
            }
            this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(50, ls));
        } catch (ClassCastException e) {
            Log.w(TAG, "Incoming session not of correct type: " + session, e);
        }
    }

    public void showSoftInput(int flags, ResultReceiver resultReceiver) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageIO(60, flags, resultReceiver));
    }

    public void hideSoftInput(int flags, ResultReceiver resultReceiver) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageIO(70, flags, resultReceiver));
    }

    public void changeInputMethodSubtype(InputMethodSubtype subtype) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(80, subtype));
    }
}
