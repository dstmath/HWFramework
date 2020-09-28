package com.huawei.android.hardware.qcomfmradio;

import android.content.Context;
import android.media.BuildConfig;
import android.mtp.HwMtpConstants;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.hardware.fmradio.FmReceiver;
import com.huawei.android.hardware.fmradio.IFmEventCallback;
import com.huawei.android.hardware.fmradio.common.BaseHwFmService;
import com.huawei.android.media.AudioSystemEx;
import java.util.Arrays;

public class QcomFmService extends BaseHwFmService {
    private static final int EVENT_LISTEN = 1;
    private static final int STD_BUF_SIZE = 128;
    private static final String TAG = "QcomFmService";
    private IFmEventCallback mCallback = null;
    private boolean mIsFmConnected = false;
    private Thread mThread = null;
    private int mUid = -1;

    public QcomFmService(Context context) {
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int acquireFd(String path) {
        int uid = Binder.getCallingUid();
        Log.d(TAG, "acquireFd uid = " + uid);
        int i = this.mUid;
        if (uid == i || i == -1) {
            int fd = FmReceiverJNI.acquireFdNative(path);
            if (fd != -1) {
                this.mUid = uid;
            }
            return fd;
        }
        Log.d(TAG, "support only one client now");
        return -1;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int audioControl(int fd, int control, int field) {
        return FmReceiverJNI.audioControlNative(fd, control, field);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int cancelSearch(int fd) {
        return FmReceiverJNI.cancelSearchNative(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int closeFd(int fd) {
        if (Binder.getCallingUid() != this.mUid) {
            Log.d(TAG, "can not close fd");
            return -1;
        }
        this.mUid = -1;
        return FmReceiverJNI.closeFdNative(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getFreq(int fd) {
        return FmReceiverJNI.getFreqNative(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setFreq(int fd, int freq) {
        return FmReceiverJNI.setFreqNative(fd, freq);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getControl(int fd, int id) {
        return FmReceiverJNI.getControlNative(fd, id);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setControl(int fd, int id, int value) {
        if (Binder.getCallingUid() != this.mUid) {
            return -1;
        }
        return FmReceiverJNI.setControlNative(fd, id, value);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int startSearch(int fd, int dir) {
        return FmReceiverJNI.startSearchNative(fd, dir);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getBuffer(int fd, byte[] buff, int index) {
        return FmReceiverJNI.getBufferNative(fd, buff, index);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getRSSI(int fd) {
        return FmReceiverJNI.getRSSINative(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setBand(int fd, int low, int high) {
        return FmReceiverJNI.setBandNative(fd, low, high);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getLowerBand(int fd) {
        return FmReceiverJNI.getLowerBandNative(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getUpperBand(int fd) {
        return FmReceiverJNI.getUpperBandNative(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setMonoStereo(int fd, int val) {
        return FmReceiverJNI.setMonoStereoNative(fd, val);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getRawRds(int fd, byte[] buff, int count) {
        return FmReceiverJNI.getRawRdsNative(fd, buff, count);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public void setNotchFilter(boolean value) {
        FmReceiverJNI.setNotchFilterNative(value);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getAudioQuilty(int fd, int value) {
        return FmReceiverJNI.getAudioQuiltyNative(fd, value);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setFmSnrThresh(int fd, int value) {
        return FmReceiverJNI.setFmSnrThreshNative(fd, value);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setFmRssiThresh(int fd, int value) {
        return FmReceiverJNI.setFmRssiThreshNative(fd, value);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public void setFmDeviceConnectionState(int state) {
        if (state == 0 && this.mIsFmConnected) {
            AudioSystemEx.setDeviceConnectionState(1048576, 0, BuildConfig.FLAVOR, BuildConfig.FLAVOR, 0);
            this.mIsFmConnected = false;
        } else if (state == 1 && !this.mIsFmConnected) {
            AudioSystemEx.setDeviceConnectionState(1048576, 1, BuildConfig.FLAVOR, BuildConfig.FLAVOR, 0);
            this.mIsFmConnected = true;
        }
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public void startListner(final int fd, IFmEventCallback cb) {
        Thread thread = this.mThread;
        if (thread != null && !thread.isInterrupted()) {
            this.mThread.interrupt();
        }
        this.mCallback = cb;
        this.mThread = new Thread() {
            /* class com.huawei.android.hardware.qcomfmradio.QcomFmService.AnonymousClass1 */

            public void run() {
                byte[] buff = new byte[128];
                Log.d(QcomFmService.TAG, "Starting listener " + fd);
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Arrays.fill(buff, (byte) 0);
                        int eventCount = QcomFmService.this.getBuffer(fd, buff, 1);
                        Log.d(QcomFmService.TAG, "Received event. Count: " + eventCount);
                        if (QcomFmService.this.mCallback != null) {
                            for (int index = 0; index < eventCount; index++) {
                                Log.d(QcomFmService.TAG, "Received <" + ((int) buff[index]) + ">");
                                byte b = buff[index];
                                if (b != 1 && b != 2) {
                                    switch (b) {
                                        case 8:
                                        case 11:
                                        case 13:
                                            QcomFmService.this.mCallback.onEventCallback(buff[index], 0, -1);
                                            break;
                                        case 9:
                                        case HwMtpConstants.TYPE_UINT128:
                                        case FmReceiver.FM_RX_SRCHLIST_MAX_STATIONS:
                                            QcomFmService.this.mCallback.onEventCallback(buff[index], 1, -1);
                                            break;
                                        default:
                                            QcomFmService.this.mCallback.onEventCallback(buff[index], -1, -1);
                                            break;
                                    }
                                } else {
                                    QcomFmService.this.mCallback.onEventCallback(buff[index], QcomFmService.this.getFreq(fd), -1);
                                }
                            }
                        }
                    } catch (RuntimeException ex) {
                        Log.d(QcomFmService.TAG, ex.toString());
                        Thread.currentThread().interrupt();
                    } catch (RemoteException e) {
                        Log.d(QcomFmService.TAG, "RunningThread InterruptedException RemoteException");
                        Thread.currentThread().interrupt();
                        try {
                            if (QcomFmService.this.mCallback != null) {
                                Log.d(QcomFmService.TAG, "mCallback is not null");
                                QcomFmService.this.mCallback.onEventCallback(2, -1, -1);
                            } else {
                                Log.d(QcomFmService.TAG, "mCallback == null");
                            }
                        } catch (RemoteException e2) {
                            Log.d(QcomFmService.TAG, "RunningThread InterruptedException RemoteException again");
                        }
                    }
                }
            }
        };
        this.mThread.start();
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public void stopListner() {
        if (Binder.getCallingUid() == this.mUid) {
            this.mCallback = null;
            Log.d(TAG, "stopping the Listener\n");
            Thread thread = this.mThread;
            if (thread != null && !thread.isInterrupted()) {
                this.mThread.interrupt();
            }
        }
    }
}
