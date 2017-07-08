package com.android.server.rms.iaware.cpu;

import android.os.Process;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.iaware.cpu.CPUFeature.CPUFeatureHandler;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
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
    private static final int DELAY_NUM = 3;
    private static final int DELAY_TIME = 10000;
    private static final String STR_BINDER = "Binder:";
    private static final String STR_INCALLUI_MAIN_THREAD = "ndroid.incallui";
    private static final String STR_INCALLUI_PROC_NAME = "com.android.incallui";
    private static final String STR_SYSTEM_SERVER_PROC_NAME = "system_server";
    private static final String TAG = "CpuThreadBoost";
    private static CpuThreadBoost sInstance;
    private List<String> mBoostThreadsList;
    private CPUFeatureHandler mCPUFeatureHandler;
    private CPUFeature mCPUFeatureInstance;
    private boolean mEnable;
    private int mIncalluiPid;
    private boolean mIsIncallUIBoost;
    private int mUiDelayNum;

    private CpuThreadBoost() {
        this.mUiDelayNum = 0;
        this.mIncalluiPid = 0;
        this.mEnable = false;
        this.mBoostThreadsList = new ArrayList();
        this.mIsIncallUIBoost = false;
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
        int len = threadsBoostInfo.length;
        this.mIsIncallUIBoost = false;
        this.mBoostThreadsList.clear();
        int i = 0;
        while (i < len) {
            if (threadsBoostInfo[i].equals(ThreadBoostConfig.GAP_IDENTIFIER)) {
                i = obtainBoostProcInfo(threadsBoostInfo, i + 1, len);
            }
            i++;
        }
    }

    private int obtainBoostProcInfo(String[] threadsBoostInfo, int start, int len) {
        int i = start;
        if (start >= len) {
            return start;
        }
        int i2;
        if (STR_SYSTEM_SERVER_PROC_NAME.equals(threadsBoostInfo[start])) {
            i2 = start + 1;
            while (i2 < len && !ThreadBoostConfig.GAP_IDENTIFIER.equals(threadsBoostInfo[i2])) {
                i = i2 + 1;
                this.mBoostThreadsList.add(threadsBoostInfo[i2]);
                i2 = i;
            }
            this.mBoostThreadsList.add(STR_BINDER);
            i = i2;
        } else if (STR_INCALLUI_PROC_NAME.equals(threadsBoostInfo[start])) {
            i2 = start + 1;
            while (i2 < len && !ThreadBoostConfig.GAP_IDENTIFIER.equals(threadsBoostInfo[i2])) {
                i = i2 + 1;
                if (STR_INCALLUI_MAIN_THREAD.equals(threadsBoostInfo[i2])) {
                    this.mIsIncallUIBoost = true;
                    break;
                }
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
        List<String> tidStrArray = new ArrayList();
        getSystemThreads(tidStrArray);
        if (this.mIsIncallUIBoost && getUIThreads(tidStrArray) < 0 && this.mCPUFeatureHandler != null) {
            this.mUiDelayNum++;
            this.mCPUFeatureHandler.sendEmptyMessageDelayed(CPUFeature.MSG_UI_BOOST, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        }
        sendPacket(tidStrArray);
    }

    public void stop() {
        this.mEnable = false;
        if (this.mCPUFeatureHandler != null) {
            this.mCPUFeatureHandler.removeMessages(CPUFeature.MSG_UI_BOOST);
        }
    }

    public boolean isIncallui(int pid) {
        return this.mIncalluiPid == pid;
    }

    public void uiBoost() {
        if (this.mEnable) {
            List<String> pidStrArray = new ArrayList();
            int pid = getUIThreads(pidStrArray);
            if (this.mUiDelayNum >= DELAY_NUM || pid >= 0 || this.mCPUFeatureHandler == null) {
                this.mUiDelayNum = 0;
                pidStrArray.add(Integer.toString(pid));
                sendPacket(pidStrArray);
                return;
            }
            this.mUiDelayNum++;
            this.mCPUFeatureHandler.sendEmptyMessageDelayed(CPUFeature.MSG_UI_BOOST, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        }
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

    private int getUIThreads(List<String> tidStrArray) {
        for (ProcessInfo process : ProcessInfoCollector.getInstance().getProcessInfoList()) {
            if (process != null && STR_INCALLUI_PROC_NAME.equals(process.mProcessName)) {
                int pid = process.mPid;
                this.mIncalluiPid = pid;
                tidStrArray.add(Integer.toString(process.mPid));
                return pid;
            }
        }
        return -1;
    }

    private String getThreadName(String tidPath) {
        InputStreamReader inputStreamReader;
        BufferedReader bufReader;
        Throwable th;
        String commFilePath = tidPath + "/" + "comm";
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader2 = null;
        BufferedReader bufferedReader = null;
        String tidName = null;
        try {
            FileInputStream input = new FileInputStream(commFilePath);
            try {
                inputStreamReader = new InputStreamReader(input, "UTF-8");
                try {
                    bufReader = new BufferedReader(inputStreamReader);
                } catch (FileNotFoundException e) {
                    inputStreamReader2 = inputStreamReader;
                    fileInputStream = input;
                    AwareLog.e(TAG, "exception file not found, file path: " + commFilePath);
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader2);
                    closeFileInputStream(fileInputStream);
                    return tidName;
                } catch (UnsupportedEncodingException e2) {
                    inputStreamReader2 = inputStreamReader;
                    fileInputStream = input;
                    AwareLog.e(TAG, "UnsupportedEncodingException ");
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader2);
                    closeFileInputStream(fileInputStream);
                    return tidName;
                } catch (IOException e3) {
                    inputStreamReader2 = inputStreamReader;
                    fileInputStream = input;
                    try {
                        AwareLog.e(TAG, "getSystemServerThreads failed!");
                        closeBufferedReader(bufferedReader);
                        closeInputStreamReader(inputStreamReader2);
                        closeFileInputStream(fileInputStream);
                        return tidName;
                    } catch (Throwable th2) {
                        th = th2;
                        closeBufferedReader(bufferedReader);
                        closeInputStreamReader(inputStreamReader2);
                        closeFileInputStream(fileInputStream);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    inputStreamReader2 = inputStreamReader;
                    fileInputStream = input;
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader2);
                    closeFileInputStream(fileInputStream);
                    throw th;
                }
            } catch (FileNotFoundException e4) {
                fileInputStream = input;
                AwareLog.e(TAG, "exception file not found, file path: " + commFilePath);
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader2);
                closeFileInputStream(fileInputStream);
                return tidName;
            } catch (UnsupportedEncodingException e5) {
                fileInputStream = input;
                AwareLog.e(TAG, "UnsupportedEncodingException ");
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader2);
                closeFileInputStream(fileInputStream);
                return tidName;
            } catch (IOException e6) {
                fileInputStream = input;
                AwareLog.e(TAG, "getSystemServerThreads failed!");
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader2);
                closeFileInputStream(fileInputStream);
                return tidName;
            } catch (Throwable th4) {
                th = th4;
                fileInputStream = input;
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader2);
                closeFileInputStream(fileInputStream);
                throw th;
            }
            try {
                tidName = bufReader.readLine();
                closeBufferedReader(bufReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(input);
                fileInputStream = input;
            } catch (FileNotFoundException e7) {
                bufferedReader = bufReader;
                inputStreamReader2 = inputStreamReader;
                fileInputStream = input;
                AwareLog.e(TAG, "exception file not found, file path: " + commFilePath);
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader2);
                closeFileInputStream(fileInputStream);
                return tidName;
            } catch (UnsupportedEncodingException e8) {
                bufferedReader = bufReader;
                inputStreamReader2 = inputStreamReader;
                fileInputStream = input;
                AwareLog.e(TAG, "UnsupportedEncodingException ");
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader2);
                closeFileInputStream(fileInputStream);
                return tidName;
            } catch (IOException e9) {
                bufferedReader = bufReader;
                inputStreamReader2 = inputStreamReader;
                fileInputStream = input;
                AwareLog.e(TAG, "getSystemServerThreads failed!");
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader2);
                closeFileInputStream(fileInputStream);
                return tidName;
            } catch (Throwable th5) {
                th = th5;
                bufferedReader = bufReader;
                inputStreamReader2 = inputStreamReader;
                fileInputStream = input;
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader2);
                closeFileInputStream(fileInputStream);
                throw th;
            }
        } catch (FileNotFoundException e10) {
            AwareLog.e(TAG, "exception file not found, file path: " + commFilePath);
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader2);
            closeFileInputStream(fileInputStream);
            return tidName;
        } catch (UnsupportedEncodingException e11) {
            AwareLog.e(TAG, "UnsupportedEncodingException ");
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader2);
            closeFileInputStream(fileInputStream);
            return tidName;
        } catch (IOException e12) {
            AwareLog.e(TAG, "getSystemServerThreads failed!");
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader2);
            closeFileInputStream(fileInputStream);
            return tidName;
        }
        return tidName;
    }

    private void getSystemThreads(List<String> tidStrArray) {
        File[] subFiles = new File("/proc/" + Process.myPid() + "/task/").listFiles();
        if (subFiles != null) {
            for (File eachTidFile : subFiles) {
                String tidPath = eachTidFile.getAbsolutePath();
                String tidName = getThreadName(tidPath);
                if (tidName != null) {
                    for (int i = 0; i < this.mBoostThreadsList.size(); i++) {
                        if (tidName.contains((CharSequence) this.mBoostThreadsList.get(i))) {
                            String tidStr = getTidStr(tidPath);
                            if (tidStr != null) {
                                tidStrArray.add(tidStr);
                            }
                        }
                    }
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
}
