package ohos.agp.window.dialog;

import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.Component;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.utils.Rect;
import ohos.agp.window.dialog.IDialog;
import ohos.agp.window.service.Window;
import ohos.agp.window.service.WindowManager;
import ohos.agp.window.wmc.AGPCommonDialogWindow;
import ohos.agp.window.wmc.AGPWindow;
import ohos.agp.window.wmc.AGPWindowManager;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.TouchEvent;

public class CommonDialog extends BaseDialog {
    private static final float BASE_WEIGHT = 1.0f;
    private static final int CONTENT_SIZE = 50;
    private static final float DEFAULT_DIM = 0.5f;
    private static final float DEF_CONTENT_WEIGHT = 2.0f;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "CommonDialog");
    private static final int SUB_TITLE_SIZE = 40;
    private static final int TITLE_SIZE = 60;
    private Rect boundRect;
    private CommonDialogAttribute mComAttribute;
    private DestroyedListener mDestroyedListener;
    private IDialog mDialog;
    private IDialog.KeyboardCallback mKeyboardCallback;
    private boolean mRemovable = true;
    private boolean movable;

    public interface DestroyedListener {
        void onDestroy();
    }

    public boolean clickKeyDown(KeyEvent keyEvent) {
        return false;
    }

    public boolean dealTouchEvent(TouchEvent touchEvent) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void onWindowConfigUpdated(WindowManager.LayoutConfig layoutConfig) {
    }

    /* access modifiers changed from: protected */
    public void onWindowSelectionUpdated(boolean z) {
    }

    public CommonDialog(Context context) {
        super(context);
        createAttribute();
        this.mDialog = this;
        if (this.mDeviceWidth > 0) {
            this.mWidth = this.mDeviceWidth;
        }
        this.mHeight = -2;
        setCornerRadius(15.0f);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.window.dialog.BaseDialog
    public void create() {
        super.create();
        if (this.mWindow instanceof AGPCommonDialogWindow) {
            AGPCommonDialogWindow aGPCommonDialogWindow = (AGPCommonDialogWindow) this.mWindow;
            aGPCommonDialogWindow.registerOnWindowParamUpdatedCallback(new AGPCommonDialogWindow.OnWindowParamUpdatedCallback() {
                /* class ohos.agp.window.dialog.CommonDialog.AnonymousClass1 */

                @Override // ohos.agp.window.wmc.AGPCommonDialogWindow.OnWindowParamUpdatedCallback
                public void onWindowParamUpdated(AGPWindow.LayoutParams layoutParams) {
                    CommonDialog.this.onWindowConfigUpdated(CommonDialog.this.transferLayoutConfig(layoutParams));
                }
            });
            aGPCommonDialogWindow.registerOnWindowSelectionUpdatedCallback(new AGPCommonDialogWindow.OnWindowSelectionUpdatedCallback() {
                /* class ohos.agp.window.dialog.CommonDialog.AnonymousClass2 */

                @Override // ohos.agp.window.wmc.AGPCommonDialogWindow.OnWindowSelectionUpdatedCallback
                public void onAGPWindowSelectionUpdated(boolean z) {
                    CommonDialog.this.onWindowSelectionUpdated(z);
                }
            });
            aGPCommonDialogWindow.setKeyEventListener(new AGPCommonDialogWindow.EventListener() {
                /* class ohos.agp.window.dialog.CommonDialog.AnonymousClass3 */

                @Override // ohos.agp.window.wmc.AGPCommonDialogWindow.EventListener
                public boolean touchProcess(TouchEvent touchEvent) {
                    return false;
                }

                @Override // ohos.agp.window.wmc.AGPCommonDialogWindow.EventListener
                public boolean keyProcess(KeyEvent keyEvent) {
                    return CommonDialog.this.deliverKeyboardCase(keyEvent);
                }
            });
            aGPCommonDialogWindow.setTouchEventListener(new AGPCommonDialogWindow.EventListener() {
                /* class ohos.agp.window.dialog.CommonDialog.AnonymousClass4 */

                @Override // ohos.agp.window.wmc.AGPCommonDialogWindow.EventListener
                public boolean keyProcess(KeyEvent keyEvent) {
                    return false;
                }

                @Override // ohos.agp.window.wmc.AGPCommonDialogWindow.EventListener
                public boolean touchProcess(TouchEvent touchEvent) {
                    return CommonDialog.this.deliverTouchCase(touchEvent);
                }
            });
        }
        this.mParam.dimAmount = DEFAULT_DIM;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private WindowManager.LayoutConfig transferLayoutConfig(AGPWindow.LayoutParams layoutParams) {
        WindowManager.LayoutConfig layoutConfig = new WindowManager.LayoutConfig();
        layoutConfig.alpha = layoutParams.alpha;
        layoutConfig.dim = layoutParams.dimAmount;
        layoutConfig.windowBrightness = layoutParams.screenBrightness;
        layoutConfig.alignment = layoutParams.gravity;
        layoutConfig.height = layoutParams.height;
        layoutConfig.width = layoutParams.width;
        layoutConfig.type = layoutParams.type;
        layoutConfig.x = layoutParams.x;
        layoutConfig.y = layoutParams.y;
        layoutConfig.flags = layoutParams.flags;
        layoutConfig.title = layoutParams.title;
        layoutConfig.animations = layoutParams.windowAnimations;
        return layoutConfig;
    }

    public boolean isMovable() {
        return this.movable;
    }

    public void setMovable(boolean z) {
        this.movable = z;
    }

    public Rect getBoundRect() {
        return this.boundRect;
    }

    public void setBoundRect(Rect rect) {
        this.boundRect = rect;
    }

    public CommonDialog setTitleIcon(int i, int i2) {
        HiLog.debug(LABEL, "setTitleIcon", new Object[0]);
        if (i2 < 0 || i2 > 2) {
            HiLog.error(LABEL, "CommonDialog iconId is wrong", new Object[0]);
            return this;
        }
        this.mComAttribute.titleIconResid[i2] = i;
        if (this.mComAttribute.titleIconView[i2] != null) {
            this.mComAttribute.titleIconView[i2].setPixelMap(i);
        }
        this.mComAttribute.titleCustomView = null;
        return this;
    }

    public CommonDialog setTitleText(String str) {
        HiLog.debug(LABEL, "setTitleText text = %{private}s", new Object[]{str});
        CommonDialogAttribute commonDialogAttribute = this.mComAttribute;
        commonDialogAttribute.titleText = str;
        if (commonDialogAttribute.titleTextComponent != null) {
            this.mComAttribute.titleTextComponent.setText(str);
            this.mComAttribute.titleTextComponent.setTextSize(60);
            this.mComAttribute.titleTextComponent.setTextAlignment(72);
        }
        this.mComAttribute.titleCustomView = null;
        return this;
    }

    public CommonDialog setTitleSubText(String str) {
        HiLog.debug(LABEL, "setTitleSubText text = %{private}s", new Object[]{str});
        CommonDialogAttribute commonDialogAttribute = this.mComAttribute;
        commonDialogAttribute.titleSubText = str;
        if (commonDialogAttribute.titleSubTextComponent != null) {
            this.mComAttribute.titleSubTextComponent.setText(str);
            this.mComAttribute.titleSubTextComponent.setTextSize(40);
            this.mComAttribute.titleSubTextComponent.setTextAlignment(72);
        }
        this.mComAttribute.titleCustomView = null;
        return this;
    }

    public CommonDialog setTitleCustomComponent(DirectionalLayout directionalLayout) {
        HiLog.debug(LABEL, "setTitleCustomComponent", new Object[0]);
        this.mComAttribute.titleCustomView = directionalLayout;
        return this;
    }

    public CommonDialog setContentImage(int i) {
        HiLog.debug(LABEL, "setContentImage", new Object[0]);
        CommonDialogAttribute commonDialogAttribute = this.mComAttribute;
        commonDialogAttribute.contentImageResid = i;
        if (commonDialogAttribute.contentImage != null) {
            this.mComAttribute.contentImage.setPixelMap(i);
        }
        this.mComAttribute.contentCustomView = null;
        return this;
    }

    public CommonDialog setContentText(String str) {
        HiLog.debug(LABEL, "setContentText", new Object[0]);
        CommonDialogAttribute commonDialogAttribute = this.mComAttribute;
        commonDialogAttribute.contentText = str;
        if (commonDialogAttribute.contentTextComponent != null) {
            this.mComAttribute.contentTextComponent.setText(str);
            this.mComAttribute.contentTextComponent.setTextSize(50);
            this.mComAttribute.contentTextComponent.setTextAlignment(72);
            this.mComAttribute.contentTextComponent.setMultipleLine(true);
        }
        this.mComAttribute.contentCustomView = null;
        return this;
    }

    public CommonDialog setContentCustomComponent(Component component) {
        HiLog.debug(LABEL, "setContentCustomComponent", new Object[0]);
        this.mComAttribute.contentCustomView = component;
        return this;
    }

    public Component getContentCustomComponent() {
        HiLog.debug(LABEL, "getContentCustomComponent", new Object[0]);
        return this.mComAttribute.contentCustomView;
    }

    public Component getButtonComponent() {
        HiLog.debug(LABEL, "getButtonComponent", new Object[0]);
        return this.mButtonLayout;
    }

    public Window getWindow() {
        if (this.mWindow == null) {
            this.mWindow = AGPWindowManager.getInstance().createDialogWindow(this.mContext, this.mFlag);
        }
        return new Window(this.mWindow);
    }

    public CommonDialog setButton(int i, String str, IDialog.ClickedListener clickedListener) {
        HiLog.debug(LABEL, "setButton", new Object[0]);
        if (i >= 0 && i <= 2) {
            this.mComAttribute.buttonTexts[i] = str;
            this.mComAttribute.buttonListener.put(Integer.valueOf(i), clickedListener);
        }
        return this;
    }

    public CommonDialog setImageButton(int i, int i2, IDialog.ClickedListener clickedListener) {
        HiLog.debug(LABEL, "setImageButton", new Object[0]);
        if (i >= 0 && i <= 2) {
            this.mComAttribute.imageButtonResid[i] = i2;
            this.mComAttribute.buttonListener.put(Integer.valueOf(i), clickedListener);
        }
        return this;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.window.dialog.BaseDialog
    public void onCreate() {
        if (this.isUserSetSize || this.mWindow == null || !(this.mWindow instanceof AGPCommonDialogWindow)) {
            HiLog.debug(LABEL, "CommonDialog not use default param.", new Object[0]);
        } else {
            ((AGPCommonDialogWindow) this.mWindow).setDefaultLayoutParam();
        }
        if (this.mWindow != null) {
            this.mWindow.setMovable(this.movable);
            this.mWindow.setBoundRect(this.boundRect);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.window.dialog.BaseDialog
    public void onShow() {
        super.onShow();
        Optional<DirectionalLayout> optional = this.mComAttribute.setupTitlePanel();
        if (optional.isPresent()) {
            this.mTitleLayout = optional.get();
        }
        Optional<DirectionalLayout> optional2 = this.mComAttribute.setupButtonPanel();
        int i = 0;
        if (optional2.isPresent()) {
            while (i < 3) {
                if (this.mComAttribute.buttonViews[i] != null) {
                    this.mComAttribute.buttonViews[i].setClickedListener(new ButtonClickListener());
                }
                i++;
            }
            this.mButtonLayout = optional2.get();
        } else {
            Optional<DirectionalLayout> optional3 = this.mComAttribute.setupImageButtonPanel();
            if (optional3.isPresent()) {
                while (i < 3) {
                    if (this.mComAttribute.imageButtonViews[i] != null) {
                        this.mComAttribute.imageButtonViews[i].setClickedListener(new ButtonClickListener());
                    }
                    i++;
                }
                this.mButtonLayout = optional3.get();
            }
        }
        Optional<Component> optional4 = this.mComAttribute.setupContentPanel();
        if (optional4.isPresent()) {
            this.mContentLayout = optional4.get();
        }
    }

    private void createAttribute() {
        if (this.mComAttribute == null) {
            this.mComAttribute = new CommonDialogAttribute(this.mContext);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.window.dialog.BaseDialog
    public void onDestroy() {
        super.onDestroy();
        DestroyedListener destroyedListener = this.mDestroyedListener;
        if (destroyedListener != null) {
            destroyedListener.onDestroy();
        }
    }

    public void setDestroyedListener(DestroyedListener destroyedListener) {
        this.mDestroyedListener = destroyedListener;
    }

    public boolean setSwipeToDismiss(boolean z) {
        if (this.mWindow instanceof AGPCommonDialogWindow) {
            return ((AGPCommonDialogWindow) this.mWindow).setSwipeToDismiss(z, new AGPWindow.OnDismissListener() {
                /* class ohos.agp.window.dialog.CommonDialog.AnonymousClass5 */

                @Override // ohos.agp.window.wmc.AGPWindow.OnDismissListener
                public void onDismissed() {
                    CommonDialog.this.destroy();
                }
            });
        }
        HiLog.warn(LABEL, "CommonDialog setSwipeToDismiss failed because window invalid.", new Object[0]);
        return false;
    }

    public boolean deliverKeyboardCase(KeyEvent keyEvent) {
        IDialog.KeyboardCallback keyboardCallback = this.mKeyboardCallback;
        if (keyboardCallback != null && keyboardCallback.clickKey(this, keyEvent)) {
            return true;
        }
        if (this.mWindow == null) {
            HiLog.error(LABEL, "deliver keyboard case mWindow is null", new Object[0]);
            return false;
        } else if (!this.mWindow.dispatchKeyboardEvent(keyEvent) && !clickKeyDown(keyEvent) && !clickKeyUp(keyEvent)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean clickKeyUp(KeyEvent keyEvent) {
        if (!isShowing() || keyEvent.getKeyCode() != 2070) {
            return false;
        }
        dealBackKeyDown();
        return true;
    }

    public boolean deliverTouchCase(TouchEvent touchEvent) {
        if (this.mWindow == null) {
            HiLog.error(LABEL, "deliver touch case mWindow is null", new Object[0]);
            return false;
        } else if (this.mWindow.dispatchTouchEventFromDialog(touchEvent)) {
            return true;
        } else {
            return dealTouchEvent(touchEvent);
        }
    }

    public void siteKeyboardCallback(IDialog.KeyboardCallback keyboardCallback) {
        this.mKeyboardCallback = keyboardCallback;
    }

    public void siteRemovable(boolean z) {
        this.mRemovable = z;
    }

    public void dealBackKeyDown() {
        if (this.mRemovable) {
            onDestroy();
        }
    }

    private class ButtonClickListener implements Component.ClickedListener {
        private ButtonClickListener() {
        }

        private void onSelect(int i) {
            IDialog.ClickedListener clickedListener;
            if (CommonDialog.this.mComAttribute.buttonListener != null && (clickedListener = CommonDialog.this.mComAttribute.buttonListener.get(Integer.valueOf(i))) != null) {
                clickedListener.onClick(CommonDialog.this.mDialog, i);
            }
        }

        @Override // ohos.agp.components.Component.ClickedListener
        public void onClick(Component component) {
            for (int i = 0; i < 3 && component != null; i++) {
                if (component.equals(CommonDialog.this.mComAttribute.buttonViews[i]) || component.equals(CommonDialog.this.mComAttribute.imageButtonViews[i])) {
                    onSelect(i);
                    return;
                }
            }
        }
    }
}
