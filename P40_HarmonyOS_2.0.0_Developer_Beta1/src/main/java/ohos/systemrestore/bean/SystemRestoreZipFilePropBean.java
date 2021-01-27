package ohos.systemrestore.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.startup.utils.StartUpStringUtil;
import ohos.systemrestore.ISystemRestoreProgressListener;
import ohos.systemrestore.SystemRestoreException;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;

public class SystemRestoreZipFilePropBean {
    private static final int BYTE_MAX_READ_BUFFER_SIZE = 4096;
    private static final String DEFAULT_OTA_CERTS_FILE = "/system/etc/security/otacerts.zip";
    private static final int EOCD_OFFSET_BYTE = 22;
    private static final int INTEGER_SHIFT_1BYTES = 8;
    private static final long PUBLISH_PROGRESS_INTERVAL_MS = 500;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218115072, SystemRestoreZipFilePropBean.class.getSimpleName());
    private static final int ZIP_FILE_FOOTER_SIZE = 6;
    private int commentLength;
    private byte[] eocdBytes;
    private long fileLength;
    private PKCS7 pkcs7Block;
    private RandomAccessFile randomAccessFile = null;
    private int signatureStart;

    private String getDefaultKeystoreFileName() {
        return DEFAULT_OTA_CERTS_FILE;
    }

    public SystemRestoreZipFilePropBean(RandomAccessFile randomAccessFile2) {
        this.randomAccessFile = randomAccessFile2;
    }

    public void getEOCDProperty() throws SystemRestoreException {
        try {
            this.fileLength = this.randomAccessFile.length();
            if (this.fileLength > 6) {
                this.randomAccessFile.seek(this.fileLength - 6);
                byte[] bArr = new byte[6];
                this.randomAccessFile.readFully(bArr);
                if (bArr[2] == -1 && bArr[3] == -1) {
                    setCommentSize(bArr);
                    setSignatureStart(bArr);
                    return;
                }
                throw new SystemRestoreException("no signature in file (no footer)");
            }
            HiLog.error(TAG, "file length less than 6 bytes.", new Object[0]);
            throw new SystemRestoreException("file length less than 6 bytes");
        } catch (IOException e) {
            StartUpStringUtil.printException(TAG, e);
            throw new SystemRestoreException("get update file property io exception");
        }
    }

    public void setEndOfCentralDirectory() throws SystemRestoreException {
        long j = this.fileLength;
        int i = this.commentLength;
        if (j >= ((long) (i + 22))) {
            this.eocdBytes = new byte[(i + 22)];
            try {
                this.randomAccessFile.seek(j - ((long) (i + 22)));
                this.randomAccessFile.readFully(this.eocdBytes);
                byte[] bArr = this.eocdBytes;
                if (bArr[0] == 80 && bArr[1] == 75 && bArr[2] == 5 && bArr[3] == 6) {
                    int i2 = 4;
                    while (true) {
                        byte[] bArr2 = this.eocdBytes;
                        if (i2 >= bArr2.length - 3) {
                            return;
                        }
                        if (bArr2[i2] == 80 && bArr2[i2 + 1] == 75 && bArr2[i2 + 2] == 5 && bArr2[i2 + 3] == 6) {
                            HiLog.error(TAG, "EOCD marker found after start of EOCD", new Object[0]);
                            throw new SystemRestoreException("EOCD marker found after start of EOCD");
                        }
                        i2++;
                    }
                } else {
                    HiLog.error(TAG, "no signature in file (bad footer)", new Object[0]);
                    throw new SystemRestoreException("no signature in file (bad footer)");
                }
            } catch (IOException e) {
                StartUpStringUtil.printException(TAG, e);
                throw new SystemRestoreException("get update file property io exception");
            }
        } else {
            HiLog.error(TAG, "file format error.", new Object[0]);
            throw new SystemRestoreException("file format error");
        }
    }

    public void verifiedCertsFile(File file) throws SystemRestoreException {
        int i = this.signatureStart;
        try {
            this.pkcs7Block = new PKCS7(new ByteArrayInputStream(this.eocdBytes, (this.commentLength + 22) - i, i));
            X509Certificate[] certificates = this.pkcs7Block.getCertificates();
            if (certificates == null || certificates.length == 0) {
                HiLog.error(TAG, "signature contains no certificates", new Object[0]);
                throw new SystemRestoreException("signature contains no certificates");
            } else {
                handleTrustedCerts(file, certificates[0].getPublicKey());
            }
        } catch (IOException e) {
            StartUpStringUtil.printException(TAG, e);
            throw new SystemRestoreException("PKCS7 IOException.");
        }
    }

    public void readAndVerifyFile(ISystemRestoreProgressListener iSystemRestoreProgressListener, boolean z) throws SystemRestoreException {
        SignerInfo signerInfo;
        long currentTimeMillis = System.currentTimeMillis();
        if (iSystemRestoreProgressListener != null) {
            iSystemRestoreProgressListener.onProgressChanged(0);
        }
        try {
            this.randomAccessFile.seek(0);
            SignerInfo[] signerInfos = this.pkcs7Block.getSignerInfos();
            if (signerInfos == null || signerInfos.length == 0) {
                HiLog.error(TAG, "signature contains no signedData", new Object[0]);
                throw new SystemRestoreException("signature contains no signedData");
            }
            SignerInfo signerInfo2 = signerInfos[0];
            if (z) {
                try {
                    signerInfo = this.pkcs7Block.verify(signerInfo2, toByteArray(new SystemRestoreInputStream(currentTimeMillis, iSystemRestoreProgressListener)));
                } catch (NoSuchAlgorithmException | SignatureException e) {
                    StartUpStringUtil.printException(TAG, e);
                    throw new SystemRestoreException("Byte signature digest verification exception");
                }
            } else {
                try {
                    signerInfo = this.pkcs7Block.verify(signerInfo2, new SystemRestoreInputStream(currentTimeMillis, iSystemRestoreProgressListener));
                } catch (IOException | NoSuchAlgorithmException | SignatureException e2) {
                    StartUpStringUtil.printException(TAG, e2);
                    throw new SystemRestoreException("InputStream signature digest verification exception");
                }
            }
            boolean interrupted = Thread.interrupted();
            if (iSystemRestoreProgressListener != null) {
                iSystemRestoreProgressListener.onProgressChanged(100);
            }
            if (interrupted) {
                throw new SystemRestoreException("verification was interrupted");
            } else if (signerInfo == null) {
                throw new SystemRestoreException("signature digest verification failed");
            }
        } catch (IOException e3) {
            StartUpStringUtil.printException(TAG, e3);
            throw new SystemRestoreException("randomAccessFile exception");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002b, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002c, code lost:
        $closeResource(r5, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002f, code lost:
        throw r6;
     */
    private void handleTrustedCerts(File file, PublicKey publicKey) throws SystemRestoreException {
        if (file == null) {
            file = getDefaultKeystoreFile();
        }
        boolean z = false;
        ZipFile zipFile = new ZipFile(file);
        CertificateFactory instance = CertificateFactory.getInstance("X.509");
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (true) {
            if (entries.hasMoreElements()) {
                if (isX509CertificateVerified(entries, publicKey, zipFile, instance)) {
                    z = true;
                    break;
                }
            }
        }
        try {
            $closeResource(null, zipFile);
        } catch (IOException | CertificateException e) {
            StartUpStringUtil.printException(TAG, e);
        }
        if (!z) {
            throw new SystemRestoreException("signature doesn't match any trusted key");
        }
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th != null) {
            try {
                autoCloseable.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        } else {
            autoCloseable.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0057, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0058, code lost:
        if (r5 != null) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005a, code lost:
        $closeResource(r7, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005d, code lost:
        throw r8;
     */
    private boolean isX509CertificateVerified(Enumeration<? extends ZipEntry> enumeration, PublicKey publicKey, ZipFile zipFile, CertificateFactory certificateFactory) {
        InputStream inputStream = zipFile.getInputStream((ZipEntry) enumeration.nextElement());
        Certificate generateCertificate = certificateFactory.generateCertificate(inputStream);
        if (generateCertificate instanceof X509Certificate) {
            X509Certificate x509Certificate = (X509Certificate) generateCertificate;
            if (HiLog.isDebuggable()) {
                HiLog.debug(TAG, "isX509CertificateVerified signatureKey %s", publicKey.getFormat());
                HiLog.debug(TAG, "isX509CertificateVerified publicKey %s", x509Certificate.getPublicKey().getFormat());
            }
            if (x509Certificate.getPublicKey().equals(publicKey)) {
                if (inputStream != null) {
                    try {
                        $closeResource(null, inputStream);
                    } catch (IOException | CertificateException e) {
                        StartUpStringUtil.printException(TAG, e);
                    }
                }
                return true;
            }
        }
        if (inputStream != null) {
            $closeResource(null, inputStream);
        }
        return false;
    }

    public long getFileLength() {
        return this.fileLength;
    }

    public int getCommentLength() {
        return this.commentLength;
    }

    private void setCommentSize(byte[] bArr) {
        this.commentLength = ((bArr[5] & 255) << 8) | (bArr[4] & 255);
    }

    public int getSignatureStart() {
        return this.signatureStart;
    }

    private void setSignatureStart(byte[] bArr) {
        this.signatureStart = ((bArr[1] & 255) << 8) | (bArr[0] & 255);
    }

    public RandomAccessFile getRandomAccessFile() {
        return this.randomAccessFile;
    }

    private File getDefaultKeystoreFile() {
        return new File(getDefaultKeystoreFileName());
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0042 A[SYNTHETIC, Splitter:B:22:0x0042] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x004f A[SYNTHETIC, Splitter:B:28:0x004f] */
    private byte[] toByteArray(InputStream inputStream) {
        Throwable th;
        IOException e;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
            try {
                int copy = copy(inputStream, byteArrayOutputStream2);
                if (HiLog.isDebuggable()) {
                    HiLog.debug(TAG, "toByteArray is %{public}d.", Integer.valueOf(copy));
                }
                byte[] byteArray = byteArrayOutputStream2.toByteArray();
                try {
                    byteArrayOutputStream2.close();
                } catch (IOException e2) {
                    StartUpStringUtil.printException(TAG, e2);
                }
                return byteArray;
            } catch (IOException e3) {
                e = e3;
                byteArrayOutputStream = byteArrayOutputStream2;
                try {
                    StartUpStringUtil.printException(TAG, e);
                    byte[] bArr = new byte[0];
                    if (byteArrayOutputStream != null) {
                    }
                    return bArr;
                } catch (Throwable th2) {
                    th = th2;
                    byteArrayOutputStream2 = byteArrayOutputStream;
                    if (byteArrayOutputStream2 != null) {
                        try {
                            byteArrayOutputStream2.close();
                        } catch (IOException e4) {
                            StartUpStringUtil.printException(TAG, e4);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (byteArrayOutputStream2 != null) {
                }
                throw th;
            }
        } catch (IOException e5) {
            e = e5;
            StartUpStringUtil.printException(TAG, e);
            byte[] bArr2 = new byte[0];
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e6) {
                    StartUpStringUtil.printException(TAG, e6);
                }
            }
            return bArr2;
        }
    }

    private int copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        long copyLarge = copyLarge(inputStream, outputStream);
        if (copyLarge > 2147483647L) {
            return -1;
        }
        return (int) copyLarge;
    }

    private long copyLarge(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bArr = new byte[4096];
        int read = inputStream.read(bArr);
        long j = 0;
        while (read != -1) {
            outputStream.write(bArr, 0, read);
            j += (long) read;
            read = inputStream.read(bArr);
        }
        return j;
    }

    /* access modifiers changed from: private */
    public class SystemRestoreInputStream extends InputStream {
        private int lastPercent = 0;
        private long lastPublishTime = 0;
        private ISystemRestoreProgressListener listener;
        private long soFars = 0;
        private long toRead = ((SystemRestoreZipFilePropBean.this.fileLength - ((long) SystemRestoreZipFilePropBean.this.commentLength)) - 2);

        public SystemRestoreInputStream(long j, ISystemRestoreProgressListener iSystemRestoreProgressListener) {
            this.lastPublishTime = j;
            this.listener = iSystemRestoreProgressListener;
        }

        @Override // java.io.InputStream
        public int read() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override // java.io.InputStream
        public int read(byte[] bArr, int i, int i2) throws IOException {
            if (this.soFars >= this.toRead || Thread.interrupted()) {
                return -1;
            }
            long j = this.soFars;
            long j2 = this.toRead;
            if (((long) i2) + j > j2) {
                i2 = (int) (j2 - j);
            }
            int read = SystemRestoreZipFilePropBean.this.randomAccessFile.read(bArr, i, i2);
            this.soFars += (long) read;
            if (this.listener != null) {
                long currentTimeMillis = System.currentTimeMillis();
                int i3 = (int) ((this.soFars * 100) / this.toRead);
                if (i3 > this.lastPercent && currentTimeMillis - this.lastPublishTime > SystemRestoreZipFilePropBean.PUBLISH_PROGRESS_INTERVAL_MS) {
                    this.lastPercent = i3;
                    this.lastPublishTime = currentTimeMillis;
                    this.listener.onProgressChanged(this.lastPercent);
                }
            }
            return read;
        }
    }
}
