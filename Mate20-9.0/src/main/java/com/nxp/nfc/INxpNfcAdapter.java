package com.nxp.nfc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.nxp.intf.IeSEClientServicesAdapter;
import com.nxp.nfc.INfcEventCallback;
import com.nxp.nfc.INfcVzw;
import com.nxp.nfc.INxpNfcAccessExtras;
import com.nxp.nfc.INxpNfcAdapterExtras;
import com.nxp.nfc.gsma.internal.INxpNfcController;
import java.util.Map;

public interface INxpNfcAdapter extends IInterface {

    public static abstract class Stub extends Binder implements INxpNfcAdapter {
        private static final String DESCRIPTOR = "com.nxp.nfc.INxpNfcAdapter";
        static final int TRANSACTION_DefaultRouteSet = 12;
        static final int TRANSACTION_MifareCLTRouteSet = 13;
        static final int TRANSACTION_MifareDesfireRouteSet = 11;
        static final int TRANSACTION_SetListenTechMask = 16;
        static final int TRANSACTION_deselectSecureElement = 8;
        static final int TRANSACTION_getActiveSecureElementList = 21;
        static final int TRANSACTION_getCommittedAidRoutingTableSize = 20;
        static final int TRANSACTION_getFWVersion = 17;
        static final int TRANSACTION_getMaxAidRoutingTableSize = 19;
        static final int TRANSACTION_getNfcEseClientServicesAdapterInterface = 14;
        static final int TRANSACTION_getNfcInfo = 24;
        static final int TRANSACTION_getNfcVzwInterface = 2;
        static final int TRANSACTION_getNxpNfcAccessExtrasInterface = 1;
        static final int TRANSACTION_getNxpNfcAdapterExtrasInterface = 3;
        static final int TRANSACTION_getNxpNfcControllerInterface = 4;
        static final int TRANSACTION_getSeInterface = 15;
        static final int TRANSACTION_getSecureElementList = 5;
        static final int TRANSACTION_getSelectedSecureElement = 6;
        static final int TRANSACTION_getServicesAidCacheSize = 18;
        static final int TRANSACTION_isListenTechMaskEnable = 26;
        static final int TRANSACTION_selectSecureElement = 7;
        static final int TRANSACTION_setConfig = 23;
        static final int TRANSACTION_setEmvCoPollProfile = 10;
        static final int TRANSACTION_setNfcEventCallback = 27;
        static final int TRANSACTION_setNfcPolling = 25;
        static final int TRANSACTION_storeSePreference = 9;
        static final int TRANSACTION_updateServiceState = 22;

        private static class Proxy implements INxpNfcAdapter {
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

            public INxpNfcAccessExtras getNxpNfcAccessExtrasInterface(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return INxpNfcAccessExtras.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public INfcVzw getNfcVzwInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return INfcVzw.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public INxpNfcAdapterExtras getNxpNfcAdapterExtrasInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return INxpNfcAdapterExtras.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public INxpNfcController getNxpNfcControllerInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return INxpNfcController.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getSecureElementList(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSelectedSecureElement(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_getSelectedSecureElement, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int selectSecureElement(String pkg, int seId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(seId);
                    this.mRemote.transact(Stub.TRANSACTION_selectSecureElement, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deselectSecureElement(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_deselectSecureElement, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void storeSePreference(int seId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(seId);
                    this.mRemote.transact(Stub.TRANSACTION_storeSePreference, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setEmvCoPollProfile(boolean enable, int route) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    _data.writeInt(route);
                    this.mRemote.transact(Stub.TRANSACTION_setEmvCoPollProfile, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void MifareDesfireRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(routeLoc);
                    _data.writeInt(fullPower);
                    _data.writeInt(lowPower);
                    _data.writeInt(noPower);
                    this.mRemote.transact(Stub.TRANSACTION_MifareDesfireRouteSet, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void DefaultRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(routeLoc);
                    _data.writeInt(fullPower);
                    _data.writeInt(lowPower);
                    _data.writeInt(noPower);
                    this.mRemote.transact(Stub.TRANSACTION_DefaultRouteSet, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void MifareCLTRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(routeLoc);
                    _data.writeInt(fullPower);
                    _data.writeInt(lowPower);
                    _data.writeInt(noPower);
                    this.mRemote.transact(Stub.TRANSACTION_MifareCLTRouteSet, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IeSEClientServicesAdapter getNfcEseClientServicesAdapterInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getNfcEseClientServicesAdapterInterface, _data, _reply, 0);
                    _reply.readException();
                    return IeSEClientServicesAdapter.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSeInterface(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(Stub.TRANSACTION_getSeInterface, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void SetListenTechMask(int flags_ListenMask, int enable_override) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags_ListenMask);
                    _data.writeInt(enable_override);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getFWVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getFWVersion, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Map getServicesAidCacheSize(int userId, String category) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(category);
                    this.mRemote.transact(Stub.TRANSACTION_getServicesAidCacheSize, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readHashMap(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMaxAidRoutingTableSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getMaxAidRoutingTableSize, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCommittedAidRoutingTableSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCommittedAidRoutingTableSize, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getActiveSecureElementList(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveSecureElementList, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateServiceState(int userId, Map serviceState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeMap(serviceState);
                    this.mRemote.transact(Stub.TRANSACTION_updateServiceState, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setConfig(String configs, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(configs);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_setConfig, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getNfcInfo(String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    this.mRemote.transact(Stub.TRANSACTION_getNfcInfo, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setNfcPolling(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    this.mRemote.transact(Stub.TRANSACTION_setNfcPolling, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isListenTechMaskEnable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_isListenTechMaskEnable, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNfcEventCallback(INfcEventCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(Stub.TRANSACTION_setNfcEventCallback, _data, _reply, 0);
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

        public static INxpNfcAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INxpNfcAdapter)) {
                return new Proxy(obj);
            }
            return (INxpNfcAdapter) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                IBinder iBinder = null;
                boolean _arg3 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        INxpNfcAccessExtras _result = getNxpNfcAccessExtrasInterface(data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            iBinder = _result.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        INfcVzw _result2 = getNfcVzwInterface();
                        reply.writeNoException();
                        if (_result2 != null) {
                            iBinder = _result2.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        INxpNfcAdapterExtras _result3 = getNxpNfcAdapterExtrasInterface();
                        reply.writeNoException();
                        if (_result3 != null) {
                            iBinder = _result3.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        INxpNfcController _result4 = getNxpNfcControllerInterface();
                        reply.writeNoException();
                        if (_result4 != null) {
                            iBinder = _result4.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result5 = getSecureElementList(data.readString());
                        reply.writeNoException();
                        reply.writeIntArray(_result5);
                        return true;
                    case TRANSACTION_getSelectedSecureElement /*6*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getSelectedSecureElement(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case TRANSACTION_selectSecureElement /*7*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = selectSecureElement(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case TRANSACTION_deselectSecureElement /*8*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = deselectSecureElement(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case TRANSACTION_storeSePreference /*9*/:
                        data.enforceInterface(DESCRIPTOR);
                        storeSePreference(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setEmvCoPollProfile /*10*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        int _result9 = setEmvCoPollProfile(_arg3, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case TRANSACTION_MifareDesfireRouteSet /*11*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        boolean _arg1 = data.readInt() != 0;
                        boolean _arg2 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        MifareDesfireRouteSet(_arg0, _arg1, _arg2, _arg3);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_DefaultRouteSet /*12*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        boolean _arg12 = data.readInt() != 0;
                        boolean _arg22 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        DefaultRouteSet(_arg02, _arg12, _arg22, _arg3);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_MifareCLTRouteSet /*13*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        boolean _arg13 = data.readInt() != 0;
                        boolean _arg23 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        MifareCLTRouteSet(_arg03, _arg13, _arg23, _arg3);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getNfcEseClientServicesAdapterInterface /*14*/:
                        data.enforceInterface(DESCRIPTOR);
                        IeSEClientServicesAdapter _result10 = getNfcEseClientServicesAdapterInterface();
                        reply.writeNoException();
                        if (_result10 != null) {
                            iBinder = _result10.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case TRANSACTION_getSeInterface /*15*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getSeInterface(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        SetListenTechMask(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getFWVersion /*17*/:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result12 = getFWVersion();
                        reply.writeNoException();
                        reply.writeByteArray(_result12);
                        return true;
                    case TRANSACTION_getServicesAidCacheSize /*18*/:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result13 = getServicesAidCacheSize(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeMap(_result13);
                        return true;
                    case TRANSACTION_getMaxAidRoutingTableSize /*19*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = getMaxAidRoutingTableSize();
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case TRANSACTION_getCommittedAidRoutingTableSize /*20*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = getCommittedAidRoutingTableSize();
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case TRANSACTION_getActiveSecureElementList /*21*/:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result16 = getActiveSecureElementList(data.readString());
                        reply.writeNoException();
                        reply.writeIntArray(_result16);
                        return true;
                    case TRANSACTION_updateServiceState /*22*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = updateServiceState(data.readInt(), data.readHashMap(getClass().getClassLoader()));
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case TRANSACTION_setConfig /*23*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = setConfig(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case TRANSACTION_getNfcInfo /*24*/:
                        data.enforceInterface(DESCRIPTOR);
                        String _result19 = getNfcInfo(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result19);
                        return true;
                    case TRANSACTION_setNfcPolling /*25*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = setNfcPolling(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case TRANSACTION_isListenTechMaskEnable /*26*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result21 = isListenTechMaskEnable();
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case TRANSACTION_setNfcEventCallback /*27*/:
                        data.enforceInterface(DESCRIPTOR);
                        setNfcEventCallback(INfcEventCallback.Stub.asInterface(data.readStrongBinder()));
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
    }

    void DefaultRouteSet(int i, boolean z, boolean z2, boolean z3) throws RemoteException;

    void MifareCLTRouteSet(int i, boolean z, boolean z2, boolean z3) throws RemoteException;

    void MifareDesfireRouteSet(int i, boolean z, boolean z2, boolean z3) throws RemoteException;

    void SetListenTechMask(int i, int i2) throws RemoteException;

    int deselectSecureElement(String str) throws RemoteException;

    int[] getActiveSecureElementList(String str) throws RemoteException;

    int getCommittedAidRoutingTableSize() throws RemoteException;

    byte[] getFWVersion() throws RemoteException;

    int getMaxAidRoutingTableSize() throws RemoteException;

    IeSEClientServicesAdapter getNfcEseClientServicesAdapterInterface() throws RemoteException;

    String getNfcInfo(String str) throws RemoteException;

    INfcVzw getNfcVzwInterface() throws RemoteException;

    INxpNfcAccessExtras getNxpNfcAccessExtrasInterface(String str) throws RemoteException;

    INxpNfcAdapterExtras getNxpNfcAdapterExtrasInterface() throws RemoteException;

    INxpNfcController getNxpNfcControllerInterface() throws RemoteException;

    int getSeInterface(int i) throws RemoteException;

    int[] getSecureElementList(String str) throws RemoteException;

    int getSelectedSecureElement(String str) throws RemoteException;

    Map getServicesAidCacheSize(int i, String str) throws RemoteException;

    boolean isListenTechMaskEnable() throws RemoteException;

    int selectSecureElement(String str, int i) throws RemoteException;

    int setConfig(String str, String str2) throws RemoteException;

    int setEmvCoPollProfile(boolean z, int i) throws RemoteException;

    void setNfcEventCallback(INfcEventCallback iNfcEventCallback) throws RemoteException;

    int setNfcPolling(int i) throws RemoteException;

    void storeSePreference(int i) throws RemoteException;

    int updateServiceState(int i, Map map) throws RemoteException;
}
