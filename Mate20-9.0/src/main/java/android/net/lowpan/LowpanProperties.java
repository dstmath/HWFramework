package android.net.lowpan;

public final class LowpanProperties {
    public static final LowpanProperty<int[]> KEY_CHANNEL_MASK = new LowpanStandardProperty(ILowpanInterface.KEY_CHANNEL_MASK, int[].class);
    public static final LowpanProperty<Integer> KEY_MAX_TX_POWER = new LowpanStandardProperty(ILowpanInterface.KEY_MAX_TX_POWER, Integer.class);

    static final class LowpanStandardProperty<T> extends LowpanProperty<T> {
        private final String mName;
        private final Class<T> mType;

        LowpanStandardProperty(String name, Class<T> type) {
            this.mName = name;
            this.mType = type;
        }

        public String getName() {
            return this.mName;
        }

        public Class<T> getType() {
            return this.mType;
        }

        public String toString() {
            return getName();
        }
    }

    private LowpanProperties() {
    }
}
