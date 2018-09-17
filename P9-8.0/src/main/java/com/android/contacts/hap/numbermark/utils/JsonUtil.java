package com.android.contacts.hap.numbermark.utils;

import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestCorrection;
import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestDetailForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestInfoForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestTelForHW;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {
    public static JsonUtil getInstance() {
        return new JsonUtil();
    }

    public String object2Json(TomsRequestTelForHW item) {
        if (item == null) {
            return null;
        }
        JSONObject object = new JSONObject();
        try {
            object.put("queryNum", item.getQueryNum());
            object.put("imsi", item.getImsi());
            object.put("imei", item.getImei());
            object.put("channelno", item.getChannelno());
            object.put("timestamp", item.getTimestamp());
            object.put("subjectNum", item.getSubjectNum());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public String object2Json(TomsRequestInfoForHW item) {
        if (item == null) {
            return null;
        }
        JSONObject object = new JSONObject();
        try {
            object.put("queryNum", item.getQueryNum());
            object.put("imsi", item.getImsi());
            object.put("imei", item.getImei());
            object.put("channelno", item.getChannelno());
            object.put("start", item.getStart());
            object.put("resultCount", item.getResultCount());
            object.put("keyword", item.getKeyword());
            object.put("custClass", item.getCustClass());
            object.put("regionCode", item.getRegionCode());
            object.put("regionName", item.getRegionName());
            object.put("poiX", item.getPoiX());
            object.put("poiY", item.getPoiY());
            object.put("poiR", item.getPoiR());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public String object2Json(TomsRequestDetailForHW item) {
        if (item == null) {
            return null;
        }
        JSONObject object = new JSONObject();
        try {
            object.put("queryNum", item.getQueryNum());
            object.put("imsi", item.getImsi());
            object.put("imei", item.getImei());
            object.put("channelno", item.getChannelno());
            object.put("id", item.getId());
            object.put("type", item.getType());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public String object2Json(TomsRequestCorrection item) {
        if (item == null) {
            return null;
        }
        JSONObject object = new JSONObject();
        try {
            object.put("queryNum", item.getQueryNum());
            object.put("imsi", item.getImsi());
            object.put("imei", item.getImei());
            object.put("channelno", item.getChannelno());
            object.put("oldName", item.getOldName());
            object.put("newName", item.getNewName());
            object.put("oldAddr", item.getOldAddr());
            object.put("newAddr", item.getNewAddr());
            object.put("oldTel", item.getOldTel());
            object.put("newTel", item.getNewTel());
            object.put("problem", item.getProblem());
            object.put("mytel", item.getMytel());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }
}
