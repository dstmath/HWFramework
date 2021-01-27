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
    static final String[][] sDefaultResponses = {new String[]{"E0Q0V1", null}, new String[]{"+CMEE=2", null}, new String[]{"+CREG=2", null}, new String[]{"+CGREG=2", null}, new String[]{"+CCWA=1", null}, new String[]{"+COPS=0", null}, new String[]{"+CFUN=1", null}, new String[]{"+CGMI", "+CGMI: Android Model AT Interpreter\r"}, new String[]{"+CGMM", "+CGMM: Android Model AT Interpreter\r"}, new String[]{"+CGMR", "+CGMR: 1.0\r"}, new String[]{"+CGSN", "000000000000000\r"}, new String[]{"+CIMI", "320720000000000\r"}, new String[]{"+CSCS=?", "+CSCS: (\"HEX\",\"UCS2\")\r"}, new String[]{"+CFUN?", "+CFUN: 1\r"}, new String[]{"+COPS=3,0;+COPS?;+COPS=3,1;+COPS?;+COPS=3,2;+COPS?", "+COPS: 0,0,\"Android\"\r+COPS: 0,1,\"Android\"\r+COPS: 0,2,\"310995\"\r"}, new String[]{"+CREG?", "+CREG: 2,5, \"0113\", \"6614\"\r"}, new String[]{"+CGREG?", "+CGREG: 2,0\r"}, new String[]{"+CSQ", "+CSQ: 16,99\r"}, new String[]{"+CNMI?", "+CNMI: 1,2,2,1,1\r"}, new String[]{"+CLIR?", "+CLIR: 1,3\r"}, new String[]{"%CPVWI=2", "%CPVWI: 0\r"}, new String[]{"+CUSD=1,\"#646#\"", "+CUSD=0,\"You have used 23 minutes\"\r"}, new String[]{"+CRSM=176,12258,0,0,10", "+CRSM: 144,0,981062200050259429F6\r"}, new String[]{"+CRSM=192,12258,0,0,15", "+CRSM: 144,0,0000000A2FE204000FF55501020000\r"}, new String[]{"+CRSM=192,28474,0,0,15", "+CRSM: 144,0,0000005a6f3a040011f5220102011e\r"}, new String[]{"+CRSM=178,28474,1,4,30", "+CRSM: 144,0,437573746f6d65722043617265ffffff07818100398799f7ffffffffffff\r"}, new String[]{"+CRSM=178,28474,2,4,30", "+CRSM: 144,0,566f696365204d61696cffffffffffff07918150367742f3ffffffffffff\r"}, new String[]{"+CRSM=178,28474,3,4,30", "+CRSM: 144,0,4164676a6dffffffffffffffffffffff0b918188551512c221436587ff01\r"}, new String[]{"+CRSM=178,28474,4,4,30", "+CRSM: 144,0,810101c1ffffffffffffffffffffffff068114455245f8ffffffffffffff\r"}, new String[]{"+CRSM=192,28490,0,0,15", "+CRSM: 144,0,000000416f4a040011f5550102010d\r"}, new String[]{"+CRSM=178,28490,1,4,13", "+CRSM: 144,0,0206092143658709ffffffffff\r"}};
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

    @Override // java.lang.Runnable
    public void run() {
        while (true) {
            ServerSocket serverSocket = this.mSS;
            if (serverSocket != null) {
                try {
                    Socket s = serverSocket.accept();
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

    @Override // com.android.internal.telephony.test.SimulatedRadioControl
    public void triggerRing(String number) {
        synchronized (this) {
            if (this.mSimulatedCallState.triggerRing(number)) {
                println("RING");
            }
        }
    }

    @Override // com.android.internal.telephony.test.SimulatedRadioControl
    public void progressConnectingCallState() {
        this.mSimulatedCallState.progressConnectingCallState();
    }

    @Override // com.android.internal.telephony.test.SimulatedRadioControl
    public void progressConnectingToActive() {
        this.mSimulatedCallState.progressConnectingToActive();
    }

    @Override // com.android.internal.telephony.test.SimulatedRadioControl
    public void setAutoProgressConnectingCall(boolean b) {
        this.mSimulatedCallState.setAutoProgressConnectingCall(b);
    }

    @Override // com.android.internal.telephony.test.SimulatedRadioControl
    public void setNextDialFailImmediately(boolean b) {
        this.mSimulatedCallState.setNextDialFailImmediately(b);
    }

    @Override // com.android.internal.telephony.test.SimulatedRadioControl
    public void setNextCallFailCause(int gsmCause) {
    }

    @Override // com.android.internal.telephony.test.SimulatedRadioControl
    public void triggerHangupForeground() {
        if (this.mSimulatedCallState.triggerHangupForeground()) {
            println("NO CARRIER");
        }
    }

    @Override // com.android.internal.telephony.test.SimulatedRadioControl
    public void triggerHangupBackground() {
        if (this.mSimulatedCallState.triggerHangupBackground()) {
            println("NO CARRIER");
        }
    }

    @Override // com.android.internal.telephony.test.SimulatedRadioControl
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

    @Override // com.android.internal.telephony.test.SimulatedRadioControl
    public void triggerSsn(int a, int b) {
    }

    @Override // com.android.internal.telephony.test.SimulatedRadioControl
    public void triggerIncomingUssd(String statusCode, String message) {
    }

    @Override // com.android.internal.telephony.test.SimulatedRadioControl
    public void triggerIncomingSMS(String message) {
    }

    @Override // com.android.internal.telephony.test.SimulatedRadioControl
    public void pauseResponses() {
        synchronized (this.mPausedResponseMonitor) {
            this.mPausedResponseCount++;
        }
    }

    @Override // com.android.internal.telephony.test.SimulatedRadioControl
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
            println(lines.get(i));
        }
    }

    private void onSMSSend(String command) {
        print("> ");
        this.mLineReader.getNextLineCtrlZ();
        println("+CMGS: 1");
    }

    /* access modifiers changed from: package-private */
    public void processLine(String line) throws InterpreterEx {
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
                while (true) {
                    String[][] strArr = sDefaultResponses;
                    if (j >= strArr.length) {
                        break;
                    } else if (command.equals(strArr[j][0])) {
                        String r = sDefaultResponses[j][1];
                        if (r != null) {
                            println(r);
                        }
                        found = true;
                    } else {
                        j++;
                    }
                }
                if (!found) {
                    throw new InterpreterEx("ERROR");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public String[] splitCommands(String line) throws InterpreterEx {
        if (line.startsWith("AT")) {
            return line.length() == 2 ? new String[0] : new String[]{line.substring(2)};
        }
        throw new InterpreterEx("ERROR");
    }

    /* access modifiers changed from: package-private */
    public void println(String s) {
        synchronized (this) {
            try {
                this.mOut.write(s.getBytes("US-ASCII"));
                this.mOut.write(13);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void print(String s) {
        synchronized (this) {
            try {
                this.mOut.write(s.getBytes("US-ASCII"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override // com.android.internal.telephony.test.SimulatedRadioControl
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
