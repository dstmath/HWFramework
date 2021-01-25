package com.android.internal.app;

import android.annotation.UnsupportedAppUsage;
import android.content.ComponentName;
import android.content.Intent;
import android.hardware.soundtrigger.IRecognitionStatusCallback;
import android.hardware.soundtrigger.SoundTrigger;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.service.voice.IVoiceInteractionService;
import android.service.voice.IVoiceInteractionSession;
import com.android.internal.app.IVoiceActionCheckCallback;
import com.android.internal.app.IVoiceInteractionSessionListener;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;
import com.android.internal.app.IVoiceInteractor;
import java.util.List;

public interface IVoiceInteractionManagerService extends IInterface {
    boolean activeServiceSupportsAssist() throws RemoteException;

    boolean activeServiceSupportsLaunchFromKeyguard() throws RemoteException;

    void closeSystemDialogs(IBinder iBinder) throws RemoteException;

    int deleteKeyphraseSoundModel(int i, String str) throws RemoteException;

    boolean deliverNewSession(IBinder iBinder, IVoiceInteractionSession iVoiceInteractionSession, IVoiceInteractor iVoiceInteractor) throws RemoteException;

    void finish(IBinder iBinder) throws RemoteException;

    ComponentName getActiveServiceComponentName() throws RemoteException;

    void getActiveServiceSupportedActions(List<String> list, IVoiceActionCheckCallback iVoiceActionCheckCallback) throws RemoteException;

    int getDisabledShowContext() throws RemoteException;

    SoundTrigger.ModuleProperties getDspModuleProperties(IVoiceInteractionService iVoiceInteractionService) throws RemoteException;

    @UnsupportedAppUsage
    SoundTrigger.KeyphraseSoundModel getKeyphraseSoundModel(int i, String str) throws RemoteException;

    int getUserDisabledShowContext() throws RemoteException;

    void hideCurrentSession() throws RemoteException;

    boolean hideSessionFromSession(IBinder iBinder) throws RemoteException;

    boolean isEnrolledForKeyphrase(IVoiceInteractionService iVoiceInteractionService, int i, String str) throws RemoteException;

    boolean isSessionRunning() throws RemoteException;

    void launchVoiceAssistFromKeyguard() throws RemoteException;

    void onLockscreenShown() throws RemoteException;

    void performDirectAction(IBinder iBinder, String str, Bundle bundle, int i, IBinder iBinder2, RemoteCallback remoteCallback, RemoteCallback remoteCallback2) throws RemoteException;

    void registerVoiceInteractionSessionListener(IVoiceInteractionSessionListener iVoiceInteractionSessionListener) throws RemoteException;

    void requestDirectActions(IBinder iBinder, int i, IBinder iBinder2, RemoteCallback remoteCallback, RemoteCallback remoteCallback2) throws RemoteException;

    void setDisabledShowContext(int i) throws RemoteException;

    void setKeepAwake(IBinder iBinder, boolean z) throws RemoteException;

    void setUiHints(IVoiceInteractionService iVoiceInteractionService, Bundle bundle) throws RemoteException;

    void showSession(IVoiceInteractionService iVoiceInteractionService, Bundle bundle, int i) throws RemoteException;

    boolean showSessionForActiveService(Bundle bundle, int i, IVoiceInteractionSessionShowCallback iVoiceInteractionSessionShowCallback, IBinder iBinder) throws RemoteException;

    boolean showSessionFromSession(IBinder iBinder, Bundle bundle, int i) throws RemoteException;

    int startAssistantActivity(IBinder iBinder, Intent intent, String str) throws RemoteException;

    int startRecognition(IVoiceInteractionService iVoiceInteractionService, int i, String str, IRecognitionStatusCallback iRecognitionStatusCallback, SoundTrigger.RecognitionConfig recognitionConfig) throws RemoteException;

    int startVoiceActivity(IBinder iBinder, Intent intent, String str) throws RemoteException;

    int stopRecognition(IVoiceInteractionService iVoiceInteractionService, int i, IRecognitionStatusCallback iRecognitionStatusCallback) throws RemoteException;

    int updateKeyphraseSoundModel(SoundTrigger.KeyphraseSoundModel keyphraseSoundModel) throws RemoteException;

    public static class Default implements IVoiceInteractionManagerService {
        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public void showSession(IVoiceInteractionService service, Bundle sessionArgs, int flags) throws RemoteException {
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public boolean deliverNewSession(IBinder token, IVoiceInteractionSession session, IVoiceInteractor interactor) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public boolean showSessionFromSession(IBinder token, Bundle sessionArgs, int flags) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public boolean hideSessionFromSession(IBinder token) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public int startVoiceActivity(IBinder token, Intent intent, String resolvedType) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public int startAssistantActivity(IBinder token, Intent intent, String resolvedType) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public void setKeepAwake(IBinder token, boolean keepAwake) throws RemoteException {
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public void closeSystemDialogs(IBinder token) throws RemoteException {
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public void finish(IBinder token) throws RemoteException {
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public void setDisabledShowContext(int flags) throws RemoteException {
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public int getDisabledShowContext() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public int getUserDisabledShowContext() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public SoundTrigger.KeyphraseSoundModel getKeyphraseSoundModel(int keyphraseId, String bcp47Locale) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public int updateKeyphraseSoundModel(SoundTrigger.KeyphraseSoundModel model) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public int deleteKeyphraseSoundModel(int keyphraseId, String bcp47Locale) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public SoundTrigger.ModuleProperties getDspModuleProperties(IVoiceInteractionService service) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public boolean isEnrolledForKeyphrase(IVoiceInteractionService service, int keyphraseId, String bcp47Locale) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public int startRecognition(IVoiceInteractionService service, int keyphraseId, String bcp47Locale, IRecognitionStatusCallback callback, SoundTrigger.RecognitionConfig recognitionConfig) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public int stopRecognition(IVoiceInteractionService service, int keyphraseId, IRecognitionStatusCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public ComponentName getActiveServiceComponentName() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public boolean showSessionForActiveService(Bundle args, int sourceFlags, IVoiceInteractionSessionShowCallback showCallback, IBinder activityToken) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public void hideCurrentSession() throws RemoteException {
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public void launchVoiceAssistFromKeyguard() throws RemoteException {
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public boolean isSessionRunning() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public boolean activeServiceSupportsAssist() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public boolean activeServiceSupportsLaunchFromKeyguard() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public void onLockscreenShown() throws RemoteException {
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public void registerVoiceInteractionSessionListener(IVoiceInteractionSessionListener listener) throws RemoteException {
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public void getActiveServiceSupportedActions(List<String> list, IVoiceActionCheckCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public void setUiHints(IVoiceInteractionService service, Bundle hints) throws RemoteException {
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public void requestDirectActions(IBinder token, int taskId, IBinder assistToken, RemoteCallback cancellationCallback, RemoteCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.app.IVoiceInteractionManagerService
        public void performDirectAction(IBinder token, String actionId, Bundle arguments, int taskId, IBinder assistToken, RemoteCallback cancellationCallback, RemoteCallback resultCallback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVoiceInteractionManagerService {
        private static final String DESCRIPTOR = "com.android.internal.app.IVoiceInteractionManagerService";
        static final int TRANSACTION_activeServiceSupportsAssist = 25;
        static final int TRANSACTION_activeServiceSupportsLaunchFromKeyguard = 26;
        static final int TRANSACTION_closeSystemDialogs = 8;
        static final int TRANSACTION_deleteKeyphraseSoundModel = 15;
        static final int TRANSACTION_deliverNewSession = 2;
        static final int TRANSACTION_finish = 9;
        static final int TRANSACTION_getActiveServiceComponentName = 20;
        static final int TRANSACTION_getActiveServiceSupportedActions = 29;
        static final int TRANSACTION_getDisabledShowContext = 11;
        static final int TRANSACTION_getDspModuleProperties = 16;
        static final int TRANSACTION_getKeyphraseSoundModel = 13;
        static final int TRANSACTION_getUserDisabledShowContext = 12;
        static final int TRANSACTION_hideCurrentSession = 22;
        static final int TRANSACTION_hideSessionFromSession = 4;
        static final int TRANSACTION_isEnrolledForKeyphrase = 17;
        static final int TRANSACTION_isSessionRunning = 24;
        static final int TRANSACTION_launchVoiceAssistFromKeyguard = 23;
        static final int TRANSACTION_onLockscreenShown = 27;
        static final int TRANSACTION_performDirectAction = 32;
        static final int TRANSACTION_registerVoiceInteractionSessionListener = 28;
        static final int TRANSACTION_requestDirectActions = 31;
        static final int TRANSACTION_setDisabledShowContext = 10;
        static final int TRANSACTION_setKeepAwake = 7;
        static final int TRANSACTION_setUiHints = 30;
        static final int TRANSACTION_showSession = 1;
        static final int TRANSACTION_showSessionForActiveService = 21;
        static final int TRANSACTION_showSessionFromSession = 3;
        static final int TRANSACTION_startAssistantActivity = 6;
        static final int TRANSACTION_startRecognition = 18;
        static final int TRANSACTION_startVoiceActivity = 5;
        static final int TRANSACTION_stopRecognition = 19;
        static final int TRANSACTION_updateKeyphraseSoundModel = 14;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceInteractionManagerService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceInteractionManagerService)) {
                return new Proxy(obj);
            }
            return (IVoiceInteractionManagerService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "showSession";
                case 2:
                    return "deliverNewSession";
                case 3:
                    return "showSessionFromSession";
                case 4:
                    return "hideSessionFromSession";
                case 5:
                    return "startVoiceActivity";
                case 6:
                    return "startAssistantActivity";
                case 7:
                    return "setKeepAwake";
                case 8:
                    return "closeSystemDialogs";
                case 9:
                    return "finish";
                case 10:
                    return "setDisabledShowContext";
                case 11:
                    return "getDisabledShowContext";
                case 12:
                    return "getUserDisabledShowContext";
                case 13:
                    return "getKeyphraseSoundModel";
                case 14:
                    return "updateKeyphraseSoundModel";
                case 15:
                    return "deleteKeyphraseSoundModel";
                case 16:
                    return "getDspModuleProperties";
                case 17:
                    return "isEnrolledForKeyphrase";
                case 18:
                    return "startRecognition";
                case 19:
                    return "stopRecognition";
                case 20:
                    return "getActiveServiceComponentName";
                case 21:
                    return "showSessionForActiveService";
                case 22:
                    return "hideCurrentSession";
                case 23:
                    return "launchVoiceAssistFromKeyguard";
                case 24:
                    return "isSessionRunning";
                case 25:
                    return "activeServiceSupportsAssist";
                case 26:
                    return "activeServiceSupportsLaunchFromKeyguard";
                case 27:
                    return "onLockscreenShown";
                case 28:
                    return "registerVoiceInteractionSessionListener";
                case 29:
                    return "getActiveServiceSupportedActions";
                case 30:
                    return "setUiHints";
                case 31:
                    return "requestDirectActions";
                case 32:
                    return "performDirectAction";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            Bundle _arg12;
            Intent _arg13;
            Intent _arg14;
            SoundTrigger.KeyphraseSoundModel _arg0;
            SoundTrigger.RecognitionConfig _arg4;
            Bundle _arg02;
            Bundle _arg15;
            RemoteCallback _arg3;
            RemoteCallback _arg42;
            Bundle _arg2;
            RemoteCallback _arg5;
            RemoteCallback _arg6;
            if (code != 1598968902) {
                boolean _arg16 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        IVoiceInteractionService _arg03 = IVoiceInteractionService.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        showSession(_arg03, _arg1, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deliverNewSession = deliverNewSession(data.readStrongBinder(), IVoiceInteractionSession.Stub.asInterface(data.readStrongBinder()), IVoiceInteractor.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(deliverNewSession ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg04 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg12 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        boolean showSessionFromSession = showSessionFromSession(_arg04, _arg12, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(showSessionFromSession ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hideSessionFromSession = hideSessionFromSession(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(hideSessionFromSession ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg05 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg13 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        int _result = startVoiceActivity(_arg05, _arg13, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg06 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg14 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        int _result2 = startAssistantActivity(_arg06, _arg14, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg07 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        setKeepAwake(_arg07, _arg16);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        closeSystemDialogs(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        finish(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        setDisabledShowContext(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getDisabledShowContext();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getUserDisabledShowContext();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        SoundTrigger.KeyphraseSoundModel _result5 = getKeyphraseSoundModel(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = SoundTrigger.KeyphraseSoundModel.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        int _result6 = updateKeyphraseSoundModel(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = deleteKeyphraseSoundModel(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        SoundTrigger.ModuleProperties _result8 = getDspModuleProperties(IVoiceInteractionService.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isEnrolledForKeyphrase = isEnrolledForKeyphrase(IVoiceInteractionService.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isEnrolledForKeyphrase ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        IVoiceInteractionService _arg08 = IVoiceInteractionService.Stub.asInterface(data.readStrongBinder());
                        int _arg17 = data.readInt();
                        String _arg22 = data.readString();
                        IRecognitionStatusCallback _arg32 = IRecognitionStatusCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg4 = SoundTrigger.RecognitionConfig.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        int _result9 = startRecognition(_arg08, _arg17, _arg22, _arg32, _arg4);
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = stopRecognition(IVoiceInteractionService.Stub.asInterface(data.readStrongBinder()), data.readInt(), IRecognitionStatusCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        ComponentName _result11 = getActiveServiceComponentName();
                        reply.writeNoException();
                        if (_result11 != null) {
                            reply.writeInt(1);
                            _result11.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        boolean showSessionForActiveService = showSessionForActiveService(_arg02, data.readInt(), IVoiceInteractionSessionShowCallback.Stub.asInterface(data.readStrongBinder()), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(showSessionForActiveService ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        hideCurrentSession();
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        launchVoiceAssistFromKeyguard();
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSessionRunning = isSessionRunning();
                        reply.writeNoException();
                        reply.writeInt(isSessionRunning ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        boolean activeServiceSupportsAssist = activeServiceSupportsAssist();
                        reply.writeNoException();
                        reply.writeInt(activeServiceSupportsAssist ? 1 : 0);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        boolean activeServiceSupportsLaunchFromKeyguard = activeServiceSupportsLaunchFromKeyguard();
                        reply.writeNoException();
                        reply.writeInt(activeServiceSupportsLaunchFromKeyguard ? 1 : 0);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        onLockscreenShown();
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        registerVoiceInteractionSessionListener(IVoiceInteractionSessionListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        getActiveServiceSupportedActions(data.createStringArrayList(), IVoiceActionCheckCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        IVoiceInteractionService _arg09 = IVoiceInteractionService.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg15 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        setUiHints(_arg09, _arg15);
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg010 = data.readStrongBinder();
                        int _arg18 = data.readInt();
                        IBinder _arg23 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg3 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg42 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg42 = null;
                        }
                        requestDirectActions(_arg010, _arg18, _arg23, _arg3, _arg42);
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg011 = data.readStrongBinder();
                        String _arg19 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _arg33 = data.readInt();
                        IBinder _arg43 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg5 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg6 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg6 = null;
                        }
                        performDirectAction(_arg011, _arg19, _arg2, _arg33, _arg43, _arg5, _arg6);
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IVoiceInteractionManagerService {
            public static IVoiceInteractionManagerService sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public void showSession(IVoiceInteractionService service, Bundle sessionArgs, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(service != null ? service.asBinder() : null);
                    if (sessionArgs != null) {
                        _data.writeInt(1);
                        sessionArgs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().showSession(service, sessionArgs, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public boolean deliverNewSession(IBinder token, IVoiceInteractionSession session, IVoiceInteractor interactor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    if (interactor != null) {
                        iBinder = interactor.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deliverNewSession(token, session, interactor);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public boolean showSessionFromSession(IBinder token, Bundle sessionArgs, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = true;
                    if (sessionArgs != null) {
                        _data.writeInt(1);
                        sessionArgs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().showSessionFromSession(token, sessionArgs, flags);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public boolean hideSessionFromSession(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hideSessionFromSession(token);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public int startVoiceActivity(IBinder token, Intent intent, String resolvedType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startVoiceActivity(token, intent, resolvedType);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public int startAssistantActivity(IBinder token, Intent intent, String resolvedType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startAssistantActivity(token, intent, resolvedType);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public void setKeepAwake(IBinder token, boolean keepAwake) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(keepAwake ? 1 : 0);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setKeepAwake(token, keepAwake);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public void closeSystemDialogs(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().closeSystemDialogs(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public void finish(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().finish(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public void setDisabledShowContext(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDisabledShowContext(flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public int getDisabledShowContext() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisabledShowContext();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public int getUserDisabledShowContext() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserDisabledShowContext();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public SoundTrigger.KeyphraseSoundModel getKeyphraseSoundModel(int keyphraseId, String bcp47Locale) throws RemoteException {
                SoundTrigger.KeyphraseSoundModel _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(keyphraseId);
                    _data.writeString(bcp47Locale);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getKeyphraseSoundModel(keyphraseId, bcp47Locale);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SoundTrigger.KeyphraseSoundModel.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public int updateKeyphraseSoundModel(SoundTrigger.KeyphraseSoundModel model) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (model != null) {
                        _data.writeInt(1);
                        model.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateKeyphraseSoundModel(model);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public int deleteKeyphraseSoundModel(int keyphraseId, String bcp47Locale) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(keyphraseId);
                    _data.writeString(bcp47Locale);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deleteKeyphraseSoundModel(keyphraseId, bcp47Locale);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public SoundTrigger.ModuleProperties getDspModuleProperties(IVoiceInteractionService service) throws RemoteException {
                SoundTrigger.ModuleProperties _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(service != null ? service.asBinder() : null);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDspModuleProperties(service);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SoundTrigger.ModuleProperties.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public boolean isEnrolledForKeyphrase(IVoiceInteractionService service, int keyphraseId, String bcp47Locale) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(service != null ? service.asBinder() : null);
                    _data.writeInt(keyphraseId);
                    _data.writeString(bcp47Locale);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isEnrolledForKeyphrase(service, keyphraseId, bcp47Locale);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public int startRecognition(IVoiceInteractionService service, int keyphraseId, String bcp47Locale, IRecognitionStatusCallback callback, SoundTrigger.RecognitionConfig recognitionConfig) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(service != null ? service.asBinder() : null);
                    _data.writeInt(keyphraseId);
                    _data.writeString(bcp47Locale);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (recognitionConfig != null) {
                        _data.writeInt(1);
                        recognitionConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startRecognition(service, keyphraseId, bcp47Locale, callback, recognitionConfig);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public int stopRecognition(IVoiceInteractionService service, int keyphraseId, IRecognitionStatusCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(service != null ? service.asBinder() : null);
                    _data.writeInt(keyphraseId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopRecognition(service, keyphraseId, callback);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public ComponentName getActiveServiceComponentName() throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActiveServiceComponentName();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public boolean showSessionForActiveService(Bundle args, int sourceFlags, IVoiceInteractionSessionShowCallback showCallback, IBinder activityToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sourceFlags);
                    _data.writeStrongBinder(showCallback != null ? showCallback.asBinder() : null);
                    _data.writeStrongBinder(activityToken);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().showSessionForActiveService(args, sourceFlags, showCallback, activityToken);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public void hideCurrentSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hideCurrentSession();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public void launchVoiceAssistFromKeyguard() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().launchVoiceAssistFromKeyguard();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public boolean isSessionRunning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSessionRunning();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public boolean activeServiceSupportsAssist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().activeServiceSupportsAssist();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public boolean activeServiceSupportsLaunchFromKeyguard() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().activeServiceSupportsLaunchFromKeyguard();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public void onLockscreenShown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onLockscreenShown();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public void registerVoiceInteractionSessionListener(IVoiceInteractionSessionListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerVoiceInteractionSessionListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public void getActiveServiceSupportedActions(List<String> voiceActions, IVoiceActionCheckCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(voiceActions);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getActiveServiceSupportedActions(voiceActions, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public void setUiHints(IVoiceInteractionService service, Bundle hints) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(service != null ? service.asBinder() : null);
                    if (hints != null) {
                        _data.writeInt(1);
                        hints.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUiHints(service, hints);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public void requestDirectActions(IBinder token, int taskId, IBinder assistToken, RemoteCallback cancellationCallback, RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(taskId);
                    _data.writeStrongBinder(assistToken);
                    if (cancellationCallback != null) {
                        _data.writeInt(1);
                        cancellationCallback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestDirectActions(token, taskId, assistToken, cancellationCallback, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractionManagerService
            public void performDirectAction(IBinder token, String actionId, Bundle arguments, int taskId, IBinder assistToken, RemoteCallback cancellationCallback, RemoteCallback resultCallback) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(token);
                        try {
                            _data.writeString(actionId);
                            if (arguments != null) {
                                _data.writeInt(1);
                                arguments.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            _data.writeInt(taskId);
                            _data.writeStrongBinder(assistToken);
                            if (cancellationCallback != null) {
                                _data.writeInt(1);
                                cancellationCallback.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (resultCallback != null) {
                                _data.writeInt(1);
                                resultCallback.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().performDirectAction(token, actionId, arguments, taskId, assistToken, cancellationCallback, resultCallback);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }
        }

        public static boolean setDefaultImpl(IVoiceInteractionManagerService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVoiceInteractionManagerService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
