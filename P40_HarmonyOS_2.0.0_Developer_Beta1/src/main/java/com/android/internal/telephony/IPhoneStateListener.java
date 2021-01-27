package com.android.internal.telephony;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.CallAttributes;
import android.telephony.CellInfo;
import android.telephony.DataConnectionRealTimeInfo;
import android.telephony.PhoneCapability;
import android.telephony.PhysicalChannelConfig;
import android.telephony.PreciseCallState;
import android.telephony.PreciseDataConnectionState;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.ims.ImsReasonInfo;
import java.util.List;
import java.util.Map;

public interface IPhoneStateListener extends IInterface {
    void onActiveDataSubIdChanged(int i) throws RemoteException;

    void onCallAttributesChanged(CallAttributes callAttributes) throws RemoteException;

    void onCallDisconnectCauseChanged(int i, int i2) throws RemoteException;

    void onCallForwardingIndicatorChanged(boolean z) throws RemoteException;

    void onCallStateChanged(int i, String str) throws RemoteException;

    void onCarrierNetworkChange(boolean z) throws RemoteException;

    void onCellInfoChanged(List<CellInfo> list) throws RemoteException;

    void onCellLocationChanged(Bundle bundle) throws RemoteException;

    void onDataActivationStateChanged(int i) throws RemoteException;

    void onDataActivity(int i) throws RemoteException;

    void onDataConnectionRealTimeInfoChanged(DataConnectionRealTimeInfo dataConnectionRealTimeInfo) throws RemoteException;

    void onDataConnectionStateChanged(int i, int i2) throws RemoteException;

    void onEmergencyNumberListChanged(Map map) throws RemoteException;

    void onImsCallDisconnectCauseChanged(ImsReasonInfo imsReasonInfo) throws RemoteException;

    void onMessageWaitingIndicatorChanged(boolean z) throws RemoteException;

    void onOemHookRawEvent(byte[] bArr) throws RemoteException;

    void onOtaspChanged(int i) throws RemoteException;

    void onPhoneCapabilityChanged(PhoneCapability phoneCapability) throws RemoteException;

    void onPhysicalChannelConfigurationChanged(List<PhysicalChannelConfig> list) throws RemoteException;

    void onPreciseCallStateChanged(PreciseCallState preciseCallState) throws RemoteException;

    void onPreciseDataConnectionStateChanged(PreciseDataConnectionState preciseDataConnectionState) throws RemoteException;

    void onRadioPowerStateChanged(int i) throws RemoteException;

    void onServiceStateChanged(ServiceState serviceState) throws RemoteException;

    void onSignalStrengthChanged(int i) throws RemoteException;

    void onSignalStrengthsChanged(SignalStrength signalStrength) throws RemoteException;

    void onSrvccStateChanged(int i) throws RemoteException;

    void onUserMobileDataStateChanged(boolean z) throws RemoteException;

    void onVoiceActivationStateChanged(int i) throws RemoteException;

    public static class Default implements IPhoneStateListener {
        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onServiceStateChanged(ServiceState serviceState) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onSignalStrengthChanged(int asu) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onMessageWaitingIndicatorChanged(boolean mwi) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onCallForwardingIndicatorChanged(boolean cfi) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onCellLocationChanged(Bundle location) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onCallStateChanged(int state, String incomingNumber) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onDataConnectionStateChanged(int state, int networkType) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onDataActivity(int direction) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onSignalStrengthsChanged(SignalStrength signalStrength) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onPhysicalChannelConfigurationChanged(List<PhysicalChannelConfig> list) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onOtaspChanged(int otaspMode) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onCellInfoChanged(List<CellInfo> list) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onPreciseCallStateChanged(PreciseCallState callState) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState dataConnectionState) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onDataConnectionRealTimeInfoChanged(DataConnectionRealTimeInfo dcRtInfo) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onSrvccStateChanged(int state) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onVoiceActivationStateChanged(int activationState) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onDataActivationStateChanged(int activationState) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onOemHookRawEvent(byte[] rawData) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onCarrierNetworkChange(boolean active) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onUserMobileDataStateChanged(boolean enabled) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onPhoneCapabilityChanged(PhoneCapability capability) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onActiveDataSubIdChanged(int subId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onRadioPowerStateChanged(int state) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onCallAttributesChanged(CallAttributes callAttributes) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onEmergencyNumberListChanged(Map emergencyNumberList) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onCallDisconnectCauseChanged(int disconnectCause, int preciseDisconnectCause) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onImsCallDisconnectCauseChanged(ImsReasonInfo imsReasonInfo) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPhoneStateListener {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IPhoneStateListener";
        static final int TRANSACTION_onActiveDataSubIdChanged = 23;
        static final int TRANSACTION_onCallAttributesChanged = 25;
        static final int TRANSACTION_onCallDisconnectCauseChanged = 27;
        static final int TRANSACTION_onCallForwardingIndicatorChanged = 4;
        static final int TRANSACTION_onCallStateChanged = 6;
        static final int TRANSACTION_onCarrierNetworkChange = 20;
        static final int TRANSACTION_onCellInfoChanged = 12;
        static final int TRANSACTION_onCellLocationChanged = 5;
        static final int TRANSACTION_onDataActivationStateChanged = 18;
        static final int TRANSACTION_onDataActivity = 8;
        static final int TRANSACTION_onDataConnectionRealTimeInfoChanged = 15;
        static final int TRANSACTION_onDataConnectionStateChanged = 7;
        static final int TRANSACTION_onEmergencyNumberListChanged = 26;
        static final int TRANSACTION_onImsCallDisconnectCauseChanged = 28;
        static final int TRANSACTION_onMessageWaitingIndicatorChanged = 3;
        static final int TRANSACTION_onOemHookRawEvent = 19;
        static final int TRANSACTION_onOtaspChanged = 11;
        static final int TRANSACTION_onPhoneCapabilityChanged = 22;
        static final int TRANSACTION_onPhysicalChannelConfigurationChanged = 10;
        static final int TRANSACTION_onPreciseCallStateChanged = 13;
        static final int TRANSACTION_onPreciseDataConnectionStateChanged = 14;
        static final int TRANSACTION_onRadioPowerStateChanged = 24;
        static final int TRANSACTION_onServiceStateChanged = 1;
        static final int TRANSACTION_onSignalStrengthChanged = 2;
        static final int TRANSACTION_onSignalStrengthsChanged = 9;
        static final int TRANSACTION_onSrvccStateChanged = 16;
        static final int TRANSACTION_onUserMobileDataStateChanged = 21;
        static final int TRANSACTION_onVoiceActivationStateChanged = 17;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onServiceStateChanged";
                case 2:
                    return "onSignalStrengthChanged";
                case 3:
                    return "onMessageWaitingIndicatorChanged";
                case 4:
                    return "onCallForwardingIndicatorChanged";
                case 5:
                    return "onCellLocationChanged";
                case 6:
                    return "onCallStateChanged";
                case 7:
                    return "onDataConnectionStateChanged";
                case 8:
                    return "onDataActivity";
                case 9:
                    return "onSignalStrengthsChanged";
                case 10:
                    return "onPhysicalChannelConfigurationChanged";
                case 11:
                    return "onOtaspChanged";
                case 12:
                    return "onCellInfoChanged";
                case 13:
                    return "onPreciseCallStateChanged";
                case 14:
                    return "onPreciseDataConnectionStateChanged";
                case 15:
                    return "onDataConnectionRealTimeInfoChanged";
                case 16:
                    return "onSrvccStateChanged";
                case 17:
                    return "onVoiceActivationStateChanged";
                case 18:
                    return "onDataActivationStateChanged";
                case 19:
                    return "onOemHookRawEvent";
                case 20:
                    return "onCarrierNetworkChange";
                case 21:
                    return "onUserMobileDataStateChanged";
                case 22:
                    return "onPhoneCapabilityChanged";
                case 23:
                    return "onActiveDataSubIdChanged";
                case 24:
                    return "onRadioPowerStateChanged";
                case 25:
                    return "onCallAttributesChanged";
                case 26:
                    return "onEmergencyNumberListChanged";
                case 27:
                    return "onCallDisconnectCauseChanged";
                case 28:
                    return "onImsCallDisconnectCauseChanged";
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
            ServiceState _arg0;
            Bundle _arg02;
            SignalStrength _arg03;
            PreciseCallState _arg04;
            PreciseDataConnectionState _arg05;
            DataConnectionRealTimeInfo _arg06;
            PhoneCapability _arg07;
            CallAttributes _arg08;
            ImsReasonInfo _arg09;
            if (code != 1598968902) {
                boolean _arg010 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ServiceState.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onServiceStateChanged(_arg0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onSignalStrengthChanged(data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        onMessageWaitingIndicatorChanged(_arg010);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        onCallForwardingIndicatorChanged(_arg010);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
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
                            _arg03 = SignalStrength.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        onSignalStrengthsChanged(_arg03);
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
                            _arg04 = PreciseCallState.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        onPreciseCallStateChanged(_arg04);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = PreciseDataConnectionState.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        onPreciseDataConnectionStateChanged(_arg05);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = DataConnectionRealTimeInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        onDataConnectionRealTimeInfoChanged(_arg06);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        onSrvccStateChanged(data.readInt());
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
                            _arg010 = true;
                        }
                        onCarrierNetworkChange(_arg010);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        onUserMobileDataStateChanged(_arg010);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = PhoneCapability.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        onPhoneCapabilityChanged(_arg07);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        onActiveDataSubIdChanged(data.readInt());
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        onRadioPowerStateChanged(data.readInt());
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = CallAttributes.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        onCallAttributesChanged(_arg08);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        onEmergencyNumberListChanged(data.readHashMap(getClass().getClassLoader()));
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        onCallDisconnectCauseChanged(data.readInt(), data.readInt());
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg09 = null;
                        }
                        onImsCallDisconnectCauseChanged(_arg09);
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
        public static class Proxy implements IPhoneStateListener {
            public static IPhoneStateListener sDefaultImpl;
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

            @Override // com.android.internal.telephony.IPhoneStateListener
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
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onServiceStateChanged(serviceState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onSignalStrengthChanged(int asu) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(asu);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSignalStrengthChanged(asu);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onMessageWaitingIndicatorChanged(boolean mwi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mwi ? 1 : 0);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onMessageWaitingIndicatorChanged(mwi);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onCallForwardingIndicatorChanged(boolean cfi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cfi ? 1 : 0);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCallForwardingIndicatorChanged(cfi);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
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
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCellLocationChanged(location);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onCallStateChanged(int state, String incomingNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    _data.writeString(incomingNumber);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCallStateChanged(state, incomingNumber);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onDataConnectionStateChanged(int state, int networkType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    _data.writeInt(networkType);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDataConnectionStateChanged(state, networkType);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onDataActivity(int direction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(direction);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDataActivity(direction);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
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
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSignalStrengthsChanged(signalStrength);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onPhysicalChannelConfigurationChanged(List<PhysicalChannelConfig> configs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(configs);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPhysicalChannelConfigurationChanged(configs);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onOtaspChanged(int otaspMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(otaspMode);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onOtaspChanged(otaspMode);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onCellInfoChanged(List<CellInfo> cellInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(cellInfo);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCellInfoChanged(cellInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
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
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPreciseCallStateChanged(callState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
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
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPreciseDataConnectionStateChanged(dataConnectionState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
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
                    if (this.mRemote.transact(15, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDataConnectionRealTimeInfoChanged(dcRtInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onSrvccStateChanged(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(16, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSrvccStateChanged(state);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onVoiceActivationStateChanged(int activationState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(activationState);
                    if (this.mRemote.transact(17, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onVoiceActivationStateChanged(activationState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onDataActivationStateChanged(int activationState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(activationState);
                    if (this.mRemote.transact(18, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDataActivationStateChanged(activationState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onOemHookRawEvent(byte[] rawData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(rawData);
                    if (this.mRemote.transact(19, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onOemHookRawEvent(rawData);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onCarrierNetworkChange(boolean active) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(active ? 1 : 0);
                    if (this.mRemote.transact(20, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCarrierNetworkChange(active);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onUserMobileDataStateChanged(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(21, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUserMobileDataStateChanged(enabled);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onPhoneCapabilityChanged(PhoneCapability capability) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (capability != null) {
                        _data.writeInt(1);
                        capability.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(22, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPhoneCapabilityChanged(capability);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onActiveDataSubIdChanged(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(23, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onActiveDataSubIdChanged(subId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onRadioPowerStateChanged(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(24, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRadioPowerStateChanged(state);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onCallAttributesChanged(CallAttributes callAttributes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callAttributes != null) {
                        _data.writeInt(1);
                        callAttributes.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(25, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCallAttributesChanged(callAttributes);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onEmergencyNumberListChanged(Map emergencyNumberList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeMap(emergencyNumberList);
                    if (this.mRemote.transact(26, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onEmergencyNumberListChanged(emergencyNumberList);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onCallDisconnectCauseChanged(int disconnectCause, int preciseDisconnectCause) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(disconnectCause);
                    _data.writeInt(preciseDisconnectCause);
                    if (this.mRemote.transact(27, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCallDisconnectCauseChanged(disconnectCause, preciseDisconnectCause);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneStateListener
            public void onImsCallDisconnectCauseChanged(ImsReasonInfo imsReasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (imsReasonInfo != null) {
                        _data.writeInt(1);
                        imsReasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(28, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onImsCallDisconnectCauseChanged(imsReasonInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPhoneStateListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPhoneStateListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
