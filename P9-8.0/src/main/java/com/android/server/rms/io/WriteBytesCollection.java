package com.android.server.rms.io;

import android.rms.utils.Utils;
import android.util.Log;
import com.android.server.rms.io.IOFileRotator.Reader;
import com.android.server.rms.io.IOFileRotator.Rewriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class WriteBytesCollection implements Reader {
    private static final String TAG = "RMS.IO.LifeTimeCollection";
    private List<WriteBytesData> mDataList = new ArrayList();

    static class TotalWrittenRewriter implements Rewriter {
        private WriteBytesCollection mWriteBytesCollection = null;

        public TotalWrittenRewriter(WriteBytesCollection collection) {
            this.mWriteBytesCollection = collection;
        }

        public void reset() {
        }

        public void read(InputStream in) throws IOException {
            this.mWriteBytesCollection.read(in);
        }

        public boolean shouldWrite() {
            return true;
        }

        public void write(OutputStream out) throws IOException {
            this.mWriteBytesCollection.write(out);
        }
    }

    public void addAll(List<WriteBytesData> dataList) {
        if (dataList == null) {
            Log.e(TAG, "addAll,the dataList is null");
        } else {
            this.mDataList.addAll(dataList);
        }
    }

    public void reset() {
        this.mDataList.clear();
    }

    public List<WriteBytesData> getAllDataList() {
        return this.mDataList;
    }

    public void read(InputStream in) throws IOException {
        if (in == null) {
            Log.e(TAG, "read,InputStream is null");
            return;
        }
        DataInputStream inputStream = new DataInputStream(in);
        List<WriteBytesData> currentList = new ArrayList();
        currentList.addAll(this.mDataList);
        this.mDataList.clear();
        while (inputStream.available() > 0) {
            this.mDataList.add(new WriteBytesData(inputStream.readLong(), inputStream.readLong()));
        }
        this.mDataList.addAll(currentList);
    }

    public void write(OutputStream out) throws IOException {
        if (out == null) {
            Log.e(TAG, "write,OutputStream is null");
            return;
        }
        DataOutputStream dataOutStream = new DataOutputStream(out);
        long currentTime = System.currentTimeMillis();
        for (WriteBytesData data : this.mDataList) {
            if (Utils.getDifferencesByDay(currentTime, data.mTime) <= ((long) IOExceptionHandle.IO_EXCEPTION_DAYS_LIMIT)) {
                dataOutStream.writeLong(data.mTime);
                dataOutStream.writeLong(data.mWriteBytes);
            }
        }
        dataOutStream.flush();
    }
}
