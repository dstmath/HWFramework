package android.inputmethodservice;

import android.Manifest.permission;
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
import android.view.inputmethod.InputMethod.SessionCallback;
import android.view.inputmethod.InputMethodSession;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.HandlerCaller.Callback;
import com.android.internal.os.SomeArgs;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethod.Stub;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.IInputSessionCallback;
import com.android.internal.view.InputConnectionWrapper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class IInputMethodWrapper extends Stub implements Callback {
    private static final int DO_ATTACH_TOKEN = 10;
    private static final int DO_CHANGE_INPUTMETHOD_SUBTYPE = 80;
    private static final int DO_CREATE_SESSION = 40;
    private static final int DO_DUMP = 1;
    private static final int DO_HIDE_SOFT_INPUT = 70;
    private static final int DO_RESTART_INPUT = 34;
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
    final WeakReference<AbstractInputMethodService> mTarget;
    final int mTargetSdkVersion;

    static final class InputMethodSessionCallbackWrapper implements SessionCallback {
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
                    return;
                } catch (RemoteException e) {
                    return;
                }
            }
            if (this.mChannel != null) {
                this.mChannel.dispose();
            }
            this.mCb.sessionCreated(null);
        }
    }

    static class Notifier {
        boolean notified;

        Notifier() {
        }
    }

    public IInputMethodWrapper(AbstractInputMethodService context, InputMethod inputMethod) {
        this.mTarget = new WeakReference(context);
        this.mContext = context.getApplicationContext();
        this.mCaller = new HandlerCaller(this.mContext, null, this, true);
        this.mInputMethod = new WeakReference(inputMethod);
        this.mTargetSdkVersion = context.getApplicationInfo().targetSdkVersion;
    }

    public InputMethod getInternalInputMethod() {
        return (InputMethod) this.mInputMethod.get();
    }

    public void executeMessage(Message msg) {
        boolean z = true;
        InputMethod inputMethod = (InputMethod) this.mInputMethod.get();
        if (inputMethod != null || msg.what == DO_DUMP) {
            SomeArgs args;
            IInputContext inputContext;
            InputConnection inputConnectionWrapper;
            EditorInfo info;
            switch (msg.what) {
                case DO_DUMP /*1*/:
                    AbstractInputMethodService target = (AbstractInputMethodService) this.mTarget.get();
                    if (target != null) {
                        args = msg.obj;
                        try {
                            target.dump((FileDescriptor) args.arg1, (PrintWriter) args.arg2, (String[]) args.arg3);
                        } catch (RuntimeException e) {
                            ((PrintWriter) args.arg2).println("Exception: " + e);
                        }
                        synchronized (args.arg4) {
                            ((CountDownLatch) args.arg4).countDown();
                            break;
                        }
                        args.recycle();
                        return;
                    }
                    return;
                case DO_ATTACH_TOKEN /*10*/:
                    inputMethod.attachToken((IBinder) msg.obj);
                    return;
                case DO_SET_INPUT_CONTEXT /*20*/:
                    inputMethod.bindInput((InputBinding) msg.obj);
                    return;
                case DO_UNSET_INPUT_CONTEXT /*30*/:
                    inputMethod.unbindInput();
                    return;
                case DO_START_INPUT /*32*/:
                    args = (SomeArgs) msg.obj;
                    inputContext = args.arg1;
                    inputConnectionWrapper = inputContext != null ? new InputConnectionWrapper(inputContext, msg.arg1) : null;
                    info = args.arg2;
                    info.makeCompatible(this.mTargetSdkVersion);
                    inputMethod.startInput(inputConnectionWrapper, info);
                    args.recycle();
                    return;
                case DO_RESTART_INPUT /*34*/:
                    args = (SomeArgs) msg.obj;
                    inputContext = (IInputContext) args.arg1;
                    inputConnectionWrapper = inputContext != null ? new InputConnectionWrapper(inputContext, msg.arg1) : null;
                    info = (EditorInfo) args.arg2;
                    info.makeCompatible(this.mTargetSdkVersion);
                    inputMethod.restartInput(inputConnectionWrapper, info);
                    args.recycle();
                    return;
                case DO_CREATE_SESSION /*40*/:
                    args = (SomeArgs) msg.obj;
                    inputMethod.createSession(new InputMethodSessionCallbackWrapper(this.mContext, (InputChannel) args.arg1, (IInputSessionCallback) args.arg2));
                    args.recycle();
                    return;
                case DO_SET_SESSION_ENABLED /*45*/:
                    InputMethodSession inputMethodSession = (InputMethodSession) msg.obj;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    inputMethod.setSessionEnabled(inputMethodSession, z);
                    return;
                case DO_REVOKE_SESSION /*50*/:
                    inputMethod.revokeSession((InputMethodSession) msg.obj);
                    return;
                case DO_SHOW_SOFT_INPUT /*60*/:
                    inputMethod.showSoftInput(msg.arg1, (ResultReceiver) msg.obj);
                    return;
                case DO_HIDE_SOFT_INPUT /*70*/:
                    inputMethod.hideSoftInput(msg.arg1, (ResultReceiver) msg.obj);
                    return;
                case DO_CHANGE_INPUTMETHOD_SUBTYPE /*80*/:
                    inputMethod.changeInputMethodSubtype((InputMethodSubtype) msg.obj);
                    return;
                default:
                    Log.w(TAG, "Unhandled message code: " + msg.what);
                    return;
            }
        }
        Log.w(TAG, "Input method reference was null, ignoring message: " + msg.what);
    }

    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        AbstractInputMethodService target = (AbstractInputMethodService) this.mTarget.get();
        if (target != null) {
            if (target.checkCallingOrSelfPermission(permission.DUMP) != 0) {
                fout.println("Permission Denial: can't dump InputMethodManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            CountDownLatch latch = new CountDownLatch(DO_DUMP);
            this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageOOOO(DO_DUMP, fd, fout, args, latch));
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
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(DO_ATTACH_TOKEN, token));
    }

    public void bindInput(InputBinding binding) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(DO_SET_INPUT_CONTEXT, new InputBinding(new InputConnectionWrapper(IInputContext.Stub.asInterface(binding.getConnectionToken()), 0), binding)));
    }

    public void unbindInput() {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessage(DO_UNSET_INPUT_CONTEXT));
    }

    public void startInput(IInputContext inputContext, int missingMethods, EditorInfo attribute) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageIOO(DO_START_INPUT, missingMethods, inputContext, attribute));
    }

    public void restartInput(IInputContext inputContext, int missingMethods, EditorInfo attribute) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageIOO(DO_RESTART_INPUT, missingMethods, inputContext, attribute));
    }

    public void createSession(InputChannel channel, IInputSessionCallback callback) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageOO(DO_CREATE_SESSION, channel, callback));
    }

    public void setSessionEnabled(IInputMethodSession session, boolean enabled) {
        try {
            InputMethodSession ls = ((IInputMethodSessionWrapper) session).getInternalInputMethodSession();
            if (ls == null) {
                Log.w(TAG, "Session is already finished: " + session);
            } else {
                this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageIO(DO_SET_SESSION_ENABLED, enabled ? DO_DUMP : 0, ls));
            }
        } catch (ClassCastException e) {
            Log.w(TAG, "Incoming session not of correct type: " + session, e);
        }
    }

    public void revokeSession(IInputMethodSession session) {
        try {
            InputMethodSession ls = ((IInputMethodSessionWrapper) session).getInternalInputMethodSession();
            if (ls == null) {
                Log.w(TAG, "Session is already finished: " + session);
            } else {
                this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(DO_REVOKE_SESSION, ls));
            }
        } catch (ClassCastException e) {
            Log.w(TAG, "Incoming session not of correct type: " + session, e);
        }
    }

    public void showSoftInput(int flags, ResultReceiver resultReceiver) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageIO(DO_SHOW_SOFT_INPUT, flags, resultReceiver));
    }

    public void hideSoftInput(int flags, ResultReceiver resultReceiver) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageIO(DO_HIDE_SOFT_INPUT, flags, resultReceiver));
    }

    public void changeInputMethodSubtype(InputMethodSubtype subtype) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(DO_CHANGE_INPUTMETHOD_SUBTYPE, subtype));
    }
}
