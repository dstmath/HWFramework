package com.huawei.android.fsm;

import android.os.Bundle;
import com.huawei.android.fsm.IHwFoldScreenManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class IHwFoldScreenManagerEx {
    private IHwFoldScreenManager mManager = new IHwFoldScreenManager.Stub() {
        /* class com.huawei.android.fsm.IHwFoldScreenManagerEx.AnonymousClass1 */

        public int getPosture() {
            return IHwFoldScreenManagerEx.this.getPosture();
        }

        public int getFoldableState() {
            return IHwFoldScreenManagerEx.this.getFoldableState();
        }

        public void registerFoldableState(IFoldableStateListener listener, int type) {
            IHwFoldScreenManagerEx.this.registerFoldableState(listener, type);
        }

        public void unregisterFoldableState(IFoldableStateListener listener) {
            IHwFoldScreenManagerEx.this.unregisterFoldableState(listener);
        }

        public int setDisplayMode(int mode) {
            return IHwFoldScreenManagerEx.this.setDisplayMode(mode);
        }

        public int getDisplayMode() {
            return IHwFoldScreenManagerEx.this.getDisplayMode();
        }

        public int lockDisplayMode(int mode) {
            return IHwFoldScreenManagerEx.this.lockDisplayMode(mode);
        }

        public int unlockDisplayMode() {
            return IHwFoldScreenManagerEx.this.unlockDisplayMode();
        }

        public void registerFoldDisplayMode(IFoldDisplayModeListener listener) {
            IHwFoldScreenManagerEx.this.registerFoldDisplayMode(listener);
        }

        public void unregisterFoldDisplayMode(IFoldDisplayModeListener listener) {
            IHwFoldScreenManagerEx.this.unregisterFoldDisplayMode(listener);
        }

        public void registerFsmTipsRequestListener(IFoldFsmTipsRequestListener listener, int type) {
            IHwFoldScreenManagerEx.this.registerFsmTipsRequestListener(listener, type);
        }

        public void unregisterFsmTipsRequestListener(IFoldFsmTipsRequestListener listener) {
            IHwFoldScreenManagerEx.this.unregisterFsmTipsRequestListener(listener);
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            IHwFoldScreenManagerEx.this.dump(fd, pw, args);
        }

        public int reqShowTipsToFsm(int reqTipsType, Bundle data) {
            return IHwFoldScreenManagerEx.this.reqShowTipsToFsm(reqTipsType, data);
        }
    };

    public IHwFoldScreenManager getIHwFoldScreenManager() {
        return this.mManager;
    }

    public int getPosture() {
        return 100;
    }

    public int getFoldableState() {
        return 0;
    }

    public void registerFoldableState(IFoldableStateListener listener, int type) {
    }

    public void unregisterFoldableState(IFoldableStateListener listener) {
    }

    public int setDisplayMode(int mode) {
        return 0;
    }

    public int getDisplayMode() {
        return 0;
    }

    public int lockDisplayMode(int mode) {
        return 0;
    }

    public int unlockDisplayMode() {
        return 0;
    }

    public void registerFoldDisplayMode(IFoldDisplayModeListener listener) {
    }

    public void unregisterFoldDisplayMode(IFoldDisplayModeListener listener) {
    }

    public void registerFsmTipsRequestListener(IFoldFsmTipsRequestListener listener, int type) {
    }

    public void unregisterFsmTipsRequestListener(IFoldFsmTipsRequestListener listener) {
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
    }

    public int reqShowTipsToFsm(int reqTipsType, Bundle data) {
        return reqTipsType;
    }
}
