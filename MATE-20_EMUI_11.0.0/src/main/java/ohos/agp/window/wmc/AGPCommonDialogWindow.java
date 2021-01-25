package ohos.agp.window.wmc;

import android.graphics.Point;
import android.view.ActionMode;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.policy.PhoneWindow;
import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.accessibility.AccessibilityEventInfo;
import ohos.accessibility.BarrierFreeInnerClient;
import ohos.agp.window.wmc.AGPWindow;
import ohos.agp.window.wmc.AGPWindowManager;
import ohos.app.Context;
import ohos.bundle.AbilityInfo;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.multimodalinput.event.TouchEvent;
import ohos.multimodalinput.eventimpl.MultimodalEventFactory;

public class AGPCommonDialogWindow extends AGPBaseDialogWindow implements Window.Callback {
    private static final float DEFAULT_DIM = 0.5f;
    private static final float DEFAULT_SCALE = 0.5f;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "AGPWindow");
    private static final int TOUCH_ERROR = -1;
    private static final int TOUCH_INSIDE = 1;
    private static final int TOUCH_OUTSIDE = 0;
    private boolean isKeyDownOut;
    protected View mDecor;
    private IComDialogPAEventLisener mIComdDialogPAListener;

    public interface IComDialogPAEventLisener {
        void onDispatch(AccessibilityEventInfo accessibilityEventInfo);
    }

    @Override // android.view.Window.Callback
    public boolean dispatchGenericMotionEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override // android.view.Window.Callback
    public boolean dispatchKeyShortcutEvent(KeyEvent keyEvent) {
        return false;
    }

    @Override // android.view.Window.Callback
    public boolean dispatchTrackballEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override // android.view.Window.Callback
    public void onActionModeFinished(ActionMode actionMode) {
    }

    @Override // android.view.Window.Callback
    public void onActionModeStarted(ActionMode actionMode) {
    }

    @Override // android.view.Window.Callback
    public void onAttachedToWindow() {
    }

    @Override // android.view.Window.Callback
    public void onContentChanged() {
    }

    @Override // android.view.Window.Callback
    public boolean onCreatePanelMenu(int i, Menu menu) {
        return false;
    }

    @Override // android.view.Window.Callback
    public View onCreatePanelView(int i) {
        return null;
    }

    @Override // android.view.Window.Callback
    public void onDetachedFromWindow() {
    }

    @Override // android.view.Window.Callback
    public boolean onMenuItemSelected(int i, MenuItem menuItem) {
        return false;
    }

    @Override // android.view.Window.Callback
    public boolean onMenuOpened(int i, Menu menu) {
        return false;
    }

    @Override // android.view.Window.Callback
    public void onPanelClosed(int i, Menu menu) {
    }

    @Override // android.view.Window.Callback
    public boolean onPreparePanel(int i, View view, Menu menu) {
        return false;
    }

    @Override // android.view.Window.Callback
    public boolean onSearchRequested() {
        return false;
    }

    @Override // android.view.Window.Callback
    public boolean onSearchRequested(SearchEvent searchEvent) {
        return false;
    }

    @Override // android.view.Window.Callback
    public void onWindowFocusChanged(boolean z) {
    }

    @Override // android.view.Window.Callback
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return null;
    }

    @Override // android.view.Window.Callback
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int i) {
        return null;
    }

    public AGPCommonDialogWindow(Context context, int i) {
        super(context, i);
        this.mAndroidWindow = new PhoneWindow(this.mAndroidContext);
        if (context.getAbilityInfo() == null || context.getAbilityInfo().getType() != AbilityInfo.AbilityType.PAGE) {
            HiLog.debug(LABEL, "AGPCommonDialogWindow construct func", new Object[0]);
            this.mAndroidWindow.setType(2038);
        }
        this.mAndroidWindow.requestFeature(1);
        this.mAndroidWindow.setContentView(this.mSurfaceView);
        this.mAndroidWindow.setCallback(this);
        this.mAndroidWindow.setWindowManager(this.mAndroidWindowManager, null, null);
        this.mAndroidParam = this.mAndroidWindow.getAttributes();
        this.mAndroidWindow.setGravity(17);
    }

    public void setDefaultLayoutParam() {
        if (this.mAndroidParam == null) {
            HiLog.error(LABEL, "setDefaultLayoutParam failed due to mAndroidParam is null", new Object[0]);
            return;
        }
        Display defaultDisplay = this.mAndroidWindowManager.getDefaultDisplay();
        Point point = new Point();
        if (defaultDisplay == null) {
            HiLog.error(LABEL, "setDefaultLayoutParam failed due to display is null", new Object[0]);
            return;
        }
        defaultDisplay.getSize(point);
        this.mAndroidParam.x = 0;
        this.mAndroidParam.y = 0;
        this.mAndroidParam.width = (int) (((float) point.x) * 0.5f);
        this.mAndroidParam.height = (int) (((float) point.y) * 0.5f);
    }

    @Override // ohos.agp.window.wmc.AGPWindow
    public void show() {
        if (this.mAndroidParam == null) {
            HiLog.error(LABEL, "show failed due to mAndroidParam is null", new Object[0]);
            return;
        }
        this.mDecor = this.mAndroidWindow.getDecorView();
        setPadding(0, 0, 0, 0);
        try {
            this.mAndroidParam.dimAmount = 0.5f;
            this.mAndroidWindowManager.addView(this.mDecor, this.mAndroidParam);
            this.mAndroidWindow.addFlags(2);
            super.show();
        } catch (WindowManager.BadTokenException e) {
            HiLog.error(LABEL, "AGPCommonDialogWindow show failed because permission denied", new Object[0]);
            throw new AGPWindowManager.BadWindowException("Permission denied" + e.getLocalizedMessage());
        }
    }

    @Override // ohos.agp.window.wmc.AGPWindow
    public void destroy() {
        if (this.mDecor != null) {
            HiLog.debug(LABEL, "AGPCommonDialogWindow removeView", new Object[0]);
            this.mAndroidWindowManager.removeView(this.mDecor);
        }
        super.destroy();
    }

    @Override // android.view.Window.Callback
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (keyEvent == null) {
            HiLog.debug(LABEL, "dispatchKeyEvent event is null", new Object[0]);
            return false;
        } else if (keyEvent.getKeyCode() != 4 || keyEvent.getAction() != 0) {
            return false;
        } else {
            if (this.mDialogDestoryListener != null) {
                this.mDialogDestoryListener.dialogDestroy();
                return true;
            }
            destroy();
            return true;
        }
    }

    @Override // android.view.Window.Callback
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent == null) {
            HiLog.debug(LABEL, "dispatchTouchEvent event is null", new Object[0]);
            return false;
        } else if (this.mAndroidWindow.superDispatchTouchEvent(motionEvent)) {
            return true;
        } else {
            return onTouchEvent(motionEvent);
        }
    }

    @Override // android.view.Window.Callback
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        synchronized (this) {
            if (accessibilityEvent != null) {
                if (this.mIComdDialogPAListener != null) {
                    AccessibilityEventInfo accessibilityEventInfo = new AccessibilityEventInfo();
                    BarrierFreeInnerClient.fillBarrierFreeEventInfo(accessibilityEvent, accessibilityEventInfo);
                    this.mIComdDialogPAListener.onDispatch(accessibilityEventInfo);
                    BarrierFreeInnerClient.fillAccessibilityEventInfo(this.mContext, accessibilityEventInfo, accessibilityEvent);
                    return true;
                }
            }
            HiLog.debug(LABEL, "Populate Accessibility Event event is null", new Object[0]);
            return false;
        }
    }

    @Override // android.view.Window.Callback
    public void onWindowAttributesChanged(WindowManager.LayoutParams layoutParams) {
        if (this.mDecor != null && this.mAndroidParam != null) {
            this.mAndroidWindowManager.updateViewLayout(this.mDecor, this.mAndroidParam);
        }
    }

    public void setComDialogPAEventListener(IComDialogPAEventLisener iComDialogPAEventLisener) {
        synchronized (this) {
            this.mIComdDialogPAListener = iComDialogPAEventLisener;
        }
    }

    private int isTouchOutsideWindow(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 4) {
            return 0;
        }
        if (this.mDecor == null) {
            HiLog.error(LABEL, "AGPCommonDialogWindow isTouchOutsideWindow return true due to mDecor is null", new Object[0]);
            return -1;
        }
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        if (this.mAndroidContext != null) {
            int scaledWindowTouchSlop = ViewConfiguration.get(this.mAndroidContext).getScaledWindowTouchSlop();
            int i = -scaledWindowTouchSlop;
            if (x >= i && y >= i && x <= this.mDecor.getWidth() + scaledWindowTouchSlop && y <= this.mDecor.getHeight() + scaledWindowTouchSlop) {
                if (motionEvent.getAction() == 0 || motionEvent.getAction() == 1) {
                    this.isKeyDownOut = false;
                }
                return 1;
            } else if (motionEvent.getAction() == 0) {
                this.isKeyDownOut = true;
                return 0;
            } else if (!this.isKeyDownOut) {
                return 1;
            } else {
                if (motionEvent.getAction() == 1) {
                    this.isKeyDownOut = false;
                }
                return 0;
            }
        } else {
            HiLog.error(LABEL, "isTouchOutsideWindow() mAndroidContext is null", new Object[0]);
            return -1;
        }
    }

    private boolean onTouchEvent(MotionEvent motionEvent) {
        if (isTouchOutsideWindow(motionEvent) != 0) {
            if (isTouchOutsideWindow(motionEvent) == 1) {
                handleMovable(motionEvent);
                Optional createEvent = MultimodalEventFactory.createEvent(motionEvent);
                if (createEvent.isPresent()) {
                    MultimodalEvent multimodalEvent = (MultimodalEvent) createEvent.get();
                    if (multimodalEvent instanceof TouchEvent) {
                        return dispatchTouchEventFromDialog((TouchEvent) multimodalEvent);
                    }
                }
                HiLog.error(LABEL, "MultimodalEvent is null or multimodalEvent is not instance of Touchevent", new Object[0]);
            }
            return false;
        } else if (this.mListener != null) {
            return this.mListener.isTouchOutside();
        } else {
            HiLog.error(LABEL, "AGPCommonDialogWindow onTouchEvent return true due to mListener is null", new Object[0]);
            return false;
        }
    }

    private void handleMovable(MotionEvent motionEvent) {
        if (this.movable) {
            if (this.move == null) {
                this.move = new AGPWindow.Move();
            }
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                this.move.lastX = motionEvent.getRawX();
                this.move.lastY = motionEvent.getRawY();
            } else if (actionMasked == 2) {
                this.move.nowX = motionEvent.getRawX();
                this.move.nowY = motionEvent.getRawY();
                this.move.tranX = this.move.nowX - this.move.lastX;
                this.move.tranY = this.move.nowY - this.move.lastY;
                if (this.mAndroidParam == null) {
                    HiLog.error(LABEL, "handleMovable mAndroidParam is null", new Object[0]);
                    return;
                }
                if (this.boundRect != null) {
                    View view = this.mDecor;
                    if (view == null) {
                        HiLog.error(LABEL, "handleMovable mDecor is null", new Object[0]);
                        return;
                    }
                    view.getLocationOnScreen(this.move.location);
                    if (((float) this.boundRect.left) <= ((float) this.move.location[0]) + this.move.tranX && ((float) this.boundRect.right) >= ((float) this.move.location[0]) + this.move.tranX + ((float) this.mAndroidParam.width) && ((float) this.boundRect.top) <= ((float) this.move.location[1]) + this.move.tranY && ((float) this.boundRect.bottom) >= ((float) this.move.location[1]) + this.move.tranY + ((float) this.mAndroidParam.height)) {
                        WindowManager.LayoutParams layoutParams = this.mAndroidParam;
                        layoutParams.x = (int) (((float) layoutParams.x) + this.move.tranX);
                        WindowManager.LayoutParams layoutParams2 = this.mAndroidParam;
                        layoutParams2.y = (int) (((float) layoutParams2.y) + this.move.tranY);
                        this.mAndroidWindow.setAttributes(this.mAndroidParam);
                    }
                } else {
                    WindowManager.LayoutParams layoutParams3 = this.mAndroidParam;
                    layoutParams3.x = (int) (((float) layoutParams3.x) + this.move.tranX);
                    WindowManager.LayoutParams layoutParams4 = this.mAndroidParam;
                    layoutParams4.y = (int) (((float) layoutParams4.y) + this.move.tranY);
                    this.mAndroidWindow.setAttributes(this.mAndroidParam);
                }
                this.move.lastX = this.move.nowX;
                this.move.lastY = this.move.nowY;
            }
        }
    }
}
