package com.huawei.nb.searchmanager.client.schema;

import java.util.HashMap;
import java.util.Map;

public enum IndexSchemaType {
    CUSTOM(0, "CUSTOM"),
    COMMON(1, "COMMON"),
    APP(2, "APP"),
    DOCUMENT(3, "DOCUMENT"),
    PHOTO(4, "PHOTO"),
    VIDEO(5, "VIDEO"),
    MUSIC(6, "MUSIC"),
    CONTACT(7, "CONTACT"),
    MESSAGE(8, "MESSAGE"),
    EMAIL(9, "EMAIL"),
    NOTEPAD(10, "NOTEPAD"),
    NOTICE(11, "NOTICE"),
    EVENT(12, "EVENT"),
    PLACE(13, "PLACE"),
    ACTION(14, "ACTION");
    
    private static final Map<Integer, IndexSchemaType> ENUMS = new HashMap();
    private int schemaCode;
    private String schemaType;

    static {
        IndexSchemaType[] values = values();
        for (IndexSchemaType indexSchemaType : values) {
            ENUMS.put(Integer.valueOf(indexSchemaType.schemaCode), indexSchemaType);
        }
    }

    private IndexSchemaType(int i, String str) {
        this.schemaCode = i;
        this.schemaType = str;
    }

    public int getSchemaCode() {
        return this.schemaCode;
    }

    public String getSchemaType() {
        return this.schemaType;
    }

    public static IndexSchemaType getSchemaType(int i) {
        IndexSchemaType indexSchemaType = ENUMS.get(Integer.valueOf(i));
        return indexSchemaType == null ? CUSTOM : indexSchemaType;
    }
}
