package com.google.android.textclassifier;

public final class RemoteActionTemplate {
    public final String action;
    public final String[] category;
    public final String data;
    public final String description;
    public final String descriptionWithAppName;
    public final NamedVariant[] extras;
    public final Integer flags;
    public final String packageName;
    public final Integer requestCode;
    public final String titleWithEntity;
    public final String titleWithoutEntity;
    public final String type;

    public RemoteActionTemplate(String titleWithoutEntity2, String titleWithEntity2, String description2, String descriptionWithAppName2, String action2, String data2, String type2, Integer flags2, String[] category2, String packageName2, NamedVariant[] extras2, Integer requestCode2) {
        this.titleWithoutEntity = titleWithoutEntity2;
        this.titleWithEntity = titleWithEntity2;
        this.description = description2;
        this.descriptionWithAppName = descriptionWithAppName2;
        this.action = action2;
        this.data = data2;
        this.type = type2;
        this.flags = flags2;
        this.category = category2;
        this.packageName = packageName2;
        this.extras = extras2;
        this.requestCode = requestCode2;
    }
}
