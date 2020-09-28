package android.view;

import android.view.SurfaceControl;
import com.android.internal.annotations.VisibleForTesting;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.function.Supplier;

public class InsetsSourceConsumer {
    protected final InsetsController mController;
    private InsetsSourceControl mSourceControl;
    private final InsetsState mState;
    private final Supplier<SurfaceControl.Transaction> mTransactionSupplier;
    private final int mType;
    protected boolean mVisible;

    @Retention(RetentionPolicy.SOURCE)
    @interface ShowResult {
        public static final int SHOW_DELAYED = 1;
        public static final int SHOW_FAILED = 2;
        public static final int SHOW_IMMEDIATELY = 0;
    }

    public InsetsSourceConsumer(int type, InsetsState state, Supplier<SurfaceControl.Transaction> transactionSupplier, InsetsController controller) {
        this.mType = type;
        this.mState = state;
        this.mTransactionSupplier = transactionSupplier;
        this.mController = controller;
        this.mVisible = InsetsState.getDefaultVisibility(type);
    }

    public void setControl(InsetsSourceControl control) {
        if (this.mSourceControl != control) {
            this.mSourceControl = control;
            applyHiddenToControl();
            if (applyLocalVisibilityOverride()) {
                this.mController.notifyVisibilityChanged();
            }
            if (this.mSourceControl == null) {
                this.mController.notifyControlRevoked(this);
            }
        }
    }

    @VisibleForTesting
    public InsetsSourceControl getControl() {
        return this.mSourceControl;
    }

    /* access modifiers changed from: package-private */
    public int getType() {
        return this.mType;
    }

    @VisibleForTesting
    public void show() {
        setVisible(true);
    }

    @VisibleForTesting
    public void hide() {
        setVisible(false);
    }

    public void onWindowFocusGained() {
    }

    public void onWindowFocusLost() {
    }

    /* access modifiers changed from: package-private */
    public boolean applyLocalVisibilityOverride() {
        if (this.mSourceControl == null || this.mState.getSource(this.mType).isVisible() == this.mVisible) {
            return false;
        }
        this.mState.getSource(this.mType).setVisible(this.mVisible);
        return true;
    }

    @VisibleForTesting
    public boolean isVisible() {
        return this.mVisible;
    }

    /* access modifiers changed from: package-private */
    public int requestShow(boolean fromController) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void notifyHidden() {
    }

    private void setVisible(boolean visible) {
        if (this.mVisible != visible) {
            this.mVisible = visible;
            applyHiddenToControl();
            applyLocalVisibilityOverride();
            this.mController.notifyVisibilityChanged();
        }
    }

    private void applyHiddenToControl() {
        if (this.mSourceControl != null) {
            SurfaceControl.Transaction t = this.mTransactionSupplier.get();
            if (this.mVisible) {
                t.show(this.mSourceControl.getLeash());
            } else {
                t.hide(this.mSourceControl.getLeash());
            }
            t.apply();
        }
    }
}
