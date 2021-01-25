package com.huawei.security.hccm.common.connection;

import android.support.annotation.NonNull;
import com.huawei.security.hccm.EnrollmentException;
import com.huawei.security.hccm.param.EnrollmentParamsSpec;
import java.security.cert.Certificate;
import org.json.JSONObject;

public interface CAConnection {
    Certificate[] enroll(@NonNull String str, @NonNull String str2, @NonNull EnrollmentParamsSpec enrollmentParamsSpec, JSONObject jSONObject) throws EnrollmentException;
}
