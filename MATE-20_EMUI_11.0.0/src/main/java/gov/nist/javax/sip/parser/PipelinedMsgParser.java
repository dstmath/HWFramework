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
    @Override // java.lang.Object
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
                if (ch != '\r') {
                    retval.append(ch);
                }
            } else {
                throw new IOException("End of stream");
            }
        } while (ch != '\n');
        return retval.toString();
    }

    @Override // java.lang.Runnable
    public void run() {
        String line1;
        String line2;
        int contentLength;
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
                            return;
                        } catch (IOException e) {
                            InternalErrorHandler.handleException(e);
                            return;
                        }
                    }
                }
                inputBuffer.append(line1);
                this.rawInputStream.startTimer();
                Debug.println("Reading Input Stream");
                do {
                    try {
                        line2 = readLine(inputStream);
                        inputBuffer.append(line2);
                    } catch (IOException ex2) {
                        this.rawInputStream.stopTimer();
                        Debug.printStackTrace(ex2);
                        try {
                            inputStream.close();
                            return;
                        } catch (IOException e2) {
                            InternalErrorHandler.handleException(e2);
                            return;
                        }
                    }
                } while (!line2.trim().equals(""));
                this.rawInputStream.stopTimer();
                inputBuffer.append(line2);
                StringMsgParser smp = new StringMsgParser(this.sipMessageListener);
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
                            int nread = 0;
                            while (true) {
                                if (nread >= contentLength) {
                                    break;
                                }
                                this.rawInputStream.startTimer();
                                try {
                                    int readlength = inputStream.read(message_body, nread, contentLength - nread);
                                    if (readlength <= 0) {
                                        this.rawInputStream.stopTimer();
                                        break;
                                    }
                                    nread += readlength;
                                } catch (IOException ex3) {
                                    Debug.logError("Exception Reading Content", ex3);
                                } finally {
                                    this.rawInputStream.stopTimer();
                                }
                            }
                            sipMessage.setMessageContent(message_body);
                        }
                        if (this.sipMessageListener != null) {
                            try {
                                this.sipMessageListener.processMessage(sipMessage);
                            } catch (Exception e3) {
                                try {
                                    return;
                                } catch (IOException e4) {
                                    return;
                                }
                            }
                        }
                    }
                } catch (ParseException ex4) {
                    Debug.logError("Detected a parse error", ex4);
                }
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e42) {
                    InternalErrorHandler.handleException(e42);
                }
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
