package huawei.android.widget.columnsystem;

/* access modifiers changed from: package-private */
public class HwColumnPolicyImpl extends HwColumnPolicy {
    private static final int COLUMN_DOUBLE = 2;
    private static final float PIXEL_PRECISION_OFFSET = 0.5f;
    private static final String TAG = HwColumnPolicyImpl.class.getSimpleName();
    private int mMaxColumnWidth;
    private int mMinColumnWidth;
    private int mTotalColumnWidth;

    HwColumnPolicyImpl() {
    }

    @Override // huawei.android.widget.columnsystem.HwColumnPolicy
    public final int getColumnWidth() {
        return this.mMinColumnWidth;
    }

    @Override // huawei.android.widget.columnsystem.HwColumnPolicy
    public final float getColumnWidth(int columns) {
        return (((float) columns) * this.mColumnWidth) + ((float) ((columns - 1) * this.mGutter));
    }

    @Override // huawei.android.widget.columnsystem.HwColumnPolicy
    public int getMaxColumnWidth() {
        return this.mMaxColumnWidth;
    }

    @Override // huawei.android.widget.columnsystem.HwColumnPolicy
    public int getMinColumnWidth() {
        return this.mMinColumnWidth;
    }

    public int getTotalColumnWidth() {
        return this.mTotalColumnWidth;
    }

    @Override // huawei.android.widget.columnsystem.HwColumnPolicy
    public void onUpdateConfig() {
        this.mColumnWidth = ((float) ((this.mWidthPixel - (this.mMargin * 2)) - (this.mGutter * (this.mTotalColumn - 1)))) / (((float) this.mTotalColumn) * 1.0f);
        this.mTotalColumnWidth = (int) (getColumnWidth(this.mTotalColumn) + 0.5f);
        if (this.mMinColumn == -2) {
            this.mMinColumnWidth = this.mTotalColumnWidth + (this.mMargin * 2);
        } else {
            this.mMinColumnWidth = (int) (getColumnWidth(this.mMinColumn) + 0.5f);
        }
        if (this.mMaxColumn == -2) {
            this.mMaxColumnWidth = this.mTotalColumnWidth + (this.mMargin * 2);
        } else {
            this.mMaxColumnWidth = (int) (getColumnWidth(this.mMaxColumn) + 0.5f);
        }
    }
}
