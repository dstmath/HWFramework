package com.android.server;

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
    public static final String CONSTANTPATH = "/sys/kernel/debug/binder/proc/";
    static final int NATIVE_SCORE = 0;
    public static final int PROCESS_ERROR = -1;
    public static final int PROCESS_IS_NATIVE = 1;
    public static final int PROCESS_NOT_NATIVE = 0;
    static final String TAG = "HwBinderMonitor";
    private String BINDER_TRANS_PATH;

    public HwBinderMonitor() {
        this.BINDER_TRANS_PATH = "/sys/kernel/debug/binder/transactions";
    }

    public void addBinderPid(ArrayList<Integer> list, int pid) {
        ArrayList<Integer> serverPidList = getNotNativeServerPidList(pid);
        for (int i = PROCESS_NOT_NATIVE; i < serverPidList.size(); i += PROCESS_IS_NATIVE) {
            if (!list.contains(serverPidList.get(i))) {
                Slog.i(TAG, "pid: " + serverPidList.get(i));
                list.add((Integer) serverPidList.get(i));
            }
        }
    }

    private int isNativeProcess(int pid) {
        if (pid <= 0) {
            Slog.w(TAG, "pid less than 0, pid is " + pid);
            return PROCESS_ERROR;
        }
        String pidAdj = getAdjForPid(pid);
        if (pidAdj == null) {
            Slog.w(TAG, "no such oom_score file and pid is" + pid);
            return PROCESS_ERROR;
        }
        try {
            if (Integer.parseInt(pidAdj.trim()) == 0) {
                return PROCESS_IS_NATIVE;
            }
            return PROCESS_NOT_NATIVE;
        } catch (NumberFormatException e) {
            Slog.e(TAG, "isNativeProcess NumberFormatException: " + e.toString());
            return PROCESS_ERROR;
        }
    }

    private final String getAdjForPid(int pid) {
        String[] outStrings = new String[PROCESS_IS_NATIVE];
        String str = "/proc/" + pid + "/oom_score";
        int[] iArr = new int[PROCESS_IS_NATIVE];
        iArr[PROCESS_NOT_NATIVE] = 4128;
        Process.readProcFile(str, iArr, outStrings, null, null);
        return outStrings[PROCESS_NOT_NATIVE];
    }

    private ArrayList<Integer> getServerPidList(int bClientPid) {
        Throwable th;
        ArrayList<Integer> serverPidList = new ArrayList();
        String pid = String.valueOf(bClientPid);
        BufferedReader bufferedReader = null;
        File file = new File(CONSTANTPATH + pid);
        if (file.exists()) {
            try {
                BufferedReader buff = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                try {
                    for (String readLine = buff.readLine(); readLine != null; readLine = buff.readLine()) {
                        if (isOutGoingLine(readLine, pid)) {
                            serverPidList.add(Integer.valueOf(getServerPid(readLine)));
                        }
                    }
                    if (buff != null) {
                        try {
                            buff.close();
                        } catch (IOException e) {
                        }
                    }
                } catch (FileNotFoundException e2) {
                    bufferedReader = buff;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e3) {
                        }
                    }
                    return serverPidList;
                } catch (IOException e4) {
                    bufferedReader = buff;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e5) {
                        }
                    }
                    return serverPidList;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = buff;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e6) {
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e7) {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                return serverPidList;
            } catch (IOException e8) {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                return serverPidList;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedReader != null) {
                    bufferedReader.close();
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
        int serverPid = PROCESS_NOT_NATIVE;
        ArrayList<Integer> serverPidList = new ArrayList();
        ArrayList<Integer> tmpServerPidList = getServerPidList(bClientPid);
        if (tmpServerPidList.size() > 0) {
            serverPid = ((Integer) tmpServerPidList.get(PROCESS_NOT_NATIVE)).intValue();
        }
        if (isNativeProcess(serverPid) == 0) {
            Slog.w(TAG, "print stack of serverPid: " + serverPid);
            serverPidList.add(Integer.valueOf(serverPid));
        }
        serverPidList.addAll(getNotNativeFinalServerPidList(serverPid));
        return serverPidList;
    }

    private ArrayList<Integer> getNotNativeFinalServerPidList(int serverPid) {
        ArrayList<Integer> finalServerPidList = new ArrayList();
        ArrayList<Integer> allServerPidList = getServerPidList(serverPid);
        int i = PROCESS_NOT_NATIVE;
        while (i < allServerPidList.size()) {
            if (isNativeProcess(((Integer) allServerPidList.get(i)).intValue()) == 0 && !finalServerPidList.contains(allServerPidList.get(i))) {
                Slog.w(TAG, "print stack of finalServerPid: " + allServerPidList.get(i));
                finalServerPidList.add((Integer) allServerPidList.get(i));
            }
            i += PROCESS_IS_NATIVE;
        }
        return finalServerPidList;
    }

    private static String readProcName(String pid) {
        try {
            String content = IoUtils.readFileAsString("/proc/" + pid + "/comm");
            if (content != null) {
                String[] segments = content.split("\n");
                if (segments.length > 0 && segments[PROCESS_NOT_NATIVE].trim().length() > 0) {
                    return segments[PROCESS_NOT_NATIVE];
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
            return matcher.group(PROCESS_IS_NATIVE) + ":" + matcher.group(2) + "(" + readProcName(matcher.group(PROCESS_IS_NATIVE)) + ":" + readProcName(matcher.group(2)) + ") -> " + matcher.group(3) + ":" + matcher.group(4) + "(" + readProcName(matcher.group(3)) + ":" + readProcName(matcher.group(4)) + ") code: " + matcher.group(5) + "\n";
        }
        return null;
    }

    public void writeTransactonToTrace(String tracesPath) {
        InputStream in;
        Throwable th;
        BufferedReader bufferedReader = null;
        File file = new File(this.BINDER_TRANS_PATH);
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        Reader reader = null;
        int index = PROCESS_NOT_NATIVE;
        if (file.exists()) {
            try {
                Thread.sleep(1600);
            } catch (InterruptedException e) {
            }
            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException e2) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e4) {
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e5) {
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e6) {
                    }
                }
                return;
            } catch (IOException e7) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e8) {
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e9) {
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e10) {
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e11) {
                    }
                }
                return;
            } catch (Throwable th2) {
                th = th2;
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e12) {
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e13) {
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e14) {
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e15) {
                    }
                }
                throw th;
            }
            try {
                Reader reader2 = new InputStreamReader(in, "UTF-8");
                try {
                    BufferedReader buff = new BufferedReader(reader2);
                    try {
                        FileOutputStream out = new FileOutputStream(tracesPath, true);
                        try {
                            out.write("\n----- binder transactions -----\n".getBytes("UTF-8"));
                            String readLine = buff.readLine();
                            while (readLine != null) {
                                String transaction = getTransactionLine(readLine);
                                if (transaction != null) {
                                    out.write(transaction.getBytes("UTF-8"));
                                }
                                readLine = buff.readLine();
                                index += PROCESS_IS_NATIVE;
                            }
                            out.write("\n----- end binder transactions -----\n".getBytes("UTF-8"));
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e16) {
                                }
                            } else {
                                inputStream = in;
                            }
                            if (reader2 != null) {
                                try {
                                    reader2.close();
                                } catch (IOException e17) {
                                }
                            } else {
                                reader = reader2;
                            }
                            if (buff != null) {
                                try {
                                    buff.close();
                                } catch (IOException e18) {
                                }
                            }
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException e19) {
                                }
                                return;
                            }
                        } catch (FileNotFoundException e20) {
                            reader = reader2;
                            fileOutputStream = out;
                            inputStream = in;
                            bufferedReader = buff;
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            if (reader != null) {
                                reader.close();
                            }
                            if (bufferedReader != null) {
                                bufferedReader.close();
                            }
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                            return;
                        } catch (IOException e21) {
                            reader = reader2;
                            fileOutputStream = out;
                            inputStream = in;
                            bufferedReader = buff;
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            if (reader != null) {
                                reader.close();
                            }
                            if (bufferedReader != null) {
                                bufferedReader.close();
                            }
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                            return;
                        } catch (Throwable th3) {
                            th = th3;
                            reader = reader2;
                            fileOutputStream = out;
                            inputStream = in;
                            bufferedReader = buff;
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            if (reader != null) {
                                reader.close();
                            }
                            if (bufferedReader != null) {
                                bufferedReader.close();
                            }
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                            throw th;
                        }
                    } catch (FileNotFoundException e22) {
                        reader = reader2;
                        inputStream = in;
                        bufferedReader = buff;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (reader != null) {
                            reader.close();
                        }
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        return;
                    } catch (IOException e23) {
                        reader = reader2;
                        inputStream = in;
                        bufferedReader = buff;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (reader != null) {
                            reader.close();
                        }
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        return;
                    } catch (Throwable th4) {
                        th = th4;
                        reader = reader2;
                        inputStream = in;
                        bufferedReader = buff;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (reader != null) {
                            reader.close();
                        }
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e24) {
                    reader = reader2;
                    inputStream = in;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (reader != null) {
                        reader.close();
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    return;
                } catch (IOException e25) {
                    reader = reader2;
                    inputStream = in;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (reader != null) {
                        reader.close();
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    return;
                } catch (Throwable th5) {
                    th = th5;
                    reader = reader2;
                    inputStream = in;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (reader != null) {
                        reader.close();
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e26) {
                inputStream = in;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return;
            } catch (IOException e27) {
                inputStream = in;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return;
            } catch (Throwable th6) {
                th = th6;
                inputStream = in;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
            return;
        }
        Slog.w(TAG, "file not exists : " + this.BINDER_TRANS_PATH);
    }
}
