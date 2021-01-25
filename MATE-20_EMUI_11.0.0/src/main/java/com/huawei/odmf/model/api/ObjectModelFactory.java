package com.huawei.odmf.model.api;

import android.content.Context;
import com.huawei.odmf.model.AObjectModel;
import java.io.File;

public final class ObjectModelFactory {
    private ObjectModelFactory() {
    }

    public static ObjectModel parse(String str, String str2) {
        return AObjectModel.parse(str, str2);
    }

    public static ObjectModel parse(File file) {
        return AObjectModel.parse(file);
    }

    public static ObjectModel parse(Context context, String str) {
        return AObjectModel.parse(context, str);
    }
}
