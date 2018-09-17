package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class DynamicAddItemListView extends ListView {
    private Button mCancelButton;
    private Button mConfirmButton;
    private Object mCustomData;
    private ViewGroup mCustomPanel;
    private View mDynamicAddItemLayout;
    private EditText mEditText;
    private ErrorTipTextLayout mErrorTipTextLayout;
    private OnAddItemListener mOnAddItemListener;
    private int mOriPaddingEnd;
    private int mOriPaddingStart;
    private int mPaddingHorizontal;
    private int mPaddingVertical;
    private TextView mTextView;
    private int mTipPadding;

    public interface OnAddItemListener {
        void onAddItem(String str, Object obj);
    }

    public DynamicAddItemListView(Context context) {
        this(context, null);
    }

    public DynamicAddItemListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842868);
    }

    public DynamicAddItemListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DynamicAddItemListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mDynamicAddItemLayout = LayoutInflater.from(context).inflate(34013216, null);
        this.mTextView = (TextView) this.mDynamicAddItemLayout.findViewById(34603095);
        this.mEditText = (EditText) this.mDynamicAddItemLayout.findViewById(34603096);
        this.mEditText.setBackground(null);
        this.mCancelButton = (Button) this.mDynamicAddItemLayout.findViewById(34603097);
        this.mConfirmButton = (Button) this.mDynamicAddItemLayout.findViewById(34603098);
        this.mCustomPanel = (ViewGroup) this.mDynamicAddItemLayout.findViewById(34603099);
        this.mErrorTipTextLayout = (ErrorTipTextLayout) this.mDynamicAddItemLayout.findViewById(34603100);
        this.mOriPaddingStart = this.mEditText.getPaddingStart();
        this.mOriPaddingEnd = this.mEditText.getPaddingEnd();
        this.mPaddingHorizontal = context.getResources().getDimensionPixelOffset(34472149);
        this.mPaddingVertical = context.getResources().getDimensionPixelOffset(34472150);
        this.mTipPadding = context.getResources().getDimensionPixelOffset(34472151);
        this.mTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                DynamicAddItemListView.this.enterEditMode();
            }
        });
        this.mConfirmButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                String s_user_input = DynamicAddItemListView.this.mEditText.getText().toString();
                if (!("".equals(s_user_input) || DynamicAddItemListView.this.mOnAddItemListener == null)) {
                    DynamicAddItemListView.this.mOnAddItemListener.onAddItem(s_user_input, DynamicAddItemListView.this.getCustomData());
                }
                DynamicAddItemListView.this.exitEditMode();
            }
        });
        this.mCancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                DynamicAddItemListView.this.exitEditMode();
            }
        });
    }

    private void enterEditMode() {
        this.mTextView.setVisibility(8);
        this.mCancelButton.setVisibility(0);
        this.mErrorTipTextLayout.setVisibility(0);
        this.mErrorTipTextLayout.setPadding(0, 0, 0, 0);
        if (this.mCustomPanel.getChildCount() != 0) {
            this.mCustomPanel.setVisibility(0);
            this.mEditText.setPaddingRelative(this.mOriPaddingStart, this.mPaddingVertical, this.mPaddingHorizontal + this.mOriPaddingEnd, this.mPaddingVertical);
        } else {
            this.mEditText.setPaddingRelative(this.mOriPaddingStart, this.mPaddingVertical, this.mOriPaddingEnd, this.mPaddingVertical);
        }
        this.mConfirmButton.setVisibility(0);
    }

    private void exitEditMode() {
        this.mEditText.setText("");
        this.mErrorTipTextLayout.setError(null);
        this.mTextView.setVisibility(0);
        this.mCancelButton.setVisibility(8);
        this.mErrorTipTextLayout.setVisibility(8);
        this.mConfirmButton.setVisibility(8);
        this.mCustomPanel.setVisibility(8);
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm != null && imm.isActive(this)) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    public void setOnAddItemListener(OnAddItemListener onAddItemListener) {
        this.mOnAddItemListener = onAddItemListener;
    }

    public void updateDynamicAddItemLayoutPos() {
        int list_height = getHeight();
        int footer_bottom = this.mDynamicAddItemLayout.getBottom();
        int last_vis = getLastVisiblePosition();
        if (footer_bottom >= list_height) {
            setSelection(last_vis);
        }
    }

    public ViewGroup getCustomPanel() {
        return this.mCustomPanel;
    }

    public Object getCustomData() {
        return this.mCustomData;
    }

    public void setCustemData(Object data) {
        this.mCustomData = data;
    }

    public void setEditTextHint(int id) {
        this.mEditText.setHint(id);
        this.mTextView.setText(id);
    }

    public void setEditTextHint(CharSequence s) {
        this.mEditText.setHint(s);
        this.mTextView.setText(s);
    }

    public void setDynamicAddItemLayoutOnTop(boolean isOnTop) {
        if (isOnTop) {
            addHeaderView(this.mDynamicAddItemLayout);
        } else {
            addFooterView(this.mDynamicAddItemLayout);
        }
    }

    public void setTextError(CharSequence s) {
        if (s != null) {
            if (this.mCustomPanel.getChildCount() != 0) {
                this.mEditText.setPaddingRelative(this.mOriPaddingStart, this.mPaddingVertical, this.mPaddingHorizontal + this.mOriPaddingEnd, 0);
            } else {
                this.mEditText.setPaddingRelative(this.mOriPaddingStart, this.mPaddingVertical, this.mOriPaddingEnd, 0);
            }
            this.mErrorTipTextLayout.setPadding(0, 0, 0, this.mTipPadding);
            this.mErrorTipTextLayout.setError(s);
            return;
        }
        this.mErrorTipTextLayout.setPadding(0, 0, 0, 0);
        this.mErrorTipTextLayout.setError(null);
        this.mEditText.setPaddingRelative(this.mOriPaddingStart, this.mPaddingVertical, this.mOriPaddingEnd, this.mPaddingVertical);
    }
}
