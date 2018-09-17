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
    private final String TAG = "DelayedDiskWrite";
    private Handler mDiskWriteHandler;
    private HandlerThread mDiskWriteHandlerThread;
    private volatile int mWriteSequence = 0;

    public interface Writer {
        void onWriteCalled(DataOutputStream dataOutputStream) throws IOException;
    }

    public void write(String filePath, Writer w) {
        write(filePath, w, true);
    }

    public void write(final String filePath, final Writer w, final boolean open) {
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
        try {
            this.mDiskWriteHandler.post(new Runnable() {
                public void run() {
                    DelayedDiskWrite.this.doWrite(filePath, w, open);
                }
            });
        } catch (NullPointerException e) {
            loge("mDiskWriteHandler NullPointerException!");
        }
    }

    private void doWrite(String filePath, Writer w, boolean open) {
        int i;
        DataOutputStream out = null;
        if (open) {
            try {
                out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)));
            } catch (IOException e) {
                loge("Error writing data file " + filePath);
                if (out != null) {
                    try {
                        out.close();
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
                if (out != null) {
                    try {
                        out.close();
                    } catch (Exception e3) {
                    }
                }
                synchronized (this) {
                    int i2 = this.mWriteSequence - 1;
                    this.mWriteSequence = i2;
                    if (i2 == 0) {
                        this.mDiskWriteHandler.getLooper().quit();
                        this.mDiskWriteHandler = null;
                        this.mDiskWriteHandlerThread = null;
                    }
                }
            }
        }
        w.onWriteCalled(out);
        if (out != null) {
            try {
                out.close();
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
