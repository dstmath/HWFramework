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
    private static final String OEM_UNLOCK_PROP = "sys.oem_unlock_allowed";
    private static final int PARTITION_TYPE_MARKER = 428873843;
    private static final String PERSISTENT_DATA_BLOCK_PROP = "ro.frp.pst";
    /* access modifiers changed from: private */
    public static final String TAG = PersistentDataBlockService.class.getSimpleName();
    private int mAllowedUid = -1;
    private long mBlockDeviceSize;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final String mDataBlockFile;
    private final CountDownLatch mInitDoneSignal = new CountDownLatch(1);
    /* access modifiers changed from: private */
    public PersistentDataBlockManagerInternal mInternalService = new PersistentDataBlockManagerInternal() {
        public void setFrpCredentialHandle(byte[] handle) {
            boolean z = true;
            int i = 0;
            Preconditions.checkArgument(handle == null || handle.length > 0, "handle must be null or non-empty");
            if (handle != null && handle.length > PersistentDataBlockService.MAX_FRP_CREDENTIAL_HANDLE_SIZE) {
                z = false;
            }
            Preconditions.checkArgument(z, "handle must not be longer than 996");
            try {
                FileOutputStream outputStream = new FileOutputStream(new File(PersistentDataBlockService.this.mDataBlockFile));
                ByteBuffer data = ByteBuffer.allocate(1000);
                if (handle != null) {
                    i = handle.length;
                }
                data.putInt(i);
                if (handle != null) {
                    data.put(handle);
                }
                data.flip();
                synchronized (PersistentDataBlockService.this.mLock) {
                    if (!PersistentDataBlockService.this.mIsWritable) {
                        IoUtils.closeQuietly(outputStream);
                        return;
                    }
                    try {
                        FileChannel channel = outputStream.getChannel();
                        channel.position((PersistentDataBlockService.this.getBlockDeviceSize() - 1) - 1000);
                        channel.write(data);
                        outputStream.flush();
                        IoUtils.closeQuietly(outputStream);
                        boolean unused = PersistentDataBlockService.this.computeAndWriteDigestLocked();
                    } catch (IOException e) {
                        try {
                            Slog.e(PersistentDataBlockService.TAG, "unable to access persistent partition", e);
                        } finally {
                            IoUtils.closeQuietly(outputStream);
                        }
                    }
                }
            } catch (FileNotFoundException e2) {
                Slog.e(PersistentDataBlockService.TAG, "partition not available", e2);
            }
        }

        public byte[] getFrpCredentialHandle() {
            if (PersistentDataBlockService.this.enforceChecksumValidity()) {
                try {
                    DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(PersistentDataBlockService.this.mDataBlockFile)));
                    try {
                        synchronized (PersistentDataBlockService.this.mLock) {
                            inputStream.skip((PersistentDataBlockService.this.getBlockDeviceSize() - 1) - 1000);
                            int length = inputStream.readInt();
                            if (length > 0) {
                                if (length <= PersistentDataBlockService.MAX_FRP_CREDENTIAL_HANDLE_SIZE) {
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
                        try {
                            throw new IllegalStateException("frp handle not readable", e);
                        } catch (Throwable th) {
                            IoUtils.closeQuietly(inputStream);
                            throw th;
                        }
                    }
                } catch (FileNotFoundException e2) {
                    throw new IllegalStateException("frp partition not available");
                }
            } else {
                throw new IllegalStateException("invalid checksum");
            }
        }

        public void forceOemUnlockEnabled(boolean enabled) {
            synchronized (PersistentDataBlockService.this.mLock) {
                PersistentDataBlockService.this.doSetOemUnlockEnabledLocked(enabled);
                boolean unused = PersistentDataBlockService.this.computeAndWriteDigestLocked();
            }
        }
    };
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public boolean mIsWritable = true;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final IBinder mService = new IPersistentDataBlockService.Stub() {
        public int write(byte[] data) throws RemoteException {
            String access$000 = PersistentDataBlockService.TAG;
            Slog.i(access$000, "write data, callingUid=" + Binder.getCallingUid() + ", callingPid=" + Binder.getCallingPid());
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
                        int length = data.length;
                        return length;
                    } catch (IOException e) {
                        try {
                            Slog.e(PersistentDataBlockService.TAG, "failed writing to the persistent data block", e);
                            return -1;
                        } finally {
                            IoUtils.closeQuietly(outputStream);
                        }
                    }
                }
            } catch (FileNotFoundException e2) {
                Slog.e(PersistentDataBlockService.TAG, "partition not available?", e2);
                return -1;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
            r2.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0044, code lost:
            android.util.Slog.e(com.android.server.PersistentDataBlockService.access$000(), "failed to close OutputStream");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
            r2.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x007c, code lost:
            android.util.Slog.e(com.android.server.PersistentDataBlockService.access$000(), "failed to close OutputStream");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
            r2.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x008c, code lost:
            android.util.Slog.e(com.android.server.PersistentDataBlockService.access$000(), "failed to close OutputStream");
         */
        public byte[] read() {
            String str;
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
                                String access$000 = PersistentDataBlockService.TAG;
                                Slog.e(access$000, "failed to read entire data block. bytes read: " + read + SliceClientPermissions.SliceAuthority.DELIMITER + totalDataSize);
                            }
                        }
                    }
                } catch (IOException e) {
                    try {
                        Slog.e(PersistentDataBlockService.TAG, "failed to read data", e);
                        return null;
                    } finally {
                        try {
                            inputStream.close();
                        } catch (IOException e2) {
                            str = "failed to close OutputStream";
                            Slog.e(PersistentDataBlockService.TAG, str);
                        }
                    }
                }
            } catch (FileNotFoundException e3) {
                Slog.e(PersistentDataBlockService.TAG, "partition not available?", e3);
                return null;
            }
            return null;
            return data;
            return bArr;
        }

        public void wipe() {
            PersistentDataBlockService.this.enforceOemUnlockWritePermission();
            synchronized (PersistentDataBlockService.this.mLock) {
                if (PersistentDataBlockService.this.nativeWipe(PersistentDataBlockService.this.mDataBlockFile) < 0) {
                    Slog.e(PersistentDataBlockService.TAG, "failed to wipe persistent partition");
                } else {
                    boolean unused = PersistentDataBlockService.this.mIsWritable = false;
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
                    boolean unused = PersistentDataBlockService.this.computeAndWriteDigestLocked();
                }
            }
        }

        public boolean getOemUnlockEnabled() {
            PersistentDataBlockService.this.enforceOemUnlockReadPermission();
            return PersistentDataBlockService.this.doGetOemUnlockEnabled();
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        public int getFlashLockState() {
            boolean z;
            PersistentDataBlockService.this.enforceOemUnlockReadPermission();
            String locked = SystemProperties.get(PersistentDataBlockService.FLASH_LOCK_PROP);
            switch (locked.hashCode()) {
                case 48:
                    if (locked.equals(PersistentDataBlockService.FLASH_LOCK_UNLOCKED)) {
                        z = true;
                        break;
                    }
                case 49:
                    if (locked.equals(PersistentDataBlockService.FLASH_LOCK_LOCKED)) {
                        z = false;
                        break;
                    }
                default:
                    z = true;
                    break;
            }
            switch (z) {
                case false:
                    return 1;
                case true:
                    return 0;
                default:
                    return -1;
            }
        }

        public int getDataBlockSize() {
            int access$800;
            enforcePersistentDataBlockAccess();
            try {
                DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(PersistentDataBlockService.this.mDataBlockFile)));
                try {
                    synchronized (PersistentDataBlockService.this.mLock) {
                        access$800 = PersistentDataBlockService.this.getTotalDataSizeLocked(inputStream);
                    }
                    IoUtils.closeQuietly(inputStream);
                    return access$800;
                } catch (IOException e) {
                    try {
                        Slog.e(PersistentDataBlockService.TAG, "error reading data block size");
                        return 0;
                    } finally {
                        IoUtils.closeQuietly(inputStream);
                    }
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
    public native int nativeWipe(String str);

    /* JADX WARNING: type inference failed for: r0v3, types: [com.android.server.PersistentDataBlockService$1, android.os.IBinder] */
    public PersistentDataBlockService(Context context) {
        super(context);
        this.mContext = context;
        this.mDataBlockFile = SystemProperties.get(PERSISTENT_DATA_BLOCK_PROP);
        this.mBlockDeviceSize = -1;
    }

    private int getAllowedUid(int userHandle) {
        String allowedPackage = this.mContext.getResources().getString(17039838);
        try {
            return this.mContext.getPackageManager().getPackageUidAsUser(allowedPackage, DumpState.DUMP_DEXOPT, userHandle);
        } catch (PackageManager.NameNotFoundException e) {
            String str = TAG;
            Slog.e(str, "not able to find package " + allowedPackage, e);
            return -1;
        }
    }

    public void onStart() {
        SystemServerInitThreadPool systemServerInitThreadPool = SystemServerInitThreadPool.get();
        $$Lambda$PersistentDataBlockService$EZl9OYaT2eNL7kfSr2nKUBjxidk r1 = new Runnable() {
            public final void run() {
                PersistentDataBlockService.lambda$onStart$0(PersistentDataBlockService.this);
            }
        };
        systemServerInitThreadPool.submit(r1, TAG + ".onStart");
    }

    public static /* synthetic */ void lambda$onStart$0(PersistentDataBlockService persistentDataBlockService) {
        persistentDataBlockService.mAllowedUid = persistentDataBlockService.getAllowedUid(0);
        persistentDataBlockService.enforceChecksumValidity();
        persistentDataBlockService.formatIfOemUnlockEnabled();
        persistentDataBlockService.publishBinderService("persistent_data_block", persistentDataBlockService.mService);
        persistentDataBlockService.mInitDoneSignal.countDown();
    }

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
    public void enforceOemUnlockReadPermission() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.READ_OEM_UNLOCK_STATE") == -1 && this.mContext.checkCallingOrSelfPermission("android.permission.OEM_UNLOCK_STATE") == -1) {
            throw new SecurityException("Can't access OEM unlock state. Requires READ_OEM_UNLOCK_STATE or OEM_UNLOCK_STATE permission.");
        }
    }

    /* access modifiers changed from: private */
    public void enforceOemUnlockWritePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.OEM_UNLOCK_STATE", "Can't modify OEM unlock state");
    }

    /* access modifiers changed from: private */
    public void enforceUid(int callingUid) {
        if (callingUid != this.mAllowedUid) {
            throw new SecurityException("uid " + callingUid + " not allowed to access PST");
        }
    }

    /* access modifiers changed from: private */
    public void enforceIsAdmin() {
        if (!UserManager.get(this.mContext).isUserAdmin(UserHandle.getCallingUserId())) {
            throw new SecurityException("Only the Admin user is allowed to change OEM unlock state");
        }
    }

    /* access modifiers changed from: private */
    public void enforceUserRestriction(String userRestriction) {
        if (UserManager.get(this.mContext).hasUserRestriction(userRestriction)) {
            throw new SecurityException("OEM unlock is disallowed by user restriction: " + userRestriction);
        }
    }

    /* access modifiers changed from: private */
    public int getTotalDataSizeLocked(DataInputStream inputStream) throws IOException {
        inputStream.skipBytes(32);
        if (inputStream.readInt() == PARTITION_TYPE_MARKER) {
            return inputStream.readInt();
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public long getBlockDeviceSize() {
        synchronized (this.mLock) {
            if (this.mBlockDeviceSize == -1) {
                this.mBlockDeviceSize = nativeGetBlockDeviceSize(this.mDataBlockFile);
            }
        }
        return this.mBlockDeviceSize;
    }

    /* access modifiers changed from: private */
    public boolean enforceChecksumValidity() {
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
    public boolean computeAndWriteDigestLocked() {
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

    /* JADX WARNING: Removed duplicated region for block: B:20:0x003e A[Catch:{ IOException -> 0x0029, all -> 0x0027 }, LOOP:0: B:18:0x0036->B:20:0x003e, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0042 A[SYNTHETIC] */
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
                                int read2 = read;
                                if (read == -1) {
                                    md.update(data, 0, read2);
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
                    int read22 = read;
                    if (read == -1) {
                    }
                    md.update(data, 0, read22);
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
    public void doSetOemUnlockEnabledLocked(boolean enabled) {
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(this.mDataBlockFile));
            try {
                FileChannel channel = outputStream.getChannel();
                channel.position(getBlockDeviceSize() - 1);
                ByteBuffer data = ByteBuffer.allocate(1);
                data.put(enabled);
                data.flip();
                channel.write(data);
                outputStream.flush();
                SystemProperties.set(OEM_UNLOCK_PROP, enabled ? FLASH_LOCK_LOCKED : FLASH_LOCK_UNLOCKED);
                IoUtils.closeQuietly(outputStream);
            } catch (IOException e) {
                Slog.e(TAG, "unable to access persistent partition", e);
                SystemProperties.set(OEM_UNLOCK_PROP, enabled ? FLASH_LOCK_LOCKED : FLASH_LOCK_UNLOCKED);
                IoUtils.closeQuietly(outputStream);
            } catch (Throwable th) {
                SystemProperties.set(OEM_UNLOCK_PROP, enabled ? FLASH_LOCK_LOCKED : FLASH_LOCK_UNLOCKED);
                IoUtils.closeQuietly(outputStream);
                throw th;
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "partition not available", e2);
        }
    }

    /* access modifiers changed from: private */
    public boolean doGetOemUnlockEnabled() {
        boolean z;
        try {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(this.mDataBlockFile)));
            try {
                synchronized (this.mLock) {
                    inputStream.skip(getBlockDeviceSize() - 1);
                    z = inputStream.readByte() != 0;
                }
                IoUtils.closeQuietly(inputStream);
                return z;
            } catch (IOException e) {
                try {
                    Slog.e(TAG, "unable to access persistent partition", e);
                    return false;
                } finally {
                    IoUtils.closeQuietly(inputStream);
                }
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "partition not available");
            return false;
        }
    }

    /* access modifiers changed from: private */
    public long doGetMaximumDataBlockSize() {
        long actualSize = (((getBlockDeviceSize() - 8) - 32) - 1000) - 1;
        if (actualSize <= 102400) {
            return actualSize;
        }
        return 102400;
    }
}
