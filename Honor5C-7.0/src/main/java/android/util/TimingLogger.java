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
            if (this.mSplits == null) {
                this.mSplits = new ArrayList();
                this.mSplitLabels = new ArrayList();
            } else {
                this.mSplits.clear();
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
            Log.d(this.mTag, this.mLabel + ": begin");
            long first = ((Long) this.mSplits.get(0)).longValue();
            long now = first;
            for (int i = 1; i < this.mSplits.size(); i++) {
                now = ((Long) this.mSplits.get(i)).longValue();
                Log.d(this.mTag, this.mLabel + ":      " + (now - ((Long) this.mSplits.get(i - 1)).longValue()) + " ms, " + ((String) this.mSplitLabels.get(i)));
            }
            Log.d(this.mTag, this.mLabel + ": end, " + (now - first) + " ms");
        }
    }
}
