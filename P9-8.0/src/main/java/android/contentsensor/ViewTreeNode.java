package android.contentsensor;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class ViewTreeNode implements Parcelable {
    public static final Creator<ViewTreeNode> CREATOR = new Creator<ViewTreeNode>() {
        public ViewTreeNode createFromParcel(Parcel source) {
            ViewTreeNode nodeGroup = new ViewTreeNode(source);
            int N = source.readInt();
            for (int i = 0; i < N; i++) {
                nodeGroup.setChildViewInList((ViewTreeNode) ViewTreeNode.CREATOR.createFromParcel(source));
            }
            return nodeGroup;
        }

        public ViewTreeNode[] newArray(int size) {
            return new ViewTreeNode[size];
        }
    };
    private static final String TAG = ViewTreeNode.class.getSimpleName();
    private static final String VIEW = "android.view.View";
    private int mBottom;
    private List<ViewTreeNode> mChildView;
    private transient Class mClass;
    private List<String> mClassPath;
    private int mIndex;
    private boolean mIsFocused;
    private int mLeft;
    private transient ViewTreeNode mParent;
    private int mRight;
    private int mScrollX;
    private int mScrollY;
    private int mTop;
    private int mViewHashCode;
    private String mViewId;
    private String mViewText;

    public ViewTreeNode() {
        this.mClassPath = new ArrayList();
        this.mChildView = new ArrayList();
    }

    private void convertClassToPath() {
        Class parent = this.mClass;
        while (parent != null) {
            String curName = parent.getName();
            if (!VIEW.equals(curName)) {
                this.mClassPath.add(curName);
                parent = parent.getSuperclass();
            } else {
                return;
            }
        }
    }

    public void setClass(Class mClass) {
        this.mClass = mClass;
    }

    public List<String> getClassPath() {
        return this.mClassPath;
    }

    public void setDimens(int left, int top, int right, int bottom, int scrollX, int scrollY) {
        this.mLeft = left;
        this.mTop = top;
        this.mRight = right;
        this.mBottom = bottom;
        this.mScrollX = scrollX;
        this.mScrollY = scrollY;
    }

    public int getLeft() {
        return this.mLeft;
    }

    public int getTop() {
        return this.mTop;
    }

    public int getRight() {
        return this.mRight;
    }

    public int getBottom() {
        return this.mBottom;
    }

    public int getScrollX() {
        return this.mScrollX;
    }

    public int getScrollY() {
        return this.mScrollY;
    }

    public boolean isFocused() {
        return this.mIsFocused;
    }

    public void setFocused(boolean mIsFocused) {
        this.mIsFocused = mIsFocused;
    }

    public String getViewId() {
        return this.mViewId;
    }

    public void setViewId(String mViewId) {
        this.mViewId = mViewId;
    }

    public int getIndex() {
        return this.mIndex;
    }

    public void setIndex(int mIndex) {
        this.mIndex = mIndex;
    }

    public void setParent(ViewTreeNode parent) {
        this.mParent = parent;
    }

    public ViewTreeNode getParent() {
        return this.mParent;
    }

    public int getViewHashCode() {
        return this.mViewHashCode;
    }

    public void setViewHashCode(int mViewHashCode) {
        this.mViewHashCode = mViewHashCode;
    }

    public String getViewText() {
        return this.mViewText;
    }

    public void setViewText(String mViewText) {
        this.mViewText = mViewText;
    }

    public ViewTreeNode getChildAt(int i) {
        if (this.mChildView == null || i >= this.mChildView.size()) {
            return new ViewTreeNode();
        }
        return (ViewTreeNode) this.mChildView.get(i);
    }

    public void setChildViewInList(ViewTreeNode childView) {
        this.mChildView.add(childView);
    }

    public int getChildCount() {
        if (this.mChildView != null) {
            return this.mChildView.size();
        }
        return 0;
    }

    public int describeContents() {
        return 0;
    }

    public ViewTreeNode(Parcel pl) {
        this.mClassPath = new ArrayList();
        this.mLeft = pl.readInt();
        this.mTop = pl.readInt();
        this.mRight = pl.readInt();
        this.mBottom = pl.readInt();
        this.mScrollX = pl.readInt();
        this.mScrollY = pl.readInt();
        this.mIsFocused = pl.readBoolean();
        this.mViewId = pl.readString();
        this.mIndex = pl.readInt();
        this.mViewHashCode = pl.readInt();
        this.mViewText = pl.readString();
        this.mClassPath = pl.createStringArrayList();
        this.mChildView = new ArrayList();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mLeft);
        dest.writeInt(this.mTop);
        dest.writeInt(this.mRight);
        dest.writeInt(this.mBottom);
        dest.writeInt(this.mScrollX);
        dest.writeInt(this.mScrollY);
        dest.writeBoolean(this.mIsFocused);
        dest.writeString(this.mViewId);
        dest.writeInt(this.mIndex);
        dest.writeInt(this.mViewHashCode);
        dest.writeString(this.mViewText);
        convertClassToPath();
        dest.writeStringList(this.mClassPath);
        int N = this.mChildView.size();
        dest.writeInt(N);
        for (int i = 0; i < N; i++) {
            getChildAt(i).writeToParcel(dest, flags);
        }
    }
}
