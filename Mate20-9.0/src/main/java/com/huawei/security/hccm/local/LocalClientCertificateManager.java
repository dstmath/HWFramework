package com.huawei.security.hccm.local;

import android.support.annotation.NonNull;
import android.util.Log;
import com.huawei.security.hccm.ClientCertificateManager;
import com.huawei.security.hccm.EnrollmentException;
import com.huawei.security.hccm.common.connection.cmp.CMPConnection;
import com.huawei.security.hccm.common.utils.BigDataUpload;
import com.huawei.security.hccm.param.EnrollmentContext;
import com.huawei.security.hccm.param.EnrollmentParamsSpec;
import com.huawei.security.hccm.param.ProtocolParamCMP;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.concurrent.locks.ReentrantLock;

public final class LocalClientCertificateManager extends ClientCertificateManager {
    private static final int CERT_ENROLMENT_RESULT = 940005001;
    private static final int CERT_STORAGE_RESULT = 940005002;
    private static final String HCCM_BIG_DATA_PNAME_INT = "RET";
    private static final String TAG = "LocalClientCertificateManager";
    private String mErrorMsg;
    private String mKeyStoreProvider = "";
    private String mKeyStoreType = "";
    private ReentrantLock mLock = new ReentrantLock();

    public LocalClientCertificateManager(String keyStoreType, String keyStoreProvider) {
        this.mKeyStoreType = keyStoreType;
        this.mKeyStoreProvider = keyStoreProvider;
    }

    public Certificate[] enroll(@NonNull EnrollmentParamsSpec params) throws Exception {
        Certificate[] chain;
        Log.i(TAG, "start to enroll certificate");
        int result = 0;
        try {
            validateEnrollmentParamsSpec(params);
            if (privateKeyExists(params.getAlias())) {
                this.mLock.lock();
                if (EnrollmentParamsSpec.ENROLLMENT_PROTOCOL_CMP.equals(params.getEnrollmentProtocol())) {
                    chain = doEnroll(params);
                } else {
                    chain = null;
                }
                if (chain.length <= 0) {
                    result = -25;
                }
                int bigDataReportResult = BigDataUpload.reportToBigData(CERT_ENROLMENT_RESULT, HCCM_BIG_DATA_PNAME_INT, result);
                if (bigDataReportResult != 0) {
                    Log.e(TAG, "report hccm cert enrolment failed " + bigDataReportResult);
                }
                Log.d(TAG, "report hccm cert enrolment succeed");
                this.mLock.unlock();
                return chain;
            }
            Log.e(TAG, "Alias associated key pair does not exist in the key store.");
            throw new EnrollmentException("Alias associated key pair does not exist in the key store.", -30);
        } catch (IllegalArgumentException | NullPointerException e) {
            this.mErrorMsg = "some of the enrollment params are null or illegal";
            Log.e(TAG, this.mErrorMsg);
            throw new EnrollmentException(this.mErrorMsg, -1);
        } catch (EnrollmentException e2) {
            Log.e(TAG, e2.getMessage());
            int result2 = e2.getErrorCode();
            throw e2;
        } catch (Exception e3) {
            Log.e(TAG, e3.getMessage());
            throw new EnrollmentException(e3.getMessage(), -9);
        } catch (Throwable th) {
            int bigDataReportResult2 = BigDataUpload.reportToBigData(CERT_ENROLMENT_RESULT, HCCM_BIG_DATA_PNAME_INT, 0);
            if (bigDataReportResult2 != 0) {
                Log.e(TAG, "report hccm cert enrolment failed " + bigDataReportResult2);
            }
            Log.d(TAG, "report hccm cert enrolment succeed");
            this.mLock.unlock();
            throw th;
        }
    }

    public void store(@NonNull EnrollmentContext context) throws EnrollmentException {
        Log.i(TAG, "start to store certificate");
        int result = 0;
        try {
            this.mLock.lock();
            Log.d(TAG, "store");
            Certificate[] chain = context.getClientCertificateChain();
            if (chain.length != 0) {
                String alias = context.getEnrollmentParams().getAlias();
                KeyStore ks = KeyStore.getInstance(this.mKeyStoreType);
                ks.load(null, null);
                ks.setKeyEntry(alias, null, chain);
                int bigDataReportResult = BigDataUpload.reportToBigData(CERT_STORAGE_RESULT, HCCM_BIG_DATA_PNAME_INT, 0);
                if (bigDataReportResult != 0) {
                    Log.e(TAG, "report cert storage data failed " + bigDataReportResult);
                }
                Log.d(TAG, "report cert storage data succeed");
                this.mLock.unlock();
                return;
            }
            result = -25;
            throw new EnrollmentException("Client certificate chain is empty", -25);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            Log.e(TAG, "store cert failed because of " + e.getMessage());
            throw new EnrollmentException(e.getMessage(), -4);
        } catch (Throwable th) {
            int bigDataReportResult2 = BigDataUpload.reportToBigData(CERT_STORAGE_RESULT, HCCM_BIG_DATA_PNAME_INT, result);
            if (bigDataReportResult2 != 0) {
                Log.e(TAG, "report cert storage data failed " + bigDataReportResult2);
            }
            Log.d(TAG, "report cert storage data succeed");
            this.mLock.unlock();
            throw th;
        }
    }

    public void delete(@NonNull String alias) throws EnrollmentException {
        try {
            this.mLock.lock();
            Log.d(TAG, "delete cert context");
            KeyStore ks = KeyStore.getInstance(this.mKeyStoreType);
            ks.load(null, null);
            if (((PrivateKey) ks.getKey(alias, null)) != null) {
                ks.deleteEntry(alias);
                this.mLock.unlock();
                return;
            }
            Log.e(TAG, "key bond to alias '" + alias + "' not exist");
            throw new EnrollmentException("Alias '" + alias + "' does not exist!", -8);
        } catch (EnrollmentException | IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException e) {
            Log.e(TAG, "delete cert failed because of " + e.getMessage());
            throw new EnrollmentException(e.getMessage(), -26);
        } catch (Throwable th) {
            this.mLock.unlock();
            throw th;
        }
    }

    public EnrollmentContext find(@NonNull String alias) throws EnrollmentException {
        try {
            this.mLock.lock();
            Log.d(TAG, "find cert context by alias");
            KeyStore ks = KeyStore.getInstance(this.mKeyStoreType);
            ks.load(null, null);
            if (((PrivateKey) ks.getKey(alias, null)) == null) {
                Log.e(TAG, "key bounded to '" + alias + "' does not exist");
                this.mLock.unlock();
                return null;
            }
            EnrollmentContext ctx = new EnrollmentContext(new EnrollmentParamsSpec.Builder(alias).build());
            ctx.setClientCertificateChain(ks.getCertificateChain(alias));
            this.mLock.unlock();
            return ctx;
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException e) {
            Log.e(TAG, "find context failed");
            throw new EnrollmentException(e.getMessage(), -27);
        } catch (Throwable th) {
            this.mLock.unlock();
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0083, code lost:
        if (r3.equals(com.huawei.security.hccm.param.EnrollmentParamsSpec.ENROLLMENT_PROTOCOL_CMP) != false) goto L_0x0091;
     */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0094  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00c8  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00e6 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00e7  */
    private void validateEnrollmentParamsSpec(EnrollmentParamsSpec params) throws Exception {
        Log.d(TAG, "validate Enrollment Params ");
        int errCode = 0;
        if (params == null || params.getAlias() == null || params.getEnrollmentCertSubject() == null || params.getEnrollmentURL() == null || params.getConnectionSettings() == null || params.getEnrollmentProtocol() == null || params.getProtocolParam() == null) {
            throw new NullPointerException("all params shouldn't be null");
        }
        char c = 1;
        if (params.getAlias().length() < 1 || params.getAlias().length() > 89) {
            throw new IllegalArgumentException("the alias length is out of its scope");
        }
        ProtocolParamCMP cmpParam = (ProtocolParamCMP) params.getProtocolParam();
        String enrollmentProtocol = params.getEnrollmentProtocol();
        int hashCode = enrollmentProtocol.hashCode();
        if (hashCode != -1922742195) {
            if (hashCode != -1922742182) {
                if (hashCode != -1922740070) {
                    if (hashCode == 525001287 && enrollmentProtocol.equals(EnrollmentParamsSpec.ENROLLMENT_PROTOCOL_SCEP)) {
                        c = 2;
                        switch (c) {
                            case 0:
                            case 1:
                                if (params.getUserCredential().getChallenge().length == 0) {
                                    this.mErrorMsg = "Local enrollment with attestation requested, but attestation challenge is not set: attestation challenge should be set under CMP/CMC protocol";
                                    errCode = -1;
                                }
                                if (cmpParam.getRootCertificate() == null) {
                                    this.mErrorMsg = "root cert is needed under cmp protocol, but it's null";
                                    errCode = -29;
                                    break;
                                }
                                break;
                            case 2:
                            case 3:
                                this.mErrorMsg = "Enrollment protocol not supported: " + params.getEnrollmentProtocol();
                                errCode = -24;
                                break;
                            default:
                                this.mErrorMsg = "Enrollment protocol unknown: " + params.getEnrollmentProtocol();
                                errCode = -28;
                                break;
                        }
                        if (errCode == 0) {
                            Log.e(TAG, this.mErrorMsg);
                            throw new EnrollmentException(this.mErrorMsg, errCode);
                        }
                        return;
                    }
                } else if (enrollmentProtocol.equals(EnrollmentParamsSpec.ENROLLMENT_PROTOCOL_EST)) {
                    c = 3;
                    switch (c) {
                        case 0:
                        case 1:
                            break;
                        case 2:
                        case 3:
                            break;
                    }
                    if (errCode == 0) {
                    }
                }
            }
        } else if (enrollmentProtocol.equals(EnrollmentParamsSpec.ENROLLMENT_PROTOCOL_CMC)) {
            c = 0;
            switch (c) {
                case 0:
                case 1:
                    break;
                case 2:
                case 3:
                    break;
            }
            if (errCode == 0) {
            }
        }
        c = 65535;
        switch (c) {
            case 0:
            case 1:
                break;
            case 2:
            case 3:
                break;
        }
        if (errCode == 0) {
        }
    }

    private boolean privateKeyExists(String alias) throws Exception {
        try {
            KeyStore ks = KeyStore.getInstance(this.mKeyStoreType);
            ks.load(null);
            return ((PrivateKey) ks.getKey(alias, null)) != null;
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            Log.e(TAG, "get keystore in failed withe message" + e.getMessage());
            throw e;
        }
    }

    private Certificate[] doEnroll(@NonNull EnrollmentParamsSpec params) throws EnrollmentException {
        String enrollmentProtocol = params.getEnrollmentProtocol();
        if (((enrollmentProtocol.hashCode() == -1922742182 && enrollmentProtocol.equals(EnrollmentParamsSpec.ENROLLMENT_PROTOCOL_CMP)) ? (char) 0 : 65535) == 0) {
            try {
                return new CMPConnection().enroll(this.mKeyStoreType, this.mKeyStoreProvider, params, params.getConnectionSettings());
            } catch (EnrollmentException e) {
                throw e;
            } catch (Exception e2) {
                Log.e(TAG, "enroll cert failed");
                throw new EnrollmentException("ca enroll failed", -9);
            }
        } else {
            throw new EnrollmentException("unsupported protocol", -24);
        }
    }
}
