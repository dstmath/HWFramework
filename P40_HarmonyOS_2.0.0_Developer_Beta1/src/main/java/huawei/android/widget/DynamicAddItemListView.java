package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
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
        this(context, attrs, context.getResources().getIdentifier("listViewStyle", "attr", "android"));
    }

    public DynamicAddItemListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DynamicAddItemListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mDynamicAddItemLayout = LayoutInflater.from(context).inflate(34013216, (ViewGroup) null);
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
        this.mTextView.setOnClickListener(new View.OnClickListener() {
            /* class huawei.android.widget.DynamicAddItemListView.AnonymousClass1 */

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DynamicAddItemListView.this.enterEditMode();
            }
        });
        this.mConfirmButton.setOnClickListener(new View.OnClickListener() {
            /* class huawei.android.widget.DynamicAddItemListView.AnonymousClass2 */

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                String userInput = DynamicAddItemListView.this.mEditText.getText().toString();
                if (!"".equals(userInput) && DynamicAddItemListView.this.mOnAddItemListener != null) {
                    DynamicAddItemListView.this.mOnAddItemListener.onAddItem(userInput, DynamicAddItemListView.this.getCustomData());
                }
                DynamicAddItemListView.this.exitEditMode();
            }
        });
        this.mCancelButton.setOnClickListener(new View.OnClickListener() {
            /* class huawei.android.widget.DynamicAddItemListView.AnonymousClass3 */

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DynamicAddItemListView.this.exitEditMode();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enterEditMode() {
        this.mTextView.setVisibility(8);
        this.mCancelButton.setVisibility(0);
        this.mErrorTipTextLayout.setVisibility(0);
        this.mErrorTipTextLayout.setPadding(0, 0, 0, 0);
        if (this.mCustomPanel.getChildCount() != 0) {
            this.mCustomPanel.setVisibility(0);
            EditText editText = this.mEditText;
            int i = this.mOriPaddingStart;
            int i2 = this.mPaddingVertical;
            editText.setPaddingRelative(i, i2, this.mPaddingHorizontal + this.mOriPaddingEnd, i2);
        } else {
            EditText editText2 = this.mEditText;
            int i3 = this.mOriPaddingStart;
            int i4 = this.mPaddingVertical;
            editText2.setPaddingRelative(i3, i4, this.mOriPaddingEnd, i4);
        }
        this.mConfirmButton.setVisibility(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void exitEditMode() {
        this.mEditText.setText("");
        this.mErrorTipTextLayout.setError(null);
        this.mTextView.setVisibility(0);
        this.mCancelButton.setVisibility(8);
        this.mErrorTipTextLayout.setVisibility(8);
        this.mConfirmButton.setVisibility(8);
        this.mCustomPanel.setVisibility(8);
        InputMethodManager inputMethodManager = InputMethodManager.peekInstance();
        if (inputMethodManager != null && inputMethodManager.isActive(this)) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    public void setOnAddItemListener(OnAddItemListener onAddItemListener) {
        this.mOnAddItemListener = onAddItemListener;
    }

    public void updateDynamicAddItemLayoutPos() {
        int listHeight = getHeight();
        int footerBottom = this.mDynamicAddItemLayout.getBottom();
        int lastVisiblePosition = getLastVisiblePosition();
        if (footerBottom >= listHeight) {
            setSelection(lastVisiblePosition);
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

    public void setEditTextHint(CharSequence hint) {
        this.mEditText.setHint(hint);
        this.mTextView.setText(hint);
    }

    public void setDynamicAddItemLayoutOnTop(boolean isOnTop) {
        if (isOnTop) {
            addHeaderView(this.mDynamicAddItemLayout);
        } else {
            addFooterView(this.mDynamicAddItemLayout);
        }
    }

    public void setTextError(CharSequence error) {
        if (error != null) {
            if (this.mCustomPanel.getChildCount() != 0) {
                this.mEditText.setPaddingRelative(this.mOriPaddingStart, this.mPaddingVertical, this.mPaddingHorizontal + this.mOriPaddingEnd, 0);
            } else {
                this.mEditText.setPaddingRelative(this.mOriPaddingStart, this.mPaddingVertical, this.mOriPaddingEnd, 0);
            }
            this.mErrorTipTextLayout.setPadding(0, 0, 0, this.mTipPadding);
            this.mErrorTipTextLayout.setError(error);
            return;
        }
        this.mErrorTipTextLayout.setPadding(0, 0, 0, 0);
        this.mErrorTipTextLayout.setError(null);
        EditText editText = this.mEditText;
        int i = this.mOriPaddingStart;
        int i2 = this.mPaddingVertical;
        editText.setPaddingRelative(i, i2, this.mOriPaddingEnd, i2);
    }
}
