package android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import com.android.internal.R;
import java.lang.ref.WeakReference;

@RemoteViews.RemoteView
public final class ViewStub extends View {
    private OnInflateListener mInflateListener;
    private int mInflatedId;
    private WeakReference<View> mInflatedViewRef;
    private LayoutInflater mInflater;
    private int mLayoutResource;

    public interface OnInflateListener {
        void onInflate(ViewStub viewStub, View view);
    }

    public class ViewReplaceRunnable implements Runnable {
        public final View view;

        ViewReplaceRunnable(View view2) {
            this.view = view2;
        }

        public void run() {
            ViewStub.this.replaceSelfWithView(this.view, (ViewGroup) ViewStub.this.getParent());
        }
    }

    public ViewStub(Context context) {
        this(context, 0);
    }

    public ViewStub(Context context, int layoutResource) {
        this(context, (AttributeSet) null);
        this.mLayoutResource = layoutResource;
    }

    public ViewStub(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewStub(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ViewStub(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewStub, defStyleAttr, defStyleRes);
        this.mInflatedId = a.getResourceId(2, -1);
        this.mLayoutResource = a.getResourceId(1, 0);
        this.mID = a.getResourceId(0, -1);
        a.recycle();
        setVisibility(8);
        setWillNotDraw(true);
    }

    public int getInflatedId() {
        return this.mInflatedId;
    }

    @RemotableViewMethod(asyncImpl = "setInflatedIdAsync")
    public void setInflatedId(int inflatedId) {
        this.mInflatedId = inflatedId;
    }

    public Runnable setInflatedIdAsync(int inflatedId) {
        this.mInflatedId = inflatedId;
        return null;
    }

    public int getLayoutResource() {
        return this.mLayoutResource;
    }

    @RemotableViewMethod(asyncImpl = "setLayoutResourceAsync")
    public void setLayoutResource(int layoutResource) {
        this.mLayoutResource = layoutResource;
    }

    public Runnable setLayoutResourceAsync(int layoutResource) {
        this.mLayoutResource = layoutResource;
        return null;
    }

    public void setLayoutInflater(LayoutInflater inflater) {
        this.mInflater = inflater;
    }

    public LayoutInflater getLayoutInflater() {
        return this.mInflater;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(0, 0);
    }

    public void draw(Canvas canvas) {
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
    }

    @RemotableViewMethod(asyncImpl = "setVisibilityAsync")
    public void setVisibility(int visibility) {
        if (this.mInflatedViewRef != null) {
            View view = (View) this.mInflatedViewRef.get();
            if (view != null) {
                view.setVisibility(visibility);
                return;
            }
            throw new IllegalStateException("setVisibility called on un-referenced view");
        }
        super.setVisibility(visibility);
        if (visibility == 0 || visibility == 4) {
            inflate();
        }
    }

    public Runnable setVisibilityAsync(int visibility) {
        if (visibility == 0 || visibility == 4) {
            return new ViewReplaceRunnable(inflateViewNoAdd((ViewGroup) getParent()));
        }
        return null;
    }

    private View inflateViewNoAdd(ViewGroup parent) {
        LayoutInflater factory;
        if (this.mInflater != null) {
            factory = this.mInflater;
        } else {
            factory = LayoutInflater.from(this.mContext);
        }
        View view = factory.inflate(this.mLayoutResource, parent, false);
        if (this.mInflatedId != -1) {
            view.setId(this.mInflatedId);
        }
        return view;
    }

    /* access modifiers changed from: private */
    public void replaceSelfWithView(View view, ViewGroup parent) {
        int index = parent.indexOfChild(this);
        parent.removeViewInLayout(this);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            parent.addView(view, index, layoutParams);
        } else {
            parent.addView(view, index);
        }
    }

    public View inflate() {
        ViewParent viewParent = getParent();
        if (viewParent == null || !(viewParent instanceof ViewGroup)) {
            throw new IllegalStateException("ViewStub must have a non-null ViewGroup viewParent");
        } else if (this.mLayoutResource != 0) {
            ViewGroup parent = (ViewGroup) viewParent;
            View view = inflateViewNoAdd(parent);
            replaceSelfWithView(view, parent);
            this.mInflatedViewRef = new WeakReference<>(view);
            if (this.mInflateListener != null) {
                this.mInflateListener.onInflate(this, view);
            }
            return view;
        } else {
            throw new IllegalArgumentException("ViewStub must have a valid layoutResource");
        }
    }

    public void setOnInflateListener(OnInflateListener inflateListener) {
        this.mInflateListener = inflateListener;
    }
}
