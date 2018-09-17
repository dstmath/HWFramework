package com.android.ims.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.ims.ImsCallProfile;
import com.android.ims.ImsConferenceState;
import com.android.ims.ImsReasonInfo;
import com.android.ims.ImsStreamMediaProfile;
import com.android.ims.ImsSuppServiceNotification;

public interface IImsCallSessionListener extends IInterface {

    public static abstract class Stub extends Binder implements IImsCallSessionListener {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsCallSessionListener";
        static final int TRANSACTION_callSessionConferenceExtendFailed = 18;
        static final int TRANSACTION_callSessionConferenceExtendReceived = 19;
        static final int TRANSACTION_callSessionConferenceExtended = 17;
        static final int TRANSACTION_callSessionConferenceStateUpdated = 24;
        static final int TRANSACTION_callSessionHandover = 26;
        static final int TRANSACTION_callSessionHandoverFailed = 27;
        static final int TRANSACTION_callSessionHeld = 5;
        static final int TRANSACTION_callSessionHoldFailed = 6;
        static final int TRANSACTION_callSessionHoldReceived = 7;
        static final int TRANSACTION_callSessionInviteParticipantsRequestDelivered = 20;
        static final int TRANSACTION_callSessionInviteParticipantsRequestFailed = 21;
        static final int TRANSACTION_callSessionMergeComplete = 12;
        static final int TRANSACTION_callSessionMergeFailed = 13;
        static final int TRANSACTION_callSessionMergeStarted = 11;
        static final int TRANSACTION_callSessionMultipartyStateChanged = 29;
        static final int TRANSACTION_callSessionProgressing = 1;
        static final int TRANSACTION_callSessionRemoveParticipantsRequestDelivered = 22;
        static final int TRANSACTION_callSessionRemoveParticipantsRequestFailed = 23;
        static final int TRANSACTION_callSessionResumeFailed = 9;
        static final int TRANSACTION_callSessionResumeReceived = 10;
        static final int TRANSACTION_callSessionResumed = 8;
        static final int TRANSACTION_callSessionStartFailed = 3;
        static final int TRANSACTION_callSessionStarted = 2;
        static final int TRANSACTION_callSessionSuppServiceReceived = 30;
        static final int TRANSACTION_callSessionTerminated = 4;
        static final int TRANSACTION_callSessionTtyModeReceived = 28;
        static final int TRANSACTION_callSessionUpdateFailed = 15;
        static final int TRANSACTION_callSessionUpdateReceived = 16;
        static final int TRANSACTION_callSessionUpdated = 14;
        static final int TRANSACTION_callSessionUssdMessageReceived = 25;

        private static class Proxy implements IImsCallSessionListener {
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

            public void callSessionProgressing(IImsCallSession session, ImsStreamMediaProfile profile) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionProgressing, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionStarted(IImsCallSession session, ImsCallProfile profile) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionStarted, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionStartFailed(IImsCallSession session, ImsReasonInfo reasonInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (reasonInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionStartFailed, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionTerminated(IImsCallSession session, ImsReasonInfo reasonInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (reasonInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionTerminated, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionHeld(IImsCallSession session, ImsCallProfile profile) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionHeld, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionHoldFailed(IImsCallSession session, ImsReasonInfo reasonInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (reasonInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionHoldFailed, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionHoldReceived(IImsCallSession session, ImsCallProfile profile) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionHoldReceived, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionResumed(IImsCallSession session, ImsCallProfile profile) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionResumed, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionResumeFailed(IImsCallSession session, ImsReasonInfo reasonInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (reasonInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionResumeFailed, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionResumeReceived(IImsCallSession session, ImsCallProfile profile) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionResumeReceived, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionMergeStarted(IImsCallSession session, IImsCallSession newSession, ImsCallProfile profile) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        asBinder = session.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    if (newSession != null) {
                        iBinder = newSession.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionMergeStarted, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionMergeComplete(IImsCallSession session) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_callSessionMergeComplete, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionMergeFailed(IImsCallSession session, ImsReasonInfo reasonInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (reasonInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionMergeFailed, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionUpdated(IImsCallSession session, ImsCallProfile profile) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionUpdated, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionUpdateFailed(IImsCallSession session, ImsReasonInfo reasonInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (reasonInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionUpdateFailed, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionUpdateReceived(IImsCallSession session, ImsCallProfile profile) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionUpdateReceived, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionConferenceExtended(IImsCallSession session, IImsCallSession newSession, ImsCallProfile profile) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        asBinder = session.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    if (newSession != null) {
                        iBinder = newSession.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionConferenceExtended, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionConferenceExtendFailed(IImsCallSession session, ImsReasonInfo reasonInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (reasonInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionConferenceExtendFailed, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionConferenceExtendReceived(IImsCallSession session, IImsCallSession newSession, ImsCallProfile profile) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        asBinder = session.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    if (newSession != null) {
                        iBinder = newSession.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionConferenceExtendReceived, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionInviteParticipantsRequestDelivered(IImsCallSession session) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_callSessionInviteParticipantsRequestDelivered, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionInviteParticipantsRequestFailed(IImsCallSession session, ImsReasonInfo reasonInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (reasonInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionInviteParticipantsRequestFailed, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionRemoveParticipantsRequestDelivered(IImsCallSession session) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_callSessionRemoveParticipantsRequestDelivered, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionRemoveParticipantsRequestFailed(IImsCallSession session, ImsReasonInfo reasonInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (reasonInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionRemoveParticipantsRequestFailed, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionConferenceStateUpdated(IImsCallSession session, ImsConferenceState state) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (state != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        state.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionConferenceStateUpdated, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionUssdMessageReceived(IImsCallSession session, int mode, String ussdMessage) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(mode);
                    _data.writeString(ussdMessage);
                    this.mRemote.transact(Stub.TRANSACTION_callSessionUssdMessageReceived, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionHandover(IImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(srcAccessTech);
                    _data.writeInt(targetAccessTech);
                    if (reasonInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionHandover, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionHandoverFailed(IImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(srcAccessTech);
                    _data.writeInt(targetAccessTech);
                    if (reasonInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionHandoverFailed, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionTtyModeReceived(IImsCallSession session, int mode) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(mode);
                    this.mRemote.transact(Stub.TRANSACTION_callSessionTtyModeReceived, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionMultipartyStateChanged(IImsCallSession session, boolean isMultiParty) throws RemoteException {
                IBinder iBinder = null;
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (isMultiParty) {
                        i = Stub.TRANSACTION_callSessionProgressing;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_callSessionMultipartyStateChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void callSessionSuppServiceReceived(IImsCallSession session, ImsSuppServiceNotification suppSrvNotification) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (suppSrvNotification != null) {
                        _data.writeInt(Stub.TRANSACTION_callSessionProgressing);
                        suppSrvNotification.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callSessionSuppServiceReceived, _data, _reply, 0);
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

        public static IImsCallSessionListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsCallSessionListener)) {
                return new Proxy(obj);
            }
            return (IImsCallSessionListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IImsCallSession _arg0;
            ImsCallProfile imsCallProfile;
            ImsReasonInfo imsReasonInfo;
            IImsCallSession _arg1;
            ImsCallProfile imsCallProfile2;
            int _arg12;
            int _arg2;
            ImsReasonInfo imsReasonInfo2;
            switch (code) {
                case TRANSACTION_callSessionProgressing /*1*/:
                    ImsStreamMediaProfile imsStreamMediaProfile;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsStreamMediaProfile = (ImsStreamMediaProfile) ImsStreamMediaProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsStreamMediaProfile = null;
                    }
                    callSessionProgressing(_arg0, imsStreamMediaProfile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionStarted /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsCallProfile = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsCallProfile = null;
                    }
                    callSessionStarted(_arg0, imsCallProfile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionStartFailed /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsReasonInfo = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        imsReasonInfo = null;
                    }
                    callSessionStartFailed(_arg0, imsReasonInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionTerminated /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsReasonInfo = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        imsReasonInfo = null;
                    }
                    callSessionTerminated(_arg0, imsReasonInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionHeld /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsCallProfile = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsCallProfile = null;
                    }
                    callSessionHeld(_arg0, imsCallProfile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionHoldFailed /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsReasonInfo = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        imsReasonInfo = null;
                    }
                    callSessionHoldFailed(_arg0, imsReasonInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionHoldReceived /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsCallProfile = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsCallProfile = null;
                    }
                    callSessionHoldReceived(_arg0, imsCallProfile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionResumed /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsCallProfile = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsCallProfile = null;
                    }
                    callSessionResumed(_arg0, imsCallProfile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionResumeFailed /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsReasonInfo = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        imsReasonInfo = null;
                    }
                    callSessionResumeFailed(_arg0, imsReasonInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionResumeReceived /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsCallProfile = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsCallProfile = null;
                    }
                    callSessionResumeReceived(_arg0, imsCallProfile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionMergeStarted /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    _arg1 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsCallProfile2 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsCallProfile2 = null;
                    }
                    callSessionMergeStarted(_arg0, _arg1, imsCallProfile2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionMergeComplete /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    callSessionMergeComplete(com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionMergeFailed /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsReasonInfo = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        imsReasonInfo = null;
                    }
                    callSessionMergeFailed(_arg0, imsReasonInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionUpdated /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsCallProfile = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsCallProfile = null;
                    }
                    callSessionUpdated(_arg0, imsCallProfile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionUpdateFailed /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsReasonInfo = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        imsReasonInfo = null;
                    }
                    callSessionUpdateFailed(_arg0, imsReasonInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionUpdateReceived /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsCallProfile = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsCallProfile = null;
                    }
                    callSessionUpdateReceived(_arg0, imsCallProfile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionConferenceExtended /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    _arg1 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsCallProfile2 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsCallProfile2 = null;
                    }
                    callSessionConferenceExtended(_arg0, _arg1, imsCallProfile2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionConferenceExtendFailed /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsReasonInfo = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        imsReasonInfo = null;
                    }
                    callSessionConferenceExtendFailed(_arg0, imsReasonInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionConferenceExtendReceived /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    _arg1 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsCallProfile2 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsCallProfile2 = null;
                    }
                    callSessionConferenceExtendReceived(_arg0, _arg1, imsCallProfile2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionInviteParticipantsRequestDelivered /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    callSessionInviteParticipantsRequestDelivered(com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionInviteParticipantsRequestFailed /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsReasonInfo = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        imsReasonInfo = null;
                    }
                    callSessionInviteParticipantsRequestFailed(_arg0, imsReasonInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionRemoveParticipantsRequestDelivered /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    callSessionRemoveParticipantsRequestDelivered(com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionRemoveParticipantsRequestFailed /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsReasonInfo = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        imsReasonInfo = null;
                    }
                    callSessionRemoveParticipantsRequestFailed(_arg0, imsReasonInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionConferenceStateUpdated /*24*/:
                    ImsConferenceState imsConferenceState;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsConferenceState = (ImsConferenceState) ImsConferenceState.CREATOR.createFromParcel(data);
                    } else {
                        imsConferenceState = null;
                    }
                    callSessionConferenceStateUpdated(_arg0, imsConferenceState);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionUssdMessageReceived /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    callSessionUssdMessageReceived(com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionHandover /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    _arg12 = data.readInt();
                    _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        imsReasonInfo2 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        imsReasonInfo2 = null;
                    }
                    callSessionHandover(_arg0, _arg12, _arg2, imsReasonInfo2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionHandoverFailed /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    _arg12 = data.readInt();
                    _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        imsReasonInfo2 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        imsReasonInfo2 = null;
                    }
                    callSessionHandoverFailed(_arg0, _arg12, _arg2, imsReasonInfo2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionTtyModeReceived /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    callSessionTtyModeReceived(com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionMultipartyStateChanged /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    callSessionMultipartyStateChanged(com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callSessionSuppServiceReceived /*30*/:
                    ImsSuppServiceNotification imsSuppServiceNotification;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        imsSuppServiceNotification = (ImsSuppServiceNotification) ImsSuppServiceNotification.CREATOR.createFromParcel(data);
                    } else {
                        imsSuppServiceNotification = null;
                    }
                    callSessionSuppServiceReceived(_arg0, imsSuppServiceNotification);
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

    void callSessionConferenceExtendFailed(IImsCallSession iImsCallSession, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionConferenceExtendReceived(IImsCallSession iImsCallSession, IImsCallSession iImsCallSession2, ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionConferenceExtended(IImsCallSession iImsCallSession, IImsCallSession iImsCallSession2, ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionConferenceStateUpdated(IImsCallSession iImsCallSession, ImsConferenceState imsConferenceState) throws RemoteException;

    void callSessionHandover(IImsCallSession iImsCallSession, int i, int i2, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionHandoverFailed(IImsCallSession iImsCallSession, int i, int i2, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionHeld(IImsCallSession iImsCallSession, ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionHoldFailed(IImsCallSession iImsCallSession, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionHoldReceived(IImsCallSession iImsCallSession, ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionInviteParticipantsRequestDelivered(IImsCallSession iImsCallSession) throws RemoteException;

    void callSessionInviteParticipantsRequestFailed(IImsCallSession iImsCallSession, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionMergeComplete(IImsCallSession iImsCallSession) throws RemoteException;

    void callSessionMergeFailed(IImsCallSession iImsCallSession, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionMergeStarted(IImsCallSession iImsCallSession, IImsCallSession iImsCallSession2, ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionMultipartyStateChanged(IImsCallSession iImsCallSession, boolean z) throws RemoteException;

    void callSessionProgressing(IImsCallSession iImsCallSession, ImsStreamMediaProfile imsStreamMediaProfile) throws RemoteException;

    void callSessionRemoveParticipantsRequestDelivered(IImsCallSession iImsCallSession) throws RemoteException;

    void callSessionRemoveParticipantsRequestFailed(IImsCallSession iImsCallSession, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionResumeFailed(IImsCallSession iImsCallSession, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionResumeReceived(IImsCallSession iImsCallSession, ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionResumed(IImsCallSession iImsCallSession, ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionStartFailed(IImsCallSession iImsCallSession, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionStarted(IImsCallSession iImsCallSession, ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionSuppServiceReceived(IImsCallSession iImsCallSession, ImsSuppServiceNotification imsSuppServiceNotification) throws RemoteException;

    void callSessionTerminated(IImsCallSession iImsCallSession, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionTtyModeReceived(IImsCallSession iImsCallSession, int i) throws RemoteException;

    void callSessionUpdateFailed(IImsCallSession iImsCallSession, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionUpdateReceived(IImsCallSession iImsCallSession, ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionUpdated(IImsCallSession iImsCallSession, ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionUssdMessageReceived(IImsCallSession iImsCallSession, int i, String str) throws RemoteException;
}
