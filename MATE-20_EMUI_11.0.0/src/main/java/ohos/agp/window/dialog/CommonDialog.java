package ohos.agp.window.dialog;

import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.Component;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.utils.Rect;
import ohos.agp.window.dialog.IDialog;
import ohos.agp.window.service.Window;
import ohos.agp.window.wmc.AGPCommonDialogWindow;
import ohos.agp.window.wmc.AGPWindowManager;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class CommonDialog extends BaseDialog {
    private static final float BASE_WEIGHT = 1.0f;
    private static final int CONTENT_SIZE = 50;
    private static final float DEF_CONTENT_WEIGHT = 2.0f;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "CommonDialog");
    private static final int SUB_TITLE_SIZE = 40;
    private static final int TITLE_SIZE = 60;
    private Rect boundRect;
    private CommonDialogAttribute mComAttribute;
    private DestroyedListener mDestroyedListener;
    private IDialog mDialog = this;
    private boolean movable;

    public interface DestroyedListener {
        void onDestroy();
    }

    public CommonDialog(Context context) {
        super(context);
        createAttribute();
        if (this.mDeviceWidth > 0) {
            this.mWidth = this.mDeviceWidth;
        }
        this.mHeight = -2;
        setCornerRadius(15.0f);
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

    public CommonDialog setTitleIcon(String str, int i) {
        HiLog.debug(LABEL, "setTitleIcon", new Object[0]);
        if (i < 0 || i > 2) {
            HiLog.error(LABEL, "CommonDialog iconId is wrong", new Object[0]);
            return this;
        }
        this.mComAttribute.titleIconUri[i] = str;
        if (this.mComAttribute.titleIconView[i] != null) {
            this.mComAttribute.titleIconView[i].setImageURI(str);
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

    public CommonDialog setTitleCustomView(DirectionalLayout directionalLayout) {
        HiLog.debug(LABEL, "setTitleCustomView", new Object[0]);
        this.mComAttribute.titleCustomView = directionalLayout;
        return this;
    }

    public CommonDialog setContentImage(String str) {
        HiLog.debug(LABEL, "setContentImage", new Object[0]);
        CommonDialogAttribute commonDialogAttribute = this.mComAttribute;
        commonDialogAttribute.contentImageUri = str;
        if (commonDialogAttribute.contentImage != null) {
            this.mComAttribute.contentImage.setImageURI(str);
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
        HiLog.debug(LABEL, "setContentCustomView", new Object[0]);
        this.mComAttribute.contentCustomView = component;
        return this;
    }

    public Component getContentCustomComponent() {
        HiLog.debug(LABEL, "getContentCustomView", new Object[0]);
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

    public CommonDialog setImageButton(int i, String str, IDialog.ClickedListener clickedListener) {
        HiLog.debug(LABEL, "setImageButton", new Object[0]);
        if (i >= 0 && i <= 2) {
            this.mComAttribute.imageButtonUri[i] = str;
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
