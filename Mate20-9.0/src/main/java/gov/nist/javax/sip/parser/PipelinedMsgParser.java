package gov.nist.javax.sip.parser;

import gov.nist.core.Debug;
import gov.nist.core.InternalErrorHandler;
import gov.nist.core.Separators;
import gov.nist.javax.sip.header.ContentLength;
import gov.nist.javax.sip.message.SIPMessage;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

public final class PipelinedMsgParser implements Runnable {
    private static int uid = 0;
    private int maxMessageSize;
    private Thread mythread;
    private Pipeline rawInputStream;
    protected SIPMessageListener sipMessageListener;
    private int sizeCounter;

    protected PipelinedMsgParser() {
    }

    private static synchronized int getNewUid() {
        int i;
        synchronized (PipelinedMsgParser.class) {
            i = uid;
            uid = i + 1;
        }
        return i;
    }

    public PipelinedMsgParser(SIPMessageListener sipMessageListener2, Pipeline in, boolean debug, int maxMessageSize2) {
        this();
        this.sipMessageListener = sipMessageListener2;
        this.rawInputStream = in;
        this.maxMessageSize = maxMessageSize2;
        this.mythread = new Thread(this);
        Thread thread = this.mythread;
        thread.setName("PipelineThread-" + getNewUid());
    }

    public PipelinedMsgParser(SIPMessageListener mhandler, Pipeline in, int maxMsgSize) {
        this(mhandler, in, false, maxMsgSize);
    }

    public PipelinedMsgParser(Pipeline in) {
        this(null, in, false, 0);
    }

    public void processInput() {
        this.mythread.start();
    }

    /* access modifiers changed from: protected */
    public Object clone() {
        PipelinedMsgParser p = new PipelinedMsgParser();
        p.rawInputStream = this.rawInputStream;
        p.sipMessageListener = this.sipMessageListener;
        new Thread(p).setName("PipelineThread");
        return p;
    }

    public void setMessageListener(SIPMessageListener mlistener) {
        this.sipMessageListener = mlistener;
    }

    private String readLine(InputStream inputStream) throws IOException {
        char ch;
        StringBuffer retval = new StringBuffer("");
        do {
            int i = inputStream.read();
            if (i != -1) {
                ch = (char) i;
                if (this.maxMessageSize > 0) {
                    this.sizeCounter--;
                    if (this.sizeCounter <= 0) {
                        throw new IOException("Max size exceeded!");
                    }
                }
                if (ch != 13) {
                    retval.append(ch);
                }
            } else {
                throw new IOException("End of stream");
            }
        } while (ch != 10);
        return retval.toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:103:0x0002 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x0109 A[SYNTHETIC, Splitter:B:67:0x0109] */
    public void run() {
        String line1;
        String line2;
        int contentLength;
        Pipeline pipeline;
        Pipeline inputStream = this.rawInputStream;
        while (true) {
            try {
                this.sizeCounter = this.maxMessageSize;
                StringBuffer inputBuffer = new StringBuffer();
                if (Debug.parserDebug) {
                    Debug.println("Starting parse!");
                }
                while (true) {
                    try {
                        line1 = readLine(inputStream);
                        if (!line1.equals(Separators.RETURN)) {
                            break;
                        } else if (Debug.parserDebug) {
                            Debug.println("Discarding blank line. ");
                        }
                    } catch (IOException ex) {
                        Debug.printStackTrace(ex);
                        this.rawInputStream.stopTimer();
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            InternalErrorHandler.handleException((Exception) e);
                        }
                        return;
                    }
                }
                inputBuffer.append(line1);
                this.rawInputStream.startTimer();
                Debug.println("Reading Input Stream");
                while (true) {
                    try {
                        line2 = readLine(inputStream);
                        inputBuffer.append(line2);
                        if (line2.trim().equals("")) {
                            break;
                        }
                    } catch (IOException ex2) {
                        this.rawInputStream.stopTimer();
                        Debug.printStackTrace(ex2);
                        try {
                            inputStream.close();
                        } catch (IOException e2) {
                            InternalErrorHandler.handleException((Exception) e2);
                        }
                        return;
                    }
                }
                this.rawInputStream.stopTimer();
                inputBuffer.append(line2);
                StringMsgParser smp = new StringMsgParser(this.sipMessageListener);
                int nread = 0;
                smp.readBody = false;
                try {
                    if (Debug.debug) {
                        Debug.println("About to parse : " + inputBuffer.toString());
                    }
                    SIPMessage sipMessage = smp.parseSIPMessage(inputBuffer.toString());
                    if (sipMessage == null) {
                        this.rawInputStream.stopTimer();
                    } else {
                        if (Debug.debug) {
                            Debug.println("Completed parsing message");
                        }
                        ContentLength cl = (ContentLength) sipMessage.getContentLength();
                        if (cl != null) {
                            contentLength = cl.getContentLength();
                        } else {
                            contentLength = 0;
                        }
                        if (Debug.debug) {
                            Debug.println("contentLength " + contentLength);
                        }
                        if (contentLength == 0) {
                            sipMessage.removeContent();
                        } else if (this.maxMessageSize == 0 || contentLength < this.sizeCounter) {
                            byte[] message_body = new byte[contentLength];
                            while (true) {
                                if (nread >= contentLength) {
                                    break;
                                }
                                this.rawInputStream.startTimer();
                                try {
                                    int readlength = inputStream.read(message_body, nread, contentLength - nread);
                                    if (readlength <= 0) {
                                        pipeline = this.rawInputStream;
                                        break;
                                    } else {
                                        nread += readlength;
                                        this.rawInputStream.stopTimer();
                                    }
                                } catch (IOException ex3) {
                                    Debug.logError("Exception Reading Content", ex3);
                                    pipeline = this.rawInputStream;
                                    pipeline.stopTimer();
                                    sipMessage.setMessageContent(message_body);
                                    if (this.sipMessageListener == null) {
                                    }
                                }
                            }
                            sipMessage.setMessageContent(message_body);
                        }
                        if (this.sipMessageListener == null) {
                            try {
                                this.sipMessageListener.processMessage(sipMessage);
                            } catch (Exception e3) {
                                try {
                                    inputStream.close();
                                } catch (IOException e4) {
                                    InternalErrorHandler.handleException((Exception) e4);
                                }
                                return;
                            }
                        }
                    }
                } catch (ParseException ex4) {
                    Debug.logError("Detected a parse error", ex4);
                }
            } catch (Throwable th) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    InternalErrorHandler.handleException((Exception) e5);
                }
                throw th;
            }
        }
    }

    public void close() {
        try {
            this.rawInputStream.close();
        } catch (IOException e) {
        }
    }
}
