package com.huawei.servicehost;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;

public class BufferShareManager implements IBinder.DeathRecipient {
    private static final Object LOCK = new Object();
    private static final String TAG = "BufferShareManager";
    private static BufferShareManager bufferShareManager;
    private IBufferShareService bufferShareService;
    private final Object lock = new Object();

    private BufferShareManager() {
        getBufferShareService();
    }

    public static BufferShareManager get() {
        BufferShareManager bufferShareManager2;
        synchronized (LOCK) {
            if (bufferShareManager == null) {
                bufferShareManager = new BufferShareManager();
            }
            bufferShareManager2 = bufferShareManager;
        }
        return bufferShareManager2;
    }

    public IImageConsumer createImageConsumer(int type) {
        synchronized (this.lock) {
            try {
                if (this.bufferShareService == null) {
                    return null;
                }
                return this.bufferShareService.createImageConsumer(type);
            } catch (RemoteException e) {
                Log.e(TAG, "createImageConsumer failed.");
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public IImageProducer createImageProducer(Surface surface) {
        synchronized (this.lock) {
            try {
                if (this.bufferShareService == null) {
                    return null;
                }
                return this.bufferShareService.createImageProducer(surface);
            } catch (RemoteException e) {
                Log.e(TAG, "createImageProducer failed.");
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public ImageWrap createImageWrap(ImageDescriptor descriptor) {
        synchronized (this.lock) {
            try {
                if (this.bufferShareService != null) {
                    IImageAllocator allocator = this.bufferShareService.getImageAllocator();
                    if (allocator == null) {
                        return null;
                    }
                    return allocator.createImageWrap(descriptor);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "createImageWrap failed.");
            }
            return null;
        }
    }

    public int getDefaultUsage() {
        int ret = 0;
        synchronized (this.lock) {
            try {
                if (this.bufferShareService != null) {
                    Log.v(TAG, "enter.");
                    ret = this.bufferShareService.getDefaultUsage();
                    Log.v(TAG, "exit.");
                } else {
                    Log.e(TAG, "null pointer, failed.");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "getDefaultUsage failed.");
            }
        }
        return ret;
    }

    private IBufferShareService getBufferShareService() {
        synchronized (this.lock) {
            if (this.bufferShareService != null) {
                return this.bufferShareService;
            }
            this.bufferShareService = ServiceFetcher.get().getBufferShareService();
            if (this.bufferShareService == null) {
                Log.e(TAG, "fail to getBufferShareService.");
                return null;
            }
            try {
                Log.v(TAG, "link servicehost ok.");
                this.bufferShareService.asBinder().linkToDeath(this, 0);
                return this.bufferShareService;
            } catch (RemoteException e) {
                Log.e(TAG, "linkToDeath to camera servicehost failed.");
                return null;
            }
        }
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        synchronized (LOCK) {
            bufferShareManager = null;
        }
        synchronized (this.lock) {
            Log.w(TAG, "camera servicehost died.");
            this.bufferShareService = null;
        }
    }
}
