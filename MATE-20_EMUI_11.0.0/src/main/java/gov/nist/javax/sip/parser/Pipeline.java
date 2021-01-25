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

    class MyTimer extends SIPStackTimerTask {
        private boolean isCancelled;
        Pipeline pipeline;

        protected MyTimer(Pipeline pipeline2) {
            this.pipeline = pipeline2;
        }

        /* access modifiers changed from: protected */
        @Override // gov.nist.javax.sip.stack.SIPStackTimerTask
        public void runTask() {
            if (!this.isCancelled) {
                try {
                    this.pipeline.close();
                } catch (IOException ex) {
                    InternalErrorHandler.handleException(ex);
                }
            }
        }

        @Override // java.util.TimerTask
        public boolean cancel() {
            boolean retval = super.cancel();
            this.isCancelled = true;
            return retval;
        }
    }

    class Buffer {
        byte[] bytes;
        int length;
        int ptr = 0;

        public Buffer(byte[] bytes2, int length2) {
            this.length = length2;
            this.bytes = bytes2;
        }

        public int getNextByte() {
            byte[] bArr = this.bytes;
            int i = this.ptr;
            this.ptr = i + 1;
            return bArr[i] & 255;
        }
    }

    public void startTimer() {
        if (this.readTimeout != -1) {
            this.myTimerTask = new MyTimer(this);
            this.timer.schedule(this.myTimerTask, (long) this.readTimeout);
        }
    }

    public void stopTimer() {
        TimerTask timerTask;
        if (this.readTimeout != -1 && (timerTask = this.myTimerTask) != null) {
            timerTask.cancel();
        }
    }

    public Pipeline(InputStream pipe2, int readTimeout2, Timer timer2) {
        this.timer = timer2;
        this.pipe = pipe2;
        this.readTimeout = readTimeout2;
    }

    public void write(byte[] bytes, int start, int length) throws IOException {
        if (!this.isClosed) {
            Buffer buff = new Buffer(bytes, length);
            buff.ptr = start;
            synchronized (this.buffList) {
                this.buffList.add(buff);
                this.buffList.notifyAll();
            }
            return;
        }
        throw new IOException("Closed!!");
    }

    public void write(byte[] bytes) throws IOException {
        if (!this.isClosed) {
            Buffer buff = new Buffer(bytes, bytes.length);
            synchronized (this.buffList) {
                this.buffList.add(buff);
                this.buffList.notifyAll();
            }
            return;
        }
        throw new IOException("Closed!!");
    }

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.isClosed = true;
        synchronized (this.buffList) {
            this.buffList.notifyAll();
        }
        this.pipe.close();
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        synchronized (this.buffList) {
            if (this.currentBuffer != null && this.currentBuffer.ptr < this.currentBuffer.length) {
                int retval = this.currentBuffer.getNextByte();
                if (this.currentBuffer.ptr == this.currentBuffer.length) {
                    this.currentBuffer = null;
                }
                return retval;
            } else if (this.isClosed && this.buffList.isEmpty()) {
                return -1;
            } else {
                do {
                    try {
                        if (this.buffList.isEmpty()) {
                            this.buffList.wait();
                        } else {
                            this.currentBuffer = (Buffer) this.buffList.removeFirst();
                            int retval2 = this.currentBuffer.getNextByte();
                            if (this.currentBuffer.ptr == this.currentBuffer.length) {
                                this.currentBuffer = null;
                            }
                            return retval2;
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
