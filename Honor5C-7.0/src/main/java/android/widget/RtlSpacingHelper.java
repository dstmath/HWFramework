package android.widget;

class RtlSpacingHelper {
    public static final int UNDEFINED = Integer.MIN_VALUE;
    private int mEnd;
    private int mExplicitLeft;
    private int mExplicitRight;
    private boolean mIsRelative;
    private boolean mIsRtl;
    private int mLeft;
    private int mRight;
    private int mStart;

    RtlSpacingHelper() {
        this.mLeft = 0;
        this.mRight = 0;
        this.mStart = UNDEFINED;
        this.mEnd = UNDEFINED;
        this.mExplicitLeft = 0;
        this.mExplicitRight = 0;
        this.mIsRtl = false;
        this.mIsRelative = false;
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
            if (end != UNDEFINED) {
                this.mLeft = end;
            }
            if (start != UNDEFINED) {
                this.mRight = start;
                return;
            }
            return;
        }
        if (start != UNDEFINED) {
            this.mLeft = start;
        }
        if (end != UNDEFINED) {
            this.mRight = end;
        }
    }

    public void setAbsolute(int left, int right) {
        this.mIsRelative = false;
        if (left != UNDEFINED) {
            this.mExplicitLeft = left;
            this.mLeft = left;
        }
        if (right != UNDEFINED) {
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
                int i;
                this.mLeft = this.mEnd != UNDEFINED ? this.mEnd : this.mExplicitLeft;
                if (this.mStart != UNDEFINED) {
                    i = this.mStart;
                } else {
                    i = this.mExplicitRight;
                }
                this.mRight = i;
            } else {
                this.mLeft = this.mStart != UNDEFINED ? this.mStart : this.mExplicitLeft;
                this.mRight = this.mEnd != UNDEFINED ? this.mEnd : this.mExplicitRight;
            }
        }
    }
}
