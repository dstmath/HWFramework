package com.huawei.android.media;

import android.media.HwMediaDrmExImpl;
import com.huawei.android.util.NoNeedProvisionException;
import java.nio.BufferUnderflowException;

public class MediaDrmEx {
    private static final String TAG = "MediaDrmEx";

    public static int getOEMCertificateStatus() throws Exception {
        return HwMediaDrmExImpl.getOEMCertificateStatus();
    }

    public static String getOEMProvisionRequest() throws NoNeedProvisionException, BufferUnderflowException {
        return HwMediaDrmExImpl.getOEMProvisionRequest();
    }

    public static int provideOEMProvisionResponse(String provisionResponse) throws Exception {
        return HwMediaDrmExImpl.provideOEMProvisionResponse(provisionResponse);
    }
}
