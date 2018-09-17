package huawei.android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;

public class DragListView extends ListView {
    private static final int SLIDE = 1;
    private static final String TAG = "DragListView";
    private Bitmap mDragBitmap;
    private DragListViewAdapter mDragListViewAdapter;
    private int mDragOffset;
    private int mDragPoint;
    private int mDragPos;
    private ImageView mDragView;
    private DropListener mDropListener;
    private boolean mDropProcessing;
    private int mFirstDragPos;
    private int mHeight;
    private int mItemHeightExpanded;
    private int mItemHeightHalf;
    private int mItemHeightNormal;
    private final int mLeftTouchTolerance;
    private int mLowerBound;
    private int mRemoveMode;
    private final int mRightTouchTolerance;
    private Rect mTempRect;
    private final int mTouchSlop;
    private int mUpperBound;
    private WindowManager mWindowManager;
    private LayoutParams mWindowParams;

    public interface DropListener {
        void drop(int i, int i2);

        void onDragEnd();

        void onDragStart(int i);
    }

    public DragListView(Context context) {
        this(context, null);
    }

    public DragListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842868);
    }

    public DragListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DragListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mRemoveMode = 0;
        this.mTempRect = new Rect();
        this.mDropProcessing = false;
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mRightTouchTolerance = Float.valueOf(getResources().getDimension(34472084)).intValue();
        this.mLeftTouchTolerance = Float.valueOf(getResources().getDimension(34472085)).intValue();
        this.mItemHeightNormal = Float.valueOf(getResources().getDimension(34472083)).intValue();
        this.mItemHeightHalf = this.mItemHeightNormal / 2;
        this.mItemHeightExpanded = this.mItemHeightNormal * 2;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.mDropListener != null && ev.getAction() == 0) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            int itemnum = pointToPosition(x, y);
            if (itemnum == -1) {
                return super.onInterceptTouchEvent(ev);
            }
            if (this.mDragListViewAdapter.getFlagCanNotDrag(itemnum)) {
                return super.onInterceptTouchEvent(ev);
            }
            View item = getChildAt(itemnum - getFirstVisiblePosition());
            this.mDragPoint = y - item.getTop();
            this.mDragOffset = ((int) ev.getRawY()) - y;
            View dragger = item.findViewById(34603119);
            boolean isDragHandle = !isRtlLocale() ? x - getPaddingStart() >= dragger.getLeft() - this.mLeftTouchTolerance ? x - getPaddingStart() < dragger.getRight() + this.mRightTouchTolerance : false : x - getPaddingEnd() >= ((ViewGroup) dragger.getParent()).getLeft() - this.mLeftTouchTolerance ? x - getPaddingEnd() < ((ViewGroup) dragger.getParent()).getRight() + this.mRightTouchTolerance : false;
            if (isDragHandle) {
                item.setDrawingCacheEnabled(true);
                item.setBackgroundColor(Color.parseColor("#e8f3ff"));
                Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());
                item.destroyDrawingCache();
                item.setBackgroundColor(0);
                startDragging(bitmap, y);
                this.mDragPos = itemnum;
                this.mFirstDragPos = this.mDragPos;
                if (this.mDropListener != null) {
                    this.mDropListener.onDragStart(this.mFirstDragPos);
                }
                this.mHeight = getHeight();
                this.mUpperBound = y - this.mTouchSlop < this.mHeight / 3 ? y - this.mTouchSlop : this.mHeight / 3;
                this.mLowerBound = this.mTouchSlop + y > (this.mHeight * 2) / 3 ? this.mTouchSlop + y : (this.mHeight * 2) / 3;
                return false;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    private int myPointToPosition(int y) {
        if (y < 0) {
            int pos = myPointToPosition(this.mItemHeightNormal + y);
            if (pos > 0) {
                return pos - 1;
            }
        }
        Rect frame = this.mTempRect;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).getHitRect(frame);
            if (frame.contains(frame.left, y)) {
                return getFirstVisiblePosition() + i;
            }
        }
        return -1;
    }

    private int getItemForPosition(int y) {
        int adjustedy = (y - this.mDragPoint) - this.mItemHeightHalf;
        int pos = myPointToPosition(adjustedy);
        if (pos >= 0) {
            if (pos <= this.mFirstDragPos) {
                return pos + 1;
            }
            return pos;
        } else if (adjustedy < 0) {
            return 0;
        } else {
            return pos;
        }
    }

    private void adjustScrollBounds(int y) {
        if (y >= this.mHeight / 3) {
            this.mUpperBound = this.mHeight / 3;
        }
        if (y <= (this.mHeight * 2) / 3) {
            this.mLowerBound = (this.mHeight * 2) / 3;
        }
    }

    private void unExpandViews(boolean deletion) {
        int i = 0;
        while (true) {
            View v = getChildAt(i);
            if (v == null) {
                if (deletion) {
                    int position = getFirstVisiblePosition();
                    int y = getChildAt(0).getTop();
                    setAdapter(getAdapter());
                    setSelectionFromTop(position, y);
                }
                layoutChildren();
                v = getChildAt(i);
                if (v == null) {
                    return;
                }
            }
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = this.mItemHeightNormal;
            v.setLayoutParams(params);
            v.setVisibility(0);
            i++;
        }
    }

    private void doExpansion() {
        int childnum = this.mDragPos - getFirstVisiblePosition();
        if (this.mDragPos > this.mFirstDragPos) {
            childnum++;
        }
        View first = getChildAt(this.mFirstDragPos - getFirstVisiblePosition());
        int i = 0;
        while (true) {
            View vv = getChildAt(i);
            if (vv != null) {
                int height = this.mItemHeightNormal;
                int visibility = 0;
                if (vv == first) {
                    if (this.mDragPos == this.mFirstDragPos || getPositionForView(vv) == getCount() - 1) {
                        visibility = 4;
                    } else {
                        height = 1;
                    }
                } else if (i == childnum && this.mDragPos < getCount() - 1) {
                    height = this.mItemHeightExpanded;
                }
                ViewGroup.LayoutParams params = vv.getLayoutParams();
                params.height = height;
                vv.setLayoutParams(params);
                vv.setVisibility(visibility);
                i++;
            } else {
                return;
            }
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (this.mDropListener == null || this.mDragView == null) {
            return super.onTouchEvent(ev);
        }
        int action = ev.getAction();
        switch (action) {
            case 0:
            case 2:
                int y = (int) ev.getY();
                dragView((int) ev.getX(), y);
                int itemnum = getItemForPosition(y);
                if (!(itemnum == -1 || this.mDragListViewAdapter.getFlagCanNotDrag(itemnum) || itemnum < 0)) {
                    if (action == 0 || itemnum != this.mDragPos) {
                        this.mDragPos = itemnum;
                        doExpansion();
                    }
                    int speed = 0;
                    adjustScrollBounds(y);
                    if (y > this.mLowerBound) {
                        speed = getLastVisiblePosition() < getCount() + -1 ? y > (this.mHeight + this.mLowerBound) / 2 ? 16 : 4 : 1;
                    } else if (y < this.mUpperBound) {
                        speed = y < this.mUpperBound / 2 ? -16 : -4;
                        if (getFirstVisiblePosition() == 0 && getChildAt(0).getTop() >= getPaddingTop()) {
                            speed = 0;
                        }
                    }
                    if (speed != 0) {
                        smoothScrollBy(speed, 30);
                        break;
                    }
                }
                break;
            case 1:
            case 3:
                this.mDragView.setVisibility(4);
                stopDragging();
                if (this.mDragPos >= 0 && this.mDragPos < getCount() && (this.mDropProcessing ^ 1) != 0) {
                    this.mDropProcessing = true;
                    this.mDropListener.drop(this.mFirstDragPos, this.mDragPos);
                    this.mDropProcessing = false;
                }
                if (this.mDropListener != null) {
                    this.mDropListener.onDragEnd();
                }
                unExpandViews(false);
                break;
        }
        return true;
    }

    private void startDragging(Bitmap bm, int y) {
        stopDragging();
        this.mWindowParams = new LayoutParams();
        this.mWindowParams.gravity = 48;
        this.mWindowParams.x = 0;
        this.mWindowParams.y = this.mDragOffset > (y - this.mDragPoint) + this.mDragOffset ? this.mDragOffset : (y - this.mDragPoint) + this.mDragOffset;
        this.mWindowParams.height = -2;
        this.mWindowParams.width = -1;
        this.mWindowParams.flags = 920;
        this.mWindowParams.format = -3;
        this.mWindowParams.windowAnimations = 0;
        Context context = getContext();
        ImageView v = new ImageView(context);
        v.setImageBitmap(bm);
        this.mDragBitmap = bm;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mWindowManager.addView(v, this.mWindowParams);
        this.mDragView = v;
    }

    private void dragView(int x, int y) {
        if (this.mRemoveMode == 1) {
            float alpha = 1.0f;
            int width = this.mDragView.getWidth();
            if (x > width / 2) {
                alpha = Integer.valueOf(width - x).floatValue() / Integer.valueOf(width / 2).floatValue();
            }
            this.mWindowParams.alpha = alpha;
        }
        this.mWindowParams.y = this.mDragOffset > (y - this.mDragPoint) + this.mDragOffset ? this.mDragOffset : (y - this.mDragPoint) + this.mDragOffset;
        this.mWindowManager.updateViewLayout(this.mDragView, this.mWindowParams);
    }

    private void stopDragging() {
        if (this.mDragView != null) {
            ((WindowManager) getContext().getSystemService("window")).removeView(this.mDragView);
            this.mDragView.setImageDrawable(null);
            this.mDragView = null;
        }
        if (this.mDragBitmap != null) {
            this.mDragBitmap.recycle();
            this.mDragBitmap = null;
        }
    }

    public void setDropListener(DropListener l) {
        this.mDropListener = l;
    }

    public void setDragListViewAdapter(DragListViewAdapter dla) {
        if (dla != null) {
            this.mDragListViewAdapter = dla;
        } else {
            Log.e("TAG", "setDragListAdapter error, mDragListAdapter == null");
        }
    }
}
