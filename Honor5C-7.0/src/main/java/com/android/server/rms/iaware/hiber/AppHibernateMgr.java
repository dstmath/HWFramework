package com.android.server.rms.iaware.hiber;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Parcel;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.hiber.bean.HiberBean;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

class AppHibernateMgr {
    private static final int CMD_HEAD_LEN = 8;
    private static final int RECV_BYTE_BUFFER_LENTH = 10240;
    private static final int RECV_BYTE_DUMP_BYTE_LENTH = 10240;
    private static final int RECV_BYTE_RECLIAM_BYTE_LENTH = 256;
    private static final String TAG = "AppHiber_Mgr";
    private static final int VAL_PARCELABLE_INT = 4;
    private static AppHibernateMgr mInstance;
    private LocalSocket mAppHiberSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private final Semaphore sendSemaphore;

    AppHibernateMgr() {
        this.sendSemaphore = new Semaphore(1);
    }

    protected static synchronized AppHibernateMgr getInstance() {
        AppHibernateMgr appHibernateMgr;
        synchronized (AppHibernateMgr.class) {
            if (mInstance == null) {
                mInstance = new AppHibernateMgr();
            }
            appHibernateMgr = mInstance;
        }
        return appHibernateMgr;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected int doHiberFrzApi(String pkgName, int[] pidArray, int frz_state) {
        if (pkgName == null || pkgName.trim().isEmpty() || pidArray == null || pidArray.length <= 0) {
            return -1;
        }
        return sendPacket(creatMsg(new HiberBean(1, pkgName, pidArray, frz_state)));
    }

    protected int[] doHiberReclaimApi() {
        if (startHiberFail()) {
            AwareLog.w(TAG, "HiberManagerService  is not Started!");
            return AppHibernateCst.EMPTY_INT_ARRAY;
        }
        int ret = sendPacket(creatMsg(new HiberBean(2, null, null, HwGlobalActionsData.FLAG_SILENTMODE_NORMAL)));
        int[] retData = AppHibernateCst.EMPTY_INT_ARRAY;
        if (ret == 0) {
            retData = recvPacket(RECV_BYTE_RECLIAM_BYTE_LENTH);
        }
        return retData;
    }

    protected int doHiberDumpApi(int dumpId) {
        if (dumpId != 1 && dumpId != 2) {
            return -1;
        }
        if (startHiberFail()) {
            AwareLog.w(TAG, "HiberManagerService  is not Started!");
            return -1;
        }
        int ret = sendPacket(creatMsg(new HiberBean(VAL_PARCELABLE_INT, null, null, dumpId)));
        if (ret == 0) {
            recvPacket(RECV_BYTE_DUMP_BYTE_LENTH);
        }
        return ret;
    }

    protected int notifyHiberStop() {
        int ret = doHiberSwitch(0);
        destroySocket();
        return ret;
    }

    protected int notifyHiberStart() {
        return doHiberSwitch(1);
    }

    private boolean startHiberFail() {
        if (-1 == doHiberSwitch(1)) {
            return true;
        }
        return false;
    }

    private int doHiberSwitch(int switchId) {
        if (switchId == 0 || switchId == 1) {
            return sendPacket(creatMsg(new HiberBean(3, null, null, switchId)));
        }
        return -1;
    }

    private ByteBuffer creatMsg(HiberBean hiberBean) {
        Parcel _data = Parcel.obtain();
        hiberBean.writeToParcel(_data, hiberBean.func_Id);
        byte[] msgBody = _data.marshall();
        _data.recycle();
        if (msgBody == null) {
            AwareLog.w(TAG, "null == msgBody");
            return null;
        }
        int msgBodyLen = msgBody.length;
        if (msgBodyLen <= 0) {
            AwareLog.w(TAG, "msgBodyLen <= 0");
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate(msgBodyLen + CMD_HEAD_LEN);
        try {
            buffer.putInt(WifiProCommonUtils.HISTORY_TYPE_EMPTY);
            buffer.putInt(msgBodyLen);
            buffer.put(msgBody);
        } catch (BufferOverflowException e) {
            AwareLog.e(TAG, "creatMsg happened  BufferOverflowException ");
            buffer = null;
        } catch (ReadOnlyBufferException e2) {
            AwareLog.e(TAG, "creatMsg happened  ReadOnlyBufferException ");
            buffer = null;
        }
        return buffer;
    }

    private void createSocket() {
        if (this.mAppHiberSocket == null) {
            try {
                this.mAppHiberSocket = new LocalSocket(3);
                this.mAppHiberSocket.connect(new LocalSocketAddress(AppHibernateCst.HIBER_EVENT_SOCKET, Namespace.RESERVED));
                this.mOutputStream = this.mAppHiberSocket.getOutputStream();
                this.mInputStream = this.mAppHiberSocket.getInputStream();
                this.mAppHiberSocket.setReceiveBufferSize(RECV_BYTE_DUMP_BYTE_LENTH);
                AwareLog.d(TAG, "createSocket   Success!");
            } catch (IOException e) {
                AwareLog.e(TAG, "createSocket happend IOException");
                destroySocket();
            }
        }
    }

    private void destroySocket() {
        closeStream();
        closeSocket();
    }

    private void closeSocket() {
        if (this.mAppHiberSocket != null) {
            try {
                this.mAppHiberSocket.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeSocket   failed! happend IOException");
            } catch (Throwable th) {
                this.mAppHiberSocket = null;
            }
            this.mAppHiberSocket = null;
        }
    }

    private void closeStream() {
        if (this.mOutputStream != null) {
            try {
                this.mOutputStream.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "mOutputStream close failed! happend IOException");
            } catch (Throwable th) {
                this.mOutputStream = null;
            }
            this.mOutputStream = null;
        }
        if (this.mInputStream != null) {
            try {
                this.mInputStream.close();
            } catch (IOException e2) {
                AwareLog.e(TAG, "mInputStream close failed! happend IOException");
            } catch (Throwable th2) {
                this.mInputStream = null;
            }
            this.mInputStream = null;
        }
    }

    private int sendPacket(ByteBuffer buffer) {
        if (buffer == null) {
            AwareLog.w(TAG, "sendPacket ByteBuffer is null!");
            return -1;
        }
        int retValue = -1;
        try {
            this.sendSemaphore.acquire();
            AwareLog.d(TAG, "sendPacket sendSemaphore : acquire");
            if (this.mAppHiberSocket == null) {
                createSocket();
            }
            if (this.mOutputStream != null) {
                try {
                    this.mOutputStream.write(buffer.array(), 0, buffer.position());
                    this.mOutputStream.flush();
                    retValue = 0;
                } catch (IOException e) {
                    AwareLog.e(TAG, "mOutputStream write failed! happend IOException");
                    destroySocket();
                }
            }
            this.sendSemaphore.release();
            AwareLog.d(TAG, "sendPacket sendSemaphore : release");
            return retValue;
        } catch (InterruptedException e2) {
            AwareLog.e(TAG, " sendPacket  happend InterruptedException");
            return -1;
        }
    }

    private int[] recvPacket(int byteSize) {
        if (this.mInputStream == null) {
            return AppHibernateCst.EMPTY_INT_ARRAY;
        }
        byte[] recvByte = new byte[byteSize];
        int readlen = -1;
        try {
            Arrays.fill(recvByte, (byte) 0);
            readlen = this.mInputStream.read(recvByte);
        } catch (IOException e) {
            AwareLog.e(TAG, " mInputStream write failed happend IOException!");
            destroySocket();
        }
        int[] ret = AppHibernateCst.EMPTY_INT_ARRAY;
        if (readlen > 0) {
            ret = parseData(recvByte);
        }
        return ret;
    }

    private int[] parseData(byte[] buf) {
        int[] retArray = AppHibernateCst.EMPTY_INT_ARRAY;
        if (buf == null || buf.length == 0) {
            AwareLog.w(TAG, "null == buf");
            return retArray;
        }
        Parcel recvMsgParcel = Parcel.obtain();
        recvMsgParcel.unmarshall(buf, 0, buf.length);
        recvMsgParcel.setDataPosition(0);
        int func_Id = recvMsgParcel.readInt();
        if (VAL_PARCELABLE_INT == func_Id) {
            AppHiberRadar.getInstance().parseData(recvMsgParcel, recvMsgParcel.readInt());
        } else if (2 == func_Id) {
            retArray = parseRecliamRes(recvMsgParcel);
        } else {
            AwareLog.i(TAG, func_Id + " is not HIBER_MANAGER_CMD_DUMP/_RECLAIM, Neglect!");
        }
        recvMsgParcel.recycle();
        return retArray;
    }

    private int[] parseRecliamRes(Parcel recvMsgParcel) {
        int len = recvMsgParcel.readInt();
        if (len == 0) {
            return AppHibernateCst.EMPTY_INT_ARRAY;
        }
        int effectNum = (recvMsgParcel.dataSize() - recvMsgParcel.dataPosition()) / VAL_PARCELABLE_INT;
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
