package android.widget;

class RtlSpacingHelper {
    public static final int UNDEFINED = Integer.MIN_VALUE;
    private int mEnd = Integer.MIN_VALUE;
    private int mExplicitLeft = 0;
    private int mExplicitRight = 0;
    private boolean mIsRelative = false;
    private boolean mIsRtl = false;
    private int mLeft = 0;
    private int mRight = 0;
    private int mStart = Integer.MIN_VALUE;

    RtlSpacingHelper() {
    }

    public int getLeft() {
        return this.mLeft;
    }

    public int getRight() {
        return this.mRight;
    }

    public int getStart() {
        return this.mIsRtl ? this.mRight : this.mLeft;
    }

    public int getEnd() {
        return this.mIsRtl ? this.mLeft : this.mRight;
    }

    public void setRelative(int start, int end) {
        this.mStart = start;
        this.mEnd = end;
        this.mIsRelative = true;
        if (this.mIsRtl) {
            if (end != Integer.MIN_VALUE) {
                this.mLeft = end;
            }
            if (start != Integer.MIN_VALUE) {
                this.mRight = start;
                return;
            }
            return;
        }
        if (start != Integer.MIN_VALUE) {
            this.mLeft = start;
        }
        if (end != Integer.MIN_VALUE) {
            this.mRight = end;
        }
    }

    public void setAbsolute(int left, int right) {
        this.mIsRelative = false;
        if (left != Integer.MIN_VALUE) {
            this.mExplicitLeft = left;
            this.mLeft = left;
        }
        if (right != Integer.MIN_VALUE) {
            this.mExplicitRight = right;
            this.mRight = right;
        }
    }

    public void setDirection(boolean isRtl) {
        if (isRtl != this.mIsRtl) {
            this.mIsRtl = isRtl;
            if (!this.mIsRelative) {
                this.mLeft = this.mExplicitLeft;
                this.mRight = this.mExplicitRight;
            } else if (isRtl) {
                this.mLeft = this.mEnd != Integer.MIN_VALUE ? this.mEnd : this.mExplicitLeft;
                this.mRight = this.mStart != Integer.MIN_VALUE ? this.mStart : this.mExplicitRight;
            } else {
                this.mLeft = this.mStart != Integer.MIN_VALUE ? this.mStart : this.mExplicitLeft;
                this.mRight = this.mEnd != Integer.MIN_VALUE ? this.mEnd : this.mExplicitRight;
            }
        }
    }
}
