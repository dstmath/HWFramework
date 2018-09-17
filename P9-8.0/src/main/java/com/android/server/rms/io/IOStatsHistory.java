package com.android.server.rms.io;

import android.rms.utils.Utils;
import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public final class IOStatsHistory implements Cloneable {
    public static final int ENTRY_BYTE_SIZE = 28;
    private static final String FORMAT_UTF = "UTF-8";
    private static final String TAG = "RMS.IO.IOStatsHistory";
    private List<Entry> mIOStatsList;
    private String mPkgName;
    private int mUid;

    private static class Entry implements Cloneable {
        public long mNumberOfRead;
        public long mNumberOfWrite;
        public long mStartTime;

        public Entry(long startTime, long numberOfRead, long numberOfWrite) {
            this.mStartTime = startTime;
            this.mNumberOfRead = numberOfRead;
            this.mNumberOfWrite = numberOfWrite;
        }

        public boolean isTheSameDay(long time) {
            return time == this.mStartTime;
        }

        public boolean isWriteBytesOverLimit() {
            return this.mNumberOfWrite / IOExceptionHandle.BYTES_SIZE_1G >= 1;
        }

        public Entry clone() {
            try {
                return (Entry) super.clone();
            } catch (CloneNotSupportedException e) {
                Log.e(IOStatsHistory.TAG, "fail to clone Entry,use the default object");
                return new Entry(0, 0, 0);
            }
        }
    }

    public IOStatsHistory(int uid, String pkgName) {
        this.mIOStatsList = new ArrayList();
        this.mUid = uid;
        this.mPkgName = pkgName;
    }

    public IOStatsHistory(int uid, String pkgName, long startTime, long numberOfRead, long numberOfWrite) {
        this(uid, pkgName);
        addEntry(startTime, numberOfRead, numberOfWrite);
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public int getUid() {
        return this.mUid;
    }

    public long getTotalBytesNum() {
        int pkgByteSize = 0;
        try {
            pkgByteSize = this.mPkgName != null ? this.mPkgName.getBytes(FORMAT_UTF).length : 0;
        } catch (Exception ex) {
            Log.e(TAG, "getTotalBytesNum,msg:" + ex.getMessage());
        }
        return (long) ((this.mIOStatsList.size() * 28) + pkgByteSize);
    }

    public void addEntry(long startTime, long numberOfRead, long numberOfWrite) {
        boolean isSameDay = false;
        Entry lastEntry = null;
        if (this.mIOStatsList.size() > 0) {
            lastEntry = (Entry) this.mIOStatsList.get(this.mIOStatsList.size() - 1);
            isSameDay = lastEntry.isTheSameDay(startTime);
        }
        if (!isSameDay || lastEntry == null) {
            this.mIOStatsList.add(new Entry(startTime, numberOfRead, numberOfWrite));
            return;
        }
        lastEntry.mNumberOfRead += numberOfRead;
        lastEntry.mNumberOfWrite += numberOfWrite;
    }

    public void addAllEntries(IOStatsHistory addHistory) {
        if (addHistory == null || addHistory.mIOStatsList.size() == 0) {
            Log.e(TAG, "addAllEntries, the addHistory is empty");
            return;
        }
        for (Entry entry : addHistory.mIOStatsList) {
            addEntry(entry.mStartTime, entry.mNumberOfRead, entry.mNumberOfWrite);
        }
    }

    public long calculateTotalWrittenBytes(long startTime, long endTime) {
        long totalWrittenBytes = 0;
        Log.i(TAG, "calculateTotalWrittenBytes:" + startTime + "," + endTime);
        for (Entry entry : this.mIOStatsList) {
            if (entry.mStartTime >= startTime && entry.mStartTime <= endTime) {
                totalWrittenBytes += entry.mNumberOfWrite;
            }
        }
        return totalWrittenBytes;
    }

    public AppExceptionData checkIfAppIOException() {
        int historyCount = this.mIOStatsList.size();
        if (historyCount < IOExceptionHandle.IO_EXCEPTION_DAYS_LIMIT) {
            Log.i(TAG, "checkIfAppIOException,the number of daily histories is too small");
            return null;
        }
        int count = historyCount - IOExceptionHandle.IO_EXCEPTION_DAYS_LIMIT;
        AppExceptionData appExceptionData = null;
        for (int index = 0; index <= count; index++) {
            appExceptionData = checkIfMuchWritting((historyCount - 1) - index);
            if (appExceptionData != null) {
                break;
            }
        }
        if (appExceptionData != null) {
            Log.i(TAG, "checkIfAppIOException,the first day is :" + Utils.getDateFormatValue(appExceptionData.mStartTime));
        }
        return appExceptionData;
    }

    private AppExceptionData checkIfMuchWritting(int recordStartIndex) {
        int ioExceptionCount = 0;
        AppExceptionData appExceptionData = new AppExceptionData();
        boolean isExistExp = false;
        long lastDayValue = 0;
        for (int recordIndex = recordStartIndex; recordIndex >= 0; recordIndex--) {
            Entry currentEntry = (Entry) this.mIOStatsList.get(recordIndex);
            if (!currentEntry.isWriteBytesOverLimit() || (ioExceptionCount > 0 && Utils.getDifferencesByDay(lastDayValue, currentEntry.mStartTime) != 1)) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "checkIfMuchWritting: overtime or not reach the limit: lastDayValue:" + lastDayValue + ",daytime:" + currentEntry.mStartTime);
                }
                ioExceptionCount = 0;
                appExceptionData.mPkgName = null;
                appExceptionData.mStartTime = 0;
                lastDayValue = 0;
            } else {
                if (ioExceptionCount == 0) {
                    appExceptionData.mPkgName = this.mPkgName;
                    appExceptionData.mStartTime = currentEntry.mStartTime;
                }
                if (Utils.DEBUG) {
                    Log.d(TAG, "checkIfMuchWritting: reach the limit: lastDayValue:" + lastDayValue + ",daytime:" + currentEntry.mStartTime);
                }
                ioExceptionCount++;
                lastDayValue = currentEntry.mStartTime;
                appExceptionData.mTotalWrittenBytes += currentEntry.mNumberOfWrite;
                if (ioExceptionCount >= IOExceptionHandle.IO_EXCEPTION_DAYS_LIMIT) {
                    isExistExp = true;
                    break;
                }
            }
        }
        return isExistExp ? appExceptionData : null;
    }

    public void dump(PrintWriter pw) {
        if (Utils.DEBUG) {
            Log.d(TAG, "dump uid:" + this.mUid);
        }
        pw.println("history:uid:" + this.mUid);
        pw.println("history:package:" + this.mPkgName);
        for (Entry entry : this.mIOStatsList) {
            StringBuilder builder = new StringBuilder();
            builder.append("uid:").append(this.mUid);
            builder.append(",startTime:").append(Utils.getDateFormatValue(entry.mStartTime));
            builder.append(",numberOfRead:").append(entry.mNumberOfRead);
            builder.append(",numberOfWrite:").append(entry.mNumberOfWrite);
            pw.println(builder.toString());
        }
    }

    public void writeToStream(DataOutputStream out) throws IOException {
        if (out == null) {
            Log.e(TAG, "writeToStream,DataOutputStream is null");
            return;
        }
        for (Entry entry : this.mIOStatsList) {
            if (entry.mNumberOfWrite != 0 || entry.mNumberOfRead != 0) {
                out.writeInt(this.mUid);
                out.writeUTF(this.mPkgName != null ? this.mPkgName : "");
                out.writeLong(entry.mStartTime);
                out.writeLong(entry.mNumberOfWrite);
                out.writeLong(entry.mNumberOfRead);
            } else if (Utils.DEBUG) {
                Log.d(TAG, "uid:" + this.mUid + ",writeToStream,either the number of writing or reading is empty");
            }
        }
    }

    public int queryNumberOfAccessingIO(int queryType, long startTime, long endTime) {
        if (this.mIOStatsList.size() == 0) {
            return -1;
        }
        int ioStatsNum = 0;
        for (Entry ioStats : this.mIOStatsList) {
            if (ioStats.mStartTime >= startTime && ioStats.mStartTime <= endTime) {
                if (queryType == 1) {
                    ioStatsNum = (int) (((long) ioStatsNum) + ioStats.mNumberOfRead);
                } else if (queryType == 2) {
                    ioStatsNum = (int) (((long) ioStatsNum) + ioStats.mNumberOfWrite);
                } else if (queryType == 3) {
                    ioStatsNum = (int) (((long) ((int) (((long) ioStatsNum) + ioStats.mNumberOfRead))) + ioStats.mNumberOfWrite);
                }
            }
        }
        return ioStatsNum;
    }

    public IOStatsHistory clone() {
        try {
            IOStatsHistory clone = (IOStatsHistory) super.clone();
            clone.mIOStatsList = new ArrayList();
            clone.addAllEntries(this);
            return clone;
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "fail to clone IOStatsHistory,use the default object");
            return new IOStatsHistory(0, "");
        }
    }

    public IOStatsHistory subtractFirstEntry(IOStatsHistory compareHistory) {
        IOStatsHistory history;
        if (this.mIOStatsList.size() == 0) {
            Log.e(TAG, "uid:" + this.mUid + "subtractFirstEntry,the IOStatsList is empty");
            return null;
        } else if (compareHistory == null) {
            if (Utils.DEBUG) {
                Log.d(TAG, "uid:" + this.mUid + "subtractFirstEntry,the compareHistory is null");
            }
            history = new IOStatsHistory(this.mUid, this.mPkgName);
            history.addAllEntries(this);
            return history;
        } else {
            Entry currentEntry = (Entry) this.mIOStatsList.get(0);
            Entry compareEntry = (Entry) compareHistory.mIOStatsList.get(0);
            long numberOfWrite = currentEntry.mNumberOfWrite - compareEntry.mNumberOfWrite;
            long numberOfRead = currentEntry.mNumberOfRead - compareEntry.mNumberOfRead;
            if (numberOfWrite == 0 && numberOfRead == 0) {
                Log.e(TAG, "uid:" + this.mUid + "subtractFirstEntry,both the number of Writting and Reading are emtpy");
                return null;
            }
            history = new IOStatsHistory(this.mUid, this.mPkgName);
            history.addEntry(currentEntry.mStartTime, numberOfRead, numberOfWrite);
            return history;
        }
    }
}
