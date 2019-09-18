package com.huawei.odmf.model.api;

import android.content.Context;
import com.huawei.odmf.model.AObjectModel;
import java.io.File;
import java.io.IOException;

public class ObjectModelFactory {
    public static ObjectModel parse(String fileDir, String fileName) {
        return AObjectModel.parse(fileDir, fileName);
    }

    public static ObjectModel parse(File file) throws IOException {
        return AObjectModel.parse(file);
    }

    public static ObjectModel parse(Context context, String assetsFileName) {
        return AObjectModel.parse(context, assetsFileName);
    }
}
