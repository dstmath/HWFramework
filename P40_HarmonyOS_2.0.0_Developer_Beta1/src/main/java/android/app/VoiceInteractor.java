package android.app;

import android.content.Context;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.SettingsStringUtil;
import android.util.ArrayMap;
import android.util.DebugUtils;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.concurrent.Executor;

public final class VoiceInteractor {
    static final boolean DEBUG = false;
    public static final String KEY_CANCELLATION_SIGNAL = "key_cancellation_signal";
    public static final String KEY_KILL_SIGNAL = "key_kill_signal";
    static final int MSG_ABORT_VOICE_RESULT = 4;
    static final int MSG_CANCEL_RESULT = 6;
    static final int MSG_COMMAND_RESULT = 5;
    static final int MSG_COMPLETE_VOICE_RESULT = 3;
    static final int MSG_CONFIRMATION_RESULT = 1;
    static final int MSG_PICK_OPTION_RESULT = 2;
    static final Request[] NO_REQUESTS = new Request[0];
    static final String TAG = "VoiceInteractor";
    final ArrayMap<IBinder, Request> mActiveRequests = new ArrayMap<>();
    Activity mActivity;
    final IVoiceInteractorCallback.Stub mCallback = new IVoiceInteractorCallback.Stub() {
        /* class android.app.VoiceInteractor.AnonymousClass2 */

        @Override // com.android.internal.app.IVoiceInteractorCallback
        public void deliverConfirmationResult(IVoiceInteractorRequest request, boolean finished, Bundle result) {
            VoiceInteractor.this.mHandlerCaller.sendMessage(VoiceInteractor.this.mHandlerCaller.obtainMessageIOO(1, finished ? 1 : 0, request, result));
        }

        @Override // com.android.internal.app.IVoiceInteractorCallback
        public void deliverPickOptionResult(IVoiceInteractorRequest request, boolean finished, PickOptionRequest.Option[] options, Bundle result) {
            VoiceInteractor.this.mHandlerCaller.sendMessage(VoiceInteractor.this.mHandlerCaller.obtainMessageIOOO(2, finished ? 1 : 0, request, options, result));
        }

        @Override // com.android.internal.app.IVoiceInteractorCallback
        public void deliverCompleteVoiceResult(IVoiceInteractorRequest request, Bundle result) {
            VoiceInteractor.this.mHandlerCaller.sendMessage(VoiceInteractor.this.mHandlerCaller.obtainMessageOO(3, request, result));
        }

        @Override // com.android.internal.app.IVoiceInteractorCallback
        public void deliverAbortVoiceResult(IVoiceInteractorRequest request, Bundle result) {
            VoiceInteractor.this.mHandlerCaller.sendMessage(VoiceInteractor.this.mHandlerCaller.obtainMessageOO(4, request, result));
        }

        @Override // com.android.internal.app.IVoiceInteractorCallback
        public void deliverCommandResult(IVoiceInteractorRequest request, boolean complete, Bundle result) {
            VoiceInteractor.this.mHandlerCaller.sendMessage(VoiceInteractor.this.mHandlerCaller.obtainMessageIOO(5, complete ? 1 : 0, request, result));
        }

        @Override // com.android.internal.app.IVoiceInteractorCallback
        public void deliverCancel(IVoiceInteractorRequest request) {
            VoiceInteractor.this.mHandlerCaller.sendMessage(VoiceInteractor.this.mHandlerCaller.obtainMessageOO(6, request, null));
        }

        @Override // com.android.internal.app.IVoiceInteractorCallback
        public void destroy() {
            VoiceInteractor.this.mHandlerCaller.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$dUWXWbBHcaaVBn031EDBP91NR7k.INSTANCE, VoiceInteractor.this));
        }
    };
    Context mContext;
    final HandlerCaller mHandlerCaller;
    final HandlerCaller.Callback mHandlerCallerCallback = new HandlerCaller.Callback() {
        /* class android.app.VoiceInteractor.AnonymousClass1 */

        @Override // com.android.internal.os.HandlerCaller.Callback
        public void executeMessage(Message msg) {
            SomeArgs args = (SomeArgs) msg.obj;
            boolean complete = false;
            switch (msg.what) {
                case 1:
                    Request request = VoiceInteractor.this.pullRequest((IVoiceInteractorRequest) args.arg1, true);
                    if (request != null) {
                        ConfirmationRequest confirmationRequest = (ConfirmationRequest) request;
                        if (msg.arg1 != 0) {
                            complete = true;
                        }
                        confirmationRequest.onConfirmationResult(complete, (Bundle) args.arg2);
                        request.clear();
                        return;
                    }
                    return;
                case 2:
                    if (msg.arg1 != 0) {
                        complete = true;
                    }
                    Request request2 = VoiceInteractor.this.pullRequest((IVoiceInteractorRequest) args.arg1, complete);
                    if (request2 != null) {
                        ((PickOptionRequest) request2).onPickOptionResult(complete, (PickOptionRequest.Option[]) args.arg2, (Bundle) args.arg3);
                        if (complete) {
                            request2.clear();
                            return;
                        }
                        return;
                    }
                    return;
                case 3:
                    Request request3 = VoiceInteractor.this.pullRequest((IVoiceInteractorRequest) args.arg1, true);
                    if (request3 != null) {
                        ((CompleteVoiceRequest) request3).onCompleteResult((Bundle) args.arg2);
                        request3.clear();
                        return;
                    }
                    return;
                case 4:
                    Request request4 = VoiceInteractor.this.pullRequest((IVoiceInteractorRequest) args.arg1, true);
                    if (request4 != null) {
                        ((AbortVoiceRequest) request4).onAbortResult((Bundle) args.arg2);
                        request4.clear();
                        return;
                    }
                    return;
                case 5:
                    boolean complete2 = msg.arg1 != 0;
                    Request request5 = VoiceInteractor.this.pullRequest((IVoiceInteractorRequest) args.arg1, complete2);
                    if (request5 != null) {
                        CommandRequest commandRequest = (CommandRequest) request5;
                        if (msg.arg1 != 0) {
                            complete = true;
                        }
                        commandRequest.onCommandResult(complete, (Bundle) args.arg2);
                        if (complete2) {
                            request5.clear();
                            return;
                        }
                        return;
                    }
                    return;
                case 6:
                    Request request6 = VoiceInteractor.this.pullRequest((IVoiceInteractorRequest) args.arg1, true);
                    if (request6 != null) {
                        request6.onCancel();
                        request6.clear();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    IVoiceInteractor mInteractor;
    final ArrayMap<Runnable, Executor> mOnDestroyCallbacks = new ArrayMap<>();
    boolean mRetaining;

    public static abstract class Request {
        Activity mActivity;
        Context mContext;
        String mName;
        IVoiceInteractorRequest mRequestInterface;

        /* access modifiers changed from: package-private */
        public abstract IVoiceInteractorRequest submit(IVoiceInteractor iVoiceInteractor, String str, IVoiceInteractorCallback iVoiceInteractorCallback) throws RemoteException;

        Request() {
        }

        public String getName() {
            return this.mName;
        }

        public void cancel() {
            IVoiceInteractorRequest iVoiceInteractorRequest = this.mRequestInterface;
            if (iVoiceInteractorRequest != null) {
                try {
                    iVoiceInteractorRequest.cancel();
                } catch (RemoteException e) {
                    Log.w(VoiceInteractor.TAG, "Voice interactor has died", e);
                }
            } else {
                throw new IllegalStateException("Request " + this + " is no longer active");
            }
        }

        public Context getContext() {
            return this.mContext;
        }

        public Activity getActivity() {
            return this.mActivity;
        }

        public void onCancel() {
        }

        public void onAttached(Activity activity) {
        }

        public void onDetached() {
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            DebugUtils.buildShortClassTag(this, sb);
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(getRequestTypeName());
            sb.append(" name=");
            sb.append(this.mName);
            sb.append('}');
            return sb.toString();
        }

        /* access modifiers changed from: package-private */
        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            writer.print(prefix);
            writer.print("mRequestInterface=");
            writer.println(this.mRequestInterface.asBinder());
            writer.print(prefix);
            writer.print("mActivity=");
            writer.println(this.mActivity);
            writer.print(prefix);
            writer.print("mName=");
            writer.println(this.mName);
        }

        /* access modifiers changed from: package-private */
        public String getRequestTypeName() {
            return "Request";
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            this.mRequestInterface = null;
            this.mContext = null;
            this.mActivity = null;
            this.mName = null;
        }
    }

    public static class ConfirmationRequest extends Request {
        final Bundle mExtras;
        final Prompt mPrompt;

        public ConfirmationRequest(Prompt prompt, Bundle extras) {
            this.mPrompt = prompt;
            this.mExtras = extras;
        }

        public ConfirmationRequest(CharSequence prompt, Bundle extras) {
            this.mPrompt = prompt != null ? new Prompt(prompt) : null;
            this.mExtras = extras;
        }

        public void onConfirmationResult(boolean confirmed, Bundle result) {
        }

        /* access modifiers changed from: package-private */
        @Override // android.app.VoiceInteractor.Request
        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            super.dump(prefix, fd, writer, args);
            writer.print(prefix);
            writer.print("mPrompt=");
            writer.println(this.mPrompt);
            if (this.mExtras != null) {
                writer.print(prefix);
                writer.print("mExtras=");
                writer.println(this.mExtras);
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.app.VoiceInteractor.Request
        public String getRequestTypeName() {
            return "Confirmation";
        }

        /* access modifiers changed from: package-private */
        @Override // android.app.VoiceInteractor.Request
        public IVoiceInteractorRequest submit(IVoiceInteractor interactor, String packageName, IVoiceInteractorCallback callback) throws RemoteException {
            return interactor.startConfirmation(packageName, callback, this.mPrompt, this.mExtras);
        }
    }

    public static class PickOptionRequest extends Request {
        final Bundle mExtras;
        final Option[] mOptions;
        final Prompt mPrompt;

        public static final class Option implements Parcelable {
            public static final Parcelable.Creator<Option> CREATOR = new Parcelable.Creator<Option>() {
                /* class android.app.VoiceInteractor.PickOptionRequest.Option.AnonymousClass1 */

                @Override // android.os.Parcelable.Creator
                public Option createFromParcel(Parcel in) {
                    return new Option(in);
                }

                @Override // android.os.Parcelable.Creator
                public Option[] newArray(int size) {
                    return new Option[size];
                }
            };
            Bundle mExtras;
            final int mIndex;
            final CharSequence mLabel;
            ArrayList<CharSequence> mSynonyms;

            public Option(CharSequence label) {
                this.mLabel = label;
                this.mIndex = -1;
            }

            public Option(CharSequence label, int index) {
                this.mLabel = label;
                this.mIndex = index;
            }

            public Option addSynonym(CharSequence synonym) {
                if (this.mSynonyms == null) {
                    this.mSynonyms = new ArrayList<>();
                }
                this.mSynonyms.add(synonym);
                return this;
            }

            public CharSequence getLabel() {
                return this.mLabel;
            }

            public int getIndex() {
                return this.mIndex;
            }

            public int countSynonyms() {
                ArrayList<CharSequence> arrayList = this.mSynonyms;
                if (arrayList != null) {
                    return arrayList.size();
                }
                return 0;
            }

            public CharSequence getSynonymAt(int index) {
                ArrayList<CharSequence> arrayList = this.mSynonyms;
                if (arrayList != null) {
                    return arrayList.get(index);
                }
                return null;
            }

            public void setExtras(Bundle extras) {
                this.mExtras = extras;
            }

            public Bundle getExtras() {
                return this.mExtras;
            }

            Option(Parcel in) {
                this.mLabel = in.readCharSequence();
                this.mIndex = in.readInt();
                this.mSynonyms = in.readCharSequenceList();
                this.mExtras = in.readBundle();
            }

            @Override // android.os.Parcelable
            public int describeContents() {
                return 0;
            }

            @Override // android.os.Parcelable
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeCharSequence(this.mLabel);
                dest.writeInt(this.mIndex);
                dest.writeCharSequenceList(this.mSynonyms);
                dest.writeBundle(this.mExtras);
            }
        }

        public PickOptionRequest(Prompt prompt, Option[] options, Bundle extras) {
            this.mPrompt = prompt;
            this.mOptions = options;
            this.mExtras = extras;
        }

        public PickOptionRequest(CharSequence prompt, Option[] options, Bundle extras) {
            this.mPrompt = prompt != null ? new Prompt(prompt) : null;
            this.mOptions = options;
            this.mExtras = extras;
        }

        public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
        }

        /* access modifiers changed from: package-private */
        @Override // android.app.VoiceInteractor.Request
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
                    Option[] optionArr = this.mOptions;
                    if (i >= optionArr.length) {
                        break;
                    }
                    Option op = optionArr[i];
                    writer.print(prefix);
                    writer.print("  #");
                    writer.print(i);
                    writer.println(SettingsStringUtil.DELIMITER);
                    writer.print(prefix);
                    writer.print("    mLabel=");
                    writer.println(op.mLabel);
                    writer.print(prefix);
                    writer.print("    mIndex=");
                    writer.println(op.mIndex);
                    if (op.mSynonyms != null && op.mSynonyms.size() > 0) {
                        writer.print(prefix);
                        writer.println("    Synonyms:");
                        for (int j = 0; j < op.mSynonyms.size(); j++) {
                            writer.print(prefix);
                            writer.print("      #");
                            writer.print(j);
                            writer.print(": ");
                            writer.println(op.mSynonyms.get(j));
                        }
                    }
                    if (op.mExtras != null) {
                        writer.print(prefix);
                        writer.print("    mExtras=");
                        writer.println(op.mExtras);
                    }
                    i++;
                }
            }
            if (this.mExtras != null) {
                writer.print(prefix);
                writer.print("mExtras=");
                writer.println(this.mExtras);
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.app.VoiceInteractor.Request
        public String getRequestTypeName() {
            return "PickOption";
        }

        /* access modifiers changed from: package-private */
        @Override // android.app.VoiceInteractor.Request
        public IVoiceInteractorRequest submit(IVoiceInteractor interactor, String packageName, IVoiceInteractorCallback callback) throws RemoteException {
            return interactor.startPickOption(packageName, callback, this.mPrompt, this.mOptions, this.mExtras);
        }
    }

    public static class CompleteVoiceRequest extends Request {
        final Bundle mExtras;
        final Prompt mPrompt;

        public CompleteVoiceRequest(Prompt prompt, Bundle extras) {
            this.mPrompt = prompt;
            this.mExtras = extras;
        }

        public CompleteVoiceRequest(CharSequence message, Bundle extras) {
            this.mPrompt = message != null ? new Prompt(message) : null;
            this.mExtras = extras;
        }

        public void onCompleteResult(Bundle result) {
        }

        /* access modifiers changed from: package-private */
        @Override // android.app.VoiceInteractor.Request
        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            super.dump(prefix, fd, writer, args);
            writer.print(prefix);
            writer.print("mPrompt=");
            writer.println(this.mPrompt);
            if (this.mExtras != null) {
                writer.print(prefix);
                writer.print("mExtras=");
                writer.println(this.mExtras);
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.app.VoiceInteractor.Request
        public String getRequestTypeName() {
            return "CompleteVoice";
        }

        /* access modifiers changed from: package-private */
        @Override // android.app.VoiceInteractor.Request
        public IVoiceInteractorRequest submit(IVoiceInteractor interactor, String packageName, IVoiceInteractorCallback callback) throws RemoteException {
            return interactor.startCompleteVoice(packageName, callback, this.mPrompt, this.mExtras);
        }
    }

    public static class AbortVoiceRequest extends Request {
        final Bundle mExtras;
        final Prompt mPrompt;

        public AbortVoiceRequest(Prompt prompt, Bundle extras) {
            this.mPrompt = prompt;
            this.mExtras = extras;
        }

        public AbortVoiceRequest(CharSequence message, Bundle extras) {
            this.mPrompt = message != null ? new Prompt(message) : null;
            this.mExtras = extras;
        }

        public void onAbortResult(Bundle result) {
        }

        /* access modifiers changed from: package-private */
        @Override // android.app.VoiceInteractor.Request
        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            super.dump(prefix, fd, writer, args);
            writer.print(prefix);
            writer.print("mPrompt=");
            writer.println(this.mPrompt);
            if (this.mExtras != null) {
                writer.print(prefix);
                writer.print("mExtras=");
                writer.println(this.mExtras);
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.app.VoiceInteractor.Request
        public String getRequestTypeName() {
            return "AbortVoice";
        }

        /* access modifiers changed from: package-private */
        @Override // android.app.VoiceInteractor.Request
        public IVoiceInteractorRequest submit(IVoiceInteractor interactor, String packageName, IVoiceInteractorCallback callback) throws RemoteException {
            return interactor.startAbortVoice(packageName, callback, this.mPrompt, this.mExtras);
        }
    }

    public static class CommandRequest extends Request {
        final Bundle mArgs;
        final String mCommand;

        public CommandRequest(String command, Bundle args) {
            this.mCommand = command;
            this.mArgs = args;
        }

        public void onCommandResult(boolean isCompleted, Bundle result) {
        }

        /* access modifiers changed from: package-private */
        @Override // android.app.VoiceInteractor.Request
        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            super.dump(prefix, fd, writer, args);
            writer.print(prefix);
            writer.print("mCommand=");
            writer.println(this.mCommand);
            if (this.mArgs != null) {
                writer.print(prefix);
                writer.print("mArgs=");
                writer.println(this.mArgs);
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.app.VoiceInteractor.Request
        public String getRequestTypeName() {
            return "Command";
        }

        /* access modifiers changed from: package-private */
        @Override // android.app.VoiceInteractor.Request
        public IVoiceInteractorRequest submit(IVoiceInteractor interactor, String packageName, IVoiceInteractorCallback callback) throws RemoteException {
            return interactor.startCommand(packageName, callback, this.mCommand, this.mArgs);
        }
    }

    public static class Prompt implements Parcelable {
        public static final Parcelable.Creator<Prompt> CREATOR = new Parcelable.Creator<Prompt>() {
            /* class android.app.VoiceInteractor.Prompt.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Prompt createFromParcel(Parcel in) {
                return new Prompt(in);
            }

            @Override // android.os.Parcelable.Creator
            public Prompt[] newArray(int size) {
                return new Prompt[size];
            }
        };
        private final CharSequence mVisualPrompt;
        private final CharSequence[] mVoicePrompts;

        public Prompt(CharSequence[] voicePrompts, CharSequence visualPrompt) {
            if (voicePrompts == null) {
                throw new NullPointerException("voicePrompts must not be null");
            } else if (voicePrompts.length == 0) {
                throw new IllegalArgumentException("voicePrompts must not be empty");
            } else if (visualPrompt != null) {
                this.mVoicePrompts = voicePrompts;
                this.mVisualPrompt = visualPrompt;
            } else {
                throw new NullPointerException("visualPrompt must not be null");
            }
        }

        public Prompt(CharSequence prompt) {
            this.mVoicePrompts = new CharSequence[]{prompt};
            this.mVisualPrompt = prompt;
        }

        public CharSequence getVoicePromptAt(int index) {
            return this.mVoicePrompts[index];
        }

        public int countVoicePrompts() {
            return this.mVoicePrompts.length;
        }

        public CharSequence getVisualPrompt() {
            return this.mVisualPrompt;
        }

        public String toString() {
            CharSequence[] charSequenceArr;
            StringBuilder sb = new StringBuilder(128);
            DebugUtils.buildShortClassTag(this, sb);
            CharSequence charSequence = this.mVisualPrompt;
            if (charSequence == null || (charSequenceArr = this.mVoicePrompts) == null || charSequenceArr.length != 1 || !charSequence.equals(charSequenceArr[0])) {
                if (this.mVisualPrompt != null) {
                    sb.append(" visual=");
                    sb.append(this.mVisualPrompt);
                }
                if (this.mVoicePrompts != null) {
                    sb.append(", voice=");
                    for (int i = 0; i < this.mVoicePrompts.length; i++) {
                        if (i > 0) {
                            sb.append(" | ");
                        }
                        sb.append(this.mVoicePrompts[i]);
                    }
                }
            } else {
                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                sb.append(this.mVisualPrompt);
            }
            sb.append('}');
            return sb.toString();
        }

        Prompt(Parcel in) {
            this.mVoicePrompts = in.readCharSequenceArray();
            this.mVisualPrompt = in.readCharSequence();
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeCharSequenceArray(this.mVoicePrompts);
            dest.writeCharSequence(this.mVisualPrompt);
        }
    }

    VoiceInteractor(IVoiceInteractor interactor, Context context, Activity activity, Looper looper) {
        this.mInteractor = interactor;
        this.mContext = context;
        this.mActivity = activity;
        this.mHandlerCaller = new HandlerCaller(context, looper, this.mHandlerCallerCallback, true);
        try {
            this.mInteractor.setKillCallback(new KillCallback(this));
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public Request pullRequest(IVoiceInteractorRequest request, boolean complete) {
        Request req;
        synchronized (this.mActiveRequests) {
            req = this.mActiveRequests.get(request.asBinder());
            if (req != null && complete) {
                this.mActiveRequests.remove(request.asBinder());
            }
        }
        return req;
    }

    private ArrayList<Request> makeRequestList() {
        int N = this.mActiveRequests.size();
        if (N < 1) {
            return null;
        }
        ArrayList<Request> list = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            list.add(this.mActiveRequests.valueAt(i));
        }
        return list;
    }

    /* access modifiers changed from: package-private */
    public void attachActivity(Activity activity) {
        this.mRetaining = false;
        if (this.mActivity != activity) {
            this.mContext = activity;
            this.mActivity = activity;
            ArrayList<Request> reqs = makeRequestList();
            if (reqs != null) {
                for (int i = 0; i < reqs.size(); i++) {
                    Request req = reqs.get(i);
                    req.mContext = activity;
                    req.mActivity = activity;
                    req.onAttached(activity);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void retainInstance() {
        this.mRetaining = true;
    }

    /* access modifiers changed from: package-private */
    public void detachActivity() {
        ArrayList<Request> reqs = makeRequestList();
        if (reqs != null) {
            for (int i = 0; i < reqs.size(); i++) {
                Request req = reqs.get(i);
                req.onDetached();
                req.mActivity = null;
                req.mContext = null;
            }
        }
        if (!this.mRetaining) {
            ArrayList<Request> reqs2 = makeRequestList();
            if (reqs2 != null) {
                for (int i2 = 0; i2 < reqs2.size(); i2++) {
                    reqs2.get(i2).cancel();
                }
            }
            this.mActiveRequests.clear();
        }
        this.mContext = null;
        this.mActivity = null;
    }

    /* access modifiers changed from: package-private */
    public void destroy() {
        for (int i = this.mActiveRequests.size() - 1; i >= 0; i--) {
            this.mActiveRequests.removeAt(i);
            this.mActiveRequests.valueAt(i).cancel();
        }
        for (int i2 = this.mOnDestroyCallbacks.size() - 1; i2 >= 0; i2--) {
            this.mOnDestroyCallbacks.valueAt(i2).execute(this.mOnDestroyCallbacks.keyAt(i2));
            this.mOnDestroyCallbacks.removeAt(i2);
        }
        this.mInteractor = null;
        Activity activity = this.mActivity;
        if (activity != null) {
            activity.setVoiceInteractor(null);
        }
    }

    public boolean submitRequest(Request request) {
        return submitRequest(request, null);
    }

    public boolean submitRequest(Request request, String name) {
        if (isDestroyed()) {
            Log.w(TAG, "Cannot interact with a destroyed voice interactor");
            return false;
        }
        try {
            if (request.mRequestInterface == null) {
                IVoiceInteractorRequest ireq = request.submit(this.mInteractor, this.mContext.getOpPackageName(), this.mCallback);
                request.mRequestInterface = ireq;
                request.mContext = this.mContext;
                request.mActivity = this.mActivity;
                request.mName = name;
                synchronized (this.mActiveRequests) {
                    this.mActiveRequests.put(ireq.asBinder(), request);
                }
                return true;
            }
            throw new IllegalStateException("Given " + request + " is already active");
        } catch (RemoteException e) {
            Log.w(TAG, "Remove voice interactor service died", e);
            return false;
        }
    }

    public Request[] getActiveRequests() {
        if (isDestroyed()) {
            Log.w(TAG, "Cannot interact with a destroyed voice interactor");
            return null;
        }
        synchronized (this.mActiveRequests) {
            int N = this.mActiveRequests.size();
            if (N <= 0) {
                return NO_REQUESTS;
            }
            Request[] requests = new Request[N];
            for (int i = 0; i < N; i++) {
                requests[i] = this.mActiveRequests.valueAt(i);
            }
            return requests;
        }
    }

    public Request getActiveRequest(String name) {
        if (isDestroyed()) {
            Log.w(TAG, "Cannot interact with a destroyed voice interactor");
            return null;
        }
        synchronized (this.mActiveRequests) {
            int N = this.mActiveRequests.size();
            for (int i = 0; i < N; i++) {
                Request req = this.mActiveRequests.valueAt(i);
                if (name != req.getName()) {
                    if (name == null || !name.equals(req.getName())) {
                    }
                }
                return req;
            }
            return null;
        }
    }

    public boolean[] supportsCommands(String[] commands) {
        if (isDestroyed()) {
            Log.w(TAG, "Cannot interact with a destroyed voice interactor");
            return new boolean[commands.length];
        }
        try {
            return this.mInteractor.supportsCommands(this.mContext.getOpPackageName(), commands);
        } catch (RemoteException e) {
            throw new RuntimeException("Voice interactor has died", e);
        }
    }

    public boolean isDestroyed() {
        return this.mInteractor == null;
    }

    public boolean registerOnDestroyedCallback(Executor executor, Runnable callback) {
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(callback);
        if (isDestroyed()) {
            Log.w(TAG, "Cannot interact with a destroyed voice interactor");
            return false;
        }
        this.mOnDestroyCallbacks.put(callback, executor);
        return true;
    }

    public boolean unregisterOnDestroyedCallback(Runnable callback) {
        Preconditions.checkNotNull(callback);
        if (isDestroyed()) {
            Log.w(TAG, "Cannot interact with a destroyed voice interactor");
            return false;
        } else if (this.mOnDestroyCallbacks.remove(callback) != null) {
            return true;
        } else {
            return false;
        }
    }

    public void notifyDirectActionsChanged() {
        if (isDestroyed()) {
            Log.w(TAG, "Cannot interact with a destroyed voice interactor");
            return;
        }
        try {
            this.mInteractor.notifyDirectActionsChanged(this.mActivity.getTaskId(), this.mActivity.getAssistToken());
        } catch (RemoteException e) {
            Log.w(TAG, "Voice interactor has died", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        String innerPrefix = prefix + "    ";
        if (this.mActiveRequests.size() > 0) {
            writer.print(prefix);
            writer.println("Active voice requests:");
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
        writer.print(prefix);
        writer.println("VoiceInteractor misc state:");
        writer.print(prefix);
        writer.print("  mInteractor=");
        writer.println(this.mInteractor.asBinder());
        writer.print(prefix);
        writer.print("  mActivity=");
        writer.println(this.mActivity);
    }

    private static final class KillCallback extends ICancellationSignal.Stub {
        private final WeakReference<VoiceInteractor> mInteractor;

        KillCallback(VoiceInteractor interactor) {
            this.mInteractor = new WeakReference<>(interactor);
        }

        @Override // android.os.ICancellationSignal
        public void cancel() {
            VoiceInteractor voiceInteractor = this.mInteractor.get();
            if (voiceInteractor != null) {
                voiceInteractor.mHandlerCaller.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$dUWXWbBHcaaVBn031EDBP91NR7k.INSTANCE, voiceInteractor));
            }
        }
    }
}
