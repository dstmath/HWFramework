package com.android.okhttp;

import com.android.okhttp.okio.Buffer;

public final class FormEncodingBuilder {
    private static final MediaType CONTENT_TYPE = null;
    private final Buffer content;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.FormEncodingBuilder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.FormEncodingBuilder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.FormEncodingBuilder.<clinit>():void");
    }

    public FormEncodingBuilder() {
        this.content = new Buffer();
    }

    public FormEncodingBuilder add(String name, String value) {
        if (this.content.size() > 0) {
            this.content.writeByte(38);
        }
        HttpUrl.canonicalize(this.content, name, 0, name.length(), " \"':;<=>@[]^`{}|/\\?#&!$(),~", false, false, true, true);
        this.content.writeByte(61);
        HttpUrl.canonicalize(this.content, value, 0, value.length(), " \"':;<=>@[]^`{}|/\\?#&!$(),~", false, false, true, true);
        return this;
    }

    public FormEncodingBuilder addEncoded(String name, String value) {
        if (this.content.size() > 0) {
            this.content.writeByte(38);
        }
        HttpUrl.canonicalize(this.content, name, 0, name.length(), " \"':;<=>@[]^`{}|/\\?#&!$(),~", true, false, true, true);
        this.content.writeByte(61);
        HttpUrl.canonicalize(this.content, value, 0, value.length(), " \"':;<=>@[]^`{}|/\\?#&!$(),~", true, false, true, true);
        return this;
    }

    public RequestBody build() {
        return RequestBody.create(CONTENT_TYPE, this.content.snapshot());
    }
}
