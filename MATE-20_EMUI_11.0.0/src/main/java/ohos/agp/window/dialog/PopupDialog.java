package ohos.agp.window.dialog;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.Component;
import ohos.agp.components.Text;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class PopupDialog extends BaseDialog {
    private static final int FONT_SIZE = 50;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "PopupDialog");
    private static final int TITLE_OFFSET = 81;
    private int mGravity;
    private Component mMainView;
    private int mMode;
    private String mText;
    private int mViewHeight;
    private int mViewWidth;
    private int mViewX;
    private int mViewY;
    private int mX;
    private int mY;

    public PopupDialog(Context context, Component component, int i, int i2) {
        super(context);
        this.mFlag = 4;
        if (component != null) {
            this.mViewX = component.getLeft();
            this.mViewY = component.getTop();
            this.mViewWidth = component.getWidth();
            this.mViewHeight = component.getHeight();
        }
        if (i == 0) {
            this.mWidth = this.mDeviceWidth >> 1;
        } else {
            this.mWidth = i;
        }
        if (i2 == 0) {
            this.mHeight = -2;
        } else {
            this.mHeight = i2;
        }
        countDefaultMode();
    }

    private void countDefaultMode() {
        HiLog.debug(LABEL, "PopupDialog countDefaultMode", new Object[0]);
        int i = this.mViewX;
        int i2 = (i + i) + this.mViewWidth > this.mDeviceWidth ? 5 : 48;
        int i3 = this.mViewY;
        this.mMode = i2 | ((i3 + i3) + this.mViewHeight < this.mDeviceHeight ? 80 : 3);
    }

    private void countDefaultPostion() {
        HiLog.debug(LABEL, "PopupDialog countDefaultPostion", new Object[0]);
        if (this.mGravity != 0) {
            HiLog.debug(LABEL, "This is show at location mode.", new Object[0]);
            return;
        }
        int i = this.mMode;
        if ((i & 3) == 3) {
            this.mX = this.mViewX;
        } else if ((i & 5) == 5) {
            this.mX = (this.mViewX + this.mViewWidth) - this.mWidth;
        } else {
            this.mX = (this.mViewX + (this.mViewWidth >> 1)) - (this.mWidth >> 1);
        }
        if ((this.mMode & 48) == 48) {
            this.mY = (this.mViewY - this.mHeight) + 81;
        } else {
            this.mY = this.mViewY + this.mViewHeight + 81;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.window.dialog.BaseDialog
    public void onShow() {
        HiLog.debug(LABEL, "PopupDialog onShow", new Object[0]);
        if (this.mLayout == null || this.mParam == null || this.mWindow == null) {
            HiLog.error(LABEL, "onShow failed for mLayout or mParam or mWindow is null", new Object[0]);
            return;
        }
        if (this.mMainView == null) {
            if (this.mText != null) {
                this.mMainView = creatDefaltView();
            } else {
                HiLog.error(LABEL, "PopupDialog no view", new Object[0]);
                return;
            }
        }
        this.mLayout.addComponent(this.mMainView);
        if (this.mGravity == 0) {
            this.mParam.gravity = 51;
        } else {
            this.mParam.gravity = this.mGravity;
        }
        if (this.mWidth > 0 && this.mHeight > 0) {
            countDefaultPostion();
            this.mParam.width = this.mWidth;
            this.mParam.height = this.mHeight;
            this.mParam.x = this.mX;
            this.mParam.y = this.mY;
        }
        this.mWindow.updateAttributes(this.mParam);
        super.onShow();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.window.dialog.BaseDialog
    public void onShowing() {
        if (this.mLayout == null || this.mParam == null || this.mWindow == null) {
            HiLog.error(LABEL, "onShowing failed for mLayout or mParam or mWindow is null", new Object[0]);
            return;
        }
        int height = this.mLayout.getHeight();
        int width = this.mLayout.getWidth();
        if (this.mWidth <= 0 || this.mHeight <= 0) {
            if (this.mHeight <= 0) {
                this.mHeight = height;
            }
            if (this.mWidth <= 0) {
                this.mWidth = width;
            }
            countDefaultPostion();
            this.mParam.width = this.mWidth;
            this.mParam.height = height;
            this.mParam.x = this.mX;
            this.mParam.y = this.mY;
            this.mWindow.updateAttributes(this.mParam);
        }
    }

    public PopupDialog setMode(int i) {
        HiLog.debug(LABEL, "PopupDialog onShow", new Object[0]);
        this.mMode = i;
        return this;
    }

    public PopupDialog setText(String str) {
        HiLog.debug(LABEL, "PopupDialog setText", new Object[0]);
        this.mText = str;
        return this;
    }

    private Component creatDefaltView() {
        Text text = new Text(this.mContext);
        text.setLeft(0);
        text.setTop(0);
        text.setWidth(this.mWidth);
        if (this.mHeight > 0) {
            text.setHeight(this.mHeight);
        } else {
            text.setHeight(-2);
            this.mHeight = -2;
        }
        text.setTextSize(50);
        text.setText(this.mText);
        text.setTextAlignment(72);
        setCornerRadius(15.0f);
        return text;
    }

    public PopupDialog setCustomComponent(Component component) {
        HiLog.debug(LABEL, "PopupDialog setCustomView", new Object[0]);
        this.mMainView = component;
        return this;
    }

    public void showAtLocation(int i, int i2, int i3) {
        HiLog.debug(LABEL, "PopupDialog onShow", new Object[0]);
        this.mGravity = i;
        this.mX = i2;
        this.mY = i3;
        show();
    }
}
