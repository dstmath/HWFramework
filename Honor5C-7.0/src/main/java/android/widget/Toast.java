package android.widget;

import android.app.INotificationManager;
import android.app.ITransientNotification.Stub;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.hwcontrol.HwWidgetFactory;
import android.hwcontrol.HwWidgetFactory.HwToast;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.R;
import com.android.internal.telephony.AbstractRILConstants;
import com.huawei.pgmng.log.LogPower;

public class Toast {
    public static final int LENGTH_LONG = 1;
    public static final int LENGTH_SHORT = 0;
    static final String TAG = "Toast";
    static final boolean localLOGV = false;
    private static HwToast mHwToast;
    private static INotificationManager sService;
    final Context mContext;
    int mDuration;
    View mNextView;
    final TN mTN;
    private String mToastStr;

    private static class TN extends Stub {
        static final long LONG_DURATION_TIMEOUT = 1000;
        static final long SHORT_DURATION_TIMEOUT = 5000;
        int mDuration;
        int mGravity;
        final Handler mHandler;
        final Runnable mHide;
        float mHorizontalMargin;
        View mNextView;
        private final LayoutParams mParams;
        final Runnable mShow;
        float mVerticalMargin;
        View mView;
        WindowManager mWM;
        int mX;
        int mY;

        TN() {
            this.mShow = new Runnable() {
                public void run() {
                    TN.this.handleShow();
                }
            };
            this.mHide = new Runnable() {
                public void run() {
                    TN.this.handleHide();
                    TN.this.mNextView = null;
                }
            };
            this.mParams = new LayoutParams();
            this.mHandler = new Handler();
            LayoutParams params = this.mParams;
            params.height = -2;
            params.width = -2;
            params.format = -3;
            params.windowAnimations = R.style.Animation_Toast;
            params.type = AbstractRILConstants.RIL_REQUEST_HW_RESTRAT_RILD;
            params.setTitle(Toast.TAG);
            params.flags = LogPower.REMOVE_VIEW;
        }

        public void show() {
            this.mHandler.post(this.mShow);
        }

        public void hide() {
            if (!this.mHandler.post(this.mHide)) {
                String threadInfo = "NULL";
                if (this.mHandler.getLooper() != null) {
                    Thread t = this.mHandler.getLooper().getThread();
                    if (t != null) {
                        threadInfo = "ThreadID[" + t.getId() + "]:" + t.toString();
                    }
                }
                Log.w(Toast.TAG, "Toast post hide failed in " + threadInfo + ", try hide immediate in " + ("ThreadID[" + Thread.currentThread().getId() + "]:" + Thread.currentThread().toString()));
                hideImmediate();
            }
        }

        public void handleShow() {
            if (this.mView != this.mNextView) {
                handleHide();
                this.mView = this.mNextView;
                Context context = this.mView.getContext().getApplicationContext();
                String packageName = this.mView.getContext().getOpPackageName();
                if (context == null) {
                    context = this.mView.getContext();
                }
                this.mWM = (WindowManager) context.getSystemService("window");
                int gravity = Gravity.getAbsoluteGravity(this.mGravity, this.mView.getContext().getResources().getConfiguration().getLayoutDirection());
                this.mParams.gravity = gravity;
                if ((gravity & 7) == 7) {
                    this.mParams.horizontalWeight = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
                }
                if ((gravity & LogPower.APP_PROCESS_EXIT) == LogPower.APP_PROCESS_EXIT) {
                    this.mParams.verticalWeight = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
                }
                this.mParams.x = this.mX;
                this.mParams.y = this.mY;
                this.mParams.verticalMargin = this.mVerticalMargin;
                this.mParams.horizontalMargin = this.mHorizontalMargin;
                this.mParams.packageName = packageName;
                this.mParams.removeTimeoutMilliseconds = this.mDuration == Toast.LENGTH_LONG ? LONG_DURATION_TIMEOUT : SHORT_DURATION_TIMEOUT;
                if (this.mView.getParent() != null) {
                    this.mWM.removeView(this.mView);
                }
                this.mWM.addView(this.mView, this.mParams);
                trySendAccessibilityEvent();
            }
        }

        private void trySendAccessibilityEvent() {
            AccessibilityManager accessibilityManager = AccessibilityManager.getInstance(this.mView.getContext());
            if (accessibilityManager.isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(64);
                event.setClassName(getClass().getName());
                event.setPackageName(this.mView.getContext().getPackageName());
                this.mView.dispatchPopulateAccessibilityEvent(event);
                accessibilityManager.sendAccessibilityEvent(event);
            }
        }

        public void handleHide() {
            if (this.mView != null) {
                if (this.mView.getParent() != null) {
                    this.mWM.removeView(this.mView);
                }
                this.mView = null;
            }
        }

        public void hideImmediate() {
            Log.w(Toast.TAG, "HIDE IMMEDIATE: " + this + " mView=" + this.mView);
            if (this.mView != null && this.mWM != null) {
                if (this.mView.getParent() != null) {
                    Log.w(Toast.TAG, "REMOVE IMMEDIATE: " + this.mView + " in " + this);
                    try {
                        this.mView.setTouchInOtherThread(true);
                        this.mWM.removeViewImmediate(this.mView);
                    } catch (Exception ex) {
                        Log.w(Toast.TAG, ex.getMessage(), ex);
                    }
                }
                this.mView = null;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.Toast.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.Toast.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.Toast.<clinit>():void");
    }

    public Toast(Context context) {
        this.mContext = context;
        this.mTN = new TN();
        this.mTN.mY = context.getResources().getDimensionPixelSize(R.dimen.toast_y_offset);
        this.mTN.mGravity = context.getResources().getInteger(R.integer.config_toastDefaultGravity);
    }

    public void show() {
        if (this.mNextView == null) {
            throw new RuntimeException("setView must have been called");
        }
        INotificationManager service = getService();
        String pkg = this.mContext.getOpPackageName();
        TN tn = this.mTN;
        tn.mNextView = this.mNextView;
        try {
            service.enqueueToastEx(pkg, tn, this.mDuration, this.mToastStr);
        } catch (RemoteException e) {
        }
    }

    public void cancel() {
        this.mTN.hide();
        try {
            getService().cancelToast(this.mContext.getPackageName(), this.mTN);
        } catch (RemoteException e) {
        }
    }

    public void setView(View view) {
        this.mNextView = view;
    }

    public View getView() {
        return this.mNextView;
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
        this.mTN.mDuration = duration;
    }

    public int getDuration() {
        return this.mDuration;
    }

    public void setMargin(float horizontalMargin, float verticalMargin) {
        this.mTN.mHorizontalMargin = horizontalMargin;
        this.mTN.mVerticalMargin = verticalMargin;
    }

    public float getHorizontalMargin() {
        return this.mTN.mHorizontalMargin;
    }

    public float getVerticalMargin() {
        return this.mTN.mVerticalMargin;
    }

    public void setGravity(int gravity, int xOffset, int yOffset) {
        this.mTN.mGravity = gravity;
        this.mTN.mX = xOffset;
        this.mTN.mY = yOffset;
    }

    public int getGravity() {
        return this.mTN.mGravity;
    }

    public int getXOffset() {
        return this.mTN.mX;
    }

    public int getYOffset() {
        return this.mTN.mY;
    }

    public LayoutParams getWindowParams() {
        return this.mTN.mParams;
    }

    public static Toast makeText(Context context, CharSequence text, int duration) {
        View v;
        Toast result = new Toast(context);
        LayoutInflater inflate = (LayoutInflater) context.getSystemService("layout_inflater");
        mHwToast = HwWidgetFactory.getHwToast(context, result, null);
        if (mHwToast != null) {
            v = mHwToast.layoutInflate(context);
        } else {
            v = inflate.inflate((int) R.layout.transient_notification, null);
        }
        ((TextView) v.findViewById(R.id.message)).setText(text);
        result.mToastStr = text == null ? "" : text.toString();
        result.mNextView = v;
        result.mDuration = duration;
        return result;
    }

    public static Toast makeText(Context context, int resId, int duration) throws NotFoundException {
        return makeText(context, context.getResources().getText(resId), duration);
    }

    public void setText(int resId) {
        setText(this.mContext.getText(resId));
    }

    public void setText(CharSequence s) {
        if (this.mNextView == null) {
            throw new RuntimeException("This Toast was not created with Toast.makeText()");
        }
        TextView tv = (TextView) this.mNextView.findViewById(R.id.message);
        if (tv == null) {
            throw new RuntimeException("This Toast was not created with Toast.makeText()");
        }
        String str;
        tv.setText(s);
        if (s == null) {
            str = "";
        } else {
            str = s.toString();
        }
        this.mToastStr = str;
    }

    private static INotificationManager getService() {
        if (sService != null) {
            return sService;
        }
        sService = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        return sService;
    }
}
