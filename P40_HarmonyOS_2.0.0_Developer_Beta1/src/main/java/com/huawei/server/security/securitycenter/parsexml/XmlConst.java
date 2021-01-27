package com.huawei.server.security.securitycenter.parsexml;

import android.util.Log;

class XmlConst {
    static final String SECURITYCENTER_CONFIG_FILENAME = "securitycenter_config.xml";
    static final String TAG_CONFIG = "config";
    static final String TAG_FEATURE = "feature";
    static final String TAG_ITEM = "item";
    static final String TAG_KEY = "key";
    static final String TAG_METHOD = "method";
    static final String TAG_MODULE = "module";
    static final String TAG_NAME = "name";
    static final String TAG_PACKAGE = "package";
    static final String TAG_VALUE = "value";

    private XmlConst() {
        Log.d("XmlConst", "constructor");
    }
}
