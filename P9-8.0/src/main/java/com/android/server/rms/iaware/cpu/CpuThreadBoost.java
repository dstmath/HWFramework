package com.android.server.rms.iaware.cpu;

import android.os.Process;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.cpu.CPUFeature.CPUFeatureHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CpuThreadBoost {
    private static final String STR_SYSTEM_SERVER_PROC_NAME = "system_server";
    private static final String TAG = "CpuThreadBoost";
    private static CpuThreadBoost sInstance;
    private List<String> mBoostThreadsList = new ArrayList();
    private CPUFeatureHandler mCPUFeatureHandler;
    private CPUFeature mCPUFeatureInstance;
    private boolean mEnable = false;
    private int mMyPid = 0;

    private CpuThreadBoost() {
    }

    public static synchronized CpuThreadBoost getInstance() {
        CpuThreadBoost cpuThreadBoost;
        synchronized (CpuThreadBoost.class) {
            if (sInstance == null) {
                sInstance = new CpuThreadBoost();
            }
            cpuThreadBoost = sInstance;
        }
        return cpuThreadBoost;
    }

    public void setBoostThreadsList(String[] threadsBoostInfo) {
        if (threadsBoostInfo == null) {
            AwareLog.i(TAG, "threadBootInfo is empty");
            return;
        }
        int len = threadsBoostInfo.length;
        this.mBoostThreadsList.clear();
        int i = 0;
        while (i < len) {
            if (ThreadBoostConfig.GAP_IDENTIFIER.equals(threadsBoostInfo[i])) {
                i = obtainBoostProcInfo(threadsBoostInfo, i + 1, len);
            }
            i++;
        }
    }

    public void notifyCommChange(int pid, int tgid) {
        if (this.mEnable) {
            int isBoost = 0;
            if (tgid == this.mMyPid) {
                isBoost = 1;
            }
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putInt(CPUFeature.MSG_BINDER_THREAD_CREATE);
            buffer.putInt(tgid);
            buffer.putInt(pid);
            buffer.putInt(isBoost);
            if (this.mCPUFeatureInstance != null) {
                this.mCPUFeatureInstance.sendPacket(buffer);
            }
        }
    }

    private int obtainBoostProcInfo(String[] threadsBoostInfo, int start, int len) {
        int i = start;
        if (start >= len) {
            return start;
        }
        if (STR_SYSTEM_SERVER_PROC_NAME.equals(threadsBoostInfo[start])) {
            int i2 = start + 1;
            while (i2 < len) {
                if ((ThreadBoostConfig.GAP_IDENTIFIER.equals(threadsBoostInfo[i2]) ^ 1) == 0) {
                    i = i2;
                    break;
                }
                i = i2 + 1;
                this.mBoostThreadsList.add(threadsBoostInfo[i2]);
                i2 = i;
            }
            i = i2;
        }
        return i - 1;
    }

    public void start(CPUFeature feature, CPUFeatureHandler handler) {
        this.mEnable = true;
        this.mCPUFeatureInstance = feature;
        this.mCPUFeatureHandler = handler;
        this.mMyPid = Process.myPid();
        List<String> tidStrArray = new ArrayList();
        getSystemThreads(this.mMyPid, tidStrArray);
        sendPacket(tidStrArray);
    }

    public void stop() {
        this.mEnable = false;
    }

    private void closeBufferedReader(BufferedReader br) {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeBufferedReader exception " + e.getMessage());
            }
        }
    }

    private void closeInputStreamReader(InputStreamReader isr) {
        if (isr != null) {
            try {
                isr.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeInputStreamReader exception " + e.getMessage());
            }
        }
    }

    private void closeFileInputStream(FileInputStream fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeFileInputStream exception " + e.getMessage());
            }
        }
    }

    private String getThreadName(String tidPath) {
        Throwable th;
        String commFilePath = tidPath + "/" + "comm";
        FileInputStream input = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufReader = null;
        String tidName = null;
        try {
            InputStreamReader inputStreamReader2;
            BufferedReader bufReader2;
            FileInputStream input2 = new FileInputStream(commFilePath);
            try {
                inputStreamReader2 = new InputStreamReader(input2, "UTF-8");
                try {
                    bufReader2 = new BufferedReader(inputStreamReader2);
                } catch (FileNotFoundException e) {
                    inputStreamReader = inputStreamReader2;
                    input = input2;
                    AwareLog.e(TAG, "exception file not found, file path: " + commFilePath);
                    closeBufferedReader(bufReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(input);
                    return tidName;
                } catch (UnsupportedEncodingException e2) {
                    inputStreamReader = inputStreamReader2;
                    input = input2;
                    AwareLog.e(TAG, "UnsupportedEncodingException ");
                    closeBufferedReader(bufReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(input);
                    return tidName;
                } catch (IOException e3) {
                    inputStreamReader = inputStreamReader2;
                    input = input2;
                    try {
                        AwareLog.e(TAG, "getSystemServerThreads failed!");
                        closeBufferedReader(bufReader);
                        closeInputStreamReader(inputStreamReader);
                        closeFileInputStream(input);
                        return tidName;
                    } catch (Throwable th2) {
                        th = th2;
                        closeBufferedReader(bufReader);
                        closeInputStreamReader(inputStreamReader);
                        closeFileInputStream(input);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    inputStreamReader = inputStreamReader2;
                    input = input2;
                    closeBufferedReader(bufReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(input);
                    throw th;
                }
            } catch (FileNotFoundException e4) {
                input = input2;
                AwareLog.e(TAG, "exception file not found, file path: " + commFilePath);
                closeBufferedReader(bufReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(input);
                return tidName;
            } catch (UnsupportedEncodingException e5) {
                input = input2;
                AwareLog.e(TAG, "UnsupportedEncodingException ");
                closeBufferedReader(bufReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(input);
                return tidName;
            } catch (IOException e6) {
                input = input2;
                AwareLog.e(TAG, "getSystemServerThreads failed!");
                closeBufferedReader(bufReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(input);
                return tidName;
            } catch (Throwable th4) {
                th = th4;
                input = input2;
                closeBufferedReader(bufReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(input);
                throw th;
            }
            try {
                tidName = bufReader2.readLine();
                closeBufferedReader(bufReader2);
                closeInputStreamReader(inputStreamReader2);
                closeFileInputStream(input2);
                input = input2;
            } catch (FileNotFoundException e7) {
                bufReader = bufReader2;
                inputStreamReader = inputStreamReader2;
                input = input2;
                AwareLog.e(TAG, "exception file not found, file path: " + commFilePath);
                closeBufferedReader(bufReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(input);
                return tidName;
            } catch (UnsupportedEncodingException e8) {
                bufReader = bufReader2;
                inputStreamReader = inputStreamReader2;
                input = input2;
                AwareLog.e(TAG, "UnsupportedEncodingException ");
                closeBufferedReader(bufReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(input);
                return tidName;
            } catch (IOException e9) {
                bufReader = bufReader2;
                inputStreamReader = inputStreamReader2;
                input = input2;
                AwareLog.e(TAG, "getSystemServerThreads failed!");
                closeBufferedReader(bufReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(input);
                return tidName;
            } catch (Throwable th5) {
                th = th5;
                bufReader = bufReader2;
                inputStreamReader = inputStreamReader2;
                input = input2;
                closeBufferedReader(bufReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(input);
                throw th;
            }
        } catch (FileNotFoundException e10) {
            AwareLog.e(TAG, "exception file not found, file path: " + commFilePath);
            closeBufferedReader(bufReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(input);
            return tidName;
        } catch (UnsupportedEncodingException e11) {
            AwareLog.e(TAG, "UnsupportedEncodingException ");
            closeBufferedReader(bufReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(input);
            return tidName;
        } catch (IOException e12) {
            AwareLog.e(TAG, "getSystemServerThreads failed!");
            closeBufferedReader(bufReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(input);
            return tidName;
        }
        return tidName;
    }

    private void getSystemThreads(int pid, List<String> tidStrArray) {
        File[] subFiles = new File("/proc/" + pid + "/task/").listFiles();
        if (subFiles != null) {
            for (File eachTidFile : subFiles) {
                String str = "";
                try {
                    str = eachTidFile.getCanonicalPath();
                    String tidName = getThreadName(str);
                    if (tidName != null) {
                        int boostThreadsListSize = this.mBoostThreadsList.size();
                        for (int i = 0; i < boostThreadsListSize; i++) {
                            if (tidName.contains((CharSequence) this.mBoostThreadsList.get(i))) {
                                String tidStr = getTidStr(str);
                                if (tidStr != null) {
                                    tidStrArray.add(tidStr);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    private String getTidStr(String tidPath) {
        String[] subStr = tidPath.split("task/");
        if (subStr.length == 2) {
            return subStr[1];
        }
        AwareLog.e(TAG, "getTid failed, error path is " + tidPath);
        return null;
    }

    private void sendPacket(List<String> tidStrArray) {
        int num = tidStrArray.size();
        int[] tids = new int[num];
        int i = 0;
        while (i < num) {
            try {
                tids[i] = Integer.parseInt((String) tidStrArray.get(i));
                i++;
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "parseInt failed!");
                return;
            }
        }
        ByteBuffer buffer = ByteBuffer.allocate((len + 2) * 4);
        buffer.putInt(CPUFeature.MSG_THREAD_BOOST);
        buffer.putInt(len);
        for (int putInt : tids) {
            buffer.putInt(putInt);
        }
        if (this.mCPUFeatureInstance != null) {
            this.mCPUFeatureInstance.sendPacket(buffer);
        }
    }

    private void removeCpusMsg() {
        if (this.mCPUFeatureHandler != null) {
            this.mCPUFeatureHandler.removeMessages(CPUFeature.MSG_SET_BOOST_CPUS);
            this.mCPUFeatureHandler.removeMessages(CPUFeature.MSG_RESET_BOOST_CPUS);
        }
    }

    public void setBoostCpus() {
        if (this.mEnable && this.mCPUFeatureHandler != null) {
            removeCpusMsg();
            this.mCPUFeatureHandler.sendEmptyMessage(CPUFeature.MSG_SET_BOOST_CPUS);
        }
    }

    public void resetBoostCpus() {
        if (this.mEnable && this.mCPUFeatureHandler != null) {
            removeCpusMsg();
            this.mCPUFeatureHandler.sendEmptyMessage(CPUFeature.MSG_RESET_BOOST_CPUS);
        }
    }
}
