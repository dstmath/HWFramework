package android.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ResourceId;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.Bundle;
import android.os.Message;
import android.text.method.MovementMethod;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.android.internal.R;
import com.android.internal.app.AlertController;

public class AlertDialog extends Dialog implements DialogInterface {
    public static final int LAYOUT_HINT_NONE = 0;
    public static final int LAYOUT_HINT_SIDE = 1;
    @Deprecated
    public static final int THEME_DEVICE_DEFAULT_DARK = 4;
    @Deprecated
    public static final int THEME_DEVICE_DEFAULT_LIGHT = 5;
    @Deprecated
    public static final int THEME_HOLO_DARK = 2;
    @Deprecated
    public static final int THEME_HOLO_LIGHT = 3;
    @Deprecated
    public static final int THEME_TRADITIONAL = 1;
    /* access modifiers changed from: private */
    public AlertController mAlert;

    public static class Builder {
        private final AlertController.AlertParams P;

        public Builder(Context context) {
            this(context, AlertDialog.resolveDialogTheme(context, 0));
        }

        public Builder(Context context, int themeResId) {
            this.P = new AlertController.AlertParams(new ContextThemeWrapper(context, AlertDialog.resolveDialogTheme(context, themeResId)));
        }

        public Context getContext() {
            return this.P.mContext;
        }

        public Builder setTitle(int titleId) {
            this.P.mTitle = this.P.mContext.getText(titleId);
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.P.mTitle = title;
            return this;
        }

        public Builder setCustomTitle(View customTitleView) {
            this.P.mCustomTitleView = customTitleView;
            return this;
        }

        public Builder setMessage(int messageId) {
            this.P.mMessage = this.P.mContext.getText(messageId);
            return this;
        }

        public Builder setMessage(CharSequence message) {
            this.P.mMessage = message;
            return this;
        }

        public Builder setIcon(int iconId) {
            this.P.mIconId = iconId;
            return this;
        }

        public Builder setIcon(Drawable icon) {
            this.P.mIcon = icon;
            return this;
        }

        public Builder setIconAttribute(int attrId) {
            TypedValue out = new TypedValue();
            this.P.mContext.getTheme().resolveAttribute(attrId, out, true);
            this.P.mIconId = out.resourceId;
            return this;
        }

        public Builder setPositiveButton(int textId, DialogInterface.OnClickListener listener) {
            this.P.mPositiveButtonText = this.P.mContext.getText(textId);
            this.P.mPositiveButtonListener = listener;
            return this;
        }

        public Builder setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
            this.P.mPositiveButtonText = text;
            this.P.mPositiveButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(int textId, DialogInterface.OnClickListener listener) {
            this.P.mNegativeButtonText = this.P.mContext.getText(textId);
            this.P.mNegativeButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
            this.P.mNegativeButtonText = text;
            this.P.mNegativeButtonListener = listener;
            return this;
        }

        public Builder setNeutralButton(int textId, DialogInterface.OnClickListener listener) {
            this.P.mNeutralButtonText = this.P.mContext.getText(textId);
            this.P.mNeutralButtonListener = listener;
            return this;
        }

        public Builder setNeutralButton(CharSequence text, DialogInterface.OnClickListener listener) {
            this.P.mNeutralButtonText = text;
            this.P.mNeutralButtonListener = listener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.P.mCancelable = cancelable;
            return this;
        }

        public Builder setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
            this.P.mOnCancelListener = onCancelListener;
            return this;
        }

        public Builder setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
            this.P.mOnDismissListener = onDismissListener;
            return this;
        }

        public Builder setOnKeyListener(DialogInterface.OnKeyListener onKeyListener) {
            this.P.mOnKeyListener = onKeyListener;
            return this;
        }

        public Builder setItems(int itemsId, DialogInterface.OnClickListener listener) {
            this.P.mItems = this.P.mContext.getResources().getTextArray(itemsId);
            this.P.mOnClickListener = listener;
            return this;
        }

        public Builder setItems(CharSequence[] items, DialogInterface.OnClickListener listener) {
            this.P.mItems = items;
            this.P.mOnClickListener = listener;
            return this;
        }

        public Builder setAdapter(ListAdapter adapter, DialogInterface.OnClickListener listener) {
            this.P.mAdapter = adapter;
            this.P.mOnClickListener = listener;
            return this;
        }

        public Builder setCursor(Cursor cursor, DialogInterface.OnClickListener listener, String labelColumn) {
            this.P.mCursor = cursor;
            this.P.mLabelColumn = labelColumn;
            this.P.mOnClickListener = listener;
            return this;
        }

        public Builder setMultiChoiceItems(int itemsId, boolean[] checkedItems, DialogInterface.OnMultiChoiceClickListener listener) {
            this.P.mItems = this.P.mContext.getResources().getTextArray(itemsId);
            this.P.mOnCheckboxClickListener = listener;
            this.P.mCheckedItems = checkedItems;
            this.P.mIsMultiChoice = true;
            return this;
        }

        public Builder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems, DialogInterface.OnMultiChoiceClickListener listener) {
            this.P.mItems = items;
            this.P.mOnCheckboxClickListener = listener;
            this.P.mCheckedItems = checkedItems;
            this.P.mIsMultiChoice = true;
            return this;
        }

        public Builder setMultiChoiceItems(Cursor cursor, String isCheckedColumn, String labelColumn, DialogInterface.OnMultiChoiceClickListener listener) {
            this.P.mCursor = cursor;
            this.P.mOnCheckboxClickListener = listener;
            this.P.mIsCheckedColumn = isCheckedColumn;
            this.P.mLabelColumn = labelColumn;
            this.P.mIsMultiChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(int itemsId, int checkedItem, DialogInterface.OnClickListener listener) {
            this.P.mItems = this.P.mContext.getResources().getTextArray(itemsId);
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mIsSingleChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(Cursor cursor, int checkedItem, String labelColumn, DialogInterface.OnClickListener listener) {
            this.P.mCursor = cursor;
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mLabelColumn = labelColumn;
            this.P.mIsSingleChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(CharSequence[] items, int checkedItem, DialogInterface.OnClickListener listener) {
            this.P.mItems = items;
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mIsSingleChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(ListAdapter adapter, int checkedItem, DialogInterface.OnClickListener listener) {
            this.P.mAdapter = adapter;
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mIsSingleChoice = true;
            return this;
        }

        public Builder setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
            this.P.mOnItemSelectedListener = listener;
            return this;
        }

        public Builder setView(int layoutResId) {
            this.P.mView = null;
            this.P.mViewLayoutResId = layoutResId;
            this.P.mViewSpacingSpecified = false;
            return this;
        }

        public Builder setView(View view) {
            this.P.mView = view;
            this.P.mViewLayoutResId = 0;
            this.P.mViewSpacingSpecified = false;
            return this;
        }

        @Deprecated
        public Builder setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
            this.P.mView = view;
            this.P.mViewLayoutResId = 0;
            this.P.mViewSpacingSpecified = true;
            this.P.mViewSpacingLeft = viewSpacingLeft;
            this.P.mViewSpacingTop = viewSpacingTop;
            this.P.mViewSpacingRight = viewSpacingRight;
            this.P.mViewSpacingBottom = viewSpacingBottom;
            return this;
        }

        @Deprecated
        public Builder setInverseBackgroundForced(boolean useInverseBackground) {
            this.P.mForceInverseBackground = useInverseBackground;
            return this;
        }

        public Builder setRecycleOnMeasureEnabled(boolean enabled) {
            this.P.mRecycleOnMeasure = enabled;
            return this;
        }

        public AlertDialog create() {
            AlertDialog dialog = new AlertDialog(this.P.mContext, 0, false);
            this.P.apply(dialog.mAlert);
            dialog.setCancelable(this.P.mCancelable);
            if (this.P.mCancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }
            dialog.setOnCancelListener(this.P.mOnCancelListener);
            dialog.setOnDismissListener(this.P.mOnDismissListener);
            if (this.P.mOnKeyListener != null) {
                dialog.setOnKeyListener(this.P.mOnKeyListener);
            }
            return dialog;
        }

        public AlertDialog show() {
            AlertDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }

    protected AlertDialog(Context context) {
        this(context, 0);
    }

    protected AlertDialog(Context context, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        this(context, 0);
        setCancelable(cancelable);
        setOnCancelListener(cancelListener);
    }

    protected AlertDialog(Context context, int themeResId) {
        this(context, themeResId, true);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    AlertDialog(Context context, int themeResId, boolean createContextThemeWrapper) {
        super(context, createContextThemeWrapper ? resolveDialogTheme(context, themeResId) : 0, createContextThemeWrapper);
        this.mWindow.alwaysReadCloseOnTouchAttr();
        this.mAlert = HwWidgetFactory.getHwAlertController(getContext(), this, getWindow());
    }

    static int resolveDialogTheme(Context context, int themeResId) {
        if (themeResId == 1) {
            return R.style.Theme_Dialog_Alert;
        }
        if (themeResId == 2) {
            return R.style.Theme_Holo_Dialog_Alert;
        }
        if (themeResId == 3) {
            return R.style.Theme_Holo_Light_Dialog_Alert;
        }
        if (themeResId == 4) {
            return 16974545;
        }
        if (themeResId == 5) {
            return 16974546;
        }
        if (ResourceId.isValid(themeResId)) {
            return themeResId;
        }
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(16843529, outValue, true);
        return outValue.resourceId;
    }

    public Button getButton(int whichButton) {
        return this.mAlert.getButton(whichButton);
    }

    public ListView getListView() {
        return this.mAlert.getListView();
    }

    public void setTitle(CharSequence title) {
        super.setTitle(title);
        this.mAlert.setTitle(title);
    }

    public void setCustomTitle(View customTitleView) {
        this.mAlert.setCustomTitle(customTitleView);
    }

    public void setMessage(CharSequence message) {
        this.mAlert.setMessage(message);
    }

    public void setMessageMovementMethod(MovementMethod movementMethod) {
        this.mAlert.setMessageMovementMethod(movementMethod);
    }

    public void setMessageHyphenationFrequency(int hyphenationFrequency) {
        this.mAlert.setMessageHyphenationFrequency(hyphenationFrequency);
    }

    public void setView(View view) {
        this.mAlert.setView(view);
    }

    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
        this.mAlert.setView(view, viewSpacingLeft, viewSpacingTop, viewSpacingRight, viewSpacingBottom);
    }

    /* access modifiers changed from: package-private */
    public void setButtonPanelLayoutHint(int layoutHint) {
        this.mAlert.setButtonPanelLayoutHint(layoutHint);
    }

    public void setButton(int whichButton, CharSequence text, Message msg) {
        this.mAlert.setButton(whichButton, text, null, msg);
    }

    public void setButton(int whichButton, CharSequence text, DialogInterface.OnClickListener listener) {
        this.mAlert.setButton(whichButton, text, listener, null);
    }

    @Deprecated
    public void setButton(CharSequence text, Message msg) {
        setButton(-1, text, msg);
    }

    @Deprecated
    public void setButton2(CharSequence text, Message msg) {
        setButton(-2, text, msg);
    }

    @Deprecated
    public void setButton3(CharSequence text, Message msg) {
        setButton(-3, text, msg);
    }

    @Deprecated
    public void setButton(CharSequence text, DialogInterface.OnClickListener listener) {
        setButton(-1, text, listener);
    }

    @Deprecated
    public void setButton2(CharSequence text, DialogInterface.OnClickListener listener) {
        setButton(-2, text, listener);
    }

    @Deprecated
    public void setButton3(CharSequence text, DialogInterface.OnClickListener listener) {
        setButton(-3, text, listener);
    }

    public void setIcon(int resId) {
        this.mAlert.setIcon(resId);
    }

    public void setIcon(Drawable icon) {
        this.mAlert.setIcon(icon);
    }

    public void setMessageNotScrolling() {
        this.mAlert.setMessageNotScrolling();
    }

    public void setIconAttribute(int attrId) {
        TypedValue out = new TypedValue();
        this.mContext.getTheme().resolveAttribute(attrId, out, true);
        this.mAlert.setIcon(out.resourceId);
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
        this.mAlert.setInverseBackgroundForced(forceInverseBackground);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAlert.installContent();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mAlert.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mAlert.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}
