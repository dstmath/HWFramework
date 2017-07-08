package com.android.internal.telephony;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.CellInfo;
import android.telephony.DataConnectionRealTimeInfo;
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
        static final int TRANSACTION_onCarrierNetworkChange = 17;
        static final int TRANSACTION_onCellInfoChanged = 11;
        static final int TRANSACTION_onCellLocationChanged = 5;
        static final int TRANSACTION_onDataActivity = 8;
        static final int TRANSACTION_onDataConnectionRealTimeInfoChanged = 14;
        static final int TRANSACTION_onDataConnectionStateChanged = 7;
        static final int TRANSACTION_onMessageWaitingIndicatorChanged = 3;
        static final int TRANSACTION_onOemHookRawEvent = 16;
        static final int TRANSACTION_onOtaspChanged = 10;
        static final int TRANSACTION_onPreciseCallStateChanged = 12;
        static final int TRANSACTION_onPreciseDataConnectionStateChanged = 13;
        static final int TRANSACTION_onServiceStateChanged = 1;
        static final int TRANSACTION_onSignalStrengthChanged = 2;
        static final int TRANSACTION_onSignalStrengthsChanged = 9;
        static final int TRANSACTION_onVoLteServiceStateChanged = 15;

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
                        _data.writeInt(Stub.TRANSACTION_onServiceStateChanged);
                        serviceState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onServiceStateChanged, _data, null, Stub.TRANSACTION_onServiceStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onSignalStrengthChanged(int asu) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(asu);
                    this.mRemote.transact(Stub.TRANSACTION_onSignalStrengthChanged, _data, null, Stub.TRANSACTION_onServiceStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onMessageWaitingIndicatorChanged(boolean mwi) throws RemoteException {
                int i = Stub.TRANSACTION_onServiceStateChanged;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!mwi) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onMessageWaitingIndicatorChanged, _data, null, Stub.TRANSACTION_onServiceStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onCallForwardingIndicatorChanged(boolean cfi) throws RemoteException {
                int i = Stub.TRANSACTION_onServiceStateChanged;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!cfi) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onCallForwardingIndicatorChanged, _data, null, Stub.TRANSACTION_onServiceStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onCellLocationChanged(Bundle location) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (location != null) {
                        _data.writeInt(Stub.TRANSACTION_onServiceStateChanged);
                        location.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onCellLocationChanged, _data, null, Stub.TRANSACTION_onServiceStateChanged);
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
                    this.mRemote.transact(Stub.TRANSACTION_onCallStateChanged, _data, null, Stub.TRANSACTION_onServiceStateChanged);
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
                    this.mRemote.transact(Stub.TRANSACTION_onDataConnectionStateChanged, _data, null, Stub.TRANSACTION_onServiceStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onDataActivity(int direction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(direction);
                    this.mRemote.transact(Stub.TRANSACTION_onDataActivity, _data, null, Stub.TRANSACTION_onServiceStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onSignalStrengthsChanged(SignalStrength signalStrength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (signalStrength != null) {
                        _data.writeInt(Stub.TRANSACTION_onServiceStateChanged);
                        signalStrength.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onSignalStrengthsChanged, _data, null, Stub.TRANSACTION_onServiceStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onOtaspChanged(int otaspMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(otaspMode);
                    this.mRemote.transact(Stub.TRANSACTION_onOtaspChanged, _data, null, Stub.TRANSACTION_onServiceStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onCellInfoChanged(List<CellInfo> cellInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(cellInfo);
                    this.mRemote.transact(Stub.TRANSACTION_onCellInfoChanged, _data, null, Stub.TRANSACTION_onServiceStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onPreciseCallStateChanged(PreciseCallState callState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callState != null) {
                        _data.writeInt(Stub.TRANSACTION_onServiceStateChanged);
                        callState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onPreciseCallStateChanged, _data, null, Stub.TRANSACTION_onServiceStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState dataConnectionState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dataConnectionState != null) {
                        _data.writeInt(Stub.TRANSACTION_onServiceStateChanged);
                        dataConnectionState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onPreciseDataConnectionStateChanged, _data, null, Stub.TRANSACTION_onServiceStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onDataConnectionRealTimeInfoChanged(DataConnectionRealTimeInfo dcRtInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dcRtInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_onServiceStateChanged);
                        dcRtInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onDataConnectionRealTimeInfoChanged, _data, null, Stub.TRANSACTION_onServiceStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onVoLteServiceStateChanged(VoLteServiceState lteState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (lteState != null) {
                        _data.writeInt(Stub.TRANSACTION_onServiceStateChanged);
                        lteState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onVoLteServiceStateChanged, _data, null, Stub.TRANSACTION_onServiceStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onOemHookRawEvent(byte[] rawData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(rawData);
                    this.mRemote.transact(Stub.TRANSACTION_onOemHookRawEvent, _data, null, Stub.TRANSACTION_onServiceStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onCarrierNetworkChange(boolean active) throws RemoteException {
                int i = Stub.TRANSACTION_onServiceStateChanged;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!active) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onCarrierNetworkChange, _data, null, Stub.TRANSACTION_onServiceStateChanged);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onServiceStateChanged /*1*/:
                    ServiceState serviceState;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        serviceState = (ServiceState) ServiceState.CREATOR.createFromParcel(data);
                    } else {
                        serviceState = null;
                    }
                    onServiceStateChanged(serviceState);
                    return true;
                case TRANSACTION_onSignalStrengthChanged /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onSignalStrengthChanged(data.readInt());
                    return true;
                case TRANSACTION_onMessageWaitingIndicatorChanged /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onMessageWaitingIndicatorChanged(data.readInt() != 0);
                    return true;
                case TRANSACTION_onCallForwardingIndicatorChanged /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onCallForwardingIndicatorChanged(data.readInt() != 0);
                    return true;
                case TRANSACTION_onCellLocationChanged /*5*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    onCellLocationChanged(bundle);
                    return true;
                case TRANSACTION_onCallStateChanged /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    onCallStateChanged(data.readInt(), data.readString());
                    return true;
                case TRANSACTION_onDataConnectionStateChanged /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDataConnectionStateChanged(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_onDataActivity /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDataActivity(data.readInt());
                    return true;
                case TRANSACTION_onSignalStrengthsChanged /*9*/:
                    SignalStrength signalStrength;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        signalStrength = (SignalStrength) SignalStrength.CREATOR.createFromParcel(data);
                    } else {
                        signalStrength = null;
                    }
                    onSignalStrengthsChanged(signalStrength);
                    return true;
                case TRANSACTION_onOtaspChanged /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    onOtaspChanged(data.readInt());
                    return true;
                case TRANSACTION_onCellInfoChanged /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    onCellInfoChanged(data.createTypedArrayList(CellInfo.CREATOR));
                    return true;
                case TRANSACTION_onPreciseCallStateChanged /*12*/:
                    PreciseCallState preciseCallState;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        preciseCallState = (PreciseCallState) PreciseCallState.CREATOR.createFromParcel(data);
                    } else {
                        preciseCallState = null;
                    }
                    onPreciseCallStateChanged(preciseCallState);
                    return true;
                case TRANSACTION_onPreciseDataConnectionStateChanged /*13*/:
                    PreciseDataConnectionState preciseDataConnectionState;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        preciseDataConnectionState = (PreciseDataConnectionState) PreciseDataConnectionState.CREATOR.createFromParcel(data);
                    } else {
                        preciseDataConnectionState = null;
                    }
                    onPreciseDataConnectionStateChanged(preciseDataConnectionState);
                    return true;
                case TRANSACTION_onDataConnectionRealTimeInfoChanged /*14*/:
                    DataConnectionRealTimeInfo dataConnectionRealTimeInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        dataConnectionRealTimeInfo = (DataConnectionRealTimeInfo) DataConnectionRealTimeInfo.CREATOR.createFromParcel(data);
                    } else {
                        dataConnectionRealTimeInfo = null;
                    }
                    onDataConnectionRealTimeInfoChanged(dataConnectionRealTimeInfo);
                    return true;
                case TRANSACTION_onVoLteServiceStateChanged /*15*/:
                    VoLteServiceState voLteServiceState;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        voLteServiceState = (VoLteServiceState) VoLteServiceState.CREATOR.createFromParcel(data);
                    } else {
                        voLteServiceState = null;
                    }
                    onVoLteServiceStateChanged(voLteServiceState);
                    return true;
                case TRANSACTION_onOemHookRawEvent /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    onOemHookRawEvent(data.createByteArray());
                    return true;
                case TRANSACTION_onCarrierNetworkChange /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    onCarrierNetworkChange(data.readInt() != 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onCallForwardingIndicatorChanged(boolean z) throws RemoteException;

    void onCallStateChanged(int i, String str) throws RemoteException;

    void onCarrierNetworkChange(boolean z) throws RemoteException;

    void onCellInfoChanged(List<CellInfo> list) throws RemoteException;

    void onCellLocationChanged(Bundle bundle) throws RemoteException;

    void onDataActivity(int i) throws RemoteException;

    void onDataConnectionRealTimeInfoChanged(DataConnectionRealTimeInfo dataConnectionRealTimeInfo) throws RemoteException;

    void onDataConnectionStateChanged(int i, int i2) throws RemoteException;

    void onMessageWaitingIndicatorChanged(boolean z) throws RemoteException;

    void onOemHookRawEvent(byte[] bArr) throws RemoteException;

    void onOtaspChanged(int i) throws RemoteException;

    void onPreciseCallStateChanged(PreciseCallState preciseCallState) throws RemoteException;

    void onPreciseDataConnectionStateChanged(PreciseDataConnectionState preciseDataConnectionState) throws RemoteException;

    void onServiceStateChanged(ServiceState serviceState) throws RemoteException;

    void onSignalStrengthChanged(int i) throws RemoteException;

    void onSignalStrengthsChanged(SignalStrength signalStrength) throws RemoteException;

    void onVoLteServiceStateChanged(VoLteServiceState voLteServiceState) throws RemoteException;
}
