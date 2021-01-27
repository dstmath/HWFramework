package ohos.agp.window.wmc;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.multimodalinput.event.TouchEvent;
import ohos.multimodalinput.eventimpl.MultimodalEventFactory;

public class AGPPopupDialogWindow extends AGPBaseDialogWindow {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "AGPWindow");
    private View mDecor;

    public AGPPopupDialogWindow(Context context, int i) {
        super(context, i);
        this.mAndroidParam = new WindowManager.LayoutParams();
    }

    @Override // ohos.agp.window.wmc.AGPWindow
    public void show() {
        this.mAndroidParam.type = 1000;
        if (!(this.mAndroidContext instanceof Activity)) {
            HiLog.error(LABEL, "AGPPopupDialogWindow show failed due to wrong mAndroidContext", new Object[0]);
            return;
        }
        View view = null;
        if (((Activity) this.mAndroidContext).getWindow() != null) {
            view = ((Activity) this.mAndroidContext).getWindow().getDecorView();
        }
        if (view != null) {
            this.mAndroidParam.token = view.getWindowToken();
            HiLog.debug(LABEL, "AGPPopupDialogWindow get parent content view sucessfully", new Object[0]);
        } else {
            this.mAndroidParam.token = this.mSurfaceView.getWindowToken();
            HiLog.debug(LABEL, "AGPPopupDialogWindow get parent content view failed, use own surfaceview", new Object[0]);
        }
        if (this.mAndroidParam.token == null) {
            HiLog.debug(LABEL, "mAndroidParam.token is null.", new Object[0]);
            return;
        }
        this.mAndroidParam.format = -3;
        PopupDecorView popupDecorView = new PopupDecorView(this.mAndroidContext);
        popupDecorView.addView(this.mSurfaceView, this.mAndroidParam.width, this.mAndroidParam.height);
        popupDecorView.setClipChildren(false);
        popupDecorView.setClipToPadding(false);
        this.mDecor = popupDecorView;
        this.mAndroidWindowManager.addView(this.mDecor, this.mAndroidParam);
        super.show();
    }

    @Override // ohos.agp.window.wmc.AGPWindow
    public void hide() {
        View view = this.mDecor;
        if (view != null) {
            view.setVisibility(8);
        }
        super.hide();
    }

    @Override // ohos.agp.window.wmc.AGPWindow
    public void destroy() {
        if (this.mDecor != null) {
            this.mAndroidWindowManager.removeView(this.mDecor);
        }
        super.destroy();
    }

    private class PopupDecorView extends FrameLayout {
        public PopupDecorView(android.content.Context context) {
            super(context);
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            if (motionEvent == null) {
                HiLog.error(AGPPopupDialogWindow.LABEL, "onTouchEvent event is null", new Object[0]);
                return false;
            }
            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();
            if (motionEvent.getAction() != 4 && (motionEvent.getAction() != 0 || (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()))) {
                Optional createEvent = MultimodalEventFactory.createEvent(motionEvent);
                if (createEvent.isPresent()) {
                    TouchEvent touchEvent = (MultimodalEvent) createEvent.get();
                    if (touchEvent instanceof TouchEvent) {
                        return AGPPopupDialogWindow.this.dispatchTouchEventFromDialog(touchEvent);
                    }
                }
                return false;
            } else if (AGPPopupDialogWindow.this.mListener == null) {
                HiLog.error(AGPPopupDialogWindow.LABEL, "AGPPopupDialogWindow onTouchEvent return false due to mListener is null", new Object[0]);
                return false;
            } else {
                AGPPopupDialogWindow.this.mListener.isTouchOutside();
                return true;
            }
        }

        @Override // android.view.View, android.view.ViewGroup
        public boolean dispatchKeyEvent(KeyEvent keyEvent) {
            if (keyEvent == null) {
                HiLog.error(AGPPopupDialogWindow.LABEL, "dispatchKeyEvent event is null", new Object[0]);
                return false;
            } else if (keyEvent.getKeyCode() != 4) {
                return super.dispatchKeyEvent(keyEvent);
            } else {
                AGPPopupDialogWindow.this.destroy();
                return true;
            }
        }
    }
}
