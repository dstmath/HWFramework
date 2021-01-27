package ohos.miscservices.inputmethodability.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.UserHandle;
import android.view.InputChannel;
import android.view.inputmethod.EditorInfo;
import java.lang.ref.WeakReference;
import java.util.Optional;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditorAttribute;
import ohos.miscservices.inputmethod.InputDataChannel;
import ohos.miscservices.inputmethod.internal.AgentCallbackSkeleton;
import ohos.miscservices.inputmethod.internal.IInputControlChannel;
import ohos.miscservices.inputmethod.internal.IInputControlChannelSkeleton;
import ohos.miscservices.inputmethod.internal.IInputMethodCore;
import ohos.miscservices.inputmethod.internal.InputDataChannelSkeleton;
import ohos.miscservices.inputmethodability.InputDataChannelWrapper;
import ohos.miscservices.inputmethodability.InputMethodAgentImpl;
import ohos.miscservices.inputmethodability.InputMethodEngine;
import ohos.miscservices.inputmethodability.adapter.InputMethodAdapter;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.PacMap;
import ohos.utils.adapter.PacMapUtils;

public class InputMethodAdapter {
    private static final String A_DESCRIPTOR = "com.android.internal.view.IInputMethod";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218110976, "InputMethodAdapter");
    private static final String Z_DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IInputMethodCore";
    private WeakReference<Context> mAospContextRefer;
    private WeakReference<ohos.app.Context> mContextRefer;
    private InputMethodEngine mInputMethodEngine;

    public InputMethodAdapter(ohos.app.Context context, InputMethodEngine inputMethodEngine) {
        HiLog.info(LABEL, "inputMethodDelegation: %{public}s", inputMethodEngine);
        this.mContextRefer = new WeakReference<>(context);
        this.mInputMethodEngine = inputMethodEngine;
        ohos.app.Context context2 = this.mContextRefer.get();
        if (context2 != null) {
            Object hostContext = context2.getHostContext();
            if (hostContext instanceof Context) {
                this.mAospContextRefer = new WeakReference<>((Context) hostContext);
            } else {
                HiLog.error(LABEL, "Get host context failed.", new Object[0]);
            }
        }
    }

    public IRemoteObject createInputMethodCoreImpl() {
        HiLog.info(LABEL, "createInputMethodCoreImpl.", new Object[0]);
        return new InputMethodCoreSkeleton(Z_DESCRIPTOR);
    }

    class InputMethodCoreSkeleton extends RemoteObject implements IInputMethodCore {
        private static final int BYTE_COUNT_BINDER_OR_REMOTE_OBJECT = 24;
        private static final int BYTE_COUNT_INT = 4;
        private static final int BYTE_COUNT_SPANNABLE_STRING = 28;
        private static final int ERR_OK = 0;
        private static final int ERR_RUNTIME_EXCEPTION = -1;
        private static final int STRING8_ALIGN_OFFSET = 3;
        private static final int TRANSACTION_BIND_INPUT = 2;
        private static final int TRANSACTION_CHANGE_INPUT_METHOD_SUBTYPE = 10;
        private static final int TRANSACTION_CREATE_AGENT = 5;
        private static final int TRANSACTION_HIDE_KEYBOARD = 9;
        private static final int TRANSACTION_INITIALIZE_INPUT = 1;
        private static final int TRANSACTION_REVOKE_SESSION = 7;
        private static final int TRANSACTION_SET_SESSION_ENABLED = 6;
        private static final int TRANSACTION_SHOW_KEYBOARD = 8;
        private static final int TRANSACTION_START_INPUT = 4;
        private static final int TRANSACTION_UNBIND_INPUT = 3;
        private final HiLogLabel subLabel = new HiLogLabel(3, 218110976, "InputMethodCoreSkeleton");
        private TaskDispatcher uiTaskDispatcher;

        @Override // ohos.rpc.IRemoteBroker
        public IRemoteObject asObject() {
            return this;
        }

        public InputMethodCoreSkeleton(String str) {
            super(str);
            ohos.app.Context context = (ohos.app.Context) InputMethodAdapter.this.mContextRefer.get();
            if (context != null) {
                this.uiTaskDispatcher = context.getUITaskDispatcher();
            }
        }

        private boolean isRightMessage(String str) {
            return InputMethodAdapter.A_DESCRIPTOR.equals(str);
        }

        @Override // ohos.rpc.RemoteObject
        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
            HiLog.debug(this.subLabel, "onRemoteRequest code=%{public}d", Integer.valueOf(i));
            switch (i) {
                case 1:
                    HiLog.debug(this.subLabel, "TRANSACTION_INITIALIZE_INPUT", new Object[0]);
                    return transactToInitializeInput(messageParcel, messageParcel2);
                case 2:
                    HiLog.debug(this.subLabel, "TRANSACTION_BIND_INPUT", new Object[0]);
                    return true;
                case 3:
                    HiLog.debug(this.subLabel, "TRANSACTION_UNBIND_INPUT", new Object[0]);
                    return true;
                case 4:
                    HiLog.debug(this.subLabel, "TRANSACTION_START_INPUT", new Object[0]);
                    return transactToStartInput(messageParcel, messageParcel2);
                case 5:
                    HiLog.debug(this.subLabel, "TRANSACTION_CREATE_AGENT", new Object[0]);
                    return transactToCreateAgent(messageParcel, messageParcel2);
                case 6:
                    HiLog.debug(this.subLabel, "TRANSACTION_SET_SESSION_ENABLED", new Object[0]);
                    return true;
                case 7:
                    HiLog.debug(this.subLabel, "TRANSACTION_REVOKE_SESSION", new Object[0]);
                    return true;
                case 8:
                    HiLog.debug(this.subLabel, "TRANSACTION_SHOW_KEYBOARD", new Object[0]);
                    return transactToShowKeyboard(messageParcel, messageParcel2);
                case 9:
                    HiLog.debug(this.subLabel, "TRANSACTION_HIDE_KEYBOARD", new Object[0]);
                    return transactToHideKeyboard(messageParcel, messageParcel2);
                case 10:
                    HiLog.debug(this.subLabel, "TRANSACTION_CHANGE_INPUT_METHOD_SUBTYPE", new Object[0]);
                    return true;
                default:
                    return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            }
        }

        private boolean transactToInitializeInput(MessageParcel messageParcel, MessageParcel messageParcel2) {
            if (!isRightMessage(messageParcel.readInterfaceToken())) {
                HiLog.error(this.subLabel, "transactToInitializeInput, error message.", new Object[0]);
                return false;
            }
            try {
                initializeInput(messageParcel.readRemoteObject(), messageParcel.readInt(), new InputMethodPrivilegedOperationsAdapter(messageParcel.readRemoteObject()).getAdaptControlChannel());
                messageParcel2.writeInt(0);
                return true;
            } catch (RemoteException unused) {
                messageParcel2.writeInt(-1);
                return true;
            }
        }

        private boolean transactToStartInput(MessageParcel messageParcel, MessageParcel messageParcel2) {
            if (!isRightMessage(messageParcel.readInterfaceToken())) {
                HiLog.error(this.subLabel, "transactToStartInput, error message.", new Object[0]);
                return false;
            }
            IRemoteObject readRemoteObject = messageParcel.readRemoteObject();
            IRemoteObject readRemoteObject2 = messageParcel.readRemoteObject();
            HiLog.debug(this.subLabel, "TRANSACTION_START_INPUT flag missMethods=%{public}d", Integer.valueOf(messageParcel.readInt()));
            Optional<EditorInfo> readEditorInfo = readEditorInfo(messageParcel);
            EditorInfo editorInfo = null;
            if (readEditorInfo.isPresent()) {
                editorInfo = readEditorInfo.get();
            }
            try {
                boolean startInput = startInput(readRemoteObject, EditorInfoAdapter.convertToEditorAttribute(editorInfo), new InputConnectionAdapter(readRemoteObject2).getAdaptRemoteObject());
                messageParcel2.writeInt(0);
                messageParcel2.writeInt(startInput ? 1 : 0);
                return true;
            } catch (RemoteException unused) {
                messageParcel2.writeInt(-1);
                return true;
            }
        }

        private boolean transactToCreateAgent(MessageParcel messageParcel, MessageParcel messageParcel2) {
            if (!isRightMessage(messageParcel.readInterfaceToken())) {
                HiLog.error(this.subLabel, "createSession, error message.", new Object[0]);
                return false;
            }
            Optional<InputChannel> readInputChannel = readInputChannel(messageParcel);
            if (!readInputChannel.isPresent()) {
                HiLog.error(this.subLabel, "createSession, read channel failed", new Object[0]);
                return false;
            }
            try {
                createAgent(new SessionCallbackAdapter((Context) InputMethodAdapter.this.mAospContextRefer.get(), messageParcel.readRemoteObject(), readInputChannel.get()).getAdaptRemoteObject());
                messageParcel2.writeInt(0);
                return true;
            } catch (RemoteException unused) {
                messageParcel2.writeInt(-1);
                return true;
            }
        }

        private boolean transactToShowKeyboard(MessageParcel messageParcel, MessageParcel messageParcel2) {
            if (!isRightMessage(messageParcel.readInterfaceToken())) {
                HiLog.error(this.subLabel, "transactToShowKeyboard, error message.", new Object[0]);
                return false;
            }
            try {
                boolean showKeyboard = showKeyboard(1);
                messageParcel2.writeInt(0);
                messageParcel2.writeInt(showKeyboard ? 1 : 0);
                return true;
            } catch (RemoteException unused) {
                messageParcel2.writeInt(-1);
                return true;
            }
        }

        private boolean transactToHideKeyboard(MessageParcel messageParcel, MessageParcel messageParcel2) {
            if (!isRightMessage(messageParcel.readInterfaceToken())) {
                HiLog.error(this.subLabel, "transactToHideKeyboard, error message.", new Object[0]);
                return false;
            }
            try {
                boolean hideKeyboard = hideKeyboard(1);
                messageParcel2.writeInt(0);
                messageParcel2.writeInt(hideKeyboard ? 1 : 0);
                return true;
            } catch (RemoteException unused) {
                messageParcel2.writeInt(-1);
                return true;
            }
        }

        private void readIBinder(MessageParcel messageParcel, Parcel parcel) {
            for (int i = 0; i < 6; i++) {
                parcel.writeInt(messageParcel.readInt());
            }
        }

        private Optional<InputChannel> readInputChannel(MessageParcel messageParcel) {
            HiLog.debug(this.subLabel, "skipReadInputChannel start read flag position=%{public}d", Integer.valueOf(messageParcel.getReadPosition()));
            if (messageParcel.readInt() == 0) {
                return Optional.empty();
            }
            Parcel obtain = Parcel.obtain();
            int readInt = messageParcel.readInt();
            obtain.writeInt(readInt);
            HiLog.debug(this.subLabel, "skipReadInputChannel isInitialized=%{public}d", Integer.valueOf(readInt));
            if (readInt == 1) {
                int readInt2 = messageParcel.readInt();
                obtain.writeInt(readInt2);
                int i = (readInt2 + 1 + 3) & -4;
                HiLog.debug(this.subLabel, "skipReadInputChannel readString8 size=%{public}d", Integer.valueOf(i));
                if (i < 0 || i > (messageParcel.getSize() - messageParcel.getReadPosition()) + 1) {
                    HiLog.error(this.subLabel, "Not a correct size to read int array.", new Object[0]);
                    return Optional.empty();
                }
                for (int i2 = 0; i2 < i / 4; i2++) {
                    obtain.writeInt(messageParcel.readInt());
                }
            }
            readIBinder(messageParcel, obtain);
            obtain.writeFileDescriptor(messageParcel.readFileDescriptor());
            HiLog.debug(this.subLabel, "convertToInputChannel", new Object[0]);
            obtain.setDataPosition(0);
            return Optional.ofNullable((InputChannel) InputChannel.CREATOR.createFromParcel(obtain));
        }

        private Optional<EditorInfo> readEditorInfo(MessageParcel messageParcel) {
            if (messageParcel.readInt() == 0) {
                return Optional.empty();
            }
            EditorInfo editorInfo = new EditorInfo();
            editorInfo.inputType = messageParcel.readInt();
            editorInfo.imeOptions = messageParcel.readInt();
            editorInfo.privateImeOptions = messageParcel.readString();
            editorInfo.actionLabel = getStringFromParcel(messageParcel);
            editorInfo.actionId = messageParcel.readInt();
            editorInfo.initialSelStart = messageParcel.readInt();
            editorInfo.initialSelEnd = messageParcel.readInt();
            editorInfo.initialCapsMode = messageParcel.readInt();
            editorInfo.hintText = getStringFromParcel(messageParcel);
            editorInfo.label = getStringFromParcel(messageParcel);
            editorInfo.packageName = messageParcel.readString();
            editorInfo.fieldId = messageParcel.readInt();
            editorInfo.fieldName = messageParcel.readString();
            editorInfo.extras = readBundle(messageParcel);
            LocaleList forLanguageTags = LocaleList.forLanguageTags(messageParcel.readString());
            UserHandle userHandle = null;
            if (forLanguageTags.isEmpty()) {
                forLanguageTags = null;
            }
            editorInfo.hintLocales = forLanguageTags;
            editorInfo.contentMimeTypes = messageParcel.readStringArray();
            int readInt = messageParcel.readInt();
            if (readInt != -10000) {
                userHandle = new UserHandle(readInt);
            }
            editorInfo.targetInputMethodUser = userHandle;
            return Optional.of(editorInfo);
        }

        private Bundle readBundle(MessageParcel messageParcel) {
            return PacMapUtils.convertIntoBundle(PacMap.PRODUCER.createFromParcel(messageParcel));
        }

        private String getStringFromParcel(MessageParcel messageParcel) {
            int readInt;
            int readInt2 = messageParcel.readInt();
            String readString = messageParcel.readString();
            if (readString != null && readInt2 != 1 && (readInt = messageParcel.readInt()) >= 1 && readInt <= 28) {
                messageParcel.readInt();
                messageParcel.readInt();
                messageParcel.readInt();
            }
            return readString;
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputMethodCore
        public void initializeInput(IRemoteObject iRemoteObject, int i, IRemoteObject iRemoteObject2) throws RemoteException {
            HiLog.debug(this.subLabel, "initializeInput", new Object[0]);
            IInputControlChannel asInterface = IInputControlChannelSkeleton.asInterface(iRemoteObject2);
            TaskDispatcher taskDispatcher = this.uiTaskDispatcher;
            if (taskDispatcher == null) {
                HiLog.error(this.subLabel, "The current UI task dispatcher is null, initialize input failed.", new Object[0]);
            } else {
                taskDispatcher.asyncDispatch(new Runnable(iRemoteObject, i, asInterface) {
                    /* class ohos.miscservices.inputmethodability.adapter.$$Lambda$InputMethodAdapter$InputMethodCoreSkeleton$XLF6TBSDl5pnt4GLIdQ4M2ApkE */
                    private final /* synthetic */ IRemoteObject f$1;
                    private final /* synthetic */ int f$2;
                    private final /* synthetic */ IInputControlChannel f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        InputMethodAdapter.InputMethodCoreSkeleton.this.lambda$initializeInput$0$InputMethodAdapter$InputMethodCoreSkeleton(this.f$1, this.f$2, this.f$3);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$initializeInput$0$InputMethodAdapter$InputMethodCoreSkeleton(IRemoteObject iRemoteObject, int i, IInputControlChannel iInputControlChannel) {
            InputMethodAdapter.this.mInputMethodEngine.initializeInput(iRemoteObject, i, iInputControlChannel);
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputMethodCore
        public void createAgent(IRemoteObject iRemoteObject) throws RemoteException {
            HiLog.debug(this.subLabel, "createAgent", new Object[0]);
            TaskDispatcher taskDispatcher = this.uiTaskDispatcher;
            if (taskDispatcher == null) {
                HiLog.error(this.subLabel, "The current UI task dispatcher is null, create agent failed.", new Object[0]);
            } else {
                taskDispatcher.asyncDispatch(new Runnable(iRemoteObject) {
                    /* class ohos.miscservices.inputmethodability.adapter.$$Lambda$InputMethodAdapter$InputMethodCoreSkeleton$KTrdu6vnh6BmOEoQkJPrEsGEfdI */
                    private final /* synthetic */ IRemoteObject f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        InputMethodAdapter.InputMethodCoreSkeleton.this.lambda$createAgent$1$InputMethodAdapter$InputMethodCoreSkeleton(this.f$1);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$createAgent$1$InputMethodAdapter$InputMethodCoreSkeleton(IRemoteObject iRemoteObject) {
            try {
                AgentCallbackSkeleton.asInterface(iRemoteObject).agentCreated(new InputMethodAgentImpl(InputMethodAdapter.this.mInputMethodEngine).asObject());
            } catch (RemoteException unused) {
                HiLog.error(this.subLabel, "RemoteException", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputMethodCore
        public boolean startInput(IRemoteObject iRemoteObject, EditorAttribute editorAttribute, IRemoteObject iRemoteObject2) throws RemoteException {
            HiLog.debug(this.subLabel, "startInput", new Object[0]);
            InputDataChannelWrapper inputDataChannelWrapper = new InputDataChannelWrapper(InputDataChannelSkeleton.asInterface(iRemoteObject2));
            TaskDispatcher taskDispatcher = this.uiTaskDispatcher;
            if (taskDispatcher == null) {
                HiLog.error(this.subLabel, "The current UI task dispatcher is null, start input failed.", new Object[0]);
                return false;
            }
            taskDispatcher.asyncDispatch(new Runnable(iRemoteObject, editorAttribute, inputDataChannelWrapper) {
                /* class ohos.miscservices.inputmethodability.adapter.$$Lambda$InputMethodAdapter$InputMethodCoreSkeleton$bqgnIhjDtB1jDZGe6Gct7i2uDyY */
                private final /* synthetic */ IRemoteObject f$1;
                private final /* synthetic */ EditorAttribute f$2;
                private final /* synthetic */ InputDataChannel f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    InputMethodAdapter.InputMethodCoreSkeleton.this.lambda$startInput$2$InputMethodAdapter$InputMethodCoreSkeleton(this.f$1, this.f$2, this.f$3);
                }
            });
            return true;
        }

        public /* synthetic */ void lambda$startInput$2$InputMethodAdapter$InputMethodCoreSkeleton(IRemoteObject iRemoteObject, EditorAttribute editorAttribute, InputDataChannel inputDataChannel) {
            InputMethodAdapter.this.mInputMethodEngine.startInput(iRemoteObject, editorAttribute, inputDataChannel);
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputMethodCore
        public boolean showKeyboard(int i) throws RemoteException {
            HiLog.debug(this.subLabel, "showKeyboard", new Object[0]);
            TaskDispatcher taskDispatcher = this.uiTaskDispatcher;
            if (taskDispatcher == null) {
                HiLog.error(this.subLabel, "The current UI task dispatcher is null, show keyboard failed.", new Object[0]);
                return false;
            }
            taskDispatcher.asyncDispatch(new Runnable(i) {
                /* class ohos.miscservices.inputmethodability.adapter.$$Lambda$InputMethodAdapter$InputMethodCoreSkeleton$jr0UfL9U8Uk8h_ehUPG0QbDgb74 */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    InputMethodAdapter.InputMethodCoreSkeleton.this.lambda$showKeyboard$3$InputMethodAdapter$InputMethodCoreSkeleton(this.f$1);
                }
            });
            return true;
        }

        public /* synthetic */ void lambda$showKeyboard$3$InputMethodAdapter$InputMethodCoreSkeleton(int i) {
            InputMethodAdapter.this.mInputMethodEngine.showKeyboard(i);
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputMethodCore
        public boolean hideKeyboard(int i) throws RemoteException {
            HiLog.debug(this.subLabel, "showKeyboard", new Object[0]);
            TaskDispatcher taskDispatcher = this.uiTaskDispatcher;
            if (taskDispatcher == null) {
                HiLog.error(this.subLabel, "The current UI task dispatcher is null, hide keyboard failed.", new Object[0]);
                return false;
            }
            taskDispatcher.asyncDispatch(new Runnable(i) {
                /* class ohos.miscservices.inputmethodability.adapter.$$Lambda$InputMethodAdapter$InputMethodCoreSkeleton$qF7Jv6e2EXMijPYDmSIbvVIvMe4 */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    InputMethodAdapter.InputMethodCoreSkeleton.this.lambda$hideKeyboard$4$InputMethodAdapter$InputMethodCoreSkeleton(this.f$1);
                }
            });
            return true;
        }

        public /* synthetic */ void lambda$hideKeyboard$4$InputMethodAdapter$InputMethodCoreSkeleton(int i) {
            InputMethodAdapter.this.mInputMethodEngine.hideKeyboard(i);
        }
    }
}
