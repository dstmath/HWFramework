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
    private int mWriteSequence = 0;

    public interface Writer {
        void onWriteCalled(DataOutputStream dataOutputStream) throws IOException;
    }

    public void write(String filePath, Writer w) {
        write(filePath, w, true);
    }

    public void write(final String filePath, final Writer w, final boolean open) {
        if (!TextUtils.isEmpty(filePath)) {
            synchronized (this) {
                int i = this.mWriteSequence + 1;
                this.mWriteSequence = i;
                if (i == 1) {
                    this.mDiskWriteHandlerThread = new HandlerThread("DelayedDiskWriteThread");
                    this.mDiskWriteHandlerThread.start();
                    this.mDiskWriteHandler = new Handler(this.mDiskWriteHandlerThread.getLooper());
                }
            }
            this.mDiskWriteHandler.post(new Runnable() {
                /* class com.android.server.net.DelayedDiskWrite.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    DelayedDiskWrite.this.doWrite(filePath, w, open);
                }
            });
            return;
        }
        throw new IllegalArgumentException("empty file path");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doWrite(String filePath, Writer w, boolean open) {
        DataOutputStream out = null;
        if (open) {
            try {
                out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)));
            } catch (IOException e) {
                try {
                    loge("Error writing data file " + filePath);
                    if (0 != 0) {
                        try {
                            out.close();
                        } catch (Exception e2) {
                        }
                    }
                    synchronized (this) {
                        int i = this.mWriteSequence - 1;
                        this.mWriteSequence = i;
                        if (i == 0) {
                            this.mDiskWriteHandler.getLooper().quit();
                            this.mDiskWriteHandler = null;
                            this.mDiskWriteHandlerThread = null;
                        }
                    }
                } catch (Throwable th) {
                    if (0 != 0) {
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
                        throw th;
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
            int i3 = this.mWriteSequence - 1;
            this.mWriteSequence = i3;
            if (i3 == 0) {
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
