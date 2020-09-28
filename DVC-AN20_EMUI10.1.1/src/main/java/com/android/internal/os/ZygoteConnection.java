package com.android.internal.os;

import android.annotation.UnsupportedAppUsage;
import android.content.pm.ApplicationInfo;
import android.metrics.LogMaker;
import android.net.Credentials;
import android.net.LocalSocket;
import android.os.Parcel;
import android.os.Process;
import android.os.Trace;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructPollfd;
import android.util.Log;
import android.util.StatsLog;
import android.util.TimeUtils;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;
import dalvik.system.VMRuntime;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import libcore.io.IoUtils;

/* access modifiers changed from: package-private */
public class ZygoteConnection {
    private static final int LENGTH = 2;
    private static final String TAG = "Zygote";
    private final String abiList;
    private boolean isEof;
    @UnsupportedAppUsage
    private final LocalSocket mSocket;
    @UnsupportedAppUsage
    private final DataOutputStream mSocketOutStream;
    private final BufferedReader mSocketReader;
    @UnsupportedAppUsage
    private final Credentials peer;

    ZygoteConnection(LocalSocket socket, String abiList2) throws IOException {
        this.mSocket = socket;
        this.abiList = abiList2;
        this.mSocketOutStream = new DataOutputStream(socket.getOutputStream());
        this.mSocketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()), 256);
        this.mSocket.setSoTimeout(1000);
        try {
            this.peer = this.mSocket.getPeerCredentials();
            this.isEof = false;
        } catch (IOException ex) {
            Log.e(TAG, "Cannot read peer credentials", ex);
            throw ex;
        }
    }

    /* access modifiers changed from: package-private */
    public FileDescriptor getFileDescriptor() {
        return this.mSocket.getFileDescriptor();
    }

    /* JADX INFO: Multiple debug info for r0v9 int[]: [D('fdsToClose' int[]), D('errnoEx' android.system.ErrnoException)] */
    /* access modifiers changed from: package-private */
    public Runnable processOneCommand(ZygoteServer zygoteServer) {
        ZygoteConnection zygoteConnection;
        FileDescriptor childPipeFd;
        try {
            String[] args = Zygote.readArgumentList(this.mSocketReader);
            FileDescriptor[] descriptors = this.mSocket.getAncillaryFileDescriptors();
            if (args == null) {
                this.isEof = true;
                return null;
            }
            boolean argsIsNull = args[0] != null;
            boolean argsIsCompliance = args.length == 2 && args[0].equals("updateHwThemeZipsAndIcons");
            if (!argsIsNull || !argsIsCompliance) {
                FileDescriptor childPipeFd2 = null;
                FileDescriptor serverPipeFd = null;
                ZygoteArguments parsedArgs = new ZygoteArguments(args);
                if (parsedArgs.mAbiListQuery) {
                    handleAbiListQuery();
                    return null;
                } else if (parsedArgs.mPidQuery) {
                    handlePidQuery();
                    return null;
                } else if (parsedArgs.mUsapPoolStatusSpecified) {
                    return handleUsapPoolStatusChange(zygoteServer, parsedArgs.mUsapPoolEnabled);
                } else {
                    if (parsedArgs.mPreloadDefault) {
                        handlePreload();
                        return null;
                    } else if (parsedArgs.mPreloadPackage != null) {
                        handlePreloadPackage(parsedArgs.mPreloadPackage, parsedArgs.mPreloadPackageLibs, parsedArgs.mPreloadPackageLibFileName, parsedArgs.mPreloadPackageCacheKey);
                        return null;
                    } else if (canPreloadApp() && parsedArgs.mPreloadApp != null) {
                        byte[] rawParcelData = Base64.getDecoder().decode(parsedArgs.mPreloadApp);
                        Parcel appInfoParcel = Parcel.obtain();
                        appInfoParcel.unmarshall(rawParcelData, 0, rawParcelData.length);
                        appInfoParcel.setDataPosition(0);
                        ApplicationInfo appInfo = ApplicationInfo.CREATOR.createFromParcel(appInfoParcel);
                        appInfoParcel.recycle();
                        if (appInfo != null) {
                            handlePreloadApp(appInfo);
                            return null;
                        }
                        throw new IllegalArgumentException("Failed to deserialize --preload-app");
                    } else if (parsedArgs.mApiBlacklistExemptions != null) {
                        return handleApiBlacklistExemptions(zygoteServer, parsedArgs.mApiBlacklistExemptions);
                    } else {
                        if (parsedArgs.mHiddenApiAccessLogSampleRate != -1) {
                            zygoteConnection = this;
                        } else if (parsedArgs.mHiddenApiAccessStatslogSampleRate != -1) {
                            zygoteConnection = this;
                        } else if (parsedArgs.mPermittedCapabilities == 0 && parsedArgs.mEffectiveCapabilities == 0) {
                            Zygote.applyUidSecurityPolicy(parsedArgs, this.peer);
                            Zygote.applyInvokeWithSecurityPolicy(parsedArgs, this.peer);
                            Zygote.applyDebuggerSystemProperty(parsedArgs);
                            Zygote.applyInvokeWithSystemProperty(parsedArgs);
                            int[][] rlimits = null;
                            if (parsedArgs.mRLimits != null) {
                                rlimits = (int[][]) parsedArgs.mRLimits.toArray(Zygote.INT_ARRAY_2D);
                            }
                            int[] fdsToIgnore = null;
                            if (parsedArgs.mInvokeWith != null) {
                                try {
                                    FileDescriptor[] pipeFds = Os.pipe2(OsConstants.O_CLOEXEC);
                                    childPipeFd2 = pipeFds[1];
                                    serverPipeFd = pipeFds[0];
                                    Os.fcntlInt(childPipeFd2, OsConstants.F_SETFD, 0);
                                    fdsToIgnore = new int[]{childPipeFd2.getInt$(), serverPipeFd.getInt$()};
                                } catch (ErrnoException errnoEx) {
                                    throw new IllegalStateException("Unable to set up pipe for invoke-with", errnoEx);
                                }
                            }
                            int[] fdsToClose = {-1, -1};
                            FileDescriptor fd = this.mSocket.getFileDescriptor();
                            if (fd != null) {
                                fdsToClose[0] = fd.getInt$();
                            }
                            FileDescriptor fd2 = zygoteServer.getZygoteSocketFileDescriptor();
                            if (fd2 != null) {
                                fdsToClose[1] = fd2.getInt$();
                            }
                            int pid = Zygote.forkAndSpecialize(parsedArgs.mUid, parsedArgs.mGid, parsedArgs.mGids, parsedArgs.mRuntimeFlags, rlimits, parsedArgs.mMountExternal, parsedArgs.mSeInfo, parsedArgs.mNiceName, fdsToClose, fdsToIgnore, parsedArgs.mStartChildZygote, parsedArgs.mInstructionSet, parsedArgs.mAppDataDir, parsedArgs.mTargetSdkVersion);
                            if (pid == 0) {
                                try {
                                    zygoteServer.setForkChild();
                                    zygoteServer.closeServerSocket();
                                    IoUtils.closeQuietly(serverPipeFd);
                                    serverPipeFd = null;
                                    childPipeFd = childPipeFd2;
                                    try {
                                        Runnable handleChildProc = handleChildProc(parsedArgs, descriptors, childPipeFd, parsedArgs.mStartChildZygote);
                                        IoUtils.closeQuietly(childPipeFd);
                                        IoUtils.closeQuietly((FileDescriptor) null);
                                        return handleChildProc;
                                    } catch (Throwable th) {
                                        th = th;
                                        IoUtils.closeQuietly(childPipeFd);
                                        IoUtils.closeQuietly(serverPipeFd);
                                        throw th;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    childPipeFd = childPipeFd2;
                                    IoUtils.closeQuietly(childPipeFd);
                                    IoUtils.closeQuietly(serverPipeFd);
                                    throw th;
                                }
                            } else {
                                IoUtils.closeQuietly(childPipeFd2);
                                handleParentProc(pid, descriptors, serverPipeFd);
                                IoUtils.closeQuietly((FileDescriptor) null);
                                IoUtils.closeQuietly(serverPipeFd);
                                return null;
                            }
                        } else {
                            throw new ZygoteSecurityException("Client may not specify capabilities: permitted=0x" + Long.toHexString(parsedArgs.mPermittedCapabilities) + ", effective=0x" + Long.toHexString(parsedArgs.mEffectiveCapabilities));
                        }
                        return zygoteConnection.handleHiddenApiAccessLogSampleRate(zygoteServer, parsedArgs.mHiddenApiAccessLogSampleRate, parsedArgs.mHiddenApiAccessStatslogSampleRate);
                    }
                }
            } else {
                int currentUserId = 0;
                if (args[1] != null && args[1].startsWith("setuid=")) {
                    currentUserId = Integer.parseInt(args[1].substring(args[1].indexOf(61) + 1));
                }
                ZygoteInit.clearHwThemeZipsAndSomeIcons();
                ZygoteInit.preloadHwThemeZipsAndSomeIcons(currentUserId);
                try {
                    this.mSocketOutStream.writeInt(1);
                    this.mSocketOutStream.writeBoolean(false);
                } catch (IOException ex) {
                    Log.e(TAG, "Error writing to command socket", ex);
                }
                return null;
            }
        } catch (IOException ex2) {
            throw new IllegalStateException("IOException on command socket", ex2);
        }
    }

    private void handleAbiListQuery() {
        try {
            byte[] abiListBytes = this.abiList.getBytes(StandardCharsets.US_ASCII);
            this.mSocketOutStream.writeInt(abiListBytes.length);
            this.mSocketOutStream.write(abiListBytes);
        } catch (IOException ioe) {
            throw new IllegalStateException("Error writing to command socket", ioe);
        }
    }

    private void handlePidQuery() {
        try {
            byte[] pidStringBytes = String.valueOf(Process.myPid()).getBytes(StandardCharsets.US_ASCII);
            this.mSocketOutStream.writeInt(pidStringBytes.length);
            this.mSocketOutStream.write(pidStringBytes);
        } catch (IOException ioe) {
            throw new IllegalStateException("Error writing to command socket", ioe);
        }
    }

    private void handlePreload() {
        try {
            if (isPreloadComplete()) {
                this.mSocketOutStream.writeInt(1);
                return;
            }
            preload();
            this.mSocketOutStream.writeInt(0);
        } catch (IOException ioe) {
            throw new IllegalStateException("Error writing to command socket", ioe);
        }
    }

    private Runnable stateChangeWithUsapPoolReset(ZygoteServer zygoteServer, Runnable stateChangeCode) {
        try {
            if (zygoteServer.isUsapPoolEnabled()) {
                Log.i(TAG, "Emptying USAP Pool due to state change.");
                Zygote.emptyUsapPool();
            }
            stateChangeCode.run();
            if (zygoteServer.isUsapPoolEnabled()) {
                Runnable fpResult = zygoteServer.fillUsapPool(new int[]{this.mSocket.getFileDescriptor().getInt$()});
                if (fpResult != null) {
                    zygoteServer.setForkChild();
                    return fpResult;
                }
                Log.i(TAG, "Finished refilling USAP Pool after state change.");
            }
            this.mSocketOutStream.writeInt(0);
            return null;
        } catch (IOException ioe) {
            throw new IllegalStateException("Error writing to command socket", ioe);
        }
    }

    private Runnable handleApiBlacklistExemptions(ZygoteServer zygoteServer, String[] exemptions) {
        return stateChangeWithUsapPoolReset(zygoteServer, new Runnable(exemptions) {
            /* class com.android.internal.os.$$Lambda$ZygoteConnection$xjqM7qW7vAjTqh2tR5XRF5Vn5mk */
            private final /* synthetic */ String[] f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                ZygoteInit.setApiBlacklistExemptions(this.f$0);
            }
        });
    }

    private Runnable handleUsapPoolStatusChange(ZygoteServer zygoteServer, boolean newStatus) {
        try {
            Runnable fpResult = zygoteServer.setUsapPoolStatus(newStatus, this.mSocket);
            if (fpResult == null) {
                this.mSocketOutStream.writeInt(0);
            } else {
                zygoteServer.setForkChild();
            }
            return fpResult;
        } catch (IOException ioe) {
            throw new IllegalStateException("Error writing to command socket", ioe);
        }
    }

    /* access modifiers changed from: private */
    public static class HiddenApiUsageLogger implements VMRuntime.HiddenApiUsageLogger {
        private static HiddenApiUsageLogger sInstance = new HiddenApiUsageLogger();
        private int mHiddenApiAccessLogSampleRate = 0;
        private int mHiddenApiAccessStatslogSampleRate = 0;
        private final MetricsLogger mMetricsLogger = new MetricsLogger();

        private HiddenApiUsageLogger() {
        }

        public static void setHiddenApiAccessLogSampleRates(int sampleRate, int newSampleRate) {
            if (sampleRate != -1) {
                sInstance.mHiddenApiAccessLogSampleRate = sampleRate;
            }
            if (newSampleRate != -1) {
                sInstance.mHiddenApiAccessStatslogSampleRate = newSampleRate;
            }
        }

        public static HiddenApiUsageLogger getInstance() {
            return sInstance;
        }

        public void hiddenApiUsed(int sampledValue, String packageName, String signature, int accessMethod, boolean accessDenied) {
            if (sampledValue < this.mHiddenApiAccessLogSampleRate) {
                logUsage(packageName, signature, accessMethod, accessDenied);
            }
            if (sampledValue < this.mHiddenApiAccessStatslogSampleRate) {
                newLogUsage(signature, accessMethod, accessDenied);
            }
        }

        private void logUsage(String packageName, String signature, int accessMethod, boolean accessDenied) {
            int accessMethodMetric = 0;
            if (accessMethod == 0) {
                accessMethodMetric = 0;
            } else if (accessMethod == 1) {
                accessMethodMetric = 1;
            } else if (accessMethod == 2) {
                accessMethodMetric = 2;
            } else if (accessMethod == 3) {
                accessMethodMetric = 3;
            }
            LogMaker logMaker = new LogMaker((int) MetricsProto.MetricsEvent.ACTION_HIDDEN_API_ACCESSED).setPackageName(packageName).addTaggedData(MetricsProto.MetricsEvent.FIELD_HIDDEN_API_SIGNATURE, signature).addTaggedData(MetricsProto.MetricsEvent.FIELD_HIDDEN_API_ACCESS_METHOD, Integer.valueOf(accessMethodMetric));
            if (accessDenied) {
                logMaker.addTaggedData(MetricsProto.MetricsEvent.FIELD_HIDDEN_API_ACCESS_DENIED, 1);
            }
            this.mMetricsLogger.write(logMaker);
        }

        private void newLogUsage(String signature, int accessMethod, boolean accessDenied) {
            int accessMethodProto = 0;
            if (accessMethod == 0) {
                accessMethodProto = 0;
            } else if (accessMethod == 1) {
                accessMethodProto = 1;
            } else if (accessMethod == 2) {
                accessMethodProto = 2;
            } else if (accessMethod == 3) {
                accessMethodProto = 3;
            }
            StatsLog.write(178, Process.myUid(), signature, accessMethodProto, accessDenied);
        }
    }

    private Runnable handleHiddenApiAccessLogSampleRate(ZygoteServer zygoteServer, int samplingRate, int statsdSamplingRate) {
        return stateChangeWithUsapPoolReset(zygoteServer, new Runnable(samplingRate, statsdSamplingRate) {
            /* class com.android.internal.os.$$Lambda$ZygoteConnection$KxVsZs4KsanePOHCU5JcuypPik */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run() {
                ZygoteConnection.lambda$handleHiddenApiAccessLogSampleRate$1(this.f$0, this.f$1);
            }
        });
    }

    static /* synthetic */ void lambda$handleHiddenApiAccessLogSampleRate$1(int samplingRate, int statsdSamplingRate) {
        ZygoteInit.setHiddenApiAccessLogSampleRate(Math.max(samplingRate, statsdSamplingRate));
        HiddenApiUsageLogger.setHiddenApiAccessLogSampleRates(samplingRate, statsdSamplingRate);
        ZygoteInit.setHiddenApiUsageLogger(HiddenApiUsageLogger.getInstance());
    }

    /* access modifiers changed from: protected */
    public void preload() {
        ZygoteInit.lazyPreload();
    }

    /* access modifiers changed from: protected */
    public boolean isPreloadComplete() {
        return ZygoteInit.isPreloadComplete();
    }

    /* access modifiers changed from: protected */
    public DataOutputStream getSocketOutputStream() {
        return this.mSocketOutStream;
    }

    /* access modifiers changed from: protected */
    public void handlePreloadPackage(String packagePath, String libsPath, String libFileName, String cacheKey) {
        throw new RuntimeException("Zygote does not support package preloading");
    }

    /* access modifiers changed from: protected */
    public boolean canPreloadApp() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void handlePreloadApp(ApplicationInfo aInfo) {
        throw new RuntimeException("Zygote does not support app preloading");
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void closeSocket() {
        try {
            this.mSocket.close();
        } catch (IOException ex) {
            Log.e(TAG, "Exception while closing command socket in parent", ex);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isClosedByPeer() {
        return this.isEof;
    }

    private Runnable handleChildProc(ZygoteArguments parsedArgs, FileDescriptor[] descriptors, FileDescriptor pipeFd, boolean isZygote) {
        closeSocket();
        if (descriptors != null) {
            try {
                Os.dup2(descriptors[0], OsConstants.STDIN_FILENO);
                Os.dup2(descriptors[1], OsConstants.STDOUT_FILENO);
                Os.dup2(descriptors[2], OsConstants.STDERR_FILENO);
                for (FileDescriptor fd : descriptors) {
                    IoUtils.closeQuietly(fd);
                }
            } catch (ErrnoException ex) {
                Log.e(TAG, "Error reopening stdio", ex);
            }
        }
        if (parsedArgs.mNiceName != null) {
            Process.setArgV0(parsedArgs.mNiceName);
        }
        Trace.traceEnd(64);
        if (parsedArgs.mInvokeWith != null) {
            WrapperInit.execApplication(parsedArgs.mInvokeWith, parsedArgs.mNiceName, parsedArgs.mTargetSdkVersion, VMRuntime.getCurrentInstructionSet(), pipeFd, parsedArgs.mRemainingArgs);
            throw new IllegalStateException("WrapperInit.execApplication unexpectedly returned");
        } else if (!isZygote) {
            return ZygoteInit.zygoteInit(parsedArgs.mTargetSdkVersion, parsedArgs.mRemainingArgs, null);
        } else {
            return ZygoteInit.childZygoteInit(parsedArgs.mTargetSdkVersion, parsedArgs.mRemainingArgs, null);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x00c9  */
    private void handleParentProc(int pid, FileDescriptor[] descriptors, FileDescriptor pipeFd) {
        boolean usingWrapper;
        boolean usingWrapper2;
        int innerPid;
        char c;
        int pid2 = pid;
        if (pid2 > 0) {
            setChildPgid(pid);
        }
        char c2 = 0;
        if (descriptors != null) {
            for (FileDescriptor fd : descriptors) {
                IoUtils.closeQuietly(fd);
            }
        }
        boolean usingWrapper3 = false;
        if (pipeFd == null || pid2 <= 0) {
            usingWrapper2 = false;
        } else {
            int innerPid2 = -1;
            try {
                StructPollfd[] fds = {new StructPollfd()};
                byte[] data = new byte[4];
                int remainingSleepTime = 30000;
                int dataIndex = 0;
                long startTime = System.nanoTime();
                while (true) {
                    if (dataIndex >= data.length || remainingSleepTime <= 0) {
                        usingWrapper2 = usingWrapper3;
                        innerPid = innerPid2;
                    } else {
                        fds[c2].fd = pipeFd;
                        fds[c2].events = (short) OsConstants.POLLIN;
                        fds[0].revents = 0;
                        fds[0].userData = null;
                        int res = Os.poll(fds, remainingSleepTime);
                        usingWrapper2 = usingWrapper3;
                        innerPid = innerPid2;
                        try {
                            remainingSleepTime = 30000 - ((int) ((System.nanoTime() - startTime) / TimeUtils.NANOS_PER_MS));
                            if (res > 0) {
                                c = 0;
                                if ((fds[0].revents & OsConstants.POLLIN) == 0) {
                                    break;
                                }
                                int readBytes = Os.read(pipeFd, data, dataIndex, 1);
                                if (readBytes >= 0) {
                                    dataIndex += readBytes;
                                } else {
                                    throw new RuntimeException("Some error");
                                }
                            } else {
                                c = 0;
                                if (res == 0) {
                                    Log.w(TAG, "Timed out waiting for child.");
                                }
                            }
                            c2 = c;
                            usingWrapper3 = usingWrapper2;
                            innerPid2 = innerPid;
                        } catch (Exception e) {
                            ex = e;
                            innerPid2 = innerPid;
                            Log.w(TAG, "Error reading pid from wrapped process, child may have died", ex);
                            if (innerPid2 > 0) {
                            }
                            usingWrapper = usingWrapper2;
                            this.mSocketOutStream.writeInt(pid2);
                            this.mSocketOutStream.writeBoolean(usingWrapper);
                        }
                    }
                }
                usingWrapper2 = usingWrapper3;
                innerPid = innerPid2;
                if (dataIndex == data.length) {
                    innerPid2 = new DataInputStream(new ByteArrayInputStream(data)).readInt();
                } else {
                    innerPid2 = innerPid;
                }
                if (innerPid2 == -1) {
                    try {
                        Log.w(TAG, "Error reading pid from wrapped process, child may have died");
                    } catch (Exception e2) {
                        ex = e2;
                    }
                }
            } catch (Exception e3) {
                ex = e3;
                usingWrapper2 = false;
                Log.w(TAG, "Error reading pid from wrapped process, child may have died", ex);
                if (innerPid2 > 0) {
                }
                usingWrapper = usingWrapper2;
                this.mSocketOutStream.writeInt(pid2);
                this.mSocketOutStream.writeBoolean(usingWrapper);
            }
            if (innerPid2 > 0) {
                int parentPid = innerPid2;
                while (parentPid > 0 && parentPid != pid2) {
                    parentPid = Process.getParentPid(parentPid);
                }
                if (parentPid > 0) {
                    Log.i(TAG, "Wrapped process has pid " + innerPid2);
                    pid2 = innerPid2;
                    usingWrapper = true;
                    this.mSocketOutStream.writeInt(pid2);
                    this.mSocketOutStream.writeBoolean(usingWrapper);
                }
                Log.w(TAG, "Wrapped process reported a pid that is not a child of the process that we forked: childPid=" + pid2 + " innerPid=" + innerPid2);
            }
        }
        usingWrapper = usingWrapper2;
        try {
            this.mSocketOutStream.writeInt(pid2);
            this.mSocketOutStream.writeBoolean(usingWrapper);
        } catch (IOException ex) {
            throw new IllegalStateException("Error writing to command socket", ex);
        }
    }

    private void setChildPgid(int pid) {
        try {
            Os.setpgid(pid, Os.getpgid(this.peer.getPid()));
        } catch (ErrnoException e) {
            Log.i(TAG, "Zygote: setpgid failed. This is normal if peer is not in our session");
        }
    }
}
