package huawei.android.widget.columnsystem;

/* access modifiers changed from: package-private */
public class HwColumnPolicyImpl extends HwColumnPolicy {
    private static final int BREAKPOINT_520DP_COLUMN = 8;
    private static final int BREAKPOINT_840DP_COLUMN = 12;
    private static final int COLUMN_DOUBLE = 2;
    private static final int DEFAULT_COLUMN = 4;
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
            this.mMinColumnWidth = (int) (getColumnWidth(this.mMinColumn) + ((float) getAdditionalWidth(true)) + 0.5f);
        }
        if (this.mMaxColumn == -2) {
            this.mMaxColumnWidth = this.mTotalColumnWidth + (this.mMargin * 2);
        } else {
            this.mMaxColumnWidth = (int) (getColumnWidth(this.mMaxColumn) + ((float) getAdditionalWidth(false)) + 0.5f);
        }
    }

    private int getAdditionalWidth(boolean isMinWidth) {
        int i = this.mColumnType;
        if (i == 1 || i == 17) {
            if (this.mTotalColumn != 4 || !isMinWidth) {
                return 0;
            }
            return this.mGutter * 2;
        } else if (i != 19) {
            return 0;
        } else {
            if (this.mTotalColumn == 8 || this.mTotalColumn == 12) {
                return this.mGutter * 2;
            }
            return 0;
        }
    }
}
