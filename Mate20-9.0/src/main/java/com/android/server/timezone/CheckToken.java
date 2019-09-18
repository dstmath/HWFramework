package com.android.server.timezone;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

final class CheckToken {
    final int mOptimisticLockId;
    final PackageVersions mPackageVersions;

    CheckToken(int optimisticLockId, PackageVersions packageVersions) {
        this.mOptimisticLockId = optimisticLockId;
        if (packageVersions != null) {
            this.mPackageVersions = packageVersions;
            return;
        }
        throw new NullPointerException("packageVersions == null");
    }

    /* access modifiers changed from: package-private */
    public byte[] toByteArray() {
        DataOutputStream dos;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(12);
        try {
            dos = new DataOutputStream(baos);
            dos.writeInt(this.mOptimisticLockId);
            dos.writeLong(this.mPackageVersions.mUpdateAppVersion);
            dos.writeLong(this.mPackageVersions.mDataAppVersion);
            $closeResource(null, dos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Unable to write into a ByteArrayOutputStream", e);
        } catch (Throwable th) {
            $closeResource(r2, dos);
            throw th;
        }
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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002c, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0025, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0029, code lost:
        $closeResource(r2, r1);
     */
    static CheckToken fromByteArray(byte[] tokenBytes) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(tokenBytes));
        CheckToken checkToken = new CheckToken(dis.readInt(), new PackageVersions(dis.readLong(), dis.readLong()));
        $closeResource(null, dis);
        return checkToken;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CheckToken checkToken = (CheckToken) o;
        if (this.mOptimisticLockId != checkToken.mOptimisticLockId) {
            return false;
        }
        return this.mPackageVersions.equals(checkToken.mPackageVersions);
    }

    public int hashCode() {
        return (31 * this.mOptimisticLockId) + this.mPackageVersions.hashCode();
    }

    public String toString() {
        return "Token{mOptimisticLockId=" + this.mOptimisticLockId + ", mPackageVersions=" + this.mPackageVersions + '}';
    }
}
