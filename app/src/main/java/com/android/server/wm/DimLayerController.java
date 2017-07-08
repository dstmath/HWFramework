package com.android.server.wm;

import android.graphics.Rect;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.TypedValue;
import java.io.PrintWriter;

class DimLayerController {
    private static final float DEFAULT_DIM_AMOUNT_DEAD_WINDOW = 0.5f;
    private static final int DEFAULT_DIM_DURATION = 200;
    private static final String TAG = null;
    private static final String TAG_LOCAL = "DimLayerController";
    private DisplayContent mDisplayContent;
    private DimLayer mSharedFullScreenDimLayer;
    private ArrayMap<DimLayerUser, DimLayerState> mState;
    private Rect mTmpBounds;

    private static class DimLayerState {
        WindowStateAnimator animator;
        boolean continueDimming;
        boolean dimAbove;
        DimLayer dimLayer;

        private DimLayerState() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.DimLayerController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.DimLayerController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.DimLayerController.<clinit>():void");
    }

    DimLayerController(DisplayContent displayContent) {
        this.mState = new ArrayMap();
        this.mTmpBounds = new Rect();
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
        DimLayerState state = getOrCreateDimLayerState(dimLayerUser);
        state.dimAbove = aboveApp;
        if (!newWinAnimator.getShown()) {
            return;
        }
        if (state.animator == null || !state.animator.getShown() || state.animator.mAnimLayer <= newWinAnimator.mAnimLayer) {
            state.animator = newWinAnimator;
            if (state.animator.mWin.mAppToken != null || dimLayerUser.dimFullscreen()) {
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
        boolean result = false;
        for (int i = this.mState.size() - 1; i >= 0; i--) {
            DimLayerUser user = (DimLayerUser) this.mState.keyAt(i);
            DimLayerState state = (DimLayerState) this.mState.valueAt(i);
            if (user.dimFullscreen() && state.dimLayer == this.mSharedFullScreenDimLayer) {
                fullScreen = i;
                if (((DimLayerState) this.mState.valueAt(i)).continueDimming) {
                    fullScreenAndDimming = i;
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
            dimAmount = DEFAULT_DIM_AMOUNT_DEAD_WINDOW;
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
        this.mDisplayContent.mService.mContext.getResources().getValue(18022400, tv, true);
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
