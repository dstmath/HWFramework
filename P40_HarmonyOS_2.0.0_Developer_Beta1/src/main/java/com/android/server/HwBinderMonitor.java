package com.android.server;

import android.annotation.SuppressLint;
import android.os.Process;
import android.util.Slog;
import com.android.server.hidata.wavemapping.modelservice.ModelBaseService;
import com.android.server.pm.auth.HwCertification;
import com.android.server.zrhung.IHwBinderMonitor;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.io.IoUtils;

public class HwBinderMonitor implements IHwBinderMonitor {
    private static final String BINDER_TRANS_PATH = "/sys/kernel/debug/binder/transactions";
    private static final String BINDER_TRANS_PATH_TRACING = "/sys/kernel/tracing/binder/transactions";
    public static final int CATCH_BADPROC_BY_PID = 2;
    public static final int CATCH_BADPROC_BY_TID = 1;
    private static final String CHARST_NAME = "UTF-8";
    public static final String CONSTANT_PATH = "/sys/kernel/debug/binder/proc/";
    private static final String CONSTANT_PATH_TRACING = "/sys/kernel/tracing/binder/proc/";
    private static final int DEFAULT_LIST_CAPACITY = 16;
    private static final String FILE_NOT_EXISTS = "file not exists";
    static final int NATIVE_SCORE = 0;
    public static final int PROCESS_ERROR = -1;
    public static final int PROCESS_IS_NATIVE = 1;
    public static final int PROCESS_NOT_NATIVE = 0;
    private static final String REG_EX = "outgoing transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+)";
    static final String TAG = "HwBinderMonitor";

    @SuppressLint({"AvoidMethodInForLoop"})
    public void addBinderPid(ArrayList<Integer> list, int pid) {
        ArrayList<Integer> serverPidList = getNotNativeServerPidList(pid);
        for (int i = 0; i < serverPidList.size(); i++) {
            if (!list.contains(serverPidList.get(i))) {
                Slog.i(TAG, "pid: " + serverPidList.get(i));
                list.add(serverPidList.get(i));
            }
        }
    }

    private int isNativeProcess(int pid) {
        if (pid <= 0) {
            Slog.w(TAG, "pid less than 0, pid is " + pid);
            return -1;
        }
        String pidAdj = getAdjForPid(pid);
        if (pidAdj == null) {
            Slog.w(TAG, "no such oom_score file and pid is" + pid);
            return -1;
        }
        try {
            if (Integer.parseInt(pidAdj.trim()) == 0) {
                return 1;
            }
            return 0;
        } catch (NumberFormatException e) {
            Slog.e(TAG, "isNativeProcess NumberFormatException: " + e.toString());
            return -1;
        }
    }

    private String getAdjForPid(int pid) {
        String[] outStrings = new String[1];
        Process.readProcFile("/proc/" + pid + "/oom_score", new int[]{4128}, outStrings, null, null);
        return outStrings[0];
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0084, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0085, code lost:
        $closeResource(r6, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0088, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x008b, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x008c, code lost:
        $closeResource(r5, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x008f, code lost:
        throw r6;
     */
    private List<Integer> getServerPidList(int bclientPid) {
        List<Integer> serverPidList = new ArrayList<>(16);
        String pid = String.valueOf(bclientPid);
        File file = new File(CONSTANT_PATH + pid);
        if (!file.exists()) {
            Slog.w(TAG, "file not exists/sys/kernel/debug/binder/proc/");
            file = new File(CONSTANT_PATH_TRACING + pid);
        }
        try {
            Reader reader = new InputStreamReader(new FileInputStream(file), CHARST_NAME);
            BufferedReader buff = new BufferedReader(reader);
            for (String readLine = buff.readLine(); readLine != null; readLine = buff.readLine()) {
                if (isOutGoingLine(readLine, pid) && getServerPid(readLine) != null) {
                    serverPidList.add(Integer.valueOf(getServerPid(readLine)));
                }
            }
            $closeResource(null, buff);
            $closeResource(null, reader);
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "file not found exception");
        } catch (IOException e2) {
            Slog.e(TAG, "io exception");
        }
        return serverPidList;
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    private boolean isOutGoingLine(String str, String pid) {
        return Pattern.compile("outgoing transaction .+ from " + pid + AwarenessInnerConstants.COLON_KEY).matcher(str).find();
    }

    private String getServerPid(String str) {
        if (str.indexOf(HwCertification.KEY_DATE_TO) < 0) {
            return null;
        }
        int beginNum = str.indexOf(HwCertification.KEY_DATE_TO) + 3;
        return str.substring(beginNum, str.indexOf(AwarenessInnerConstants.COLON_KEY, beginNum));
    }

    public ArrayList<Integer> getNotNativeServerPidList(int clientPid) {
        int serverPid = 0;
        ArrayList<Integer> serverPidList = new ArrayList<>(16);
        List<Integer> serverPids = getServerPidList(clientPid);
        if (!(serverPids instanceof ArrayList)) {
            return new ArrayList<>(1);
        }
        ArrayList<Integer> tmpServerPidList = (ArrayList) serverPids;
        if (tmpServerPidList.size() > 0) {
            serverPid = tmpServerPidList.get(0).intValue();
        }
        if (isNativeProcess(serverPid) == 0) {
            Slog.w(TAG, "print stack of serverPid: " + serverPid);
            serverPidList.add(Integer.valueOf(serverPid));
        }
        serverPidList.addAll(getNotNativeFinalServerPidList(serverPid));
        return serverPidList;
    }

    @SuppressLint({"AvoidMethodInForLoop"})
    private List<Integer> getNotNativeFinalServerPidList(int serverPid) {
        List<Integer> finalServerPidList = new ArrayList<>(16);
        List<Integer> allServerPidList = getServerPidList(serverPid);
        for (int i = 0; i < allServerPidList.size(); i++) {
            if (isNativeProcess(allServerPidList.get(i).intValue()) == 0 && !finalServerPidList.contains(allServerPidList.get(i))) {
                Slog.w(TAG, "print stack of finalServerPid: " + allServerPidList.get(i));
                finalServerPidList.add(allServerPidList.get(i));
            }
        }
        return finalServerPidList;
    }

    private static String readProcName(String pid) {
        try {
            String content = IoUtils.readFileAsString("/proc/" + pid + "/comm");
            if (content != null) {
                String[] segments = content.split(System.lineSeparator());
                if (segments.length > 0 && segments[0].trim().length() > 0) {
                    return segments[0];
                }
            }
            return ModelBaseService.UNKONW_IDENTIFY_RET;
        } catch (IOException e) {
            return ModelBaseService.UNKONW_IDENTIFY_RET;
        }
    }

    private static String getTransactionLine(String str) {
        Matcher matcher = Pattern.compile(REG_EX).matcher(str);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1) + AwarenessInnerConstants.COLON_KEY + matcher.group(2) + "(" + readProcName(matcher.group(1)) + AwarenessInnerConstants.COLON_KEY + readProcName(matcher.group(2)) + ") -> " + matcher.group(3) + AwarenessInnerConstants.COLON_KEY + matcher.group(4) + "(" + readProcName(matcher.group(3)) + AwarenessInnerConstants.COLON_KEY + readProcName(matcher.group(4)) + ") code: " + matcher.group(5) + System.lineSeparator();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0057, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0058, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005b, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005e, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005f, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0062, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0065, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0066, code lost:
        $closeResource(r3, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0069, code lost:
        throw r4;
     */
    public void writeTransactonToTrace(String tracesPath) {
        File file = new File(BINDER_TRANS_PATH);
        if (!file.exists()) {
            Slog.w(TAG, "file not exists/sys/kernel/debug/binder/transactions");
            file = new File(BINDER_TRANS_PATH_TRACING);
            if (!file.exists()) {
                Slog.w(TAG, "file not exists/sys/kernel/tracing/binder/transactions");
                return;
            }
        }
        try {
            Reader reader = new InputStreamReader(new FileInputStream(file), CHARST_NAME);
            BufferedReader buff = new BufferedReader(reader);
            FileOutputStream out = new FileOutputStream(tracesPath, true);
            Thread.sleep(1600);
            writeTracesPath(out, buff, 0);
            $closeResource(null, out);
            $closeResource(null, buff);
            $closeResource(null, reader);
        } catch (InterruptedException e) {
            Slog.d(TAG, "thread interrupted exception");
        } catch (FileNotFoundException e2) {
            Slog.d(TAG, "file not found exception");
        } catch (IOException e3) {
            Slog.d(TAG, "io exception occured");
        }
    }

    private void writeTracesPath(FileOutputStream out, BufferedReader buff, int index) throws IOException {
        out.write((System.lineSeparator() + "----- binder transactions -----" + System.lineSeparator()).getBytes(CHARST_NAME));
        String readLine = buff.readLine();
        int readIndex = index;
        while (readLine != null) {
            String transaction = getTransactionLine(readLine);
            if (transaction != null) {
                out.write(transaction.getBytes(CHARST_NAME));
            }
            readLine = buff.readLine();
            readIndex++;
        }
        out.write((System.lineSeparator() + "----- end binder transactions -----" + System.lineSeparator()).getBytes(CHARST_NAME));
    }

    private static int parseTransactionLine(String str, int tid) {
        Matcher matcher = Pattern.compile(REG_EX).matcher(str);
        if (!matcher.find()) {
            return -1;
        }
        Slog.i(TAG, "parseTransactionLine1 : " + tid + " " + matcher.group(1) + AwarenessInnerConstants.COLON_KEY + matcher.group(2) + " " + matcher.group(3) + AwarenessInnerConstants.COLON_KEY + matcher.group(4));
        if (tid == parseInt(matcher.group(2))) {
            return parseInt(matcher.group(3));
        }
        return -1;
    }

    public int catchBadproc(int id, int ops) {
        List<String> transLines = new ArrayList<>(16);
        int ret = -1;
        if (readTransactionLines(transLines) > 0) {
            for (int i = 0; i < transLines.size(); i++) {
                String tl = transLines.get(i);
                if (ops == 1) {
                    Slog.i(TAG, "search blocked process by tid " + id);
                    ret = parseTransactionLine(tl, id);
                }
                if (ops == 2) {
                    Slog.i(TAG, "search blocked process by pid " + id);
                    ret = findPidInTransactionLine(tl, id);
                }
                if (ret > 0) {
                    return ret;
                }
            }
        }
        return ret;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0059, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005a, code lost:
        $closeResource(r0, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005d, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0060, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0061, code lost:
        $closeResource(r0, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0064, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0067, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0068, code lost:
        $closeResource(r0, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x006b, code lost:
        throw r5;
     */
    private int readTransactionLines(List<String> transLines) {
        int lineCount = 0;
        File file = new File(BINDER_TRANS_PATH);
        if (!file.exists()) {
            Slog.w(TAG, "file not exists/sys/kernel/debug/binder/transactions");
            return -1;
        }
        try {
            InputStream in = new FileInputStream(file);
            Reader reader = new InputStreamReader(in, CHARST_NAME);
            BufferedReader buff = new BufferedReader(reader);
            Pattern pattern = Pattern.compile("outgoing transaction");
            for (String readLine = buff.readLine(); readLine != null; readLine = buff.readLine()) {
                if (pattern.matcher(readLine).find()) {
                    transLines.add(readLine);
                    lineCount++;
                }
            }
            $closeResource(null, buff);
            $closeResource(null, reader);
            $closeResource(null, in);
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "FileNotFoundException");
        } catch (IOException e2) {
            Slog.e(TAG, "IOException");
        }
        return lineCount;
    }

    private int findPidInTransactionLine(String transLine, int pid) {
        int pidTo;
        Matcher matcher = Pattern.compile(REG_EX).matcher(transLine);
        if (!matcher.find() || (pidTo = parseInt(matcher.group(3))) != pid) {
            return -1;
        }
        return pidTo;
    }

    private static int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Slog.e(TAG, "parseInt NumberFormatException e");
            return -1;
        }
    }
}
