package android.telephony.ims.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsConferenceState;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.ImsSuppServiceNotification;
import com.android.ims.internal.IImsCallSession;

public interface IImsCallSessionListener extends IInterface {

    public static abstract class Stub extends Binder implements IImsCallSessionListener {
        private static final String DESCRIPTOR = "android.telephony.ims.aidl.IImsCallSessionListener";
        static final int TRANSACTION_callSessionConferenceExtendFailed = 18;
        static final int TRANSACTION_callSessionConferenceExtendReceived = 19;
        static final int TRANSACTION_callSessionConferenceExtended = 17;
        static final int TRANSACTION_callSessionConferenceStateUpdated = 24;
        static final int TRANSACTION_callSessionHandover = 26;
        static final int TRANSACTION_callSessionHandoverFailed = 27;
        static final int TRANSACTION_callSessionHeld = 5;
        static final int TRANSACTION_callSessionHoldFailed = 6;
        static final int TRANSACTION_callSessionHoldReceived = 7;
        static final int TRANSACTION_callSessionInitiated = 2;
        static final int TRANSACTION_callSessionInitiatedFailed = 3;
        static final int TRANSACTION_callSessionInviteParticipantsRequestDelivered = 20;
        static final int TRANSACTION_callSessionInviteParticipantsRequestFailed = 21;
        static final int TRANSACTION_callSessionMayHandover = 28;
        static final int TRANSACTION_callSessionMergeComplete = 12;
        static final int TRANSACTION_callSessionMergeFailed = 13;
        static final int TRANSACTION_callSessionMergeStarted = 11;
        static final int TRANSACTION_callSessionMultipartyStateChanged = 30;
        static final int TRANSACTION_callSessionProgressing = 1;
        static final int TRANSACTION_callSessionRemoveParticipantsRequestDelivered = 22;
        static final int TRANSACTION_callSessionRemoveParticipantsRequestFailed = 23;
        static final int TRANSACTION_callSessionResumeFailed = 9;
        static final int TRANSACTION_callSessionResumeReceived = 10;
        static final int TRANSACTION_callSessionResumed = 8;
        static final int TRANSACTION_callSessionRttMessageReceived = 34;
        static final int TRANSACTION_callSessionRttModifyRequestReceived = 32;
        static final int TRANSACTION_callSessionRttModifyResponseReceived = 33;
        static final int TRANSACTION_callSessionSuppServiceReceived = 31;
        static final int TRANSACTION_callSessionTerminated = 4;
        static final int TRANSACTION_callSessionTtyModeReceived = 29;
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

            public void callSessionProgressing(ImsStreamMediaProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionInitiated(ImsCallProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionInitiatedFailed(ImsReasonInfo reasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reasonInfo != null) {
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionTerminated(ImsReasonInfo reasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reasonInfo != null) {
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionHeld(ImsCallProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionHoldFailed(ImsReasonInfo reasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reasonInfo != null) {
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionHoldReceived(ImsCallProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionResumed(ImsCallProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionResumeFailed(ImsReasonInfo reasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reasonInfo != null) {
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionResumeReceived(ImsCallProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionMergeStarted(IImsCallSession newSession, ImsCallProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(newSession != null ? newSession.asBinder() : null);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionMergeComplete(IImsCallSession session) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionMergeFailed(ImsReasonInfo reasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reasonInfo != null) {
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionUpdated(ImsCallProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionUpdateFailed(ImsReasonInfo reasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reasonInfo != null) {
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionUpdateReceived(ImsCallProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionConferenceExtended(IImsCallSession newSession, ImsCallProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(newSession != null ? newSession.asBinder() : null);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionConferenceExtendFailed(ImsReasonInfo reasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reasonInfo != null) {
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionConferenceExtendReceived(IImsCallSession newSession, ImsCallProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(newSession != null ? newSession.asBinder() : null);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionInviteParticipantsRequestDelivered() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionInviteParticipantsRequestFailed(ImsReasonInfo reasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reasonInfo != null) {
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(21, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionRemoveParticipantsRequestDelivered() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(22, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionRemoveParticipantsRequestFailed(ImsReasonInfo reasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reasonInfo != null) {
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(23, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionConferenceStateUpdated(ImsConferenceState state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (state != null) {
                        _data.writeInt(1);
                        state.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(24, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionUssdMessageReceived(int mode, String ussdMessage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeString(ussdMessage);
                    this.mRemote.transact(25, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionHandover(int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(srcAccessTech);
                    _data.writeInt(targetAccessTech);
                    if (reasonInfo != null) {
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(26, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionHandoverFailed(int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(srcAccessTech);
                    _data.writeInt(targetAccessTech);
                    if (reasonInfo != null) {
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(27, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionMayHandover(int srcAccessTech, int targetAccessTech) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(srcAccessTech);
                    _data.writeInt(targetAccessTech);
                    this.mRemote.transact(28, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionTtyModeReceived(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    this.mRemote.transact(29, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionMultipartyStateChanged(boolean isMultiParty) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isMultiParty);
                    this.mRemote.transact(30, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionSuppServiceReceived(ImsSuppServiceNotification suppSrvNotification) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (suppSrvNotification != null) {
                        _data.writeInt(1);
                        suppSrvNotification.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(31, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionRttModifyRequestReceived(ImsCallProfile callProfile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callProfile != null) {
                        _data.writeInt(1);
                        callProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(32, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionRttModifyResponseReceived(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    this.mRemote.transact(33, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void callSessionRttMessageReceived(String rttMessage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rttMessage);
                    this.mRemote.transact(34, _data, null, 1);
                } finally {
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: android.telephony.ims.ImsStreamMediaProfile} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: android.telephony.ims.ImsCallProfile} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v10, resolved type: android.telephony.ims.ImsReasonInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v14, resolved type: android.telephony.ims.ImsReasonInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v18, resolved type: android.telephony.ims.ImsCallProfile} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v22, resolved type: android.telephony.ims.ImsReasonInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v26, resolved type: android.telephony.ims.ImsCallProfile} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v30, resolved type: android.telephony.ims.ImsCallProfile} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v34, resolved type: android.telephony.ims.ImsReasonInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v38, resolved type: android.telephony.ims.ImsCallProfile} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v42, resolved type: android.telephony.ims.ImsCallProfile} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v48, resolved type: android.telephony.ims.ImsReasonInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v52, resolved type: android.telephony.ims.ImsCallProfile} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v56, resolved type: android.telephony.ims.ImsReasonInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v60, resolved type: android.telephony.ims.ImsCallProfile} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v64, resolved type: android.telephony.ims.ImsCallProfile} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v68, resolved type: android.telephony.ims.ImsReasonInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v72, resolved type: android.telephony.ims.ImsCallProfile} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v76, resolved type: android.telephony.ims.ImsReasonInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v80, resolved type: android.telephony.ims.ImsReasonInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v84, resolved type: android.telephony.ims.ImsConferenceState} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v89, resolved type: android.telephony.ims.ImsReasonInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v93, resolved type: android.telephony.ims.ImsReasonInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v103, resolved type: android.telephony.ims.ImsSuppServiceNotification} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v107, resolved type: android.telephony.ims.ImsCallProfile} */
        /* JADX WARNING: type inference failed for: r1v1 */
        /* JADX WARNING: type inference failed for: r1v114 */
        /* JADX WARNING: type inference failed for: r1v115 */
        /* JADX WARNING: type inference failed for: r1v116 */
        /* JADX WARNING: type inference failed for: r1v117 */
        /* JADX WARNING: type inference failed for: r1v118 */
        /* JADX WARNING: type inference failed for: r1v119 */
        /* JADX WARNING: type inference failed for: r1v120 */
        /* JADX WARNING: type inference failed for: r1v121 */
        /* JADX WARNING: type inference failed for: r1v122 */
        /* JADX WARNING: type inference failed for: r1v123 */
        /* JADX WARNING: type inference failed for: r1v124 */
        /* JADX WARNING: type inference failed for: r1v125 */
        /* JADX WARNING: type inference failed for: r1v126 */
        /* JADX WARNING: type inference failed for: r1v127 */
        /* JADX WARNING: type inference failed for: r1v128 */
        /* JADX WARNING: type inference failed for: r1v129 */
        /* JADX WARNING: type inference failed for: r1v130 */
        /* JADX WARNING: type inference failed for: r1v131 */
        /* JADX WARNING: type inference failed for: r1v132 */
        /* JADX WARNING: type inference failed for: r1v133 */
        /* JADX WARNING: type inference failed for: r1v134 */
        /* JADX WARNING: type inference failed for: r1v135 */
        /* JADX WARNING: type inference failed for: r1v136 */
        /* JADX WARNING: type inference failed for: r1v137 */
        /* JADX WARNING: type inference failed for: r1v138 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                ? _arg0 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsStreamMediaProfile.CREATOR.createFromParcel(data);
                        }
                        callSessionProgressing(_arg0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsCallProfile.CREATOR.createFromParcel(data);
                        }
                        callSessionInitiated(_arg0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        }
                        callSessionInitiatedFailed(_arg0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        }
                        callSessionTerminated(_arg0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsCallProfile.CREATOR.createFromParcel(data);
                        }
                        callSessionHeld(_arg0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        }
                        callSessionHoldFailed(_arg0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsCallProfile.CREATOR.createFromParcel(data);
                        }
                        callSessionHoldReceived(_arg0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsCallProfile.CREATOR.createFromParcel(data);
                        }
                        callSessionResumed(_arg0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        }
                        callSessionResumeFailed(_arg0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsCallProfile.CREATOR.createFromParcel(data);
                        }
                        callSessionResumeReceived(_arg0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        IImsCallSession _arg02 = IImsCallSession.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg0 = ImsCallProfile.CREATOR.createFromParcel(data);
                        }
                        callSessionMergeStarted(_arg02, _arg0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionMergeComplete(IImsCallSession.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        }
                        callSessionMergeFailed(_arg0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsCallProfile.CREATOR.createFromParcel(data);
                        }
                        callSessionUpdated(_arg0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        }
                        callSessionUpdateFailed(_arg0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsCallProfile.CREATOR.createFromParcel(data);
                        }
                        callSessionUpdateReceived(_arg0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        IImsCallSession _arg03 = IImsCallSession.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg0 = ImsCallProfile.CREATOR.createFromParcel(data);
                        }
                        callSessionConferenceExtended(_arg03, _arg0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        }
                        callSessionConferenceExtendFailed(_arg0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        IImsCallSession _arg04 = IImsCallSession.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg0 = ImsCallProfile.CREATOR.createFromParcel(data);
                        }
                        callSessionConferenceExtendReceived(_arg04, _arg0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionInviteParticipantsRequestDelivered();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        }
                        callSessionInviteParticipantsRequestFailed(_arg0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionRemoveParticipantsRequestDelivered();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        }
                        callSessionRemoveParticipantsRequestFailed(_arg0);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsConferenceState.CREATOR.createFromParcel(data);
                        }
                        callSessionConferenceStateUpdated(_arg0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionUssdMessageReceived(data.readInt(), data.readString());
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        int _arg1 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        }
                        callSessionHandover(_arg05, _arg1, _arg0);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        }
                        callSessionHandoverFailed(_arg06, _arg12, _arg0);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionMayHandover(data.readInt(), data.readInt());
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionTtyModeReceived(data.readInt());
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionMultipartyStateChanged(data.readInt() != 0);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsSuppServiceNotification.CREATOR.createFromParcel(data);
                        }
                        callSessionSuppServiceReceived(_arg0);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImsCallProfile.CREATOR.createFromParcel(data);
                        }
                        callSessionRttModifyRequestReceived(_arg0);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionRttModifyResponseReceived(data.readInt());
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionRttMessageReceived(data.readString());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void callSessionConferenceExtendFailed(ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionConferenceExtendReceived(IImsCallSession iImsCallSession, ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionConferenceExtended(IImsCallSession iImsCallSession, ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionConferenceStateUpdated(ImsConferenceState imsConferenceState) throws RemoteException;

    void callSessionHandover(int i, int i2, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionHandoverFailed(int i, int i2, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionHeld(ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionHoldFailed(ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionHoldReceived(ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionInitiated(ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionInitiatedFailed(ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionInviteParticipantsRequestDelivered() throws RemoteException;

    void callSessionInviteParticipantsRequestFailed(ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionMayHandover(int i, int i2) throws RemoteException;

    void callSessionMergeComplete(IImsCallSession iImsCallSession) throws RemoteException;

    void callSessionMergeFailed(ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionMergeStarted(IImsCallSession iImsCallSession, ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionMultipartyStateChanged(boolean z) throws RemoteException;

    void callSessionProgressing(ImsStreamMediaProfile imsStreamMediaProfile) throws RemoteException;

    void callSessionRemoveParticipantsRequestDelivered() throws RemoteException;

    void callSessionRemoveParticipantsRequestFailed(ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionResumeFailed(ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionResumeReceived(ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionResumed(ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionRttMessageReceived(String str) throws RemoteException;

    void callSessionRttModifyRequestReceived(ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionRttModifyResponseReceived(int i) throws RemoteException;

    void callSessionSuppServiceReceived(ImsSuppServiceNotification imsSuppServiceNotification) throws RemoteException;

    void callSessionTerminated(ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionTtyModeReceived(int i) throws RemoteException;

    void callSessionUpdateFailed(ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionUpdateReceived(ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionUpdated(ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionUssdMessageReceived(int i, String str) throws RemoteException;
}
