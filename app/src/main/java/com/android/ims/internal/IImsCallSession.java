package com.android.ims.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.ims.ImsCallProfile;
import com.android.ims.ImsStreamMediaProfile;

public interface IImsCallSession extends IInterface {

    public static abstract class Stub extends Binder implements IImsCallSession {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsCallSession";
        static final int TRANSACTION_accept = 13;
        static final int TRANSACTION_close = 1;
        static final int TRANSACTION_extendToConference = 20;
        static final int TRANSACTION_getCallId = 2;
        static final int TRANSACTION_getCallProfile = 3;
        static final int TRANSACTION_getLocalCallProfile = 4;
        static final int TRANSACTION_getProperty = 6;
        static final int TRANSACTION_getRemoteCallProfile = 5;
        static final int TRANSACTION_getState = 7;
        static final int TRANSACTION_getVideoCallProvider = 27;
        static final int TRANSACTION_hold = 16;
        static final int TRANSACTION_inviteParticipants = 21;
        static final int TRANSACTION_isInCall = 8;
        static final int TRANSACTION_isMultiparty = 28;
        static final int TRANSACTION_merge = 18;
        static final int TRANSACTION_reject = 14;
        static final int TRANSACTION_removeParticipants = 22;
        static final int TRANSACTION_resume = 17;
        static final int TRANSACTION_sendDtmf = 23;
        static final int TRANSACTION_sendUssd = 26;
        static final int TRANSACTION_setListener = 9;
        static final int TRANSACTION_setMute = 10;
        static final int TRANSACTION_start = 11;
        static final int TRANSACTION_startConference = 12;
        static final int TRANSACTION_startDtmf = 24;
        static final int TRANSACTION_stopDtmf = 25;
        static final int TRANSACTION_terminate = 15;
        static final int TRANSACTION_update = 19;

        private static class Proxy implements IImsCallSession {
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

            public void close() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_close, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCallId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCallId, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ImsCallProfile getCallProfile() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ImsCallProfile imsCallProfile;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCallProfile, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        imsCallProfile = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(_reply);
                    } else {
                        imsCallProfile = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return imsCallProfile;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ImsCallProfile getLocalCallProfile() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ImsCallProfile imsCallProfile;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getLocalCallProfile, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        imsCallProfile = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(_reply);
                    } else {
                        imsCallProfile = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return imsCallProfile;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ImsCallProfile getRemoteCallProfile() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ImsCallProfile imsCallProfile;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getRemoteCallProfile, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        imsCallProfile = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(_reply);
                    } else {
                        imsCallProfile = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return imsCallProfile;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getProperty(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    this.mRemote.transact(Stub.TRANSACTION_getProperty, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isInCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isInCall, _data, _reply, 0);
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

            public void setListener(IImsCallSessionListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMute(boolean muted) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (muted) {
                        i = Stub.TRANSACTION_close;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setMute, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void start(String callee, ImsCallProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callee);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_close);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_start, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startConference(String[] participants, ImsCallProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(participants);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_close);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startConference, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void accept(int callType, ImsStreamMediaProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callType);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_close);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_accept, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reject(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    this.mRemote.transact(Stub.TRANSACTION_reject, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void terminate(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    this.mRemote.transact(Stub.TRANSACTION_terminate, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void hold(ImsStreamMediaProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_close);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_hold, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resume(ImsStreamMediaProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_close);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_resume, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void merge() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_merge, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void update(int callType, ImsStreamMediaProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callType);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_close);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_update, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void extendToConference(String[] participants) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(participants);
                    this.mRemote.transact(Stub.TRANSACTION_extendToConference, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void inviteParticipants(String[] participants) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(participants);
                    this.mRemote.transact(Stub.TRANSACTION_inviteParticipants, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeParticipants(String[] participants) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(participants);
                    this.mRemote.transact(Stub.TRANSACTION_removeParticipants, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendDtmf(char c, Message result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(c);
                    if (result != null) {
                        _data.writeInt(Stub.TRANSACTION_close);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendDtmf, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startDtmf(char c) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(c);
                    this.mRemote.transact(Stub.TRANSACTION_startDtmf, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopDtmf() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopDtmf, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendUssd(String ussdMessage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ussdMessage);
                    this.mRemote.transact(Stub.TRANSACTION_sendUssd, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsVideoCallProvider getVideoCallProvider() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getVideoCallProvider, _data, _reply, 0);
                    _reply.readException();
                    IImsVideoCallProvider _result = com.android.ims.internal.IImsVideoCallProvider.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isMultiparty() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isMultiparty, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsCallSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsCallSession)) {
                return new Proxy(obj);
            }
            return (IImsCallSession) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _result;
            ImsCallProfile _result2;
            boolean _result3;
            ImsCallProfile imsCallProfile;
            int _arg0;
            ImsStreamMediaProfile imsStreamMediaProfile;
            ImsStreamMediaProfile imsStreamMediaProfile2;
            switch (code) {
                case TRANSACTION_close /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    close();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getCallId /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getCallId();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_getCallProfile /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getCallProfile();
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_close);
                        _result2.writeToParcel(reply, TRANSACTION_close);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getLocalCallProfile /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getLocalCallProfile();
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_close);
                        _result2.writeToParcel(reply, TRANSACTION_close);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getRemoteCallProfile /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getRemoteCallProfile();
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_close);
                        _result2.writeToParcel(reply, TRANSACTION_close);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getProperty /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getProperty(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_getState /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result4 = getState();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_isInCall /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isInCall();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_close : 0);
                    return true;
                case TRANSACTION_setListener /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    setListener(com.android.ims.internal.IImsCallSessionListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setMute /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    setMute(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_start /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    if (data.readInt() != 0) {
                        imsCallProfile = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsCallProfile = null;
                    }
                    start(_arg02, imsCallProfile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startConference /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    String[] _arg03 = data.createStringArray();
                    if (data.readInt() != 0) {
                        imsCallProfile = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsCallProfile = null;
                    }
                    startConference(_arg03, imsCallProfile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_accept /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        imsStreamMediaProfile = (ImsStreamMediaProfile) ImsStreamMediaProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsStreamMediaProfile = null;
                    }
                    accept(_arg0, imsStreamMediaProfile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_reject /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    reject(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_terminate /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    terminate(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_hold /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        imsStreamMediaProfile2 = (ImsStreamMediaProfile) ImsStreamMediaProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsStreamMediaProfile2 = null;
                    }
                    hold(imsStreamMediaProfile2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_resume /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        imsStreamMediaProfile2 = (ImsStreamMediaProfile) ImsStreamMediaProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsStreamMediaProfile2 = null;
                    }
                    resume(imsStreamMediaProfile2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_merge /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    merge();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_update /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        imsStreamMediaProfile = (ImsStreamMediaProfile) ImsStreamMediaProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsStreamMediaProfile = null;
                    }
                    update(_arg0, imsStreamMediaProfile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_extendToConference /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    extendToConference(data.createStringArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_inviteParticipants /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    inviteParticipants(data.createStringArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeParticipants /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeParticipants(data.createStringArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendDtmf /*23*/:
                    Message message;
                    data.enforceInterface(DESCRIPTOR);
                    char _arg04 = (char) data.readInt();
                    if (data.readInt() != 0) {
                        message = (Message) Message.CREATOR.createFromParcel(data);
                    } else {
                        message = null;
                    }
                    sendDtmf(_arg04, message);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startDtmf /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    startDtmf((char) data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopDtmf /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopDtmf();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendUssd /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    sendUssd(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getVideoCallProvider /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    IImsVideoCallProvider _result5 = getVideoCallProvider();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result5 != null ? _result5.asBinder() : null);
                    return true;
                case TRANSACTION_isMultiparty /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isMultiparty();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_close : 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void accept(int i, ImsStreamMediaProfile imsStreamMediaProfile) throws RemoteException;

    void close() throws RemoteException;

    void extendToConference(String[] strArr) throws RemoteException;

    String getCallId() throws RemoteException;

    ImsCallProfile getCallProfile() throws RemoteException;

    ImsCallProfile getLocalCallProfile() throws RemoteException;

    String getProperty(String str) throws RemoteException;

    ImsCallProfile getRemoteCallProfile() throws RemoteException;

    int getState() throws RemoteException;

    IImsVideoCallProvider getVideoCallProvider() throws RemoteException;

    void hold(ImsStreamMediaProfile imsStreamMediaProfile) throws RemoteException;

    void inviteParticipants(String[] strArr) throws RemoteException;

    boolean isInCall() throws RemoteException;

    boolean isMultiparty() throws RemoteException;

    void merge() throws RemoteException;

    void reject(int i) throws RemoteException;

    void removeParticipants(String[] strArr) throws RemoteException;

    void resume(ImsStreamMediaProfile imsStreamMediaProfile) throws RemoteException;

    void sendDtmf(char c, Message message) throws RemoteException;

    void sendUssd(String str) throws RemoteException;

    void setListener(IImsCallSessionListener iImsCallSessionListener) throws RemoteException;

    void setMute(boolean z) throws RemoteException;

    void start(String str, ImsCallProfile imsCallProfile) throws RemoteException;

    void startConference(String[] strArr, ImsCallProfile imsCallProfile) throws RemoteException;

    void startDtmf(char c) throws RemoteException;

    void stopDtmf() throws RemoteException;

    void terminate(int i) throws RemoteException;

    void update(int i, ImsStreamMediaProfile imsStreamMediaProfile) throws RemoteException;
}
