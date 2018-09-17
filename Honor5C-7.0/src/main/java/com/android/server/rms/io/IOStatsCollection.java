package com.android.server.rms.io;

import android.util.Log;
import android.util.SparseArray;
import com.android.server.rms.io.IOFileRotator.Reader;
import com.android.server.rms.utils.Utils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class IOStatsCollection implements Reader, Cloneable {
    private static final String TAG = "IO.IOStatsCollection";
    private SparseArray<IOStatsHistory> mStatsMap;

    public IOStatsCollection() {
        this.mStatsMap = new SparseArray();
    }

    public int getTotalBytes() {
        int totalBytes = 0;
        for (int index = 0; index < this.mStatsMap.size(); index++) {
            totalBytes = (int) (((long) totalBytes) + ((IOStatsHistory) this.mStatsMap.get(this.mStatsMap.keyAt(index))).getTotalBytesNum());
        }
        return totalBytes;
    }

    public SparseArray<IOStatsHistory> getIOStatsHistoryMap() {
        return this.mStatsMap;
    }

    public void reset() {
        this.mStatsMap.clear();
    }

    public void dump(PrintWriter pw) {
        if (Utils.DEBUG) {
            Log.d(TAG, "begin dump IOStatsCollection");
        }
        for (int index = 0; index < this.mStatsMap.size(); index++) {
            int uid = this.mStatsMap.keyAt(index);
            IOStatsHistory history = (IOStatsHistory) this.mStatsMap.get(uid);
            if (history == null) {
                Log.e(TAG, "uid:" + uid + "'s history is empty");
            } else {
                history.dump(pw);
            }
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "end dump IOStatsCollection");
        }
    }

    public void addHistories(IOStatsCollection fromCollection) {
        if (fromCollection == null || fromCollection.getIOStatsHistoryMap().size() == 0) {
            Log.e(TAG, "addHistories,the fromCollection is empty");
            return;
        }
        SparseArray<IOStatsHistory> fromHistoryMap = fromCollection.getIOStatsHistoryMap();
        for (int index = 0; index < fromHistoryMap.size(); index++) {
            int uid = fromHistoryMap.keyAt(index);
            IOStatsHistory history = (IOStatsHistory) this.mStatsMap.get(uid);
            IOStatsHistory addHistory = (IOStatsHistory) fromHistoryMap.get(uid);
            if (history == null) {
                this.mStatsMap.put(uid, addHistory.clone());
            } else {
                history.addAllEntries(addHistory);
            }
        }
    }

    public IOStatsCollection clone() {
        try {
            IOStatsCollection clone = (IOStatsCollection) super.clone();
            clone.mStatsMap = new SparseArray();
            clone.addHistories(this);
            return clone;
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "fail to clone IOStatsCollection,use the default value");
            return new IOStatsCollection();
        }
    }

    public void recordHistory(IOStatsHistory history) {
        if (history == null) {
            Log.e(TAG, "recordHistory,history is null");
            return;
        }
        int uid = history.getUid();
        if (uid <= 0) {
            Log.e(TAG, "recordHistory,uid is invalid");
            return;
        }
        IOStatsHistory historyFound = (IOStatsHistory) this.mStatsMap.get(uid);
        if (historyFound == null) {
            this.mStatsMap.put(uid, history.clone());
        } else {
            historyFound.addAllEntries(history);
        }
    }

    public void read(InputStream in) throws IOException {
        if (in == null) {
            Log.e(TAG, "read,InputStream is null");
            return;
        }
        DataInputStream inputStream = new DataInputStream(in);
        SparseArray<IOStatsHistory> current = this.mStatsMap.clone();
        this.mStatsMap.clear();
        while (inputStream.available() >= 28) {
            IOStatsHistory history;
            int uid = inputStream.readInt();
            String packageName = inputStream.readUTF();
            long startTime = inputStream.readLong();
            long numberOfWrite = inputStream.readLong();
            long numberOfRead = inputStream.readLong();
            if (this.mStatsMap.indexOfKey(uid) >= 0) {
                history = (IOStatsHistory) this.mStatsMap.get(uid);
            } else {
                history = new IOStatsHistory(uid, packageName);
                this.mStatsMap.put(uid, history);
            }
            history.addEntry(startTime, numberOfRead, numberOfWrite);
        }
        for (int index = 0; index < current.size(); index++) {
            uid = current.keyAt(index);
            history = (IOStatsHistory) current.get(uid);
            if (this.mStatsMap.indexOfKey(uid) >= 0) {
                ((IOStatsHistory) this.mStatsMap.get(uid)).addAllEntries(history);
            } else {
                this.mStatsMap.put(uid, history);
            }
        }
    }

    public void write(OutputStream out) throws IOException {
        if (out == null) {
            Log.e(TAG, "write,OutputStream is null");
            return;
        }
        DataOutputStream dataOutStream = new DataOutputStream(out);
        for (int index = 0; index < this.mStatsMap.size(); index++) {
            ((IOStatsHistory) this.mStatsMap.get(this.mStatsMap.keyAt(index))).writeToStream(dataOutStream);
        }
        dataOutStream.flush();
    }
}
