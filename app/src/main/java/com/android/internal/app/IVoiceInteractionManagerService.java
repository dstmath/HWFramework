package com.android.internal.app;

import android.content.ComponentName;
import android.content.Intent;
import android.hardware.soundtrigger.IRecognitionStatusCallback;
import android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel;
import android.hardware.soundtrigger.SoundTrigger.ModuleProperties;
import android.hardware.soundtrigger.SoundTrigger.RecognitionConfig;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.service.voice.IVoiceInteractionService;
import android.service.voice.IVoiceInteractionSession;

public interface IVoiceInteractionManagerService extends IInterface {

    public static abstract class Stub extends Binder implements IVoiceInteractionManagerService {
        private static final String DESCRIPTOR = "com.android.internal.app.IVoiceInteractionManagerService";
        static final int TRANSACTION_activeServiceSupportsAssist = 24;
        static final int TRANSACTION_activeServiceSupportsLaunchFromKeyguard = 25;
        static final int TRANSACTION_closeSystemDialogs = 7;
        static final int TRANSACTION_deleteKeyphraseSoundModel = 14;
        static final int TRANSACTION_deliverNewSession = 2;
        static final int TRANSACTION_finish = 8;
        static final int TRANSACTION_getActiveServiceComponentName = 19;
        static final int TRANSACTION_getDisabledShowContext = 10;
        static final int TRANSACTION_getDspModuleProperties = 15;
        static final int TRANSACTION_getKeyphraseSoundModel = 12;
        static final int TRANSACTION_getUserDisabledShowContext = 11;
        static final int TRANSACTION_hideCurrentSession = 21;
        static final int TRANSACTION_hideSessionFromSession = 4;
        static final int TRANSACTION_isEnrolledForKeyphrase = 16;
        static final int TRANSACTION_isSessionRunning = 23;
        static final int TRANSACTION_launchVoiceAssistFromKeyguard = 22;
        static final int TRANSACTION_onLockscreenShown = 26;
        static final int TRANSACTION_setDisabledShowContext = 9;
        static final int TRANSACTION_setKeepAwake = 6;
        static final int TRANSACTION_showSession = 1;
        static final int TRANSACTION_showSessionForActiveService = 20;
        static final int TRANSACTION_showSessionFromSession = 3;
        static final int TRANSACTION_startRecognition = 17;
        static final int TRANSACTION_startVoiceActivity = 5;
        static final int TRANSACTION_stopRecognition = 18;
        static final int TRANSACTION_updateKeyphraseSoundModel = 13;

        private static class Proxy implements IVoiceInteractionManagerService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void showSession(IVoiceInteractionService service, Bundle sessionArgs, int flags) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (service != null) {
                        iBinder = service.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (sessionArgs != null) {
                        _data.writeInt(Stub.TRANSACTION_showSession);
                        sessionArgs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_showSession, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean deliverNewSession(IBinder token, IVoiceInteractionSession session, IVoiceInteractor interactor) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (session != null) {
                        asBinder = session.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    if (interactor != null) {
                        iBinder = interactor.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_deliverNewSession, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean showSessionFromSession(IBinder token, Bundle sessionArgs, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (sessionArgs != null) {
                        _data.writeInt(Stub.TRANSACTION_showSession);
                        sessionArgs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_showSessionFromSession, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hideSessionFromSession(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_hideSessionFromSession, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startVoiceActivity(IBinder token, Intent intent, String resolvedType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_showSession);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    this.mRemote.transact(Stub.TRANSACTION_startVoiceActivity, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setKeepAwake(IBinder token, boolean keepAwake) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (keepAwake) {
                        i = Stub.TRANSACTION_showSession;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setKeepAwake, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void closeSystemDialogs(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_closeSystemDialogs, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finish(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_finish, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDisabledShowContext(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_setDisabledShowContext, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDisabledShowContext() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDisabledShowContext, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUserDisabledShowContext() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getUserDisabledShowContext, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public KeyphraseSoundModel getKeyphraseSoundModel(int keyphraseId, String bcp47Locale) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    KeyphraseSoundModel keyphraseSoundModel;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(keyphraseId);
                    _data.writeString(bcp47Locale);
                    this.mRemote.transact(Stub.TRANSACTION_getKeyphraseSoundModel, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        keyphraseSoundModel = (KeyphraseSoundModel) KeyphraseSoundModel.CREATOR.createFromParcel(_reply);
                    } else {
                        keyphraseSoundModel = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return keyphraseSoundModel;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateKeyphraseSoundModel(KeyphraseSoundModel model) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (model != null) {
                        _data.writeInt(Stub.TRANSACTION_showSession);
                        model.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateKeyphraseSoundModel, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteKeyphraseSoundModel(int keyphraseId, String bcp47Locale) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(keyphraseId);
                    _data.writeString(bcp47Locale);
                    this.mRemote.transact(Stub.TRANSACTION_deleteKeyphraseSoundModel, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ModuleProperties getDspModuleProperties(IVoiceInteractionService service) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ModuleProperties moduleProperties;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (service != null) {
                        iBinder = service.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_getDspModuleProperties, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        moduleProperties = (ModuleProperties) ModuleProperties.CREATOR.createFromParcel(_reply);
                    } else {
                        moduleProperties = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return moduleProperties;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isEnrolledForKeyphrase(IVoiceInteractionService service, int keyphraseId, String bcp47Locale) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (service != null) {
                        iBinder = service.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(keyphraseId);
                    _data.writeString(bcp47Locale);
                    this.mRemote.transact(Stub.TRANSACTION_isEnrolledForKeyphrase, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startRecognition(IVoiceInteractionService service, int keyphraseId, String bcp47Locale, IRecognitionStatusCallback callback, RecognitionConfig recognitionConfig) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (service != null) {
                        asBinder = service.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    _data.writeInt(keyphraseId);
                    _data.writeString(bcp47Locale);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (recognitionConfig != null) {
                        _data.writeInt(Stub.TRANSACTION_showSession);
                        recognitionConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startRecognition, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int stopRecognition(IVoiceInteractionService service, int keyphraseId, IRecognitionStatusCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (service != null) {
                        asBinder = service.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    _data.writeInt(keyphraseId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_stopRecognition, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName getActiveServiceComponentName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ComponentName componentName;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveServiceComponentName, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        componentName = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return componentName;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean showSessionForActiveService(Bundle args, int sourceFlags, IVoiceInteractionSessionShowCallback showCallback, IBinder activityToken) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (args != null) {
                        _data.writeInt(Stub.TRANSACTION_showSession);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sourceFlags);
                    if (showCallback != null) {
                        iBinder = showCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeStrongBinder(activityToken);
                    this.mRemote.transact(Stub.TRANSACTION_showSessionForActiveService, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void hideCurrentSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_hideCurrentSession, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void launchVoiceAssistFromKeyguard() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_launchVoiceAssistFromKeyguard, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSessionRunning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isSessionRunning, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean activeServiceSupportsAssist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_activeServiceSupportsAssist, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean activeServiceSupportsLaunchFromKeyguard() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_activeServiceSupportsLaunchFromKeyguard, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onLockscreenShown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onLockscreenShown, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IVoiceInteractionService _arg0;
            Bundle bundle;
            boolean _result;
            IBinder _arg02;
            int _result2;
            switch (code) {
                case TRANSACTION_showSession /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.service.voice.IVoiceInteractionService.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    showSession(_arg0, bundle, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_deliverNewSession /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = deliverNewSession(data.readStrongBinder(), android.service.voice.IVoiceInteractionSession.Stub.asInterface(data.readStrongBinder()), com.android.internal.app.IVoiceInteractor.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_showSession : 0);
                    return true;
                case TRANSACTION_showSessionFromSession /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = showSessionFromSession(_arg02, bundle, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_showSession : 0);
                    return true;
                case TRANSACTION_hideSessionFromSession /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hideSessionFromSession(data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_showSession : 0);
                    return true;
                case TRANSACTION_startVoiceActivity /*5*/:
                    Intent intent;
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    _result2 = startVoiceActivity(_arg02, intent, data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setKeepAwake /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    setKeepAwake(data.readStrongBinder(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_closeSystemDialogs /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    closeSystemDialogs(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_finish /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    finish(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setDisabledShowContext /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDisabledShowContext(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getDisabledShowContext /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDisabledShowContext();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getUserDisabledShowContext /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getUserDisabledShowContext();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getKeyphraseSoundModel /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    KeyphraseSoundModel _result3 = getKeyphraseSoundModel(data.readInt(), data.readString());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_showSession);
                        _result3.writeToParcel(reply, TRANSACTION_showSession);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_updateKeyphraseSoundModel /*13*/:
                    KeyphraseSoundModel keyphraseSoundModel;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        keyphraseSoundModel = (KeyphraseSoundModel) KeyphraseSoundModel.CREATOR.createFromParcel(data);
                    } else {
                        keyphraseSoundModel = null;
                    }
                    _result2 = updateKeyphraseSoundModel(keyphraseSoundModel);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_deleteKeyphraseSoundModel /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = deleteKeyphraseSoundModel(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getDspModuleProperties /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    ModuleProperties _result4 = getDspModuleProperties(android.service.voice.IVoiceInteractionService.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_showSession);
                        _result4.writeToParcel(reply, TRANSACTION_showSession);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_isEnrolledForKeyphrase /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isEnrolledForKeyphrase(android.service.voice.IVoiceInteractionService.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_showSession : 0);
                    return true;
                case TRANSACTION_startRecognition /*17*/:
                    RecognitionConfig recognitionConfig;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.service.voice.IVoiceInteractionService.Stub.asInterface(data.readStrongBinder());
                    int _arg1 = data.readInt();
                    String _arg2 = data.readString();
                    IRecognitionStatusCallback _arg3 = android.hardware.soundtrigger.IRecognitionStatusCallback.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        recognitionConfig = (RecognitionConfig) RecognitionConfig.CREATOR.createFromParcel(data);
                    } else {
                        recognitionConfig = null;
                    }
                    _result2 = startRecognition(_arg0, _arg1, _arg2, _arg3, recognitionConfig);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_stopRecognition /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = stopRecognition(android.service.voice.IVoiceInteractionService.Stub.asInterface(data.readStrongBinder()), data.readInt(), android.hardware.soundtrigger.IRecognitionStatusCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getActiveServiceComponentName /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    ComponentName _result5 = getActiveServiceComponentName();
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_showSession);
                        _result5.writeToParcel(reply, TRANSACTION_showSession);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_showSessionForActiveService /*20*/:
                    Bundle bundle2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    _result = showSessionForActiveService(bundle2, data.readInt(), com.android.internal.app.IVoiceInteractionSessionShowCallback.Stub.asInterface(data.readStrongBinder()), data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_showSession : 0);
                    return true;
                case TRANSACTION_hideCurrentSession /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    hideCurrentSession();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_launchVoiceAssistFromKeyguard /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    launchVoiceAssistFromKeyguard();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isSessionRunning /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSessionRunning();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_showSession : 0);
                    return true;
                case TRANSACTION_activeServiceSupportsAssist /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = activeServiceSupportsAssist();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_showSession : 0);
                    return true;
                case TRANSACTION_activeServiceSupportsLaunchFromKeyguard /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = activeServiceSupportsLaunchFromKeyguard();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_showSession : 0);
                    return true;
                case TRANSACTION_onLockscreenShown /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    onLockscreenShown();
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean activeServiceSupportsAssist() throws RemoteException;

    boolean activeServiceSupportsLaunchFromKeyguard() throws RemoteException;

    void closeSystemDialogs(IBinder iBinder) throws RemoteException;

    int deleteKeyphraseSoundModel(int i, String str) throws RemoteException;

    boolean deliverNewSession(IBinder iBinder, IVoiceInteractionSession iVoiceInteractionSession, IVoiceInteractor iVoiceInteractor) throws RemoteException;

    void finish(IBinder iBinder) throws RemoteException;

    ComponentName getActiveServiceComponentName() throws RemoteException;

    int getDisabledShowContext() throws RemoteException;

    ModuleProperties getDspModuleProperties(IVoiceInteractionService iVoiceInteractionService) throws RemoteException;

    KeyphraseSoundModel getKeyphraseSoundModel(int i, String str) throws RemoteException;

    int getUserDisabledShowContext() throws RemoteException;

    void hideCurrentSession() throws RemoteException;

    boolean hideSessionFromSession(IBinder iBinder) throws RemoteException;

    boolean isEnrolledForKeyphrase(IVoiceInteractionService iVoiceInteractionService, int i, String str) throws RemoteException;

    boolean isSessionRunning() throws RemoteException;

    void launchVoiceAssistFromKeyguard() throws RemoteException;

    void onLockscreenShown() throws RemoteException;

    void setDisabledShowContext(int i) throws RemoteException;

    void setKeepAwake(IBinder iBinder, boolean z) throws RemoteException;

    void showSession(IVoiceInteractionService iVoiceInteractionService, Bundle bundle, int i) throws RemoteException;

    boolean showSessionForActiveService(Bundle bundle, int i, IVoiceInteractionSessionShowCallback iVoiceInteractionSessionShowCallback, IBinder iBinder) throws RemoteException;

    boolean showSessionFromSession(IBinder iBinder, Bundle bundle, int i) throws RemoteException;

    int startRecognition(IVoiceInteractionService iVoiceInteractionService, int i, String str, IRecognitionStatusCallback iRecognitionStatusCallback, RecognitionConfig recognitionConfig) throws RemoteException;

    int startVoiceActivity(IBinder iBinder, Intent intent, String str) throws RemoteException;

    int stopRecognition(IVoiceInteractionService iVoiceInteractionService, int i, IRecognitionStatusCallback iRecognitionStatusCallback) throws RemoteException;

    int updateKeyphraseSoundModel(KeyphraseSoundModel keyphraseSoundModel) throws RemoteException;
}
