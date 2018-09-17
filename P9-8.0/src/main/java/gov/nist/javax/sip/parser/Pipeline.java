package gov.nist.javax.sip.parser;

import gov.nist.core.InternalErrorHandler;
import gov.nist.javax.sip.stack.SIPStackTimerTask;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

public class Pipeline extends InputStream {
    private LinkedList buffList = new LinkedList();
    private Buffer currentBuffer;
    private boolean isClosed;
    private TimerTask myTimerTask;
    private InputStream pipe;
    private int readTimeout;
    private Timer timer;

    class Buffer {
        byte[] bytes;
        int length;
        int ptr = 0;

        public Buffer(byte[] bytes, int length) {
            this.length = length;
            this.bytes = bytes;
        }

        public int getNextByte() {
            byte[] bArr = this.bytes;
            int i = this.ptr;
            this.ptr = i + 1;
            return bArr[i] & 255;
        }
    }

    class MyTimer extends SIPStackTimerTask {
        private boolean isCancelled;
        Pipeline pipeline;

        protected MyTimer(Pipeline pipeline) {
            this.pipeline = pipeline;
        }

        protected void runTask() {
            if (!this.isCancelled) {
                try {
                    this.pipeline.close();
                } catch (Exception ex) {
                    InternalErrorHandler.handleException(ex);
                }
            }
        }

        public boolean cancel() {
            boolean retval = super.cancel();
            this.isCancelled = true;
            return retval;
        }
    }

    public void startTimer() {
        if (this.readTimeout != -1) {
            this.myTimerTask = new MyTimer(this);
            this.timer.schedule(this.myTimerTask, (long) this.readTimeout);
        }
    }

    public void stopTimer() {
        if (!(this.readTimeout == -1 || this.myTimerTask == null)) {
            this.myTimerTask.cancel();
        }
    }

    public Pipeline(InputStream pipe, int readTimeout, Timer timer) {
        this.timer = timer;
        this.pipe = pipe;
        this.readTimeout = readTimeout;
    }

    public void write(byte[] bytes, int start, int length) throws IOException {
        if (this.isClosed) {
            throw new IOException("Closed!!");
        }
        Buffer buff = new Buffer(bytes, length);
        buff.ptr = start;
        synchronized (this.buffList) {
            this.buffList.add(buff);
            this.buffList.notifyAll();
        }
    }

    public void write(byte[] bytes) throws IOException {
        if (this.isClosed) {
            throw new IOException("Closed!!");
        }
        Buffer buff = new Buffer(bytes, bytes.length);
        synchronized (this.buffList) {
            this.buffList.add(buff);
            this.buffList.notifyAll();
        }
    }

    public void close() throws IOException {
        this.isClosed = true;
        synchronized (this.buffList) {
            this.buffList.notifyAll();
        }
        this.pipe.close();
    }

    /* JADX WARNING: Missing block: B:11:0x0026, code:
            return r2;
     */
    /* JADX WARNING: Missing block: B:31:0x0066, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read() throws IOException {
        synchronized (this.buffList) {
            int retval;
            if (this.currentBuffer != null && this.currentBuffer.ptr < this.currentBuffer.length) {
                retval = this.currentBuffer.getNextByte();
                if (this.currentBuffer.ptr == this.currentBuffer.length) {
                    this.currentBuffer = null;
                }
            } else if (this.isClosed && this.buffList.isEmpty()) {
                return -1;
            } else {
                do {
                    try {
                        if (this.buffList.isEmpty()) {
                            this.buffList.wait();
                        } else {
                            this.currentBuffer = (Buffer) this.buffList.removeFirst();
                            retval = this.currentBuffer.getNextByte();
                            if (this.currentBuffer.ptr == this.currentBuffer.length) {
                                this.currentBuffer = null;
                            }
                        }
                    } catch (InterruptedException ex) {
                        throw new IOException(ex.getMessage());
                    } catch (NoSuchElementException ex2) {
                        ex2.printStackTrace();
                        throw new IOException(ex2.getMessage());
                    }
                } while (!this.isClosed);
                return -1;
            }
        }
    }
}
