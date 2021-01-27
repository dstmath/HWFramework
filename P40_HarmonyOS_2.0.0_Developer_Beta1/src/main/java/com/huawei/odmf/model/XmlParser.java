package com.huawei.odmf.model;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFXmlParserException;
import com.huawei.odmf.model.api.ObjectModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public final class XmlParser {
    private XmlParser() {
    }

    public static ObjectModel parseToModel(String str, String str2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            throw new ODMFIllegalArgumentException("parameter fileDir or fileName is null");
        }
        try {
            ObjectModel model = new XmlParserHelper(new FileInputStream(new File(str, str2)), str2).getModel();
            if (model != null) {
                return model;
            }
            throw new ODMFXmlParserException("xml parser exception");
        } catch (FileNotFoundException unused) {
            throw new ODMFIllegalArgumentException("The xml file not found");
        }
    }

    public static ObjectModel parseToModel(File file) {
        if (file != null) {
            try {
                ObjectModel model = new XmlParserHelper(new FileInputStream(file), file.getName()).getModel();
                if (model != null) {
                    return model;
                }
                throw new ODMFXmlParserException("xml parser exception");
            } catch (FileNotFoundException unused) {
                throw new ODMFIllegalArgumentException("The xml file not found");
            }
        } else {
            throw new ODMFIllegalArgumentException("parameter file error");
        }
    }

    public static ObjectModel parseToModel(Context context, String str) {
        if (TextUtils.isEmpty(str) || context == null) {
            throw new ODMFIllegalArgumentException("parameter assetsFileName error");
        }
        ObjectModel model = new XmlParserHelper(context, str).getModel();
        if (model != null) {
            return model;
        }
        throw new ODMFXmlParserException("xml parser exception");
    }
}
