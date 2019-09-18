package com.android.server.locksettings.recoverablekeystore.storage;

import android.util.SparseArray;
import com.android.server.locksettings.recoverablekeystore.storage.RecoverySessionStorage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;
import javax.security.auth.Destroyable;

public class RecoverySessionStorage implements Destroyable {
    private final SparseArray<ArrayList<Entry>> mSessionsByUid = new SparseArray<>();

    public static class Entry implements Destroyable {
        private final byte[] mKeyClaimant;
        private final byte[] mLskfHash;
        /* access modifiers changed from: private */
        public final String mSessionId;
        private final byte[] mVaultParams;

        public Entry(String sessionId, byte[] lskfHash, byte[] keyClaimant, byte[] vaultParams) {
            this.mLskfHash = lskfHash;
            this.mSessionId = sessionId;
            this.mKeyClaimant = keyClaimant;
            this.mVaultParams = vaultParams;
        }

        public byte[] getLskfHash() {
            return this.mLskfHash;
        }

        public byte[] getKeyClaimant() {
            return this.mKeyClaimant;
        }

        public byte[] getVaultParams() {
            return this.mVaultParams;
        }

        public void destroy() {
            Arrays.fill(this.mLskfHash, (byte) 0);
            Arrays.fill(this.mKeyClaimant, (byte) 0);
        }
    }

    public Entry get(int uid, String sessionId) {
        ArrayList<Entry> userEntries = this.mSessionsByUid.get(uid);
        if (userEntries == null) {
            return null;
        }
        Iterator<Entry> it = userEntries.iterator();
        while (it.hasNext()) {
            Entry entry = it.next();
            if (sessionId.equals(entry.mSessionId)) {
                return entry;
            }
        }
        return null;
    }

    public void add(int uid, Entry entry) {
        if (this.mSessionsByUid.get(uid) == null) {
            this.mSessionsByUid.put(uid, new ArrayList());
        }
        this.mSessionsByUid.get(uid).add(entry);
    }

    public void remove(int uid, String sessionId) {
        if (this.mSessionsByUid.get(uid) != null) {
            this.mSessionsByUid.get(uid).removeIf(new Predicate(sessionId) {
                private final /* synthetic */ String f$0;

                {
                    this.f$0 = r1;
                }

                public final boolean test(Object obj) {
                    return ((RecoverySessionStorage.Entry) obj).mSessionId.equals(this.f$0);
                }
            });
        }
    }

    public void remove(int uid) {
        ArrayList<Entry> entries = this.mSessionsByUid.get(uid);
        if (entries != null) {
            Iterator<Entry> it = entries.iterator();
            while (it.hasNext()) {
                it.next().destroy();
            }
            this.mSessionsByUid.remove(uid);
        }
    }

    public int size() {
        int size = 0;
        int numberOfUsers = this.mSessionsByUid.size();
        for (int i = 0; i < numberOfUsers; i++) {
            size += this.mSessionsByUid.valueAt(i).size();
        }
        return size;
    }

    public void destroy() {
        int numberOfUids = this.mSessionsByUid.size();
        for (int i = 0; i < numberOfUids; i++) {
            Iterator<Entry> it = this.mSessionsByUid.valueAt(i).iterator();
            while (it.hasNext()) {
                it.next().destroy();
            }
        }
    }
}
