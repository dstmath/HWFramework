package com.huawei.servicehost;

import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class BufferShareManager implements DeathRecipient {
    private static final boolean DEBUG = true;
    private static final String TAG = "BufferShareManager";
    private static BufferShareManager gBufferShareManager;
    private static final Object gLock = new Object();
    private IBufferShareService mBufferShareService;
    private final Object mLock = new Object();

    private BufferShareManager() {
        getBufferShareService();
    }

    public static BufferShareManager get() {
        BufferShareManager bufferShareManager;
        synchronized (gLock) {
            if (gBufferShareManager == null) {
                gBufferShareManager = new BufferShareManager();
            }
            bufferShareManager = gBufferShareManager;
        }
        return bufferShareManager;
    }

    public IImageConsumer createImageConsumer(int type) {
        synchronized (this.mLock) {
            try {
                if (this.mBufferShareService != null) {
                    IImageConsumer createImageConsumer = this.mBufferShareService.createImageConsumer(type);
                    return createImageConsumer;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "failed.");
            }
        }
        return null;
    }

    public IImageProducer createImageProducer(Surface val) {
        synchronized (this.mLock) {
            try {
                if (this.mBufferShareService != null) {
                    IImageProducer createImageProducer = this.mBufferShareService.createImageProducer(val);
                    return createImageProducer;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "failed.");
            }
        }
        return null;
    }

    public ImageWrap createImageWrap(ImageDescriptor val) {
        synchronized (this.mLock) {
            try {
                if (this.mBufferShareService != null) {
                    ImageWrap createImageWrap = this.mBufferShareService.getImageAllocator().createImageWrap(val);
                    return createImageWrap;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "failed.");
            }
        }
        return null;
    }

    public int getDefaultUsage() {
        int ret = 0;
        synchronized (this.mLock) {
            try {
                if (this.mBufferShareService != null) {
                    Log.v(TAG, "enter.");
                    ret = this.mBufferShareService.getDefaultUsage();
                    Log.v(TAG, "exit.");
                } else {
                    Log.e(TAG, "null pointer, failed.");
                }
            } catch (RemoteException e) {
            }
        }
        return ret;
    }

    public IBufferShareService getBufferShareService() {
        synchronized (this.mLock) {
            IBufferShareService iBufferShareService;
            if (this.mBufferShareService != null) {
                iBufferShareService = this.mBufferShareService;
                return iBufferShareService;
            }
            this.mBufferShareService = ServiceFetcher.get().getBufferShareService();
            if (this.mBufferShareService == null) {
                Log.e(TAG, "fail to getBufferShareService.");
                return null;
            }
            try {
                Log.v(TAG, "link servicehost ok.");
                this.mBufferShareService.asBinder().linkToDeath(this, 0);
                iBufferShareService = this.mBufferShareService;
                return iBufferShareService;
            } catch (RemoteException e) {
                Log.e(TAG, "linkToDeath to camera servicehost failed.");
                return null;
            }
        }
    }

    @SuppressWarnings({"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
    public void binderDied() {
        synchronized (gLock) {
            gBufferShareManager = null;
        }
        synchronized (this.mLock) {
            Log.w(TAG, "camera servicehost died.");
            this.mBufferShareService = null;
        }
    }
}
