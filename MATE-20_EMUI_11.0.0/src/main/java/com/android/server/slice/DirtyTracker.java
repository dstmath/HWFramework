package com.android.server.slice;

import java.io.IOException;
import org.xmlpull.v1.XmlSerializer;

public interface DirtyTracker {

    public interface Persistable {
        String getFileName();

        void writeTo(XmlSerializer xmlSerializer) throws IOException;
    }

    void onPersistableDirty(Persistable persistable);
}
