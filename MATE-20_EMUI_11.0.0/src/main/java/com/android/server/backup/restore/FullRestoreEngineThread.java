package com.android.server.backup.restore;

import android.os.ParcelFileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import libcore.io.IoUtils;

class FullRestoreEngineThread implements Runnable {
    FullRestoreEngine mEngine;
    InputStream mEngineStream;
    private final boolean mMustKillAgent = false;

    FullRestoreEngineThread(FullRestoreEngine engine, ParcelFileDescriptor engineSocket) {
        this.mEngine = engine;
        engine.setRunning(true);
        this.mEngineStream = new FileInputStream(engineSocket.getFileDescriptor(), true);
    }

    FullRestoreEngineThread(FullRestoreEngine engine, InputStream inputStream) {
        this.mEngine = engine;
        engine.setRunning(true);
        this.mEngineStream = inputStream;
    }

    public boolean isRunning() {
        return this.mEngine.isRunning();
    }

    public int waitForResult() {
        return this.mEngine.waitForResult();
    }

    @Override // java.lang.Runnable
    public void run() {
        while (this.mEngine.isRunning()) {
            try {
                this.mEngine.restoreOneFile(this.mEngineStream, this.mMustKillAgent, this.mEngine.mBuffer, this.mEngine.mOnlyPackage, this.mEngine.mAllowApks, this.mEngine.mEphemeralOpToken, this.mEngine.mMonitor);
            } finally {
                IoUtils.closeQuietly(this.mEngineStream);
            }
        }
    }

    public void handleTimeout() {
        IoUtils.closeQuietly(this.mEngineStream);
        this.mEngine.handleTimeout();
    }
}
