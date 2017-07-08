package com.android.contacts.hap.numbermark.hwtoms.api;

import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestCorrection;
import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestDetailForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestInfoForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestTelForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseCorrection;
import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseDetailForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseInfoForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseTelForHW;
import java.util.List;

public interface IApiManager {
    TomsResponseCorrection correction(TomsRequestCorrection tomsRequestCorrection, String str);

    List<TomsResponseDetailForHW> detailForHuawei(TomsRequestDetailForHW tomsRequestDetailForHW, String str);

    List<TomsResponseInfoForHW> infoForHuawei(TomsRequestInfoForHW tomsRequestInfoForHW, String str);

    TomsResponseTelForHW telForHuawei(TomsRequestTelForHW tomsRequestTelForHW, String str);
}
