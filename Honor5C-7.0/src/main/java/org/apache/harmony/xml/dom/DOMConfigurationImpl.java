package org.apache.harmony.xml.dom;

import android.icu.text.PluralRules;
import java.util.Map;
import libcore.icu.ICU;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public final class DOMConfigurationImpl implements DOMConfiguration {
    private static final Map<String, Parameter> PARAMETERS = null;
    private boolean cdataSections;
    private boolean comments;
    private boolean datatypeNormalization;
    private boolean entities;
    private DOMErrorHandler errorHandler;
    private boolean namespaces;
    private String schemaLocation;
    private String schemaType;
    private boolean splitCdataSections;
    private boolean validate;
    private boolean wellFormed;

    interface Parameter {
        boolean canSet(DOMConfigurationImpl dOMConfigurationImpl, Object obj);

        Object get(DOMConfigurationImpl dOMConfigurationImpl);

        void set(DOMConfigurationImpl dOMConfigurationImpl, Object obj);
    }

    static abstract class BooleanParameter implements Parameter {
        BooleanParameter() {
        }

        public boolean canSet(DOMConfigurationImpl config, Object value) {
            return value instanceof Boolean;
        }
    }

    /* renamed from: org.apache.harmony.xml.dom.DOMConfigurationImpl.13 */
    class AnonymousClass13 implements DOMStringList {
        final /* synthetic */ String[] val$result;

        AnonymousClass13(String[] val$result) {
            this.val$result = val$result;
        }

        public String item(int index) {
            return index < this.val$result.length ? this.val$result[index] : null;
        }

        public int getLength() {
            return this.val$result.length;
        }

        public boolean contains(String str) {
            return DOMConfigurationImpl.PARAMETERS.containsKey(str);
        }
    }

    static class FixedParameter implements Parameter {
        final Object onlyValue;

        FixedParameter(Object onlyValue) {
            this.onlyValue = onlyValue;
        }

        public Object get(DOMConfigurationImpl config) {
            return this.onlyValue;
        }

        public void set(DOMConfigurationImpl config, Object value) {
            if (!this.onlyValue.equals(value)) {
                throw new DOMException((short) 9, "Unsupported value: " + value);
            }
        }

        public boolean canSet(DOMConfigurationImpl config, Object value) {
            return this.onlyValue.equals(value);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.harmony.xml.dom.DOMConfigurationImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.dom.DOMConfigurationImpl.<clinit>():void");
    }

    public DOMConfigurationImpl() {
        this.cdataSections = true;
        this.comments = true;
        this.datatypeNormalization = false;
        this.entities = true;
        this.namespaces = true;
        this.splitCdataSections = true;
        this.validate = false;
        this.wellFormed = true;
    }

    public boolean canSetParameter(String name, Object value) {
        Parameter parameter = (Parameter) PARAMETERS.get(name);
        return parameter != null ? parameter.canSet(this, value) : false;
    }

    public void setParameter(String name, Object value) throws DOMException {
        Parameter parameter = (Parameter) PARAMETERS.get(name);
        if (parameter == null) {
            throw new DOMException((short) 8, "No such parameter: " + name);
        }
        try {
            parameter.set(this, value);
        } catch (NullPointerException e) {
            throw new DOMException((short) 17, "Null not allowed for " + name);
        } catch (ClassCastException e2) {
            throw new DOMException((short) 17, "Invalid type for " + name + PluralRules.KEYWORD_RULE_SEPARATOR + value.getClass());
        }
    }

    public Object getParameter(String name) throws DOMException {
        Parameter parameter = (Parameter) PARAMETERS.get(name);
        if (parameter != null) {
            return parameter.get(this);
        }
        throw new DOMException((short) 8, "No such parameter: " + name);
    }

    public DOMStringList getParameterNames() {
        return new AnonymousClass13((String[]) PARAMETERS.keySet().toArray(new String[PARAMETERS.size()]));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void normalize(Node node) {
        switch (node.getNodeType()) {
            case NodeFilter.SHOW_ELEMENT /*1*/:
                NamedNodeMap attributes = ((ElementImpl) node).getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    normalize(attributes.item(i));
                }
                break;
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                checkTextValidity(((AttrImpl) node).getValue());
            case XmlPullParser.END_TAG /*3*/:
                break;
            case NodeFilter.SHOW_TEXT /*4*/:
                CDATASectionImpl cdata = (CDATASectionImpl) node;
                if (!this.cdataSections) {
                    node = cdata.replaceWithText();
                    break;
                }
                if (cdata.needsSplitting()) {
                    if (this.splitCdataSections) {
                        cdata.split();
                        report((short) 1, "cdata-sections-splitted");
                    } else {
                        report((short) 2, "wf-invalid-character");
                    }
                }
                checkTextValidity(cdata.buffer);
            case XmlPullParser.CDSECT /*5*/:
            case XmlPullParser.ENTITY_REF /*6*/:
            case XmlPullParser.DOCDECL /*10*/:
            case ICU.U_ILLEGAL_CHAR_FOUND /*12*/:
            case XmlPullParser.IGNORABLE_WHITESPACE /*7*/:
                checkTextValidity(((ProcessingInstructionImpl) node).getData());
            case NodeFilter.SHOW_CDATA_SECTION /*8*/:
                CommentImpl comment = (CommentImpl) node;
                if (this.comments) {
                    if (comment.containsDashDash()) {
                        report((short) 2, "wf-invalid-character");
                    }
                    checkTextValidity(comment.buffer);
                    return;
                }
                comment.getParentNode().removeChild(comment);
            case XmlPullParser.COMMENT /*9*/:
            case ICU.U_TRUNCATED_CHAR_FOUND /*11*/:
                break;
            default:
                throw new DOMException((short) 9, "Unsupported node type " + node.getNodeType());
        }
    }

    private void checkTextValidity(CharSequence s) {
        if (this.wellFormed && !isValid(s)) {
            report((short) 2, "wf-invalid-character");
        }
    }

    private boolean isValid(CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean valid = (c == '\t' || c == '\n' || c == '\r' || (c >= ' ' && c <= '\ud7ff')) ? true : c >= '\ue000' && c <= '\ufffd';
            if (!valid) {
                return false;
            }
        }
        return true;
    }

    private void report(short severity, String type) {
        if (this.errorHandler != null) {
            this.errorHandler.handleError(new DOMErrorImpl(severity, type));
        }
    }
}
