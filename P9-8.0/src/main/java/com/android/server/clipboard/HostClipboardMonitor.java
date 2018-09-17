package com.android.server.clipboard;

import android.util.Slog;
import java.io.IOException;
import java.io.RandomAccessFile;

/* compiled from: ClipboardService */
class HostClipboardMonitor implements Runnable {
    private static final String PIPE_DEVICE = "/dev/qemu_pipe";
    private static final String PIPE_NAME = "pipe:clipboard";
    private HostClipboardCallback mHostClipboardCallback;
    private RandomAccessFile mPipe = null;

    /* compiled from: ClipboardService */
    public interface HostClipboardCallback {
        void onHostClipboardUpdated(String str);
    }

    private void openPipe() {
        try {
            byte[] b = new byte[(PIPE_NAME.length() + 1)];
            b[PIPE_NAME.length()] = (byte) 0;
            System.arraycopy(PIPE_NAME.getBytes(), 0, b, 0, PIPE_NAME.length());
            this.mPipe = new RandomAccessFile(PIPE_DEVICE, "rw");
            this.mPipe.write(b);
        } catch (IOException e) {
            try {
                if (this.mPipe != null) {
                    this.mPipe.close();
                }
            } catch (IOException e2) {
            }
            this.mPipe = null;
        }
    }

    public HostClipboardMonitor(HostClipboardCallback cb) {
        this.mHostClipboardCallback = cb;
    }

    public void run() {
        while (!Thread.interrupted()) {
            while (this.mPipe == null) {
                try {
                    openPipe();
                    Thread.sleep(100);
                } catch (IOException e) {
                    try {
                        this.mPipe.close();
                    } catch (IOException e2) {
                    }
                    this.mPipe = null;
                } catch (InterruptedException e3) {
                }
            }
            byte[] receivedData = new byte[Integer.reverseBytes(this.mPipe.readInt())];
            this.mPipe.readFully(receivedData);
            this.mHostClipboardCallback.onHostClipboardUpdated(new String(receivedData));
        }
    }

    public void setHostClipboard(String content) {
        try {
            if (this.mPipe != null) {
                this.mPipe.writeInt(Integer.reverseBytes(content.getBytes().length));
                this.mPipe.write(content.getBytes());
            }
        } catch (IOException e) {
            Slog.e("HostClipboardMonitor", "Failed to set host clipboard " + e.getMessage());
        }
    }
}
