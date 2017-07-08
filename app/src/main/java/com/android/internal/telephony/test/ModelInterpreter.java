package com.android.internal.telephony.test;

import android.os.HandlerThread;
import android.os.Looper;
import android.telephony.Rlog;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ModelInterpreter implements Runnable, SimulatedRadioControl {
    static final int CONNECTING_PAUSE_MSEC = 500;
    static final String LOG_TAG = "ModelInterpreter";
    static final int MAX_CALLS = 6;
    static final int PROGRESS_CALL_STATE = 1;
    static final String[][] sDefaultResponses = null;
    private String mFinalResponse;
    HandlerThread mHandlerThread;
    InputStream mIn;
    LineReader mLineReader;
    OutputStream mOut;
    int mPausedResponseCount;
    Object mPausedResponseMonitor;
    ServerSocket mSS;
    SimulatedGsmCallState mSimulatedCallState;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.test.ModelInterpreter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.test.ModelInterpreter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.test.ModelInterpreter.<clinit>():void");
    }

    public ModelInterpreter(InputStream in, OutputStream out) {
        this.mPausedResponseMonitor = new Object();
        this.mIn = in;
        this.mOut = out;
        init();
    }

    public ModelInterpreter(InetSocketAddress sa) throws IOException {
        this.mPausedResponseMonitor = new Object();
        this.mSS = new ServerSocket();
        this.mSS.setReuseAddress(true);
        this.mSS.bind(sa);
        init();
    }

    private void init() {
        new Thread(this, LOG_TAG).start();
        this.mHandlerThread = new HandlerThread(LOG_TAG);
        this.mHandlerThread.start();
        this.mSimulatedCallState = new SimulatedGsmCallState(this.mHandlerThread.getLooper());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        loop0:
        while (true) {
            if (this.mSS != null) {
                try {
                    Socket s = this.mSS.accept();
                    try {
                        this.mIn = s.getInputStream();
                        this.mOut = s.getOutputStream();
                        Rlog.i(LOG_TAG, "New connection accepted");
                    } catch (IOException ex) {
                        Rlog.w(LOG_TAG, "IOException on accepted socket(); re-listening", ex);
                    }
                } catch (IOException ex2) {
                    Rlog.w(LOG_TAG, "IOException on socket.accept(); stopping", ex2);
                    return;
                }
            }
            this.mLineReader = new LineReader(this.mIn);
            println("Welcome");
            while (true) {
                String line = this.mLineReader.getNextLine();
                if (line == null) {
                    break;
                }
                synchronized (this.mPausedResponseMonitor) {
                    while (true) {
                        if (this.mPausedResponseCount <= 0) {
                            break;
                        }
                        try {
                            this.mPausedResponseMonitor.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
                synchronized (this) {
                    try {
                        this.mFinalResponse = "OK";
                        processLine(line);
                        println(this.mFinalResponse);
                    } catch (InterpreterEx ex3) {
                        println(ex3.mResult);
                    } catch (RuntimeException ex4) {
                        ex4.printStackTrace();
                        println("ERROR");
                    }
                }
            }
            Rlog.i(LOG_TAG, "Disconnected");
            if (this.mSS == null) {
                return;
            }
        }
    }

    public void triggerRing(String number) {
        synchronized (this) {
            if (this.mSimulatedCallState.triggerRing(number)) {
                println("RING");
            }
        }
    }

    public void progressConnectingCallState() {
        this.mSimulatedCallState.progressConnectingCallState();
    }

    public void progressConnectingToActive() {
        this.mSimulatedCallState.progressConnectingToActive();
    }

    public void setAutoProgressConnectingCall(boolean b) {
        this.mSimulatedCallState.setAutoProgressConnectingCall(b);
    }

    public void setNextDialFailImmediately(boolean b) {
        this.mSimulatedCallState.setNextDialFailImmediately(b);
    }

    public void setNextCallFailCause(int gsmCause) {
    }

    public void triggerHangupForeground() {
        if (this.mSimulatedCallState.triggerHangupForeground()) {
            println("NO CARRIER");
        }
    }

    public void triggerHangupBackground() {
        if (this.mSimulatedCallState.triggerHangupBackground()) {
            println("NO CARRIER");
        }
    }

    public void triggerHangupAll() {
        if (this.mSimulatedCallState.triggerHangupAll()) {
            println("NO CARRIER");
        }
    }

    public void sendUnsolicited(String unsol) {
        synchronized (this) {
            println(unsol);
        }
    }

    public void triggerSsn(int a, int b) {
    }

    public void triggerIncomingUssd(String statusCode, String message) {
    }

    public void triggerIncomingSMS(String message) {
    }

    public void pauseResponses() {
        synchronized (this.mPausedResponseMonitor) {
            this.mPausedResponseCount += PROGRESS_CALL_STATE;
        }
    }

    public void resumeResponses() {
        synchronized (this.mPausedResponseMonitor) {
            this.mPausedResponseCount--;
            if (this.mPausedResponseCount == 0) {
                this.mPausedResponseMonitor.notifyAll();
            }
        }
    }

    private void onAnswer() throws InterpreterEx {
        if (!this.mSimulatedCallState.onAnswer()) {
            throw new InterpreterEx("ERROR");
        }
    }

    private void onHangup() throws InterpreterEx {
        if (this.mSimulatedCallState.onAnswer()) {
            this.mFinalResponse = "NO CARRIER";
            return;
        }
        throw new InterpreterEx("ERROR");
    }

    private void onCHLD(String command) throws InterpreterEx {
        char c1 = '\u0000';
        char c0 = command.charAt(MAX_CALLS);
        if (command.length() >= 8) {
            c1 = command.charAt(7);
        }
        if (!this.mSimulatedCallState.onChld(c0, c1)) {
            throw new InterpreterEx("ERROR");
        }
    }

    private void onDial(String command) throws InterpreterEx {
        if (!this.mSimulatedCallState.onDial(command.substring(PROGRESS_CALL_STATE))) {
            throw new InterpreterEx("ERROR");
        }
    }

    private void onCLCC() {
        List<String> lines = this.mSimulatedCallState.getClccLines();
        int s = lines.size();
        for (int i = 0; i < s; i += PROGRESS_CALL_STATE) {
            println((String) lines.get(i));
        }
    }

    private void onSMSSend(String command) {
        print("> ");
        String pdu = this.mLineReader.getNextLineCtrlZ();
        println("+CMGS: 1");
    }

    void processLine(String line) throws InterpreterEx {
        String[] commands = splitCommands(line);
        for (int i = 0; i < commands.length; i += PROGRESS_CALL_STATE) {
            String command = commands[i];
            if (command.equals("A")) {
                onAnswer();
            } else if (command.equals("H")) {
                onHangup();
            } else if (command.startsWith("+CHLD=")) {
                onCHLD(command);
            } else if (command.equals("+CLCC")) {
                onCLCC();
            } else if (command.startsWith("D")) {
                onDial(command);
            } else if (command.startsWith("+CMGS=")) {
                onSMSSend(command);
            } else {
                boolean found = false;
                int j = 0;
                while (j < sDefaultResponses.length) {
                    if (command.equals(sDefaultResponses[j][0])) {
                        String r = sDefaultResponses[j][PROGRESS_CALL_STATE];
                        if (r != null) {
                            println(r);
                        }
                        found = true;
                        if (!found) {
                            throw new InterpreterEx("ERROR");
                        }
                    } else {
                        j += PROGRESS_CALL_STATE;
                    }
                }
                if (!found) {
                    throw new InterpreterEx("ERROR");
                }
            }
        }
    }

    String[] splitCommands(String line) throws InterpreterEx {
        if (!line.startsWith("AT")) {
            throw new InterpreterEx("ERROR");
        } else if (line.length() == 2) {
            return new String[0];
        } else {
            String[] ret = new String[PROGRESS_CALL_STATE];
            ret[0] = line.substring(2);
            return ret;
        }
    }

    void println(String s) {
        synchronized (this) {
            try {
                this.mOut.write(s.getBytes("US-ASCII"));
                this.mOut.write(13);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    void print(String s) {
        synchronized (this) {
            try {
                this.mOut.write(s.getBytes("US-ASCII"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void shutdown() {
        Looper looper = this.mHandlerThread.getLooper();
        if (looper != null) {
            looper.quit();
        }
        try {
            this.mIn.close();
        } catch (IOException e) {
        }
        try {
            this.mOut.close();
        } catch (IOException e2) {
        }
    }
}
