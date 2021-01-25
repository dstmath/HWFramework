package com.huawei.coauth.auth.authmsg;

import com.huawei.coauth.msg.Modules;

public enum CoAuthOperationType {
    HEADER(0),
    CREATE_CO_AUTH_PAIR_GROUP(1),
    DESTROY_CO_AUTH_PAIR_GROUP(2),
    CO_AUTH(3),
    CANCEL_CO_AUTH(4),
    CO_AUTH_CONTEXT(5),
    CO_AUTH_PAIR_GROUP_HEADER(6),
    CO_AUTH_QUERY_METHOD(9),
    GET_PROPERTY(10),
    SET_PROPERTY(11),
    INIT_CO_AUTH_IDM_GROUP_REQUEST(12),
    CREATE_CO_AUTH_PAIR_GROUP_RESPONSE(1001),
    DESTROY_CO_AUTH_PAIR_GROUP_RESPONSE(Modules.DEFAULT_OTHER),
    CO_AUTH_RESPONSE(1003),
    CANCEL_CO_AUTH_RESPONSE(1004),
    CO_AUTH_START_RESPONSE(1005),
    CO_AUTH_QUERY_METHOD_RESPONSE(1006),
    CO_AUTH_GET_PROPERTY_RESPONSE(1007),
    CO_AUTH_SET_PROPERTY_RESPONSE(1008),
    INIT_CO_AUTH_IDM_GROUP_RESPONSE(1009);
    
    private final int value;

    private CoAuthOperationType(int value2) {
        this.value = value2;
    }

    public int getValue() {
        return this.value;
    }
}
