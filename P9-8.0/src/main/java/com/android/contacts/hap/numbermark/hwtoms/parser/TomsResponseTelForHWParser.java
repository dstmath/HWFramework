package com.android.contacts.hap.numbermark.hwtoms.parser;

import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseTelForHW;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TomsResponseTelForHWParser extends TomsJsonParser<TomsResponseTelForHW> {
    public List<TomsResponseTelForHW> listParser(JSONArray array, String errorCode) throws JSONException {
        List<TomsResponseTelForHW> list = new ArrayList();
        int array_length = array.length();
        for (int i = 0; i < array_length; i++) {
            list.add(objectParser(array.getJSONObject(i), errorCode));
        }
        return list;
    }

    public TomsResponseTelForHW objectParser(JSONObject jsonObject, String errorCode) {
        TomsResponseTelForHW bean = new TomsResponseTelForHW();
        bean.setErrorCode(errorCode);
        bean.setName(jsonObject.optString("name"));
        bean.setClassname1(jsonObject.optString("classname1"));
        bean.setClassname2(jsonObject.optString("classname2"));
        bean.setType(jsonObject.optString("type"));
        return bean;
    }
}
