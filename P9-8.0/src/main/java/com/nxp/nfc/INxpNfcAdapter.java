package com.nxp.nfc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.nxp.intf.IeSEClientServicesAdapter;
import com.nxp.nfc.gsma.internal.INxpNfcController;
import java.util.Map;

public interface INxpNfcAdapter extends IInterface {

    public static abstract class Stub extends Binder implements INxpNfcAdapter {
        private static final String DESCRIPTOR = "com.nxp.nfc.INxpNfcAdapter";
        static final int TRANSACTION_DefaultRouteSet = 13;
        static final int TRANSACTION_MifareCLTRouteSet = 14;
        static final int TRANSACTION_MifareDesfireRouteSet = 12;
        static final int TRANSACTION_SetListenTechMask = 17;
        static final int TRANSACTION_deselectSecureElement = 9;
        static final int TRANSACTION_getActiveSecureElementList = 22;
        static final int TRANSACTION_getCommittedAidRoutingTableSize = 21;
        static final int TRANSACTION_getFWVersion = 18;
        static final int TRANSACTION_getMaxAidRoutingTableSize = 20;
        static final int TRANSACTION_getNfcDtaInterface = 1;
        static final int TRANSACTION_getNfcEseClientServicesAdapterInterface = 15;
        static final int TRANSACTION_getNfcInfo = 25;
        static final int TRANSACTION_getNfcVzwInterface = 3;
        static final int TRANSACTION_getNxpNfcAccessExtrasInterface = 2;
        static final int TRANSACTION_getNxpNfcAdapterExtrasInterface = 4;
        static final int TRANSACTION_getNxpNfcControllerInterface = 5;
        static final int TRANSACTION_getSeInterface = 16;
        static final int TRANSACTION_getSecureElementList = 6;
        static final int TRANSACTION_getSelectedSecureElement = 7;
        static final int TRANSACTION_getServicesAidCacheSize = 19;
        static final int TRANSACTION_selectSecureElement = 8;
        static final int TRANSACTION_setConfig = 24;
        static final int TRANSACTION_setEmvCoPollProfile = 11;
        static final int TRANSACTION_storeSePreference = 10;
        static final int TRANSACTION_updateServiceState = 23;

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

            public INfcDta getNfcDtaInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    INfcDta _result = com.nxp.nfc.INfcDta.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public INxpNfcAccessExtras getNxpNfcAccessExtrasInterface(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    INxpNfcAccessExtras _result = com.nxp.nfc.INxpNfcAccessExtras.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    INfcVzw _result = com.nxp.nfc.INfcVzw.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    INxpNfcAdapterExtras _result = com.nxp.nfc.INxpNfcAdapterExtras.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    INxpNfcController _result = com.nxp.nfc.gsma.internal.INxpNfcController.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
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
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setEmvCoPollProfile(boolean enable, int route) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(route);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void MifareDesfireRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(routeLoc);
                    _data.writeInt(fullPower ? 1 : 0);
                    if (lowPower) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!noPower) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void DefaultRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(routeLoc);
                    _data.writeInt(fullPower ? 1 : 0);
                    if (lowPower) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!noPower) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void MifareCLTRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(routeLoc);
                    _data.writeInt(fullPower ? 1 : 0);
                    if (lowPower) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!noPower) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(14, _data, _reply, 0);
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
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    IeSEClientServicesAdapter _result = com.nxp.intf.IeSEClientServicesAdapter.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(17, _data, _reply, 0);
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
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
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
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    return _result;
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
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
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
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
            int[] _result;
            int _result2;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    INfcDta _result3 = getNfcDtaInterface();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result3 != null ? _result3.asBinder() : null);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    INxpNfcAccessExtras _result4 = getNxpNfcAccessExtrasInterface(data.readString());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result4 != null ? _result4.asBinder() : null);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    INfcVzw _result5 = getNfcVzwInterface();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result5 != null ? _result5.asBinder() : null);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    INxpNfcAdapterExtras _result6 = getNxpNfcAdapterExtrasInterface();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result6 != null ? _result6.asBinder() : null);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    INxpNfcController _result7 = getNxpNfcControllerInterface();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result7 != null ? _result7.asBinder() : null);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSecureElementList(data.readString());
                    reply.writeNoException();
                    reply.writeIntArray(_result);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSelectedSecureElement(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = selectSecureElement(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = deselectSecureElement(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    storeSePreference(data.readInt());
                    reply.writeNoException();
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setEmvCoPollProfile(data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    MifareDesfireRouteSet(data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    DefaultRouteSet(data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    MifareCLTRouteSet(data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    IeSEClientServicesAdapter _result8 = getNfcEseClientServicesAdapterInterface();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result8 != null ? _result8.asBinder() : null);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSeInterface(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    SetListenTechMask(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _result9 = getFWVersion();
                    reply.writeNoException();
                    reply.writeByteArray(_result9);
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    Map _result10 = getServicesAidCacheSize(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeMap(_result10);
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getMaxAidRoutingTableSize();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getCommittedAidRoutingTableSize();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getActiveSecureElementList(data.readString());
                    reply.writeNoException();
                    reply.writeIntArray(_result);
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateServiceState(data.readInt(), data.readHashMap(getClass().getClassLoader()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setConfig(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    String _result11 = getNfcInfo(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result11);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
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

    INfcDta getNfcDtaInterface() throws RemoteException;

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

    int selectSecureElement(String str, int i) throws RemoteException;

    int setConfig(String str, String str2) throws RemoteException;

    int setEmvCoPollProfile(boolean z, int i) throws RemoteException;

    void storeSePreference(int i) throws RemoteException;

    int updateServiceState(int i, Map map) throws RemoteException;
}
