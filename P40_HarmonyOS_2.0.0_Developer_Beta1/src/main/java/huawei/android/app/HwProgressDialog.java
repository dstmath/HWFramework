package huawei.android.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import huawei.android.widget.HwWidgetUtils;
import huawei.android.widget.loader.ResLoaderUtil;
import java.text.NumberFormat;

public class HwProgressDialog extends AlertDialog {
    public static final int STYLE_HORIZONTAL = 1;
    public static final int STYLE_SPINNER = 0;
    private View mCancel;
    private int mIncrementBy;
    private int mIncrementSecondaryBy;
    private Drawable mIndeterminateDrawable;
    private boolean mIsHasStarted;
    private boolean mIsIndeterminate;
    private int mMax;
    private CharSequence mMessage;
    private TextView mMessageView;
    private ProgressBar mProgress;
    private Drawable mProgressDrawable;
    private TextView mProgressNumber;
    private String mProgressNumberFormat;
    private TextView mProgressPercent;
    private NumberFormat mProgressPercentFormat;
    private int mProgressStyle = 0;
    private int mProgressVal;
    private int mSecondaryProgressVal;
    private View mSpace;
    private Handler mViewUpdateHandler;

    public HwProgressDialog(Context context) {
        super(context);
        initFormats();
    }

    public HwProgressDialog(Context context, int theme) {
        super(context, theme);
        initFormats();
    }

    private void initFormats() {
        this.mProgressNumberFormat = "%1d/%2d";
        this.mProgressPercentFormat = NumberFormat.getPercentInstance();
        this.mProgressPercentFormat.setMaximumFractionDigits(0);
    }

    public static HwProgressDialog show(Context context, CharSequence title, CharSequence message) {
        return show(context, title, message, false);
    }

    public static HwProgressDialog show(Context context, CharSequence title, CharSequence message, boolean isIndeterminate) {
        return show(context, title, message, isIndeterminate, false, null);
    }

    public static HwProgressDialog show(Context context, CharSequence title, CharSequence message, boolean isIndeterminate, boolean isCancelable) {
        return show(context, title, message, isIndeterminate, isCancelable, null);
    }

    public static HwProgressDialog show(Context context, CharSequence title, CharSequence message, boolean isIndeterminate, boolean isCancelable, DialogInterface.OnCancelListener cancelListener) {
        HwProgressDialog dialog = new HwProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIndeterminate(isIndeterminate);
        dialog.setCancelable(isCancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
        return dialog;
    }

    /* access modifiers changed from: protected */
    @Override // android.app.AlertDialog, android.app.Dialog
    public void onCreate(Bundle savedInstanceState) {
        View view;
        Context context = getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        Resources resources = context.getResources();
        TypedArray typedArray = getContext().obtainStyledAttributes(null, HwWidgetUtils.getResourceDeclareStyleableIntArray("android", "AlertDialog"), resources.getIdentifier("alertDialogStyle", "attr", "android"), 0);
        if (this.mProgressStyle == 1) {
            initHandler();
            if (HwWidgetUtils.isHwTheme(getContext())) {
                view = inflater.inflate(34013221, (ViewGroup) null);
                this.mMessageView = (TextView) view.findViewById(34603129);
                this.mProgress = (ProgressBar) view.findViewById(34603130);
                this.mProgressPercent = (TextView) view.findViewById(34603131);
                this.mCancel = view.findViewById(34603132);
                this.mSpace = view.findViewById(34603133);
                view.setTag(this);
            } else {
                View view2 = inflater.inflate(typedArray.getResourceId(HwWidgetUtils.getResourceDeclareStyleableIndex("com.android.internal", "AlertDialog_horizontalProgressLayout"), resources.getIdentifier("alert_dialog_progress", ResLoaderUtil.LAYOUT, "android")), (ViewGroup) null);
                this.mProgress = (ProgressBar) view2.findViewById(resources.getIdentifier("progress", ResLoaderUtil.ID, "android"));
                this.mProgressNumber = (TextView) view2.findViewById(resources.getIdentifier("progress_number", ResLoaderUtil.ID, "android"));
                this.mProgressPercent = (TextView) view2.findViewById(resources.getIdentifier("progress_percent", ResLoaderUtil.ID, "android"));
                view = view2;
            }
            setView(view);
        } else {
            View view3 = inflater.inflate(typedArray.getResourceId(HwWidgetUtils.getResourceDeclareStyleableIndex("com.android.internal", "AlertDialog_progressLayout"), resources.getIdentifier("progress_dialog", ResLoaderUtil.LAYOUT, "android")), (ViewGroup) null);
            this.mProgress = (ProgressBar) view3.findViewById(resources.getIdentifier("progress", ResLoaderUtil.ID, "android"));
            this.mMessageView = (TextView) view3.findViewById(resources.getIdentifier("message", ResLoaderUtil.ID, "android"));
            setView(view3);
        }
        typedArray.recycle();
        initData();
        onProgressChanged();
        super.onCreate(savedInstanceState);
    }

    private void initHandler() {
        this.mViewUpdateHandler = new Handler() {
            /* class huawei.android.app.HwProgressDialog.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int progress = HwProgressDialog.this.mProgress.getProgress();
                int max = HwProgressDialog.this.mProgress.getMax();
                if (!HwWidgetUtils.isHwTheme(HwProgressDialog.this.getContext())) {
                    if (HwProgressDialog.this.mProgressNumberFormat != null) {
                        HwProgressDialog.this.mProgressNumber.setText(String.format(HwProgressDialog.this.mProgressNumberFormat, Integer.valueOf(progress), Integer.valueOf(max)));
                    } else {
                        HwProgressDialog.this.mProgressNumber.setText("");
                    }
                }
                if (HwProgressDialog.this.mProgressPercentFormat == null || max == 0) {
                    HwProgressDialog.this.mProgressPercent.setText("");
                    return;
                }
                SpannableString percentStr = new SpannableString(HwProgressDialog.this.mProgressPercentFormat.format(((double) progress) / ((double) max)));
                percentStr.setSpan(new StyleSpan(1), 0, percentStr.length(), 33);
                HwProgressDialog.this.mProgressPercent.setText(percentStr);
            }
        };
    }

    private void initData() {
        int i = this.mMax;
        if (i > 0) {
            setMax(i);
        }
        int i2 = this.mProgressVal;
        if (i2 > 0) {
            setProgress(i2);
        }
        int i3 = this.mSecondaryProgressVal;
        if (i3 > 0) {
            setSecondaryProgress(i3);
        }
        int i4 = this.mIncrementBy;
        if (i4 > 0) {
            incrementProgressBy(i4);
        }
        int i5 = this.mIncrementSecondaryBy;
        if (i5 > 0) {
            incrementSecondaryProgressBy(i5);
        }
        Drawable drawable = this.mProgressDrawable;
        if (drawable != null) {
            setProgressDrawable(drawable);
        }
        Drawable drawable2 = this.mIndeterminateDrawable;
        if (drawable2 != null) {
            setIndeterminateDrawable(drawable2);
        }
        CharSequence charSequence = this.mMessage;
        if (charSequence != null) {
            setMessage(charSequence);
        }
        setIndeterminate(this.mIsIndeterminate);
    }

    @Override // android.app.Dialog
    public void onStart() {
        View view;
        super.onStart();
        if (containsButtons() && (view = this.mSpace) != null) {
            view.setVisibility(8);
        }
        this.mIsHasStarted = true;
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Dialog
    public void onStop() {
        super.onStop();
        this.mIsHasStarted = false;
    }

    public void setProgress(int value) {
        ProgressBar progressBar;
        if (!this.mIsHasStarted || (progressBar = this.mProgress) == null) {
            this.mProgressVal = value;
            return;
        }
        progressBar.setProgress(value);
        onProgressChanged();
    }

    public void setSecondaryProgress(int secondaryProgress) {
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            progressBar.setSecondaryProgress(secondaryProgress);
            onProgressChanged();
            return;
        }
        this.mSecondaryProgressVal = secondaryProgress;
    }

    public int getProgress() {
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            return progressBar.getProgress();
        }
        return this.mProgressVal;
    }

    public int getSecondaryProgress() {
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            return progressBar.getSecondaryProgress();
        }
        return this.mSecondaryProgressVal;
    }

    public int getMax() {
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            return progressBar.getMax();
        }
        return this.mMax;
    }

    public void setMax(int max) {
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            progressBar.setMax(max);
            onProgressChanged();
            return;
        }
        this.mMax = max;
    }

    public void incrementProgressBy(int diff) {
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            progressBar.incrementProgressBy(diff);
            onProgressChanged();
            return;
        }
        this.mIncrementBy += diff;
    }

    public void incrementSecondaryProgressBy(int diff) {
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            progressBar.incrementSecondaryProgressBy(diff);
            onProgressChanged();
            return;
        }
        this.mIncrementSecondaryBy += diff;
    }

    public void setProgressDrawable(Drawable drawable) {
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            progressBar.setProgressDrawable(drawable);
        } else {
            this.mProgressDrawable = drawable;
        }
    }

    public void setIndeterminateDrawable(Drawable drawable) {
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            progressBar.setIndeterminateDrawable(drawable);
        } else {
            this.mIndeterminateDrawable = drawable;
        }
    }

    public void setIndeterminate(boolean isIndeterminate) {
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            progressBar.setIndeterminate(isIndeterminate);
        } else {
            this.mIsIndeterminate = isIndeterminate;
        }
    }

    public boolean isIndeterminate() {
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            return progressBar.isIndeterminate();
        }
        return this.mIsIndeterminate;
    }

    @Override // android.app.AlertDialog
    public void setMessage(CharSequence message) {
        if (this.mProgress == null) {
            this.mMessage = message;
        } else if (this.mProgressStyle != 1) {
            this.mMessageView.setText(message);
        } else if (HwWidgetUtils.isHwTheme(getContext())) {
            this.mMessageView.setText(message);
        } else {
            super.setMessage(message);
        }
    }

    public void setProgressStyle(int style) {
        this.mProgressStyle = style;
    }

    public int getProgressStyle() {
        return this.mProgressStyle;
    }

    public View getCancelButton() {
        return this.mCancel;
    }

    public void disableCancelButton() {
        this.mCancel.setVisibility(8);
    }

    public void setProgressNumberFormat(String format) {
        this.mProgressNumberFormat = format;
        onProgressChanged();
    }

    public void setProgressPercentFormat(NumberFormat format) {
        this.mProgressPercentFormat = format;
        onProgressChanged();
    }

    private void onProgressChanged() {
        Handler handler;
        if (this.mProgressStyle == 1 && (handler = this.mViewUpdateHandler) != null && !handler.hasMessages(0)) {
            this.mViewUpdateHandler.sendEmptyMessage(0);
        }
    }
}
