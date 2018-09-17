package java.util.stream;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public interface Sink<T> extends Consumer<T> {

    public interface OfDouble extends Sink<Double>, DoubleConsumer {
        void accept(double d);

        void accept(Double i) {
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Sink.OfDouble.accept(Double)");
            }
            accept(i.lambda$-java_util_stream_DoublePipeline_13468());
        }
    }

    public interface OfInt extends Sink<Integer>, IntConsumer {
        void accept(int i);

        void accept(Integer i) {
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Sink.OfInt.accept(Integer)");
            }
            accept(i.lambda$-java_util_stream_IntPipeline_14709());
        }
    }

    public interface OfLong extends Sink<Long>, LongConsumer {
        void accept(long j);

        void accept(Long i) {
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Sink.OfLong.accept(Long)");
            }
            accept(i.lambda$-java_util_stream_LongPipeline_13909());
        }
    }

    public static abstract class ChainedReference<T, E_OUT> implements Sink<T> {
        protected final Sink<? super E_OUT> downstream;

        public ChainedReference(Sink<? super E_OUT> downstream) {
            this.downstream = (Sink) Objects.requireNonNull(downstream);
        }

        public void begin(long size) {
            this.downstream.begin(size);
        }

        public void end() {
            this.downstream.end();
        }

        public boolean cancellationRequested() {
            return this.downstream.cancellationRequested();
        }
    }

    public static abstract class ChainedDouble<E_OUT> implements OfDouble {
        protected final Sink<? super E_OUT> downstream;

        public ChainedDouble(Sink<? super E_OUT> downstream) {
            this.downstream = (Sink) Objects.requireNonNull(downstream);
        }

        public void begin(long size) {
            this.downstream.begin(size);
        }

        public void end() {
            this.downstream.end();
        }

        public boolean cancellationRequested() {
            return this.downstream.cancellationRequested();
        }
    }

    public static abstract class ChainedInt<E_OUT> implements OfInt {
        protected final Sink<? super E_OUT> downstream;

        public ChainedInt(Sink<? super E_OUT> downstream) {
            this.downstream = (Sink) Objects.requireNonNull(downstream);
        }

        public void begin(long size) {
            this.downstream.begin(size);
        }

        public void end() {
            this.downstream.end();
        }

        public boolean cancellationRequested() {
            return this.downstream.cancellationRequested();
        }
    }

    public static abstract class ChainedLong<E_OUT> implements OfLong {
        protected final Sink<? super E_OUT> downstream;

        public ChainedLong(Sink<? super E_OUT> downstream) {
            this.downstream = (Sink) Objects.requireNonNull(downstream);
        }

        public void begin(long size) {
            this.downstream.begin(size);
        }

        public void end() {
            this.downstream.end();
        }

        public boolean cancellationRequested() {
            return this.downstream.cancellationRequested();
        }
    }

    void begin(long size) {
    }

    void end() {
    }

    boolean cancellationRequested() {
        return false;
    }

    /* renamed from: accept */
    void -java_util_stream_IntPipeline-mthref-0(int value) {
        throw new IllegalStateException("called wrong accept method");
    }

    /* renamed from: accept */
    void -java_util_stream_LongPipeline-mthref-0(long value) {
        throw new IllegalStateException("called wrong accept method");
    }

    /* renamed from: accept */
    void -java_util_stream_ReferencePipeline$9$1-mthref-0(double value) {
        throw new IllegalStateException("called wrong accept method");
    }
}
