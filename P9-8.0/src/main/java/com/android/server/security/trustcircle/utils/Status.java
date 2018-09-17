package com.android.server.security.trustcircle.utils;

public class Status {

    public enum ExceptionStep {
        NO_EXCEPTION,
        REG,
        LOGIN,
        UPDATE,
        LOGOUT,
        UNREG,
        SWITCH_USER
    }

    public enum LifeCycleStauts {
        NOT_REGISTER,
        REGISTERED,
        LOGINED,
        LOGOUTED,
        UNKNOWN
    }

    public enum TCIS_Result {
        SUCCESS(0),
        UNKNOWN(2046820353),
        BAD_ACCESS(2046820354),
        BAD_PARAM(2046820355),
        UNKNOWN_CMD(2046820356),
        BUF_TOO_SHORT(2046820357),
        OUT_OF_MEM(2046820358),
        TIMEOUT(2046820359),
        HASH(2046820360),
        SIGN(2046820361),
        VERIF(2046820362),
        KEY_GEN(2046820363),
        READ(2046820364),
        WRITE(2046820365),
        ERASE(2046820366),
        NOT_MATCH(2046820367),
        GEN_RESPONSE(2046820368),
        GET_DEVICEID(2046820369),
        GET_LAST_IDENTIFIED_RESULT(2046820370),
        AUTHENTICATOR_SIGN(2046820371),
        GET_ID_LIST(2046820372),
        GET_AUTHENTICATOR_VERSION(2046820373),
        UN_INITIALIZED(2046820374),
        NO_OPTIONAL_LEVEL(2046820375),
        FILE_RELATE_ABNORMAL(2046820376),
        INVALID_VERSION(2046820377),
        PARCEL(2046820384),
        CROSS_USER(2046820385),
        NOT_REG(2046820386),
        NOT_LOGIN(2046820387);
        
        private int value;

        private TCIS_Result(int value) {
            this.value = -1;
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

    public enum UpdateStauts {
        NO_NEED_UPDATE,
        NEED_UPDATE,
        ERROR
    }
}
