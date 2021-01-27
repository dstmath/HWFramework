package com.android.server.security;

import android.os.SharedMemory;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Pair;
import android.util.Slog;
import android.util.apk.ApkSignatureVerifier;
import android.util.apk.ByteBufferFactory;
import android.util.apk.SignatureNotFoundException;
import android.util.apk.VerityBuilder;
import com.android.server.job.controllers.JobStatus;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import libcore.util.HexEncoding;
import sun.security.pkcs.PKCS7;

public abstract class VerityUtils {
    private static final int COMMON_LINUX_PAGE_SIZE_IN_BYTES = 4096;
    private static final boolean DEBUG = false;
    public static final String FSVERITY_SIGNATURE_FILE_EXTENSION = ".fsv_sig";
    private static final int MAX_SIGNATURE_FILE_SIZE_BYTES = 8192;
    private static final String TAG = "VerityUtils";

    private static native byte[] constructFsverityDescriptorNative(long j);

    private static native byte[] constructFsverityExtensionNative(short s, int i);

    private static native byte[] constructFsverityFooterNative(int i);

    private static native byte[] constructFsveritySignedDataNative(byte[] bArr);

    private static native int enableFsverityNative(String str);

    private static native int measureFsverityNative(String str);

    public static boolean isFsveritySignatureFile(File file) {
        return file.getName().endsWith(FSVERITY_SIGNATURE_FILE_EXTENSION);
    }

    public static String getFsveritySignatureFilePath(String filePath) {
        return filePath + FSVERITY_SIGNATURE_FILE_EXTENSION;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00b1, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00b2, code lost:
        $closeResource(r3, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00b5, code lost:
        throw r6;
     */
    public static void setUpFsverity(String filePath, String signaturePath) throws IOException, DigestException, NoSuchAlgorithmException {
        byte[] expectedMeasurement = new PKCS7(Files.readAllBytes(Paths.get(signaturePath, new String[0]))).getContentInfo().getContentBytes();
        TrackedBufferFactory bufferFactory = new TrackedBufferFactory();
        byte[] actualMeasurement = generateFsverityMetadata(filePath, signaturePath, bufferFactory);
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
        FileChannel ch = raf.getChannel();
        ch.position(roundUpToNextMultiple(ch.size(), 4096));
        ByteBuffer buffer = bufferFactory.getBuffer();
        long offset = (long) buffer.position();
        long size = (long) buffer.limit();
        while (offset < size) {
            long s = (long) ch.write(buffer);
            offset += s;
            size -= s;
        }
        $closeResource(null, raf);
        if (Arrays.equals(expectedMeasurement, actualMeasurement)) {
            int errno = enableFsverityNative(filePath);
            if (errno != 0) {
                throw new IOException("Failed to enable fs-verity on " + filePath + ": " + Os.strerror(errno));
            }
            return;
        }
        throw new SecurityException("fs-verity measurement mismatch: " + bytesToString(actualMeasurement) + " != " + bytesToString(expectedMeasurement));
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public static boolean hasFsverity(String filePath) {
        int errno = measureFsverityNative(filePath);
        if (errno == 0) {
            return true;
        }
        if (errno == OsConstants.ENODATA) {
            return false;
        }
        Slog.e(TAG, "Failed to measure fs-verity, errno " + errno + ": " + filePath);
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0048, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        android.util.Slog.e(com.android.server.security.VerityUtils.TAG, "Failed to set up apk verity: ", r1);
        r2 = com.android.server.security.VerityUtils.SetupResult.failed();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0054, code lost:
        if (0 != 0) goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0056, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0059, code lost:
        return r2;
     */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0048 A[ExcHandler: ErrnoException | SignatureNotFoundException | IOException | SecurityException | DigestException | NoSuchAlgorithmException (r1v1 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:6:0x0012] */
    public static SetupResult generateApkVeritySetupData(String apkPath) {
        SharedMemory shm = null;
        byte[] signedVerityHash = ApkSignatureVerifier.getVerityRootHash(apkPath);
        if (signedVerityHash == null) {
            SetupResult skipped = SetupResult.skipped();
            if (0 != 0) {
                shm.close();
            }
            return skipped;
        }
        try {
            Pair<SharedMemory, Integer> result = generateFsVerityIntoSharedMemory(apkPath, signedVerityHash);
            SharedMemory shm2 = (SharedMemory) result.first;
            int contentSize = ((Integer) result.second).intValue();
            FileDescriptor rfd = shm2.getFileDescriptor();
            if (rfd != null) {
                if (rfd.valid()) {
                    SetupResult ok = SetupResult.ok(Os.dup(rfd), contentSize);
                    shm2.close();
                    return ok;
                }
            }
            SetupResult failed = SetupResult.failed();
            shm2.close();
            return failed;
        } catch (ErrnoException | SignatureNotFoundException | IOException | SecurityException | DigestException | NoSuchAlgorithmException e) {
        } catch (Throwable th) {
            if (0 != 0) {
                shm.close();
            }
            throw th;
        }
    }

    public static byte[] generateApkVerityRootHash(String apkPath) throws NoSuchAlgorithmException, DigestException, IOException {
        return ApkSignatureVerifier.generateApkVerityRootHash(apkPath);
    }

    public static byte[] getVerityRootHash(String apkPath) throws IOException, SignatureNotFoundException {
        return ApkSignatureVerifier.getVerityRootHash(apkPath);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002b, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0027, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0028, code lost:
        $closeResource(r1, r0);
     */
    private static byte[] generateFsverityMetadata(String filePath, String signaturePath, ByteBufferFactory trackedBufferFactory) throws IOException, DigestException, NoSuchAlgorithmException {
        RandomAccessFile file = new RandomAccessFile(filePath, "r");
        VerityBuilder.VerityResult result = VerityBuilder.generateFsVerityTree(file, trackedBufferFactory);
        ByteBuffer buffer = result.verityData;
        buffer.position(result.merkleTreeSize);
        byte[] measurement = generateFsverityDescriptorAndMeasurement(file, result.rootHash, signaturePath, buffer);
        buffer.flip();
        byte[] constructFsveritySignedDataNative = constructFsveritySignedDataNative(measurement);
        $closeResource(null, file);
        return constructFsveritySignedDataNative;
    }

    private static byte[] generateFsverityDescriptorAndMeasurement(RandomAccessFile file, byte[] rootHash, String pkcs7SignaturePath, ByteBuffer output) throws IOException, NoSuchAlgorithmException, DigestException {
        int origPosition = output.position();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] desc = constructFsverityDescriptorNative(file.length());
        output.put(desc);
        md.update(desc);
        byte[] authExt = constructFsverityExtensionNative(1, rootHash.length);
        output.put(authExt);
        output.put(rootHash);
        md.update(authExt);
        md.update(rootHash);
        ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        output.putShort(1);
        output.position(output.position() + 6);
        Path path = Paths.get(pkcs7SignaturePath, new String[0]);
        if (Files.size(path) <= 8192) {
            byte[] pkcs7Signature = Files.readAllBytes(path);
            output.put(constructFsverityExtensionNative(3, pkcs7Signature.length));
            output.put(pkcs7Signature);
            output.put(constructFsverityFooterNative(output.position() - origPosition));
            return md.digest();
        }
        throw new IllegalArgumentException("Signature size is unexpectedly large: " + pkcs7SignaturePath);
    }

    private static Pair<SharedMemory, Integer> generateFsVerityIntoSharedMemory(String apkPath, byte[] expectedRootHash) throws IOException, DigestException, NoSuchAlgorithmException, SignatureNotFoundException {
        TrackedShmBufferFactory shmBufferFactory = new TrackedShmBufferFactory();
        byte[] generatedRootHash = ApkSignatureVerifier.generateApkVerity(apkPath, shmBufferFactory);
        if (Arrays.equals(expectedRootHash, generatedRootHash)) {
            int contentSize = shmBufferFactory.getBufferLimit();
            SharedMemory shm = shmBufferFactory.releaseSharedMemory();
            if (shm == null) {
                throw new IllegalStateException("Failed to generate verity tree into shared memory");
            } else if (shm.setProtect(OsConstants.PROT_READ)) {
                return Pair.create(shm, Integer.valueOf(contentSize));
            } else {
                throw new SecurityException("Failed to set up shared memory correctly");
            }
        } else {
            throw new SecurityException("verity hash mismatch: " + bytesToString(generatedRootHash) + " != " + bytesToString(expectedRootHash));
        }
    }

    private static String bytesToString(byte[] bytes) {
        return HexEncoding.encodeToString(bytes);
    }

    public static class SetupResult {
        private static final int RESULT_FAILED = 3;
        private static final int RESULT_OK = 1;
        private static final int RESULT_SKIPPED = 2;
        private final int mCode;
        private final int mContentSize;
        private final FileDescriptor mFileDescriptor;

        public static SetupResult ok(FileDescriptor fileDescriptor, int contentSize) {
            return new SetupResult(1, fileDescriptor, contentSize);
        }

        public static SetupResult skipped() {
            return new SetupResult(2, null, -1);
        }

        public static SetupResult failed() {
            return new SetupResult(3, null, -1);
        }

        private SetupResult(int code, FileDescriptor fileDescriptor, int contentSize) {
            this.mCode = code;
            this.mFileDescriptor = fileDescriptor;
            this.mContentSize = contentSize;
        }

        public boolean isFailed() {
            return this.mCode == 3;
        }

        public boolean isOk() {
            return this.mCode == 1;
        }

        public FileDescriptor getUnownedFileDescriptor() {
            return this.mFileDescriptor;
        }

        public int getContentSize() {
            return this.mContentSize;
        }
    }

    /* access modifiers changed from: private */
    public static class TrackedShmBufferFactory implements ByteBufferFactory {
        private ByteBuffer mBuffer;
        private SharedMemory mShm;

        private TrackedShmBufferFactory() {
        }

        public ByteBuffer create(int capacity) {
            try {
                if (this.mBuffer == null) {
                    this.mShm = SharedMemory.create("apkverity", capacity);
                    if (this.mShm.setProtect(OsConstants.PROT_READ | OsConstants.PROT_WRITE)) {
                        this.mBuffer = this.mShm.mapReadWrite();
                        return this.mBuffer;
                    }
                    throw new SecurityException("Failed to set protection");
                }
                throw new IllegalStateException("Multiple instantiation from this factory");
            } catch (ErrnoException e) {
                throw new SecurityException("Failed to set protection", e);
            }
        }

        public SharedMemory releaseSharedMemory() {
            ByteBuffer byteBuffer = this.mBuffer;
            if (byteBuffer != null) {
                SharedMemory.unmap(byteBuffer);
                this.mBuffer = null;
            }
            SharedMemory tmp = this.mShm;
            this.mShm = null;
            return tmp;
        }

        public int getBufferLimit() {
            ByteBuffer byteBuffer = this.mBuffer;
            if (byteBuffer == null) {
                return -1;
            }
            return byteBuffer.limit();
        }
    }

    /* access modifiers changed from: private */
    public static class TrackedBufferFactory implements ByteBufferFactory {
        private ByteBuffer mBuffer;

        private TrackedBufferFactory() {
        }

        public ByteBuffer create(int capacity) {
            if (this.mBuffer == null) {
                this.mBuffer = ByteBuffer.allocate(capacity);
                return this.mBuffer;
            }
            throw new IllegalStateException("Multiple instantiation from this factory");
        }

        public ByteBuffer getBuffer() {
            return this.mBuffer;
        }
    }

    private static long roundUpToNextMultiple(long number, long divisor) {
        if (number <= JobStatus.NO_LATEST_RUNTIME - divisor) {
            return (((divisor - 1) + number) / divisor) * divisor;
        }
        throw new IllegalArgumentException("arithmetic overflow");
    }
}
