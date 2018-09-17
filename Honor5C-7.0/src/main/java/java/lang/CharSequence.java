package java.lang;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator.OfInt;
import java.util.Spliterators;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public interface CharSequence {

    final /* synthetic */ class -java_util_stream_IntStream_chars__LambdaImpl0 implements Supplier {
        private /* synthetic */ CharSequence val$this;

        public /* synthetic */ -java_util_stream_IntStream_chars__LambdaImpl0(CharSequence charSequence) {
            this.val$this = charSequence;
        }

        public Object get() {
            return this.val$this.-java_lang_CharSequence_lambda$1();
        }
    }

    final /* synthetic */ class -java_util_stream_IntStream_codePoints__LambdaImpl0 implements Supplier {
        private /* synthetic */ CharSequence val$this;

        public /* synthetic */ -java_util_stream_IntStream_codePoints__LambdaImpl0(CharSequence charSequence) {
            this.val$this = charSequence;
        }

        public Object get() {
            return this.val$this.-java_lang_CharSequence_lambda$2();
        }
    }

    char charAt(int i);

    int length();

    CharSequence subSequence(int i, int i2);

    String toString();

    IntStream chars() {
        return StreamSupport.intStream(new -java_util_stream_IntStream_chars__LambdaImpl0(), 16464, false);
    }

    /* synthetic */ OfInt -java_lang_CharSequence_lambda$1() {
        return Spliterators.spliterator(new PrimitiveIterator.OfInt() {
            int cur;

            public /* bridge */ /* synthetic */ Object next() {
                return next();
            }

            {
                this.cur = 0;
            }

            public boolean hasNext() {
                return this.cur < CharSequence.this.length();
            }

            public int nextInt() {
                if (hasNext()) {
                    CharSequence charSequence = CharSequence.this;
                    int i = this.cur;
                    this.cur = i + 1;
                    return charSequence.charAt(i);
                }
                throw new NoSuchElementException();
            }

            public void forEachRemaining(IntConsumer block) {
                while (this.cur < CharSequence.this.length()) {
                    block.accept(CharSequence.this.charAt(this.cur));
                    this.cur++;
                }
            }
        }, (long) length(), 16);
    }

    IntStream codePoints() {
        return StreamSupport.intStream(new -java_util_stream_IntStream_codePoints__LambdaImpl0(), 16, false);
    }

    /* synthetic */ OfInt -java_lang_CharSequence_lambda$2() {
        return Spliterators.spliteratorUnknownSize(new PrimitiveIterator.OfInt() {
            int cur;

            public /* bridge */ /* synthetic */ Object next() {
                return next();
            }

            {
                this.cur = 0;
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void forEachRemaining(IntConsumer block) {
                Throwable th;
                int length = CharSequence.this.length();
                int i = this.cur;
                while (i < length) {
                    int i2;
                    try {
                        i2 = i + 1;
                        char c1 = CharSequence.this.charAt(i);
                        if (!Character.isHighSurrogate(c1) || i2 >= length) {
                            block.accept(c1);
                        } else {
                            char c2 = CharSequence.this.charAt(i2);
                            if (Character.isLowSurrogate(c2)) {
                                i2++;
                                block.accept(Character.toCodePoint(c1, c2));
                            } else {
                                try {
                                    block.accept(c1);
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                        }
                        i = i2;
                    } catch (Throwable th3) {
                        th = th3;
                        i2 = i;
                    }
                }
                this.cur = i;
            }

            public boolean hasNext() {
                return this.cur < CharSequence.this.length();
            }

            public int nextInt() {
                int length = CharSequence.this.length();
                if (this.cur >= length) {
                    throw new NoSuchElementException();
                }
                CharSequence charSequence = CharSequence.this;
                int i = this.cur;
                this.cur = i + 1;
                char c1 = charSequence.charAt(i);
                if (Character.isHighSurrogate(c1) && this.cur < length) {
                    char c2 = CharSequence.this.charAt(this.cur);
                    if (Character.isLowSurrogate(c2)) {
                        this.cur++;
                        return Character.toCodePoint(c1, c2);
                    }
                }
                return c1;
            }
        }, 16);
    }
}
