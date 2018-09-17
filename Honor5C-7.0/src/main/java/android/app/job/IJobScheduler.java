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
        static final int TRANSACTION_cancel = 3;
        static final int TRANSACTION_cancelAll = 4;
        static final int TRANSACTION_getAllPendingJobs = 5;
        static final int TRANSACTION_getPendingJob = 6;
        static final int TRANSACTION_schedule = 1;
        static final int TRANSACTION_scheduleAsPackage = 2;

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
                        _data.writeInt(Stub.TRANSACTION_schedule);
                        job.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_schedule, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_schedule);
                        job.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeString(tag);
                    this.mRemote.transact(Stub.TRANSACTION_scheduleAsPackage, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_cancel, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_cancelAll, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getAllPendingJobs, _data, _reply, 0);
                    _reply.readException();
                    List<JobInfo> _result = _reply.createTypedArrayList(JobInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public JobInfo getPendingJob(int jobId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    JobInfo jobInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(jobId);
                    this.mRemote.transact(Stub.TRANSACTION_getPendingJob, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        jobInfo = (JobInfo) JobInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        jobInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return jobInfo;
                } catch (Throwable th) {
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            JobInfo jobInfo;
            int _result;
            switch (code) {
                case TRANSACTION_schedule /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        jobInfo = (JobInfo) JobInfo.CREATOR.createFromParcel(data);
                    } else {
                        jobInfo = null;
                    }
                    _result = schedule(jobInfo);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_scheduleAsPackage /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        jobInfo = (JobInfo) JobInfo.CREATOR.createFromParcel(data);
                    } else {
                        jobInfo = null;
                    }
                    _result = scheduleAsPackage(jobInfo, data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_cancel /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancel(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelAll /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelAll();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getAllPendingJobs /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<JobInfo> _result2 = getAllPendingJobs();
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_getPendingJob /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    JobInfo _result3 = getPendingJob(data.readInt());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_schedule);
                        _result3.writeToParcel(reply, TRANSACTION_schedule);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void cancel(int i) throws RemoteException;

    void cancelAll() throws RemoteException;

    List<JobInfo> getAllPendingJobs() throws RemoteException;

    JobInfo getPendingJob(int i) throws RemoteException;

    int schedule(JobInfo jobInfo) throws RemoteException;

    int scheduleAsPackage(JobInfo jobInfo, String str, int i, String str2) throws RemoteException;
}
