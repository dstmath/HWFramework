package com.huawei.android.provider;

import android.provider.BaseColumns;

public final class VoicemailContractEx {
    public static final String SOURCE_PACKAGE_FIELD = "source_package";

    public static final class Voicemails implements BaseColumns {
        public static final String STATE = "state";
        public static final String _DATA = "_data";
    }
}
