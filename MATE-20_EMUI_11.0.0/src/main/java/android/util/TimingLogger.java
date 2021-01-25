package android.util;

import android.os.SystemClock;
import java.util.ArrayList;

public class TimingLogger {
    private boolean mDisabled;
    private String mLabel;
    ArrayList<String> mSplitLabels;
    ArrayList<Long> mSplits;
    private String mTag;

    public TimingLogger(String tag, String label) {
        reset(tag, label);
    }

    public void reset(String tag, String label) {
        this.mTag = tag;
        this.mLabel = label;
        reset();
    }

    public void reset() {
        this.mDisabled = !Log.isLoggable(this.mTag, 2);
        if (!this.mDisabled) {
            ArrayList<Long> arrayList = this.mSplits;
            if (arrayList == null) {
                this.mSplits = new ArrayList<>();
                this.mSplitLabels = new ArrayList<>();
            } else {
                arrayList.clear();
                this.mSplitLabels.clear();
            }
            addSplit(null);
        }
    }

    public void addSplit(String splitLabel) {
        if (!this.mDisabled) {
            this.mSplits.add(Long.valueOf(SystemClock.elapsedRealtime()));
            this.mSplitLabels.add(splitLabel);
        }
    }

    public void dumpToLog() {
        if (!this.mDisabled) {
            String str = this.mTag;
            Log.d(str, this.mLabel + ": begin");
            long first = this.mSplits.get(0).longValue();
            long now = first;
            for (int i = 1; i < this.mSplits.size(); i++) {
                now = this.mSplits.get(i).longValue();
                long prev = this.mSplits.get(i - 1).longValue();
                String str2 = this.mTag;
                Log.d(str2, this.mLabel + ":      " + (now - prev) + " ms, " + this.mSplitLabels.get(i));
            }
            String str3 = this.mTag;
            Log.d(str3, this.mLabel + ": end, " + (now - first) + " ms");
        }
    }
}
