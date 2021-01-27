package android.app;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.DialogInterface;
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
import com.android.internal.R;
import java.text.NumberFormat;

@Deprecated
public class ProgressDialog extends AlertDialog {
    public static final int STYLE_HORIZONTAL = 1;
    public static final int STYLE_SPINNER = 0;
    private boolean mHasStarted;
    private int mIncrementBy;
    private int mIncrementSecondaryBy;
    private boolean mIndeterminate;
    private Drawable mIndeterminateDrawable;
    private int mMax;
    private CharSequence mMessage;
    @UnsupportedAppUsage
    private TextView mMessageView;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private ProgressBar mProgress;
    private Drawable mProgressDrawable;
    @UnsupportedAppUsage
    private TextView mProgressNumber;
    private String mProgressNumberFormat;
    private TextView mProgressPercent;
    private NumberFormat mProgressPercentFormat;
    private int mProgressStyle = 0;
    private int mProgressVal;
    private int mSecondaryProgressVal;
    private Handler mViewUpdateHandler;

    public ProgressDialog(Context context) {
        super(context);
        initFormats();
    }

    public ProgressDialog(Context context, int theme) {
        super(context, theme);
        initFormats();
    }

    private void initFormats() {
        this.mProgressNumberFormat = "%1d/%2d";
        this.mProgressPercentFormat = NumberFormat.getPercentInstance();
        this.mProgressPercentFormat.setMaximumFractionDigits(0);
    }

    public static ProgressDialog show(Context context, CharSequence title, CharSequence message) {
        return show(context, title, message, false);
    }

    public static ProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static ProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static ProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
        return dialog;
    }

    /* access modifiers changed from: protected */
    @Override // android.app.AlertDialog, android.app.Dialog
    public void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(this.mContext);
        TypedArray a = this.mContext.obtainStyledAttributes(null, R.styleable.AlertDialog, 16842845, 0);
        if (this.mProgressStyle == 1) {
            this.mViewUpdateHandler = new Handler() {
                /* class android.app.ProgressDialog.AnonymousClass1 */

                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    int progress = ProgressDialog.this.mProgress.getProgress();
                    int max = ProgressDialog.this.mProgress.getMax();
                    if (ProgressDialog.this.mProgressNumberFormat != null) {
                        ProgressDialog.this.mProgressNumber.setText(String.format(ProgressDialog.this.mProgressNumberFormat, Integer.valueOf(progress), Integer.valueOf(max)));
                    } else {
                        ProgressDialog.this.mProgressNumber.setText("");
                    }
                    if (ProgressDialog.this.mProgressPercentFormat != null) {
                        SpannableString tmp = new SpannableString(ProgressDialog.this.mProgressPercentFormat.format(((double) progress) / ((double) max)));
                        tmp.setSpan(new StyleSpan(1), 0, tmp.length(), 33);
                        ProgressDialog.this.mProgressPercent.setText(tmp);
                        return;
                    }
                    ProgressDialog.this.mProgressPercent.setText("");
                }
            };
            View view = inflater.inflate(a.getResourceId(13, R.layout.alert_dialog_progress), (ViewGroup) null);
            this.mProgress = (ProgressBar) view.findViewById(16908301);
            this.mProgressNumber = (TextView) view.findViewById(R.id.progress_number);
            this.mProgressPercent = (TextView) view.findViewById(R.id.progress_percent);
            setView(view);
        } else {
            View view2 = inflater.inflate(a.getResourceId(18, R.layout.progress_dialog), (ViewGroup) null);
            this.mProgress = (ProgressBar) view2.findViewById(16908301);
            this.mMessageView = (TextView) view2.findViewById(16908299);
            setView(view2);
        }
        a.recycle();
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
        setIndeterminate(this.mIndeterminate);
        onProgressChanged();
        super.onCreate(savedInstanceState);
    }

    @Override // android.app.Dialog
    public void onStart() {
        super.onStart();
        this.mHasStarted = true;
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Dialog
    public void onStop() {
        super.onStop();
        this.mHasStarted = false;
    }

    public void setProgress(int value) {
        if (this.mHasStarted) {
            this.mProgress.setProgress(value);
            onProgressChanged();
            return;
        }
        this.mProgressVal = value;
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

    public void setProgressDrawable(Drawable d) {
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            progressBar.setProgressDrawable(d);
        } else {
            this.mProgressDrawable = d;
        }
    }

    public void setIndeterminateDrawable(Drawable d) {
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            progressBar.setIndeterminateDrawable(d);
        } else {
            this.mIndeterminateDrawable = d;
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            progressBar.setIndeterminate(indeterminate);
        } else {
            this.mIndeterminate = indeterminate;
        }
    }

    public boolean isIndeterminate() {
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            return progressBar.isIndeterminate();
        }
        return this.mIndeterminate;
    }

    @Override // android.app.AlertDialog
    public void setMessage(CharSequence message) {
        if (this.mProgress == null) {
            this.mMessage = message;
        } else if (this.mProgressStyle == 1) {
            super.setMessage(message);
        } else {
            this.mMessageView.setText(message);
        }
    }

    public void setProgressStyle(int style) {
        this.mProgressStyle = style;
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
