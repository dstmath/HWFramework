package android.app;

import android.content.Context;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.security.keymaster.KeymasterDefs;
import android.util.ArrayMap;
import android.util.DebugUtils;
import android.util.Log;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.app.IVoiceInteractorCallback;
import com.android.internal.app.IVoiceInteractorCallback.Stub;
import com.android.internal.app.IVoiceInteractorRequest;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.HandlerCaller.Callback;
import com.android.internal.os.SomeArgs;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public final class VoiceInteractor {
    static final boolean DEBUG = false;
    static final int MSG_ABORT_VOICE_RESULT = 4;
    static final int MSG_CANCEL_RESULT = 6;
    static final int MSG_COMMAND_RESULT = 5;
    static final int MSG_COMPLETE_VOICE_RESULT = 3;
    static final int MSG_CONFIRMATION_RESULT = 1;
    static final int MSG_PICK_OPTION_RESULT = 2;
    static final Request[] NO_REQUESTS = null;
    static final String TAG = "VoiceInteractor";
    final ArrayMap<IBinder, Request> mActiveRequests;
    Activity mActivity;
    final Stub mCallback;
    Context mContext;
    final HandlerCaller mHandlerCaller;
    final Callback mHandlerCallerCallback;
    final IVoiceInteractor mInteractor;
    boolean mRetaining;

    public static abstract class Request {
        Activity mActivity;
        Context mContext;
        String mName;
        IVoiceInteractorRequest mRequestInterface;

        abstract IVoiceInteractorRequest submit(IVoiceInteractor iVoiceInteractor, String str, IVoiceInteractorCallback iVoiceInteractorCallback) throws RemoteException;

        Request() {
        }

        public String getName() {
            return this.mName;
        }

        public void cancel() {
            if (this.mRequestInterface == null) {
                throw new IllegalStateException("Request " + this + " is no longer active");
            }
            try {
                this.mRequestInterface.cancel();
            } catch (RemoteException e) {
                Log.w(VoiceInteractor.TAG, "Voice interactor has died", e);
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
            StringBuilder sb = new StringBuilder(KeymasterDefs.KM_ALGORITHM_HMAC);
            DebugUtils.buildShortClassTag(this, sb);
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(getRequestTypeName());
            sb.append(" name=");
            sb.append(this.mName);
            sb.append('}');
            return sb.toString();
        }

        void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
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

        String getRequestTypeName() {
            return "Request";
        }

        void clear() {
            this.mRequestInterface = null;
            this.mContext = null;
            this.mActivity = null;
            this.mName = null;
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
            Prompt prompt = null;
            if (message != null) {
                prompt = new Prompt(message);
            }
            this.mPrompt = prompt;
            this.mExtras = extras;
        }

        public void onAbortResult(Bundle result) {
        }

        void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
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

        String getRequestTypeName() {
            return "AbortVoice";
        }

        IVoiceInteractorRequest submit(IVoiceInteractor interactor, String packageName, IVoiceInteractorCallback callback) throws RemoteException {
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

        void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
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

        String getRequestTypeName() {
            return "Command";
        }

        IVoiceInteractorRequest submit(IVoiceInteractor interactor, String packageName, IVoiceInteractorCallback callback) throws RemoteException {
            return interactor.startCommand(packageName, callback, this.mCommand, this.mArgs);
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
            Prompt prompt = null;
            if (message != null) {
                prompt = new Prompt(message);
            }
            this.mPrompt = prompt;
            this.mExtras = extras;
        }

        public void onCompleteResult(Bundle result) {
        }

        void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
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

        String getRequestTypeName() {
            return "CompleteVoice";
        }

        IVoiceInteractorRequest submit(IVoiceInteractor interactor, String packageName, IVoiceInteractorCallback callback) throws RemoteException {
            return interactor.startCompleteVoice(packageName, callback, this.mPrompt, this.mExtras);
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
            Prompt prompt2 = null;
            if (prompt != null) {
                prompt2 = new Prompt(prompt);
            }
            this.mPrompt = prompt2;
            this.mExtras = extras;
        }

        public void onConfirmationResult(boolean confirmed, Bundle result) {
        }

        void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
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

        String getRequestTypeName() {
            return "Confirmation";
        }

        IVoiceInteractorRequest submit(IVoiceInteractor interactor, String packageName, IVoiceInteractorCallback callback) throws RemoteException {
            return interactor.startConfirmation(packageName, callback, this.mPrompt, this.mExtras);
        }
    }

    public static class PickOptionRequest extends Request {
        final Bundle mExtras;
        final Option[] mOptions;
        final Prompt mPrompt;

        public static final class Option implements Parcelable {
            public static final Creator<Option> CREATOR = null;
            Bundle mExtras;
            final int mIndex;
            final CharSequence mLabel;
            ArrayList<CharSequence> mSynonyms;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.VoiceInteractor.PickOptionRequest.Option.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.VoiceInteractor.PickOptionRequest.Option.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 9 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 10 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: android.app.VoiceInteractor.PickOptionRequest.Option.<clinit>():void");
            }

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
                    this.mSynonyms = new ArrayList();
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
                return this.mSynonyms != null ? this.mSynonyms.size() : 0;
            }

            public CharSequence getSynonymAt(int index) {
                return this.mSynonyms != null ? (CharSequence) this.mSynonyms.get(index) : null;
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

            public int describeContents() {
                return 0;
            }

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
            Prompt prompt2 = null;
            if (prompt != null) {
                prompt2 = new Prompt(prompt);
            }
            this.mPrompt = prompt2;
            this.mOptions = options;
            this.mExtras = extras;
        }

        public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
        }

        void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            super.dump(prefix, fd, writer, args);
            writer.print(prefix);
            writer.print("mPrompt=");
            writer.println(this.mPrompt);
            if (this.mOptions != null) {
                writer.print(prefix);
                writer.println("Options:");
                for (int i = 0; i < this.mOptions.length; i += VoiceInteractor.MSG_CONFIRMATION_RESULT) {
                    Option op = this.mOptions[i];
                    writer.print(prefix);
                    writer.print("  #");
                    writer.print(i);
                    writer.println(":");
                    writer.print(prefix);
                    writer.print("    mLabel=");
                    writer.println(op.mLabel);
                    writer.print(prefix);
                    writer.print("    mIndex=");
                    writer.println(op.mIndex);
                    if (op.mSynonyms != null && op.mSynonyms.size() > 0) {
                        writer.print(prefix);
                        writer.println("    Synonyms:");
                        for (int j = 0; j < op.mSynonyms.size(); j += VoiceInteractor.MSG_CONFIRMATION_RESULT) {
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
                }
            }
            if (this.mExtras != null) {
                writer.print(prefix);
                writer.print("mExtras=");
                writer.println(this.mExtras);
            }
        }

        String getRequestTypeName() {
            return "PickOption";
        }

        IVoiceInteractorRequest submit(IVoiceInteractor interactor, String packageName, IVoiceInteractorCallback callback) throws RemoteException {
            return interactor.startPickOption(packageName, callback, this.mPrompt, this.mOptions, this.mExtras);
        }
    }

    public static class Prompt implements Parcelable {
        public static final Creator<Prompt> CREATOR = null;
        private final CharSequence mVisualPrompt;
        private final CharSequence[] mVoicePrompts;

        /* renamed from: android.app.VoiceInteractor.Prompt.1 */
        static class AnonymousClass1 implements Creator<Prompt> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m26createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public Prompt createFromParcel(Parcel in) {
                return new Prompt(in);
            }

            public /* bridge */ /* synthetic */ Object[] m27newArray(int size) {
                return newArray(size);
            }

            public Prompt[] newArray(int size) {
                return new Prompt[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.VoiceInteractor.Prompt.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.VoiceInteractor.Prompt.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.VoiceInteractor.Prompt.<clinit>():void");
        }

        public Prompt(CharSequence[] voicePrompts, CharSequence visualPrompt) {
            if (voicePrompts == null) {
                throw new NullPointerException("voicePrompts must not be null");
            } else if (voicePrompts.length == 0) {
                throw new IllegalArgumentException("voicePrompts must not be empty");
            } else if (visualPrompt == null) {
                throw new NullPointerException("visualPrompt must not be null");
            } else {
                this.mVoicePrompts = voicePrompts;
                this.mVisualPrompt = visualPrompt;
            }
        }

        public Prompt(CharSequence prompt) {
            CharSequence[] charSequenceArr = new CharSequence[VoiceInteractor.MSG_CONFIRMATION_RESULT];
            charSequenceArr[0] = prompt;
            this.mVoicePrompts = charSequenceArr;
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
            StringBuilder sb = new StringBuilder(KeymasterDefs.KM_ALGORITHM_HMAC);
            DebugUtils.buildShortClassTag(this, sb);
            if (this.mVisualPrompt == null || this.mVoicePrompts == null || this.mVoicePrompts.length != VoiceInteractor.MSG_CONFIRMATION_RESULT || !this.mVisualPrompt.equals(this.mVoicePrompts[0])) {
                if (this.mVisualPrompt != null) {
                    sb.append(" visual=");
                    sb.append(this.mVisualPrompt);
                }
                if (this.mVoicePrompts != null) {
                    sb.append(", voice=");
                    for (int i = 0; i < this.mVoicePrompts.length; i += VoiceInteractor.MSG_CONFIRMATION_RESULT) {
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

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeCharSequenceArray(this.mVoicePrompts);
            dest.writeCharSequence(this.mVisualPrompt);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.VoiceInteractor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.VoiceInteractor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.VoiceInteractor.<clinit>():void");
    }

    VoiceInteractor(IVoiceInteractor interactor, Context context, Activity activity, Looper looper) {
        this.mHandlerCallerCallback = new Callback() {
            public void executeMessage(Message msg) {
                boolean z = VoiceInteractor.DEBUG;
                SomeArgs args = msg.obj;
                Request request;
                boolean complete;
                switch (msg.what) {
                    case VoiceInteractor.MSG_CONFIRMATION_RESULT /*1*/:
                        request = VoiceInteractor.this.pullRequest((IVoiceInteractorRequest) args.arg1, true);
                        if (request != null) {
                            ConfirmationRequest confirmationRequest = (ConfirmationRequest) request;
                            if (msg.arg1 != 0) {
                                z = true;
                            }
                            confirmationRequest.onConfirmationResult(z, (Bundle) args.arg2);
                            request.clear();
                        }
                    case VoiceInteractor.MSG_PICK_OPTION_RESULT /*2*/:
                        complete = msg.arg1 != 0 ? true : VoiceInteractor.DEBUG;
                        request = VoiceInteractor.this.pullRequest((IVoiceInteractorRequest) args.arg1, complete);
                        if (request != null) {
                            ((PickOptionRequest) request).onPickOptionResult(complete, (Option[]) args.arg2, (Bundle) args.arg3);
                            if (complete) {
                                request.clear();
                            }
                        }
                    case VoiceInteractor.MSG_COMPLETE_VOICE_RESULT /*3*/:
                        request = VoiceInteractor.this.pullRequest((IVoiceInteractorRequest) args.arg1, true);
                        if (request != null) {
                            ((CompleteVoiceRequest) request).onCompleteResult((Bundle) args.arg2);
                            request.clear();
                        }
                    case VoiceInteractor.MSG_ABORT_VOICE_RESULT /*4*/:
                        request = VoiceInteractor.this.pullRequest((IVoiceInteractorRequest) args.arg1, true);
                        if (request != null) {
                            ((AbortVoiceRequest) request).onAbortResult((Bundle) args.arg2);
                            request.clear();
                        }
                    case VoiceInteractor.MSG_COMMAND_RESULT /*5*/:
                        complete = msg.arg1 != 0 ? true : VoiceInteractor.DEBUG;
                        request = VoiceInteractor.this.pullRequest((IVoiceInteractorRequest) args.arg1, complete);
                        if (request != null) {
                            CommandRequest commandRequest = (CommandRequest) request;
                            if (msg.arg1 != 0) {
                                z = true;
                            }
                            commandRequest.onCommandResult(z, (Bundle) args.arg2);
                            if (complete) {
                                request.clear();
                            }
                        }
                    case VoiceInteractor.MSG_CANCEL_RESULT /*6*/:
                        request = VoiceInteractor.this.pullRequest((IVoiceInteractorRequest) args.arg1, true);
                        if (request != null) {
                            request.onCancel();
                            request.clear();
                        }
                    default:
                }
            }
        };
        this.mCallback = new Stub() {
            public void deliverConfirmationResult(IVoiceInteractorRequest request, boolean finished, Bundle result) {
                VoiceInteractor.this.mHandlerCaller.sendMessage(VoiceInteractor.this.mHandlerCaller.obtainMessageIOO(VoiceInteractor.MSG_CONFIRMATION_RESULT, finished ? VoiceInteractor.MSG_CONFIRMATION_RESULT : 0, request, result));
            }

            public void deliverPickOptionResult(IVoiceInteractorRequest request, boolean finished, Option[] options, Bundle result) {
                VoiceInteractor.this.mHandlerCaller.sendMessage(VoiceInteractor.this.mHandlerCaller.obtainMessageIOOO(VoiceInteractor.MSG_PICK_OPTION_RESULT, finished ? VoiceInteractor.MSG_CONFIRMATION_RESULT : 0, request, options, result));
            }

            public void deliverCompleteVoiceResult(IVoiceInteractorRequest request, Bundle result) {
                VoiceInteractor.this.mHandlerCaller.sendMessage(VoiceInteractor.this.mHandlerCaller.obtainMessageOO(VoiceInteractor.MSG_COMPLETE_VOICE_RESULT, request, result));
            }

            public void deliverAbortVoiceResult(IVoiceInteractorRequest request, Bundle result) {
                VoiceInteractor.this.mHandlerCaller.sendMessage(VoiceInteractor.this.mHandlerCaller.obtainMessageOO(VoiceInteractor.MSG_ABORT_VOICE_RESULT, request, result));
            }

            public void deliverCommandResult(IVoiceInteractorRequest request, boolean complete, Bundle result) {
                VoiceInteractor.this.mHandlerCaller.sendMessage(VoiceInteractor.this.mHandlerCaller.obtainMessageIOO(VoiceInteractor.MSG_COMMAND_RESULT, complete ? VoiceInteractor.MSG_CONFIRMATION_RESULT : 0, request, result));
            }

            public void deliverCancel(IVoiceInteractorRequest request) throws RemoteException {
                VoiceInteractor.this.mHandlerCaller.sendMessage(VoiceInteractor.this.mHandlerCaller.obtainMessageOO(VoiceInteractor.MSG_CANCEL_RESULT, request, null));
            }
        };
        this.mActiveRequests = new ArrayMap();
        this.mInteractor = interactor;
        this.mContext = context;
        this.mActivity = activity;
        this.mHandlerCaller = new HandlerCaller(context, looper, this.mHandlerCallerCallback, true);
    }

    Request pullRequest(IVoiceInteractorRequest request, boolean complete) {
        Request req;
        synchronized (this.mActiveRequests) {
            req = (Request) this.mActiveRequests.get(request.asBinder());
            if (req != null && complete) {
                this.mActiveRequests.remove(request.asBinder());
            }
        }
        return req;
    }

    private ArrayList<Request> makeRequestList() {
        int N = this.mActiveRequests.size();
        if (N < MSG_CONFIRMATION_RESULT) {
            return null;
        }
        ArrayList<Request> list = new ArrayList(N);
        for (int i = 0; i < N; i += MSG_CONFIRMATION_RESULT) {
            list.add((Request) this.mActiveRequests.valueAt(i));
        }
        return list;
    }

    void attachActivity(Activity activity) {
        this.mRetaining = DEBUG;
        if (this.mActivity != activity) {
            this.mContext = activity;
            this.mActivity = activity;
            ArrayList<Request> reqs = makeRequestList();
            if (reqs != null) {
                for (int i = 0; i < reqs.size(); i += MSG_CONFIRMATION_RESULT) {
                    Request req = (Request) reqs.get(i);
                    req.mContext = activity;
                    req.mActivity = activity;
                    req.onAttached(activity);
                }
            }
        }
    }

    void retainInstance() {
        this.mRetaining = true;
    }

    void detachActivity() {
        int i;
        ArrayList<Request> reqs = makeRequestList();
        if (reqs != null) {
            for (i = 0; i < reqs.size(); i += MSG_CONFIRMATION_RESULT) {
                Request req = (Request) reqs.get(i);
                req.onDetached();
                req.mActivity = null;
                req.mContext = null;
            }
        }
        if (!this.mRetaining) {
            reqs = makeRequestList();
            if (reqs != null) {
                for (i = 0; i < reqs.size(); i += MSG_CONFIRMATION_RESULT) {
                    ((Request) reqs.get(i)).cancel();
                }
            }
            this.mActiveRequests.clear();
        }
        this.mContext = null;
        this.mActivity = null;
    }

    public boolean submitRequest(Request request) {
        return submitRequest(request, null);
    }

    public boolean submitRequest(Request request, String name) {
        try {
            if (request.mRequestInterface != null) {
                throw new IllegalStateException("Given " + request + " is already active");
            }
            IVoiceInteractorRequest ireq = request.submit(this.mInteractor, this.mContext.getOpPackageName(), this.mCallback);
            request.mRequestInterface = ireq;
            request.mContext = this.mContext;
            request.mActivity = this.mActivity;
            request.mName = name;
            synchronized (this.mActiveRequests) {
                this.mActiveRequests.put(ireq.asBinder(), request);
            }
            return true;
        } catch (RemoteException e) {
            Log.w(TAG, "Remove voice interactor service died", e);
            return DEBUG;
        }
    }

    public Request[] getActiveRequests() {
        synchronized (this.mActiveRequests) {
            int N = this.mActiveRequests.size();
            if (N <= 0) {
                Request[] requestArr = NO_REQUESTS;
                return requestArr;
            }
            Request[] requests = new Request[N];
            for (int i = 0; i < N; i += MSG_CONFIRMATION_RESULT) {
                requests[i] = (Request) this.mActiveRequests.valueAt(i);
            }
            return requests;
        }
    }

    public Request getActiveRequest(String name) {
        synchronized (this.mActiveRequests) {
            int N = this.mActiveRequests.size();
            for (int i = 0; i < N; i += MSG_CONFIRMATION_RESULT) {
                Request req = (Request) this.mActiveRequests.valueAt(i);
                if (name == req.getName() || (name != null && name.equals(req.getName()))) {
                    return req;
                }
            }
            return null;
        }
    }

    public boolean[] supportsCommands(String[] commands) {
        try {
            return this.mInteractor.supportsCommands(this.mContext.getOpPackageName(), commands);
        } catch (RemoteException e) {
            throw new RuntimeException("Voice interactor has died", e);
        }
    }

    void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        String innerPrefix = prefix + "    ";
        if (this.mActiveRequests.size() > 0) {
            writer.print(prefix);
            writer.println("Active voice requests:");
            for (int i = 0; i < this.mActiveRequests.size(); i += MSG_CONFIRMATION_RESULT) {
                Request req = (Request) this.mActiveRequests.valueAt(i);
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
}
