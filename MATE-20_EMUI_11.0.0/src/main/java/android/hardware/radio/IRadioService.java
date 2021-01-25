package android.hardware.radio;

import android.hardware.radio.IAnnouncementListener;
import android.hardware.radio.ICloseHandle;
import android.hardware.radio.ITuner;
import android.hardware.radio.ITunerCallback;
import android.hardware.radio.RadioManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IRadioService extends IInterface {
    ICloseHandle addAnnouncementListener(int[] iArr, IAnnouncementListener iAnnouncementListener) throws RemoteException;

    List<RadioManager.ModuleProperties> listModules() throws RemoteException;

    ITuner openTuner(int i, RadioManager.BandConfig bandConfig, boolean z, ITunerCallback iTunerCallback) throws RemoteException;

    public static class Default implements IRadioService {
        @Override // android.hardware.radio.IRadioService
        public List<RadioManager.ModuleProperties> listModules() throws RemoteException {
            return null;
        }

        @Override // android.hardware.radio.IRadioService
        public ITuner openTuner(int moduleId, RadioManager.BandConfig bandConfig, boolean withAudio, ITunerCallback callback) throws RemoteException {
            return null;
        }

        @Override // android.hardware.radio.IRadioService
        public ICloseHandle addAnnouncementListener(int[] enabledTypes, IAnnouncementListener listener) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IRadioService {
        private static final String DESCRIPTOR = "android.hardware.radio.IRadioService";
        static final int TRANSACTION_addAnnouncementListener = 3;
        static final int TRANSACTION_listModules = 1;
        static final int TRANSACTION_openTuner = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRadioService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRadioService)) {
                return new Proxy(obj);
            }
            return (IRadioService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "listModules";
            }
            if (transactionCode == 2) {
                return "openTuner";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "addAnnouncementListener";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            RadioManager.BandConfig _arg1;
            if (code != 1) {
                IBinder iBinder = null;
                if (code == 2) {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg1 = RadioManager.BandConfig.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    ITuner _result = openTuner(_arg0, _arg1, data.readInt() != 0, ITunerCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result != null) {
                        iBinder = _result.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                } else if (code == 3) {
                    data.enforceInterface(DESCRIPTOR);
                    ICloseHandle _result2 = addAnnouncementListener(data.createIntArray(), IAnnouncementListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result2 != null) {
                        iBinder = _result2.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                } else if (code != 1598968902) {
                    return super.onTransact(code, data, reply, flags);
                } else {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
            } else {
                data.enforceInterface(DESCRIPTOR);
                List<RadioManager.ModuleProperties> _result3 = listModules();
                reply.writeNoException();
                reply.writeTypedList(_result3);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IRadioService {
            public static IRadioService sDefaultImpl;
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

            @Override // android.hardware.radio.IRadioService
            public List<RadioManager.ModuleProperties> listModules() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().listModules();
                    }
                    _reply.readException();
                    List<RadioManager.ModuleProperties> _result = _reply.createTypedArrayList(RadioManager.ModuleProperties.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.radio.IRadioService
            public ITuner openTuner(int moduleId, RadioManager.BandConfig bandConfig, boolean withAudio, ITunerCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(moduleId);
                    int i = 1;
                    if (bandConfig != null) {
                        _data.writeInt(1);
                        bandConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!withAudio) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openTuner(moduleId, bandConfig, withAudio, callback);
                    }
                    _reply.readException();
                    ITuner _result = ITuner.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.radio.IRadioService
            public ICloseHandle addAnnouncementListener(int[] enabledTypes, IAnnouncementListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(enabledTypes);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addAnnouncementListener(enabledTypes, listener);
                    }
                    _reply.readException();
                    ICloseHandle _result = ICloseHandle.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRadioService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IRadioService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
