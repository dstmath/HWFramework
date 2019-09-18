package com.huawei.wallet.sdk.business.diploma.util;

import android.content.Context;
import com.huawei.wallet.sdk.business.diploma.request.DiplomaUploadRequest;
import com.huawei.wallet.sdk.business.diploma.response.DiplomaUploadResponse;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.common.apdu.ese.impl.ESEInfoManager;
import com.huawei.wallet.sdk.common.http.service.CommonService;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.WalletTaException;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.util.concurrent.Executors;

public class CertUtil {
    private static final int INIT_FLAG_FAIL = 0;
    private static final int INIT_FLAG_SUCCESS = 1;
    private static final String SIGN_CERT = "RSAWithCert";
    private static final int UPLOADFLAG_FAIL = 0;
    private static final int UPLOADFLAG_SUCCESS = 1;
    /* access modifiers changed from: private */
    public static String businessCert = "";
    /* access modifiers changed from: private */
    public static String deviceCert = "";
    /* access modifiers changed from: private */
    public static UploadCertInterface uploadCertInterface;

    public static int initCert(Context context) {
        int flag = DiplomaUtil.initDiploma(context);
        LogC.e("=============" + flag, false);
        return flag;
    }

    public static void uploadCert(final Context context, UploadCertInterface listener) {
        uploadCertInterface = listener;
        if (initCert(context) != 1) {
            LogC.e("judgeInitResult failed", false);
            uploadCertInterface.onEventBack(1, "upload fail");
        } else if (DiplomaUtil.getUploadFlag(context) == 0) {
            Executors.newSingleThreadExecutor().submit(new Runnable() {
                public void run() {
                    CommonService service = new CommonService(context, AddressNameMgr.MODULE_NAME_WISECLOUDVIRTUALCARD);
                    DiplomaUploadRequest diplomaUploadRequest = new DiplomaUploadRequest();
                    String sign = "";
                    String deviceId = PhoneDeviceUtil.getDeviceID(context);
                    try {
                        String unused = CertUtil.deviceCert = WalletTaManager.getInstance(context).queryCertification().getDeviceCert();
                        String unused2 = CertUtil.businessCert = WalletTaManager.getInstance(context).queryCertification().getServiceCert();
                        sign = WalletTaManager.getInstance(context).queryCertification().getAuthSignResult();
                    } catch (WalletTaException e) {
                        LogC.e("DiplomaUtil|DiplomaUploadResponse|WalletTaException", false);
                    }
                    diplomaUploadRequest.setDeviceCert(CertUtil.deviceCert);
                    diplomaUploadRequest.setBusinessCert(CertUtil.businessCert);
                    diplomaUploadRequest.setDeviceId(deviceId);
                    diplomaUploadRequest.setSignType(CertUtil.SIGN_CERT);
                    diplomaUploadRequest.setCplcList(ESEInfoManager.getInstance(context).queryCplcListString());
                    diplomaUploadRequest.setSign(sign);
                    DiplomaUploadResponse response = service.uploadDiploma(diplomaUploadRequest);
                    if (response.getResultCode() == 0 || response.getResultCode() == 11) {
                        LogC.i("uploadDiploma success" + response.getResultCode(), false);
                        try {
                            WalletTaManager.getInstance(context).setCertUploadFlag(1);
                        } catch (WalletTaException e2) {
                            LogC.i("uploadDiploma|setCertUploadFlag WalletTaException", false);
                        }
                        CertUtil.uploadCertInterface.onEventBack(0, "upload success");
                        return;
                    }
                    LogC.i("uploadDiploma failed" + response.getResultCode(), false);
                    CertUtil.uploadCertInterface.onEventBack(1, "upload fail");
                }
            });
        } else {
            LogC.i("uploadDiploma cer has upload", false);
            uploadCertInterface.onEventBack(0, "upload success");
        }
    }
}
