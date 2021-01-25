package com.android.server.display;

import android.view.SurfaceControl;

public class SurfaceControlExt {

    public static class TransactionEx {
        SurfaceControl.Transaction mTransaction;

        public TransactionEx(SurfaceControl.Transaction transaction) {
            this.mTransaction = transaction;
        }

        public SurfaceControl.Transaction getTransaction() {
            return this.mTransaction;
        }
    }
}
