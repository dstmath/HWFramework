package huawei.android.widget;

import android.telephony.MSimTelephonyConstants;
import android.util.Log;
import android.widget.SectionIndexer;
import java.util.Arrays;

public class MySectionIndexer implements SectionIndexer {
    private int mCount;
    private int[] mPositions;
    private String[] mSections;

    public MySectionIndexer(String[] sections, int[] counts, boolean digitLast) {
        if (sections == null || counts == null) {
            throw new NullPointerException();
        } else if (sections.length != counts.length) {
            throw new IllegalArgumentException("The sections and counts arrays must have the same length");
        } else {
            int size = sections.length;
            this.mSections = sections;
            this.mPositions = new int[size];
            int position = 0;
            for (int i = 0; i < size; i++) {
                if (this.mSections[i] == null) {
                    this.mSections[i] = MSimTelephonyConstants.MY_RADIO_PLATFORM;
                } else {
                    this.mSections[i] = this.mSections[i].trim();
                }
                this.mPositions[i] = position;
                position += counts[i];
                Log.i("MySectionIndexer", this.mSections[i] + "  counts[" + i + "]:" + counts[i]);
            }
            this.mCount = position;
        }
    }

    public MySectionIndexer(String[] sections, int[] counts) {
        this(sections, counts, false);
    }

    public Object[] getSections() {
        return this.mSections;
    }

    public int getPositionForSection(int section) {
        if (section < 0 || section >= this.mSections.length) {
            return -1;
        }
        return this.mPositions[section];
    }

    public int getSectionForPosition(int position) {
        if (position < 0 || position >= this.mCount) {
            return -1;
        }
        int index = Arrays.binarySearch(this.mPositions, position);
        if (index < 0) {
            index = (-index) - 2;
        }
        return index;
    }
}
