package com.huawei.odmf.core;

import java.io.IOException;

public interface ODMFMigration {
    void migrate(ASchema aSchema, String str, String str2) throws IOException;
}
