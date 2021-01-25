package com.android.server.backup.encryption.chunk;

public final class ChunksMetadataProto {
    public static final int AES_256_GCM = 1;
    public static final int CHUNK_ORDERING_TYPE_UNSPECIFIED = 0;
    public static final int EXPLICIT_STARTS = 1;
    public static final int INLINE_LENGTHS = 2;
    public static final int SHA_256 = 1;
    public static final int UNKNOWN_CHECKSUM_TYPE = 0;
    public static final int UNKNOWN_CIPHER_TYPE = 0;

    public final class Chunk {
        public static final long HASH = 1151051235329L;
        public static final long LENGTH = 1120986464258L;

        public Chunk() {
        }
    }

    public final class ChunkListing {
        public static final long CHUNKS = 2246267895809L;
        public static final long CHUNK_ORDERING_TYPE = 1159641169925L;
        public static final long CIPHER_TYPE = 1159641169922L;
        public static final long DOCUMENT_ID = 1138166333443L;
        public static final long FINGERPRINT_MIXER_SALT = 1151051235332L;

        public ChunkListing() {
        }
    }

    public final class ChunkOrdering {
        public static final long CHECKSUM = 1151051235330L;
        public static final long STARTS = 5519032975361L;

        public ChunkOrdering() {
        }
    }

    public final class ChunksMetadata {
        public static final long CHECKSUM_TYPE = 1159641169923L;
        public static final long CHUNK_ORDERING = 1151051235330L;
        public static final long CHUNK_ORDERING_TYPE = 1159641169925L;
        public static final long CIPHER_TYPE = 1159641169921L;

        public ChunksMetadata() {
        }
    }
}
