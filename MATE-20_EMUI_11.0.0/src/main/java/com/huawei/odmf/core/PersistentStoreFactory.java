package com.huawei.odmf.core;

import android.content.Context;
import android.net.Uri;
import java.util.Map;

public final class PersistentStoreFactory {
    private PersistentStoreFactory() {
    }

    public static int createPersistentStore(Uri uri, Configuration configuration, Context context, String str) {
        return PersistentStoreCoordinator.getDefault().createPersistentStore(uri, configuration, context, str);
    }

    /*  JADX ERROR: StackOverflowError in pass: MarkFinallyVisitor
        java.lang.StackOverflowError
        	at jadx.core.dex.instructions.IfNode.isSame(IfNode.java:122)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.sameInsns(MarkFinallyVisitor.java:451)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.compareBlocks(MarkFinallyVisitor.java:436)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.checkBlocksTree(MarkFinallyVisitor.java:408)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.checkBlocksTree(MarkFinallyVisitor.java:411)
        */
    public static int createEncryptedPersistentStore(android.net.Uri r7, com.huawei.odmf.core.Configuration r8, android.content.Context r9, java.lang.String r10, byte[] r11) {
        /*
            if (r11 == 0) goto L_0x0028
            int r0 = r11.length
            if (r0 == 0) goto L_0x0028
            r0 = 0
            com.huawei.odmf.core.PersistentStoreCoordinator r1 = com.huawei.odmf.core.PersistentStoreCoordinator.getDefault()     // Catch:{ all -> 0x001d }
            r2 = r7
            r3 = r8
            r4 = r9
            r5 = r10
            r6 = r11
            int r7 = r1.createEncryptedPersistentStore(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x001d }
            r8 = r0
        L_0x0014:
            int r9 = r11.length
            if (r8 >= r9) goto L_0x001c
            r11[r8] = r0
            int r8 = r8 + 1
            goto L_0x0014
        L_0x001c:
            return r7
        L_0x001d:
            r7 = move-exception
            r8 = r0
        L_0x001f:
            int r9 = r11.length
            if (r8 >= r9) goto L_0x0027
            r11[r8] = r0
            int r8 = r8 + 1
            goto L_0x001f
        L_0x0027:
            throw r7
        L_0x0028:
            com.huawei.odmf.exception.ODMFIllegalArgumentException r7 = new com.huawei.odmf.exception.ODMFIllegalArgumentException
            java.lang.String r8 = "encrypted database must be set a not null key"
            r7.<init>(r8)
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.odmf.core.PersistentStoreFactory.createEncryptedPersistentStore(android.net.Uri, com.huawei.odmf.core.Configuration, android.content.Context, java.lang.String, byte[]):int");
    }

    public static int createCrossPersistentStore(Uri uri, Configuration configuration, Context context, Map<Uri, byte[]> map) {
        return PersistentStoreCoordinator.getDefault().createCrossPersistentStore(uri, configuration, context, map);
    }

    public static PersistentStore getPersistentStore(Uri uri) {
        return PersistentStoreCoordinator.getDefault().getPersistentStore(uri);
    }
}
