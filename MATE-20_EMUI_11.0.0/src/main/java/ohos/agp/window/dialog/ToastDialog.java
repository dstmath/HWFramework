package ohos.agp.window.dialog;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.Component;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.Text;
import ohos.agp.window.wmc.AGPToastWindow;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ToastDialog extends CommonDialog {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "ToastDialog");
    private static final int TEXT_SIZE = 60;
    private static final int WIDTH_BASE = 3;
    private static final int WIDTH_SUM = 4;
    private int mToastDuration;
    private int mToastGravity;
    private int mToastX;
    private int mToastY;

    public ToastDialog(Context context) {
        super(context);
        this.mFlag = 5;
        if (this.mDeviceWidth > 0) {
            this.mWidth = (this.mDeviceWidth * 3) / 4;
        }
        this.mHeight = -2;
    }

    public ToastDialog setText(String str) {
        HiLog.debug(LABEL, "setText text = %{private}s", new Object[]{str});
        Text text = new Text(this.mContext);
        text.setText(str);
        text.setContentPosition(0.0f, 0.0f);
        text.setTextSize(60);
        text.setTextAlignment(72);
        text.setWidth(-1);
        text.setHeight(-2);
        setContentCustomComponent(text);
        setCornerRadius(15.0f);
        return this;
    }

    public ToastDialog setComponent(DirectionalLayout directionalLayout) {
        HiLog.debug(LABEL, "setView enter", new Object[0]);
        setContentCustomComponent(directionalLayout);
        return this;
    }

    public Component getComponent() {
        HiLog.debug(LABEL, "getView", new Object[0]);
        return getContentCustomComponent();
    }

    public void cancel() {
        HiLog.debug(LABEL, "cancel toast", new Object[0]);
        if (this.mWindow != null && (this.mWindow instanceof AGPToastWindow)) {
            ((AGPToastWindow) this.mWindow).cancel();
        }
    }

    @Override // ohos.agp.window.dialog.BaseDialog
    public ToastDialog setGravity(int i) {
        super.setGravity(i);
        this.mToastGravity = i;
        return this;
    }

    @Override // ohos.agp.window.dialog.BaseDialog
    public ToastDialog setOffset(int i, int i2) {
        super.setOffset(i, i2);
        this.mToastX = i;
        this.mToastY = i2;
        return this;
    }

    @Override // ohos.agp.window.dialog.BaseDialog
    public ToastDialog setSize(int i, int i2) {
        if (i < -2 || i == 0 || i2 < -2 || i2 == 0) {
            HiLog.error(LABEL, "setSize() Invalied size.", new Object[0]);
            return this;
        }
        this.mWidth = i;
        this.mHeight = i2;
        this.isUserSetSize = true;
        return this;
    }

    @Override // ohos.agp.window.dialog.BaseDialog
    public BaseDialog setDuration(int i) {
        HiLog.debug(LABEL, "setDuration ms = %{private}d", new Object[]{Integer.valueOf(i)});
        if (i > 0) {
            super.setDuration(i);
            this.mToastDuration = i;
        }
        return this;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.window.dialog.CommonDialog, ohos.agp.window.dialog.BaseDialog
    public void onCreate() {
        if (this.mWindow != null && (this.mWindow instanceof AGPToastWindow)) {
            ((AGPToastWindow) this.mWindow).setDefaultSize();
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.window.dialog.CommonDialog, ohos.agp.window.dialog.BaseDialog
    public void onShow() {
        super.onShow();
        if (this.mWindow != null && (this.mWindow instanceof AGPToastWindow)) {
            if (!(this.mToastX == 0 && this.mToastY == 0)) {
                int i = this.mToastX;
                ((AGPToastWindow) this.mWindow).setOffset(i, i);
            }
            if (this.mToastDuration != 0) {
                ((AGPToastWindow) this.mWindow).setDuration(this.mToastDuration);
            }
            if (this.mToastGravity != 0) {
                ((AGPToastWindow) this.mWindow).setGravity(this.mToastGravity);
            }
            if (this.isUserSetSize) {
                ((AGPToastWindow) this.mWindow).setSize(this.mWidth, this.mHeight);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.window.dialog.CommonDialog, ohos.agp.window.dialog.BaseDialog
    public void onDestroy() {
        super.onDestroy();
        cancel();
    }
}
