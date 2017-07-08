package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewGroup.OnHierarchyChangeListener;
import com.android.internal.R;

public class RadioGroup extends LinearLayout {
    private int mCheckedId;
    private android.widget.CompoundButton.OnCheckedChangeListener mChildOnCheckedChangeListener;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private PassThroughHierarchyChangeListener mPassThroughListener;
    private boolean mProtectFromCheckedChange;

    private class CheckedStateTracker implements android.widget.CompoundButton.OnCheckedChangeListener {
        private CheckedStateTracker() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!RadioGroup.this.mProtectFromCheckedChange) {
                RadioGroup.this.mProtectFromCheckedChange = true;
                if (RadioGroup.this.mCheckedId != -1) {
                    RadioGroup.this.setCheckedStateForView(RadioGroup.this.mCheckedId, false);
                }
                RadioGroup.this.mProtectFromCheckedChange = false;
                RadioGroup.this.setCheckedId(buttonView.getId());
            }
        }
    }

    public static class LayoutParams extends android.widget.LinearLayout.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(int w, int h, float initWeight) {
            super(w, h, initWeight);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        protected void setBaseAttributes(TypedArray a, int widthAttr, int heightAttr) {
            if (a.hasValue(widthAttr)) {
                this.width = a.getLayoutDimension(widthAttr, "layout_width");
            } else {
                this.width = -2;
            }
            if (a.hasValue(heightAttr)) {
                this.height = a.getLayoutDimension(heightAttr, "layout_height");
            } else {
                this.height = -2;
            }
        }
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(RadioGroup radioGroup, int i);
    }

    private class PassThroughHierarchyChangeListener implements OnHierarchyChangeListener {
        private OnHierarchyChangeListener mOnHierarchyChangeListener;

        private PassThroughHierarchyChangeListener() {
        }

        public void onChildViewAdded(View parent, View child) {
            if (parent == RadioGroup.this && (child instanceof RadioButton)) {
                if (child.getId() == -1) {
                    child.setId(View.generateViewId());
                }
                ((RadioButton) child).setOnCheckedChangeWidgetListener(RadioGroup.this.mChildOnCheckedChangeListener);
            }
            if (this.mOnHierarchyChangeListener != null) {
                this.mOnHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        public void onChildViewRemoved(View parent, View child) {
            if (parent == RadioGroup.this && (child instanceof RadioButton)) {
                ((RadioButton) child).setOnCheckedChangeWidgetListener(null);
            }
            if (this.mOnHierarchyChangeListener != null) {
                this.mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }

    public RadioGroup(Context context) {
        super(context);
        this.mCheckedId = -1;
        this.mProtectFromCheckedChange = false;
        setOrientation(1);
        init();
    }

    public RadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCheckedId = -1;
        this.mProtectFromCheckedChange = false;
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RadioGroup, R.attr.radioButtonStyle, 0);
        int value = attributes.getResourceId(1, -1);
        if (value != -1) {
            this.mCheckedId = value;
        }
        setOrientation(attributes.getInt(0, 1));
        attributes.recycle();
        init();
    }

    private void init() {
        this.mChildOnCheckedChangeListener = new CheckedStateTracker();
        this.mPassThroughListener = new PassThroughHierarchyChangeListener();
        super.setOnHierarchyChangeListener(this.mPassThroughListener);
    }

    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        this.mPassThroughListener.mOnHierarchyChangeListener = listener;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        if (this.mCheckedId != -1) {
            this.mProtectFromCheckedChange = true;
            setCheckedStateForView(this.mCheckedId, true);
            this.mProtectFromCheckedChange = false;
            setCheckedId(this.mCheckedId);
        }
    }

    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        if (child instanceof RadioButton) {
            RadioButton button = (RadioButton) child;
            if (button.isChecked()) {
                this.mProtectFromCheckedChange = true;
                if (this.mCheckedId != -1) {
                    setCheckedStateForView(this.mCheckedId, false);
                }
                this.mProtectFromCheckedChange = false;
                setCheckedId(button.getId());
            }
        }
        super.addView(child, index, params);
    }

    public void check(int id) {
        if (id == -1 || id != this.mCheckedId) {
            if (this.mCheckedId != -1) {
                setCheckedStateForView(this.mCheckedId, false);
            }
            if (id != -1) {
                setCheckedStateForView(id, true);
            }
            setCheckedId(id);
        }
    }

    private void setCheckedId(int id) {
        this.mCheckedId = id;
        if (this.mOnCheckedChangeListener != null) {
            this.mOnCheckedChangeListener.onCheckedChanged(this, this.mCheckedId);
        }
    }

    private void setCheckedStateForView(int viewId, boolean checked) {
        View checkedView = findViewById(viewId);
        if (checkedView != null && (checkedView instanceof RadioButton)) {
            ((RadioButton) checkedView).setChecked(checked);
        }
    }

    public int getCheckedRadioButtonId() {
        return this.mCheckedId;
    }

    public void clearCheck() {
        check(-1);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mOnCheckedChangeListener = listener;
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    protected android.widget.LinearLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    public CharSequence getAccessibilityClassName() {
        return RadioGroup.class.getName();
    }
}
