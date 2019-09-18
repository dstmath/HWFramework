package java.lang;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterators;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public interface CharSequence {
    char charAt(int i);

    int length();

    CharSequence subSequence(int i, int i2);

    String toString();

    IntStream chars() {
        return StreamSupport.intStream(new Supplier() {
            public final Object get() {
                return Spliterators.spliterator((PrimitiveIterator.OfInt) new PrimitiveIterator.OfInt() {
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
                }, (long) CharSequence.this.length(), 16);
            }
        }, 16464, false);
    }

    IntStream codePoints() {
        return StreamSupport.intStream(new Supplier() {
            public final Object get() {
                return Spliterators.spliteratorUnknownSize((PrimitiveIterator.OfInt) new PrimitiveIterator.OfInt() {
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
                                    if (Character.isHighSurrogate(c1)) {
                                        if (i < length) {
                                            char c2 = CharSequence.this.charAt(i);
                                            if (Character.isLowSurrogate(c2)) {
                                                i++;
                                                block.accept(Character.toCodePoint(c1, c2));
                                            } else {
                                                block.accept(c1);
                                            }
                                            i2 = i;
                                        }
                                    }
                                    block.accept(c1);
                                    i2 = i;
                                } catch (Throwable th2) {
                                    th = th2;
                                    this.cur = i;
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                i = i2;
                                th = th3;
                                this.cur = i;
                                throw th;
                            }
                        }
                        this.cur = i2;
                    }

                    public boolean hasNext() {
                        return this.cur < CharSequence.this.length();
                    }

                    public int nextInt() {
                        int length = CharSequence.this.length();
                        if (this.cur < length) {
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
                        throw new NoSuchElementException();
                    }
                }, 16);
            }
        }, 16, false);
    }
}
