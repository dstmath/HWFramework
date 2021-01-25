package ohos.rpc;

import java.io.FileDescriptor;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import ohos.utils.Parcel;

public class MessageParcel extends Parcel {
    private static final int EXC_ARRAY_STORE = -12;
    private static final int EXC_CLASS_CAST = -13;
    private static final int EXC_ILLEGAL_ARGUMENT = -3;
    private static final int EXC_ILLEGAL_STATE = -5;
    private static final int EXC_INDEX_OUTOF_BOUNDS = -10;
    private static final int EXC_INSECURITY = -1;
    private static final int EXC_NEGATIVE_ARRAY_SIZE = -11;
    private static final int EXC_NULL_POINTER = -4;
    private static final int EXC_REMOTE_TRANSACTION_FAILED = -200;
    private static final int EXC_UNSUPPORTED_OPERATION = -7;
    private static final MessageParcel[] HOLDER_POOL = new MessageParcel[8];
    private static final int POOL_SIZE = 8;
    private long mNativeObject;
    private boolean mOwnsNativeObject;

    private native void nativeCloseFileDescriptor(FileDescriptor fileDescriptor);

    private static native FileDescriptor nativeDupFileDescriptor(FileDescriptor fileDescriptor);

    private native void nativeFreeObject(long j);

    private native int nativeGetRawDataCapacity();

    private native long nativeNewObject(long j);

    private native FileDescriptor nativeReadFileDescriptor();

    private native String nativeReadInterfaceToken();

    private native byte[] nativeReadRawData(int i);

    private native IRemoteObject nativeReadRemoteObject();

    private native boolean nativeWriteFileDescriptor(FileDescriptor fileDescriptor);

    private native boolean nativeWriteInterfaceToken(String str, int i);

    private native boolean nativeWriteRawData(byte[] bArr, int i);

    private native boolean nativeWriteRemoteObject(IRemoteObject iRemoteObject);

    MessageParcel(long j) {
        initNativeObject(j);
    }

    private void initNativeObject(long j) {
        this.mNativeObject = nativeNewObject(j);
    }

    public static final MessageParcel obtain() {
        return new MessageParcel(0);
    }

    public static final MessageParcel create() {
        return new MessageParcel(0);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.utils.Parcel
    public void finalize() throws Throwable {
        if (this.mOwnsNativeObject) {
            nativeFreeObject(this.mNativeObject);
        }
        super.finalize();
    }

    public static MessageParcel obtain(long j) {
        synchronized (HOLDER_POOL) {
            for (int i = 0; i < 8; i++) {
                if (HOLDER_POOL[i] != null) {
                    MessageParcel messageParcel = HOLDER_POOL[i];
                    HOLDER_POOL[i] = null;
                    messageParcel.initNativeObject(j);
                    return messageParcel;
                }
            }
            return new MessageParcel(j);
        }
    }

    public static MessageParcel create(long j) {
        synchronized (HOLDER_POOL) {
            for (int i = 0; i < 8; i++) {
                if (HOLDER_POOL[i] != null) {
                    MessageParcel messageParcel = HOLDER_POOL[i];
                    HOLDER_POOL[i] = null;
                    messageParcel.initNativeObject(j);
                    return messageParcel;
                }
            }
            return new MessageParcel(j);
        }
    }

    @Override // ohos.utils.Parcel
    public final void reclaim() {
        if (this.mOwnsNativeObject) {
            flushBuffer();
            return;
        }
        this.mNativeObject = 0;
        synchronized (HOLDER_POOL) {
            int i = 0;
            while (true) {
                if (i >= 8) {
                    break;
                } else if (HOLDER_POOL[i] == null) {
                    HOLDER_POOL[i] = this;
                    break;
                } else {
                    i++;
                }
            }
        }
    }

    public boolean writeRemoteObject(IRemoteObject iRemoteObject) {
        return nativeWriteRemoteObject(iRemoteObject);
    }

    public IRemoteObject readRemoteObject() {
        return nativeReadRemoteObject();
    }

    public boolean writeInterfaceToken(String str) {
        if (str == null) {
            return false;
        }
        return nativeWriteInterfaceToken(str, str.length());
    }

    public String readInterfaceToken() {
        return nativeReadInterfaceToken();
    }

    public boolean writeFileDescriptor(FileDescriptor fileDescriptor) {
        return nativeWriteFileDescriptor(fileDescriptor);
    }

    public FileDescriptor readFileDescriptor() {
        return nativeReadFileDescriptor();
    }

    public boolean writeRawData(byte[] bArr, int i) {
        return nativeWriteRawData(bArr, i);
    }

    public byte[] readRawData(int i) {
        return nativeReadRawData(i);
    }

    public int getRawDataCapacity() {
        return nativeGetRawDataCapacity();
    }

    public void closeFileDescriptor(FileDescriptor fileDescriptor) {
        nativeCloseFileDescriptor(fileDescriptor);
    }

    public static FileDescriptor dupFileDescriptor(FileDescriptor fileDescriptor) {
        return nativeDupFileDescriptor(fileDescriptor);
    }

    public IRemoteObject[] createRemoteObjectArray() {
        int readInt = readInt();
        if (readInt <= 0) {
            return null;
        }
        IRemoteObject[] iRemoteObjectArr = new IRemoteObject[readInt];
        for (int i = 0; i < readInt; i++) {
            iRemoteObjectArr[i] = readRemoteObject();
        }
        return iRemoteObjectArr;
    }

    public List<IRemoteObject> createRemoteObjectList() {
        int readInt = readInt();
        if (readInt <= 0) {
            return null;
        }
        ArrayList arrayList = new ArrayList(readInt);
        for (int i = 0; i < readInt; i++) {
            arrayList.add(readRemoteObject());
        }
        return arrayList;
    }

    public boolean writeRemoteObjectArray(IRemoteObject[] iRemoteObjectArr) {
        if (iRemoteObjectArr == null) {
            writeInt(-1);
            return false;
        }
        int length = iRemoteObjectArr.length;
        writeInt(length);
        for (IRemoteObject iRemoteObject : iRemoteObjectArr) {
            writeRemoteObject(iRemoteObject);
        }
        return true;
    }

    public boolean writeRemoteObjectList(List<IRemoteObject> list) {
        if (list == null) {
            writeInt(-1);
            return false;
        }
        int size = list.size();
        writeInt(size);
        for (int i = 0; i < size; i++) {
            writeRemoteObject(list.get(i));
        }
        return true;
    }

    public void readRemoteObjectArray(IRemoteObject[] iRemoteObjectArr) {
        int readInt;
        if (iRemoteObjectArr != null && (readInt = readInt()) == iRemoteObjectArr.length) {
            for (int i = 0; i < readInt; i++) {
                iRemoteObjectArr[i] = readRemoteObject();
            }
        }
    }

    public void readRemoteObjectList(List<IRemoteObject> list) {
        if (list != null) {
            int readInt = readInt();
            int size = list.size();
            int i = 0;
            while (i < size && i < readInt) {
                list.set(i, readRemoteObject());
                i++;
            }
            while (i < readInt) {
                list.add(readRemoteObject());
                i++;
            }
            while (i < size) {
                list.remove(readInt);
                i++;
            }
        }
    }

    private Exception createException(int i, String str) {
        if (i == EXC_REMOTE_TRANSACTION_FAILED) {
            return new RemoteException(str);
        }
        if (i == -7) {
            return new UnsupportedOperationException(str);
        }
        if (i == -1) {
            return new SecurityException(str);
        }
        if (i == -5) {
            return new IllegalStateException(str);
        }
        if (i == -4) {
            return new NullPointerException(str);
        }
        if (i == -3) {
            return new IllegalArgumentException(str);
        }
        switch (i) {
            case EXC_CLASS_CAST /* -13 */:
                return new ClassCastException(str);
            case EXC_ARRAY_STORE /* -12 */:
                return new ArrayStoreException(str);
            case EXC_NEGATIVE_ARRAY_SIZE /* -11 */:
                return new NegativeArraySizeException(str);
            case -10:
                return new IndexOutOfBoundsException(str);
            default:
                return new RuntimeException("Unknown exception code: " + i + " msg " + str);
        }
    }

    public void writeException(Exception exc) {
        int i;
        if (exc instanceof SecurityException) {
            i = -1;
        } else if (exc instanceof IllegalArgumentException) {
            i = -3;
        } else if (exc instanceof NullPointerException) {
            i = -4;
        } else if (exc instanceof IllegalStateException) {
            i = -5;
        } else if (exc instanceof UnsupportedOperationException) {
            i = -7;
        } else if (exc instanceof IndexOutOfBoundsException) {
            i = -10;
        } else if (exc instanceof NegativeArraySizeException) {
            i = EXC_NEGATIVE_ARRAY_SIZE;
        } else if (exc instanceof ArrayStoreException) {
            i = EXC_ARRAY_STORE;
        } else if (exc instanceof ClassCastException) {
            i = EXC_CLASS_CAST;
        } else {
            i = exc instanceof RemoteException ? EXC_REMOTE_TRANSACTION_FAILED : 0;
        }
        writeInt(i);
        if (i != 0) {
            writeString(exc.getMessage());
        } else if (exc instanceof RuntimeException) {
            throw ((RuntimeException) exc);
        } else {
            throw new RuntimeException(exc);
        }
    }

    public void writeNoException() {
        writeInt(0);
    }

    public void readException() {
        int readInt = readInt();
        if (readInt != 0) {
            QuietThrow.quietThrow(createException(readInt, readString()));
        }
    }

    public static FileDescriptor getFdFromDatagramSocket(DatagramSocket datagramSocket) {
        FileDescriptor fileDescriptor$;
        if (datagramSocket == null || (fileDescriptor$ = datagramSocket.getFileDescriptor$()) == null) {
            return null;
        }
        return dupFileDescriptor(fileDescriptor$);
    }

    public static int getFd(FileDescriptor fileDescriptor) {
        if (fileDescriptor == null) {
            return -1;
        }
        return fileDescriptor.getInt$();
    }

    public static int detachFd(FileDescriptor fileDescriptor) {
        FileDescriptor release$;
        if (fileDescriptor == null || (release$ = fileDescriptor.release$()) == null) {
            return -1;
        }
        int int$ = release$.getInt$();
        long ownerId$ = release$.getOwnerId$();
        if (!(int$ == -1 || ownerId$ == 0)) {
            release$.setOwnerId$(0);
        }
        return int$;
    }

    private static class QuietThrow {
        private QuietThrow() {
        }

        public static void quietThrow(Throwable th) {
            quietThrowInner(th);
        }

        private static <T extends Throwable> void quietThrowInner(Throwable th) throws Throwable {
            throw th;
        }
    }
}
