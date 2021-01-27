package ohos.utils.zson;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import ohos.utils.fastjson.JSONReader;
import ohos.utils.fastjson.parser.Feature;
import ohos.utils.zson.ZSONTools;

public class ZSONReader implements AutoCloseable {
    private final JSONReader reader;
    private final Deque<State> stateStack = new ArrayDeque();

    /* access modifiers changed from: private */
    public enum State {
        OBJECT,
        ARRAY,
        NAME
    }

    public ZSONReader(Reader reader2) {
        this.reader = (JSONReader) ZSONTools.callFastJson(new ZSONTools.Caller(reader2) {
            /* class ohos.utils.zson.$$Lambda$ZSONReader$1oEtyoQidr5Ti0jdyRcl8f_PzE */
            private final /* synthetic */ Reader f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.utils.zson.ZSONTools.Caller
            public final Object call() {
                return ZSONReader.lambda$new$0(this.f$0);
            }
        });
    }

    static /* synthetic */ JSONReader lambda$new$0(Reader reader2) {
        return new JSONReader(reader2);
    }

    public void startArray() {
        JSONReader jSONReader = this.reader;
        Objects.requireNonNull(jSONReader);
        ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
            /* class ohos.utils.zson.$$Lambda$ns9ehMoSjg7hvgtFwQrcnU5uCNQ */

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                JSONReader.this.startArray();
            }
        });
        this.stateStack.push(State.ARRAY);
    }

    public void startObject() {
        JSONReader jSONReader = this.reader;
        Objects.requireNonNull(jSONReader);
        ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
            /* class ohos.utils.zson.$$Lambda$LFWiA0ZYb6lyoH04y_ujbRksMc */

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                JSONReader.this.startObject();
            }
        });
        this.stateStack.push(State.OBJECT);
        this.stateStack.push(State.NAME);
    }

    public boolean hasNext() {
        JSONReader jSONReader = this.reader;
        Objects.requireNonNull(jSONReader);
        return ((Boolean) ZSONTools.callFastJson(new ZSONTools.Caller() {
            /* class ohos.utils.zson.$$Lambda$oyoy2U0gAmH9OQ0LRyb46RDVOs4 */

            @Override // ohos.utils.zson.ZSONTools.Caller
            public final Object call() {
                return Boolean.valueOf(JSONReader.this.hasNext());
            }
        })).booleanValue();
    }

    public String readName() {
        if (this.stateStack.peek() == State.NAME) {
            JSONReader jSONReader = this.reader;
            Objects.requireNonNull(jSONReader);
            String str = (String) ZSONTools.callFastJson(new ZSONTools.Caller() {
                /* class ohos.utils.zson.$$Lambda$AlSnKlCprfnvAhEONbfb3fDpKf4 */

                @Override // ohos.utils.zson.ZSONTools.Caller
                public final Object call() {
                    return JSONReader.this.readString();
                }
            });
            this.stateStack.pop();
            return str;
        }
        throw new ZSONException("Illegal state, not a name");
    }

    public boolean readBoolean() {
        return ((Boolean) readValue(Boolean.TYPE)).booleanValue();
    }

    public double readDouble() {
        return ((Double) readValue(Double.TYPE)).doubleValue();
    }

    public int readInt() {
        return ((Integer) readValue(Integer.TYPE)).intValue();
    }

    public float readFloat() {
        return ((Float) readValue(Float.TYPE)).floatValue();
    }

    public long readLong() {
        return ((Long) readValue(Long.TYPE)).longValue();
    }

    public Object readNull() {
        Object readValue = readValue(Object.class);
        if (readValue == null) {
            return readValue;
        }
        throw new ZSONException("Illegal state, value is non-null");
    }

    public String readString() {
        return (String) readValue(String.class);
    }

    private <T> T readValue(Class<T> cls) {
        if (!this.stateStack.isEmpty()) {
            State peek = this.stateStack.peek();
            if (peek == State.ARRAY) {
                return (T) ZSONTools.callFastJson(new ZSONTools.Caller(cls) {
                    /* class ohos.utils.zson.$$Lambda$ZSONReader$Y1Na1HTaCbzxOgc1hFYK_GEBjw */
                    private final /* synthetic */ Class f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // ohos.utils.zson.ZSONTools.Caller
                    public final Object call() {
                        return ZSONReader.this.lambda$readValue$1$ZSONReader(this.f$1);
                    }
                });
            }
            if (peek == State.OBJECT) {
                T t = (T) ZSONTools.callFastJson(new ZSONTools.Caller(cls) {
                    /* class ohos.utils.zson.$$Lambda$ZSONReader$yh93TPnl8vjJfEJA1O54xFegN0A */
                    private final /* synthetic */ Class f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // ohos.utils.zson.ZSONTools.Caller
                    public final Object call() {
                        return ZSONReader.this.lambda$readValue$2$ZSONReader(this.f$1);
                    }
                });
                if (!isObjectEnd()) {
                    this.stateStack.push(State.NAME);
                }
                return t;
            }
            throw new ZSONException("Illegal state, not a value of " + cls.getSimpleName());
        }
        throw new ZSONException("Illegal state, not a value of " + cls.getSimpleName());
    }

    public /* synthetic */ Object lambda$readValue$1$ZSONReader(Class cls) {
        return this.reader.readObject((Class<Object>) cls);
    }

    public /* synthetic */ Object lambda$readValue$2$ZSONReader(Class cls) {
        return this.reader.readObject((Class<Object>) cls);
    }

    private boolean isObjectEnd() {
        JSONReader jSONReader = this.reader;
        Objects.requireNonNull(jSONReader);
        return ((Integer) ZSONTools.callFastJson(new ZSONTools.Caller() {
            /* class ohos.utils.zson.$$Lambda$rMEsdDBw4PJmYnGnB0FU2ayPRUQ */

            @Override // ohos.utils.zson.ZSONTools.Caller
            public final Object call() {
                return Integer.valueOf(JSONReader.this.peek());
            }
        })).intValue() == 13;
    }

    public ZSONType peek() {
        if (!this.stateStack.isEmpty() && this.stateStack.peek() == State.NAME) {
            return ZSONType.NAME;
        }
        JSONReader jSONReader = this.reader;
        Objects.requireNonNull(jSONReader);
        ZSONType of = ZSONType.of(((Integer) ZSONTools.callFastJson(new ZSONTools.Caller() {
            /* class ohos.utils.zson.$$Lambda$rMEsdDBw4PJmYnGnB0FU2ayPRUQ */

            @Override // ohos.utils.zson.ZSONTools.Caller
            public final Object call() {
                return Integer.valueOf(JSONReader.this.peek());
            }
        })).intValue());
        return of == ZSONType.UNKNOWN ? ZSONType.of(ZSONTools.nextJsonToken(this.reader)) : of;
    }

    public void skipValue() {
        readValue(Object.class);
    }

    public void endArray() {
        if (this.stateStack.pop() == State.ARRAY) {
            JSONReader jSONReader = this.reader;
            Objects.requireNonNull(jSONReader);
            ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
                /* class ohos.utils.zson.$$Lambda$E2d_NpykBl00ifsXK4n6T8G9SAE */

                @Override // ohos.utils.zson.ZSONTools.VoidCaller
                public final void call() {
                    JSONReader.this.endArray();
                }
            });
            return;
        }
        throw new ZSONException("Illegal state, not the end of array");
    }

    public void endObject() {
        if (this.stateStack.pop() == State.OBJECT) {
            JSONReader jSONReader = this.reader;
            Objects.requireNonNull(jSONReader);
            ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
                /* class ohos.utils.zson.$$Lambda$GfolVJ0c1EG7B5uUDh67IfkNdI */

                @Override // ohos.utils.zson.ZSONTools.VoidCaller
                public final void call() {
                    JSONReader.this.endObject();
                }
            });
            return;
        }
        throw new ZSONException("Illegal state, not the end of object");
    }

    public <T> T read(Class<T> cls) {
        return (T) read((Class<Object>) cls, false);
    }

    public <T> T read(Class<T> cls, boolean z) {
        if (z) {
            configExtFeatures();
        }
        return (T) ZSONTools.callFastJson(new ZSONTools.Caller(cls) {
            /* class ohos.utils.zson.$$Lambda$ZSONReader$9xS8YCn_R3ePTIJATxVfiOHx7mg */
            private final /* synthetic */ Class f$1;

            {
                this.f$1 = r2;
            }

            @Override // ohos.utils.zson.ZSONTools.Caller
            public final Object call() {
                return ZSONReader.this.lambda$read$3$ZSONReader(this.f$1);
            }
        });
    }

    public /* synthetic */ Object lambda$read$3$ZSONReader(Class cls) {
        return this.reader.readObject((Class<Object>) cls);
    }

    private void configExtFeatures() {
        ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
            /* class ohos.utils.zson.$$Lambda$ZSONReader$LlJiDrwqXunEQ0hWl6dmMO4vUhQ */

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                ZSONReader.this.lambda$configExtFeatures$4$ZSONReader();
            }
        });
        ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
            /* class ohos.utils.zson.$$Lambda$ZSONReader$1RYtqwVI840HrcwvDhmar1eZv5k */

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                ZSONReader.this.lambda$configExtFeatures$5$ZSONReader();
            }
        });
        ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
            /* class ohos.utils.zson.$$Lambda$ZSONReader$4utub_wYNeE27kS_O8sYyPtwwys */

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                ZSONReader.this.lambda$configExtFeatures$6$ZSONReader();
            }
        });
        ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
            /* class ohos.utils.zson.$$Lambda$ZSONReader$zqUyJ4RRaUToh13189Nm4FJcJc */

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                ZSONReader.this.lambda$configExtFeatures$7$ZSONReader();
            }
        });
    }

    public /* synthetic */ void lambda$configExtFeatures$4$ZSONReader() throws IOException {
        this.reader.config(Feature.AllowComment, true);
    }

    public /* synthetic */ void lambda$configExtFeatures$5$ZSONReader() throws IOException {
        this.reader.config(Feature.AllowUnQuotedFieldNames, true);
    }

    public /* synthetic */ void lambda$configExtFeatures$6$ZSONReader() throws IOException {
        this.reader.config(Feature.AllowSingleQuotes, true);
    }

    public /* synthetic */ void lambda$configExtFeatures$7$ZSONReader() throws IOException {
        this.reader.config(Feature.AllowArbitraryCommas, true);
    }

    public void read(Object obj) {
        read(obj, false);
    }

    public void read(Object obj, boolean z) {
        if (z) {
            configExtFeatures();
        }
        ZSONTools.callFastJson(new ZSONTools.VoidCaller(obj) {
            /* class ohos.utils.zson.$$Lambda$ZSONReader$Wif_MJpwJqfsv3hQ1kaBwTIUWz8 */
            private final /* synthetic */ Object f$1;

            {
                this.f$1 = r2;
            }

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                ZSONReader.this.lambda$read$8$ZSONReader(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$read$8$ZSONReader(Object obj) throws IOException {
        this.reader.readObject(obj);
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        JSONReader jSONReader = this.reader;
        Objects.requireNonNull(jSONReader);
        ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
            /* class ohos.utils.zson.$$Lambda$cSUF2GrfwcL8XlZohhOtP1hwCVg */

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                JSONReader.this.close();
            }
        });
        this.stateStack.clear();
    }
}
