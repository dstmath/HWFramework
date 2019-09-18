package com.android.i18n.phonenumbers;

import java.io.InputStream;

public interface MetadataLoader {
    InputStream loadMetadata(String str);
}
