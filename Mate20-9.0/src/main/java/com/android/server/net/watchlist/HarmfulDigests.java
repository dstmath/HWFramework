package com.android.server.net.watchlist;

import com.android.internal.util.HexDump;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class HarmfulDigests {
    private final Set<String> mDigestSet;

    HarmfulDigests(List<byte[]> digests) {
        HashSet<String> tmpDigestSet = new HashSet<>();
        int size = digests.size();
        for (int i = 0; i < size; i++) {
            tmpDigestSet.add(HexDump.toHexString(digests.get(i)));
        }
        this.mDigestSet = Collections.unmodifiableSet(tmpDigestSet);
    }

    public boolean contains(byte[] digest) {
        return this.mDigestSet.contains(HexDump.toHexString(digest));
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        for (String digest : this.mDigestSet) {
            pw.println(digest);
        }
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
    }
}
