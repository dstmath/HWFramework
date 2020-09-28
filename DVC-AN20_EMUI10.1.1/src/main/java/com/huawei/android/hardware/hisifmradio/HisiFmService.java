package com.huawei.android.hardware.hisifmradio;

import android.content.Context;
import android.media.BuildConfig;
import android.mtp.HwMtpConstants;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.hardware.fmradio.FmReceiver;
import com.huawei.android.hardware.fmradio.IFmEventCallback;
import com.huawei.android.hardware.fmradio.common.BaseHwFmService;
import com.huawei.android.media.AudioSystemEx;
import java.util.Arrays;

public class HisiFmService extends BaseHwFmService {
    private static final int EVENT_LISTEN = 1;
    private static final int STD_BUF_SIZE = 128;
    private static final String TAG = "HisiFmService";
    private IFmEventCallback mCallback = null;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.huawei.android.hardware.hisifmradio.HisiFmService.AnonymousClass1 */

        public void binderDied() {
            if (HisiFmService.this.mCallback != null) {
                HisiFmService.this.mCallback.asBinder().unlinkToDeath(HisiFmService.this.mDeathRecipient, 0);
            }
            if (HisiFmService.this.mThread != null && !HisiFmService.this.mThread.isInterrupted()) {
                HisiFmService.this.mThread.interrupt();
            }
            FmReceiverJniAdapter.closeFdNativeAdapter(HisiFmService.this.mFd);
            HisiFmService.this.mUid = -1;
            HisiFmService.this.mFd = -1;
            HisiFmService.this.mCallback = null;
            Log.w(HisiFmService.TAG, "binderDied ,stop the Listener and closeFd");
        }
    };
    private int mFd = -1;
    private boolean mIsFmConnected = false;
    private Thread mThread = null;
    private int mUid = -1;

    public HisiFmService(Context context) {
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int acquireFd(String path) {
        int uid = Binder.getCallingUid();
        Log.d(TAG, "acquireFd uid = " + uid);
        int i = this.mUid;
        if (uid == i || i == -1) {
            int fd = FmReceiverJniAdapter.acquireFdNativeAdapter(path);
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
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int cancelSearch(int fd) {
        return FmReceiverJniAdapter.cancelSearchNativeAdapter(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int closeFd(int fd) {
        if (Binder.getCallingUid() != this.mUid) {
            Log.d(TAG, "can not close fd");
            return -1;
        }
        this.mUid = -1;
        Log.i(TAG, "closeFD and set mUid = -1.");
        return FmReceiverJniAdapter.closeFdNativeAdapter(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getFreq(int fd) {
        return FmReceiverJniAdapter.getFreqNativeAdapter(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setFreq(int fd, int freq) {
        return FmReceiverJniAdapter.setFreqNativeAdapter(fd, freq);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getControl(int fd, int id) {
        return FmReceiverJniAdapter.getControlNativeAdapter(fd, id);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setControl(int fd, int id, int value) {
        if (Binder.getCallingUid() != this.mUid) {
            return -1;
        }
        return FmReceiverJniAdapter.setControlNativeAdapter(fd, id, value);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int startSearch(int fd, int dir) {
        return FmReceiverJniAdapter.startSearchNativeAdapter(fd, dir);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getBuffer(int fd, byte[] buff, int index) {
        return FmReceiverJniAdapter.getBufferNativeAdapter(fd, buff, index);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getRSSI(int fd) {
        return FmReceiverJniAdapter.getRSSINativeAdapter(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setBand(int fd, int low, int high) {
        return FmReceiverJniAdapter.setBandNativeAdapter(fd, low, high);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getLowerBand(int fd) {
        return FmReceiverJniAdapter.getLowerBandNativeAdapter(fd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getUpperBand(int fd) {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setMonoStereo(int fd, int val) {
        return FmReceiverJniAdapter.setMonoStereoNativeAdapter(fd, val);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getRawRds(int fd, byte[] buff, int count) {
        return FmReceiverJniAdapter.getRawRdsNativeAdapter(fd, buff, count);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public void setNotchFilter(boolean value) {
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getAudioQuilty(int fd, int value) {
        return FmReceiverJniAdapter.getAudioQuiltyNativeAdapter(fd, value);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setFmSnrThresh(int fd, int value) {
        return FmReceiverJniAdapter.setFmSnrThreshNativeAdapter(fd, value);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setFmRssiThresh(int fd, int value) {
        return 0;
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
        if (cb == null) {
            Log.d(TAG, "Starting listener cb is null");
            return;
        }
        Thread thread = this.mThread;
        if (thread != null && !thread.isInterrupted()) {
            this.mThread.interrupt();
        }
        this.mCallback = cb;
        this.mFd = fd;
        try {
            cb.asBinder().linkToDeath(this.mDeathRecipient, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "can not link to death");
        }
        this.mThread = new Thread() {
            /* class com.huawei.android.hardware.hisifmradio.HisiFmService.AnonymousClass2 */

            public void run() {
                byte[] buff = new byte[128];
                Log.d(HisiFmService.TAG, "Starting listener " + fd);
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Arrays.fill(buff, (byte) 0);
                        int eventCount = HisiFmService.this.getBuffer(fd, buff, 1);
                        Log.d(HisiFmService.TAG, "Received event. Count: " + eventCount);
                        if (HisiFmService.this.mCallback != null) {
                            for (int index = 0; index < eventCount; index++) {
                                Log.d(HisiFmService.TAG, "Received <" + ((int) buff[index]) + ">");
                                byte b = buff[index];
                                if (b != 1 && b != 2) {
                                    switch (b) {
                                        case 8:
                                        case 11:
                                        case 13:
                                            HisiFmService.this.mCallback.onEventCallback(buff[index], 0, -1);
                                            break;
                                        case 9:
                                        case HwMtpConstants.TYPE_UINT128:
                                        case FmReceiver.FM_RX_SRCHLIST_MAX_STATIONS:
                                            HisiFmService.this.mCallback.onEventCallback(buff[index], 1, -1);
                                            break;
                                        default:
                                            HisiFmService.this.mCallback.onEventCallback(buff[index], -1, -1);
                                            break;
                                    }
                                } else {
                                    HisiFmService.this.mCallback.onEventCallback(buff[index], HisiFmService.this.getFreq(fd), -1);
                                }
                            }
                        }
                    } catch (RuntimeException e) {
                        Log.d(HisiFmService.TAG, "RunningThread RuntimeException");
                        Thread.currentThread().interrupt();
                    } catch (RemoteException e2) {
                        Log.d(HisiFmService.TAG, "RunningThread RemoteException");
                        Thread.currentThread().interrupt();
                        try {
                            if (HisiFmService.this.mCallback != null) {
                                Log.d(HisiFmService.TAG, "mCallback is not null");
                                HisiFmService.this.mCallback.onEventCallback(2, -1, -1);
                            } else {
                                Log.d(HisiFmService.TAG, "mCallback == null");
                            }
                        } catch (RemoteException e3) {
                            Log.d(HisiFmService.TAG, "RunningThread InterruptedException for RemoteException");
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
            IFmEventCallback iFmEventCallback = this.mCallback;
            if (iFmEventCallback != null) {
                iFmEventCallback.asBinder().unlinkToDeath(this.mDeathRecipient, 0);
            }
            this.mCallback = null;
            Log.d(TAG, "stopping the Listener.");
            Thread thread = this.mThread;
            if (thread != null && !thread.isInterrupted()) {
                this.mThread.interrupt();
            }
        }
    }
}
