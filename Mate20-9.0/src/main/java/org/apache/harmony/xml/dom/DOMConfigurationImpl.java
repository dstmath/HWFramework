package org.apache.harmony.xml.dom;

import android.icu.text.PluralRules;
import java.util.Map;
import java.util.TreeMap;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class DOMConfigurationImpl implements DOMConfiguration {
    /* access modifiers changed from: private */
    public static final Map<String, Parameter> PARAMETERS = new TreeMap(String.CASE_INSENSITIVE_ORDER);
    /* access modifiers changed from: private */
    public boolean cdataSections = true;
    /* access modifiers changed from: private */
    public boolean comments = true;
    /* access modifiers changed from: private */
    public boolean datatypeNormalization = false;
    /* access modifiers changed from: private */
    public boolean entities = true;
    /* access modifiers changed from: private */
    public DOMErrorHandler errorHandler;
    /* access modifiers changed from: private */
    public boolean namespaces = true;
    /* access modifiers changed from: private */
    public String schemaLocation;
    /* access modifiers changed from: private */
    public String schemaType;
    /* access modifiers changed from: private */
    public boolean splitCdataSections = true;
    /* access modifiers changed from: private */
    public boolean validate = false;
    /* access modifiers changed from: private */
    public boolean wellFormed = true;

    static abstract class BooleanParameter implements Parameter {
        BooleanParameter() {
        }

        public boolean canSet(DOMConfigurationImpl config, Object value) {
            return value instanceof Boolean;
        }
    }

    static class FixedParameter implements Parameter {
        final Object onlyValue;

        FixedParameter(Object onlyValue2) {
            this.onlyValue = onlyValue2;
        }

        public Object get(DOMConfigurationImpl config) {
            return this.onlyValue;
        }

        public void set(DOMConfigurationImpl config, Object value) {
            if (!this.onlyValue.equals(value)) {
                throw new DOMException(9, "Unsupported value: " + value);
            }
        }

        public boolean canSet(DOMConfigurationImpl config, Object value) {
            return this.onlyValue.equals(value);
        }
    }

    interface Parameter {
        boolean canSet(DOMConfigurationImpl dOMConfigurationImpl, Object obj);

        Object get(DOMConfigurationImpl dOMConfigurationImpl);

        void set(DOMConfigurationImpl dOMConfigurationImpl, Object obj);
    }

    static {
        PARAMETERS.put("canonical-form", new FixedParameter(false));
        PARAMETERS.put("cdata-sections", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return Boolean.valueOf(config.cdataSections);
            }

            public void set(DOMConfigurationImpl config, Object value) {
                boolean unused = config.cdataSections = ((Boolean) value).booleanValue();
            }
        });
        PARAMETERS.put("check-character-normalization", new FixedParameter(false));
        PARAMETERS.put("comments", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return Boolean.valueOf(config.comments);
            }

            public void set(DOMConfigurationImpl config, Object value) {
                boolean unused = config.comments = ((Boolean) value).booleanValue();
            }
        });
        PARAMETERS.put("datatype-normalization", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return Boolean.valueOf(config.datatypeNormalization);
            }

            public void set(DOMConfigurationImpl config, Object value) {
                if (((Boolean) value).booleanValue()) {
                    boolean unused = config.datatypeNormalization = true;
                    boolean unused2 = config.validate = true;
                    return;
                }
                boolean unused3 = config.datatypeNormalization = false;
            }
        });
        PARAMETERS.put("element-content-whitespace", new FixedParameter(true));
        PARAMETERS.put("entities", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return Boolean.valueOf(config.entities);
            }

            public void set(DOMConfigurationImpl config, Object value) {
                boolean unused = config.entities = ((Boolean) value).booleanValue();
            }
        });
        PARAMETERS.put("error-handler", new Parameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.errorHandler;
            }

            public void set(DOMConfigurationImpl config, Object value) {
                DOMErrorHandler unused = config.errorHandler = (DOMErrorHandler) value;
            }

            public boolean canSet(DOMConfigurationImpl config, Object value) {
                return value == null || (value instanceof DOMErrorHandler);
            }
        });
        PARAMETERS.put("infoset", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return Boolean.valueOf(!config.entities && !config.datatypeNormalization && !config.cdataSections && config.wellFormed && config.comments && config.namespaces);
            }

            public void set(DOMConfigurationImpl config, Object value) {
                if (((Boolean) value).booleanValue()) {
                    boolean unused = config.entities = false;
                    boolean unused2 = config.datatypeNormalization = false;
                    boolean unused3 = config.cdataSections = false;
                    boolean unused4 = config.wellFormed = true;
                    boolean unused5 = config.comments = true;
                    boolean unused6 = config.namespaces = true;
                }
            }
        });
        PARAMETERS.put("namespaces", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return Boolean.valueOf(config.namespaces);
            }

            public void set(DOMConfigurationImpl config, Object value) {
                boolean unused = config.namespaces = ((Boolean) value).booleanValue();
            }
        });
        PARAMETERS.put("namespace-declarations", new FixedParameter(true));
        PARAMETERS.put("normalize-characters", new FixedParameter(false));
        PARAMETERS.put("schema-location", new Parameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.schemaLocation;
            }

            public void set(DOMConfigurationImpl config, Object value) {
                String unused = config.schemaLocation = (String) value;
            }

            public boolean canSet(DOMConfigurationImpl config, Object value) {
                return value == null || (value instanceof String);
            }
        });
        PARAMETERS.put("schema-type", new Parameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.schemaType;
            }

            public void set(DOMConfigurationImpl config, Object value) {
                String unused = config.schemaType = (String) value;
            }

            public boolean canSet(DOMConfigurationImpl config, Object value) {
                return value == null || (value instanceof String);
            }
        });
        PARAMETERS.put("split-cdata-sections", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return Boolean.valueOf(config.splitCdataSections);
            }

            public void set(DOMConfigurationImpl config, Object value) {
                boolean unused = config.splitCdataSections = ((Boolean) value).booleanValue();
            }
        });
        PARAMETERS.put("validate", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return Boolean.valueOf(config.validate);
            }

            public void set(DOMConfigurationImpl config, Object value) {
                boolean unused = config.validate = ((Boolean) value).booleanValue();
            }
        });
        PARAMETERS.put("validate-if-schema", new FixedParameter(false));
        PARAMETERS.put("well-formed", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return Boolean.valueOf(config.wellFormed);
            }

            public void set(DOMConfigurationImpl config, Object value) {
                boolean unused = config.wellFormed = ((Boolean) value).booleanValue();
            }
        });
    }

    public boolean canSetParameter(String name, Object value) {
        Parameter parameter = PARAMETERS.get(name);
        return parameter != null && parameter.canSet(this, value);
    }

    public void setParameter(String name, Object value) throws DOMException {
        Parameter parameter = PARAMETERS.get(name);
        if (parameter != null) {
            try {
                parameter.set(this, value);
            } catch (NullPointerException e) {
                throw new DOMException(17, "Null not allowed for " + name);
            } catch (ClassCastException e2) {
                throw new DOMException(17, "Invalid type for " + name + PluralRules.KEYWORD_RULE_SEPARATOR + value.getClass());
            }
        } else {
            throw new DOMException(8, "No such parameter: " + name);
        }
    }

    public Object getParameter(String name) throws DOMException {
        Parameter parameter = PARAMETERS.get(name);
        if (parameter != null) {
            return parameter.get(this);
        }
        throw new DOMException(8, "No such parameter: " + name);
    }

    public DOMStringList getParameterNames() {
        return internalGetParameterNames();
    }

    private static DOMStringList internalGetParameterNames() {
        final String[] result = (String[]) PARAMETERS.keySet().toArray(new String[PARAMETERS.size()]);
        return new DOMStringList() {
            public String item(int index) {
                if (index < result.length) {
                    return result[index];
                }
                return null;
            }

            public int getLength() {
                return result.length;
            }

            public boolean contains(String str) {
                return DOMConfigurationImpl.PARAMETERS.containsKey(str);
            }
        };
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x007f, code lost:
        r0 = ((org.apache.harmony.xml.dom.TextImpl) r5).minimize();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0086, code lost:
        if (r0 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0088, code lost:
        checkTextValidity(r0.buffer);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00b1, code lost:
        r0 = r5.getFirstChild();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b5, code lost:
        if (r0 == null) goto L_0x00c1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00b7, code lost:
        r1 = r0.getNextSibling();
        normalize(r0);
        r0 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        return;
     */
    public void normalize(Node node) {
        switch (node.getNodeType()) {
            case 1:
                NamedNodeMap attributes = ((ElementImpl) node).getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    normalize(attributes.item(i));
                }
                break;
            case 2:
                checkTextValidity(((AttrImpl) node).getValue());
                return;
            case 3:
                break;
            case 4:
                CDATASectionImpl cdata = (CDATASectionImpl) node;
                if (!this.cdataSections) {
                    node = cdata.replaceWithText();
                    break;
                } else {
                    if (cdata.needsSplitting()) {
                        if (this.splitCdataSections) {
                            cdata.split();
                            report(1, "cdata-sections-splitted");
                        } else {
                            report(2, "wf-invalid-character");
                        }
                    }
                    checkTextValidity(cdata.buffer);
                    return;
                }
            case 5:
            case 6:
            case 10:
            case 12:
                return;
            case 7:
                checkTextValidity(((ProcessingInstructionImpl) node).getData());
                return;
            case 8:
                CommentImpl comment = (CommentImpl) node;
                if (!this.comments) {
                    comment.getParentNode().removeChild(comment);
                    return;
                }
                if (comment.containsDashDash()) {
                    report(2, "wf-invalid-character");
                }
                checkTextValidity(comment.buffer);
                return;
            case 9:
            case 11:
                break;
            default:
                throw new DOMException(9, "Unsupported node type " + node.getNodeType());
        }
    }

    private void checkTextValidity(CharSequence s) {
        if (this.wellFormed && !isValid(s)) {
            report(2, "wf-invalid-character");
        }
    }

    private boolean isValid(CharSequence text) {
        int i = 0;
        while (true) {
            boolean valid = true;
            if (i >= text.length()) {
                return true;
            }
            char c = text.charAt(i);
            if (!(c == 9 || c == 10 || c == 13 || ((c >= ' ' && c <= 55295) || (c >= 57344 && c <= 65533)))) {
                valid = false;
            }
            if (!valid) {
                return false;
            }
            i++;
        }
    }

    private void report(short severity, String type) {
        if (this.errorHandler != null) {
            this.errorHandler.handleError(new DOMErrorImpl(severity, type));
        }
    }
}
