package android.view;

import android.content.Context;
import android.util.Log;
import com.huawei.internal.widget.ConstantValues;

public class HwViewGroupImpl implements IHwViewGroup {
    private static final int MILLISECOND_PER_SECOND = 1000;
    private static final int OFFSET_THRESHOLD = 4;
    private static final int SPEED_STEP_FACTOR = 50;
    private static final int SPEED_THRESHOLD = 200;
    private static final String TAG = "HwViewGroupImpl";
    private static final int TOUCH_SLOP_FOR_720P = 16;
    private static volatile HwViewGroupImpl sInstance = null;
    private int mCurrentMoveCount = 0;
    private boolean mHasMultiTouch = false;
    private long mLastEventTime = 0;
    private int mLastSingleMoveOffsetY = 0;
    private int mLastSpeedY = 0;
    private int mLastX = 0;
    private int mLastY = 0;
    private int mTriggeredMoveCount = 0;

    private HwViewGroupImpl() {
    }

    public static synchronized HwViewGroupImpl getDefault() {
        HwViewGroupImpl hwViewGroupImpl;
        synchronized (HwViewGroupImpl.class) {
            if (sInstance == null) {
                sInstance = new HwViewGroupImpl();
            }
            hwViewGroupImpl = sInstance;
        }
        return hwViewGroupImpl;
    }

    public HwViewGroupImpl newInstance() {
        return new HwViewGroupImpl();
    }

    public boolean isSubRecyclerView(ViewGroup instance) {
        Class clazz = instance.getClass();
        if (clazz == null) {
            return false;
        }
        clazz.getName();
        if (!clazz.getName().contains("RecyclerView")) {
            return false;
        }
        while (clazz != null) {
            if ("android.support.v7.widget.RecyclerView".equals(clazz.getName()) || "androidx.recyclerview.widget.RecyclerView".equals(clazz.getName())) {
                return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    public boolean accelerateSliding(Context context, boolean isAccelerateSliding, MotionEvent ev) {
        boolean result = false;
        int actionMasked = ev.getAction() & ConstantValues.MAX_CHANNEL_VALUE;
        if (actionMasked == 0 || actionMasked == 1) {
            result = false;
            reset(ev);
        }
        if (actionMasked == 5 || actionMasked == 6) {
            this.mHasMultiTouch = true;
        }
        if (actionMasked == 2) {
            int i = this.mTriggeredMoveCount;
            if ((i != 0 && this.mCurrentMoveCount > i) || this.mHasMultiTouch) {
                return result;
            }
            this.mCurrentMoveCount++;
            int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
            int moveY = (int) (ev.getY() + 0.5f);
            int moveX = (int) (ev.getX() + 0.5f);
            int singleMoveOffsetY = Math.abs(moveY - this.mLastY);
            int singleMoveOffsetX = Math.abs(moveX - this.mLastX);
            int offsetTime = (int) (ev.getEventTime() - this.mLastEventTime);
            int speedY = offsetTime == 0 ? 0 : (singleMoveOffsetY * 1000) / offsetTime;
            int i2 = this.mCurrentMoveCount;
            int speedThreashold = ((((i2 - 1) * 50) + 200) * touchSlop) / 16;
            int offsetThreashold = (touchSlop * 4) / 16;
            if (!isAccelerateSliding && this.mTriggeredMoveCount == 0 && singleMoveOffsetY > singleMoveOffsetX && speedY >= speedThreashold && singleMoveOffsetY >= offsetThreashold) {
                result = true;
                this.mTriggeredMoveCount = i2;
                Log.i(TAG, "accelerateSliding accelerate sliding.. mTriggeredMoveCount = " + this.mTriggeredMoveCount);
            }
            this.mLastSingleMoveOffsetY = singleMoveOffsetY;
            this.mLastY = moveY;
            this.mLastX = moveX;
            this.mLastSpeedY = speedY;
            this.mLastEventTime = ev.getEventTime();
        }
        return result;
    }

    private void reset(MotionEvent ev) {
        this.mCurrentMoveCount = 0;
        this.mTriggeredMoveCount = 0;
        this.mLastY = (int) (ev.getY() + 0.5f);
        this.mLastX = (int) (ev.getX() + 0.5f);
        this.mLastEventTime = ev.getEventTime();
        this.mLastSpeedY = 0;
        this.mLastSingleMoveOffsetY = 0;
        this.mHasMultiTouch = false;
    }
}
