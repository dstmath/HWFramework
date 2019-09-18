package com.android.server;

import android.annotation.SuppressLint;
import android.os.Process;
import android.util.Slog;
import com.android.server.HwServiceFactory;
import com.android.server.pm.auth.HwCertification;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.io.IoUtils;

public class HwBinderMonitor implements HwServiceFactory.IHwBinderMonitor {
    public static final int CATCH_BADPROC_BY_PID = 2;
    public static final int CATCH_BADPROC_BY_TID = 1;
    public static final String CONSTANTPATH = "/sys/kernel/debug/binder/proc/";
    private static final String CONSTANTPATH_TRACING = "/sys/kernel/tracing/binder/proc/";
    static final int NATIVE_SCORE = 0;
    public static final int PROCESS_ERROR = -1;
    public static final int PROCESS_IS_NATIVE = 1;
    public static final int PROCESS_NOT_NATIVE = 0;
    static final String TAG = "HwBinderMonitor";
    private String BINDER_TRANS_PATH = "/sys/kernel/debug/binder/transactions";
    private String BINDER_TRANS_PATH_TRACING = "/sys/kernel/tracing/binder/transactions";

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

    private final String getAdjForPid(int pid) {
        String[] outStrings = new String[1];
        Process.readProcFile("/proc/" + pid + "/oom_score", new int[]{4128}, outStrings, null, null);
        return outStrings[0];
    }

    private ArrayList<Integer> getServerPidList(int bClientPid) {
        ArrayList<Integer> serverPidList = new ArrayList<>();
        String pid = String.valueOf(bClientPid);
        BufferedReader buff = null;
        File file = new File(CONSTANTPATH + pid);
        InputStream in = null;
        Reader reader = null;
        if (!file.exists()) {
            Slog.w(TAG, "file not exists : /sys/kernel/debug/binder/proc/");
            file = new File(CONSTANTPATH_TRACING + pid);
        }
        if (file.exists()) {
            try {
                InputStream in2 = new FileInputStream(file);
                Reader reader2 = new InputStreamReader(in2, "UTF-8");
                BufferedReader buff2 = new BufferedReader(reader2);
                for (String readLine = buff2.readLine(); readLine != null; readLine = buff2.readLine()) {
                    if (isOutGoingLine(readLine, pid)) {
                        serverPidList.add(Integer.valueOf(getServerPid(readLine)));
                    }
                }
                try {
                    buff2.close();
                } catch (IOException e) {
                }
                try {
                    reader2.close();
                } catch (IOException e2) {
                }
                try {
                    in2.close();
                } catch (IOException e3) {
                }
            } catch (FileNotFoundException e4) {
                if (buff != null) {
                    try {
                        buff.close();
                    } catch (IOException e5) {
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e6) {
                    }
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e7) {
                if (buff != null) {
                    try {
                        buff.close();
                    } catch (IOException e8) {
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e9) {
                    }
                }
                if (in != null) {
                    in.close();
                }
            } catch (Throwable th) {
                if (buff != null) {
                    try {
                        buff.close();
                    } catch (IOException e10) {
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e11) {
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e12) {
                    }
                }
                throw th;
            }
        }
        return serverPidList;
    }

    private boolean isOutGoingLine(String str, String pid) {
        return Pattern.compile("outgoing transaction .+ from " + pid + ":").matcher(str).find();
    }

    private String getServerPid(String str) {
        int beginNum = str.indexOf(HwCertification.KEY_DATE_TO) + 3;
        return str.substring(beginNum, str.indexOf(":", beginNum));
    }

    public ArrayList<Integer> getNotNativeServerPidList(int bClientPid) {
        int serverPid = 0;
        ArrayList<Integer> serverPidList = new ArrayList<>();
        ArrayList<Integer> tmpServerPidList = getServerPidList(bClientPid);
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
    private ArrayList<Integer> getNotNativeFinalServerPidList(int serverPid) {
        ArrayList<Integer> finalServerPidList = new ArrayList<>();
        ArrayList<Integer> allServerPidList = getServerPidList(serverPid);
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
                String[] segments = content.split("\n");
                if (segments.length > 0 && segments[0].trim().length() > 0) {
                    return segments[0];
                }
            }
            return "unknown";
        } catch (IOException e) {
            return "unknown";
        }
    }

    private static String getTransactionLine(String str) {
        Matcher matcher = Pattern.compile("outgoing transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+)").matcher(str);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1) + ":" + matcher.group(2) + "(" + readProcName(matcher.group(1)) + ":" + readProcName(matcher.group(2)) + ") -> " + matcher.group(3) + ":" + matcher.group(4) + "(" + readProcName(matcher.group(3)) + ":" + readProcName(matcher.group(4)) + ") code: " + matcher.group(5) + "\n";
    }

    public void writeTransactonToTrace(String tracesPath) {
        BufferedReader buff = null;
        File file = new File(this.BINDER_TRANS_PATH);
        InputStream in = null;
        FileOutputStream out = null;
        Reader reader = null;
        int index = 0;
        if (!file.exists()) {
            Slog.w(TAG, "file not exists : " + this.BINDER_TRANS_PATH);
            file = new File(this.BINDER_TRANS_PATH_TRACING);
            if (!file.exists()) {
                Slog.w(TAG, "file not exists : " + this.BINDER_TRANS_PATH_TRACING);
                return;
            }
        }
        try {
            Thread.sleep(1600);
        } catch (InterruptedException e) {
        }
        try {
            InputStream in2 = new FileInputStream(file);
            Reader reader2 = new InputStreamReader(in2, "UTF-8");
            BufferedReader buff2 = new BufferedReader(reader2);
            FileOutputStream out2 = new FileOutputStream(tracesPath, true);
            out2.write("\n----- binder transactions -----\n".getBytes("UTF-8"));
            String readLine = buff2.readLine();
            while (readLine != null) {
                String transaction = getTransactionLine(readLine);
                if (transaction != null) {
                    out2.write(transaction.getBytes("UTF-8"));
                }
                readLine = buff2.readLine();
                index++;
            }
            out2.write("\n----- end binder transactions -----\n".getBytes("UTF-8"));
            try {
                in2.close();
            } catch (IOException e2) {
            }
            try {
                reader2.close();
            } catch (IOException e3) {
            }
            try {
                buff2.close();
            } catch (IOException e4) {
            }
            try {
                out2.close();
            } catch (IOException e5) {
            }
        } catch (FileNotFoundException e6) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e7) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e8) {
                }
            }
            if (buff != null) {
                try {
                    buff.close();
                } catch (IOException e9) {
                }
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e10) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e11) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e12) {
                }
            }
            if (buff != null) {
                try {
                    buff.close();
                } catch (IOException e13) {
                }
            }
            if (out != null) {
                out.close();
            }
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e14) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e15) {
                }
            }
            if (buff != null) {
                try {
                    buff.close();
                } catch (IOException e16) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e17) {
                }
            }
            throw th;
        }
    }

    private static int parseTransactionLine(String str, int tid) {
        Matcher matcher = Pattern.compile("outgoing transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+)").matcher(str);
        if (!matcher.find()) {
            return -1;
        }
        Slog.i(TAG, "parseTransactionLine1 : " + tid + " " + matcher.group(1) + ":" + matcher.group(2) + " " + matcher.group(3) + ":" + matcher.group(4));
        if (tid == Integer.parseInt(matcher.group(2))) {
            return Integer.parseInt(matcher.group(3));
        }
        return -1;
    }

    public int catchBadproc(int id, int ops) {
        ArrayList<String> transLines = new ArrayList<>();
        int ret = -1;
        if (readTransactionLines(transLines) > 0) {
            for (int i = 0; i < transLines.size(); i++) {
                String tl = transLines.get(i);
                if (ops == 1) {
                    Slog.i(TAG, "search blocked process by tid " + id);
                    ret = parseTransactionLine(tl, id);
                } else if (ops == 2) {
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

    private int readTransactionLines(ArrayList<String> transLines) {
        StringBuilder sb;
        String str;
        int lineCount = 0;
        BufferedReader buff = null;
        File file = new File(this.BINDER_TRANS_PATH);
        InputStream in = null;
        Reader reader = null;
        if (!file.exists()) {
            Slog.w(TAG, "file not exists : " + this.BINDER_TRANS_PATH);
            return -1;
        }
        try {
            InputStream in2 = new FileInputStream(file);
            Reader reader2 = new InputStreamReader(in2, "UTF-8");
            BufferedReader buff2 = new BufferedReader(reader2);
            Pattern pattern = Pattern.compile("outgoing transaction");
            for (String readLine = buff2.readLine(); readLine != null; readLine = buff2.readLine()) {
                if (pattern.matcher(readLine).find()) {
                    transLines.add(readLine);
                    lineCount++;
                }
            }
            try {
                buff2.close();
            } catch (IOException e) {
                Slog.e(TAG, "IOException" + e);
            }
            try {
                reader2.close();
            } catch (IOException e2) {
                Slog.e(TAG, "IOException" + e2);
            }
            try {
                in2.close();
            } catch (IOException e3) {
                e = e3;
                str = TAG;
                sb = new StringBuilder();
            }
        } catch (FileNotFoundException e4) {
            Slog.e(TAG, "FileNotFoundException " + e4);
            if (buff != null) {
                try {
                    buff.close();
                } catch (IOException e5) {
                    Slog.e(TAG, "IOException" + e5);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e6) {
                    Slog.e(TAG, "IOException" + e6);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e7) {
                    e = e7;
                    str = TAG;
                    sb = new StringBuilder();
                    sb.append("IOException");
                    sb.append(e);
                    Slog.e(str, sb.toString());
                    return lineCount;
                }
            }
        } catch (IOException e8) {
            Slog.e(TAG, "IOException" + e8);
            if (buff != null) {
                try {
                    buff.close();
                } catch (IOException e9) {
                    Slog.e(TAG, "IOException" + e9);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e10) {
                    Slog.e(TAG, "IOException" + e10);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e11) {
                    e = e11;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable e12) {
            if (buff != null) {
                try {
                    buff.close();
                } catch (IOException e13) {
                    Slog.e(TAG, "IOException" + e13);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e14) {
                    Slog.e(TAG, "IOException" + e14);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e15) {
                    Slog.e(TAG, "IOException" + e15);
                }
            }
            throw e12;
        }
        return lineCount;
    }

    private int findPidInTransactionLine(String transLine, int pid) {
        Matcher matcher = Pattern.compile("outgoing transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+)").matcher(transLine);
        if (matcher.find()) {
            int pid_to = Integer.parseInt(matcher.group(3));
            if (pid_to == pid) {
                return pid_to;
            }
        }
        return -1;
    }
}
