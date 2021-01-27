package com.android.server;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.persistentdata.IPersistentDataBlockService;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.DumpState;
import com.android.server.slice.SliceClientPermissions;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import libcore.io.IoUtils;

public class PersistentDataBlockService extends SystemService {
    public static final int DIGEST_SIZE_BYTES = 32;
    private static final String FLASH_LOCK_LOCKED = "1";
    private static final String FLASH_LOCK_PROP = "ro.boot.flash.locked";
    private static final String FLASH_LOCK_UNLOCKED = "0";
    private static final int FRP_CREDENTIAL_RESERVED_SIZE = 1000;
    private static final int HEADER_SIZE = 8;
    private static final int MAX_DATA_BLOCK_SIZE = 102400;
    private static final int MAX_FRP_CREDENTIAL_HANDLE_SIZE = 996;
    private static final int MAX_TEST_MODE_DATA_SIZE = 9996;
    private static final String OEM_UNLOCK_PROP = "sys.oem_unlock_allowed";
    private static final int PARTITION_TYPE_MARKER = 428873843;
    private static final String PERSISTENT_DATA_BLOCK_PROP = "ro.frp.pst";
    private static final String TAG = PersistentDataBlockService.class.getSimpleName();
    private static final int TEST_MODE_RESERVED_SIZE = 10000;
    private int mAllowedUid = -1;
    private long mBlockDeviceSize;
    private final Context mContext;
    private final String mDataBlockFile;
    private final CountDownLatch mInitDoneSignal = new CountDownLatch(1);
    private PersistentDataBlockManagerInternal mInternalService = new PersistentDataBlockManagerInternal() {
        /* class com.android.server.PersistentDataBlockService.AnonymousClass2 */

        @Override // com.android.server.PersistentDataBlockManagerInternal
        public void setFrpCredentialHandle(byte[] handle) {
            writeInternal(handle, PersistentDataBlockService.this.getFrpCredentialDataOffset(), PersistentDataBlockService.MAX_FRP_CREDENTIAL_HANDLE_SIZE);
        }

        @Override // com.android.server.PersistentDataBlockManagerInternal
        public byte[] getFrpCredentialHandle() {
            return readInternal(PersistentDataBlockService.this.getFrpCredentialDataOffset(), PersistentDataBlockService.MAX_FRP_CREDENTIAL_HANDLE_SIZE);
        }

        @Override // com.android.server.PersistentDataBlockManagerInternal
        public void setTestHarnessModeData(byte[] data) {
            writeInternal(data, PersistentDataBlockService.this.getTestHarnessModeDataOffset(), PersistentDataBlockService.MAX_TEST_MODE_DATA_SIZE);
        }

        @Override // com.android.server.PersistentDataBlockManagerInternal
        public byte[] getTestHarnessModeData() {
            byte[] data = readInternal(PersistentDataBlockService.this.getTestHarnessModeDataOffset(), PersistentDataBlockService.MAX_TEST_MODE_DATA_SIZE);
            if (data == null) {
                return new byte[0];
            }
            return data;
        }

        @Override // com.android.server.PersistentDataBlockManagerInternal
        public void clearTestHarnessModeData() {
            writeDataBuffer(PersistentDataBlockService.this.getTestHarnessModeDataOffset(), ByteBuffer.allocate(Math.min((int) PersistentDataBlockService.MAX_TEST_MODE_DATA_SIZE, getTestHarnessModeData().length) + 4));
        }

        private void writeInternal(byte[] data, long offset, int dataLength) {
            boolean z = true;
            int i = 0;
            Preconditions.checkArgument(data == null || data.length > 0, "data must be null or non-empty");
            if (data != null && data.length > dataLength) {
                z = false;
            }
            Preconditions.checkArgument(z, "data must not be longer than " + dataLength);
            ByteBuffer dataBuffer = ByteBuffer.allocate(dataLength + 4);
            if (data != null) {
                i = data.length;
            }
            dataBuffer.putInt(i);
            if (data != null) {
                dataBuffer.put(data);
            }
            dataBuffer.flip();
            writeDataBuffer(offset, dataBuffer);
        }

        private void writeDataBuffer(long offset, ByteBuffer dataBuffer) {
            try {
                FileOutputStream outputStream = new FileOutputStream(new File(PersistentDataBlockService.this.mDataBlockFile));
                synchronized (PersistentDataBlockService.this.mLock) {
                    if (!PersistentDataBlockService.this.mIsWritable) {
                        IoUtils.closeQuietly(outputStream);
                        return;
                    }
                    try {
                        FileChannel channel = outputStream.getChannel();
                        channel.position(offset);
                        channel.write(dataBuffer);
                        outputStream.flush();
                        IoUtils.closeQuietly(outputStream);
                        PersistentDataBlockService.this.computeAndWriteDigestLocked();
                    } catch (IOException e) {
                        Slog.e(PersistentDataBlockService.TAG, "unable to access persistent partition", e);
                        IoUtils.closeQuietly(outputStream);
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(outputStream);
                        throw th;
                    }
                }
            } catch (FileNotFoundException e2) {
                Slog.e(PersistentDataBlockService.TAG, "partition not available", e2);
            }
        }

        private byte[] readInternal(long offset, int maxLength) {
            if (PersistentDataBlockService.this.enforceChecksumValidity()) {
                try {
                    DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(PersistentDataBlockService.this.mDataBlockFile)));
                    try {
                        synchronized (PersistentDataBlockService.this.mLock) {
                            inputStream.skip(offset);
                            int length = inputStream.readInt();
                            if (length > 0) {
                                if (length <= maxLength) {
                                    byte[] bytes = new byte[length];
                                    inputStream.readFully(bytes);
                                    IoUtils.closeQuietly(inputStream);
                                    return bytes;
                                }
                            }
                            IoUtils.closeQuietly(inputStream);
                            return null;
                        }
                    } catch (IOException e) {
                        throw new IllegalStateException("persistent partition not readable", e);
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(inputStream);
                        throw th;
                    }
                } catch (FileNotFoundException e2) {
                    throw new IllegalStateException("persistent partition not available");
                }
            } else {
                throw new IllegalStateException("invalid checksum");
            }
        }

        @Override // com.android.server.PersistentDataBlockManagerInternal
        public void forceOemUnlockEnabled(boolean enabled) {
            synchronized (PersistentDataBlockService.this.mLock) {
                PersistentDataBlockService.this.doSetOemUnlockEnabledLocked(enabled);
                PersistentDataBlockService.this.computeAndWriteDigestLocked();
            }
        }
    };
    @GuardedBy({"mLock"})
    private boolean mIsWritable = true;
    private final Object mLock = new Object();
    private final IBinder mService = new IPersistentDataBlockService.Stub() {
        /* class com.android.server.PersistentDataBlockService.AnonymousClass1 */

        public int write(byte[] data) throws RemoteException {
            String str = PersistentDataBlockService.TAG;
            Slog.i(str, "write data, callingUid=" + Binder.getCallingUid() + ", callingPid=" + Binder.getCallingPid());
            PersistentDataBlockService.this.enforceUid(Binder.getCallingUid());
            long maxBlockSize = PersistentDataBlockService.this.doGetMaximumDataBlockSize();
            if (((long) data.length) > maxBlockSize) {
                return (int) (-maxBlockSize);
            }
            try {
                DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(new File(PersistentDataBlockService.this.mDataBlockFile)));
                ByteBuffer headerAndData = ByteBuffer.allocate(data.length + 8);
                headerAndData.putInt(PersistentDataBlockService.PARTITION_TYPE_MARKER);
                headerAndData.putInt(data.length);
                headerAndData.put(data);
                synchronized (PersistentDataBlockService.this.mLock) {
                    if (!PersistentDataBlockService.this.mIsWritable) {
                        IoUtils.closeQuietly(outputStream);
                        return -1;
                    }
                    try {
                        outputStream.write(new byte[32], 0, 32);
                        outputStream.write(headerAndData.array());
                        outputStream.flush();
                        IoUtils.closeQuietly(outputStream);
                        if (!PersistentDataBlockService.this.computeAndWriteDigestLocked()) {
                            return -1;
                        }
                        return data.length;
                    } catch (IOException e) {
                        Slog.e(PersistentDataBlockService.TAG, "failed writing to the persistent data block", e);
                        IoUtils.closeQuietly(outputStream);
                        return -1;
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(outputStream);
                        throw th;
                    }
                }
            } catch (FileNotFoundException e2) {
                Slog.e(PersistentDataBlockService.TAG, "partition not available?", e2);
                return -1;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
            r2.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x007b, code lost:
            android.util.Slog.e(com.android.server.PersistentDataBlockService.TAG, "failed to close OutputStream");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
            r2.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x008b, code lost:
            android.util.Slog.e(com.android.server.PersistentDataBlockService.TAG, "failed to close OutputStream");
         */
        public byte[] read() {
            byte[] data;
            byte[] bArr;
            PersistentDataBlockService.this.enforceUid(Binder.getCallingUid());
            if (!PersistentDataBlockService.this.enforceChecksumValidity()) {
                return new byte[0];
            }
            try {
                DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(PersistentDataBlockService.this.mDataBlockFile)));
                try {
                    synchronized (PersistentDataBlockService.this.mLock) {
                        int totalDataSize = PersistentDataBlockService.this.getTotalDataSizeLocked(inputStream);
                        if (totalDataSize == 0) {
                            bArr = new byte[0];
                        } else {
                            data = new byte[totalDataSize];
                            int read = inputStream.read(data, 0, totalDataSize);
                            if (read < totalDataSize) {
                                String str = PersistentDataBlockService.TAG;
                                Slog.e(str, "failed to read entire data block. bytes read: " + read + SliceClientPermissions.SliceAuthority.DELIMITER + totalDataSize);
                            }
                        }
                    }
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Slog.e(PersistentDataBlockService.TAG, "failed to close OutputStream");
                    }
                    return bArr;
                } catch (IOException e2) {
                    Slog.e(PersistentDataBlockService.TAG, "failed to read data", e2);
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                        Slog.e(PersistentDataBlockService.TAG, "failed to close OutputStream");
                    }
                    return null;
                } catch (Throwable th) {
                    try {
                        inputStream.close();
                    } catch (IOException e4) {
                        Slog.e(PersistentDataBlockService.TAG, "failed to close OutputStream");
                    }
                    throw th;
                }
            } catch (FileNotFoundException e5) {
                Slog.e(PersistentDataBlockService.TAG, "partition not available?", e5);
                return null;
            }
            return null;
            return data;
        }

        public void wipe() {
            PersistentDataBlockService.this.enforceOemUnlockWritePermission();
            synchronized (PersistentDataBlockService.this.mLock) {
                if (PersistentDataBlockService.this.nativeWipe(PersistentDataBlockService.this.mDataBlockFile) < 0) {
                    Slog.e(PersistentDataBlockService.TAG, "failed to wipe persistent partition");
                } else {
                    PersistentDataBlockService.this.mIsWritable = false;
                    Slog.i(PersistentDataBlockService.TAG, "persistent partition now wiped and unwritable");
                }
            }
        }

        public void setOemUnlockEnabled(boolean enabled) throws SecurityException {
            if (!ActivityManager.isUserAMonkey()) {
                PersistentDataBlockService.this.enforceOemUnlockWritePermission();
                PersistentDataBlockService.this.enforceIsAdmin();
                if (enabled) {
                    PersistentDataBlockService.this.enforceUserRestriction("no_oem_unlock");
                    PersistentDataBlockService.this.enforceUserRestriction("no_factory_reset");
                }
                synchronized (PersistentDataBlockService.this.mLock) {
                    PersistentDataBlockService.this.doSetOemUnlockEnabledLocked(enabled);
                    PersistentDataBlockService.this.computeAndWriteDigestLocked();
                }
            }
        }

        public boolean getOemUnlockEnabled() {
            PersistentDataBlockService.this.enforceOemUnlockReadPermission();
            return PersistentDataBlockService.this.doGetOemUnlockEnabled();
        }

        /* JADX WARNING: Removed duplicated region for block: B:12:0x0033  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0037 A[RETURN] */
        public int getFlashLockState() {
            boolean z;
            PersistentDataBlockService.this.enforceOemUnlockReadPermission();
            String locked = SystemProperties.get(PersistentDataBlockService.FLASH_LOCK_PROP);
            int hashCode = locked.hashCode();
            if (hashCode != 48) {
                if (hashCode == 49 && locked.equals(PersistentDataBlockService.FLASH_LOCK_LOCKED)) {
                    z = false;
                    if (z) {
                        return !z ? -1 : 0;
                    }
                    return 1;
                }
            } else if (locked.equals(PersistentDataBlockService.FLASH_LOCK_UNLOCKED)) {
                z = true;
                if (z) {
                }
            }
            z = true;
            if (z) {
            }
        }

        public int getDataBlockSize() {
            int totalDataSizeLocked;
            enforcePersistentDataBlockAccess();
            try {
                DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(PersistentDataBlockService.this.mDataBlockFile)));
                try {
                    synchronized (PersistentDataBlockService.this.mLock) {
                        totalDataSizeLocked = PersistentDataBlockService.this.getTotalDataSizeLocked(inputStream);
                    }
                    return totalDataSizeLocked;
                } catch (IOException e) {
                    Slog.e(PersistentDataBlockService.TAG, "error reading data block size");
                    return 0;
                } finally {
                    IoUtils.closeQuietly(inputStream);
                }
            } catch (FileNotFoundException e2) {
                Slog.e(PersistentDataBlockService.TAG, "partition not available");
                return 0;
            }
        }

        private void enforcePersistentDataBlockAccess() {
            if (PersistentDataBlockService.this.mContext.checkCallingPermission("android.permission.ACCESS_PDB_STATE") != 0) {
                PersistentDataBlockService.this.enforceUid(Binder.getCallingUid());
            }
        }

        public long getMaximumDataBlockSize() {
            PersistentDataBlockService.this.enforceUid(Binder.getCallingUid());
            return PersistentDataBlockService.this.doGetMaximumDataBlockSize();
        }

        public boolean hasFrpCredentialHandle() {
            enforcePersistentDataBlockAccess();
            try {
                return PersistentDataBlockService.this.mInternalService.getFrpCredentialHandle() != null;
            } catch (IllegalStateException e) {
                Slog.e(PersistentDataBlockService.TAG, "error reading frp handle", e);
                throw new UnsupportedOperationException("cannot read frp credential");
            }
        }
    };

    private native long nativeGetBlockDeviceSize(String str);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native int nativeWipe(String str);

    /* JADX WARN: Type inference failed for: r0v3, types: [com.android.server.PersistentDataBlockService$1, android.os.IBinder] */
    public PersistentDataBlockService(Context context) {
        super(context);
        this.mContext = context;
        this.mDataBlockFile = SystemProperties.get(PERSISTENT_DATA_BLOCK_PROP);
        this.mBlockDeviceSize = -1;
    }

    private int getAllowedUid(int userHandle) {
        String allowedPackage = this.mContext.getResources().getString(17039875);
        try {
            return this.mContext.getPackageManager().getPackageUidAsUser(allowedPackage, DumpState.DUMP_DEXOPT, userHandle);
        } catch (PackageManager.NameNotFoundException e) {
            String str = TAG;
            Slog.e(str, "not able to find package " + allowedPackage, e);
            return -1;
        }
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        SystemServerInitThreadPool systemServerInitThreadPool = SystemServerInitThreadPool.get();
        $$Lambda$PersistentDataBlockService$EZl9OYaT2eNL7kfSr2nKUBjxidk r1 = new Runnable() {
            /* class com.android.server.$$Lambda$PersistentDataBlockService$EZl9OYaT2eNL7kfSr2nKUBjxidk */

            @Override // java.lang.Runnable
            public final void run() {
                PersistentDataBlockService.this.lambda$onStart$0$PersistentDataBlockService();
            }
        };
        systemServerInitThreadPool.submit(r1, TAG + ".onStart");
    }

    public /* synthetic */ void lambda$onStart$0$PersistentDataBlockService() {
        this.mAllowedUid = getAllowedUid(0);
        enforceChecksumValidity();
        formatIfOemUnlockEnabled();
        publishBinderService("persistent_data_block", this.mService);
        this.mInitDoneSignal.countDown();
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 500) {
            try {
                if (this.mInitDoneSignal.await(10, TimeUnit.SECONDS)) {
                    LocalServices.addService(PersistentDataBlockManagerInternal.class, this.mInternalService);
                } else {
                    throw new IllegalStateException("Service " + TAG + " init timeout");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Service " + TAG + " init interrupted", e);
            }
        }
        super.onBootPhase(phase);
    }

    private void formatIfOemUnlockEnabled() {
        boolean enabled = doGetOemUnlockEnabled();
        if (enabled) {
            synchronized (this.mLock) {
                formatPartitionLocked(true);
            }
        }
        SystemProperties.set(OEM_UNLOCK_PROP, enabled ? FLASH_LOCK_LOCKED : FLASH_LOCK_UNLOCKED);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enforceOemUnlockReadPermission() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.READ_OEM_UNLOCK_STATE") == -1 && this.mContext.checkCallingOrSelfPermission("android.permission.OEM_UNLOCK_STATE") == -1) {
            throw new SecurityException("Can't access OEM unlock state. Requires READ_OEM_UNLOCK_STATE or OEM_UNLOCK_STATE permission.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enforceOemUnlockWritePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.OEM_UNLOCK_STATE", "Can't modify OEM unlock state");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enforceUid(int callingUid) {
        if (callingUid != this.mAllowedUid) {
            throw new SecurityException("uid " + callingUid + " not allowed to access PST");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enforceIsAdmin() {
        if (!UserManager.get(this.mContext).isUserAdmin(UserHandle.getCallingUserId())) {
            throw new SecurityException("Only the Admin user is allowed to change OEM unlock state");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enforceUserRestriction(String userRestriction) {
        if (UserManager.get(this.mContext).hasUserRestriction(userRestriction)) {
            throw new SecurityException("OEM unlock is disallowed by user restriction: " + userRestriction);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getTotalDataSizeLocked(DataInputStream inputStream) throws IOException {
        inputStream.skipBytes(32);
        if (inputStream.readInt() == PARTITION_TYPE_MARKER) {
            return inputStream.readInt();
        }
        return 0;
    }

    private long getBlockDeviceSize() {
        synchronized (this.mLock) {
            if (this.mBlockDeviceSize == -1) {
                this.mBlockDeviceSize = nativeGetBlockDeviceSize(this.mDataBlockFile);
            }
        }
        return this.mBlockDeviceSize;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getFrpCredentialDataOffset() {
        return (getBlockDeviceSize() - 1) - 1000;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getTestHarnessModeDataOffset() {
        return getFrpCredentialDataOffset() - JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean enforceChecksumValidity() {
        byte[] storedDigest = new byte[32];
        synchronized (this.mLock) {
            byte[] digest = computeDigestLocked(storedDigest);
            if (digest != null) {
                if (Arrays.equals(storedDigest, digest)) {
                    return true;
                }
            }
            Slog.i(TAG, "Formatting FRP partition...");
            formatPartitionLocked(false);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean computeAndWriteDigestLocked() {
        byte[] digest = computeDigestLocked(null);
        if (digest == null) {
            return false;
        }
        try {
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(new File(this.mDataBlockFile)));
            try {
                outputStream.write(digest, 0, 32);
                outputStream.flush();
                IoUtils.closeQuietly(outputStream);
                return true;
            } catch (IOException e) {
                Slog.e(TAG, "failed to write block checksum", e);
                IoUtils.closeQuietly(outputStream);
                return false;
            } catch (Throwable th) {
                IoUtils.closeQuietly(outputStream);
                throw th;
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "partition not available?", e2);
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0038 A[Catch:{ IOException -> 0x0047, all -> 0x0045 }, LOOP:0: B:14:0x0030->B:16:0x0038, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x003c A[SYNTHETIC] */
    private byte[] computeDigestLocked(byte[] storedDigest) {
        byte[] data;
        int read;
        try {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(this.mDataBlockFile)));
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                if (storedDigest != null) {
                    try {
                        if (storedDigest.length == 32) {
                            inputStream.read(storedDigest);
                            data = new byte[1024];
                            md.update(data, 0, 32);
                            while (true) {
                                read = inputStream.read(data);
                                if (read == -1) {
                                    md.update(data, 0, read);
                                } else {
                                    IoUtils.closeQuietly(inputStream);
                                    return md.digest();
                                }
                            }
                        }
                    } catch (IOException e) {
                        Slog.e(TAG, "failed to read partition", e);
                        IoUtils.closeQuietly(inputStream);
                        return null;
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(inputStream);
                        throw th;
                    }
                }
                inputStream.skipBytes(32);
                data = new byte[1024];
                md.update(data, 0, 32);
                while (true) {
                    read = inputStream.read(data);
                    if (read == -1) {
                    }
                    md.update(data, 0, read);
                }
            } catch (NoSuchAlgorithmException e2) {
                Slog.e(TAG, "SHA-256 not supported?", e2);
                IoUtils.closeQuietly(inputStream);
                return null;
            }
        } catch (FileNotFoundException e3) {
            Slog.e(TAG, "partition not available?", e3);
            return null;
        }
    }

    private void formatPartitionLocked(boolean setOemUnlockEnabled) {
        try {
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(new File(this.mDataBlockFile)));
            try {
                outputStream.write(new byte[32], 0, 32);
                outputStream.writeInt(PARTITION_TYPE_MARKER);
                outputStream.writeInt(0);
                outputStream.flush();
                IoUtils.closeQuietly(outputStream);
                doSetOemUnlockEnabledLocked(setOemUnlockEnabled);
                computeAndWriteDigestLocked();
            } catch (IOException e) {
                Slog.e(TAG, "failed to format block", e);
                IoUtils.closeQuietly(outputStream);
            } catch (Throwable th) {
                IoUtils.closeQuietly(outputStream);
                throw th;
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "partition not available?", e2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doSetOemUnlockEnabledLocked(boolean enabled) {
        String str = FLASH_LOCK_LOCKED;
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(this.mDataBlockFile));
            try {
                FileChannel channel = outputStream.getChannel();
                channel.position(getBlockDeviceSize() - 1);
                byte b = 1;
                ByteBuffer data = ByteBuffer.allocate(1);
                if (!enabled) {
                    b = 0;
                }
                data.put(b);
                data.flip();
                channel.write(data);
                outputStream.flush();
                if (!enabled) {
                    str = FLASH_LOCK_UNLOCKED;
                }
                SystemProperties.set(OEM_UNLOCK_PROP, str);
                IoUtils.closeQuietly(outputStream);
            } catch (IOException e) {
                Slog.e(TAG, "unable to access persistent partition", e);
                if (!enabled) {
                    str = FLASH_LOCK_UNLOCKED;
                }
                SystemProperties.set(OEM_UNLOCK_PROP, str);
                IoUtils.closeQuietly(outputStream);
            } catch (Throwable th) {
                if (!enabled) {
                    str = FLASH_LOCK_UNLOCKED;
                }
                SystemProperties.set(OEM_UNLOCK_PROP, str);
                IoUtils.closeQuietly(outputStream);
                throw th;
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "partition not available", e2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean doGetOemUnlockEnabled() {
        boolean z;
        try {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(this.mDataBlockFile)));
            try {
                synchronized (this.mLock) {
                    inputStream.skip(getBlockDeviceSize() - 1);
                    z = inputStream.readByte() != 0;
                }
                return z;
            } catch (IOException e) {
                Slog.e(TAG, "unable to access persistent partition", e);
                return false;
            } finally {
                IoUtils.closeQuietly(inputStream);
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "partition not available");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long doGetMaximumDataBlockSize() {
        long actualSize = ((((getBlockDeviceSize() - 8) - 32) - JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY) - 1000) - 1;
        if (actualSize <= 102400) {
            return actualSize;
        }
        return 102400;
    }
}
