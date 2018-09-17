package com.android.contacts.hap.numbermark.hwtoms.parser;

import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseInfoForHW;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TomsResponseInfoForHWParser extends TomsJsonParser<TomsResponseInfoForHW> {
    public List<TomsResponseInfoForHW> listParser(JSONArray array, String errorCode) throws JSONException {
        List<TomsResponseInfoForHW> list = new ArrayList();
        int array_length = array.length();
        for (int i = 0; i < array_length; i++) {
            list.add(objectParser(array.getJSONObject(i), errorCode));
        }
        return list;
    }

    public TomsResponseInfoForHW objectParser(JSONObject jsonObject, String errorCode) {
        TomsResponseInfoForHW bean = new TomsResponseInfoForHW();
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
        return bean;
    }
}
