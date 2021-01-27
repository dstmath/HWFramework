package com.huawei.security.hccm.local;

import android.support.annotation.NonNull;
import android.util.Log;
import com.huawei.security.hccm.ClientCertificateManager;
import com.huawei.security.hccm.EnrollmentException;
import com.huawei.security.hccm.common.connection.cmp.CMPConnection;
import com.huawei.security.hccm.common.utils.BigDataUpload;
import com.huawei.security.hccm.param.EnrollmentContext;
import com.huawei.security.hccm.param.EnrollmentParamsSpec;
import com.huawei.security.hccm.param.ProtocolParam;
import com.huawei.security.hccm.param.ProtocolParamCMP;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.concurrent.locks.ReentrantLock;

public final class LocalClientCertificateManager extends ClientCertificateManager {
    private static final int CERT_ENROLMENT_RESULT = 940005001;
    private static final int CERT_STORAGE_RESULT = 940005002;
    private static final String HCCM_BIG_DATA_PNAME_INT = "RET";
    private static final String TAG = "LocalClientCertificateManager";
    private static final ReentrantLock mLock = new ReentrantLock();
    private String mErrorMsg;
    private String mKeyStoreProvider = "";
    private String mKeyStoreType = "";

    public LocalClientCertificateManager(String keyStoreType, String keyStoreProvider) {
        this.mKeyStoreType = keyStoreType;
        this.mKeyStoreProvider = keyStoreProvider;
    }

    @Override // com.huawei.security.hccm.ClientCertificateManager
    public Certificate[] enroll(@NonNull EnrollmentParamsSpec params) throws EnrollmentException {
        Log.i(TAG, "Start to enroll certificate");
        mLock.lock();
        try {
            validateEnrollmentParamsSpec(params);
            if (isPrivateKeyExists(params.getAlias())) {
                Certificate[] chain = null;
                if (EnrollmentParamsSpec.ENROLLMENT_PROTOCOL_CMP.equals(params.getEnrollmentProtocol())) {
                    chain = doEnroll(params);
                }
                if (chain != null) {
                    int result = chain.length > 0 ? 0 : -25;
                    mLock.unlock();
                    int bigDataReportResult = BigDataUpload.reportToBigData(CERT_ENROLMENT_RESULT, HCCM_BIG_DATA_PNAME_INT, result);
                    if (bigDataReportResult != 0) {
                        Log.e(TAG, "Report hccm cert enrolment failed " + bigDataReportResult);
                    }
                    Log.d(TAG, "Report hccm cert enrolment succeed");
                    return chain;
                }
                this.mErrorMsg = "failed to enroll: get cert chain null!";
                throw new EnrollmentException(this.mErrorMsg, -1);
            }
            throw new EnrollmentException("Alias associated key pair does not exist in the key store.", -30);
        } catch (EnrollmentException e) {
            Log.e(TAG, e.getMessage());
            e.getErrorCode();
            throw e;
        } catch (Exception e2) {
            Log.e(TAG, e2.getMessage());
            throw new EnrollmentException(e2.getMessage(), -9);
        } catch (Throwable th) {
            mLock.unlock();
            int bigDataReportResult2 = BigDataUpload.reportToBigData(CERT_ENROLMENT_RESULT, HCCM_BIG_DATA_PNAME_INT, 0);
            if (bigDataReportResult2 != 0) {
                Log.e(TAG, "Report hccm cert enrolment failed " + bigDataReportResult2);
            }
            Log.d(TAG, "Report hccm cert enrolment succeed");
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0066, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        android.util.Log.e(com.huawei.security.hccm.local.LocalClientCertificateManager.TAG, "Store cert failed: " + r7.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0089, code lost:
        throw new com.huawei.security.hccm.EnrollmentException(r7.getMessage(), -4);
     */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0066 A[ExcHandler: IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException (r7v3 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:8:0x005c] */
    @Override // com.huawei.security.hccm.ClientCertificateManager
    public void store(@NonNull EnrollmentContext context) throws EnrollmentException {
        Log.i(TAG, "Start to store certificate");
        mLock.lock();
        Log.d(TAG, "Store");
        Certificate[] chain = context.getClientCertificateChain();
        if (chain.length != 0) {
            String alias = context.getEnrollmentParams().getAlias();
            KeyStore ks = KeyStore.getInstance(this.mKeyStoreType);
            ks.load(null);
            ks.setKeyEntry(alias, null, chain);
            mLock.unlock();
            int bigDataReportResult = BigDataUpload.reportToBigData(CERT_STORAGE_RESULT, HCCM_BIG_DATA_PNAME_INT, 0);
            if (bigDataReportResult != 0) {
                Log.e(TAG, "Report cert storage data failed " + bigDataReportResult);
            }
            Log.d(TAG, "Report cert storage data succeed");
            return;
        }
        try {
            throw new EnrollmentException("Client certificate chain is empty", -25);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
        } catch (Throwable th) {
            mLock.unlock();
            int bigDataReportResult2 = BigDataUpload.reportToBigData(CERT_STORAGE_RESULT, HCCM_BIG_DATA_PNAME_INT, -25);
            if (bigDataReportResult2 != 0) {
                Log.e(TAG, "Report cert storage data failed " + bigDataReportResult2);
            }
            Log.d(TAG, "Report cert storage data succeed");
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0084, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        android.util.Log.e(com.huawei.security.hccm.local.LocalClientCertificateManager.TAG, "Delete cert failed: " + r1.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x00a8, code lost:
        throw new com.huawei.security.hccm.EnrollmentException(r1.getMessage(), -26);
     */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0084 A[ExcHandler: EnrollmentException | IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateException (r1v4 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:3:0x0022] */
    @Override // com.huawei.security.hccm.ClientCertificateManager
    public void delete(@NonNull String alias) throws EnrollmentException {
        mLock.lock();
        Log.d(TAG, "Delete cert context");
        KeyStore ks = KeyStore.getInstance(this.mKeyStoreType);
        ks.load(null);
        KeyStore.Entry entry = ks.getEntry(alias, null);
        if (entry != null) {
            try {
                if (((KeyStore.PrivateKeyEntry) entry).getPrivateKey() != null) {
                    ks.deleteEntry(alias);
                    mLock.unlock();
                    return;
                }
                Log.e(TAG, "Key bond to alias '" + alias + "' not exist");
                throw new EnrollmentException("Alias '" + alias + "' does not exist!", -8);
            } catch (EnrollmentException | IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateException e) {
            } catch (Throwable th) {
                mLock.unlock();
                throw th;
            }
        } else {
            Log.e(TAG, "No key found under alias!");
            throw new EnrollmentException("Alias '" + alias + "' does not exist!", -8);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0078, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        android.util.Log.e(com.huawei.security.hccm.local.LocalClientCertificateManager.TAG, "Find context failed");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0089, code lost:
        throw new com.huawei.security.hccm.EnrollmentException(r1.getMessage(), -27);
     */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0078 A[ExcHandler: IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateException (r1v4 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:2:0x0020] */
    @Override // com.huawei.security.hccm.ClientCertificateManager
    public EnrollmentContext find(@NonNull String alias) throws EnrollmentException {
        mLock.lock();
        Log.d(TAG, "Find cert context by alias");
        KeyStore ks = KeyStore.getInstance(this.mKeyStoreType);
        ks.load(null);
        KeyStore.Entry entry = ks.getEntry(alias, null);
        if (entry == null) {
            try {
                Log.e(TAG, "Key bounded to '" + alias + "' does not exist");
            } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateException e) {
            } catch (Throwable th) {
                mLock.unlock();
                throw th;
            }
        }
        if (((KeyStore.PrivateKeyEntry) entry).getPrivateKey() == null) {
            Log.e(TAG, "Key bounded to '" + alias + "' does not exist");
            mLock.unlock();
            return null;
        }
        EnrollmentContext context = new EnrollmentContext(new EnrollmentParamsSpec.Builder(alias).build());
        context.setClientCertificateChain(ks.getCertificateChain(alias));
        mLock.unlock();
        return context;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void validateEnrollmentParamsSpec(EnrollmentParamsSpec params) throws EnrollmentException {
        boolean z;
        Log.d(TAG, "Validate Enrollment Params.");
        int errCode = 0;
        if (params == null || params.isCommonUsedParamsNull()) {
            this.mErrorMsg = "All params shouldn't be null";
            throw new EnrollmentException(this.mErrorMsg, -1);
        } else if (params.getAlias().length() < 1 || params.getAlias().length() > 89) {
            this.mErrorMsg = "the alias length is out of its scope";
            throw new EnrollmentException(this.mErrorMsg, -1);
        } else {
            ProtocolParam<?> protocolParam = params.getProtocolParam();
            if (protocolParam instanceof ProtocolParamCMP) {
                ProtocolParamCMP cmpParam = (ProtocolParamCMP) protocolParam;
                String enrollmentProtocol = params.getEnrollmentProtocol();
                switch (enrollmentProtocol.hashCode()) {
                    case -1922742195:
                        if (enrollmentProtocol.equals(EnrollmentParamsSpec.ENROLLMENT_PROTOCOL_CMC)) {
                            z = false;
                            break;
                        }
                        z = true;
                        break;
                    case -1922742182:
                        if (enrollmentProtocol.equals(EnrollmentParamsSpec.ENROLLMENT_PROTOCOL_CMP)) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case -1922740070:
                        if (enrollmentProtocol.equals(EnrollmentParamsSpec.ENROLLMENT_PROTOCOL_EST)) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case 525001287:
                        if (enrollmentProtocol.equals(EnrollmentParamsSpec.ENROLLMENT_PROTOCOL_SCEP)) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    default:
                        z = true;
                        break;
                }
                if (!z || z) {
                    if (cmpParam.getRootCertificate() == null) {
                        this.mErrorMsg = "root cert is needed under cmp protocol, but it's null";
                        errCode = -29;
                    }
                } else if (z || z) {
                    this.mErrorMsg = "Enrollment protocol not supported: " + params.getEnrollmentProtocol();
                    errCode = -24;
                } else {
                    this.mErrorMsg = "Enrollment protocol unknown: " + params.getEnrollmentProtocol();
                    errCode = -28;
                }
                if (errCode != 0) {
                    Log.e(TAG, this.mErrorMsg);
                    throw new EnrollmentException(this.mErrorMsg, errCode);
                }
                return;
            }
            throw new EnrollmentException("ProtocolParam is not CMP", -24);
        }
    }

    private boolean isPrivateKeyExists(String alias) throws EnrollmentException {
        try {
            KeyStore ks = KeyStore.getInstance(this.mKeyStoreType);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(alias, null);
            if (entry == null) {
                Log.e(TAG, "Key bounded to '" + alias + "' does not exist");
            }
            return ((KeyStore.PrivateKeyEntry) entry).getPrivateKey() != null;
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateException e) {
            throw new EnrollmentException("failed to check private key: " + e.getMessage(), -9);
        }
    }

    @NonNull
    private Certificate[] doEnroll(@NonNull EnrollmentParamsSpec params) throws EnrollmentException {
        String enrollmentProtocol = params.getEnrollmentProtocol();
        if (((enrollmentProtocol.hashCode() == -1922742182 && enrollmentProtocol.equals(EnrollmentParamsSpec.ENROLLMENT_PROTOCOL_CMP)) ? (char) 0 : 65535) == 0) {
            try {
                return new CMPConnection().enroll(this.mKeyStoreType, this.mKeyStoreProvider, params, params.getConnectionSettings());
            } catch (EnrollmentException e) {
                throw e;
            } catch (Exception e2) {
                Log.e(TAG, "Enroll cert failed: " + e2.getMessage());
                throw new EnrollmentException("CA enroll failed: " + e2.getMessage(), -9);
            }
        } else {
            throw new EnrollmentException("Unsupported protocol", -24);
        }
    }
}
