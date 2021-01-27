package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INetworkPolicyListener extends IInterface {
    void onMeteredIfacesChanged(String[] strArr) throws RemoteException;

    void onRestrictBackgroundChanged(boolean z) throws RemoteException;

    void onSubscriptionOverride(int i, int i2, int i3) throws RemoteException;

    void onUidPoliciesChanged(int i, int i2) throws RemoteException;

    void onUidRulesChanged(int i, int i2) throws RemoteException;

    public static class Default implements INetworkPolicyListener {
        @Override // android.net.INetworkPolicyListener
        public void onUidRulesChanged(int uid, int uidRules) throws RemoteException {
        }

        @Override // android.net.INetworkPolicyListener
        public void onMeteredIfacesChanged(String[] meteredIfaces) throws RemoteException {
        }

        @Override // android.net.INetworkPolicyListener
        public void onRestrictBackgroundChanged(boolean restrictBackground) throws RemoteException {
        }

        @Override // android.net.INetworkPolicyListener
        public void onUidPoliciesChanged(int uid, int uidPolicies) throws RemoteException {
        }

        @Override // android.net.INetworkPolicyListener
        public void onSubscriptionOverride(int subId, int overrideMask, int overrideValue) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INetworkPolicyListener {
        private static final String DESCRIPTOR = "android.net.INetworkPolicyListener";
        static final int TRANSACTION_onMeteredIfacesChanged = 2;
        static final int TRANSACTION_onRestrictBackgroundChanged = 3;
        static final int TRANSACTION_onSubscriptionOverride = 5;
        static final int TRANSACTION_onUidPoliciesChanged = 4;
        static final int TRANSACTION_onUidRulesChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetworkPolicyListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetworkPolicyListener)) {
                return new Proxy(obj);
            }
            return (INetworkPolicyListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onUidRulesChanged";
            }
            if (transactionCode == 2) {
                return "onMeteredIfacesChanged";
            }
            if (transactionCode == 3) {
                return "onRestrictBackgroundChanged";
            }
            if (transactionCode == 4) {
                return "onUidPoliciesChanged";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "onSubscriptionOverride";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onUidRulesChanged(data.readInt(), data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onMeteredIfacesChanged(data.createStringArray());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onRestrictBackgroundChanged(data.readInt() != 0);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onUidPoliciesChanged(data.readInt(), data.readInt());
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                onSubscriptionOverride(data.readInt(), data.readInt(), data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements INetworkPolicyListener {
            public static INetworkPolicyListener sDefaultImpl;
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

            @Override // android.net.INetworkPolicyListener
            public void onUidRulesChanged(int uid, int uidRules) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(uidRules);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUidRulesChanged(uid, uidRules);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkPolicyListener
            public void onMeteredIfacesChanged(String[] meteredIfaces) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(meteredIfaces);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onMeteredIfacesChanged(meteredIfaces);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkPolicyListener
            public void onRestrictBackgroundChanged(boolean restrictBackground) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(restrictBackground ? 1 : 0);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRestrictBackgroundChanged(restrictBackground);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkPolicyListener
            public void onUidPoliciesChanged(int uid, int uidPolicies) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(uidPolicies);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUidPoliciesChanged(uid, uidPolicies);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkPolicyListener
            public void onSubscriptionOverride(int subId, int overrideMask, int overrideValue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(overrideMask);
                    _data.writeInt(overrideValue);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSubscriptionOverride(subId, overrideMask, overrideValue);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INetworkPolicyListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INetworkPolicyListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
