package org.apache.http.impl.conn.tsccm;

import java.lang.ref.Reference;

@Deprecated
public interface RefQueueHandler {
    void handleReference(Reference<?> reference);
}
