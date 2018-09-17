package com.android.server;

import android.annotation.SuppressLint;
import android.os.Process;
import android.util.Slog;
import com.android.server.HwServiceFactory.IHwBinderMonitor;
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

public class HwBinderMonitor implements IHwBinderMonitor {
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
                list.add((Integer) serverPidList.get(i));
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

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0095 A:{SYNTHETIC, Splitter: B:29:0x0095} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x008c A:{SYNTHETIC, Splitter: B:24:0x008c} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x009e A:{SYNTHETIC, Splitter: B:34:0x009e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<Integer> getServerPidList(int bClientPid) {
        Throwable th;
        ArrayList<Integer> serverPidList = new ArrayList();
        String pid = String.valueOf(bClientPid);
        BufferedReader buff = null;
        File file = new File(CONSTANTPATH + pid);
        if (!file.exists()) {
            Slog.w(TAG, "file not exists : /sys/kernel/debug/binder/proc/");
            file = new File(CONSTANTPATH_TRACING + pid);
        }
        if (file.exists()) {
            try {
                BufferedReader buff2 = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                try {
                    for (String readLine = buff2.readLine(); readLine != null; readLine = buff2.readLine()) {
                        if (isOutGoingLine(readLine, pid)) {
                            serverPidList.add(Integer.valueOf(getServerPid(readLine)));
                        }
                    }
                    if (buff2 != null) {
                        try {
                            buff2.close();
                        } catch (IOException e) {
                        }
                    }
                } catch (FileNotFoundException e2) {
                    buff = buff2;
                    if (buff != null) {
                    }
                    return serverPidList;
                } catch (IOException e3) {
                    buff = buff2;
                    if (buff != null) {
                    }
                    return serverPidList;
                } catch (Throwable th2) {
                    th = th2;
                    buff = buff2;
                    if (buff != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e4) {
                if (buff != null) {
                    try {
                        buff.close();
                    } catch (IOException e5) {
                    }
                }
                return serverPidList;
            } catch (IOException e6) {
                if (buff != null) {
                    try {
                        buff.close();
                    } catch (IOException e7) {
                    }
                }
                return serverPidList;
            } catch (Throwable th3) {
                th = th3;
                if (buff != null) {
                    try {
                        buff.close();
                    } catch (IOException e8) {
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
        ArrayList<Integer> serverPidList = new ArrayList();
        ArrayList<Integer> tmpServerPidList = getServerPidList(bClientPid);
        if (tmpServerPidList.size() > 0) {
            serverPid = ((Integer) tmpServerPidList.get(0)).intValue();
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
        ArrayList<Integer> finalServerPidList = new ArrayList();
        ArrayList<Integer> allServerPidList = getServerPidList(serverPid);
        int i = 0;
        while (i < allServerPidList.size()) {
            if (isNativeProcess(((Integer) allServerPidList.get(i)).intValue()) == 0 && (finalServerPidList.contains(allServerPidList.get(i)) ^ 1) != 0) {
                Slog.w(TAG, "print stack of finalServerPid: " + allServerPidList.get(i));
                finalServerPidList.add((Integer) allServerPidList.get(i));
            }
            i++;
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
        String regEx = "outgoing transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+)";
        Matcher matcher = Pattern.compile("outgoing transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+)").matcher(str);
        if (matcher.find()) {
            return matcher.group(1) + ":" + matcher.group(2) + "(" + readProcName(matcher.group(1)) + ":" + readProcName(matcher.group(2)) + ") -> " + matcher.group(3) + ":" + matcher.group(4) + "(" + readProcName(matcher.group(3)) + ":" + readProcName(matcher.group(4)) + ") code: " + matcher.group(5) + "\n";
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:73:0x0118 A:{SYNTHETIC, Splitter: B:73:0x0118} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x011e A:{SYNTHETIC, Splitter: B:77:0x011e} */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0124 A:{SYNTHETIC, Splitter: B:81:0x0124} */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x012a A:{SYNTHETIC, Splitter: B:85:0x012a} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00f7 A:{SYNTHETIC, Splitter: B:53:0x00f7} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00fd A:{SYNTHETIC, Splitter: B:57:0x00fd} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0103 A:{SYNTHETIC, Splitter: B:61:0x0103} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0109 A:{SYNTHETIC, Splitter: B:65:0x0109} */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0139 A:{SYNTHETIC, Splitter: B:93:0x0139} */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x013f A:{SYNTHETIC, Splitter: B:97:0x013f} */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0145 A:{SYNTHETIC, Splitter: B:101:0x0145} */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x014b A:{SYNTHETIC, Splitter: B:105:0x014b} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0118 A:{SYNTHETIC, Splitter: B:73:0x0118} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x011e A:{SYNTHETIC, Splitter: B:77:0x011e} */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0124 A:{SYNTHETIC, Splitter: B:81:0x0124} */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x012a A:{SYNTHETIC, Splitter: B:85:0x012a} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00f7 A:{SYNTHETIC, Splitter: B:53:0x00f7} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00fd A:{SYNTHETIC, Splitter: B:57:0x00fd} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0103 A:{SYNTHETIC, Splitter: B:61:0x0103} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0109 A:{SYNTHETIC, Splitter: B:65:0x0109} */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0139 A:{SYNTHETIC, Splitter: B:93:0x0139} */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x013f A:{SYNTHETIC, Splitter: B:97:0x013f} */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0145 A:{SYNTHETIC, Splitter: B:101:0x0145} */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x014b A:{SYNTHETIC, Splitter: B:105:0x014b} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0118 A:{SYNTHETIC, Splitter: B:73:0x0118} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x011e A:{SYNTHETIC, Splitter: B:77:0x011e} */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0124 A:{SYNTHETIC, Splitter: B:81:0x0124} */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x012a A:{SYNTHETIC, Splitter: B:85:0x012a} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00f7 A:{SYNTHETIC, Splitter: B:53:0x00f7} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00fd A:{SYNTHETIC, Splitter: B:57:0x00fd} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0103 A:{SYNTHETIC, Splitter: B:61:0x0103} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0109 A:{SYNTHETIC, Splitter: B:65:0x0109} */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0139 A:{SYNTHETIC, Splitter: B:93:0x0139} */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x013f A:{SYNTHETIC, Splitter: B:97:0x013f} */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0145 A:{SYNTHETIC, Splitter: B:101:0x0145} */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x014b A:{SYNTHETIC, Splitter: B:105:0x014b} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0118 A:{SYNTHETIC, Splitter: B:73:0x0118} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x011e A:{SYNTHETIC, Splitter: B:77:0x011e} */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0124 A:{SYNTHETIC, Splitter: B:81:0x0124} */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x012a A:{SYNTHETIC, Splitter: B:85:0x012a} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00f7 A:{SYNTHETIC, Splitter: B:53:0x00f7} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00fd A:{SYNTHETIC, Splitter: B:57:0x00fd} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0103 A:{SYNTHETIC, Splitter: B:61:0x0103} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0109 A:{SYNTHETIC, Splitter: B:65:0x0109} */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0139 A:{SYNTHETIC, Splitter: B:93:0x0139} */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x013f A:{SYNTHETIC, Splitter: B:97:0x013f} */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0145 A:{SYNTHETIC, Splitter: B:101:0x0145} */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x014b A:{SYNTHETIC, Splitter: B:105:0x014b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void writeTransactonToTrace(String tracesPath) {
        InputStream in;
        Reader reader;
        Throwable th;
        BufferedReader buff = null;
        File file = new File(this.BINDER_TRANS_PATH);
        InputStream in2 = null;
        FileOutputStream out = null;
        Reader reader2 = null;
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
            in = new FileInputStream(file);
            try {
                reader = new InputStreamReader(in, "UTF-8");
            } catch (FileNotFoundException e2) {
                in2 = in;
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (IOException e3) {
                    }
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e4) {
                    }
                }
                if (buff != null) {
                    try {
                        buff.close();
                    } catch (IOException e5) {
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e6) {
                    }
                }
            } catch (IOException e7) {
                in2 = in;
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (IOException e8) {
                    }
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e9) {
                    }
                }
                if (buff != null) {
                    try {
                        buff.close();
                    } catch (IOException e10) {
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e11) {
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                in2 = in;
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (IOException e12) {
                    }
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e13) {
                    }
                }
                if (buff != null) {
                    try {
                        buff.close();
                    } catch (IOException e14) {
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e15) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e16) {
            if (in2 != null) {
            }
            if (reader2 != null) {
            }
            if (buff != null) {
            }
            if (out != null) {
            }
        } catch (IOException e17) {
            if (in2 != null) {
            }
            if (reader2 != null) {
            }
            if (buff != null) {
            }
            if (out != null) {
            }
        } catch (Throwable th3) {
            th = th3;
            if (in2 != null) {
            }
            if (reader2 != null) {
            }
            if (buff != null) {
            }
            if (out != null) {
            }
            throw th;
        }
        try {
            BufferedReader buff2 = new BufferedReader(reader);
            try {
                FileOutputStream out2 = new FileOutputStream(tracesPath, true);
                try {
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
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e18) {
                        }
                    } else {
                        in2 = in;
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e19) {
                        }
                    } else {
                        reader2 = reader;
                    }
                    if (buff2 != null) {
                        try {
                            buff2.close();
                        } catch (IOException e20) {
                        }
                    }
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException e21) {
                        }
                    }
                } catch (FileNotFoundException e22) {
                    reader2 = reader;
                    out = out2;
                    in2 = in;
                    buff = buff2;
                    if (in2 != null) {
                    }
                    if (reader2 != null) {
                    }
                    if (buff != null) {
                    }
                    if (out != null) {
                    }
                } catch (IOException e23) {
                    reader2 = reader;
                    out = out2;
                    in2 = in;
                    buff = buff2;
                    if (in2 != null) {
                    }
                    if (reader2 != null) {
                    }
                    if (buff != null) {
                    }
                    if (out != null) {
                    }
                } catch (Throwable th4) {
                    th = th4;
                    reader2 = reader;
                    out = out2;
                    in2 = in;
                    buff = buff2;
                    if (in2 != null) {
                    }
                    if (reader2 != null) {
                    }
                    if (buff != null) {
                    }
                    if (out != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e24) {
                reader2 = reader;
                in2 = in;
                buff = buff2;
                if (in2 != null) {
                }
                if (reader2 != null) {
                }
                if (buff != null) {
                }
                if (out != null) {
                }
            } catch (IOException e25) {
                reader2 = reader;
                in2 = in;
                buff = buff2;
                if (in2 != null) {
                }
                if (reader2 != null) {
                }
                if (buff != null) {
                }
                if (out != null) {
                }
            } catch (Throwable th5) {
                th = th5;
                reader2 = reader;
                in2 = in;
                buff = buff2;
                if (in2 != null) {
                }
                if (reader2 != null) {
                }
                if (buff != null) {
                }
                if (out != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e26) {
            reader2 = reader;
            in2 = in;
            if (in2 != null) {
            }
            if (reader2 != null) {
            }
            if (buff != null) {
            }
            if (out != null) {
            }
        } catch (IOException e27) {
            reader2 = reader;
            in2 = in;
            if (in2 != null) {
            }
            if (reader2 != null) {
            }
            if (buff != null) {
            }
            if (out != null) {
            }
        } catch (Throwable th6) {
            th = th6;
            reader2 = reader;
            in2 = in;
            if (in2 != null) {
            }
            if (reader2 != null) {
            }
            if (buff != null) {
            }
            if (out != null) {
            }
            throw th;
        }
    }

    private static int parseTransactionLine(String str, int tid) {
        String regEx = "outgoing transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+)";
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
        ArrayList<String> transLines = new ArrayList();
        int ret = -1;
        if (readTransactionLines(transLines) > 0) {
            for (int i = 0; i < transLines.size(); i++) {
                String tl = (String) transLines.get(i);
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

    /* JADX WARNING: Removed duplicated region for block: B:45:0x00a8 A:{SYNTHETIC, Splitter: B:45:0x00a8} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x009c A:{SYNTHETIC, Splitter: B:39:0x009c} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x008c A:{SYNTHETIC, Splitter: B:30:0x008c} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00a8 A:{SYNTHETIC, Splitter: B:45:0x00a8} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x009c A:{SYNTHETIC, Splitter: B:39:0x009c} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x008c A:{SYNTHETIC, Splitter: B:30:0x008c} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x009c A:{SYNTHETIC, Splitter: B:39:0x009c} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x008c A:{SYNTHETIC, Splitter: B:30:0x008c} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00a8 A:{SYNTHETIC, Splitter: B:45:0x00a8} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int readTransactionLines(ArrayList<String> transLines) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        int lineCount = 0;
        BufferedReader buff = null;
        File file = new File(this.BINDER_TRANS_PATH);
        if (file.exists()) {
            try {
                InputStream in = new FileInputStream(file);
                InputStream inputStream;
                try {
                    Reader reader = new InputStreamReader(in, "UTF-8");
                    try {
                        BufferedReader buff2 = new BufferedReader(reader);
                        try {
                            String regEx = "outgoing transaction";
                            Pattern pattern = Pattern.compile("outgoing transaction");
                            for (String readLine = buff2.readLine(); readLine != null; readLine = buff2.readLine()) {
                                if (pattern.matcher(readLine).find()) {
                                    transLines.add(readLine);
                                    lineCount++;
                                }
                            }
                            if (buff2 != null) {
                                try {
                                    buff2.close();
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                }
                            }
                            inputStream = in;
                        } catch (FileNotFoundException e4) {
                            e2 = e4;
                            inputStream = in;
                            buff = buff2;
                            e2.printStackTrace();
                            if (buff != null) {
                            }
                            return lineCount;
                        } catch (IOException e5) {
                            e3 = e5;
                            Reader reader2 = reader;
                            inputStream = in;
                            buff = buff2;
                            try {
                                e3.printStackTrace();
                                if (buff != null) {
                                }
                                return lineCount;
                            } catch (Throwable th2) {
                                th = th2;
                                if (buff != null) {
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            inputStream = in;
                            buff = buff2;
                            if (buff != null) {
                            }
                            throw th;
                        }
                    } catch (FileNotFoundException e6) {
                        e2 = e6;
                        inputStream = in;
                        e2.printStackTrace();
                        if (buff != null) {
                            try {
                                buff.close();
                            } catch (IOException e32) {
                                e32.printStackTrace();
                            }
                        }
                        return lineCount;
                    } catch (IOException e7) {
                        e32 = e7;
                        inputStream = in;
                        e32.printStackTrace();
                        if (buff != null) {
                            try {
                                buff.close();
                            } catch (IOException e322) {
                                e322.printStackTrace();
                            }
                        }
                        return lineCount;
                    } catch (Throwable th4) {
                        th = th4;
                        inputStream = in;
                        if (buff != null) {
                            try {
                                buff.close();
                            } catch (IOException e3222) {
                                e3222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e8) {
                    e2 = e8;
                    inputStream = in;
                    e2.printStackTrace();
                    if (buff != null) {
                    }
                    return lineCount;
                } catch (IOException e9) {
                    e3222 = e9;
                    inputStream = in;
                    e3222.printStackTrace();
                    if (buff != null) {
                    }
                    return lineCount;
                } catch (Throwable th5) {
                    th = th5;
                    if (buff != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e10) {
                e2 = e10;
                e2.printStackTrace();
                if (buff != null) {
                }
                return lineCount;
            } catch (IOException e11) {
                e3222 = e11;
                e3222.printStackTrace();
                if (buff != null) {
                }
                return lineCount;
            }
            return lineCount;
        }
        Slog.w(TAG, "file not exists : " + this.BINDER_TRANS_PATH);
        return -1;
    }

    private int findPidInTransactionLine(String transLine, int pid) {
        String regEx = "outgoing transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+)";
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
