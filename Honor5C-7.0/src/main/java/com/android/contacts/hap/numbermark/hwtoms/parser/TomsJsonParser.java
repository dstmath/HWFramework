package com.android.contacts.hap.numbermark.hwtoms.parser;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class TomsJsonParser<T> {
    public abstract List<T> listParser(JSONArray jSONArray, String str) throws JSONException;

    public abstract T objectParser(JSONObject jSONObject, String str);
}
