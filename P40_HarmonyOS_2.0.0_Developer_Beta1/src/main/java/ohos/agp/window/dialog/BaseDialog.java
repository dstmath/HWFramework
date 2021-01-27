package ohos.agp.window.dialog;

import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.colors.RgbPalette;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
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
    private int mAlignment;
    private boolean mAutoClosable;
    protected DirectionalLayout mButtonLayout;
    protected Component mContentLayout;
    protected Context mContext;
    protected float mCornerRadius;
    private boolean mCreated;
    private int mDelayTimeMs;
    protected int mDeviceHeight;
    protected int mDeviceWidth;
    private DialogListener mDialogListener;
    private DisplayCallback mDisplayCallback;
    protected int mFlag;
    protected int mHeight;
    protected DirectionalLayout mLayout;
    protected AGPWindow.LayoutParams mParam;
    private RemoveCallback mRemoveCallback;
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

    public interface DisplayCallback {
        void onDisplay(IDialog iDialog);
    }

    public interface RemoveCallback {
        void onRemove(IDialog iDialog);
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
        if (!this.mCreated) {
            create();
        }
        if (this.mLayout == null || this.mWindow == null) {
            HiLog.error(LABEL, "show Content mLayout or mWindow be null", new Object[0]);
            return;
        }
        onShow();
        addSubLayout();
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
        this.mWindow.setPreContentLayout(this.mLayout, this.mDeviceWidth, this.mDeviceHeight);
        onShowing();
        setLimitSize(scrollView);
        this.mWindow.setContentLayout(scrollView);
        this.mWindow.setSize(this.mWidth, this.mHeight, this.isUserSetSize);
        if (this.mShowing) {
            HiLog.debug(LABEL, "BaseDialog setVisibility and return", new Object[0]);
            this.mLayout.setVisibility(0);
            return;
        }
        try {
            this.mWindow.show();
            callDisplayCallback();
            this.mShowing = true;
            doTimeoutClose();
        } catch (WindowManager.PermissionException e) {
            throw new WindowManager.PermissionException("Permission denied: " + e.getLocalizedMessage());
        }
    }

    private void callDisplayCallback() {
        DisplayCallback displayCallback = this.mDisplayCallback;
        if (displayCallback != null) {
            displayCallback.onDisplay(this);
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
        if (this.mWidth > 0) {
            int width = this.mLayout.getWidth();
            if (width <= 0 || width >= this.mWidth) {
                scrollView.setWidth(this.mWidth);
            } else {
                scrollView.setWidth(width);
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
            return;
        }
        directionalLayout.setVisibility(2);
        this.mWindow.hide();
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
        AGPBaseDialogWindow aGPBaseDialogWindow = this.mWindow;
        if (aGPBaseDialogWindow == null) {
            return this;
        }
        aGPBaseDialogWindow.setSize(i, i2, this.isUserSetSize);
        return this;
    }

    public BaseDialog setAlignment(int i) {
        HiLog.debug(LABEL, "setAlignment value = %{private}d", new Object[]{Integer.valueOf(i)});
        this.mAlignment = i;
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

    private void processLayoutConflicts() {
        Component component = this.mContentLayout;
        if (component != null) {
            ComponentContainer.LayoutConfig layoutConfig = component.getLayoutConfig();
            HiLog.debug(LABEL, "mContentLayout w=%{public}d,h=%{public}d", new Object[]{Integer.valueOf(layoutConfig.width), Integer.valueOf(layoutConfig.height)});
            if (layoutConfig.width == -1) {
                int i = this.mWidth;
                if (i <= 0) {
                    this.mWidth = -1;
                    i = -1;
                }
                int i2 = this.mWidth;
                int i3 = this.mDeviceWidth;
                if (i2 > i3) {
                    i = i3;
                }
                this.mLayout.setWidth(i);
            }
            if (layoutConfig.height == -1) {
                int i4 = this.mHeight;
                if (i4 <= 0) {
                    this.mHeight = -1;
                    i4 = -1;
                }
                int i5 = this.mHeight;
                int i6 = this.mDeviceHeight;
                if (i5 > i6) {
                    i4 = i6;
                }
                this.mLayout.setHeight(i4);
            }
        }
    }

    private void addSubLayout() {
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
        processLayoutConflicts();
        DirectionalLayout directionalLayout2 = this.mButtonLayout;
        if (directionalLayout2 != null) {
            this.mLayout.addComponent(directionalLayout2);
        }
    }

    /* access modifiers changed from: protected */
    public void onShow() {
        AGPWindow.LayoutParams layoutParams = this.mParam;
        boolean z = false;
        if (layoutParams == null || this.mLayout == null || this.mWindow == null) {
            HiLog.error(LABEL, "onShow failed for mParam or mLayout or mWindow is null", new Object[0]);
            return;
        }
        int i = this.mWidth;
        if (i != 0) {
            layoutParams.width = i;
            z = true;
        }
        int i2 = this.mWidth;
        if (i2 == -1) {
            this.mLayout.setWidth(i2);
        }
        int i3 = this.mHeight;
        if (i3 != 0) {
            this.mParam.height = i3;
            z = true;
        }
        int i4 = this.mHeight;
        if (i4 == -1) {
            this.mLayout.setHeight(i4);
        }
        if (!(this.mX == 0 && this.mY == 0)) {
            AGPWindow.LayoutParams layoutParams2 = this.mParam;
            layoutParams2.x = this.mX;
            layoutParams2.y = this.mY;
            z = true;
        }
        int i5 = this.mAlignment;
        if (i5 != 0) {
            this.mParam.gravity = i5;
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
        if (i > 0 && height < i) {
            height = this.mDeviceHeight;
            if (i <= height) {
                height = i;
            }
            this.mLayout.setHeight(height);
        }
        int i2 = this.mWidth;
        if (i2 > 0 && width < i2) {
            width = this.mDeviceWidth;
            if (i2 <= width) {
                width = i2;
            }
            this.mLayout.setWidth(width);
        }
        if (!(height == this.mLayout.getHeight() && width == this.mLayout.getWidth())) {
            HiLog.debug(LABEL, "Second PreContentLayout", new Object[0]);
            this.mWindow.setPreContentLayout(this.mLayout, this.mDeviceWidth, this.mDeviceHeight);
        }
        int i3 = this.mHeight;
        int i4 = this.mWidth;
        if (i3 <= 0) {
            i3 = this.mLayout.getHeight();
        }
        if (this.mWidth <= 0) {
            i4 = this.mLayout.getWidth();
        }
        HiLog.debug(LABEL, "onShowing winHeight = %{private}d, winWidth = %{private}d", new Object[]{Integer.valueOf(i3), Integer.valueOf(i4)});
        this.mWindow.setDialogSize(i4, i3);
    }

    public void registerRemoveCallback(RemoveCallback removeCallback) {
        this.mRemoveCallback = removeCallback;
    }

    @Override // ohos.agp.window.dialog.IDialog
    public void remove() {
        RemoveCallback removeCallback = this.mRemoveCallback;
        if (removeCallback != null) {
            removeCallback.onRemove(this);
        }
        destroy();
    }

    public void registerDisplayCallback(DisplayCallback displayCallback) {
        this.mDisplayCallback = displayCallback;
    }

    public Component searchComponentViaId(int i) {
        return this.mLayout.findComponentById(i);
    }

    public final Component obtainComponentViaId(int i) {
        Component findComponentById = this.mLayout.findComponentById(i);
        if (findComponentById != null) {
            return findComponentById;
        }
        throw new IllegalStateException("Component could not be found.");
    }
}
