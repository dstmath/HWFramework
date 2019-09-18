package com.android.server.rms.io;

import android.rms.utils.Utils;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.rms.io.IOFileRotator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public final class IOStatsCollection implements IOFileRotator.Reader, Cloneable {
    private static final String TAG = "RMS.IO.IOStatsCollection";
    private SparseArray<IOStatsHistory> mStatsMap = new SparseArray<>();

    public int getTotalBytes() {
        int totalBytes = 0;
        int statsMapSize = this.mStatsMap.size();
        for (int index = 0; index < statsMapSize; index++) {
            totalBytes = (int) (((long) totalBytes) + this.mStatsMap.get(this.mStatsMap.keyAt(index)).getTotalBytesNum());
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
        int statsMapSize = this.mStatsMap.size();
        for (int index = 0; index < statsMapSize; index++) {
            int uid = this.mStatsMap.keyAt(index);
            IOStatsHistory history = this.mStatsMap.get(uid);
            if (history == null) {
                Log.e(TAG, "uid:" + uid + "'s history is empty");
            } else {
                history.dump(pw);
            }
        }
        if (Utils.DEBUG != 0) {
            Log.d(TAG, "end dump IOStatsCollection");
        }
    }

    public void addHistories(IOStatsCollection fromCollection) {
        if (fromCollection == null || fromCollection.getIOStatsHistoryMap().size() == 0) {
            Log.e(TAG, "addHistories,the fromCollection is empty");
            return;
        }
        SparseArray<IOStatsHistory> fromHistoryMap = fromCollection.getIOStatsHistoryMap();
        int historyMapSize = fromHistoryMap.size();
        for (int index = 0; index < historyMapSize; index++) {
            int uid = fromHistoryMap.keyAt(index);
            IOStatsHistory history = this.mStatsMap.get(uid);
            IOStatsHistory addHistory = fromHistoryMap.get(uid);
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
            clone.mStatsMap = new SparseArray<>();
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
        IOStatsHistory historyFound = this.mStatsMap.get(uid);
        if (historyFound == null) {
            this.mStatsMap.put(uid, history.clone());
        } else {
            historyFound.addAllEntries(history);
        }
    }

    public void read(InputStream in) throws IOException {
        IOStatsHistory history;
        InputStream inputStream = in;
        if (inputStream == null) {
            Log.e(TAG, "read,InputStream is null");
            return;
        }
        DataInputStream inputStream2 = new DataInputStream(inputStream);
        SparseArray<IOStatsHistory> current = this.mStatsMap.clone();
        this.mStatsMap.clear();
        while (inputStream2.available() >= 28) {
            int uid = inputStream2.readInt();
            String packageName = inputStream2.readUTF();
            long startTime = inputStream2.readLong();
            long numberOfWrite = inputStream2.readLong();
            long numberOfRead = inputStream2.readLong();
            if (this.mStatsMap.indexOfKey(uid) >= 0) {
                history = this.mStatsMap.get(uid);
            } else {
                history = new IOStatsHistory(uid, packageName);
                this.mStatsMap.put(uid, history);
            }
            history.addEntry(startTime, numberOfRead, numberOfWrite);
        }
        int curSize = current.size();
        for (int index = 0; index < curSize; index++) {
            int uid2 = current.keyAt(index);
            IOStatsHistory history2 = current.get(uid2);
            if (this.mStatsMap.indexOfKey(uid2) >= 0) {
                this.mStatsMap.get(uid2).addAllEntries(history2);
            } else {
                this.mStatsMap.put(uid2, history2);
            }
        }
    }

    public void write(OutputStream out) throws IOException {
        if (out == null) {
            Log.e(TAG, "write,OutputStream is null");
            return;
        }
        DataOutputStream dataOutStream = new DataOutputStream(out);
        int statsMapSize = this.mStatsMap.size();
        for (int index = 0; index < statsMapSize; index++) {
            this.mStatsMap.get(this.mStatsMap.keyAt(index)).writeToStream(dataOutStream);
        }
        dataOutStream.flush();
    }
}
