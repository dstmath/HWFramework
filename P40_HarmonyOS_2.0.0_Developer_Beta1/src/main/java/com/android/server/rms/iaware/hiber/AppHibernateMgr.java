package com.android.server.rms.iaware.hiber;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Parcel;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.system.ErrnoException;
import com.android.server.rms.iaware.CommonUtils;
import com.android.server.rms.iaware.hiber.bean.HiberBean;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.android.system.ErrnoExceptionEx;
import com.huawei.android.system.Int32RefEx;
import com.huawei.android.system.OsConstantsEx;
import com.huawei.android.system.OsEx;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/* access modifiers changed from: package-private */
public class AppHibernateMgr {
    private static final int CMD_HEAD_LEN = 8;
    private static final long FLUSH_TIMEOUT = 2000;
    private static final Object LOCK = new Object();
    private static final int RECEIVE_TIMEOUT = 4000;
    private static final int RECV_BYTE_BUFFER_LENTH = 10240;
    private static final int RECV_BYTE_DUMP_BYTE_LENTH = 10240;
    private static final int RECV_BYTE_RECLIAM_BYTE_LENTH = 256;
    private static final String TAG = "AppHiber_Mgr";
    private static final int VAL_PARCELABLE_INT = 4;
    private static AppHibernateMgr sInstance;
    private LocalSocket mAppHiberSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private final ReentrantReadWriteLock mSocketLock = new ReentrantReadWriteLock();

    AppHibernateMgr() {
    }

    protected static AppHibernateMgr getInstance() {
        AppHibernateMgr appHibernateMgr;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new AppHibernateMgr();
            }
            appHibernateMgr = sInstance;
        }
        return appHibernateMgr;
    }

    /* access modifiers changed from: protected */
    public int doHiberFrzApi(String pkgName, int[] pidArray, int frzState) {
        if (pkgName == null || pkgName.trim().isEmpty() || pidArray == null || pidArray.length <= 0) {
            return -1;
        }
        return sendPacket(creatMsg(new HiberBean(1, pkgName, pidArray, frzState)));
    }

    /* access modifiers changed from: protected */
    public int[] doHiberReclaimApi() {
        if (startHiberFail()) {
            AwareLog.w(TAG, "HiberManagerService is not Started!");
            return AppHibernateCst.EMPTY_INT_ARRAY;
        }
        int ret = sendPacket(creatMsg(new HiberBean(2, null, null, 1024)));
        int[] retData = AppHibernateCst.EMPTY_INT_ARRAY;
        if (ret == 0) {
            return recvPacket(256);
        }
        return retData;
    }

    /* access modifiers changed from: protected */
    public int doHiberDumpApi(int dumpId) {
        if (dumpId != 1 && dumpId != 2) {
            return -1;
        }
        if (startHiberFail()) {
            AwareLog.w(TAG, "HiberManagerService is not Started!");
            return -1;
        }
        int ret = sendPacket(creatMsg(new HiberBean(4, null, null, dumpId)));
        if (ret == 0) {
            recvPacket(10240);
        }
        return ret;
    }

    /* access modifiers changed from: protected */
    public int notifyHiberStop() {
        int ret = doHiberSwitch(0);
        destroySocket();
        return ret;
    }

    /* access modifiers changed from: protected */
    public int notifyHiberStart() {
        return doHiberSwitch(1);
    }

    private boolean startHiberFail() {
        if (doHiberSwitch(1) == -1) {
            return true;
        }
        return false;
    }

    private int doHiberSwitch(int switchId) {
        if (switchId != 0 && switchId != 1) {
            return -1;
        }
        int tempSwitchId = switchId;
        if (switchId == 1) {
            if (MemoryConstant.getConfigReclaimFileCache() && MemoryConstant.isKernCompressEnable()) {
                tempSwitchId |= 2;
            } else if (!MemoryConstant.getConfigReclaimFileCache() || MemoryConstant.isKernCompressEnable()) {
                tempSwitchId |= 4;
            } else {
                tempSwitchId |= 8;
            }
        }
        return sendPacket(creatMsg(new HiberBean(3, null, null, tempSwitchId)));
    }

    private ByteBuffer creatMsg(HiberBean hiberBean) {
        Parcel data = Parcel.obtain();
        hiberBean.writeToParcel(data, hiberBean.funcId);
        byte[] msgBody = data.marshall();
        data.recycle();
        if (msgBody == null) {
            AwareLog.w(TAG, "msgBody == null");
            return null;
        }
        int msgBodyLen = msgBody.length;
        if (msgBodyLen <= 0) {
            AwareLog.w(TAG, "msgBodyLen <= 0");
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate(msgBodyLen + 8);
        try {
            buffer.putInt(AppHibernateCst.MSB_BASE_VALUE_APPHIBER);
            buffer.putInt(msgBodyLen);
            buffer.put(msgBody);
            return buffer;
        } catch (BufferOverflowException e) {
            AwareLog.e(TAG, "creatMsg happened BufferOverflowException ");
            return null;
        } catch (ReadOnlyBufferException e2) {
            AwareLog.e(TAG, "creatMsg happened ReadOnlyBufferException ");
            return null;
        }
    }

    private void createSocket() {
        this.mSocketLock.writeLock().lock();
        if (this.mAppHiberSocket != null) {
            this.mSocketLock.writeLock().unlock();
            return;
        }
        try {
            this.mAppHiberSocket = new LocalSocket(3);
            this.mAppHiberSocket.connect(new LocalSocketAddress(AppHibernateCst.HIBER_EVENT_SOCKET, LocalSocketAddress.Namespace.RESERVED));
            this.mAppHiberSocket.setSoTimeout(RECEIVE_TIMEOUT);
            this.mOutputStream = this.mAppHiberSocket.getOutputStream();
            this.mInputStream = this.mAppHiberSocket.getInputStream();
            this.mAppHiberSocket.setReceiveBufferSize(10240);
            AwareLog.d(TAG, "createSocket Success!");
        } catch (IOException e) {
            AwareLog.e(TAG, "createSocket happened IOException");
            this.mSocketLock.writeLock().unlock();
            destroySocket();
            this.mSocketLock.writeLock().lock();
        } catch (Throwable th) {
            this.mSocketLock.writeLock().unlock();
            throw th;
        }
        this.mSocketLock.writeLock().unlock();
    }

    private void destroySocket() {
        this.mSocketLock.writeLock().lock();
        try {
            closeStream();
            closeSocket();
        } finally {
            this.mSocketLock.writeLock().unlock();
        }
    }

    private void closeSocket() {
        LocalSocket localSocket = this.mAppHiberSocket;
        if (localSocket != null) {
            try {
                localSocket.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeSocket failed! happened IOException");
            } catch (Throwable th) {
                this.mAppHiberSocket = null;
                throw th;
            }
            this.mAppHiberSocket = null;
        }
    }

    private void closeStream() {
        OutputStream outputStream = this.mOutputStream;
        if (outputStream != null) {
            CommonUtils.closeStream(outputStream, TAG, "mOutputStream close failed! happend IOException");
            this.mOutputStream = null;
        }
        InputStream inputStream = this.mInputStream;
        if (inputStream != null) {
            CommonUtils.closeStream(inputStream, TAG, "mInputStream close failed! happend IOException");
            this.mInputStream = null;
        }
    }

    /* JADX INFO: finally extract failed */
    private int sendPacket(ByteBuffer buffer) {
        if (buffer == null) {
            AwareLog.w(TAG, "sendPacket ByteBuffer is null!");
            return -1;
        }
        this.mSocketLock.readLock().lock();
        if (this.mAppHiberSocket == null) {
            this.mSocketLock.readLock().unlock();
            createSocket();
            this.mSocketLock.readLock().lock();
        }
        OutputStream outputStream = this.mOutputStream;
        if (outputStream != null) {
            try {
                outputStream.write(buffer.array(), 0, buffer.position());
                flush(FLUSH_TIMEOUT);
                this.mSocketLock.readLock().unlock();
                return 0;
            } catch (IOException e) {
                AwareLog.e(TAG, "mOutputStream write failed! happened IOException");
                this.mSocketLock.readLock().unlock();
                destroySocket();
                this.mSocketLock.readLock().lock();
                this.mSocketLock.readLock().unlock();
                return -1;
            } catch (Throwable th) {
                this.mSocketLock.readLock().unlock();
                throw th;
            }
        } else {
            this.mSocketLock.readLock().unlock();
            return -1;
        }
    }

    private void flush(long millis) throws IOException {
        FileDescriptor myFd = this.mAppHiberSocket.getFileDescriptor();
        if (myFd != null) {
            long start = SystemClock.uptimeMillis();
            Int32RefEx pending = new Int32RefEx(0);
            while (true) {
                try {
                    OsEx.ioctlInt(myFd, OsConstantsEx.TIOCOUTQ, pending);
                    if (pending.getValue() > 0) {
                        if (SystemClock.uptimeMillis() - start < millis) {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                return;
                            }
                        } else {
                            AwareLog.e(TAG, "Socket flush timed out !!!");
                            throw new IOException("flush timeout");
                        }
                    } else {
                        return;
                    }
                } catch (ErrnoException e2) {
                    throw ErrnoExceptionEx.rethrowAsIOException(e2);
                }
            }
        } else {
            throw new IOException("socket closed");
        }
    }

    private int[] recvPacket(int byteSize) {
        this.mSocketLock.readLock().lock();
        if (this.mInputStream == null) {
            this.mSocketLock.readLock().unlock();
            return AppHibernateCst.EMPTY_INT_ARRAY;
        }
        byte[] recvByte = new byte[byteSize];
        int readlen = -1;
        try {
            Arrays.fill(recvByte, (byte) 0);
            readlen = this.mInputStream.read(recvByte);
        } catch (IOException e) {
            AwareLog.e(TAG, "mInputStream write failed happened IOException!");
            this.mSocketLock.readLock().unlock();
            destroySocket();
            this.mSocketLock.readLock().lock();
        } catch (Throwable th) {
            this.mSocketLock.readLock().unlock();
            throw th;
        }
        this.mSocketLock.readLock().unlock();
        int[] ret = AppHibernateCst.EMPTY_INT_ARRAY;
        if (readlen > 0) {
            return parseData(recvByte);
        }
        return ret;
    }

    private int[] parseData(byte[] buf) {
        int[] retArray = AppHibernateCst.EMPTY_INT_ARRAY;
        if (buf == null || buf.length == 0) {
            AwareLog.w(TAG, "buf == null");
            return retArray;
        }
        Parcel recvMsgParcel = Parcel.obtain();
        recvMsgParcel.unmarshall(buf, 0, buf.length);
        recvMsgParcel.setDataPosition(0);
        int funcId = recvMsgParcel.readInt();
        if (funcId == 4) {
            AppHiberRadar.getInstance().parseData(recvMsgParcel, recvMsgParcel.readInt());
        } else if (funcId == 2) {
            retArray = parseRecliamRes(recvMsgParcel);
        } else {
            AwareLog.i(TAG, funcId + " is not HIBER_MANAGER_CMD_DUMP/_RECLAIM, Neglect!");
        }
        recvMsgParcel.recycle();
        return retArray;
    }

    private int[] parseRecliamRes(Parcel recvMsgParcel) {
        int len = recvMsgParcel.readInt();
        if (len == 0) {
            return AppHibernateCst.EMPTY_INT_ARRAY;
        }
        int effectNum = (recvMsgParcel.dataSize() - recvMsgParcel.dataPosition()) / 4;
        AwareLog.i(TAG, "reclaim part success, be interrupted process number = " + len + " (, max effectNum = " + effectNum + ")");
        if (len > effectNum) {
            len = effectNum;
        }
        int[] retArray = new int[len];
        for (int i = 0; i < len; i++) {
            retArray[i] = recvMsgParcel.readInt();
        }
        return retArray;
    }
}
