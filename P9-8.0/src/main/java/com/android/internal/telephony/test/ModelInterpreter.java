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
    static final String[][] sDefaultResponses;
    private String mFinalResponse;
    HandlerThread mHandlerThread;
    InputStream mIn;
    LineReader mLineReader;
    OutputStream mOut;
    int mPausedResponseCount;
    Object mPausedResponseMonitor;
    ServerSocket mSS;
    SimulatedGsmCallState mSimulatedCallState;

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

    public void run() {
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
                    Rlog.i(LOG_TAG, "Disconnected");
                    if (this.mSS == null) {
                        return;
                    }
                } else {
                    synchronized (this.mPausedResponseMonitor) {
                        while (this.mPausedResponseCount > 0) {
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
            this.mPausedResponseCount++;
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
        char c1 = 0;
        char c0 = command.charAt(6);
        if (command.length() >= 8) {
            c1 = command.charAt(7);
        }
        if (!this.mSimulatedCallState.onChld(c0, c1)) {
            throw new InterpreterEx("ERROR");
        }
    }

    private void onDial(String command) throws InterpreterEx {
        if (!this.mSimulatedCallState.onDial(command.substring(1))) {
            throw new InterpreterEx("ERROR");
        }
    }

    private void onCLCC() {
        List<String> lines = this.mSimulatedCallState.getClccLines();
        int s = lines.size();
        for (int i = 0; i < s; i++) {
            println((String) lines.get(i));
        }
    }

    private void onSMSSend(String command) {
        print("> ");
        String pdu = this.mLineReader.getNextLineCtrlZ();
        println("+CMGS: 1");
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x0016 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x007d A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void processLine(String line) throws InterpreterEx {
        String[] commands = splitCommands(line);
        for (String command : commands) {
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
                        String r = sDefaultResponses[j][1];
                        if (r != null) {
                            println(r);
                        }
                        found = true;
                        if (found) {
                            throw new InterpreterEx("ERROR");
                        }
                    } else {
                        j++;
                    }
                }
                if (found) {
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
            return new String[]{line.substring(2)};
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
        return;
    }

    void print(String s) {
        synchronized (this) {
            try {
                this.mOut.write(s.getBytes("US-ASCII"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return;
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

    static {
        r0 = new String[31][];
        r0[0] = new String[]{"E0Q0V1", null};
        r0[1] = new String[]{"+CMEE=2", null};
        r0[2] = new String[]{"+CREG=2", null};
        r0[3] = new String[]{"+CGREG=2", null};
        r0[4] = new String[]{"+CCWA=1", null};
        r0[5] = new String[]{"+COPS=0", null};
        r0[6] = new String[]{"+CFUN=1", null};
        r0[7] = new String[]{"+CGMI", "+CGMI: Android Model AT Interpreter\r"};
        r0[8] = new String[]{"+CGMM", "+CGMM: Android Model AT Interpreter\r"};
        r0[9] = new String[]{"+CGMR", "+CGMR: 1.0\r"};
        r0[10] = new String[]{"+CGSN", "000000000000000\r"};
        r0[11] = new String[]{"+CIMI", "320720000000000\r"};
        r0[12] = new String[]{"+CSCS=?", "+CSCS: (\"HEX\",\"UCS2\")\r"};
        r0[13] = new String[]{"+CFUN?", "+CFUN: 1\r"};
        r0[14] = new String[]{"+COPS=3,0;+COPS?;+COPS=3,1;+COPS?;+COPS=3,2;+COPS?", "+COPS: 0,0,\"Android\"\r+COPS: 0,1,\"Android\"\r+COPS: 0,2,\"310995\"\r"};
        r0[15] = new String[]{"+CREG?", "+CREG: 2,5, \"0113\", \"6614\"\r"};
        r0[16] = new String[]{"+CGREG?", "+CGREG: 2,0\r"};
        r0[17] = new String[]{"+CSQ", "+CSQ: 16,99\r"};
        r0[18] = new String[]{"+CNMI?", "+CNMI: 1,2,2,1,1\r"};
        r0[19] = new String[]{"+CLIR?", "+CLIR: 1,3\r"};
        r0[20] = new String[]{"%CPVWI=2", "%CPVWI: 0\r"};
        r0[21] = new String[]{"+CUSD=1,\"#646#\"", "+CUSD=0,\"You have used 23 minutes\"\r"};
        r0[22] = new String[]{"+CRSM=176,12258,0,0,10", "+CRSM: 144,0,981062200050259429F6\r"};
        r0[23] = new String[]{"+CRSM=192,12258,0,0,15", "+CRSM: 144,0,0000000A2FE204000FF55501020000\r"};
        r0[24] = new String[]{"+CRSM=192,28474,0,0,15", "+CRSM: 144,0,0000005a6f3a040011f5220102011e\r"};
        r0[25] = new String[]{"+CRSM=178,28474,1,4,30", "+CRSM: 144,0,437573746f6d65722043617265ffffff07818100398799f7ffffffffffff\r"};
        r0[26] = new String[]{"+CRSM=178,28474,2,4,30", "+CRSM: 144,0,566f696365204d61696cffffffffffff07918150367742f3ffffffffffff\r"};
        r0[27] = new String[]{"+CRSM=178,28474,3,4,30", "+CRSM: 144,0,4164676a6dffffffffffffffffffffff0b918188551512c221436587ff01\r"};
        r0[28] = new String[]{"+CRSM=178,28474,4,4,30", "+CRSM: 144,0,810101c1ffffffffffffffffffffffff068114455245f8ffffffffffffff\r"};
        r0[29] = new String[]{"+CRSM=192,28490,0,0,15", "+CRSM: 144,0,000000416f4a040011f5550102010d\r"};
        r0[30] = new String[]{"+CRSM=178,28490,1,4,13", "+CRSM: 144,0,0206092143658709ffffffffff\r"};
        sDefaultResponses = r0;
    }
}
