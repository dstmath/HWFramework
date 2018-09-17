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
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
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
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
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
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
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
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
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
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
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
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
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
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
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
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
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
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
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
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, _reply, 0);
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
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    if (newSession != null) {
                        iBinder = newSession.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, _reply, 0);
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
                    this.mRemote.transact(12, _data, _reply, 0);
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
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, _reply, 0);
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
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
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
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
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
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, _reply, 0);
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
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    if (newSession != null) {
                        iBinder = newSession.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(17, _data, _reply, 0);
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
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, _reply, 0);
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
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    if (newSession != null) {
                        iBinder = newSession.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(19, _data, _reply, 0);
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
                    this.mRemote.transact(20, _data, _reply, 0);
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
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(21, _data, _reply, 0);
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
                    this.mRemote.transact(22, _data, _reply, 0);
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
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(23, _data, _reply, 0);
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
                        _data.writeInt(1);
                        state.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(24, _data, _reply, 0);
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
                    this.mRemote.transact(25, _data, _reply, 0);
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
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(26, _data, _reply, 0);
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
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(27, _data, _reply, 0);
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
                    this.mRemote.transact(28, _data, _reply, 0);
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
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(29, _data, _reply, 0);
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
                        _data.writeInt(1);
                        suppSrvNotification.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(30, _data, _reply, 0);
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
            ImsCallProfile _arg1;
            ImsReasonInfo _arg12;
            IImsCallSession _arg13;
            ImsCallProfile _arg2;
            int _arg14;
            int _arg22;
            ImsReasonInfo _arg3;
            switch (code) {
                case 1:
                    ImsStreamMediaProfile _arg15;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg15 = (ImsStreamMediaProfile) ImsStreamMediaProfile.CREATOR.createFromParcel(data);
                    } else {
                        _arg15 = null;
                    }
                    callSessionProgressing(_arg0, _arg15);
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    callSessionStarted(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg12 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    callSessionStartFailed(_arg0, _arg12);
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg12 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    callSessionTerminated(_arg0, _arg12);
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    callSessionHeld(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg12 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    callSessionHoldFailed(_arg0, _arg12);
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    callSessionHoldReceived(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    callSessionResumed(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg12 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    callSessionResumeFailed(_arg0, _arg12);
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    callSessionResumeReceived(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    _arg13 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg2 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    callSessionMergeStarted(_arg0, _arg13, _arg2);
                    reply.writeNoException();
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    callSessionMergeComplete(com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg12 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    callSessionMergeFailed(_arg0, _arg12);
                    reply.writeNoException();
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    callSessionUpdated(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg12 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    callSessionUpdateFailed(_arg0, _arg12);
                    reply.writeNoException();
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    callSessionUpdateReceived(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    _arg13 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg2 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    callSessionConferenceExtended(_arg0, _arg13, _arg2);
                    reply.writeNoException();
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg12 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    callSessionConferenceExtendFailed(_arg0, _arg12);
                    reply.writeNoException();
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    _arg13 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg2 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    callSessionConferenceExtendReceived(_arg0, _arg13, _arg2);
                    reply.writeNoException();
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    callSessionInviteParticipantsRequestDelivered(com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg12 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    callSessionInviteParticipantsRequestFailed(_arg0, _arg12);
                    reply.writeNoException();
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    callSessionRemoveParticipantsRequestDelivered(com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg12 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    callSessionRemoveParticipantsRequestFailed(_arg0, _arg12);
                    reply.writeNoException();
                    return true;
                case 24:
                    ImsConferenceState _arg16;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg16 = (ImsConferenceState) ImsConferenceState.CREATOR.createFromParcel(data);
                    } else {
                        _arg16 = null;
                    }
                    callSessionConferenceStateUpdated(_arg0, _arg16);
                    reply.writeNoException();
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    callSessionUssdMessageReceived(com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 26:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    _arg14 = data.readInt();
                    _arg22 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg3 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    callSessionHandover(_arg0, _arg14, _arg22, _arg3);
                    reply.writeNoException();
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    _arg14 = data.readInt();
                    _arg22 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg3 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    callSessionHandoverFailed(_arg0, _arg14, _arg22, _arg3);
                    reply.writeNoException();
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    callSessionTtyModeReceived(com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    callSessionMultipartyStateChanged(com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 30:
                    ImsSuppServiceNotification _arg17;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsCallSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg17 = (ImsSuppServiceNotification) ImsSuppServiceNotification.CREATOR.createFromParcel(data);
                    } else {
                        _arg17 = null;
                    }
                    callSessionSuppServiceReceived(_arg0, _arg17);
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
