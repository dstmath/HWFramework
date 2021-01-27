package ohos.utils.zson;

import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import ohos.utils.fastjson.JSONWriter;
import ohos.utils.fastjson.serializer.SerializerFeature;
import ohos.utils.zson.ZSONTools;

public class ZSONWriter implements AutoCloseable, Flushable {
    private final JSONWriter writer;

    public ZSONWriter(Writer writer2) {
        this(writer2, false);
    }

    public ZSONWriter(Writer writer2, boolean z) {
        this.writer = (JSONWriter) ZSONTools.callFastJson(new ZSONTools.Caller(writer2) {
            /* class ohos.utils.zson.$$Lambda$ZSONWriter$iuT78OMtG08kyU09NHIWZCpu9U */
            private final /* synthetic */ Writer f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.utils.zson.ZSONTools.Caller
            public final Object call() {
                return ZSONWriter.lambda$new$0(this.f$0);
            }
        });
        this.writer.config(SerializerFeature.PrettyFormat, z);
    }

    static /* synthetic */ JSONWriter lambda$new$0(Writer writer2) {
        return new JSONWriter(writer2);
    }

    public void startArray() {
        JSONWriter jSONWriter = this.writer;
        Objects.requireNonNull(jSONWriter);
        ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
            /* class ohos.utils.zson.$$Lambda$yQ8PzdHhBgTHnn92HqIjin0OClM */

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                JSONWriter.this.startArray();
            }
        });
    }

    public void startObject() {
        JSONWriter jSONWriter = this.writer;
        Objects.requireNonNull(jSONWriter);
        ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
            /* class ohos.utils.zson.$$Lambda$9a6W_1dDOjf7C6U2ntVs3dMotGU */

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                JSONWriter.this.startObject();
            }
        });
    }

    public /* synthetic */ void lambda$writeName$1$ZSONWriter(String str) throws IOException {
        this.writer.writeKey(str);
    }

    public void writeName(String str) {
        ZSONTools.callFastJson(new ZSONTools.VoidCaller(str) {
            /* class ohos.utils.zson.$$Lambda$ZSONWriter$_YScp2y1XMDktMLM0OjvCdrDM */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                ZSONWriter.this.lambda$writeName$1$ZSONWriter(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$writeBoolean$2$ZSONWriter(boolean z) throws IOException {
        this.writer.writeValue(Boolean.valueOf(z));
    }

    public void writeBoolean(boolean z) {
        ZSONTools.callFastJson(new ZSONTools.VoidCaller(z) {
            /* class ohos.utils.zson.$$Lambda$ZSONWriter$d8gNedhM3pF2EDNVmgM4hQfJ8R4 */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                ZSONWriter.this.lambda$writeBoolean$2$ZSONWriter(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$writeNull$3$ZSONWriter() throws IOException {
        this.writer.writeValue(null);
    }

    public void writeNull() {
        ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
            /* class ohos.utils.zson.$$Lambda$ZSONWriter$nOrmLVTKL57p7Vb5BHtrgv5U2QQ */

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                ZSONWriter.this.lambda$writeNull$3$ZSONWriter();
            }
        });
    }

    public /* synthetic */ void lambda$writeNumber$4$ZSONWriter(Number number) throws IOException {
        this.writer.writeValue(number);
    }

    public void writeNumber(Number number) {
        ZSONTools.callFastJson(new ZSONTools.VoidCaller(number) {
            /* class ohos.utils.zson.$$Lambda$ZSONWriter$5AIFMUb8vUwj_Qmy5g_Fzr8VOls */
            private final /* synthetic */ Number f$1;

            {
                this.f$1 = r2;
            }

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                ZSONWriter.this.lambda$writeNumber$4$ZSONWriter(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$writeString$5$ZSONWriter(String str) throws IOException {
        this.writer.writeValue(str);
    }

    public void writeString(String str) {
        ZSONTools.callFastJson(new ZSONTools.VoidCaller(str) {
            /* class ohos.utils.zson.$$Lambda$ZSONWriter$3oLEUQhE6qpm2K9O09Pxpp7JcE0 */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                ZSONWriter.this.lambda$writeString$5$ZSONWriter(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$writeObject$6$ZSONWriter(Object obj) throws IOException {
        this.writer.writeObject(obj);
    }

    public void writeObject(Object obj) {
        ZSONTools.callFastJson(new ZSONTools.VoidCaller(obj) {
            /* class ohos.utils.zson.$$Lambda$ZSONWriter$SfUp_IU17nyAOoQzyQD3q2ub2ZQ */
            private final /* synthetic */ Object f$1;

            {
                this.f$1 = r2;
            }

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                ZSONWriter.this.lambda$writeObject$6$ZSONWriter(this.f$1);
            }
        });
    }

    public void endArray() {
        JSONWriter jSONWriter = this.writer;
        Objects.requireNonNull(jSONWriter);
        ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
            /* class ohos.utils.zson.$$Lambda$clSuD9spzfOTdxXcfYs52sH5ZGk */

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                JSONWriter.this.endArray();
            }
        });
    }

    public void endObject() {
        JSONWriter jSONWriter = this.writer;
        Objects.requireNonNull(jSONWriter);
        ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
            /* class ohos.utils.zson.$$Lambda$bcLxc5hJg2LZgekHqoMKlC9f_A */

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                JSONWriter.this.endObject();
            }
        });
    }

    @Override // java.io.Flushable
    public void flush() {
        JSONWriter jSONWriter = this.writer;
        Objects.requireNonNull(jSONWriter);
        ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
            /* class ohos.utils.zson.$$Lambda$HA3aPj1FiFiMsW5wxa00SpPBfd8 */

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                JSONWriter.this.flush();
            }
        });
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        JSONWriter jSONWriter = this.writer;
        Objects.requireNonNull(jSONWriter);
        ZSONTools.callFastJson(new ZSONTools.VoidCaller() {
            /* class ohos.utils.zson.$$Lambda$__yw_rOhNcNYS_EmQcRfr_IzPWA */

            @Override // ohos.utils.zson.ZSONTools.VoidCaller
            public final void call() {
                JSONWriter.this.close();
            }
        });
    }
}
