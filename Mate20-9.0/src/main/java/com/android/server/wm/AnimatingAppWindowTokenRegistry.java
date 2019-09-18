package com.android.server.wm;

import android.util.ArrayMap;
import android.util.ArraySet;
import java.io.PrintWriter;
import java.util.ArrayList;

class AnimatingAppWindowTokenRegistry {
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
                int i2 = this.mTmpRunnableList.size() - 1;
                while (true) {
                    int i3 = i2;
                    if (i3 >= 0) {
                        this.mTmpRunnableList.get(i3).run();
                        i2 = i3 - 1;
                    } else {
                        this.mTmpRunnableList.clear();
                        this.mEndingDeferredFinish = false;
                        return;
                    }
                }
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
