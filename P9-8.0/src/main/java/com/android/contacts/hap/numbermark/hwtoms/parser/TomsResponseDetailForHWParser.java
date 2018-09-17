package com.android.contacts.hap.numbermark.hwtoms.parser;

import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseDetailForHW;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TomsResponseDetailForHWParser extends TomsJsonParser<TomsResponseDetailForHW> {
    public List<TomsResponseDetailForHW> listParser(JSONArray array, String errorCode) throws JSONException {
        List<TomsResponseDetailForHW> list = new ArrayList();
        int arrayLength = array.length();
        for (int i = 0; i < arrayLength; i++) {
            list.add(objectParser(array.getJSONObject(i), errorCode));
        }
        return list;
    }

    public TomsResponseDetailForHW objectParser(JSONObject jsonObject, String errorCode) {
        TomsResponseDetailForHW bean = new TomsResponseDetailForHW();
        bean.setErrorCode(errorCode);
        bean.setId(jsonObject.optString("id"));
        bean.setCustName(jsonObject.optString("custName"));
        bean.setTel(jsonObject.optString("tel"));
        bean.setAddress(jsonObject.optString("address"));
        bean.setLogo(jsonObject.optString("logo"));
        bean.setRegionCode(jsonObject.optString("regionCode"));
        bean.setRegionName(jsonObject.optString("regionName"));
        bean.setClassCode1(jsonObject.optString("classCode1"));
        bean.setClassname1(jsonObject.optString("classname1"));
        bean.setClassCode2(jsonObject.optString("classCode2"));
        bean.setClassname2(jsonObject.optString("classname2"));
        bean.setPoiX(jsonObject.optString("poiX"));
        bean.setPoiY(jsonObject.optString("poiY"));
        bean.setType(jsonObject.optString("type"));
        bean.setUrl(jsonObject.optString("url"));
        bean.setWeibo(jsonObject.optString("weibo"));
        bean.setWeixin(jsonObject.optString("weixin"));
        bean.setYixin(jsonObject.optString("yixin"));
        return bean;
    }
}
