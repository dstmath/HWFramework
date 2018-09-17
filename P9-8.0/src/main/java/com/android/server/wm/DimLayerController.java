package com.android.server.wm;

import android.graphics.Rect;
import android.util.ArrayMap;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.TypedValue;
import java.io.PrintWriter;

class DimLayerController {
    private static final float DEFAULT_DIM_AMOUNT_DEAD_WINDOW = 0.5f;
    private static final int DEFAULT_DIM_DURATION = 200;
    private static final String TAG = "WindowManager";
    private static final String TAG_LOCAL = "DimLayerController";
    private DisplayContent mDisplayContent;
    private DimLayer mSharedFullScreenDimLayer;
    private ArrayMap<DimLayerUser, DimLayerState> mState = new ArrayMap();
    private Rect mTmpBounds = new Rect();

    private static class DimLayerState {
        WindowStateAnimator animator;
        boolean continueDimming;
        boolean dimAbove;
        DimLayer dimLayer;

        /* synthetic */ DimLayerState(DimLayerState -this0) {
            this();
        }

        private DimLayerState() {
        }
    }

    DimLayerController(DisplayContent displayContent) {
        this.mDisplayContent = displayContent;
    }

    void updateDimLayer(DimLayerUser dimLayerUser) {
        DimLayer newDimLayer;
        DimLayerState state = getOrCreateDimLayerState(dimLayerUser);
        boolean previousFullscreen = state.dimLayer != null ? state.dimLayer == this.mSharedFullScreenDimLayer : false;
        int displayId = this.mDisplayContent.getDisplayId();
        if (!dimLayerUser.dimFullscreen()) {
            if (state.dimLayer == null || previousFullscreen) {
                newDimLayer = new DimLayer(this.mDisplayContent.mService, dimLayerUser, displayId, getDimLayerTag(dimLayerUser));
            } else {
                newDimLayer = state.dimLayer;
            }
            dimLayerUser.getDimBounds(this.mTmpBounds);
            newDimLayer.setBounds(this.mTmpBounds);
        } else if (!previousFullscreen || this.mSharedFullScreenDimLayer == null) {
            newDimLayer = this.mSharedFullScreenDimLayer;
            if (newDimLayer == null) {
                if (state.dimLayer != null) {
                    newDimLayer = state.dimLayer;
                } else {
                    newDimLayer = new DimLayer(this.mDisplayContent.mService, dimLayerUser, displayId, getDimLayerTag(dimLayerUser));
                }
                dimLayerUser.getDimBounds(this.mTmpBounds);
                newDimLayer.setBounds(this.mTmpBounds);
                this.mSharedFullScreenDimLayer = newDimLayer;
                if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(this.mDisplayContent.mDisplayId)) {
                    this.mSharedFullScreenDimLayer = null;
                }
            } else if (state.dimLayer != null) {
                state.dimLayer.destroySurface();
            }
        } else {
            this.mSharedFullScreenDimLayer.setBoundsForFullscreen();
            return;
        }
        state.dimLayer = newDimLayer;
    }

    private static String getDimLayerTag(DimLayerUser dimLayerUser) {
        return "DimLayerController/" + dimLayerUser.toShortString();
    }

    private DimLayerState getOrCreateDimLayerState(DimLayerUser dimLayerUser) {
        DimLayerState state = (DimLayerState) this.mState.get(dimLayerUser);
        if (state != null) {
            return state;
        }
        state = new DimLayerState();
        this.mState.put(dimLayerUser, state);
        return state;
    }

    private void setContinueDimming(DimLayerUser dimLayerUser) {
        DimLayerState state = (DimLayerState) this.mState.get(dimLayerUser);
        if (state != null) {
            state.continueDimming = true;
        }
    }

    boolean isDimming() {
        for (int i = this.mState.size() - 1; i >= 0; i--) {
            DimLayerState state = (DimLayerState) this.mState.valueAt(i);
            if (state.dimLayer != null && state.dimLayer.isDimming()) {
                return true;
            }
        }
        return false;
    }

    void resetDimming() {
        for (int i = this.mState.size() - 1; i >= 0; i--) {
            ((DimLayerState) this.mState.valueAt(i)).continueDimming = false;
        }
    }

    private boolean getContinueDimming(DimLayerUser dimLayerUser) {
        DimLayerState state = (DimLayerState) this.mState.get(dimLayerUser);
        return state != null ? state.continueDimming : false;
    }

    void startDimmingIfNeeded(DimLayerUser dimLayerUser, WindowStateAnimator newWinAnimator, boolean aboveApp) {
        updateDimLayer(dimLayerUser);
        DimLayerState state = getOrCreateDimLayerState(dimLayerUser);
        state.dimAbove = aboveApp;
        if (!newWinAnimator.getShown()) {
            return;
        }
        if (state.animator == null || (state.animator.getShown() ^ 1) != 0 || state.animator.mAnimLayer <= newWinAnimator.mAnimLayer) {
            state.animator = newWinAnimator;
            if (state.animator.mWin.mAppToken != null || (dimLayerUser.dimFullscreen() ^ 1) == 0) {
                dimLayerUser.getDimBounds(this.mTmpBounds);
            } else {
                this.mDisplayContent.getLogicalDisplayRect(this.mTmpBounds);
            }
            state.dimLayer.setBounds(this.mTmpBounds);
        }
    }

    void stopDimmingIfNeeded() {
        for (int i = this.mState.size() - 1; i >= 0; i--) {
            stopDimmingIfNeeded((DimLayerUser) this.mState.keyAt(i));
        }
    }

    private void stopDimmingIfNeeded(DimLayerUser dimLayerUser) {
        DimLayerState state = (DimLayerState) this.mState.get(dimLayerUser);
        if ((state.animator == null || !state.animator.mWin.mWillReplaceWindow) && !state.continueDimming && state.dimLayer.isDimming()) {
            state.animator = null;
            dimLayerUser.getDimBounds(this.mTmpBounds);
            state.dimLayer.setBounds(this.mTmpBounds);
        }
    }

    boolean animateDimLayers() {
        int fullScreen = -1;
        int fullScreenAndDimming = -1;
        int topFullScreenUserLayer = 0;
        boolean result = false;
        for (int i = this.mState.size() - 1; i >= 0; i--) {
            DimLayerUser user = (DimLayerUser) this.mState.keyAt(i);
            DimLayerState state = (DimLayerState) this.mState.valueAt(i);
            if (!user.isAttachedToDisplay()) {
                Slog.w(TAG, "Leaked dim user=" + user.toShortString() + " state=" + state);
                removeDimLayerUser(user);
            } else if (user.dimFullscreen() && state.dimLayer == this.mSharedFullScreenDimLayer) {
                fullScreen = i;
                if (state.continueDimming && (topFullScreenUserLayer == 0 || (state.animator != null && state.animator.mAnimLayer > topFullScreenUserLayer))) {
                    fullScreenAndDimming = i;
                    if (state.animator != null) {
                        topFullScreenUserLayer = state.animator.mAnimLayer;
                    }
                }
            } else {
                result |= animateDimLayers(user);
            }
        }
        if (fullScreenAndDimming != -1) {
            return result | animateDimLayers((DimLayerUser) this.mState.keyAt(fullScreenAndDimming));
        }
        if (fullScreen != -1) {
            return result | animateDimLayers((DimLayerUser) this.mState.keyAt(fullScreen));
        }
        return result;
    }

    private boolean animateDimLayers(DimLayerUser dimLayerUser) {
        int dimLayer;
        float dimAmount;
        DimLayerState state = (DimLayerState) this.mState.get(dimLayerUser);
        if (state.animator == null) {
            dimLayer = state.dimLayer.getLayer();
            dimAmount = 0.0f;
        } else if (state.dimAbove) {
            dimLayer = state.animator.mAnimLayer + 1;
            dimAmount = 0.5f;
        } else {
            dimLayer = state.animator.mAnimLayer - 1;
            dimAmount = state.animator.mWin.mAttrs.dimAmount;
        }
        float targetAlpha = state.dimLayer.getTargetAlpha();
        if (targetAlpha != dimAmount) {
            if (state.animator == null) {
                state.dimLayer.hide(200);
            } else {
                long duration;
                if (!state.animator.mAnimating || state.animator.mAnimation == null) {
                    duration = 200;
                } else {
                    duration = state.animator.mAnimation.computeDurationHint();
                }
                if (targetAlpha > dimAmount) {
                    duration = getDimLayerFadeDuration(duration);
                }
                state.dimLayer.show(dimLayer, dimAmount, duration);
                if (targetAlpha == 0.0f) {
                    DisplayContent displayContent = this.mDisplayContent;
                    displayContent.pendingLayoutChanges |= 1;
                    this.mDisplayContent.setLayoutNeeded();
                }
            }
        } else if (state.dimLayer.getLayer() != dimLayer) {
            state.dimLayer.setLayer(dimLayer);
        }
        if (state.dimLayer.isAnimating()) {
            if (this.mDisplayContent.mService.okToDisplay()) {
                return state.dimLayer.stepAnimation();
            }
            state.dimLayer.show();
        }
        return false;
    }

    boolean isDimming(DimLayerUser dimLayerUser, WindowStateAnimator winAnimator) {
        DimLayerState state = (DimLayerState) this.mState.get(dimLayerUser);
        return (state == null || state.animator != winAnimator) ? false : state.dimLayer.isDimming();
    }

    private long getDimLayerFadeDuration(long duration) {
        TypedValue tv = new TypedValue();
        this.mDisplayContent.mService.mContext.getResources().getValue(18022401, tv, true);
        if (tv.type == 6) {
            return (long) tv.getFraction((float) duration, (float) duration);
        }
        if (tv.type < 16 || tv.type > 31) {
            return duration;
        }
        return (long) tv.data;
    }

    void close() {
        for (int i = this.mState.size() - 1; i >= 0; i--) {
            ((DimLayerState) this.mState.valueAt(i)).dimLayer.destroySurface();
        }
        this.mState.clear();
        this.mSharedFullScreenDimLayer = null;
    }

    void removeDimLayerUser(DimLayerUser dimLayerUser) {
        DimLayerState state = (DimLayerState) this.mState.get(dimLayerUser);
        if (state != null) {
            if (state.dimLayer != this.mSharedFullScreenDimLayer) {
                state.dimLayer.destroySurface();
            }
            this.mState.remove(dimLayerUser);
        }
        if (this.mState.isEmpty()) {
            this.mSharedFullScreenDimLayer = null;
        }
    }

    boolean hasDimLayerUser(DimLayerUser dimLayerUser) {
        return this.mState.containsKey(dimLayerUser);
    }

    boolean hasSharedFullScreenDimLayer() {
        return this.mSharedFullScreenDimLayer != null;
    }

    void applyDimBehind(DimLayerUser dimLayerUser, WindowStateAnimator animator) {
        applyDim(dimLayerUser, animator, false);
    }

    void applyDimAbove(DimLayerUser dimLayerUser, WindowStateAnimator animator) {
        applyDim(dimLayerUser, animator, true);
    }

    void applyDim(DimLayerUser dimLayerUser, WindowStateAnimator animator, boolean aboveApp) {
        if (dimLayerUser == null) {
            Slog.e(TAG, "Trying to apply dim layer for: " + this + ", but no dim layer user found.");
            return;
        }
        if (!getContinueDimming(dimLayerUser)) {
            setContinueDimming(dimLayerUser);
            if (!isDimming(dimLayerUser, animator)) {
                startDimmingIfNeeded(dimLayerUser, animator, aboveApp);
            }
        }
    }

    void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + TAG_LOCAL);
        String doubleSpace = "  ";
        String prefixPlusDoubleSpace = prefix + "  ";
        int n = this.mState.size();
        for (int i = 0; i < n; i++) {
            pw.println(prefixPlusDoubleSpace + ((DimLayerUser) this.mState.keyAt(i)).toShortString());
            DimLayerState state = (DimLayerState) this.mState.valueAt(i);
            pw.println(prefixPlusDoubleSpace + "  " + "dimLayer=" + (state.dimLayer == this.mSharedFullScreenDimLayer ? "shared" : state.dimLayer) + ", animator=" + state.animator + ", continueDimming=" + state.continueDimming);
            if (state.dimLayer != null) {
                state.dimLayer.printTo(prefixPlusDoubleSpace + "  ", pw);
            }
        }
    }
}
