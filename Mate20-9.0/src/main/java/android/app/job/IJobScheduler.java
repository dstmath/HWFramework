package android.app.job;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IJobScheduler extends IInterface {

    public static abstract class Stub extends Binder implements IJobScheduler {
        private static final String DESCRIPTOR = "android.app.job.IJobScheduler";
        static final int TRANSACTION_cancel = 4;
        static final int TRANSACTION_cancelAll = 5;
        static final int TRANSACTION_enqueue = 2;
        static final int TRANSACTION_getAllPendingJobs = 6;
        static final int TRANSACTION_getPendingJob = 7;
        static final int TRANSACTION_schedule = 1;
        static final int TRANSACTION_scheduleAsPackage = 3;

        private static class Proxy implements IJobScheduler {
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

            public int schedule(JobInfo job) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (job != null) {
                        _data.writeInt(1);
                        job.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int enqueue(JobInfo job, JobWorkItem work) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (job != null) {
                        _data.writeInt(1);
                        job.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (work != null) {
                        _data.writeInt(1);
                        work.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int scheduleAsPackage(JobInfo job, String packageName, int userId, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (job != null) {
                        _data.writeInt(1);
                        job.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeString(tag);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancel(int jobId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(jobId);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelAll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<JobInfo> getAllPendingJobs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(JobInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public JobInfo getPendingJob(int jobId) throws RemoteException {
                JobInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(jobId);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = JobInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
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

        public static IJobScheduler asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IJobScheduler)) {
                return new Proxy(obj);
            }
            return (IJobScheduler) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: android.app.job.JobInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: android.app.job.JobWorkItem} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v10, resolved type: android.app.job.JobInfo} */
        /* JADX WARNING: type inference failed for: r1v1 */
        /* JADX WARNING: type inference failed for: r1v18 */
        /* JADX WARNING: type inference failed for: r1v19 */
        /* JADX WARNING: type inference failed for: r1v20 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            JobInfo _arg0;
            if (code != 1598968902) {
                ? _arg02 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = JobInfo.CREATOR.createFromParcel(data);
                        }
                        int _result = schedule(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = JobInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg02 = JobWorkItem.CREATOR.createFromParcel(data);
                        }
                        int _result2 = enqueue(_arg0, _arg02);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = JobInfo.CREATOR.createFromParcel(data);
                        }
                        int _result3 = scheduleAsPackage(_arg02, data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        cancel(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        cancelAll();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        List<JobInfo> _result4 = getAllPendingJobs();
                        reply.writeNoException();
                        reply.writeTypedList(_result4);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        JobInfo _result5 = getPendingJob(data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
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

    void cancel(int i) throws RemoteException;

    void cancelAll() throws RemoteException;

    int enqueue(JobInfo jobInfo, JobWorkItem jobWorkItem) throws RemoteException;

    List<JobInfo> getAllPendingJobs() throws RemoteException;

    JobInfo getPendingJob(int i) throws RemoteException;

    int schedule(JobInfo jobInfo) throws RemoteException;

    int scheduleAsPackage(JobInfo jobInfo, String str, int i, String str2) throws RemoteException;
}
