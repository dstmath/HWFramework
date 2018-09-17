package com.android.server.net;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DelayedDiskWrite {
    private final String TAG;
    private Handler mDiskWriteHandler;
    private HandlerThread mDiskWriteHandlerThread;
    private int mWriteSequence;

    /* renamed from: com.android.server.net.DelayedDiskWrite.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ String val$filePath;
        final /* synthetic */ boolean val$open;
        final /* synthetic */ Writer val$w;

        AnonymousClass1(String val$filePath, Writer val$w, boolean val$open) {
            this.val$filePath = val$filePath;
            this.val$w = val$w;
            this.val$open = val$open;
        }

        public void run() {
            DelayedDiskWrite.this.doWrite(this.val$filePath, this.val$w, this.val$open);
        }
    }

    public interface Writer {
        void onWriteCalled(DataOutputStream dataOutputStream) throws IOException;
    }

    public DelayedDiskWrite() {
        this.mWriteSequence = 0;
        this.TAG = "DelayedDiskWrite";
    }

    public void write(String filePath, Writer w) {
        write(filePath, w, true);
    }

    public void write(String filePath, Writer w, boolean open) {
        if (TextUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("empty file path");
        }
        synchronized (this) {
            int i = this.mWriteSequence + 1;
            this.mWriteSequence = i;
            if (i == 1) {
                this.mDiskWriteHandlerThread = new HandlerThread("DelayedDiskWriteThread");
                this.mDiskWriteHandlerThread.start();
                this.mDiskWriteHandler = new Handler(this.mDiskWriteHandlerThread.getLooper());
            }
        }
        this.mDiskWriteHandler.post(new AnonymousClass1(filePath, w, open));
    }

    private void doWrite(String filePath, Writer w, boolean open) {
        int i;
        DataOutputStream dataOutputStream = null;
        if (open) {
            try {
                dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)));
            } catch (IOException e) {
                loge("Error writing data file " + filePath);
                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (Exception e2) {
                    }
                }
                synchronized (this) {
                    i = this.mWriteSequence - 1;
                    this.mWriteSequence = i;
                    if (i == 0) {
                        this.mDiskWriteHandler.getLooper().quit();
                        this.mDiskWriteHandler = null;
                        this.mDiskWriteHandlerThread = null;
                    }
                }
            } catch (Throwable th) {
                int i2;
                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (Exception e3) {
                    }
                }
                synchronized (this) {
                }
                i2 = this.mWriteSequence - 1;
                this.mWriteSequence = i2;
                if (i2 == 0) {
                    this.mDiskWriteHandler.getLooper().quit();
                    this.mDiskWriteHandler = null;
                    this.mDiskWriteHandlerThread = null;
                }
            }
        }
        w.onWriteCalled(dataOutputStream);
        if (dataOutputStream != null) {
            try {
                dataOutputStream.close();
            } catch (Exception e4) {
            }
        }
        synchronized (this) {
            i = this.mWriteSequence - 1;
            this.mWriteSequence = i;
            if (i == 0) {
                this.mDiskWriteHandler.getLooper().quit();
                this.mDiskWriteHandler = null;
                this.mDiskWriteHandlerThread = null;
            }
        }
    }

    private void loge(String s) {
        Log.e("DelayedDiskWrite", s);
    }
}
