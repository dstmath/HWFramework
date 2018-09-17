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

public class LifeTimeCollection implements Reader {
    private static final String TAG = "RMS.IO.LifeTimeCollection";
    private List<LifeTimeData> mDataList = new ArrayList();

    static class LifeTimeRewriter implements Rewriter {
        private LifeTimeCollection mLifeTimeCollection = null;

        public LifeTimeRewriter(LifeTimeCollection collection) {
            this.mLifeTimeCollection = collection;
        }

        public void reset() {
        }

        public void read(InputStream in) throws IOException {
            this.mLifeTimeCollection.read(in);
        }

        public boolean shouldWrite() {
            return true;
        }

        public void write(OutputStream out) throws IOException {
            this.mLifeTimeCollection.write(out);
        }
    }

    public void addAll(List<LifeTimeData> dataList) {
        if (dataList == null) {
            Log.e(TAG, "addAll,the dataList is null");
        } else {
            this.mDataList.addAll(dataList);
        }
    }

    public void reset() {
        this.mDataList.clear();
    }

    public List<LifeTimeData> getAllDataList() {
        return this.mDataList;
    }

    public void read(InputStream in) throws IOException {
        if (in == null) {
            Log.e(TAG, "read,InputStream is null");
            return;
        }
        DataInputStream inputStream = new DataInputStream(in);
        List<LifeTimeData> currentList = new ArrayList();
        currentList.addAll(this.mDataList);
        this.mDataList.clear();
        while (inputStream.available() > 0) {
            this.mDataList.add(new LifeTimeData(inputStream.readLong(), inputStream.readInt()));
        }
        this.mDataList.addAll(currentList);
    }

    public void write(OutputStream out) throws IOException {
        if (out == null) {
            Log.e(TAG, "write,OutputStream is null");
            return;
        }
        long currentTime = System.currentTimeMillis();
        DataOutputStream dataOutStream = new DataOutputStream(out);
        for (LifeTimeData data : this.mDataList) {
            if (Utils.getDifferencesByDay(currentTime, data.mTime) <= ((long) IOExceptionHandle.IO_EXCEPTION_DAYS_LIMIT)) {
                dataOutStream.writeLong(data.mTime);
                dataOutStream.writeInt(data.mLifeTime);
            }
        }
        dataOutStream.flush();
    }
}
