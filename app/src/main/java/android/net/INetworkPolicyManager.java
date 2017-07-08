package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INetworkPolicyManager extends IInterface {

    public static abstract class Stub extends Binder implements INetworkPolicyManager {
        private static final String DESCRIPTOR = "android.net.INetworkPolicyManager";
        static final int TRANSACTION_addRestrictBackgroundWhitelistedUid = 16;
        static final int TRANSACTION_addUidPolicy = 2;
        static final int TRANSACTION_factoryReset = 23;
        static final int TRANSACTION_getNetworkPolicies = 11;
        static final int TRANSACTION_getNetworkQuotaInfo = 21;
        static final int TRANSACTION_getRestrictBackground = 14;
        static final int TRANSACTION_getRestrictBackgroundByCaller = 19;
        static final int TRANSACTION_getRestrictBackgroundWhitelistedUids = 18;
        static final int TRANSACTION_getUidPolicy = 4;
        static final int TRANSACTION_getUidsWithPolicy = 5;
        static final int TRANSACTION_isNetworkMetered = 22;
        static final int TRANSACTION_isUidForeground = 6;
        static final int TRANSACTION_onTetheringChanged = 15;
        static final int TRANSACTION_registerListener = 8;
        static final int TRANSACTION_removeRestrictBackgroundWhitelistedUid = 17;
        static final int TRANSACTION_removeUidPolicy = 3;
        static final int TRANSACTION_setConnectivityListener = 7;
        static final int TRANSACTION_setDeviceIdleMode = 20;
        static final int TRANSACTION_setNetworkPolicies = 10;
        static final int TRANSACTION_setRestrictBackground = 13;
        static final int TRANSACTION_setUidPolicy = 1;
        static final int TRANSACTION_snoozeLimit = 12;
        static final int TRANSACTION_unregisterListener = 9;

        private static class Proxy implements INetworkPolicyManager {
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

            public void setUidPolicy(int uid, int policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(policy);
                    this.mRemote.transact(Stub.TRANSACTION_setUidPolicy, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addUidPolicy(int uid, int policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(policy);
                    this.mRemote.transact(Stub.TRANSACTION_addUidPolicy, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeUidPolicy(int uid, int policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(policy);
                    this.mRemote.transact(Stub.TRANSACTION_removeUidPolicy, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUidPolicy(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_getUidPolicy, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getUidsWithPolicy(int policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(policy);
                    this.mRemote.transact(Stub.TRANSACTION_getUidsWithPolicy, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isUidForeground(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_isUidForeground, _data, _reply, 0);
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

            public void setConnectivityListener(INetworkPolicyListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setConnectivityListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerListener(INetworkPolicyListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterListener(INetworkPolicyListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNetworkPolicies(NetworkPolicy[] policies) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(policies, 0);
                    this.mRemote.transact(Stub.TRANSACTION_setNetworkPolicies, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkPolicy[] getNetworkPolicies(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkPolicies, _data, _reply, 0);
                    _reply.readException();
                    NetworkPolicy[] _result = (NetworkPolicy[]) _reply.createTypedArray(NetworkPolicy.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void snoozeLimit(NetworkTemplate template) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (template != null) {
                        _data.writeInt(Stub.TRANSACTION_setUidPolicy);
                        template.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_snoozeLimit, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRestrictBackground(boolean restrictBackground) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (restrictBackground) {
                        i = Stub.TRANSACTION_setUidPolicy;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setRestrictBackground, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getRestrictBackground() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getRestrictBackground, _data, _reply, 0);
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

            public void onTetheringChanged(String iface, boolean tethering) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    if (tethering) {
                        i = Stub.TRANSACTION_setUidPolicy;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onTetheringChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addRestrictBackgroundWhitelistedUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_addRestrictBackgroundWhitelistedUid, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeRestrictBackgroundWhitelistedUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_removeRestrictBackgroundWhitelistedUid, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getRestrictBackgroundWhitelistedUids() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getRestrictBackgroundWhitelistedUids, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRestrictBackgroundByCaller() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getRestrictBackgroundByCaller, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDeviceIdleMode(boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = Stub.TRANSACTION_setUidPolicy;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setDeviceIdleMode, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkQuotaInfo getNetworkQuotaInfo(NetworkState state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkQuotaInfo networkQuotaInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (state != null) {
                        _data.writeInt(Stub.TRANSACTION_setUidPolicy);
                        state.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkQuotaInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkQuotaInfo = (NetworkQuotaInfo) NetworkQuotaInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        networkQuotaInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkQuotaInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isNetworkMetered(NetworkState state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (state != null) {
                        _data.writeInt(Stub.TRANSACTION_setUidPolicy);
                        state.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isNetworkMetered, _data, _reply, 0);
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

            public void factoryReset(String subscriber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(subscriber);
                    this.mRemote.transact(Stub.TRANSACTION_factoryReset, _data, _reply, 0);
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

        public static INetworkPolicyManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetworkPolicyManager)) {
                return new Proxy(obj);
            }
            return (INetworkPolicyManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            int[] _result2;
            boolean _result3;
            NetworkState networkState;
            switch (code) {
                case TRANSACTION_setUidPolicy /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    setUidPolicy(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addUidPolicy /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    addUidPolicy(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeUidPolicy /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeUidPolicy(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getUidPolicy /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUidPolicy(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getUidsWithPolicy /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getUidsWithPolicy(data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_isUidForeground /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isUidForeground(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_setUidPolicy : 0);
                    return true;
                case TRANSACTION_setConnectivityListener /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    setConnectivityListener(android.net.INetworkPolicyListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerListener /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerListener(android.net.INetworkPolicyListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterListener /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterListener(android.net.INetworkPolicyListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setNetworkPolicies /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    setNetworkPolicies((NetworkPolicy[]) data.createTypedArray(NetworkPolicy.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getNetworkPolicies /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    NetworkPolicy[] _result4 = getNetworkPolicies(data.readString());
                    reply.writeNoException();
                    reply.writeTypedArray(_result4, TRANSACTION_setUidPolicy);
                    return true;
                case TRANSACTION_snoozeLimit /*12*/:
                    NetworkTemplate networkTemplate;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkTemplate = (NetworkTemplate) NetworkTemplate.CREATOR.createFromParcel(data);
                    } else {
                        networkTemplate = null;
                    }
                    snoozeLimit(networkTemplate);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setRestrictBackground /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    setRestrictBackground(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getRestrictBackground /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getRestrictBackground();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_setUidPolicy : 0);
                    return true;
                case TRANSACTION_onTetheringChanged /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    onTetheringChanged(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addRestrictBackgroundWhitelistedUid /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    addRestrictBackgroundWhitelistedUid(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeRestrictBackgroundWhitelistedUid /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeRestrictBackgroundWhitelistedUid(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getRestrictBackgroundWhitelistedUids /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getRestrictBackgroundWhitelistedUids();
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_getRestrictBackgroundByCaller /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getRestrictBackgroundByCaller();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setDeviceIdleMode /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDeviceIdleMode(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getNetworkQuotaInfo /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkState = (NetworkState) NetworkState.CREATOR.createFromParcel(data);
                    } else {
                        networkState = null;
                    }
                    NetworkQuotaInfo _result5 = getNetworkQuotaInfo(networkState);
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_setUidPolicy);
                        _result5.writeToParcel(reply, TRANSACTION_setUidPolicy);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_isNetworkMetered /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkState = (NetworkState) NetworkState.CREATOR.createFromParcel(data);
                    } else {
                        networkState = null;
                    }
                    _result3 = isNetworkMetered(networkState);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_setUidPolicy : 0);
                    return true;
                case TRANSACTION_factoryReset /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    factoryReset(data.readString());
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addRestrictBackgroundWhitelistedUid(int i) throws RemoteException;

    void addUidPolicy(int i, int i2) throws RemoteException;

    void factoryReset(String str) throws RemoteException;

    NetworkPolicy[] getNetworkPolicies(String str) throws RemoteException;

    NetworkQuotaInfo getNetworkQuotaInfo(NetworkState networkState) throws RemoteException;

    boolean getRestrictBackground() throws RemoteException;

    int getRestrictBackgroundByCaller() throws RemoteException;

    int[] getRestrictBackgroundWhitelistedUids() throws RemoteException;

    int getUidPolicy(int i) throws RemoteException;

    int[] getUidsWithPolicy(int i) throws RemoteException;

    boolean isNetworkMetered(NetworkState networkState) throws RemoteException;

    boolean isUidForeground(int i) throws RemoteException;

    void onTetheringChanged(String str, boolean z) throws RemoteException;

    void registerListener(INetworkPolicyListener iNetworkPolicyListener) throws RemoteException;

    void removeRestrictBackgroundWhitelistedUid(int i) throws RemoteException;

    void removeUidPolicy(int i, int i2) throws RemoteException;

    void setConnectivityListener(INetworkPolicyListener iNetworkPolicyListener) throws RemoteException;

    void setDeviceIdleMode(boolean z) throws RemoteException;

    void setNetworkPolicies(NetworkPolicy[] networkPolicyArr) throws RemoteException;

    void setRestrictBackground(boolean z) throws RemoteException;

    void setUidPolicy(int i, int i2) throws RemoteException;

    void snoozeLimit(NetworkTemplate networkTemplate) throws RemoteException;

    void unregisterListener(INetworkPolicyListener iNetworkPolicyListener) throws RemoteException;
}
