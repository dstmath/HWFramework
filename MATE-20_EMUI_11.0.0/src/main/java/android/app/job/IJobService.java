package android.app.job;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IJobService extends IInterface {
    @UnsupportedAppUsage
    void startJob(JobParameters jobParameters) throws RemoteException;

    @UnsupportedAppUsage
    void stopJob(JobParameters jobParameters) throws RemoteException;

    public static class Default implements IJobService {
        @Override // android.app.job.IJobService
        public void startJob(JobParameters jobParams) throws RemoteException {
        }

        @Override // android.app.job.IJobService
        public void stopJob(JobParameters jobParams) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IJobService {
        private static final String DESCRIPTOR = "android.app.job.IJobService";
        static final int TRANSACTION_startJob = 1;
        static final int TRANSACTION_stopJob = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IJobService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IJobService)) {
                return new Proxy(obj);
            }
            return (IJobService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "startJob";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "stopJob";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            JobParameters _arg0;
            JobParameters _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = JobParameters.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                startJob(_arg0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = JobParameters.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                stopJob(_arg02);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IJobService {
            public static IJobService sDefaultImpl;
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

            @Override // android.app.job.IJobService
            public void startJob(JobParameters jobParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (jobParams != null) {
                        _data.writeInt(1);
                        jobParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startJob(jobParams);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.job.IJobService
            public void stopJob(JobParameters jobParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (jobParams != null) {
                        _data.writeInt(1);
                        jobParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().stopJob(jobParams);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IJobService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IJobService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
