package android.service.voice;

import android.R;
import android.app.Dialog;
import android.app.DirectAction;
import android.app.Instrumentation;
import android.app.VoiceInteractor;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Region;
import android.inputmethodservice.SoftInputWindow;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Binder;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.Message;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.SettingsStringUtil;
import android.service.voice.IVoiceInteractionSession;
import android.util.ArrayMap;
import android.util.DebugUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import com.android.internal.annotations.Immutable;
import com.android.internal.app.IVoiceInteractionManagerService;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.app.IVoiceInteractorCallback;
import com.android.internal.app.IVoiceInteractorRequest;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.Preconditions;
import com.android.internal.util.function.pooled.PooledLambda;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class VoiceInteractionSession implements KeyEvent.Callback, ComponentCallbacks2 {
    static final boolean DEBUG = false;
    static final int MSG_CANCEL = 7;
    static final int MSG_CLOSE_SYSTEM_DIALOGS = 102;
    static final int MSG_DESTROY = 103;
    static final int MSG_HANDLE_ASSIST = 104;
    static final int MSG_HANDLE_SCREENSHOT = 105;
    static final int MSG_HIDE = 107;
    static final int MSG_ON_LOCKSCREEN_SHOWN = 108;
    static final int MSG_SHOW = 106;
    static final int MSG_START_ABORT_VOICE = 4;
    static final int MSG_START_COMMAND = 5;
    static final int MSG_START_COMPLETE_VOICE = 3;
    static final int MSG_START_CONFIRMATION = 1;
    static final int MSG_START_PICK_OPTION = 2;
    static final int MSG_SUPPORTS_COMMANDS = 6;
    static final int MSG_TASK_FINISHED = 101;
    static final int MSG_TASK_STARTED = 100;
    public static final int SHOW_SOURCE_ACTIVITY = 16;
    public static final int SHOW_SOURCE_APPLICATION = 8;
    public static final int SHOW_SOURCE_ASSIST_GESTURE = 4;
    public static final int SHOW_SOURCE_AUTOMOTIVE_SYSTEM_UI = 128;
    public static final int SHOW_SOURCE_NOTIFICATION = 64;
    public static final int SHOW_SOURCE_PUSH_TO_TALK = 32;
    public static final int SHOW_WITH_ASSIST = 1;
    public static final int SHOW_WITH_SCREENSHOT = 2;
    static final String TAG = "VoiceInteractionSession";
    final ArrayMap<IBinder, Request> mActiveRequests;
    final MyCallbacks mCallbacks;
    FrameLayout mContentFrame;
    final Context mContext;
    final KeyEvent.DispatcherState mDispatcherState;
    final HandlerCaller mHandlerCaller;
    boolean mInShowWindow;
    LayoutInflater mInflater;
    boolean mInitialized;
    final ViewTreeObserver.OnComputeInternalInsetsListener mInsetsComputer;
    final IVoiceInteractor mInteractor;
    ICancellationSignal mKillCallback;
    final Map<SafeResultListener, Consumer<Bundle>> mRemoteCallbacks;
    View mRootView;
    final IVoiceInteractionSession mSession;
    IVoiceInteractionManagerService mSystemService;
    int mTheme;
    TypedArray mThemeAttrs;
    final Insets mTmpInsets;
    IBinder mToken;
    boolean mUiEnabled;
    final WeakReference<VoiceInteractionSession> mWeakRef;
    SoftInputWindow mWindow;
    boolean mWindowAdded;
    boolean mWindowVisible;
    boolean mWindowWasVisible;

    public static final class Insets {
        public static final int TOUCHABLE_INSETS_CONTENT = 1;
        public static final int TOUCHABLE_INSETS_FRAME = 0;
        public static final int TOUCHABLE_INSETS_REGION = 3;
        public final Rect contentInsets = new Rect();
        public int touchableInsets;
        public final Region touchableRegion = new Region();
    }

    public static class Request {
        final IVoiceInteractorCallback mCallback;
        final String mCallingPackage;
        final int mCallingUid;
        final Bundle mExtras;
        final IVoiceInteractorRequest mInterface = new IVoiceInteractorRequest.Stub() {
            /* class android.service.voice.VoiceInteractionSession.Request.AnonymousClass1 */

            @Override // com.android.internal.app.IVoiceInteractorRequest
            public void cancel() throws RemoteException {
                VoiceInteractionSession session = Request.this.mSession.get();
                if (session != null) {
                    session.mHandlerCaller.sendMessage(session.mHandlerCaller.obtainMessageO(7, Request.this));
                }
            }
        };
        final WeakReference<VoiceInteractionSession> mSession;

        Request(String packageName, int uid, IVoiceInteractorCallback callback, VoiceInteractionSession session, Bundle extras) {
            this.mCallingPackage = packageName;
            this.mCallingUid = uid;
            this.mCallback = callback;
            this.mSession = session.mWeakRef;
            this.mExtras = extras;
        }

        public int getCallingUid() {
            return this.mCallingUid;
        }

        public String getCallingPackage() {
            return this.mCallingPackage;
        }

        public Bundle getExtras() {
            return this.mExtras;
        }

        public boolean isActive() {
            VoiceInteractionSession session = this.mSession.get();
            if (session == null) {
                return false;
            }
            return session.isRequestActive(this.mInterface.asBinder());
        }

        /* access modifiers changed from: package-private */
        public void finishRequest() {
            VoiceInteractionSession session = this.mSession.get();
            if (session != null) {
                Request req = session.removeRequest(this.mInterface.asBinder());
                if (req == null) {
                    throw new IllegalStateException("Request not active: " + this);
                } else if (req != this) {
                    throw new IllegalStateException("Current active request " + req + " not same as calling request " + this);
                }
            } else {
                throw new IllegalStateException("VoiceInteractionSession has been destroyed");
            }
        }

        public void cancel() {
            try {
                finishRequest();
                this.mCallback.deliverCancel(this.mInterface);
            } catch (RemoteException e) {
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            DebugUtils.buildShortClassTag(this, sb);
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(this.mInterface.asBinder());
            sb.append(" pkg=");
            sb.append(this.mCallingPackage);
            sb.append(" uid=");
            UserHandle.formatUid(sb, this.mCallingUid);
            sb.append('}');
            return sb.toString();
        }

        /* access modifiers changed from: package-private */
        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            writer.print(prefix);
            writer.print("mInterface=");
            writer.println(this.mInterface.asBinder());
            writer.print(prefix);
            writer.print("mCallingPackage=");
            writer.print(this.mCallingPackage);
            writer.print(" mCallingUid=");
            UserHandle.formatUid(writer, this.mCallingUid);
            writer.println();
            writer.print(prefix);
            writer.print("mCallback=");
            writer.println(this.mCallback.asBinder());
            if (this.mExtras != null) {
                writer.print(prefix);
                writer.print("mExtras=");
                writer.println(this.mExtras);
            }
        }
    }

    public static final class ConfirmationRequest extends Request {
        final VoiceInteractor.Prompt mPrompt;

        ConfirmationRequest(String packageName, int uid, IVoiceInteractorCallback callback, VoiceInteractionSession session, VoiceInteractor.Prompt prompt, Bundle extras) {
            super(packageName, uid, callback, session, extras);
            this.mPrompt = prompt;
        }

        public VoiceInteractor.Prompt getVoicePrompt() {
            return this.mPrompt;
        }

        @Deprecated
        public CharSequence getPrompt() {
            VoiceInteractor.Prompt prompt = this.mPrompt;
            if (prompt != null) {
                return prompt.getVoicePromptAt(0);
            }
            return null;
        }

        public void sendConfirmationResult(boolean confirmed, Bundle result) {
            try {
                finishRequest();
                this.mCallback.deliverConfirmationResult(this.mInterface, confirmed, result);
            } catch (RemoteException e) {
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.service.voice.VoiceInteractionSession.Request
        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            super.dump(prefix, fd, writer, args);
            writer.print(prefix);
            writer.print("mPrompt=");
            writer.println(this.mPrompt);
        }
    }

    public static final class PickOptionRequest extends Request {
        final VoiceInteractor.PickOptionRequest.Option[] mOptions;
        final VoiceInteractor.Prompt mPrompt;

        PickOptionRequest(String packageName, int uid, IVoiceInteractorCallback callback, VoiceInteractionSession session, VoiceInteractor.Prompt prompt, VoiceInteractor.PickOptionRequest.Option[] options, Bundle extras) {
            super(packageName, uid, callback, session, extras);
            this.mPrompt = prompt;
            this.mOptions = options;
        }

        public VoiceInteractor.Prompt getVoicePrompt() {
            return this.mPrompt;
        }

        @Deprecated
        public CharSequence getPrompt() {
            VoiceInteractor.Prompt prompt = this.mPrompt;
            if (prompt != null) {
                return prompt.getVoicePromptAt(0);
            }
            return null;
        }

        public VoiceInteractor.PickOptionRequest.Option[] getOptions() {
            return this.mOptions;
        }

        /* access modifiers changed from: package-private */
        public void sendPickOptionResult(boolean finished, VoiceInteractor.PickOptionRequest.Option[] selections, Bundle result) {
            if (finished) {
                try {
                    finishRequest();
                } catch (RemoteException e) {
                    return;
                }
            }
            this.mCallback.deliverPickOptionResult(this.mInterface, finished, selections, result);
        }

        public void sendIntermediatePickOptionResult(VoiceInteractor.PickOptionRequest.Option[] selections, Bundle result) {
            sendPickOptionResult(false, selections, result);
        }

        public void sendPickOptionResult(VoiceInteractor.PickOptionRequest.Option[] selections, Bundle result) {
            sendPickOptionResult(true, selections, result);
        }

        /* access modifiers changed from: package-private */
        @Override // android.service.voice.VoiceInteractionSession.Request
        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            super.dump(prefix, fd, writer, args);
            writer.print(prefix);
            writer.print("mPrompt=");
            writer.println(this.mPrompt);
            if (this.mOptions != null) {
                writer.print(prefix);
                writer.println("Options:");
                int i = 0;
                while (true) {
                    VoiceInteractor.PickOptionRequest.Option[] optionArr = this.mOptions;
                    if (i < optionArr.length) {
                        VoiceInteractor.PickOptionRequest.Option op = optionArr[i];
                        writer.print(prefix);
                        writer.print("  #");
                        writer.print(i);
                        writer.println(SettingsStringUtil.DELIMITER);
                        writer.print(prefix);
                        writer.print("    mLabel=");
                        writer.println(op.getLabel());
                        writer.print(prefix);
                        writer.print("    mIndex=");
                        writer.println(op.getIndex());
                        if (op.countSynonyms() > 0) {
                            writer.print(prefix);
                            writer.println("    Synonyms:");
                            for (int j = 0; j < op.countSynonyms(); j++) {
                                writer.print(prefix);
                                writer.print("      #");
                                writer.print(j);
                                writer.print(": ");
                                writer.println(op.getSynonymAt(j));
                            }
                        }
                        if (op.getExtras() != null) {
                            writer.print(prefix);
                            writer.print("    mExtras=");
                            writer.println(op.getExtras());
                        }
                        i++;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    public static final class CompleteVoiceRequest extends Request {
        final VoiceInteractor.Prompt mPrompt;

        CompleteVoiceRequest(String packageName, int uid, IVoiceInteractorCallback callback, VoiceInteractionSession session, VoiceInteractor.Prompt prompt, Bundle extras) {
            super(packageName, uid, callback, session, extras);
            this.mPrompt = prompt;
        }

        public VoiceInteractor.Prompt getVoicePrompt() {
            return this.mPrompt;
        }

        @Deprecated
        public CharSequence getMessage() {
            VoiceInteractor.Prompt prompt = this.mPrompt;
            if (prompt != null) {
                return prompt.getVoicePromptAt(0);
            }
            return null;
        }

        public void sendCompleteResult(Bundle result) {
            try {
                finishRequest();
                this.mCallback.deliverCompleteVoiceResult(this.mInterface, result);
            } catch (RemoteException e) {
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.service.voice.VoiceInteractionSession.Request
        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            super.dump(prefix, fd, writer, args);
            writer.print(prefix);
            writer.print("mPrompt=");
            writer.println(this.mPrompt);
        }
    }

    public static final class AbortVoiceRequest extends Request {
        final VoiceInteractor.Prompt mPrompt;

        AbortVoiceRequest(String packageName, int uid, IVoiceInteractorCallback callback, VoiceInteractionSession session, VoiceInteractor.Prompt prompt, Bundle extras) {
            super(packageName, uid, callback, session, extras);
            this.mPrompt = prompt;
        }

        public VoiceInteractor.Prompt getVoicePrompt() {
            return this.mPrompt;
        }

        @Deprecated
        public CharSequence getMessage() {
            VoiceInteractor.Prompt prompt = this.mPrompt;
            if (prompt != null) {
                return prompt.getVoicePromptAt(0);
            }
            return null;
        }

        public void sendAbortResult(Bundle result) {
            try {
                finishRequest();
                this.mCallback.deliverAbortVoiceResult(this.mInterface, result);
            } catch (RemoteException e) {
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.service.voice.VoiceInteractionSession.Request
        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            super.dump(prefix, fd, writer, args);
            writer.print(prefix);
            writer.print("mPrompt=");
            writer.println(this.mPrompt);
        }
    }

    public static final class CommandRequest extends Request {
        final String mCommand;

        CommandRequest(String packageName, int uid, IVoiceInteractorCallback callback, VoiceInteractionSession session, String command, Bundle extras) {
            super(packageName, uid, callback, session, extras);
            this.mCommand = command;
        }

        public String getCommand() {
            return this.mCommand;
        }

        /* access modifiers changed from: package-private */
        public void sendCommandResult(boolean finished, Bundle result) {
            if (finished) {
                try {
                    finishRequest();
                } catch (RemoteException e) {
                    return;
                }
            }
            this.mCallback.deliverCommandResult(this.mInterface, finished, result);
        }

        public void sendIntermediateResult(Bundle result) {
            sendCommandResult(false, result);
        }

        public void sendResult(Bundle result) {
            sendCommandResult(true, result);
        }

        /* access modifiers changed from: package-private */
        @Override // android.service.voice.VoiceInteractionSession.Request
        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            super.dump(prefix, fd, writer, args);
            writer.print(prefix);
            writer.print("mCommand=");
            writer.println(this.mCommand);
        }
    }

    /* access modifiers changed from: package-private */
    public class MyCallbacks implements HandlerCaller.Callback, SoftInputWindow.Callback {
        MyCallbacks() {
        }

        @Override // com.android.internal.os.HandlerCaller.Callback
        public void executeMessage(Message msg) {
            SomeArgs args = null;
            int i = msg.what;
            switch (i) {
                case 1:
                    VoiceInteractionSession.this.onRequestConfirmation((ConfirmationRequest) msg.obj);
                    break;
                case 2:
                    VoiceInteractionSession.this.onRequestPickOption((PickOptionRequest) msg.obj);
                    break;
                case 3:
                    VoiceInteractionSession.this.onRequestCompleteVoice((CompleteVoiceRequest) msg.obj);
                    break;
                case 4:
                    VoiceInteractionSession.this.onRequestAbortVoice((AbortVoiceRequest) msg.obj);
                    break;
                case 5:
                    VoiceInteractionSession.this.onRequestCommand((CommandRequest) msg.obj);
                    break;
                case 6:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    args2.arg1 = VoiceInteractionSession.this.onGetSupportedCommands((String[]) args2.arg1);
                    args2.complete();
                    args = null;
                    break;
                case 7:
                    VoiceInteractionSession.this.onCancelRequest((Request) msg.obj);
                    break;
                default:
                    switch (i) {
                        case 100:
                            VoiceInteractionSession.this.onTaskStarted((Intent) msg.obj, msg.arg1);
                            break;
                        case 101:
                            VoiceInteractionSession.this.onTaskFinished((Intent) msg.obj, msg.arg1);
                            break;
                        case 102:
                            VoiceInteractionSession.this.onCloseSystemDialogs();
                            break;
                        case 103:
                            VoiceInteractionSession.this.doDestroy();
                            break;
                        case 104:
                            args = (SomeArgs) msg.obj;
                            VoiceInteractionSession.this.doOnHandleAssist(args.argi1, (IBinder) args.arg5, (Bundle) args.arg1, (AssistStructure) args.arg2, (Throwable) args.arg3, (AssistContent) args.arg4, args.argi5, args.argi6);
                            break;
                        case 105:
                            VoiceInteractionSession.this.onHandleScreenshot((Bitmap) msg.obj);
                            break;
                        case 106:
                            args = (SomeArgs) msg.obj;
                            VoiceInteractionSession.this.doShow((Bundle) args.arg1, msg.arg1, (IVoiceInteractionSessionShowCallback) args.arg2);
                            break;
                        case 107:
                            VoiceInteractionSession.this.doHide();
                            break;
                        case 108:
                            VoiceInteractionSession.this.onLockscreenShown();
                            break;
                    }
            }
            if (args != null) {
                args.recycle();
            }
        }

        @Override // android.inputmethodservice.SoftInputWindow.Callback
        public void onBackPressed() {
            VoiceInteractionSession.this.onBackPressed();
        }
    }

    public VoiceInteractionSession(Context context) {
        this(context, new Handler());
    }

    public VoiceInteractionSession(Context context, Handler handler) {
        this.mDispatcherState = new KeyEvent.DispatcherState();
        this.mTheme = 0;
        this.mUiEnabled = true;
        this.mActiveRequests = new ArrayMap<>();
        this.mTmpInsets = new Insets();
        this.mWeakRef = new WeakReference<>(this);
        this.mRemoteCallbacks = new ArrayMap();
        this.mInteractor = new IVoiceInteractor.Stub() {
            /* class android.service.voice.VoiceInteractionSession.AnonymousClass1 */

            @Override // com.android.internal.app.IVoiceInteractor
            public IVoiceInteractorRequest startConfirmation(String callingPackage, IVoiceInteractorCallback callback, VoiceInteractor.Prompt prompt, Bundle extras) {
                ConfirmationRequest request = new ConfirmationRequest(callingPackage, Binder.getCallingUid(), callback, VoiceInteractionSession.this, prompt, extras);
                VoiceInteractionSession.this.addRequest(request);
                VoiceInteractionSession.this.mHandlerCaller.sendMessage(VoiceInteractionSession.this.mHandlerCaller.obtainMessageO(1, request));
                return request.mInterface;
            }

            @Override // com.android.internal.app.IVoiceInteractor
            public IVoiceInteractorRequest startPickOption(String callingPackage, IVoiceInteractorCallback callback, VoiceInteractor.Prompt prompt, VoiceInteractor.PickOptionRequest.Option[] options, Bundle extras) {
                PickOptionRequest request = new PickOptionRequest(callingPackage, Binder.getCallingUid(), callback, VoiceInteractionSession.this, prompt, options, extras);
                VoiceInteractionSession.this.addRequest(request);
                VoiceInteractionSession.this.mHandlerCaller.sendMessage(VoiceInteractionSession.this.mHandlerCaller.obtainMessageO(2, request));
                return request.mInterface;
            }

            @Override // com.android.internal.app.IVoiceInteractor
            public IVoiceInteractorRequest startCompleteVoice(String callingPackage, IVoiceInteractorCallback callback, VoiceInteractor.Prompt message, Bundle extras) {
                CompleteVoiceRequest request = new CompleteVoiceRequest(callingPackage, Binder.getCallingUid(), callback, VoiceInteractionSession.this, message, extras);
                VoiceInteractionSession.this.addRequest(request);
                VoiceInteractionSession.this.mHandlerCaller.sendMessage(VoiceInteractionSession.this.mHandlerCaller.obtainMessageO(3, request));
                return request.mInterface;
            }

            @Override // com.android.internal.app.IVoiceInteractor
            public IVoiceInteractorRequest startAbortVoice(String callingPackage, IVoiceInteractorCallback callback, VoiceInteractor.Prompt message, Bundle extras) {
                AbortVoiceRequest request = new AbortVoiceRequest(callingPackage, Binder.getCallingUid(), callback, VoiceInteractionSession.this, message, extras);
                VoiceInteractionSession.this.addRequest(request);
                VoiceInteractionSession.this.mHandlerCaller.sendMessage(VoiceInteractionSession.this.mHandlerCaller.obtainMessageO(4, request));
                return request.mInterface;
            }

            @Override // com.android.internal.app.IVoiceInteractor
            public IVoiceInteractorRequest startCommand(String callingPackage, IVoiceInteractorCallback callback, String command, Bundle extras) {
                CommandRequest request = new CommandRequest(callingPackage, Binder.getCallingUid(), callback, VoiceInteractionSession.this, command, extras);
                VoiceInteractionSession.this.addRequest(request);
                VoiceInteractionSession.this.mHandlerCaller.sendMessage(VoiceInteractionSession.this.mHandlerCaller.obtainMessageO(5, request));
                return request.mInterface;
            }

            @Override // com.android.internal.app.IVoiceInteractor
            public boolean[] supportsCommands(String callingPackage, String[] commands) {
                SomeArgs args = VoiceInteractionSession.this.mHandlerCaller.sendMessageAndWait(VoiceInteractionSession.this.mHandlerCaller.obtainMessageIOO(6, 0, commands, null));
                if (args == null) {
                    return new boolean[commands.length];
                }
                boolean[] res = (boolean[]) args.arg1;
                args.recycle();
                return res;
            }

            @Override // com.android.internal.app.IVoiceInteractor
            public void notifyDirectActionsChanged(int taskId, IBinder assistToken) {
                VoiceInteractionSession.this.mHandlerCaller.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$lR4OeV3qsxUCrL7Xl2vrhTvEo.INSTANCE, VoiceInteractionSession.this, new ActivityId(taskId, assistToken)));
            }

            @Override // com.android.internal.app.IVoiceInteractor
            public void setKillCallback(ICancellationSignal callback) {
                VoiceInteractionSession.this.mKillCallback = callback;
            }
        };
        this.mSession = new IVoiceInteractionSession.Stub() {
            /* class android.service.voice.VoiceInteractionSession.AnonymousClass2 */

            @Override // android.service.voice.IVoiceInteractionSession
            public void show(Bundle sessionArgs, int flags, IVoiceInteractionSessionShowCallback showCallback) {
                VoiceInteractionSession.this.mHandlerCaller.sendMessage(VoiceInteractionSession.this.mHandlerCaller.obtainMessageIOO(106, flags, sessionArgs, showCallback));
            }

            @Override // android.service.voice.IVoiceInteractionSession
            public void hide() {
                VoiceInteractionSession.this.mHandlerCaller.removeMessages(106);
                VoiceInteractionSession.this.mHandlerCaller.sendMessage(VoiceInteractionSession.this.mHandlerCaller.obtainMessage(107));
            }

            @Override // android.service.voice.IVoiceInteractionSession
            public void handleAssist(final int taskId, final IBinder assistToken, final Bundle data, final AssistStructure structure, final AssistContent content, final int index, final int count) {
                new Thread("AssistStructure retriever") {
                    /* class android.service.voice.VoiceInteractionSession.AnonymousClass2.AnonymousClass1 */

                    @Override // java.lang.Thread, java.lang.Runnable
                    public void run() {
                        Throwable failure = null;
                        AssistStructure assistStructure = structure;
                        if (assistStructure != null) {
                            try {
                                assistStructure.ensureData();
                            } catch (Throwable e) {
                                Log.w(VoiceInteractionSession.TAG, "Failure retrieving AssistStructure", e);
                                failure = e;
                            }
                        }
                        SomeArgs args = SomeArgs.obtain();
                        args.argi1 = taskId;
                        args.arg1 = data;
                        args.arg2 = failure == null ? structure : null;
                        args.arg3 = failure;
                        args.arg4 = content;
                        args.arg5 = assistToken;
                        args.argi5 = index;
                        args.argi6 = count;
                        VoiceInteractionSession.this.mHandlerCaller.sendMessage(VoiceInteractionSession.this.mHandlerCaller.obtainMessageO(104, args));
                    }
                }.start();
            }

            @Override // android.service.voice.IVoiceInteractionSession
            public void handleScreenshot(Bitmap screenshot) {
                VoiceInteractionSession.this.mHandlerCaller.sendMessage(VoiceInteractionSession.this.mHandlerCaller.obtainMessageO(105, screenshot));
            }

            @Override // android.service.voice.IVoiceInteractionSession
            public void taskStarted(Intent intent, int taskId) {
                VoiceInteractionSession.this.mHandlerCaller.sendMessage(VoiceInteractionSession.this.mHandlerCaller.obtainMessageIO(100, taskId, intent));
            }

            @Override // android.service.voice.IVoiceInteractionSession
            public void taskFinished(Intent intent, int taskId) {
                VoiceInteractionSession.this.mHandlerCaller.sendMessage(VoiceInteractionSession.this.mHandlerCaller.obtainMessageIO(101, taskId, intent));
            }

            @Override // android.service.voice.IVoiceInteractionSession
            public void closeSystemDialogs() {
                VoiceInteractionSession.this.mHandlerCaller.sendMessage(VoiceInteractionSession.this.mHandlerCaller.obtainMessage(102));
            }

            @Override // android.service.voice.IVoiceInteractionSession
            public void onLockscreenShown() {
                VoiceInteractionSession.this.mHandlerCaller.sendMessage(VoiceInteractionSession.this.mHandlerCaller.obtainMessage(108));
            }

            @Override // android.service.voice.IVoiceInteractionSession
            public void destroy() {
                VoiceInteractionSession.this.mHandlerCaller.sendMessage(VoiceInteractionSession.this.mHandlerCaller.obtainMessage(103));
            }
        };
        this.mCallbacks = new MyCallbacks();
        this.mInsetsComputer = new ViewTreeObserver.OnComputeInternalInsetsListener() {
            /* class android.service.voice.VoiceInteractionSession.AnonymousClass3 */

            @Override // android.view.ViewTreeObserver.OnComputeInternalInsetsListener
            public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo info) {
                VoiceInteractionSession voiceInteractionSession = VoiceInteractionSession.this;
                voiceInteractionSession.onComputeInsets(voiceInteractionSession.mTmpInsets);
                info.contentInsets.set(VoiceInteractionSession.this.mTmpInsets.contentInsets);
                info.visibleInsets.set(VoiceInteractionSession.this.mTmpInsets.contentInsets);
                info.touchableRegion.set(VoiceInteractionSession.this.mTmpInsets.touchableRegion);
                info.setTouchableInsets(VoiceInteractionSession.this.mTmpInsets.touchableInsets);
            }
        };
        this.mContext = context;
        this.mHandlerCaller = new HandlerCaller(context, handler.getLooper(), this.mCallbacks, true);
    }

    public Context getContext() {
        return this.mContext;
    }

    /* access modifiers changed from: package-private */
    public void addRequest(Request req) {
        synchronized (this) {
            this.mActiveRequests.put(req.mInterface.asBinder(), req);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isRequestActive(IBinder reqInterface) {
        boolean containsKey;
        synchronized (this) {
            containsKey = this.mActiveRequests.containsKey(reqInterface);
        }
        return containsKey;
    }

    /* access modifiers changed from: package-private */
    public Request removeRequest(IBinder reqInterface) {
        Request remove;
        synchronized (this) {
            remove = this.mActiveRequests.remove(reqInterface);
        }
        return remove;
    }

    /* access modifiers changed from: package-private */
    public void doCreate(IVoiceInteractionManagerService service, IBinder token) {
        this.mSystemService = service;
        this.mToken = token;
        onCreate();
    }

    /* access modifiers changed from: package-private */
    public void doShow(Bundle args, int flags, final IVoiceInteractionSessionShowCallback showCallback) {
        if (this.mInShowWindow) {
            Log.w(TAG, "Re-entrance in to showWindow");
            return;
        }
        try {
            this.mInShowWindow = true;
            onPrepareShow(args, flags);
            if (!this.mWindowVisible) {
                ensureWindowAdded();
            }
            onShow(args, flags);
            if (!this.mWindowVisible) {
                this.mWindowVisible = true;
                if (this.mUiEnabled) {
                    this.mWindow.show();
                }
            }
            if (showCallback != null) {
                if (this.mUiEnabled) {
                    this.mRootView.invalidate();
                    this.mRootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        /* class android.service.voice.VoiceInteractionSession.AnonymousClass4 */

                        @Override // android.view.ViewTreeObserver.OnPreDrawListener
                        public boolean onPreDraw() {
                            VoiceInteractionSession.this.mRootView.getViewTreeObserver().removeOnPreDrawListener(this);
                            try {
                                showCallback.onShown();
                                return true;
                            } catch (RemoteException e) {
                                Log.w(VoiceInteractionSession.TAG, "Error calling onShown", e);
                                return true;
                            }
                        }
                    });
                } else {
                    try {
                        showCallback.onShown();
                    } catch (RemoteException e) {
                        Log.w(TAG, "Error calling onShown", e);
                    }
                }
            }
        } finally {
            this.mWindowWasVisible = true;
            this.mInShowWindow = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void doHide() {
        if (this.mWindowVisible) {
            ensureWindowHidden();
            this.mWindowVisible = false;
            onHide();
        }
    }

    /* access modifiers changed from: package-private */
    public void doDestroy() {
        onDestroy();
        ICancellationSignal iCancellationSignal = this.mKillCallback;
        if (iCancellationSignal != null) {
            try {
                iCancellationSignal.cancel();
            } catch (RemoteException e) {
            }
            this.mKillCallback = null;
        }
        if (this.mInitialized) {
            this.mRootView.getViewTreeObserver().removeOnComputeInternalInsetsListener(this.mInsetsComputer);
            if (this.mWindowAdded) {
                this.mWindow.dismiss();
                this.mWindowAdded = false;
            }
            this.mInitialized = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void ensureWindowCreated() {
        if (!this.mInitialized) {
            if (this.mUiEnabled) {
                this.mInitialized = true;
                this.mInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                this.mWindow = new SoftInputWindow(this.mContext, TAG, this.mTheme, this.mCallbacks, this, this.mDispatcherState, 2031, 80, true);
                this.mWindow.getWindow().addFlags(16843008);
                this.mThemeAttrs = this.mContext.obtainStyledAttributes(R.styleable.VoiceInteractionSession);
                this.mRootView = this.mInflater.inflate(com.android.internal.R.layout.voice_interaction_session, (ViewGroup) null);
                this.mRootView.setSystemUiVisibility(1792);
                this.mWindow.setContentView(this.mRootView);
                this.mRootView.getViewTreeObserver().addOnComputeInternalInsetsListener(this.mInsetsComputer);
                this.mContentFrame = (FrameLayout) this.mRootView.findViewById(16908290);
                this.mWindow.getWindow().setLayout(-1, -1);
                this.mWindow.setToken(this.mToken);
                return;
            }
            throw new IllegalStateException("setUiEnabled is false");
        }
    }

    /* access modifiers changed from: package-private */
    public void ensureWindowAdded() {
        if (this.mUiEnabled && !this.mWindowAdded) {
            this.mWindowAdded = true;
            ensureWindowCreated();
            View v = onCreateContentView();
            if (v != null) {
                setContentView(v);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void ensureWindowHidden() {
        SoftInputWindow softInputWindow = this.mWindow;
        if (softInputWindow != null) {
            softInputWindow.hide();
        }
    }

    public void setDisabledShowContext(int flags) {
        try {
            this.mSystemService.setDisabledShowContext(flags);
        } catch (RemoteException e) {
        }
    }

    public int getDisabledShowContext() {
        try {
            return this.mSystemService.getDisabledShowContext();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public int getUserDisabledShowContext() {
        try {
            return this.mSystemService.getUserDisabledShowContext();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public void show(Bundle args, int flags) {
        IBinder iBinder = this.mToken;
        if (iBinder != null) {
            try {
                this.mSystemService.showSessionFromSession(iBinder, args, flags);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("Can't call before onCreate()");
        }
    }

    public void hide() {
        IBinder iBinder = this.mToken;
        if (iBinder != null) {
            try {
                this.mSystemService.hideSessionFromSession(iBinder);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("Can't call before onCreate()");
        }
    }

    public void setUiEnabled(boolean enabled) {
        if (this.mUiEnabled != enabled) {
            this.mUiEnabled = enabled;
            if (!this.mWindowVisible) {
                return;
            }
            if (enabled) {
                ensureWindowAdded();
                this.mWindow.show();
                return;
            }
            ensureWindowHidden();
        }
    }

    public void setTheme(int theme) {
        if (this.mWindow == null) {
            this.mTheme = theme;
            return;
        }
        throw new IllegalStateException("Must be called before onCreate()");
    }

    public void startVoiceActivity(Intent intent) {
        if (this.mToken != null) {
            try {
                intent.migrateExtraStreamToClipData();
                intent.prepareToLeaveProcess(this.mContext);
                Instrumentation.checkStartActivityResult(this.mSystemService.startVoiceActivity(this.mToken, intent, intent.resolveType(this.mContext.getContentResolver())), intent);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("Can't call before onCreate()");
        }
    }

    public void startAssistantActivity(Intent intent) {
        if (this.mToken != null) {
            try {
                intent.migrateExtraStreamToClipData();
                intent.prepareToLeaveProcess(this.mContext);
                Instrumentation.checkStartActivityResult(this.mSystemService.startAssistantActivity(this.mToken, intent, intent.resolveType(this.mContext.getContentResolver())), intent);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("Can't call before onCreate()");
        }
    }

    public final void requestDirectActions(ActivityId activityId, CancellationSignal cancellationSignal, Executor resultExecutor, Consumer<List<DirectAction>> callback) {
        RemoteCallback cancellationCallback;
        Preconditions.checkNotNull(activityId);
        Preconditions.checkNotNull(resultExecutor);
        Preconditions.checkNotNull(callback);
        if (this.mToken != null) {
            if (cancellationSignal != null) {
                cancellationSignal.throwIfCanceled();
            }
            if (cancellationSignal != null) {
                cancellationCallback = new RemoteCallback(new RemoteCallback.OnResultListener() {
                    /* class android.service.voice.$$Lambda$VoiceInteractionSession$KRmvXWbKzOj6uOiuAkDjhkUvQiw */

                    @Override // android.os.RemoteCallback.OnResultListener
                    public final void onResult(Bundle bundle) {
                        VoiceInteractionSession.lambda$requestDirectActions$0(CancellationSignal.this, bundle);
                    }
                });
            } else {
                cancellationCallback = null;
            }
            try {
                this.mSystemService.requestDirectActions(this.mToken, activityId.getTaskId(), activityId.getAssistToken(), cancellationCallback, new RemoteCallback(createSafeResultListener(new Consumer(resultExecutor, callback) {
                    /* class android.service.voice.$$Lambda$VoiceInteractionSession$ONdRuCsOqsJCBOvPdgOMEsz684 */
                    private final /* synthetic */ Executor f$0;
                    private final /* synthetic */ Consumer f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        VoiceInteractionSession.lambda$requestDirectActions$2(this.f$0, this.f$1, (Bundle) obj);
                    }
                })));
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalStateException("Can't call before onCreate()");
        }
    }

    static /* synthetic */ void lambda$requestDirectActions$0(CancellationSignal cancellationSignal, Bundle b) {
        IBinder cancellation;
        if (b != null && (cancellation = b.getBinder(VoiceInteractor.KEY_CANCELLATION_SIGNAL)) != null) {
            cancellationSignal.setRemote(ICancellationSignal.Stub.asInterface(cancellation));
        }
    }

    static /* synthetic */ void lambda$requestDirectActions$2(Executor resultExecutor, Consumer callback, Bundle result) {
        List<DirectAction> list;
        if (result == null) {
            list = Collections.emptyList();
        } else {
            ParceledListSlice<DirectAction> pls = (ParceledListSlice) result.getParcelable(DirectAction.KEY_ACTIONS_LIST);
            if (pls != null) {
                List<DirectAction> receivedList = pls.getList();
                list = receivedList != null ? receivedList : Collections.emptyList();
            } else {
                list = Collections.emptyList();
            }
        }
        resultExecutor.execute(new Runnable(callback, list) {
            /* class android.service.voice.$$Lambda$VoiceInteractionSession$fvrSEzYI3LvOpmfME5kNVi91bw */
            private final /* synthetic */ Consumer f$0;
            private final /* synthetic */ List f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.accept(this.f$1);
            }
        });
    }

    public void onDirectActionsInvalidated(ActivityId activityId) {
    }

    public final void performDirectAction(DirectAction action, Bundle extras, CancellationSignal cancellationSignal, Executor resultExecutor, Consumer<Bundle> resultListener) {
        RemoteCallback cancellationCallback;
        if (this.mToken != null) {
            Preconditions.checkNotNull(resultExecutor);
            Preconditions.checkNotNull(resultListener);
            if (cancellationSignal != null) {
                cancellationSignal.throwIfCanceled();
            }
            if (cancellationSignal != null) {
                cancellationCallback = new RemoteCallback(createSafeResultListener(new Consumer() {
                    /* class android.service.voice.$$Lambda$VoiceInteractionSession$2YI2merL0kdgL83g93OW541J8w */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        VoiceInteractionSession.lambda$performDirectAction$3(CancellationSignal.this, (Bundle) obj);
                    }
                }));
            } else {
                cancellationCallback = null;
            }
            try {
                this.mSystemService.performDirectAction(this.mToken, action.getId(), extras, action.getTaskId(), action.getActivityId(), cancellationCallback, new RemoteCallback(createSafeResultListener(new Consumer(resultExecutor, resultListener) {
                    /* class android.service.voice.$$Lambda$VoiceInteractionSession$9GV3ALC6LWOMyg5zazTo6TodsHU */
                    private final /* synthetic */ Executor f$0;
                    private final /* synthetic */ Consumer f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        VoiceInteractionSession.lambda$performDirectAction$6(this.f$0, this.f$1, (Bundle) obj);
                    }
                })));
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalStateException("Can't call before onCreate()");
        }
    }

    static /* synthetic */ void lambda$performDirectAction$3(CancellationSignal cancellationSignal, Bundle b) {
        IBinder cancellation;
        if (b != null && (cancellation = b.getBinder(VoiceInteractor.KEY_CANCELLATION_SIGNAL)) != null) {
            cancellationSignal.setRemote(ICancellationSignal.Stub.asInterface(cancellation));
        }
    }

    static /* synthetic */ void lambda$performDirectAction$6(Executor resultExecutor, Consumer resultListener, Bundle b) {
        if (b != null) {
            resultExecutor.execute(new Runnable(resultListener, b) {
                /* class android.service.voice.$$Lambda$VoiceInteractionSession$sg0qPgWHpOBD2lLJ7BGNEVSsBdo */
                private final /* synthetic */ Consumer f$0;
                private final /* synthetic */ Bundle f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.accept(this.f$1);
                }
            });
        } else {
            resultExecutor.execute(new Runnable(resultListener) {
                /* class android.service.voice.$$Lambda$VoiceInteractionSession$bujvs7MJfXO9xSx9M8NS3hINZ_k */
                private final /* synthetic */ Consumer f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.accept(Bundle.EMPTY);
                }
            });
        }
    }

    public void setKeepAwake(boolean keepAwake) {
        IBinder iBinder = this.mToken;
        if (iBinder != null) {
            try {
                this.mSystemService.setKeepAwake(iBinder, keepAwake);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("Can't call before onCreate()");
        }
    }

    public void closeSystemDialogs() {
        IBinder iBinder = this.mToken;
        if (iBinder != null) {
            try {
                this.mSystemService.closeSystemDialogs(iBinder);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("Can't call before onCreate()");
        }
    }

    public LayoutInflater getLayoutInflater() {
        ensureWindowCreated();
        return this.mInflater;
    }

    public Dialog getWindow() {
        ensureWindowCreated();
        return this.mWindow;
    }

    public void finish() {
        IBinder iBinder = this.mToken;
        if (iBinder != null) {
            try {
                this.mSystemService.finish(iBinder);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("Can't call before onCreate()");
        }
    }

    public void onCreate() {
        doOnCreate();
    }

    private void doOnCreate() {
        int i = this.mTheme;
        if (i == 0) {
            i = com.android.internal.R.style.Theme_DeviceDefault_VoiceInteractionSession;
        }
        this.mTheme = i;
    }

    public void onPrepareShow(Bundle args, int showFlags) {
    }

    public void onShow(Bundle args, int showFlags) {
    }

    public void onHide() {
    }

    public void onDestroy() {
    }

    public View onCreateContentView() {
        return null;
    }

    public void setContentView(View view) {
        ensureWindowCreated();
        this.mContentFrame.removeAllViews();
        this.mContentFrame.addView(view, new FrameLayout.LayoutParams(-1, -1));
        this.mContentFrame.requestApplyInsets();
    }

    /* access modifiers changed from: package-private */
    public void doOnHandleAssist(int taskId, IBinder assistToken, Bundle data, AssistStructure structure, Throwable failure, AssistContent content, int index, int count) {
        if (failure != null) {
            onAssistStructureFailure(failure);
        }
        onHandleAssist(new AssistState(new ActivityId(taskId, assistToken), data, structure, content, index, count));
    }

    public void onAssistStructureFailure(Throwable failure) {
    }

    @Deprecated
    public void onHandleAssist(Bundle data, AssistStructure structure, AssistContent content) {
    }

    public void onHandleAssist(AssistState state) {
        if (state.getIndex() == 0) {
            onHandleAssist(state.getAssistData(), state.getAssistStructure(), state.getAssistContent());
        } else {
            onHandleAssistSecondary(state.getAssistData(), state.getAssistStructure(), state.getAssistContent(), state.getIndex(), state.getCount());
        }
    }

    @Deprecated
    public void onHandleAssistSecondary(Bundle data, AssistStructure structure, AssistContent content, int index, int count) {
    }

    public void onHandleScreenshot(Bitmap screenshot) {
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        return false;
    }

    public void onBackPressed() {
        hide();
    }

    public void onCloseSystemDialogs() {
        hide();
    }

    public void onLockscreenShown() {
        hide();
    }

    @Override // android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
    }

    @Override // android.content.ComponentCallbacks
    public void onLowMemory() {
    }

    @Override // android.content.ComponentCallbacks2
    public void onTrimMemory(int level) {
    }

    public void onComputeInsets(Insets outInsets) {
        outInsets.contentInsets.left = 0;
        outInsets.contentInsets.bottom = 0;
        outInsets.contentInsets.right = 0;
        View decor = getWindow().getWindow().getDecorView();
        outInsets.contentInsets.top = decor.getHeight();
        outInsets.touchableInsets = 0;
        outInsets.touchableRegion.setEmpty();
    }

    public void onTaskStarted(Intent intent, int taskId) {
    }

    public void onTaskFinished(Intent intent, int taskId) {
        hide();
    }

    public boolean[] onGetSupportedCommands(String[] commands) {
        return new boolean[commands.length];
    }

    public void onRequestConfirmation(ConfirmationRequest request) {
    }

    public void onRequestPickOption(PickOptionRequest request) {
    }

    public void onRequestCompleteVoice(CompleteVoiceRequest request) {
    }

    public void onRequestAbortVoice(AbortVoiceRequest request) {
    }

    public void onRequestCommand(CommandRequest request) {
    }

    public void onCancelRequest(Request request) {
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.print(prefix);
        writer.print("mToken=");
        writer.println(this.mToken);
        writer.print(prefix);
        writer.print("mTheme=#");
        writer.println(Integer.toHexString(this.mTheme));
        writer.print(prefix);
        writer.print("mUiEnabled=");
        writer.println(this.mUiEnabled);
        writer.print(" mInitialized=");
        writer.println(this.mInitialized);
        writer.print(prefix);
        writer.print("mWindowAdded=");
        writer.print(this.mWindowAdded);
        writer.print(" mWindowVisible=");
        writer.println(this.mWindowVisible);
        writer.print(prefix);
        writer.print("mWindowWasVisible=");
        writer.print(this.mWindowWasVisible);
        writer.print(" mInShowWindow=");
        writer.println(this.mInShowWindow);
        if (this.mActiveRequests.size() > 0) {
            writer.print(prefix);
            writer.println("Active requests:");
            String innerPrefix = prefix + "    ";
            for (int i = 0; i < this.mActiveRequests.size(); i++) {
                Request req = this.mActiveRequests.valueAt(i);
                writer.print(prefix);
                writer.print("  #");
                writer.print(i);
                writer.print(": ");
                writer.println(req);
                req.dump(innerPrefix, fd, writer, args);
            }
        }
    }

    private SafeResultListener createSafeResultListener(Consumer<Bundle> consumer) {
        SafeResultListener listener;
        synchronized (this) {
            listener = new SafeResultListener(consumer, this);
            this.mRemoteCallbacks.put(listener, consumer);
        }
        return listener;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Consumer<Bundle> removeSafeResultListener(SafeResultListener listener) {
        Consumer<Bundle> remove;
        synchronized (this) {
            remove = this.mRemoteCallbacks.remove(listener);
        }
        return remove;
    }

    @Immutable
    public static final class AssistState {
        private final ActivityId mActivityId;
        private final AssistContent mContent;
        private final int mCount;
        private final Bundle mData;
        private final int mIndex;
        private final AssistStructure mStructure;

        AssistState(ActivityId activityId, Bundle data, AssistStructure structure, AssistContent content, int index, int count) {
            this.mActivityId = activityId;
            this.mIndex = index;
            this.mCount = count;
            this.mData = data;
            this.mStructure = structure;
            this.mContent = content;
        }

        public boolean isFocused() {
            return this.mIndex == 0;
        }

        public int getIndex() {
            return this.mIndex;
        }

        public int getCount() {
            return this.mCount;
        }

        public ActivityId getActivityId() {
            return this.mActivityId;
        }

        public Bundle getAssistData() {
            return this.mData;
        }

        public AssistStructure getAssistStructure() {
            return this.mStructure;
        }

        public AssistContent getAssistContent() {
            return this.mContent;
        }
    }

    public static class ActivityId {
        private final IBinder mAssistToken;
        private final int mTaskId;

        ActivityId(int taskId, IBinder assistToken) {
            this.mTaskId = taskId;
            this.mAssistToken = assistToken;
        }

        /* access modifiers changed from: package-private */
        public int getTaskId() {
            return this.mTaskId;
        }

        /* access modifiers changed from: package-private */
        public IBinder getAssistToken() {
            return this.mAssistToken;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ActivityId that = (ActivityId) o;
            if (this.mTaskId != that.mTaskId) {
                return false;
            }
            IBinder iBinder = this.mAssistToken;
            if (iBinder != null) {
                return iBinder.equals(that.mAssistToken);
            }
            if (that.mAssistToken == null) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            int i = this.mTaskId * 31;
            IBinder iBinder = this.mAssistToken;
            return i + (iBinder != null ? iBinder.hashCode() : 0);
        }
    }

    /* access modifiers changed from: private */
    public static class SafeResultListener implements RemoteCallback.OnResultListener {
        private final WeakReference<VoiceInteractionSession> mWeakSession;

        SafeResultListener(Consumer<Bundle> consumer, VoiceInteractionSession session) {
            this.mWeakSession = new WeakReference<>(session);
        }

        @Override // android.os.RemoteCallback.OnResultListener
        public void onResult(Bundle result) {
            Consumer<Bundle> consumer;
            VoiceInteractionSession session = this.mWeakSession.get();
            if (session != null && (consumer = session.removeSafeResultListener(this)) != null) {
                consumer.accept(result);
            }
        }
    }
}
