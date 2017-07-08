package com.android.server;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.persistentdata.IPersistentDataBlockService.Stub;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
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
import libcore.io.IoUtils;

public class PersistentDataBlockService extends SystemService {
    public static final int DIGEST_SIZE_BYTES = 32;
    private static final String FLASH_LOCK_LOCKED = "1";
    private static final String FLASH_LOCK_PROP = "ro.boot.flash.locked";
    private static final String FLASH_LOCK_UNLOCKED = "0";
    private static final int HEADER_SIZE = 8;
    private static final int MAX_DATA_BLOCK_SIZE = 102400;
    private static final String OEM_UNLOCK_PROP = "sys.oem_unlock_allowed";
    private static final int PARTITION_TYPE_MARKER = 428873843;
    private static final String PERSISTENT_DATA_BLOCK_PROP = "ro.frp.pst";
    private static final String TAG = null;
    private int mAllowedUid;
    private long mBlockDeviceSize;
    private final Context mContext;
    private final String mDataBlockFile;
    @GuardedBy("mLock")
    private boolean mIsWritable;
    private final Object mLock;
    private final IBinder mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.PersistentDataBlockService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.PersistentDataBlockService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.PersistentDataBlockService.<clinit>():void");
    }

    private native long nativeGetBlockDeviceSize(String str);

    private native int nativeWipe(String str);

    public PersistentDataBlockService(Context context) {
        super(context);
        this.mLock = new Object();
        this.mAllowedUid = -1;
        this.mIsWritable = true;
        this.mService = new Stub() {
            public int write(byte[] data) throws RemoteException {
                Slog.i(PersistentDataBlockService.TAG, "write data, callingUid=" + Binder.getCallingUid() + ", callingPid=" + Binder.getCallingPid());
                PersistentDataBlockService.this.enforceUid(Binder.getCallingUid());
                long maxBlockSize = (PersistentDataBlockService.this.getBlockDeviceSize() - 8) - 1;
                if (((long) data.length) > maxBlockSize) {
                    return (int) (-maxBlockSize);
                }
                try {
                    DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(new File(PersistentDataBlockService.this.mDataBlockFile)));
                    ByteBuffer headerAndData = ByteBuffer.allocate(data.length + PersistentDataBlockService.HEADER_SIZE);
                    headerAndData.putInt(PersistentDataBlockService.PARTITION_TYPE_MARKER);
                    headerAndData.putInt(data.length);
                    headerAndData.put(data);
                    synchronized (PersistentDataBlockService.this.mLock) {
                        if (PersistentDataBlockService.this.mIsWritable) {
                            try {
                                outputStream.write(new byte[PersistentDataBlockService.DIGEST_SIZE_BYTES], 0, PersistentDataBlockService.DIGEST_SIZE_BYTES);
                                outputStream.write(headerAndData.array());
                                outputStream.flush();
                                IoUtils.closeQuietly(outputStream);
                                if (PersistentDataBlockService.this.computeAndWriteDigestLocked()) {
                                    int length = data.length;
                                    return length;
                                }
                                return -1;
                            } catch (IOException e) {
                                Slog.e(PersistentDataBlockService.TAG, "failed writing to the persistent data block", e);
                                IoUtils.closeQuietly(outputStream);
                                return -1;
                            } catch (Throwable th) {
                                IoUtils.closeQuietly(outputStream);
                            }
                        } else {
                            IoUtils.closeQuietly(outputStream);
                            return -1;
                        }
                    }
                } catch (FileNotFoundException e2) {
                    Slog.e(PersistentDataBlockService.TAG, "partition not available?", e2);
                    return -1;
                }
            }

            public byte[] read() {
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
                                byte[] bArr = new byte[0];
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    Slog.e(PersistentDataBlockService.TAG, "failed to close OutputStream");
                                }
                                return bArr;
                            }
                            byte[] data = new byte[totalDataSize];
                            int read = inputStream.read(data, 0, totalDataSize);
                            if (read < totalDataSize) {
                                Slog.e(PersistentDataBlockService.TAG, "failed to read entire data block. bytes read: " + read + "/" + totalDataSize);
                                try {
                                    inputStream.close();
                                } catch (IOException e2) {
                                    Slog.e(PersistentDataBlockService.TAG, "failed to close OutputStream");
                                }
                                return null;
                            }
                            try {
                                inputStream.close();
                            } catch (IOException e3) {
                                Slog.e(PersistentDataBlockService.TAG, "failed to close OutputStream");
                            }
                            return data;
                        }
                    } catch (IOException e4) {
                        try {
                            Slog.e(PersistentDataBlockService.TAG, "failed to read data", e4);
                            return null;
                        } finally {
                            try {
                                inputStream.close();
                            } catch (IOException e5) {
                                Slog.e(PersistentDataBlockService.TAG, "failed to close OutputStream");
                            }
                        }
                    }
                } catch (FileNotFoundException e6) {
                    Slog.e(PersistentDataBlockService.TAG, "partition not available?", e6);
                    return null;
                }
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

            public void setOemUnlockEnabled(boolean enabled) {
                if (!ActivityManager.isUserAMonkey()) {
                    PersistentDataBlockService.this.enforceOemUnlockWritePermission();
                    PersistentDataBlockService.this.enforceIsAdmin();
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

            public int getFlashLockState() {
                PersistentDataBlockService.this.enforceOemUnlockReadPermission();
                String locked = SystemProperties.get(PersistentDataBlockService.FLASH_LOCK_PROP);
                if (locked.equals(PersistentDataBlockService.FLASH_LOCK_LOCKED)) {
                    return 1;
                }
                if (locked.equals(PersistentDataBlockService.FLASH_LOCK_UNLOCKED)) {
                    return 0;
                }
                return -1;
            }

            public int getDataBlockSize() {
                enforcePersistentDataBlockAccess();
                try {
                    DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(PersistentDataBlockService.this.mDataBlockFile)));
                    try {
                        int -wrap3;
                        synchronized (PersistentDataBlockService.this.mLock) {
                            -wrap3 = PersistentDataBlockService.this.getTotalDataSizeLocked(inputStream);
                        }
                        IoUtils.closeQuietly(inputStream);
                        return -wrap3;
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
                long actualSize = (PersistentDataBlockService.this.getBlockDeviceSize() - 8) - 1;
                return actualSize <= 102400 ? actualSize : 102400;
            }
        };
        this.mContext = context;
        this.mDataBlockFile = SystemProperties.get(PERSISTENT_DATA_BLOCK_PROP);
        this.mBlockDeviceSize = -1;
        this.mAllowedUid = getAllowedUid(0);
    }

    private int getAllowedUid(int userHandle) {
        String allowedPackage = this.mContext.getResources().getString(17039461);
        int allowedUid = -1;
        try {
            allowedUid = this.mContext.getPackageManager().getPackageUidAsUser(allowedPackage, DumpState.DUMP_DEXOPT, userHandle);
        } catch (NameNotFoundException e) {
            Slog.e(TAG, "not able to find package " + allowedPackage, e);
        }
        return allowedUid;
    }

    public void onStart() {
        enforceChecksumValidity();
        formatIfOemUnlockEnabled();
        publishBinderService("persistent_data_block", this.mService);
    }

    private void formatIfOemUnlockEnabled() {
        String str;
        boolean enabled = doGetOemUnlockEnabled();
        if (enabled) {
            synchronized (this.mLock) {
                formatPartitionLocked(true);
            }
        }
        String str2 = OEM_UNLOCK_PROP;
        if (enabled) {
            str = FLASH_LOCK_LOCKED;
        } else {
            str = FLASH_LOCK_UNLOCKED;
        }
        SystemProperties.set(str2, str);
    }

    private void enforceOemUnlockReadPermission() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.READ_OEM_UNLOCK_STATE") == -1 && this.mContext.checkCallingOrSelfPermission("android.permission.OEM_UNLOCK_STATE") == -1) {
            throw new SecurityException("Can't access OEM unlock state. Requires READ_OEM_UNLOCK_STATE or OEM_UNLOCK_STATE permission.");
        }
    }

    private void enforceOemUnlockWritePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.OEM_UNLOCK_STATE", "Can't modify OEM unlock state");
    }

    private void enforceUid(int callingUid) {
        if (callingUid != this.mAllowedUid) {
            throw new SecurityException("uid " + callingUid + " not allowed to access PST");
        }
    }

    private void enforceIsAdmin() {
        if (!UserManager.get(this.mContext).isUserAdmin(UserHandle.getCallingUserId())) {
            throw new SecurityException("Only the Admin user is allowed to change OEM unlock state");
        }
    }

    private int getTotalDataSizeLocked(DataInputStream inputStream) throws IOException {
        inputStream.skipBytes(DIGEST_SIZE_BYTES);
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

    private boolean enforceChecksumValidity() {
        byte[] storedDigest = new byte[DIGEST_SIZE_BYTES];
        synchronized (this.mLock) {
            byte[] digest = computeDigestLocked(storedDigest);
            if (digest == null || !Arrays.equals(storedDigest, digest)) {
                Slog.i(TAG, "Formatting FRP partition...");
                formatPartitionLocked(false);
                return false;
            }
            return true;
        }
    }

    private boolean computeAndWriteDigestLocked() {
        byte[] digest = computeDigestLocked(null);
        if (digest == null) {
            return false;
        }
        try {
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(new File(this.mDataBlockFile)));
            try {
                outputStream.write(digest, 0, DIGEST_SIZE_BYTES);
                outputStream.flush();
                return true;
            } catch (IOException e) {
                Slog.e(TAG, "failed to write block checksum", e);
                return false;
            } finally {
                IoUtils.closeQuietly(outputStream);
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "partition not available?", e2);
            return false;
        }
    }

    private byte[] computeDigestLocked(byte[] storedDigest) {
        try {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(this.mDataBlockFile)));
            try {
                byte[] data;
                int read;
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                if (storedDigest != null) {
                    try {
                        if (storedDigest.length == DIGEST_SIZE_BYTES) {
                            inputStream.read(storedDigest);
                            data = new byte[DumpState.DUMP_PROVIDERS];
                            md.update(data, 0, DIGEST_SIZE_BYTES);
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
                        return null;
                    } finally {
                        IoUtils.closeQuietly(inputStream);
                    }
                }
                inputStream.skipBytes(DIGEST_SIZE_BYTES);
                data = new byte[DumpState.DUMP_PROVIDERS];
                md.update(data, 0, DIGEST_SIZE_BYTES);
                while (true) {
                    read = inputStream.read(data);
                    if (read == -1) {
                        IoUtils.closeQuietly(inputStream);
                        return md.digest();
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
                outputStream.write(new byte[DIGEST_SIZE_BYTES], 0, DIGEST_SIZE_BYTES);
                outputStream.writeInt(PARTITION_TYPE_MARKER);
                outputStream.writeInt(0);
                outputStream.flush();
                doSetOemUnlockEnabledLocked(setOemUnlockEnabled);
                computeAndWriteDigestLocked();
            } catch (IOException e) {
                Slog.e(TAG, "failed to format block", e);
            } finally {
                IoUtils.closeQuietly(outputStream);
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "partition not available?", e2);
        }
    }

    private void doSetOemUnlockEnabledLocked(boolean enabled) {
        byte b = (byte) 1;
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(this.mDataBlockFile));
            try {
                FileChannel channel = outputStream.getChannel();
                channel.position(getBlockDeviceSize() - 1);
                ByteBuffer data = ByteBuffer.allocate(1);
                if (!enabled) {
                    b = (byte) 0;
                }
                data.put(b);
                data.flip();
                channel.write(data);
                outputStream.flush();
            } catch (IOException e) {
                Slog.e(TAG, "unable to access persistent partition", e);
            } finally {
                SystemProperties.set(OEM_UNLOCK_PROP, enabled ? FLASH_LOCK_LOCKED : FLASH_LOCK_UNLOCKED);
                IoUtils.closeQuietly(outputStream);
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "partition not available", e2);
        }
    }

    private boolean doGetOemUnlockEnabled() {
        try {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(this.mDataBlockFile)));
            try {
                boolean z;
                synchronized (this.mLock) {
                    inputStream.skip(getBlockDeviceSize() - 1);
                    if (inputStream.readByte() != null) {
                        z = true;
                    } else {
                        z = false;
                    }
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
}
