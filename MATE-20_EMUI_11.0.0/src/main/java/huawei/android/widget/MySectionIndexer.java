package huawei.android.widget;

import android.util.Log;
import android.widget.SectionIndexer;
import java.util.Arrays;

public class MySectionIndexer implements SectionIndexer {
    private static final int ENSURE_NEGATIVE_VALUES = 2;
    private int mCount;
    private int[] mPositions;
    private String[] mSections;

    public MySectionIndexer(String[] sections, int[] counts, boolean isDigitLast) {
        if (sections == null || counts == null) {
            throw new NullPointerException();
        } else if (sections.length == counts.length) {
            int size = sections.length;
            this.mSections = new String[size];
            System.arraycopy(sections, 0, this.mSections, 0, size);
            this.mPositions = new int[size];
            int position = 0;
            for (int i = 0; i < size; i++) {
                String[] strArr = this.mSections;
                if (strArr[i] == null) {
                    strArr[i] = "";
                } else {
                    strArr[i] = strArr[i].trim();
                }
                this.mPositions[i] = position;
                position += counts[i];
                Log.i("MySectionIndexer", this.mSections[i] + "  counts[" + i + "]:" + counts[i]);
            }
            this.mCount = position;
        } else {
            throw new IllegalArgumentException("The sections and counts arrays must have the same length");
        }
    }

    public MySectionIndexer(String[] sections, int[] counts) {
        this(sections, counts, false);
    }

    @Override // android.widget.SectionIndexer
    public Object[] getSections() {
        String[] strArr = this.mSections;
        if (strArr == null || strArr.length == 0) {
            return new String[0];
        }
        int size = strArr.length;
        String[] sections = new String[size];
        System.arraycopy(strArr, 0, sections, 0, size);
        return sections;
    }

    @Override // android.widget.SectionIndexer
    public int getPositionForSection(int section) {
        if (section < 0 || section >= this.mSections.length) {
            return -1;
        }
        return this.mPositions[section];
    }

    @Override // android.widget.SectionIndexer
    public int getSectionForPosition(int position) {
        if (position < 0 || position >= this.mCount) {
            return -1;
        }
        int index = Arrays.binarySearch(this.mPositions, position);
        return index >= 0 ? index : (-index) - 2;
    }
}
