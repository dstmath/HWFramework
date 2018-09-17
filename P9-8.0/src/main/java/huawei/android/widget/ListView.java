package huawei.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.AbsListView.LayoutParams;
import android.widget.CursorAdapter;
import android.widget.HwAbsListView.AdapterDataSetObserver;
import android.widget.ListAdapter;
import java.util.HashMap;
import java.util.Map.Entry;

public class ListView extends android.widget.ListView {
    private static final int DEL_ITEM_NUM_ABOVEANDIN_SCREEN = 3;
    private static final int DEL_ITEM_NUM_ABOVE_SCREEN = 0;
    private static final int DEL_ITEM_NUM_BELOW_SCREEN = 2;
    private static final int DEL_ITEM_NUM_IN_SCREEN = 1;
    private int mBackupFirstVisiblePosition;
    private int mBackupLastVisiblePosition;
    private int mBaclupnumAboveScreen;

    public ListView(Context context) {
        this(context, null);
    }

    public ListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842868);
    }

    public ListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mAnimationViewIndexMap = new HashMap();
        this.mBackupAdapterObjectMap = new HashMap();
    }

    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        fillBackupAdapterObjectMap();
        this.mBackupAdapter = adapter;
    }

    public void addHeaderView(View v, Object data, boolean isSelectable) {
        super.addHeaderView(v, data, isSelectable);
        fillBackupAdapterObjectMap();
    }

    public void addFooterView(View v, Object data, boolean isSelectable) {
        super.addFooterView(v, data, isSelectable);
        fillBackupAdapterObjectMap();
    }

    public void setItemDeleteAnimation(boolean enable) {
        this.mListAnimationSwitch = false;
    }

    public boolean getItemDeleteAnimation() {
        return this.mListAnimationSwitch;
    }

    protected void fillBackupAdapterObjectMap() {
        if (this.mAdapter != null && (getItemDeleteAnimation() ^ 1) == 0) {
            this.mBackupItemCount = this.mAdapter.getCount();
            this.mBackupAdapterObjectMap.clear();
            int i;
            if (this.mBackupAdapter instanceof CursorAdapter) {
                for (i = 0; i < this.mBackupItemCount; i++) {
                    this.mBackupAdapterObjectMap.put(Long.valueOf(this.mAdapter.getItemId(i)), Integer.valueOf(i));
                }
            } else {
                for (i = 0; i < this.mBackupItemCount; i++) {
                    this.mBackupAdapterObjectMap.put(this.mAdapter.getItem(i), Integer.valueOf(i));
                }
            }
        }
    }

    protected int calculatePos(int position) {
        if (this.mAnimationViewIndexMap.size() != 0) {
            if (position > this.mBackupLastVisiblePosition) {
                return position - getScrapViewNum(3);
            }
            if (position < this.mBackupFirstVisiblePosition) {
                return position - getScrapViewNum(0);
            }
        }
        return position;
    }

    protected int calculateMaxPos() {
        return super.calculateMaxPos() + getScrapViewNum(3);
    }

    protected int calculateMinPos() {
        return getScrapViewNum(0);
    }

    protected int calculateSyncPosition(int pos) {
        if (!getItemDeleteAnimation()) {
            return pos;
        }
        int temp = this.mBaclupnumAboveScreen;
        this.mBaclupnumAboveScreen = 0;
        if (pos > temp) {
            return pos - temp;
        }
        return pos;
    }

    private int getScrapViewNum(int screenRange) {
        if (!getItemDeleteAnimation()) {
            return 0;
        }
        int numAboveScreen = 0;
        int numWithinScreen = 0;
        int numBelowScreen = 0;
        for (Entry<Integer, Integer> entry : this.mAnimationViewIndexMap.entrySet()) {
            Integer index = (Integer) entry.getKey();
            if (index.intValue() < this.mBackupFirstVisiblePosition) {
                numAboveScreen++;
            } else if (index.intValue() > this.mBackupLastVisiblePosition) {
                numBelowScreen++;
            } else {
                numWithinScreen++;
            }
        }
        switch (screenRange) {
            case 0:
                return numAboveScreen;
            case 1:
                return numWithinScreen;
            case 2:
                return numBelowScreen;
            case 3:
                return numWithinScreen + numAboveScreen;
            default:
                return numWithinScreen;
        }
    }

    protected void playAnimation(final AdapterDataSetObserver observer) {
        ValueAnimator animator = ValueAnimator.ofInt(new int[]{100, 0});
        animator.setInterpolator(new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f));
        this.mBackupFirstVisiblePosition = getFirstVisiblePosition();
        this.mBackupLastVisiblePosition = getLastVisiblePosition();
        this.mBaclupnumAboveScreen = getScrapViewNum(0);
        for (Entry<Integer, Integer> entry : this.mAnimationViewIndexMap.entrySet()) {
            Integer index = (Integer) entry.getKey();
            if (index.intValue() >= this.mBackupFirstVisiblePosition && index.intValue() <= this.mBackupLastVisiblePosition) {
                this.mAnimationViewIndexMap.put(index, Integer.valueOf(getChildAt(index.intValue() - getFirstVisiblePosition()).getHeight()));
            }
        }
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                for (Entry<Integer, Integer> entry : ListView.this.mAnimationViewIndexMap.entrySet()) {
                    Integer index = (Integer) entry.getKey();
                    if (index.intValue() >= ListView.this.mBackupFirstVisiblePosition && index.intValue() <= ListView.this.mBackupLastVisiblePosition) {
                        View child = ListView.this.getChildAt(index.intValue() - ListView.this.getFirstVisiblePosition());
                        if (child != null) {
                            LayoutParams lp = (LayoutParams) child.getLayoutParams();
                            int value = ((Integer) animation.getAnimatedValue()).intValue();
                            lp.height = (((Integer) entry.getValue()).intValue() * value) / 100;
                            child.setLayoutParams(lp);
                            child.setAlpha(((float) value) / 100.0f);
                        }
                    }
                }
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                animation.cancel();
                if (ListView.this.mBackupAdapterObjectMap != null) {
                    ListView.this.fillBackupAdapterObjectMap();
                    ListView.this.mAnimationViewIndexMap.clear();
                    int childCount = ListView.this.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        ListView.this.getChildAt(i).setAlpha(1.0f);
                    }
                    observer.onChangedAndRecordItem();
                }
                ListView.this.setEnabled(true);
            }
        });
        animator.setDuration(200);
        animator.start();
    }
}
