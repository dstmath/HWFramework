package ohos.agp.window.dialog;

import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.colors.RgbPalette;
import ohos.agp.components.Component;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.ScrollView;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.utils.Point;
import ohos.agp.window.service.Display;
import ohos.agp.window.service.DisplayManager;
import ohos.agp.window.service.WindowManager;
import ohos.agp.window.wmc.AGPBaseDialogWindow;
import ohos.agp.window.wmc.AGPWindow;
import ohos.agp.window.wmc.AGPWindowManager;
import ohos.app.Context;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class BaseDialog implements IDialog {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "BaseDialog");
    private boolean isTransparent;
    private boolean isTransparentChange;
    protected boolean isUserSetSize;
    private boolean mAutoClosable;
    protected DirectionalLayout mButtonLayout;
    protected Component mContentLayout;
    protected Context mContext;
    private float mCornerRadius;
    private boolean mCreated;
    private int mDelayTimeMs;
    protected int mDeviceHeight;
    protected int mDeviceWidth;
    private DialogListener mDialogListener;
    protected int mFlag;
    private int mGravity;
    protected int mHeight;
    protected DirectionalLayout mLayout;
    protected AGPWindow.LayoutParams mParam;
    private boolean mShowing;
    private TaskDispatcher mTaskDispatcher;
    protected DirectionalLayout mTitleLayout;
    protected int mWidth;
    protected AGPBaseDialogWindow mWindow;
    private AGPWindowManager mWindowManager;
    private int mX;
    private int mY;

    public interface DialogListener {
        boolean isTouchOutside();
    }

    /* access modifiers changed from: protected */
    public void onCreate() {
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
    }

    /* access modifiers changed from: protected */
    public void onHide() {
    }

    static {
        AGPWindowManager.getInstance();
    }

    public BaseDialog(Context context) {
        this(context, 2);
    }

    public BaseDialog(Context context, int i) {
        HiLog.debug(LABEL, "new BaseDialog", new Object[0]);
        if (context != null) {
            this.mContext = context;
            this.mTaskDispatcher = context.getUITaskDispatcher();
            this.mFlag = i;
            this.isUserSetSize = false;
            getDisplaySize(context);
            return;
        }
        throw new WindowManager.PermissionException("BaseDialog context is null.");
    }

    private void getDisplaySize(Context context) {
        Optional<Display> defaultDisplay = DisplayManager.getInstance().getDefaultDisplay(context);
        if (!defaultDisplay.isPresent()) {
            HiLog.error(LABEL, "CommonDialog optDisplay is empty.", new Object[0]);
            return;
        }
        Point point = new Point();
        defaultDisplay.get().getSize(point);
        this.mDeviceWidth = (int) point.position[0];
        this.mDeviceHeight = (int) point.position[1];
    }

    @Override // ohos.agp.window.dialog.IDialog
    public void show() {
        HiLog.debug(LABEL, "show", new Object[0]);
        if (!this.mCreated) {
            create();
        }
        if (this.mLayout == null || this.mWindow == null) {
            HiLog.error(LABEL, "show Content mLayout or mWindow be null", new Object[0]);
            return;
        }
        onShow();
        DirectionalLayout directionalLayout = this.mTitleLayout;
        if (directionalLayout != null) {
            this.mLayout.addComponent(directionalLayout);
        }
        Component component = this.mContentLayout;
        if (component != null) {
            this.mLayout.addComponent(component);
        } else {
            HiLog.error(LABEL, "BaseDialog Content shuoldn't be null", new Object[0]);
        }
        DirectionalLayout directionalLayout2 = this.mButtonLayout;
        if (directionalLayout2 != null) {
            this.mLayout.addComponent(directionalLayout2);
        }
        ScrollView scrollView = new ScrollView(this.mContext);
        if (this.mWidth == -1) {
            scrollView.setWidth(-1);
        } else {
            scrollView.setWidth(-2);
        }
        if (this.mHeight == -1) {
            scrollView.setHeight(-1);
        } else {
            scrollView.setHeight(-2);
        }
        scrollView.addComponent(this.mLayout);
        this.mWindow.setPreContentLayout(scrollView, this.mDeviceWidth, this.mDeviceHeight);
        onShowing();
        setLimitSize(scrollView);
        this.mWindow.setContentLayout(scrollView);
        if (this.mShowing) {
            HiLog.debug(LABEL, "BaseDialog setVisibility and return", new Object[0]);
            this.mLayout.setVisibility(0);
            return;
        }
        try {
            this.mWindow.show();
            this.mShowing = true;
            doTimeoutClose();
        } catch (WindowManager.PermissionException e) {
            throw new WindowManager.PermissionException("Permission denied: " + e.getLocalizedMessage());
        }
    }

    private void doTimeoutClose() {
        TaskDispatcher taskDispatcher;
        if (this.mDelayTimeMs > 0 && (taskDispatcher = this.mTaskDispatcher) != null) {
            taskDispatcher.delayDispatch(new Runnable() {
                /* class ohos.agp.window.dialog.BaseDialog.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    HiLog.debug(BaseDialog.LABEL, "BaseDialog duration timeout, dialog close", new Object[0]);
                    BaseDialog.this.destroy();
                }
            }, (long) this.mDelayTimeMs);
        }
    }

    private void setLimitSize(ScrollView scrollView) {
        if (this.mHeight > 0) {
            int height = this.mLayout.getHeight();
            if (height <= 0 || height >= this.mHeight) {
                scrollView.setHeight(this.mHeight);
            } else {
                scrollView.setHeight(height);
            }
        }
    }

    @Override // ohos.agp.window.dialog.IDialog
    public void hide() {
        HiLog.debug(LABEL, "hide", new Object[0]);
        onHide();
        DirectionalLayout directionalLayout = this.mLayout;
        if (directionalLayout == null) {
            HiLog.error(LABEL, "hide mLayout is null", new Object[0]);
        } else {
            directionalLayout.setVisibility(8);
        }
    }

    @Override // ohos.agp.window.dialog.IDialog
    public void destroy() {
        HiLog.debug(LABEL, "destroy", new Object[0]);
        if (this.mCreated) {
            onDestroy();
            AGPWindowManager aGPWindowManager = this.mWindowManager;
            if (aGPWindowManager != null) {
                aGPWindowManager.destroyWindow(this.mWindow);
                this.mWindowManager = null;
            }
            this.mWindow = null;
            this.mParam = null;
            this.mLayout = null;
            this.mCreated = false;
            this.mShowing = false;
        }
    }

    public boolean isShowing() {
        HiLog.debug(LABEL, "isShowing", new Object[0]);
        return this.mShowing;
    }

    public BaseDialog setTransparent(boolean z) {
        HiLog.debug(LABEL, "setTransparent isEnable = %{private}b", new Object[]{Boolean.valueOf(z)});
        if (this.isTransparent != z) {
            this.isTransparent = z;
            this.isTransparentChange = true;
        }
        return this;
    }

    public BaseDialog setCornerRadius(float f) {
        HiLog.debug(LABEL, "setCornerRadius radius = %{private}d", new Object[]{Float.valueOf(f)});
        this.mCornerRadius = f;
        return this;
    }

    public BaseDialog setAutoClosable(boolean z) {
        HiLog.debug(LABEL, "setAutoClosable closable = %{private}b", new Object[]{Boolean.valueOf(z)});
        this.mAutoClosable = z;
        return this;
    }

    public BaseDialog setDuration(int i) {
        HiLog.debug(LABEL, "setDuration ms = %{private}d", new Object[]{Integer.valueOf(i)});
        if (i > 0) {
            this.mDelayTimeMs = i;
        } else {
            HiLog.error(LABEL, "BaseDialog wrong duration, set failed", new Object[0]);
        }
        return this;
    }

    public BaseDialog setSize(int i, int i2) {
        HiLog.debug(LABEL, "setSize width = %{private}d; height = %{private}d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
        if (i < -2 || i == 0 || i2 < -2 || i2 == 0) {
            HiLog.error(LABEL, "setSize() Invalied size.", new Object[0]);
            return this;
        }
        this.mWidth = i;
        this.mHeight = i2;
        this.isUserSetSize = true;
        return this;
    }

    public BaseDialog setGravity(int i) {
        HiLog.debug(LABEL, "setGravity value = %{private}d", new Object[]{Integer.valueOf(i)});
        this.mGravity = i;
        return this;
    }

    public BaseDialog setOffset(int i, int i2) {
        HiLog.debug(LABEL, "setOffset offsetX = %{private}d; offsetY = %{private}d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
        this.mX = i;
        this.mY = i2;
        return this;
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.mDialogListener = dialogListener;
    }

    /* access modifiers changed from: protected */
    public void create() {
        HiLog.debug(LABEL, "BaseDialog create", new Object[0]);
        if (!this.mCreated) {
            this.mWindowManager = AGPWindowManager.getInstance();
            AGPWindowManager aGPWindowManager = this.mWindowManager;
            if (aGPWindowManager == null) {
                HiLog.error(LABEL, "BaseDialog mWindowManager is null", new Object[0]);
                return;
            }
            if (this.mWindow == null) {
                this.mWindow = aGPWindowManager.createDialogWindow(this.mContext, this.mFlag);
            }
            this.mWindow.setDialogListener(new AGPBaseDialogWindow.DialogListener() {
                /* class ohos.agp.window.dialog.BaseDialog.AnonymousClass2 */

                @Override // ohos.agp.window.wmc.AGPBaseDialogWindow.DialogListener
                public boolean isTouchOutside() {
                    if (BaseDialog.this.mDialogListener != null && BaseDialog.this.mDialogListener.isTouchOutside()) {
                        return true;
                    }
                    if (!BaseDialog.this.mAutoClosable || !BaseDialog.this.mShowing) {
                        return false;
                    }
                    BaseDialog.this.destroy();
                    return true;
                }
            });
            this.mWindow.setDialogDestoryListener(new AGPBaseDialogWindow.DialogDestoryListener() {
                /* class ohos.agp.window.dialog.BaseDialog.AnonymousClass3 */

                @Override // ohos.agp.window.wmc.AGPBaseDialogWindow.DialogDestoryListener
                public void dialogDestroy() {
                    BaseDialog.this.destroy();
                }
            });
            onCreate();
            this.mWindow.load();
            this.mParam = this.mWindow.getAttributes();
            if (this.mParam == null) {
                HiLog.error(LABEL, "BaseDialog window getAttributes return null", new Object[0]);
                return;
            }
            this.mLayout = new DirectionalLayout(this.mContext);
            this.mLayout.setWidth(-2);
            this.mLayout.setHeight(-2);
            setWindowTransparent();
            setWindowCornerRadius();
            this.mCreated = true;
        }
    }

    private void setWindowTransparent() {
        if (this.isTransparentChange) {
            ShapeElement shapeElement = new ShapeElement();
            if (this.isTransparent) {
                shapeElement.setRgbColor(RgbPalette.TRANSPARENT);
            } else {
                shapeElement.setRgbColor(RgbPalette.WHITE);
            }
            this.mLayout.setBackground(shapeElement);
            this.isTransparentChange = false;
            this.mWindow.setTransparent(this.isTransparent);
        }
    }

    private void setWindowCornerRadius() {
        if (this.mCornerRadius > 0.0f && !this.isTransparent) {
            if (this.mLayout == null || this.mWindow == null) {
                HiLog.error(LABEL, "setWindowCornerRadius failed for mLayout or mWindow is null", new Object[0]);
                return;
            }
            ShapeElement shapeElement = new ShapeElement();
            shapeElement.setRgbColor(RgbPalette.WHITE);
            shapeElement.setCornerRadius(this.mCornerRadius);
            this.mLayout.setBackground(shapeElement);
            this.mWindow.setTransparent(true);
        }
    }

    /* access modifiers changed from: protected */
    public void onShow() {
        DirectionalLayout directionalLayout;
        AGPWindow.LayoutParams layoutParams = this.mParam;
        boolean z = false;
        if (layoutParams == null || (directionalLayout = this.mLayout) == null || this.mWindow == null) {
            HiLog.error(LABEL, "onShow failed for mParam or mLayout or mWindow is null", new Object[0]);
            return;
        }
        int i = this.mWidth;
        if (i != 0) {
            layoutParams.width = i;
            directionalLayout.setWidth(layoutParams.width);
            z = true;
        }
        int i2 = this.mHeight;
        if (i2 != 0) {
            AGPWindow.LayoutParams layoutParams2 = this.mParam;
            layoutParams2.height = i2;
            this.mLayout.setHeight(layoutParams2.height);
            z = true;
        }
        if (!(this.mX == 0 && this.mY == 0)) {
            AGPWindow.LayoutParams layoutParams3 = this.mParam;
            layoutParams3.x = this.mX;
            layoutParams3.y = this.mY;
            z = true;
        }
        int i3 = this.mGravity;
        if (i3 != 0) {
            this.mParam.gravity = i3;
            z = true;
        }
        if (z) {
            this.mWindow.updateAttributes(this.mParam);
        }
    }

    /* access modifiers changed from: protected */
    public void onShowing() {
        DirectionalLayout directionalLayout = this.mLayout;
        if (directionalLayout == null || this.mWindow == null) {
            HiLog.error(LABEL, "onShowing failed for mLayout or mWindow is null", new Object[0]);
            return;
        }
        int height = directionalLayout.getHeight();
        int width = this.mLayout.getWidth();
        int i = this.mHeight;
        if (i > 0 && height > i) {
            height = i;
        }
        int i2 = this.mWidth;
        if (i2 > 0 && width > i2) {
            width = i2;
        }
        HiLog.debug(LABEL, "onShowing height = %d, width = %d", new Object[]{Integer.valueOf(height), Integer.valueOf(width)});
        this.mWindow.setDialogSize(width, height);
    }
}
