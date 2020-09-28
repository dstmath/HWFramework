package com.huawei.android.hardware.fmradio;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor;
import com.huawei.android.hardware.fmradio.IFmEventCallback;
import com.huawei.android.hardware.mtkfmradio.MtkFmConfig;

public interface IHwFmService extends IInterface {
    int acquireFd(String str) throws RemoteException;

    int audioControl(int i, int i2, int i3) throws RemoteException;

    int cancelSearch(int i) throws RemoteException;

    int closeFd(int i) throws RemoteException;

    boolean disable() throws RemoteException;

    boolean enable(MtkFmConfig mtkFmConfig, IBinder iBinder) throws RemoteException;

    int[] getAfInfo() throws RemoteException;

    int getAudioQuilty(int i, int i2) throws RemoteException;

    int getBuffer(int i, byte[] bArr, int i2) throws RemoteException;

    int getControl(int i, int i2) throws RemoteException;

    int getFreq(int i) throws RemoteException;

    int getLowerBand(int i) throws RemoteException;

    int getPrgmId() throws RemoteException;

    String getPrgmServices() throws RemoteException;

    int getPrgmType() throws RemoteException;

    int getRSSI(int i) throws RemoteException;

    String getRadioText() throws RemoteException;

    int getRawRds(int i, byte[] bArr, int i2) throws RemoteException;

    int getRdsStatus() throws RemoteException;

    int getRssi() throws RemoteException;

    int getUpperBand(int i) throws RemoteException;

    boolean mtkCancelSearch() throws RemoteException;

    void registerListener(IBaseFmRxEvCallbacksAdaptor iBaseFmRxEvCallbacksAdaptor) throws RemoteException;

    boolean registerRdsGroupProcessing(int i) throws RemoteException;

    boolean searchStations(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    int setBand(int i, int i2, int i3) throws RemoteException;

    int setControl(int i, int i2, int i3) throws RemoteException;

    void setFmDeviceConnectionState(int i) throws RemoteException;

    int setFmRssiThresh(int i, int i2) throws RemoteException;

    int setFmSnrThresh(int i, int i2) throws RemoteException;

    int setFreq(int i, int i2) throws RemoteException;

    boolean setLowPwrMode(boolean z) throws RemoteException;

    int setMonoStereo(int i, int i2) throws RemoteException;

    void setNotchFilter(boolean z) throws RemoteException;

    boolean setRdsOnOff(int i) throws RemoteException;

    boolean setStation(int i) throws RemoteException;

    void startListner(int i, IFmEventCallback iFmEventCallback) throws RemoteException;

    int startSearch(int i, int i2) throws RemoteException;

    void stopListner() throws RemoteException;

    void unregisterListener(IBaseFmRxEvCallbacksAdaptor iBaseFmRxEvCallbacksAdaptor) throws RemoteException;

    public static abstract class Stub extends Binder implements IHwFmService {
        private static final String DESCRIPTOR = "com.huawei.android.hardware.fmradio.IHwFmService";
        static final int TRANSACTION_acquireFd = 1;
        static final int TRANSACTION_audioControl = 2;
        static final int TRANSACTION_cancelSearch = 3;
        static final int TRANSACTION_closeFd = 4;
        static final int TRANSACTION_disable = 25;
        static final int TRANSACTION_enable = 24;
        static final int TRANSACTION_getAfInfo = 39;
        static final int TRANSACTION_getAudioQuilty = 18;
        static final int TRANSACTION_getBuffer = 10;
        static final int TRANSACTION_getControl = 7;
        static final int TRANSACTION_getFreq = 5;
        static final int TRANSACTION_getLowerBand = 13;
        static final int TRANSACTION_getPrgmId = 33;
        static final int TRANSACTION_getPrgmServices = 30;
        static final int TRANSACTION_getPrgmType = 32;
        static final int TRANSACTION_getRSSI = 11;
        static final int TRANSACTION_getRadioText = 31;
        static final int TRANSACTION_getRawRds = 16;
        static final int TRANSACTION_getRdsStatus = 37;
        static final int TRANSACTION_getRssi = 38;
        static final int TRANSACTION_getUpperBand = 14;
        static final int TRANSACTION_mtkCancelSearch = 26;
        static final int TRANSACTION_registerListener = 34;
        static final int TRANSACTION_registerRdsGroupProcessing = 40;
        static final int TRANSACTION_searchStations = 27;
        static final int TRANSACTION_setBand = 12;
        static final int TRANSACTION_setControl = 8;
        static final int TRANSACTION_setFmDeviceConnectionState = 21;
        static final int TRANSACTION_setFmRssiThresh = 20;
        static final int TRANSACTION_setFmSnrThresh = 19;
        static final int TRANSACTION_setFreq = 6;
        static final int TRANSACTION_setLowPwrMode = 36;
        static final int TRANSACTION_setMonoStereo = 15;
        static final int TRANSACTION_setNotchFilter = 17;
        static final int TRANSACTION_setRdsOnOff = 29;
        static final int TRANSACTION_setStation = 28;
        static final int TRANSACTION_startListner = 22;
        static final int TRANSACTION_startSearch = 9;
        static final int TRANSACTION_stopListner = 23;
        static final int TRANSACTION_unregisterListener = 35;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwFmService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwFmService)) {
                return new Proxy(obj);
            }
            return (IHwFmService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            MtkFmConfig _arg0;
            if (code != 1598968902) {
                boolean _arg02 = false;
                boolean _arg03 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = acquireFd(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = audioControl(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = cancelSearch(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = closeFd(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getFreq(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = setFreq(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = getControl(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = setControl(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = startSearch(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        byte[] _arg1 = data.createByteArray();
                        int _result10 = getBuffer(_arg04, _arg1, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        reply.writeByteArray(_arg1);
                        return true;
                    case TRANSACTION_getRSSI /*{ENCODED_INT: 11}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getRSSI(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = setBand(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case TRANSACTION_getLowerBand /*{ENCODED_INT: 13}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = getLowerBand(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case TRANSACTION_getUpperBand /*{ENCODED_INT: 14}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = getUpperBand(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case TRANSACTION_setMonoStereo /*{ENCODED_INT: 15}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = setMonoStereo(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        byte[] _arg12 = data.createByteArray();
                        int _result16 = getRawRds(_arg05, _arg12, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        reply.writeByteArray(_arg12);
                        return true;
                    case TRANSACTION_setNotchFilter /*{ENCODED_INT: 17}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setNotchFilter(_arg02);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getAudioQuilty /*{ENCODED_INT: 18}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = getAudioQuilty(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case TRANSACTION_setFmSnrThresh /*{ENCODED_INT: 19}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = setFmSnrThresh(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case TRANSACTION_setFmRssiThresh /*{ENCODED_INT: 20}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = setFmRssiThresh(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case TRANSACTION_setFmDeviceConnectionState /*{ENCODED_INT: 21}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setFmDeviceConnectionState(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_startListner /*{ENCODED_INT: 22}*/:
                        data.enforceInterface(DESCRIPTOR);
                        startListner(data.readInt(), IFmEventCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_stopListner /*{ENCODED_INT: 23}*/:
                        data.enforceInterface(DESCRIPTOR);
                        stopListner();
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_enable /*{ENCODED_INT: 24}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = MtkFmConfig.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean enable = enable(_arg0, data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(enable ? 1 : 0);
                        return true;
                    case TRANSACTION_disable /*{ENCODED_INT: 25}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disable = disable();
                        reply.writeNoException();
                        reply.writeInt(disable ? 1 : 0);
                        return true;
                    case TRANSACTION_mtkCancelSearch /*{ENCODED_INT: 26}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean mtkCancelSearch = mtkCancelSearch();
                        reply.writeNoException();
                        reply.writeInt(mtkCancelSearch ? 1 : 0);
                        return true;
                    case TRANSACTION_searchStations /*{ENCODED_INT: 27}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean searchStations = searchStations(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(searchStations ? 1 : 0);
                        return true;
                    case TRANSACTION_setStation /*{ENCODED_INT: 28}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean station = setStation(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(station ? 1 : 0);
                        return true;
                    case TRANSACTION_setRdsOnOff /*{ENCODED_INT: 29}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean rdsOnOff = setRdsOnOff(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(rdsOnOff ? 1 : 0);
                        return true;
                    case TRANSACTION_getPrgmServices /*{ENCODED_INT: 30}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String _result20 = getPrgmServices();
                        reply.writeNoException();
                        reply.writeString(_result20);
                        return true;
                    case TRANSACTION_getRadioText /*{ENCODED_INT: 31}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String _result21 = getRadioText();
                        reply.writeNoException();
                        reply.writeString(_result21);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        int _result22 = getPrgmType();
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case TRANSACTION_getPrgmId /*{ENCODED_INT: 33}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result23 = getPrgmId();
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case TRANSACTION_registerListener /*{ENCODED_INT: 34}*/:
                        data.enforceInterface(DESCRIPTOR);
                        registerListener(IBaseFmRxEvCallbacksAdaptor.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_unregisterListener /*{ENCODED_INT: 35}*/:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterListener(IBaseFmRxEvCallbacksAdaptor.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setLowPwrMode /*{ENCODED_INT: 36}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        boolean lowPwrMode = setLowPwrMode(_arg03);
                        reply.writeNoException();
                        reply.writeInt(lowPwrMode ? 1 : 0);
                        return true;
                    case TRANSACTION_getRdsStatus /*{ENCODED_INT: 37}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result24 = getRdsStatus();
                        reply.writeNoException();
                        reply.writeInt(_result24);
                        return true;
                    case TRANSACTION_getRssi /*{ENCODED_INT: 38}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result25 = getRssi();
                        reply.writeNoException();
                        reply.writeInt(_result25);
                        return true;
                    case TRANSACTION_getAfInfo /*{ENCODED_INT: 39}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result26 = getAfInfo();
                        reply.writeNoException();
                        reply.writeIntArray(_result26);
                        return true;
                    case TRANSACTION_registerRdsGroupProcessing /*{ENCODED_INT: 40}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerRdsGroupProcessing = registerRdsGroupProcessing(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(registerRdsGroupProcessing ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IHwFmService {
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

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int acquireFd(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int audioControl(int fd, int control, int field) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeInt(control);
                    _data.writeInt(field);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int cancelSearch(int fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int closeFd(int fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int getFreq(int fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int setFreq(int fd, int freq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeInt(freq);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int getControl(int fd, int id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeInt(id);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int setControl(int fd, int id, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeInt(id);
                    _data.writeInt(value);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int startSearch(int fd, int dir) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeInt(dir);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int getBuffer(int fd, byte[] buff, int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeByteArray(buff);
                    _data.writeInt(index);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(buff);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int getRSSI(int fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    this.mRemote.transact(Stub.TRANSACTION_getRSSI, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int setBand(int fd, int low, int high) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeInt(low);
                    _data.writeInt(high);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int getLowerBand(int fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    this.mRemote.transact(Stub.TRANSACTION_getLowerBand, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int getUpperBand(int fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    this.mRemote.transact(Stub.TRANSACTION_getUpperBand, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int setMonoStereo(int fd, int val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeInt(val);
                    this.mRemote.transact(Stub.TRANSACTION_setMonoStereo, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int getRawRds(int fd, byte[] buff, int count) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeByteArray(buff);
                    _data.writeInt(count);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(buff);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public void setNotchFilter(boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_setNotchFilter, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int getAudioQuilty(int fd, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeInt(value);
                    this.mRemote.transact(Stub.TRANSACTION_getAudioQuilty, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int setFmSnrThresh(int fd, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeInt(value);
                    this.mRemote.transact(Stub.TRANSACTION_setFmSnrThresh, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int setFmRssiThresh(int fd, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeInt(value);
                    this.mRemote.transact(Stub.TRANSACTION_setFmRssiThresh, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public void setFmDeviceConnectionState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(Stub.TRANSACTION_setFmDeviceConnectionState, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public void startListner(int fd, IFmEventCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fd);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(Stub.TRANSACTION_startListner, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public void stopListner() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopListner, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public boolean enable(MtkFmConfig configSettings, IBinder callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (configSettings != null) {
                        _data.writeInt(1);
                        configSettings.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback);
                    this.mRemote.transact(Stub.TRANSACTION_enable, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public boolean disable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_disable, _data, _reply, 0);
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

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public boolean mtkCancelSearch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_mtkCancelSearch, _data, _reply, 0);
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

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public boolean searchStations(int mode, int dwellPeriod, int direction, int pty, int pi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeInt(dwellPeriod);
                    _data.writeInt(direction);
                    _data.writeInt(pty);
                    _data.writeInt(pi);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_searchStations, _data, _reply, 0);
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

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public boolean setStation(int frequencyKHz) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(frequencyKHz);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_setStation, _data, _reply, 0);
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

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public boolean setRdsOnOff(int onOff) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(onOff);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_setRdsOnOff, _data, _reply, 0);
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

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public String getPrgmServices() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPrgmServices, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public String getRadioText() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getRadioText, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int getPrgmType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int getPrgmId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPrgmId, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public void registerListener(IBaseFmRxEvCallbacksAdaptor listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(Stub.TRANSACTION_registerListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public void unregisterListener(IBaseFmRxEvCallbacksAdaptor listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public boolean setLowPwrMode(boolean val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(val ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_setLowPwrMode, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int getRdsStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getRdsStatus, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int getRssi() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getRssi, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public int[] getAfInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAfInfo, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IHwFmService
            public boolean registerRdsGroupProcessing(int fmGrpsToProc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fmGrpsToProc);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_registerRdsGroupProcessing, _data, _reply, 0);
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
        }
    }
}
