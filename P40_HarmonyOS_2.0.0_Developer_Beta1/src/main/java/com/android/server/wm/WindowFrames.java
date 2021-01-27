package com.android.server.wm;

import android.graphics.Rect;
import android.util.proto.ProtoOutputStream;
import com.android.server.wm.utils.InsetUtils;
import com.android.server.wm.utils.WmDisplayCutout;
import java.io.PrintWriter;

public class WindowFrames {
    private static final StringBuilder sTmpSB = new StringBuilder();
    final Rect mCompatFrame = new Rect();
    final Rect mContainingFrame = new Rect();
    private boolean mContentChanged;
    public final Rect mContentFrame = new Rect();
    final Rect mContentInsets = new Rect();
    private boolean mContentInsetsChanged;
    public final Rect mDecorFrame = new Rect();
    WmDisplayCutout mDisplayCutout = WmDisplayCutout.NO_CUTOUT;
    private boolean mDisplayCutoutChanged;
    public final Rect mDisplayFrame = new Rect();
    final Rect mFrame = new Rect();
    private boolean mFrameSizeChanged = false;
    private boolean mHasOutsets;
    final Rect mLastContentInsets = new Rect();
    private WmDisplayCutout mLastDisplayCutout = WmDisplayCutout.NO_CUTOUT;
    final Rect mLastFrame = new Rect();
    final Rect mLastOutsets = new Rect();
    final Rect mLastOverscanInsets = new Rect();
    final Rect mLastStableInsets = new Rect();
    final Rect mLastVisibleInsets = new Rect();
    public final Rect mOutsetFrame = new Rect();
    final Rect mOutsets = new Rect();
    private boolean mOutsetsChanged = false;
    public final Rect mOverscanFrame = new Rect();
    final Rect mOverscanInsets = new Rect();
    private boolean mOverscanInsetsChanged;
    public final Rect mParentFrame = new Rect();
    private boolean mParentFrameWasClippedByDisplayCutout;
    public final Rect mStableFrame = new Rect();
    final Rect mStableInsets = new Rect();
    private boolean mStableInsetsChanged;
    private final Rect mTmpRect = new Rect();
    public final Rect mVisibleFrame = new Rect();
    final Rect mVisibleInsets = new Rect();
    private boolean mVisibleInsetsChanged;

    public WindowFrames() {
    }

    public WindowFrames(Rect parentFrame, Rect displayFrame, Rect overscanFrame, Rect contentFrame, Rect visibleFrame, Rect decorFrame, Rect stableFrame, Rect outsetFrame) {
        setFrames(parentFrame, displayFrame, overscanFrame, contentFrame, visibleFrame, decorFrame, stableFrame, outsetFrame);
    }

    public void setFrames(Rect parentFrame, Rect displayFrame, Rect overscanFrame, Rect contentFrame, Rect visibleFrame, Rect decorFrame, Rect stableFrame, Rect outsetFrame) {
        this.mParentFrame.set(parentFrame);
        this.mDisplayFrame.set(displayFrame);
        this.mOverscanFrame.set(overscanFrame);
        this.mContentFrame.set(contentFrame);
        this.mVisibleFrame.set(visibleFrame);
        this.mDecorFrame.set(decorFrame);
        this.mStableFrame.set(stableFrame);
        this.mOutsetFrame.set(outsetFrame);
    }

    public void setParentFrameWasClippedByDisplayCutout(boolean parentFrameWasClippedByDisplayCutout) {
        this.mParentFrameWasClippedByDisplayCutout = parentFrameWasClippedByDisplayCutout;
    }

    /* access modifiers changed from: package-private */
    public boolean parentFrameWasClippedByDisplayCutout() {
        return this.mParentFrameWasClippedByDisplayCutout;
    }

    public void setDisplayCutout(WmDisplayCutout displayCutout) {
        this.mDisplayCutout = displayCutout;
    }

    private boolean didFrameSizeChange() {
        return (this.mLastFrame.width() == this.mFrame.width() && this.mLastFrame.height() == this.mFrame.height()) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public void calculateOutsets() {
        if (this.mHasOutsets) {
            InsetUtils.insetsBetweenFrames(this.mOutsetFrame, this.mContentFrame, this.mOutsets);
        }
    }

    /* access modifiers changed from: package-private */
    public void calculateDockedDividerInsets(Rect cutoutInsets) {
        this.mTmpRect.set(this.mDisplayFrame);
        this.mTmpRect.inset(cutoutInsets);
        this.mTmpRect.intersectUnchecked(this.mStableFrame);
        InsetUtils.insetsBetweenFrames(this.mDisplayFrame, this.mTmpRect, this.mStableInsets);
        this.mContentInsets.setEmpty();
        this.mVisibleInsets.setEmpty();
        this.mDisplayCutout = WmDisplayCutout.NO_CUTOUT;
    }

    /* access modifiers changed from: package-private */
    public void calculateInsets(boolean windowsAreFloating, boolean inFullscreenContainer, Rect windowBounds) {
        boolean overrideBottomInset = true;
        boolean overrideRightInset = !windowsAreFloating && !inFullscreenContainer && this.mFrame.right > windowBounds.right;
        if (windowsAreFloating || inFullscreenContainer || this.mFrame.bottom <= windowBounds.bottom) {
            overrideBottomInset = false;
        }
        this.mTmpRect.set(this.mFrame.left, this.mFrame.top, overrideRightInset ? windowBounds.right : this.mFrame.right, overrideBottomInset ? windowBounds.bottom : this.mFrame.bottom);
        InsetUtils.insetsBetweenFrames(this.mTmpRect, this.mContentFrame, this.mContentInsets);
        InsetUtils.insetsBetweenFrames(this.mTmpRect, this.mVisibleFrame, this.mVisibleInsets);
        InsetUtils.insetsBetweenFrames(this.mTmpRect, this.mStableFrame, this.mStableInsets);
    }

    /* access modifiers changed from: package-private */
    public void calculateInsetsforHwFreeform(int offset) {
        this.mContentInsets.top += offset;
        this.mVisibleInsets.top += offset;
        this.mStableInsets.top += offset;
    }

    /* access modifiers changed from: package-private */
    public void scaleInsets(float scale) {
        this.mOverscanInsets.scale(scale);
        this.mContentInsets.scale(scale);
        this.mVisibleInsets.scale(scale);
        this.mStableInsets.scale(scale);
        this.mOutsets.scale(scale);
    }

    /* access modifiers changed from: package-private */
    public void offsetFrames(int layoutXDiff, int layoutYDiff) {
        this.mFrame.offset(layoutXDiff, layoutYDiff);
        this.mContentFrame.offset(layoutXDiff, layoutYDiff);
        this.mVisibleFrame.offset(layoutXDiff, layoutYDiff);
        this.mStableFrame.offset(layoutXDiff, layoutYDiff);
    }

    /* access modifiers changed from: package-private */
    public boolean setReportResizeHints() {
        this.mOverscanInsetsChanged |= !this.mLastOverscanInsets.equals(this.mOverscanInsets);
        this.mContentInsetsChanged |= !this.mLastContentInsets.equals(this.mContentInsets);
        this.mVisibleInsetsChanged |= !this.mLastVisibleInsets.equals(this.mVisibleInsets);
        this.mStableInsetsChanged |= !this.mLastStableInsets.equals(this.mStableInsets);
        this.mOutsetsChanged |= !this.mLastOutsets.equals(this.mOutsets);
        this.mFrameSizeChanged |= didFrameSizeChange();
        this.mDisplayCutoutChanged |= !this.mLastDisplayCutout.equals(this.mDisplayCutout);
        if (this.mOverscanInsetsChanged || this.mContentInsetsChanged || this.mVisibleInsetsChanged || this.mStableInsetsChanged || this.mOutsetsChanged || this.mFrameSizeChanged || this.mDisplayCutoutChanged) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void resetInsetsChanged() {
        this.mOverscanInsetsChanged = false;
        this.mContentInsetsChanged = false;
        this.mVisibleInsetsChanged = false;
        this.mStableInsetsChanged = false;
        this.mOutsetsChanged = false;
        this.mFrameSizeChanged = false;
        this.mDisplayCutoutChanged = false;
    }

    /* access modifiers changed from: package-private */
    public void updateLastInsetValues() {
        this.mLastOverscanInsets.set(this.mOverscanInsets);
        this.mLastContentInsets.set(this.mContentInsets);
        this.mLastVisibleInsets.set(this.mVisibleInsets);
        this.mLastStableInsets.set(this.mStableInsets);
        this.mLastOutsets.set(this.mOutsets);
        this.mLastDisplayCutout = this.mDisplayCutout;
    }

    /* access modifiers changed from: package-private */
    public void resetLastContentInsets() {
        this.mLastContentInsets.set(-1, -1, -1, -1);
    }

    public void setHasOutsets(boolean hasOutsets) {
        if (this.mHasOutsets != hasOutsets) {
            this.mHasOutsets = hasOutsets;
            if (!hasOutsets) {
                this.mOutsets.setEmpty();
            }
        }
    }

    public void setContentChanged(boolean contentChanged) {
        this.mContentChanged = contentChanged;
    }

    /* access modifiers changed from: package-private */
    public boolean hasContentChanged() {
        return this.mContentChanged;
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        this.mParentFrame.writeToProto(proto, 1146756268040L);
        this.mContentFrame.writeToProto(proto, 1146756268034L);
        this.mDisplayFrame.writeToProto(proto, 1146756268036L);
        this.mOverscanFrame.writeToProto(proto, 1146756268039L);
        this.mVisibleFrame.writeToProto(proto, 1146756268041L);
        this.mDecorFrame.writeToProto(proto, 1146756268035L);
        this.mOutsetFrame.writeToProto(proto, 1146756268038L);
        this.mContainingFrame.writeToProto(proto, 1146756268033L);
        this.mFrame.writeToProto(proto, 1146756268037L);
        this.mDisplayCutout.getDisplayCutout().writeToProto(proto, 1146756268042L);
        this.mContentInsets.writeToProto(proto, 1146756268043L);
        this.mOverscanInsets.writeToProto(proto, 1146756268044L);
        this.mVisibleInsets.writeToProto(proto, 1146756268045L);
        this.mStableInsets.writeToProto(proto, 1146756268046L);
        this.mOutsets.writeToProto(proto, 1146756268047L);
        proto.end(token);
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "Frames: containing=" + this.mContainingFrame.toShortString(sTmpSB) + " parent=" + this.mParentFrame.toShortString(sTmpSB));
        pw.println(prefix + "    display=" + this.mDisplayFrame.toShortString(sTmpSB) + " overscan=" + this.mOverscanFrame.toShortString(sTmpSB));
        pw.println(prefix + "    content=" + this.mContentFrame.toShortString(sTmpSB) + " visible=" + this.mVisibleFrame.toShortString(sTmpSB));
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("    decor=");
        sb.append(this.mDecorFrame.toShortString(sTmpSB));
        pw.println(sb.toString());
        pw.println(prefix + "    outset=" + this.mOutsetFrame.toShortString(sTmpSB));
        pw.println(prefix + "mFrame=" + this.mFrame.toShortString(sTmpSB) + " last=" + this.mLastFrame.toShortString(sTmpSB));
        pw.println(prefix + " cutout=" + this.mDisplayCutout.getDisplayCutout() + " last=" + this.mLastDisplayCutout.getDisplayCutout());
        pw.print(prefix + "Cur insets: overscan=" + this.mOverscanInsets.toShortString(sTmpSB) + " content=" + this.mContentInsets.toShortString(sTmpSB) + " visible=" + this.mVisibleInsets.toShortString(sTmpSB) + " stable=" + this.mStableInsets.toShortString(sTmpSB) + " outsets=" + this.mOutsets.toShortString(sTmpSB));
        pw.println(prefix + "Lst insets: overscan=" + this.mLastOverscanInsets.toShortString(sTmpSB) + " content=" + this.mLastContentInsets.toShortString(sTmpSB) + " visible=" + this.mLastVisibleInsets.toShortString(sTmpSB) + " stable=" + this.mLastStableInsets.toShortString(sTmpSB) + " outset=" + this.mLastOutsets.toShortString(sTmpSB));
    }

    /* access modifiers changed from: package-private */
    public String getInsetsInfo() {
        return "ci=" + this.mContentInsets.toShortString() + " vi=" + this.mVisibleInsets.toShortString() + " si=" + this.mStableInsets.toShortString() + " of=" + this.mOutsets.toShortString();
    }

    /* access modifiers changed from: package-private */
    public String getInsetsChangedInfo() {
        return "contentInsetsChanged=" + this.mContentInsetsChanged + " " + this.mContentInsets.toShortString() + " visibleInsetsChanged=" + this.mVisibleInsetsChanged + " " + this.mVisibleInsets.toShortString() + " stableInsetsChanged=" + this.mStableInsetsChanged + " " + this.mStableInsets.toShortString() + " outsetsChanged=" + this.mOutsetsChanged + " " + this.mOutsets.toShortString() + " displayCutoutChanged=" + this.mDisplayCutoutChanged;
    }
}
