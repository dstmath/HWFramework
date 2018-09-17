package android.animation;

public abstract class BidirectionalTypeConverter<T, V> extends TypeConverter<T, V> {
    private BidirectionalTypeConverter mInvertedConverter;

    private static class InvertedConverter<From, To> extends BidirectionalTypeConverter<From, To> {
        private BidirectionalTypeConverter<To, From> mConverter;

        public InvertedConverter(BidirectionalTypeConverter<To, From> converter) {
            super(converter.getTargetType(), converter.getSourceType());
            this.mConverter = converter;
        }

        public From convertBack(To value) {
            return this.mConverter.convert(value);
        }

        public To convert(From value) {
            return this.mConverter.convertBack(value);
        }
    }

    public abstract T convertBack(V v);

    public BidirectionalTypeConverter(Class<T> fromClass, Class<V> toClass) {
        super(fromClass, toClass);
    }

    public BidirectionalTypeConverter<V, T> invert() {
        if (this.mInvertedConverter == null) {
            this.mInvertedConverter = new InvertedConverter(this);
        }
        return this.mInvertedConverter;
    }
}
