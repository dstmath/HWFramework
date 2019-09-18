package com.android.internal.telephony.imsphone;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telecom.Connection;
import android.telephony.Rlog;
import com.android.internal.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ImsRttTextHandler extends Handler {
    private static final int APPEND_TO_NETWORK_BUFFER = 2;
    private static final int ATTEMPT_SEND_TO_NETWORK = 4;
    private static final int EXPIRE_SENT_CODEPOINT_COUNT = 5;
    private static final int INITIALIZE = 1;
    private static final String LOG_TAG = "ImsRttTextHandler";
    public static final int MAX_BUFFERED_CHARACTER_COUNT = 5;
    public static final int MAX_BUFFERING_DELAY_MILLIS = 200;
    public static final int MAX_CODEPOINTS_PER_SECOND = 30;
    private static final int MILLIS_PER_SECOND = 1000;
    private static final int SEND_TO_INCALL = 3;
    private static final int TEARDOWN = 9999;
    private StringBuffer mBufferedTextToIncall = new StringBuffer();
    private StringBuffer mBufferedTextToNetwork = new StringBuffer();
    private int mCodepointsAvailableForTransmission = 30;
    private final NetworkWriter mNetworkWriter;
    /* access modifiers changed from: private */
    public CountDownLatch mReadNotifier;
    private InCallReaderThread mReaderThread;
    private Connection.RttTextStream mRttTextStream;

    private class InCallReaderThread extends Thread {
        private final Connection.RttTextStream mReaderThreadRttTextStream;

        public InCallReaderThread(Connection.RttTextStream textStream) {
            this.mReaderThreadRttTextStream = textStream;
        }

        public void run() {
            while (true) {
                try {
                    String charsReceived = this.mReaderThreadRttTextStream.read();
                    if (charsReceived == null) {
                        break;
                    } else if (charsReceived.length() != 0) {
                        ImsRttTextHandler.this.obtainMessage(2, charsReceived).sendToTarget();
                        if (ImsRttTextHandler.this.mReadNotifier != null) {
                            ImsRttTextHandler.this.mReadNotifier.countDown();
                        }
                    }
                } catch (IOException e) {
                    Rlog.e(ImsRttTextHandler.LOG_TAG, "RttReaderThread - IOException encountered reading from in-call: %s", e);
                    ImsRttTextHandler.this.obtainMessage(ImsRttTextHandler.TEARDOWN).sendToTarget();
                    return;
                }
            }
            if (Thread.currentThread().isInterrupted()) {
                Rlog.i(ImsRttTextHandler.LOG_TAG, "RttReaderThread - Thread interrupted. Finishing.");
                return;
            }
            Rlog.e(ImsRttTextHandler.LOG_TAG, "RttReaderThread - Stream closed unexpectedly. Attempt to reinitialize.");
            ImsRttTextHandler.this.obtainMessage(ImsRttTextHandler.TEARDOWN).sendToTarget();
        }
    }

    public interface NetworkWriter {
        void write(String str);
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i != TEARDOWN) {
            switch (i) {
                case 1:
                    if (this.mRttTextStream == null && this.mReaderThread == null) {
                        this.mRttTextStream = (Connection.RttTextStream) msg.obj;
                        this.mReaderThread = new InCallReaderThread(this.mRttTextStream);
                        this.mReaderThread.start();
                        break;
                    } else {
                        Rlog.e(LOG_TAG, "RTT text stream already initialized. Ignoring.");
                        return;
                    }
                    break;
                case 2:
                    this.mBufferedTextToNetwork.append((String) msg.obj);
                    if (this.mBufferedTextToNetwork.codePointCount(0, this.mBufferedTextToNetwork.length()) < 5) {
                        sendEmptyMessageDelayed(4, 200);
                        break;
                    } else {
                        sendMessageAtFrontOfQueue(obtainMessage(4));
                        break;
                    }
                case 3:
                    String messageToIncall = (String) msg.obj;
                    try {
                        this.mRttTextStream.write(messageToIncall);
                        break;
                    } catch (IOException e) {
                        Rlog.e(LOG_TAG, "IOException encountered writing to in-call: %s", e);
                        obtainMessage(TEARDOWN).sendToTarget();
                        this.mBufferedTextToIncall.append(messageToIncall);
                        break;
                    }
                case 4:
                    int numCodePointsSent = Math.min(this.mBufferedTextToNetwork.codePointCount(0, this.mBufferedTextToNetwork.length()), this.mCodepointsAvailableForTransmission);
                    if (numCodePointsSent != 0) {
                        int endSendIndex = this.mBufferedTextToNetwork.offsetByCodePoints(0, numCodePointsSent);
                        String stringToSend = this.mBufferedTextToNetwork.substring(0, endSendIndex);
                        this.mBufferedTextToNetwork.delete(0, endSendIndex);
                        this.mNetworkWriter.write(stringToSend);
                        this.mCodepointsAvailableForTransmission -= numCodePointsSent;
                        sendMessageDelayed(obtainMessage(5, numCodePointsSent, 0), 1000);
                        break;
                    }
                    break;
                case 5:
                    this.mCodepointsAvailableForTransmission += msg.arg1;
                    if (this.mCodepointsAvailableForTransmission > 0) {
                        sendMessageAtFrontOfQueue(obtainMessage(4));
                        break;
                    }
                    break;
            }
        } else {
            try {
                if (this.mReaderThread != null) {
                    this.mReaderThread.join(1000);
                }
            } catch (InterruptedException e2) {
            }
            this.mReaderThread = null;
            this.mRttTextStream = null;
        }
    }

    public ImsRttTextHandler(Looper looper, NetworkWriter networkWriter) {
        super(looper);
        this.mNetworkWriter = networkWriter;
    }

    public void sendToInCall(String msg) {
        obtainMessage(3, msg).sendToTarget();
    }

    public void initialize(Connection.RttTextStream rttTextStream) {
        obtainMessage(1, rttTextStream).sendToTarget();
    }

    public void tearDown() {
        obtainMessage(TEARDOWN).sendToTarget();
    }

    @VisibleForTesting
    public void setReadNotifier(CountDownLatch latch) {
        this.mReadNotifier = latch;
    }

    public String getNetworkBufferText() {
        return this.mBufferedTextToNetwork.toString();
    }
}
