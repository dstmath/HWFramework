package ohos.agp.window.wmc;

import android.app.INotificationManager;
import android.app.ITransientNotification;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.View;
import android.view.WindowManager;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.window.wmc.AGPWindowManager;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AGPToastWindow extends AGPBaseDialogWindow {
    private static final int DEFAULT_HEIGHT = 100;
    private static final int DEFAULT_WIDTH = 700;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "AGPToastWindow");
    private static INotificationManager sService;
    private boolean isCustomized;
    private int mDuration = 4000;
    private final ToastNotification mToastNotification = new ToastNotification(this.mAndroidContext.getPackageName(), null);

    public AGPToastWindow(Context context, int i) {
        super(context, i);
        this.mAndroidParam = this.mToastNotification.mParams;
        this.isCustomized = false;
    }

    public void setDuration(int i) {
        HiLog.debug(LABEL, "setDuration duration = %{private}d", new Object[]{Integer.valueOf(i)});
        this.mToastNotification.mDuration = i;
        this.mDuration = i;
    }

    public int getDuration() {
        HiLog.debug(LABEL, "getDuration", new Object[0]);
        return this.mToastNotification.mDuration;
    }

    public void setDefaultSize() {
        HiLog.debug(LABEL, "setDefaultSize", new Object[0]);
        this.mAndroidParam.width = 700;
        this.mAndroidParam.height = 100;
    }

    public void setSize(int i, int i2) {
        HiLog.debug(LABEL, "enter toast setSize", new Object[0]);
        this.mToastNotification.mWidth = i;
        this.mToastNotification.mHeight = i2;
        this.isCustomized = true;
    }

    @Override // ohos.agp.window.wmc.AGPBaseDialogWindow
    public void setDialogSize(int i, int i2) {
        HiLog.debug(LABEL, "enter toast setDialogSize", new Object[0]);
        if (!this.isCustomized) {
            this.mToastNotification.mWidth = i;
            this.mToastNotification.mHeight = i2;
        }
    }

    @Override // ohos.agp.window.wmc.AGPWindow
    public void show() {
        HiLog.debug(LABEL, "show", new Object[0]);
        INotificationManager service = getService();
        String packageName = this.mAndroidContext.getPackageName();
        this.mToastNotification.mNextView = this.mSurfaceView;
        this.mToastNotification.mWM = this.mAndroidWindowManager;
        this.mToastNotification.mService = service;
        try {
            service.enqueueToast(packageName, this.mToastNotification, this.mDuration, this.mAndroidWindowManager.getDefaultDisplay().getDisplayId());
        } catch (RemoteException unused) {
            throw new AGPWindowManager.BadWindowException("Can't enqueueToast toast");
        }
    }

    public void cancel() {
        HiLog.debug(LABEL, "cancel", new Object[0]);
        this.mToastNotification.cancel();
    }

    public void setGravity(int i) {
        HiLog.debug(LABEL, "setGravity value = %{private}d", new Object[]{Integer.valueOf(i)});
        this.mToastNotification.mGravity = AGPWindowManager.getAndroidGravity(i);
    }

    public void setOffset(int i, int i2) {
        HiLog.debug(LABEL, "setOffset offsetX = %{private}d, offsetY = %{private}d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
        this.mToastNotification.mX = i;
        this.mToastNotification.mY = i2;
    }

    private static INotificationManager getService() {
        INotificationManager iNotificationManager = sService;
        if (iNotificationManager != null) {
            return iNotificationManager;
        }
        sService = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        return sService;
    }

    private static class ToastNotification extends ITransientNotification.Stub {
        private static final int CANCEL = 2;
        private static final int DEFAULT_DURATION_TIMEOUT = 4000;
        private static final int HIDE = 1;
        private static final int SHOW = 0;
        private int mDuration;
        private int mGravity;
        private final Handler mHandler;
        private int mHeight;
        private View mNextView;
        private String mPackageName;
        private WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
        private INotificationManager mService;
        private View mView;
        private WindowManager mWM;
        private int mWidth;
        private int mX;
        private int mY;

        ToastNotification(String str, Looper looper) {
            initParams();
            this.mPackageName = str;
            if (looper == null && (looper = Looper.myLooper()) == null) {
                throw new AGPWindowManager.BadWindowException("Can't show toast, Looper.don't prepare");
            }
            this.mHandler = new Handler(looper, null) {
                /* class ohos.agp.window.wmc.AGPToastWindow.ToastNotification.AnonymousClass1 */

                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    int i = message.what;
                    if (i != 0) {
                        if (i == 1) {
                            ToastNotification.this.handleHide();
                            ToastNotification.this.mNextView = null;
                        } else if (i == 2) {
                            ToastNotification.this.handleHide();
                            ToastNotification.this.mNextView = null;
                            try {
                                ToastNotification.this.mService.cancelToast(ToastNotification.this.mPackageName, ToastNotification.this);
                            } catch (RemoteException unused) {
                                throw new AGPWindowManager.BadWindowException("Can't cancel toast");
                            }
                        }
                    } else if (message.obj instanceof IBinder) {
                        ToastNotification.this.doToastShow((IBinder) message.obj);
                    }
                }
            };
        }

        private void initParams() {
            WindowManager.LayoutParams layoutParams = this.mParams;
            layoutParams.height = -2;
            layoutParams.width = -2;
            layoutParams.format = -3;
            layoutParams.windowAnimations = 16973828;
            layoutParams.type = 2005;
            layoutParams.setTitle("Toast");
            this.mParams.flags = 152;
            this.mDuration = DEFAULT_DURATION_TIMEOUT;
            this.mWM = null;
            this.mX = 0;
            this.mY = 0;
            this.mWidth = 0;
            this.mHeight = 0;
            this.mGravity = 80;
        }

        public void show(IBinder iBinder) {
            this.mHandler.obtainMessage(0, iBinder).sendToTarget();
        }

        public void hide() {
            this.mHandler.obtainMessage(1).sendToTarget();
        }

        public void cancel() {
            this.mHandler.obtainMessage(2).sendToTarget();
        }

        public void doToastShow(IBinder iBinder) {
            View view;
            if (!this.mHandler.hasMessages(2) && !this.mHandler.hasMessages(1) && (view = this.mNextView) != null && this.mWM != null && !view.equals(this.mView)) {
                handleHide();
                this.mView = this.mNextView;
                WindowManager.LayoutParams layoutParams = this.mParams;
                layoutParams.packageName = this.mPackageName;
                layoutParams.token = iBinder;
                layoutParams.gravity = this.mGravity;
                layoutParams.x = this.mX;
                layoutParams.y = this.mY;
                int i = this.mWidth;
                if (i != 0) {
                    layoutParams.width = i;
                } else {
                    layoutParams.width = 700;
                }
                int i2 = this.mHeight;
                if (i2 != 0) {
                    this.mParams.height = i2;
                } else {
                    this.mParams.height = 100;
                }
                this.mParams.hideTimeoutMilliseconds = (long) this.mDuration;
                if (this.mView.getParent() != null) {
                    this.mWM.removeView(this.mView);
                }
                this.mWM.addView(this.mView, this.mParams);
            }
        }

        public void handleHide() {
            WindowManager windowManager;
            View view = this.mView;
            if (view != null) {
                if (!(view.getParent() == null || (windowManager = this.mWM) == null)) {
                    windowManager.removeViewImmediate(this.mView);
                }
                this.mView = null;
            }
        }
    }
}
