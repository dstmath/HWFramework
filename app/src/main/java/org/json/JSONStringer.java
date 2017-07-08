package org.json;

import android.icu.impl.PatternTokenizer;
import android.icu.text.PluralRules;
import dalvik.bytecode.Opcodes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import libcore.icu.ICU;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public class JSONStringer {
    private final String indent;
    final StringBuilder out;
    private final List<Scope> stack;

    enum Scope {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.json.JSONStringer.Scope.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.json.JSONStringer.Scope.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: org.json.JSONStringer.Scope.<clinit>():void");
        }
    }

    public JSONStringer() {
        this.out = new StringBuilder();
        this.stack = new ArrayList();
        this.indent = null;
    }

    JSONStringer(int indentSpaces) {
        this.out = new StringBuilder();
        this.stack = new ArrayList();
        char[] indentChars = new char[indentSpaces];
        Arrays.fill(indentChars, ' ');
        this.indent = new String(indentChars);
    }

    public JSONStringer array() throws JSONException {
        return open(Scope.EMPTY_ARRAY, "[");
    }

    public JSONStringer endArray() throws JSONException {
        return close(Scope.EMPTY_ARRAY, Scope.NONEMPTY_ARRAY, "]");
    }

    public JSONStringer object() throws JSONException {
        return open(Scope.EMPTY_OBJECT, "{");
    }

    public JSONStringer endObject() throws JSONException {
        return close(Scope.EMPTY_OBJECT, Scope.NONEMPTY_OBJECT, "}");
    }

    JSONStringer open(Scope empty, String openBracket) throws JSONException {
        if (!this.stack.isEmpty() || this.out.length() <= 0) {
            beforeValue();
            this.stack.add(empty);
            this.out.append(openBracket);
            return this;
        }
        throw new JSONException("Nesting problem: multiple top-level roots");
    }

    JSONStringer close(Scope empty, Scope nonempty, String closeBracket) throws JSONException {
        Scope context = peek();
        if (context == nonempty || context == empty) {
            this.stack.remove(this.stack.size() - 1);
            if (context == nonempty) {
                newline();
            }
            this.out.append(closeBracket);
            return this;
        }
        throw new JSONException("Nesting problem");
    }

    private Scope peek() throws JSONException {
        if (!this.stack.isEmpty()) {
            return (Scope) this.stack.get(this.stack.size() - 1);
        }
        throw new JSONException("Nesting problem");
    }

    private void replaceTop(Scope topOfStack) {
        this.stack.set(this.stack.size() - 1, topOfStack);
    }

    public JSONStringer value(Object value) throws JSONException {
        if (this.stack.isEmpty()) {
            throw new JSONException("Nesting problem");
        } else if (value instanceof JSONArray) {
            ((JSONArray) value).writeTo(this);
            return this;
        } else if (value instanceof JSONObject) {
            ((JSONObject) value).writeTo(this);
            return this;
        } else {
            beforeValue();
            if (value == null || (value instanceof Boolean) || value == JSONObject.NULL) {
                this.out.append(value);
            } else if (value instanceof Number) {
                this.out.append(JSONObject.numberToString((Number) value));
            } else {
                string(value.toString());
            }
            return this;
        }
    }

    public JSONStringer value(boolean value) throws JSONException {
        if (this.stack.isEmpty()) {
            throw new JSONException("Nesting problem");
        }
        beforeValue();
        this.out.append(value);
        return this;
    }

    public JSONStringer value(double value) throws JSONException {
        if (this.stack.isEmpty()) {
            throw new JSONException("Nesting problem");
        }
        beforeValue();
        this.out.append(JSONObject.numberToString(Double.valueOf(value)));
        return this;
    }

    public JSONStringer value(long value) throws JSONException {
        if (this.stack.isEmpty()) {
            throw new JSONException("Nesting problem");
        }
        beforeValue();
        this.out.append(value);
        return this;
    }

    private void string(String value) {
        this.out.append("\"");
        int length = value.length();
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            switch (c) {
                case NodeFilter.SHOW_CDATA_SECTION /*8*/:
                    this.out.append("\\b");
                    break;
                case XmlPullParser.COMMENT /*9*/:
                    this.out.append("\\t");
                    break;
                case XmlPullParser.DOCDECL /*10*/:
                    this.out.append("\\n");
                    break;
                case ICU.U_ILLEGAL_CHAR_FOUND /*12*/:
                    this.out.append("\\f");
                    break;
                case Opcodes.OP_MOVE_EXCEPTION /*13*/:
                    this.out.append("\\r");
                    break;
                case Opcodes.OP_NEW_INSTANCE /*34*/:
                case Opcodes.OP_CMPL_DOUBLE /*47*/:
                case Opcodes.OP_IPUT_BOOLEAN /*92*/:
                    this.out.append(PatternTokenizer.BACK_SLASH).append(c);
                    break;
                default:
                    if (c > '\u001f') {
                        this.out.append(c);
                        break;
                    }
                    this.out.append(String.format("\\u%04x", new Object[]{Integer.valueOf(c)}));
                    break;
            }
        }
        this.out.append("\"");
    }

    private void newline() {
        if (this.indent != null) {
            this.out.append("\n");
            for (int i = 0; i < this.stack.size(); i++) {
                this.out.append(this.indent);
            }
        }
    }

    public JSONStringer key(String name) throws JSONException {
        if (name == null) {
            throw new JSONException("Names must be non-null");
        }
        beforeKey();
        string(name);
        return this;
    }

    private void beforeKey() throws JSONException {
        Scope context = peek();
        if (context == Scope.NONEMPTY_OBJECT) {
            this.out.append(',');
        } else if (context != Scope.EMPTY_OBJECT) {
            throw new JSONException("Nesting problem");
        }
        newline();
        replaceTop(Scope.DANGLING_KEY);
    }

    private void beforeValue() throws JSONException {
        if (!this.stack.isEmpty()) {
            Scope context = peek();
            if (context == Scope.EMPTY_ARRAY) {
                replaceTop(Scope.NONEMPTY_ARRAY);
                newline();
            } else if (context == Scope.NONEMPTY_ARRAY) {
                this.out.append(',');
                newline();
            } else if (context == Scope.DANGLING_KEY) {
                this.out.append(this.indent == null ? ":" : PluralRules.KEYWORD_RULE_SEPARATOR);
                replaceTop(Scope.NONEMPTY_OBJECT);
            } else if (context != Scope.NULL) {
                throw new JSONException("Nesting problem");
            }
        }
    }

    public String toString() {
        return this.out.length() == 0 ? null : this.out.toString();
    }
}
