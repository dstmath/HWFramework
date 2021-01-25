package com.android.server.backup.encryption.chunk;

import android.util.proto.ProtoInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChunkListingMap {
    private final Map<ChunkHash, Entry> mChunksByHash;

    public static ChunkListingMap readFromProto(ProtoInputStream inputStream) throws IOException {
        Map<ChunkHash, Entry> entries = new HashMap<>();
        long start = 0;
        while (inputStream.nextField() != -1) {
            if (inputStream.getFieldNumber() == 1) {
                long chunkToken = inputStream.start(2246267895809L);
                Chunk chunk = Chunk.readFromProto(inputStream);
                entries.put(new ChunkHash(chunk.getHash()), new Entry(start, chunk.getLength()));
                start += (long) chunk.getLength();
                inputStream.end(chunkToken);
            }
        }
        return new ChunkListingMap(entries);
    }

    private ChunkListingMap(Map<ChunkHash, Entry> chunksByHash) {
        this.mChunksByHash = Collections.unmodifiableMap(new HashMap(chunksByHash));
    }

    public boolean hasChunk(ChunkHash hash) {
        return this.mChunksByHash.containsKey(hash);
    }

    public Entry getChunkEntry(ChunkHash hash) {
        return this.mChunksByHash.get(hash);
    }

    public int getChunkCount() {
        return this.mChunksByHash.size();
    }

    public static final class Entry {
        private final int mLength;
        private final long mStart;

        private Entry(long start, int length) {
            this.mStart = start;
            this.mLength = length;
        }

        public int getLength() {
            return this.mLength;
        }

        public long getStart() {
            return this.mStart;
        }
    }
}
