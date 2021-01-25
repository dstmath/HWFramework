package com.android.internal.os;

import android.os.BatteryStats;
import android.os.Parcel;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ParseUtils;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public class BatteryStatsHistory {
    private static final boolean DEBUG = false;
    public static final String FILE_SUFFIX = ".bin";
    public static final String HISTORY_DIR = "battery-history";
    private static final int MIN_FREE_SPACE = 104857600;
    private static final String TAG = "BatteryStatsHistory";
    private AtomicFile mActiveFile;
    private int mCurrentFileIndex;
    private Parcel mCurrentParcel;
    private int mCurrentParcelEnd;
    private final List<Integer> mFileNumbers = new ArrayList();
    private final Parcel mHistoryBuffer;
    private final File mHistoryDir;
    private List<Parcel> mHistoryParcels = null;
    private int mParcelIndex = 0;
    private int mRecordCount = 0;
    private final BatteryStatsImpl mStats;

    public BatteryStatsHistory(BatteryStatsImpl stats, File systemDir, Parcel historyBuffer) {
        this.mStats = stats;
        this.mHistoryBuffer = historyBuffer;
        this.mHistoryDir = new File(systemDir, HISTORY_DIR);
        this.mHistoryDir.mkdirs();
        if (!this.mHistoryDir.exists()) {
            Slog.wtf(TAG, "HistoryDir does not exist:" + this.mHistoryDir.getPath());
        }
        final Set<Integer> dedup = new ArraySet<>();
        this.mHistoryDir.listFiles(new FilenameFilter() {
            /* class com.android.internal.os.BatteryStatsHistory.AnonymousClass1 */

            @Override // java.io.FilenameFilter
            public boolean accept(File dir, String name) {
                int b = name.lastIndexOf(BatteryStatsHistory.FILE_SUFFIX);
                if (b <= 0) {
                    return false;
                }
                Integer c = Integer.valueOf(ParseUtils.parseInt(name.substring(0, b), -1));
                if (c.intValue() == -1) {
                    return false;
                }
                dedup.add(c);
                return true;
            }
        });
        if (!dedup.isEmpty()) {
            this.mFileNumbers.addAll(dedup);
            Collections.sort(this.mFileNumbers);
            List<Integer> list = this.mFileNumbers;
            setActiveFile(list.get(list.size() - 1).intValue());
            return;
        }
        this.mFileNumbers.add(0);
        setActiveFile(0);
    }

    public BatteryStatsHistory(BatteryStatsImpl stats, Parcel historyBuffer) {
        this.mStats = stats;
        this.mHistoryDir = null;
        this.mHistoryBuffer = historyBuffer;
    }

    private void setActiveFile(int fileNumber) {
        this.mActiveFile = getFile(fileNumber);
    }

    private AtomicFile getFile(int num) {
        File file = this.mHistoryDir;
        return new AtomicFile(new File(file, num + FILE_SUFFIX));
    }

    public void startNextFile() {
        if (this.mFileNumbers.isEmpty()) {
            Slog.wtf(TAG, "mFileNumbers should never be empty");
            return;
        }
        List<Integer> list = this.mFileNumbers;
        int next = list.get(list.size() - 1).intValue() + 1;
        this.mFileNumbers.add(Integer.valueOf(next));
        setActiveFile(next);
        if (!hasFreeDiskSpace()) {
            getFile(this.mFileNumbers.remove(0).intValue()).delete();
        }
        while (this.mFileNumbers.size() > this.mStats.mConstants.MAX_HISTORY_FILES) {
            getFile(this.mFileNumbers.get(0).intValue()).delete();
            this.mFileNumbers.remove(0);
        }
    }

    public void resetAllFiles() {
        for (Integer i : this.mFileNumbers) {
            getFile(i.intValue()).delete();
        }
        this.mFileNumbers.clear();
        this.mFileNumbers.add(0);
        setActiveFile(0);
    }

    public boolean startIteratingHistory() {
        this.mRecordCount = 0;
        this.mCurrentFileIndex = 0;
        this.mCurrentParcel = null;
        this.mCurrentParcelEnd = 0;
        this.mParcelIndex = 0;
        return true;
    }

    public void finishIteratingHistory() {
        Parcel parcel = this.mHistoryBuffer;
        parcel.setDataPosition(parcel.dataSize());
    }

    public Parcel getNextParcel(BatteryStats.HistoryItem out) {
        if (this.mRecordCount == 0) {
            out.clear();
        }
        this.mRecordCount++;
        Parcel parcel = this.mCurrentParcel;
        if (parcel != null) {
            if (parcel.dataPosition() < this.mCurrentParcelEnd) {
                return this.mCurrentParcel;
            }
            Parcel parcel2 = this.mHistoryBuffer;
            Parcel parcel3 = this.mCurrentParcel;
            if (parcel2 == parcel3) {
                return null;
            }
            List<Parcel> list = this.mHistoryParcels;
            if (list == null || !list.contains(parcel3)) {
                this.mCurrentParcel.recycle();
            }
        }
        while (this.mCurrentFileIndex < this.mFileNumbers.size() - 1) {
            this.mCurrentParcel = null;
            this.mCurrentParcelEnd = 0;
            Parcel p = Parcel.obtain();
            List<Integer> list2 = this.mFileNumbers;
            int i = this.mCurrentFileIndex;
            this.mCurrentFileIndex = i + 1;
            if (readFileToParcel(p, getFile(list2.get(i).intValue()))) {
                int bufSize = p.readInt();
                int curPos = p.dataPosition();
                this.mCurrentParcelEnd = curPos + bufSize;
                this.mCurrentParcel = p;
                if (curPos < this.mCurrentParcelEnd) {
                    return this.mCurrentParcel;
                }
            } else {
                p.recycle();
            }
        }
        if (this.mHistoryParcels != null) {
            while (this.mParcelIndex < this.mHistoryParcels.size()) {
                List<Parcel> list3 = this.mHistoryParcels;
                int i2 = this.mParcelIndex;
                this.mParcelIndex = i2 + 1;
                Parcel p2 = list3.get(i2);
                if (skipHead(p2)) {
                    int bufSize2 = p2.readInt();
                    int curPos2 = p2.dataPosition();
                    this.mCurrentParcelEnd = curPos2 + bufSize2;
                    this.mCurrentParcel = p2;
                    if (curPos2 < this.mCurrentParcelEnd) {
                        return this.mCurrentParcel;
                    }
                }
            }
        }
        if (this.mHistoryBuffer.dataSize() <= 0) {
            return null;
        }
        this.mHistoryBuffer.setDataPosition(0);
        this.mCurrentParcel = this.mHistoryBuffer;
        this.mCurrentParcelEnd = this.mCurrentParcel.dataSize();
        return this.mCurrentParcel;
    }

    public boolean readFileToParcel(Parcel out, AtomicFile file) {
        try {
            SystemClock.uptimeMillis();
            byte[] raw = file.readFully();
            out.unmarshall(raw, 0, raw.length);
            out.setDataPosition(0);
            return skipHead(out);
        } catch (Exception e) {
            Slog.e(TAG, "Error reading file " + file.getBaseFile().getPath(), e);
            return false;
        }
    }

    private boolean skipHead(Parcel p) {
        p.setDataPosition(0);
        int version = p.readInt();
        BatteryStatsImpl batteryStatsImpl = this.mStats;
        if (version != 186) {
            return false;
        }
        p.readLong();
        return true;
    }

    public void writeToParcel(Parcel out) {
        SystemClock.uptimeMillis();
        out.writeInt(this.mFileNumbers.size() - 1);
        for (int i = 0; i < this.mFileNumbers.size() - 1; i++) {
            AtomicFile file = getFile(this.mFileNumbers.get(i).intValue());
            byte[] raw = new byte[0];
            try {
                raw = file.readFully();
            } catch (Exception e) {
                Slog.e(TAG, "Error reading file " + file.getBaseFile().getPath(), e);
            }
            out.writeByteArray(raw);
        }
    }

    public void readFromParcel(Parcel in) {
        SystemClock.uptimeMillis();
        this.mHistoryParcels = new ArrayList();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            byte[] temp = in.createByteArray();
            if (temp.length != 0) {
                Parcel p = Parcel.obtain();
                p.unmarshall(temp, 0, temp.length);
                p.setDataPosition(0);
                this.mHistoryParcels.add(p);
            }
        }
    }

    private boolean hasFreeDiskSpace() {
        return new StatFs(this.mHistoryDir.getAbsolutePath()).getAvailableBytes() > 104857600;
    }

    public List<Integer> getFilesNumbers() {
        return this.mFileNumbers;
    }

    public AtomicFile getActiveFile() {
        return this.mActiveFile;
    }

    public int getHistoryUsedSize() {
        int ret = 0;
        for (int i = 0; i < this.mFileNumbers.size() - 1; i++) {
            ret = (int) (((long) ret) + getFile(this.mFileNumbers.get(i).intValue()).getBaseFile().length());
        }
        int ret2 = ret + this.mHistoryBuffer.dataSize();
        if (this.mHistoryParcels != null) {
            for (int i2 = 0; i2 < this.mHistoryParcels.size(); i2++) {
                ret2 += this.mHistoryParcels.get(i2).dataSize();
            }
        }
        return ret2;
    }
}
