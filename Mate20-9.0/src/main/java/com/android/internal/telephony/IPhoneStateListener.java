package com.android.internal.telephony;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.CellInfo;
import android.telephony.DataConnectionRealTimeInfo;
import android.telephony.PhysicalChannelConfig;
import android.telephony.PreciseCallState;
import android.telephony.PreciseDataConnectionState;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.VoLteServiceState;
import java.util.List;

public interface IPhoneStateListener extends IInterface {

    public static abstract class Stub extends Binder implements IPhoneStateListener {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IPhoneStateListener";
        static final int TRANSACTION_onCallForwardingIndicatorChanged = 4;
        static final int TRANSACTION_onCallStateChanged = 6;
        static final int TRANSACTION_onCarrierNetworkChange = 20;
        static final int TRANSACTION_onCellInfoChanged = 12;
        static final int TRANSACTION_onCellLocationChanged = 5;
        static final int TRANSACTION_onDataActivationStateChanged = 18;
        static final int TRANSACTION_onDataActivity = 8;
        static final int TRANSACTION_onDataConnectionRealTimeInfoChanged = 15;
        static final int TRANSACTION_onDataConnectionStateChanged = 7;
        static final int TRANSACTION_onMessageWaitingIndicatorChanged = 3;
        static final int TRANSACTION_onOemHookRawEvent = 19;
        static final int TRANSACTION_onOtaspChanged = 11;
        static final int TRANSACTION_onPhysicalChannelConfigurationChanged = 10;
        static final int TRANSACTION_onPreciseCallStateChanged = 13;
        static final int TRANSACTION_onPreciseDataConnectionStateChanged = 14;
        static final int TRANSACTION_onServiceStateChanged = 1;
        static final int TRANSACTION_onSignalStrengthChanged = 2;
        static final int TRANSACTION_onSignalStrengthsChanged = 9;
        static final int TRANSACTION_onUserMobileDataStateChanged = 21;
        static final int TRANSACTION_onVoLteServiceStateChanged = 16;
        static final int TRANSACTION_onVoiceActivationStateChanged = 17;

        private static class Proxy implements IPhoneStateListener {
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

            public void onServiceStateChanged(ServiceState serviceState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (serviceState != null) {
                        _data.writeInt(1);
                        serviceState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSignalStrengthChanged(int asu) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(asu);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onMessageWaitingIndicatorChanged(boolean mwi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mwi);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCallForwardingIndicatorChanged(boolean cfi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cfi);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCellLocationChanged(Bundle location) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (location != null) {
                        _data.writeInt(1);
                        location.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCallStateChanged(int state, String incomingNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    _data.writeString(incomingNumber);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDataConnectionStateChanged(int state, int networkType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    _data.writeInt(networkType);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDataActivity(int direction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(direction);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSignalStrengthsChanged(SignalStrength signalStrength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (signalStrength != null) {
                        _data.writeInt(1);
                        signalStrength.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPhysicalChannelConfigurationChanged(List<PhysicalChannelConfig> configs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(configs);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onOtaspChanged(int otaspMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(otaspMode);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCellInfoChanged(List<CellInfo> cellInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(cellInfo);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPreciseCallStateChanged(PreciseCallState callState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callState != null) {
                        _data.writeInt(1);
                        callState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState dataConnectionState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dataConnectionState != null) {
                        _data.writeInt(1);
                        dataConnectionState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDataConnectionRealTimeInfoChanged(DataConnectionRealTimeInfo dcRtInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dcRtInfo != null) {
                        _data.writeInt(1);
                        dcRtInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onVoLteServiceStateChanged(VoLteServiceState lteState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (lteState != null) {
                        _data.writeInt(1);
                        lteState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onVoiceActivationStateChanged(int activationState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(activationState);
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDataActivationStateChanged(int activationState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(activationState);
                    this.mRemote.transact(18, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onOemHookRawEvent(byte[] rawData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(rawData);
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCarrierNetworkChange(boolean active) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(active);
                    this.mRemote.transact(20, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onUserMobileDataStateChanged(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled);
                    this.mRemote.transact(21, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPhoneStateListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPhoneStateListener)) {
                return new Proxy(obj);
            }
            return (IPhoneStateListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v0, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: android.telephony.ServiceState} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v6, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v12, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v11, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v21, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v12, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v13, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v14, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v30, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v15, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v16, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v17, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v34, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v18, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v19, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v20, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v38, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v21, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v22, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v23, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v42, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v24, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v25, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v28, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v29, resolved type: android.telephony.SignalStrength} */
        /* JADX WARNING: type inference failed for: r3v3, types: [android.telephony.ServiceState] */
        /* JADX WARNING: type inference failed for: r3v8, types: [android.os.Bundle] */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg0 = false;
                SignalStrength _arg02 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (ServiceState) ServiceState.CREATOR.createFromParcel(data);
                        }
                        onServiceStateChanged(_arg02);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onSignalStrengthChanged(data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onMessageWaitingIndicatorChanged(_arg0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onCallForwardingIndicatorChanged(_arg0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onCellLocationChanged(_arg02);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onCallStateChanged(data.readInt(), data.readString());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onDataConnectionStateChanged(data.readInt(), data.readInt());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        onDataActivity(data.readInt());
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = SignalStrength.CREATOR.createFromParcel(data);
                        }
                        onSignalStrengthsChanged(_arg02);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        onPhysicalChannelConfigurationChanged(data.createTypedArrayList(PhysicalChannelConfig.CREATOR));
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        onOtaspChanged(data.readInt());
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        onCellInfoChanged(data.createTypedArrayList(CellInfo.CREATOR));
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (PreciseCallState) PreciseCallState.CREATOR.createFromParcel(data);
                        }
                        onPreciseCallStateChanged(_arg02);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (PreciseDataConnectionState) PreciseDataConnectionState.CREATOR.createFromParcel(data);
                        }
                        onPreciseDataConnectionStateChanged(_arg02);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (DataConnectionRealTimeInfo) DataConnectionRealTimeInfo.CREATOR.createFromParcel(data);
                        }
                        onDataConnectionRealTimeInfoChanged(_arg02);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (VoLteServiceState) VoLteServiceState.CREATOR.createFromParcel(data);
                        }
                        onVoLteServiceStateChanged(_arg02);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        onVoiceActivationStateChanged(data.readInt());
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        onDataActivationStateChanged(data.readInt());
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        onOemHookRawEvent(data.createByteArray());
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onCarrierNetworkChange(_arg0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onUserMobileDataStateChanged(_arg0);
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

    void onCallForwardingIndicatorChanged(boolean z) throws RemoteException;

    void onCallStateChanged(int i, String str) throws RemoteException;

    void onCarrierNetworkChange(boolean z) throws RemoteException;

    void onCellInfoChanged(List<CellInfo> list) throws RemoteException;

    void onCellLocationChanged(Bundle bundle) throws RemoteException;

    void onDataActivationStateChanged(int i) throws RemoteException;

    void onDataActivity(int i) throws RemoteException;

    void onDataConnectionRealTimeInfoChanged(DataConnectionRealTimeInfo dataConnectionRealTimeInfo) throws RemoteException;

    void onDataConnectionStateChanged(int i, int i2) throws RemoteException;

    void onMessageWaitingIndicatorChanged(boolean z) throws RemoteException;

    void onOemHookRawEvent(byte[] bArr) throws RemoteException;

    void onOtaspChanged(int i) throws RemoteException;

    void onPhysicalChannelConfigurationChanged(List<PhysicalChannelConfig> list) throws RemoteException;

    void onPreciseCallStateChanged(PreciseCallState preciseCallState) throws RemoteException;

    void onPreciseDataConnectionStateChanged(PreciseDataConnectionState preciseDataConnectionState) throws RemoteException;

    void onServiceStateChanged(ServiceState serviceState) throws RemoteException;

    void onSignalStrengthChanged(int i) throws RemoteException;

    void onSignalStrengthsChanged(SignalStrength signalStrength) throws RemoteException;

    void onUserMobileDataStateChanged(boolean z) throws RemoteException;

    void onVoLteServiceStateChanged(VoLteServiceState voLteServiceState) throws RemoteException;

    void onVoiceActivationStateChanged(int i) throws RemoteException;
}
