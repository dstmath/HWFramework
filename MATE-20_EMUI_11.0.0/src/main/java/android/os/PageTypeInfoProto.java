package android.os;

public final class PageTypeInfoProto {
    public static final long BLOCKS = 2246267895812L;
    public static final long MIGRATE_TYPES = 2246267895811L;
    public static final long PAGES_PER_BLOCK = 1120986464258L;
    public static final long PAGE_BLOCK_ORDER = 1120986464257L;

    public final class MigrateType {
        public static final long FREE_PAGES_COUNT = 2220498092036L;
        public static final long NODE = 1120986464257L;
        public static final long TYPE = 1138166333443L;
        public static final long ZONE = 1138166333442L;

        public MigrateType() {
        }
    }

    public final class Block {
        public static final long CMA = 1120986464262L;
        public static final long HIGHATOMIC = 1120986464265L;
        public static final long ISOLATE = 1120986464264L;
        public static final long MOVABLE = 1120986464261L;
        public static final long NODE = 1120986464257L;
        public static final long RECLAIMABLE = 1120986464260L;
        public static final long RESERVE = 1120986464263L;
        public static final long UNMOVABLE = 1120986464259L;
        public static final long ZONE = 1138166333442L;

        public Block() {
        }
    }
}
