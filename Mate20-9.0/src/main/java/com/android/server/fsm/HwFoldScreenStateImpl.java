package com.android.server.fsm;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayViewport;
import android.hardware.display.HwFoldScreenState;
import android.hardware.input.InputManagerInternal;
import android.os.PowerManager;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.wm.WindowManagerInternal;
import huawei.android.hwutil.HwFullScreenDisplay;

public class HwFoldScreenStateImpl extends HwFoldScreenState {
    public static final int DISPLAY_REGION_MODE_FULL = 7;
    public static final int DISPLAY_REGION_MODE_MAIN = 1;
    public static final int DISPLAY_REGION_MODE_SUB = 6;
    public static final int DISPLAY_REGION_MODE_UNKNOWN = 0;
    public static final int NAV_BAR_HEIGHT = 128;
    public static final int REGION_TYPE_EDGE = 2;
    public static final int REGION_TYPE_MAIN = 1;
    public static final int REGION_TYPE_SUB = 4;
    public static final int REGION_TYPE_UNKNOWN = 0;
    private static final String TAG = "Fsm_FoldScreenStateImpl";
    private static WakeupManager mWakeupManager = null;
    private Context mContext;
    private int mDisplayMode = 0;
    private Rect mFullDispRect = new Rect();
    private InputManagerInternal mIM;
    private Rect mMainDispRect = new Rect();
    private Rect mSubDispRect = new Rect();
    private Rect mTmpDispRect = new Rect();
    private WindowManagerInternal mWM;

    public HwFoldScreenStateImpl(Context context) {
        this.mContext = context;
        initDisplayRect();
    }

    public Rect getScreenDispRect(int orientation) {
        if (orientation < 0 || orientation > 3) {
            return null;
        }
        Rect tmpDispRect = getCurrentDispRect();
        if (tmpDispRect == null) {
            return null;
        }
        if (orientation == 0) {
            this.mTmpDispRect.set(tmpDispRect);
        } else if (orientation == 1) {
            this.mTmpDispRect.left = tmpDispRect.top;
            this.mTmpDispRect.right = tmpDispRect.bottom;
            this.mTmpDispRect.top = SCREEN_FOLD_FULL_WIDTH - tmpDispRect.right;
            this.mTmpDispRect.bottom = SCREEN_FOLD_FULL_WIDTH - tmpDispRect.left;
        } else if (orientation == 2) {
            this.mTmpDispRect.left = SCREEN_FOLD_FULL_WIDTH - tmpDispRect.right;
            this.mTmpDispRect.right = SCREEN_FOLD_FULL_WIDTH - tmpDispRect.left;
            this.mTmpDispRect.top = SCREEN_FOLD_REAL_FULL_HEIGHT - tmpDispRect.bottom;
            this.mTmpDispRect.bottom = SCREEN_FOLD_REAL_FULL_HEIGHT - tmpDispRect.top;
        } else if (orientation == 3) {
            this.mTmpDispRect.left = SCREEN_FOLD_REAL_FULL_HEIGHT - tmpDispRect.bottom;
            this.mTmpDispRect.right = SCREEN_FOLD_REAL_FULL_HEIGHT - tmpDispRect.top;
            this.mTmpDispRect.top = tmpDispRect.left;
            this.mTmpDispRect.bottom = tmpDispRect.right;
        }
        Slog.d("Fsm_FoldScreenStateImpl", "getScreenDispRect=" + tmpDispRect);
        return this.mTmpDispRect;
    }

    public int getDisplayMode() {
        return this.mDisplayMode;
    }

    public int setDisplayMode(int mode) {
        boolean isScreenOn = ((PowerManager) this.mContext.getSystemService("power")).isScreenOn();
        Rect dispRegion = getCurrentDispRect();
        if (dispRegion != null) {
            int h = dispRegion.height();
            HwFullScreenDisplay.setFullScreenData(h, h - 128, dispRegion.width());
            setRealDisplayMode(mode);
        }
        if (!isScreenOn && mWakeupManager != null) {
            mWakeupManager.setFoldScreenReady();
            mWakeupManager.wakeup();
        }
        return 0;
    }

    public void adjustViewportFrame(DisplayViewport viewport, Rect currentLayerRect, Rect currentDisplayRect) {
        int orientation = viewport.orientation;
        boolean isRealFoldDevice = mIsFoldScreenDevice && !mIsSimulated;
        Rect currentDispRect = getScreenDispRect(orientation);
        if (currentDispRect != null) {
            if (isRealFoldDevice) {
                if (currentLayerRect != null) {
                    viewport.logicalFrame.set(currentDisplayRect);
                }
            } else if (!viewport.logicalFrame.isEmpty()) {
                viewport.logicalFrame.set(currentDispRect);
            }
            if (!viewport.physicalFrame.isEmpty()) {
                if (isRealFoldDevice) {
                    viewport.physicalFrame.set(currentDisplayRect);
                } else {
                    viewport.physicalFrame.set(currentDispRect);
                }
                if (orientation != 0) {
                    if (orientation == 1) {
                        if (!isRealFoldDevice) {
                            int tmpOffsetHeight = SCREEN_FOLD_FULL_WIDTH - currentDispRect.height();
                            if (tmpOffsetHeight > 0) {
                                if (currentDispRect.top == 0) {
                                    viewport.physicalFrame.top += tmpOffsetHeight;
                                    viewport.physicalFrame.bottom += tmpOffsetHeight;
                                } else {
                                    viewport.physicalFrame.top -= tmpOffsetHeight;
                                    viewport.physicalFrame.bottom -= tmpOffsetHeight;
                                }
                            }
                        }
                    } else if (orientation == 3) {
                        if (!isRealFoldDevice) {
                            int tmpOffsetWidth = SCREEN_FOLD_REAL_FULL_HEIGHT - currentDispRect.width();
                            if (tmpOffsetWidth > 0) {
                                viewport.physicalFrame.left -= tmpOffsetWidth;
                                viewport.physicalFrame.right -= tmpOffsetWidth;
                            }
                        } else if (this.mDisplayMode == 3) {
                            int tmpOffsetWidth2 = SCREEN_FOLD_FULL_WIDTH - SCREEN_FOLD_MAIN_WIDTH;
                            viewport.physicalFrame.left += tmpOffsetWidth2;
                            viewport.physicalFrame.right += tmpOffsetWidth2;
                        } else if (this.mDisplayMode == 2) {
                            int tmpOffsetWidth3 = SCREEN_FOLD_FULL_WIDTH - SCREEN_FOLD_MAIN_WIDTH;
                            viewport.physicalFrame.left -= tmpOffsetWidth3;
                            viewport.physicalFrame.right -= tmpOffsetWidth3;
                        }
                    } else if (orientation == 2) {
                        if (!isRealFoldDevice) {
                            int tmpOffsetHeight2 = SCREEN_FOLD_REAL_FULL_HEIGHT - currentDispRect.height();
                            int tmpOffsetWidth4 = SCREEN_FOLD_FULL_WIDTH - currentDispRect.width();
                            if (tmpOffsetHeight2 > 0) {
                                viewport.physicalFrame.top -= tmpOffsetHeight2;
                                viewport.physicalFrame.bottom -= tmpOffsetHeight2;
                            }
                            if (tmpOffsetWidth4 > 0) {
                                if (currentDispRect.left == 0) {
                                    viewport.physicalFrame.left += tmpOffsetWidth4;
                                    viewport.physicalFrame.right += tmpOffsetWidth4;
                                } else {
                                    viewport.physicalFrame.left -= tmpOffsetWidth4;
                                    viewport.physicalFrame.right -= tmpOffsetWidth4;
                                }
                            }
                        } else if (this.mDisplayMode == 3) {
                            int tmpOffsetHeight3 = SCREEN_FOLD_FULL_WIDTH - SCREEN_FOLD_MAIN_WIDTH;
                            viewport.physicalFrame.top += tmpOffsetHeight3;
                            viewport.physicalFrame.bottom += tmpOffsetHeight3;
                        } else if (this.mDisplayMode == 2) {
                            int tmpOffsetHeight4 = SCREEN_FOLD_FULL_WIDTH - SCREEN_FOLD_MAIN_WIDTH;
                            viewport.physicalFrame.top -= tmpOffsetHeight4;
                            viewport.physicalFrame.bottom -= tmpOffsetHeight4;
                        }
                    }
                }
            }
        }
    }

    public int rotateScreen() {
        if (!mIsFoldScreenDevice || mIsSimulated) {
            return 0;
        }
        return 3;
    }

    private void initDisplayRect() {
        this.mFullDispRect.set(0, 0, SCREEN_FOLD_FULL_WIDTH, SCREEN_FOLD_FULL_HEIGHT);
        this.mMainDispRect.set(SCREEN_FOLD_FULL_WIDTH - SCREEN_FOLD_MAIN_WIDTH, 0, SCREEN_FOLD_FULL_WIDTH, SCREEN_FOLD_FULL_HEIGHT);
        this.mSubDispRect.set(0, 0, SCREEN_FOLD_MAIN_WIDTH, SCREEN_FOLD_FULL_HEIGHT);
    }

    private Rect getCurrentDispRect() {
        return getDispRect(this.mDisplayMode);
    }

    private Rect getDispRect(int mode) {
        Rect screenRect = null;
        if (mode == 0) {
            screenRect = this.mFullDispRect;
        } else if (1 == mode) {
            screenRect = this.mFullDispRect;
        } else if (2 == mode) {
            screenRect = this.mMainDispRect;
        } else if (3 == mode) {
            screenRect = this.mSubDispRect;
        } else if (4 == mode) {
            screenRect = this.mFullDispRect;
        }
        Slog.d("Fsm_FoldScreenStateImpl", "getCurrentDispRect = " + screenRect);
        return screenRect;
    }

    private void setRealDisplayMode(int mode) {
        if (mode == this.mDisplayMode) {
            Slog.d("Fsm_FoldScreenStateImpl", "Current mode don't change, return!");
            return;
        }
        Rect screenRect = getDispRect(mode);
        if (this.mWM == null) {
            this.mWM = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        }
        if (!(this.mWM == null || screenRect == null || screenRect.isEmpty())) {
            if (this.mIM == null) {
                this.mIM = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
            }
            if (this.mIM != null) {
                this.mIM.setDisplayMode(mode, SCREEN_FOLD_SUB_WIDTH, SCREEN_FOLD_MAIN_WIDTH, SCREEN_FOLD_FULL_HEIGHT);
                Slog.d("Fsm_FoldScreenStateImpl", "mIM.setDisplayMode !");
            }
            Slog.d("Fsm_FoldScreenStateImpl", "setRealDisplayMode new mode:" + mode + ", old mode:" + this.mDisplayMode + " screenRect=" + screenRect);
            this.mDisplayMode = mode;
            this.mWM.setForcedDisplaySize(0, screenRect.width(), screenRect.height());
        }
    }

    public static int getDisplayRect(Point point) {
        int displayRect = 0;
        int x = SCREEN_FOLD_FULL_WIDTH - point.y;
        int y = point.x;
        Slog.i("Fsm_FoldScreenStateImpl", "getDisplayRect x:" + x + ", y:" + y);
        if (y > SCREEN_FOLD_FULL_HEIGHT) {
            return 0;
        }
        if (x <= SCREEN_FOLD_SUB_WIDTH) {
            displayRect = 4;
        } else if (x < SCREEN_FOLD_SUB_WIDTH + SCREEN_FOLD_EDGE_WIDTH) {
            displayRect = 2;
        } else if (x < SCREEN_FOLD_FULL_WIDTH) {
            displayRect = 1;
        }
        return displayRect;
    }

    public static void setWakeUpManager(WakeupManager wm) {
        mWakeupManager = wm;
    }
}
