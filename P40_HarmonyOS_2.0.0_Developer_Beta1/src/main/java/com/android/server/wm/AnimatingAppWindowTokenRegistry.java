package com.android.server.wm;

import android.util.ArrayMap;
import android.util.ArraySet;
import java.io.PrintWriter;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class AnimatingAppWindowTokenRegistry {
    private ArraySet<AppWindowToken> mAnimatingTokens = new ArraySet<>();
    private boolean mEndingDeferredFinish;
    private ArrayMap<AppWindowToken, Runnable> mFinishedTokens = new ArrayMap<>();
    private ArrayList<Runnable> mTmpRunnableList = new ArrayList<>();

    AnimatingAppWindowTokenRegistry() {
    }

    /* access modifiers changed from: package-private */
    public void notifyStarting(AppWindowToken token) {
        this.mAnimatingTokens.add(token);
    }

    /* access modifiers changed from: package-private */
    public void notifyFinished(AppWindowToken token) {
        this.mAnimatingTokens.remove(token);
        this.mFinishedTokens.remove(token);
        if (this.mAnimatingTokens.isEmpty()) {
            endDeferringFinished();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean notifyAboutToFinish(AppWindowToken token, Runnable endDeferFinishCallback) {
        if (!this.mAnimatingTokens.remove(token)) {
            return false;
        }
        if (this.mAnimatingTokens.isEmpty()) {
            endDeferringFinished();
            return false;
        }
        this.mFinishedTokens.put(token, endDeferFinishCallback);
        return true;
    }

    /* JADX INFO: finally extract failed */
    private void endDeferringFinished() {
        if (!this.mEndingDeferredFinish) {
            try {
                this.mEndingDeferredFinish = true;
                for (int i = this.mFinishedTokens.size() - 1; i >= 0; i--) {
                    this.mTmpRunnableList.add(this.mFinishedTokens.valueAt(i));
                }
                this.mFinishedTokens.clear();
                for (int i2 = this.mTmpRunnableList.size() - 1; i2 >= 0; i2--) {
                    this.mTmpRunnableList.get(i2).run();
                }
                this.mTmpRunnableList.clear();
                this.mEndingDeferredFinish = false;
            } catch (Throwable th) {
                this.mEndingDeferredFinish = false;
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String header, String prefix) {
        if (!this.mAnimatingTokens.isEmpty() || !this.mFinishedTokens.isEmpty()) {
            pw.print(prefix);
            pw.println(header);
            String prefix2 = prefix + "  ";
            pw.print(prefix2);
            pw.print("mAnimatingTokens=");
            pw.println(this.mAnimatingTokens);
            pw.print(prefix2);
            pw.print("mFinishedTokens=");
            pw.println(this.mFinishedTokens);
        }
    }
}
