package com.leisen.wallet.sdk.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

public class RequestParams {
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    protected String contentEncoding;
    protected final ConcurrentHashMap<String, FileWrapper> fileParams;
    private boolean isRepeatable;
    protected final ConcurrentHashMap<String, String> urlParams;
    protected final ConcurrentHashMap<String, Object> urlParamsWithObjects;

    public static class FileWrapper {
        public final String contentType;
        public final File file;

        public FileWrapper(File file, String contentType) {
            this.file = file;
            this.contentType = contentType;
        }
    }

    public RequestParams() {
        this(null);
    }

    public RequestParams(Map<String, String> source) {
        this.urlParams = new ConcurrentHashMap();
        this.urlParamsWithObjects = new ConcurrentHashMap();
        this.fileParams = new ConcurrentHashMap();
        this.contentEncoding = "UTF-8";
        this.isRepeatable = false;
        if (source != null) {
            for (Entry<String, String> entry : source.entrySet()) {
                put((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    public void put(String key, String value) {
        if (key != null && value != null) {
            this.urlParams.put(key, value);
        }
    }

    public void put(String key, Object value) {
        if (key != null && value != null) {
            this.urlParamsWithObjects.put(key, value);
        }
    }

    public void put(String key, File file, String contentType) throws FileNotFoundException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException();
        } else if (key != null) {
            this.fileParams.put(key, new FileWrapper(file, contentType));
        }
    }

    public void put(String key, int value) {
        if (key != null) {
            this.urlParams.put(key, String.valueOf(value));
        }
    }

    public void put(String key, long value) {
        if (key != null) {
            this.urlParams.put(key, String.valueOf(value));
        }
    }

    protected List<BasicNameValuePair> getParamsList() {
        List<BasicNameValuePair> lparams = new LinkedList();
        for (Entry<String, String> entry : this.urlParams.entrySet()) {
            lparams.add(new BasicNameValuePair((String) entry.getKey(), (String) entry.getValue()));
        }
        lparams.addAll(getParamsList(null, this.urlParamsWithObjects));
        return lparams;
    }

    private List<BasicNameValuePair> getParamsList(String key, Object value) {
        List<BasicNameValuePair> params = new LinkedList();
        List list;
        Object nestedValue;
        int nestedValueIndex;
        if (value instanceof Map) {
            Map map = (Map) value;
            list = new ArrayList(map.keySet());
            Collections.sort(list);
            for (Object nestedKey : list) {
                if (nestedKey instanceof String) {
                    nestedValue = map.get(nestedKey);
                    if (nestedValue != null) {
                        String nestedKey2;
                        if (key != null) {
                            nestedKey2 = String.format("%s[%s]", new Object[]{key, nestedKey});
                        } else {
                            nestedKey2 = (String) nestedKey;
                        }
                        params.addAll(getParamsList(nestedKey2, nestedValue));
                    }
                }
            }
        } else if (value instanceof List) {
            list = (List) value;
            int listSize = list.size();
            for (nestedValueIndex = 0; nestedValueIndex < listSize; nestedValueIndex++) {
                params.addAll(getParamsList(String.format("%s[%d]", new Object[]{key, Integer.valueOf(nestedValueIndex)}), list.get(nestedValueIndex)));
            }
        } else if (value instanceof Object[]) {
            for (Object paramsList : (Object[]) value) {
                params.addAll(getParamsList(String.format("%s[%d]", new Object[]{key, Integer.valueOf(nestedValueIndex)}), paramsList));
            }
        } else if (value instanceof Set) {
            for (Object nestedValue2 : (Set) value) {
                params.addAll(getParamsList(key, nestedValue2));
            }
        } else {
            params.add(new BasicNameValuePair(key, value.toString()));
        }
        return params;
    }

    protected String getParamString() {
        return URLEncodedUtils.format(getParamsList(), this.contentEncoding);
    }

    public HttpEntity getEntity(ResponseHandlerInterface responseHandler) throws IOException {
        return createFormEntity();
    }

    private HttpEntity createFormEntity() {
        try {
            return new UrlEncodedFormEntity(getParamsList(), this.contentEncoding);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setHttpEntityIsRepeatable(boolean isRepeatable) {
        this.isRepeatable = isRepeatable;
    }
}
