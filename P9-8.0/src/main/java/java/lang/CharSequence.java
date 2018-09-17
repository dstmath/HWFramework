package java.lang;

import java.lang.-$Lambda$S9HjrJh0nDg7IyU6wZdPArnZWRQ.AnonymousClass1;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator.OfInt;
import java.util.Spliterators;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public interface CharSequence {
    char charAt(int i);

    int length();

    CharSequence subSequence(int i, int i2);

    String toString();

    IntStream chars() {
        return StreamSupport.intStream(new -$Lambda$S9HjrJh0nDg7IyU6wZdPArnZWRQ(this), 16464, false);
    }

    /* synthetic */ OfInt lambda$-java_lang_CharSequence_6032() {
        return Spliterators.spliterator(new PrimitiveIterator.OfInt() {
            int cur = 0;

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
        return StreamSupport.intStream(new AnonymousClass1(this), 16, false);
    }

    /* synthetic */ OfInt lambda$-java_lang_CharSequence_8746() {
        return Spliterators.spliteratorUnknownSize(new PrimitiveIterator.OfInt() {
            int cur = 0;

            public void forEachRemaining(IntConsumer block) {
                int i;
                Throwable th;
                int length = CharSequence.this.length();
                int i2 = this.cur;
                while (i2 < length) {
                    try {
                        i = i2 + 1;
                        try {
                            char c1 = CharSequence.this.charAt(i2);
                            if (!Character.isHighSurrogate(c1) || i >= length) {
                                block.accept(c1);
                            } else {
                                char c2 = CharSequence.this.charAt(i);
                                if (Character.isLowSurrogate(c2)) {
                                    i++;
                                    block.accept(Character.toCodePoint(c1, c2));
                                } else {
                                    block.accept(c1);
                                }
                            }
                            i2 = i;
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        i = i2;
                    }
                }
                this.cur = i2;
                return;
                this.cur = i;
                throw th;
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
