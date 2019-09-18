package android.preference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.R;

public abstract class DialogPreference extends Preference implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener, PreferenceManager.OnActivityDestroyListener {
    private AlertDialog.Builder mBuilder;
    private Dialog mDialog;
    private Drawable mDialogIcon;
    private int mDialogLayoutResId;
    private CharSequence mDialogMessage;
    private CharSequence mDialogTitle;
    private CharSequence mNegativeButtonText;
    private CharSequence mPositiveButtonText;
    private int mWhichButtonClicked;

    private static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        Bundle dialogBundle;
        boolean isDialogShowing;

        public SavedState(Parcel source) {
            super(source);
            this.isDialogShowing = source.readInt() != 1 ? false : true;
            this.dialogBundle = source.readBundle();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.isDialogShowing ? 1 : 0);
            dest.writeBundle(this.dialogBundle);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public DialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DialogPreference, defStyleAttr, defStyleRes);
        this.mDialogTitle = a.getString(0);
        if (this.mDialogTitle == null) {
            this.mDialogTitle = getTitle();
        }
        this.mDialogMessage = a.getString(1);
        this.mDialogIcon = a.getDrawable(2);
        this.mPositiveButtonText = a.getString(3);
        this.mNegativeButtonText = a.getString(4);
        this.mDialogLayoutResId = a.getResourceId(5, this.mDialogLayoutResId);
        a.recycle();
    }

    public DialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842897);
    }

    public DialogPreference(Context context) {
        this(context, null);
    }

    public void setDialogTitle(CharSequence dialogTitle) {
        this.mDialogTitle = dialogTitle;
    }

    public void setDialogTitle(int dialogTitleResId) {
        setDialogTitle((CharSequence) getContext().getString(dialogTitleResId));
    }

    public CharSequence getDialogTitle() {
        return this.mDialogTitle;
    }

    public void setDialogMessage(CharSequence dialogMessage) {
        this.mDialogMessage = dialogMessage;
    }

    public void setDialogMessage(int dialogMessageResId) {
        setDialogMessage((CharSequence) getContext().getString(dialogMessageResId));
    }

    public CharSequence getDialogMessage() {
        return this.mDialogMessage;
    }

    public void setDialogIcon(Drawable dialogIcon) {
        this.mDialogIcon = dialogIcon;
    }

    public void setDialogIcon(int dialogIconRes) {
        this.mDialogIcon = getContext().getDrawable(dialogIconRes);
    }

    public Drawable getDialogIcon() {
        return this.mDialogIcon;
    }

    public void setPositiveButtonText(CharSequence positiveButtonText) {
        this.mPositiveButtonText = positiveButtonText;
    }

    public void setPositiveButtonText(int positiveButtonTextResId) {
        setPositiveButtonText((CharSequence) getContext().getString(positiveButtonTextResId));
    }

    public CharSequence getPositiveButtonText() {
        return this.mPositiveButtonText;
    }

    public void setNegativeButtonText(CharSequence negativeButtonText) {
        this.mNegativeButtonText = negativeButtonText;
    }

    public void setNegativeButtonText(int negativeButtonTextResId) {
        setNegativeButtonText((CharSequence) getContext().getString(negativeButtonTextResId));
    }

    public CharSequence getNegativeButtonText() {
        return this.mNegativeButtonText;
    }

    public void setDialogLayoutResource(int dialogLayoutResId) {
        this.mDialogLayoutResId = dialogLayoutResId;
    }

    public int getDialogLayoutResource() {
        return this.mDialogLayoutResId;
    }

    /* access modifiers changed from: protected */
    public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
    }

    /* access modifiers changed from: protected */
    public void onClick() {
        if (this.mDialog == null || !this.mDialog.isShowing()) {
            showDialog(null);
        }
    }

    /* access modifiers changed from: protected */
    public void showDialog(Bundle state) {
        Context context = getContext();
        this.mWhichButtonClicked = -2;
        this.mBuilder = new AlertDialog.Builder(context).setTitle(this.mDialogTitle).setIcon(this.mDialogIcon).setPositiveButton(this.mPositiveButtonText, this).setNegativeButton(this.mNegativeButtonText, this);
        View contentView = onCreateDialogView();
        if (contentView != null) {
            onBindDialogView(contentView);
            this.mBuilder.setView(contentView);
        } else {
            this.mBuilder.setMessage(this.mDialogMessage);
        }
        onPrepareDialogBuilder(this.mBuilder);
        getPreferenceManager().registerOnActivityDestroyListener(this);
        Dialog dialog = this.mBuilder.create();
        this.mDialog = dialog;
        if (state != null) {
            dialog.onRestoreInstanceState(state);
        }
        if (needInputMethod()) {
            requestInputMethod(dialog);
        }
        dialog.setOnDismissListener(this);
        dialog.show();
    }

    /* access modifiers changed from: protected */
    public boolean needInputMethod() {
        return false;
    }

    private void requestInputMethod(Dialog dialog) {
        dialog.getWindow().setSoftInputMode(5);
    }

    /* access modifiers changed from: protected */
    public View onCreateDialogView() {
        if (this.mDialogLayoutResId == 0) {
            return null;
        }
        return LayoutInflater.from(this.mBuilder.getContext()).inflate(this.mDialogLayoutResId, (ViewGroup) null);
    }

    /* access modifiers changed from: protected */
    public void onBindDialogView(View view) {
        View dialogMessageView = view.findViewById(16908299);
        if (dialogMessageView != null) {
            CharSequence message = getDialogMessage();
            int newVisibility = 8;
            if (!TextUtils.isEmpty(message)) {
                if (dialogMessageView instanceof TextView) {
                    ((TextView) dialogMessageView).setText(message);
                }
                newVisibility = 0;
            }
            if (dialogMessageView.getVisibility() != newVisibility) {
                dialogMessageView.setVisibility(newVisibility);
            }
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        this.mWhichButtonClicked = which;
    }

    public void onDismiss(DialogInterface dialog) {
        getPreferenceManager().unregisterOnActivityDestroyListener(this);
        this.mDialog = null;
        onDialogClosed(this.mWhichButtonClicked == -1);
    }

    /* access modifiers changed from: protected */
    public void onDialogClosed(boolean positiveResult) {
    }

    public Dialog getDialog() {
        return this.mDialog;
    }

    public void onActivityDestroy() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (this.mDialog == null || !this.mDialog.isShowing()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.isDialogShowing = true;
        myState.dialogBundle = this.mDialog.onSaveInstanceState();
        return myState;
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (myState.isDialogShowing) {
            showDialog(myState.dialogBundle);
        }
    }
}
