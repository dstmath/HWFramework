package android.icu.text;

import android.icu.text.MessagePattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MessagePatternUtil {

    public static class ArgNode extends MessageContentsNode {
        /* access modifiers changed from: private */
        public MessagePattern.ArgType argType;
        /* access modifiers changed from: private */
        public ComplexArgStyleNode complexStyle;
        /* access modifiers changed from: private */
        public String name;
        /* access modifiers changed from: private */
        public int number = -1;
        /* access modifiers changed from: private */
        public String style;
        /* access modifiers changed from: private */
        public String typeName;

        public MessagePattern.ArgType getArgType() {
            return this.argType;
        }

        public String getName() {
            return this.name;
        }

        public int getNumber() {
            return this.number;
        }

        public String getTypeName() {
            return this.typeName;
        }

        public String getSimpleStyle() {
            return this.style;
        }

        public ComplexArgStyleNode getComplexStyle() {
            return this.complexStyle;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            sb.append(this.name);
            if (this.argType != MessagePattern.ArgType.NONE) {
                sb.append(',');
                sb.append(this.typeName);
                if (this.argType != MessagePattern.ArgType.SIMPLE) {
                    sb.append(',');
                    sb.append(this.complexStyle.toString());
                } else if (this.style != null) {
                    sb.append(',');
                    sb.append(this.style);
                }
            }
            sb.append('}');
            return sb.toString();
        }

        private ArgNode() {
            super(MessageContentsNode.Type.ARG);
        }

        /* access modifiers changed from: private */
        public static ArgNode createArgNode() {
            return new ArgNode();
        }
    }

    public static class ComplexArgStyleNode extends Node {
        private MessagePattern.ArgType argType;
        /* access modifiers changed from: private */
        public boolean explicitOffset;
        private volatile List<VariantNode> list;
        /* access modifiers changed from: private */
        public double offset;

        public MessagePattern.ArgType getArgType() {
            return this.argType;
        }

        public boolean hasExplicitOffset() {
            return this.explicitOffset;
        }

        public double getOffset() {
            return this.offset;
        }

        public List<VariantNode> getVariants() {
            return this.list;
        }

        public VariantNode getVariantsByType(List<VariantNode> numericVariants, List<VariantNode> keywordVariants) {
            if (numericVariants != null) {
                numericVariants.clear();
            }
            keywordVariants.clear();
            VariantNode other = null;
            for (VariantNode variant : this.list) {
                if (variant.isSelectorNumeric()) {
                    numericVariants.add(variant);
                } else if (!PluralRules.KEYWORD_OTHER.equals(variant.getSelector())) {
                    keywordVariants.add(variant);
                } else if (other == null) {
                    other = variant;
                }
            }
            return other;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('(');
            sb.append(this.argType.toString());
            sb.append(" style) ");
            if (hasExplicitOffset()) {
                sb.append("offset:");
                sb.append(this.offset);
                sb.append(' ');
            }
            sb.append(this.list.toString());
            return sb.toString();
        }

        private ComplexArgStyleNode(MessagePattern.ArgType argType2) {
            super();
            this.list = new ArrayList();
            this.argType = argType2;
        }

        /* access modifiers changed from: private */
        public void addVariant(VariantNode variant) {
            this.list.add(variant);
        }

        /* access modifiers changed from: private */
        public ComplexArgStyleNode freeze() {
            this.list = Collections.unmodifiableList(this.list);
            return this;
        }
    }

    public static class MessageContentsNode extends Node {
        private Type type;

        public enum Type {
            TEXT,
            ARG,
            REPLACE_NUMBER
        }

        public Type getType() {
            return this.type;
        }

        public String toString() {
            return "{REPLACE_NUMBER}";
        }

        private MessageContentsNode(Type type2) {
            super();
            this.type = type2;
        }

        /* access modifiers changed from: private */
        public static MessageContentsNode createReplaceNumberNode() {
            return new MessageContentsNode(Type.REPLACE_NUMBER);
        }
    }

    public static class MessageNode extends Node {
        private volatile List<MessageContentsNode> list;

        public List<MessageContentsNode> getContents() {
            return this.list;
        }

        public String toString() {
            return this.list.toString();
        }

        private MessageNode() {
            super();
            this.list = new ArrayList();
        }

        /* access modifiers changed from: private */
        public void addContentsNode(MessageContentsNode node) {
            if ((node instanceof TextNode) && !this.list.isEmpty()) {
                MessageContentsNode lastNode = this.list.get(this.list.size() - 1);
                if (lastNode instanceof TextNode) {
                    TextNode textNode = (TextNode) lastNode;
                    String unused = textNode.text = textNode.text + ((TextNode) node).text;
                    return;
                }
            }
            this.list.add(node);
        }

        /* access modifiers changed from: private */
        public MessageNode freeze() {
            this.list = Collections.unmodifiableList(this.list);
            return this;
        }
    }

    public static class Node {
        private Node() {
        }
    }

    public static class TextNode extends MessageContentsNode {
        /* access modifiers changed from: private */
        public String text;

        public String getText() {
            return this.text;
        }

        public String toString() {
            return "«" + this.text + "»";
        }

        private TextNode(String text2) {
            super(MessageContentsNode.Type.TEXT);
            this.text = text2;
        }
    }

    public static class VariantNode extends Node {
        /* access modifiers changed from: private */
        public MessageNode msgNode;
        /* access modifiers changed from: private */
        public double numericValue;
        /* access modifiers changed from: private */
        public String selector;

        public String getSelector() {
            return this.selector;
        }

        public boolean isSelectorNumeric() {
            return this.numericValue != -1.23456789E8d;
        }

        public double getSelectorValue() {
            return this.numericValue;
        }

        public MessageNode getMessage() {
            return this.msgNode;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (isSelectorNumeric()) {
                sb.append(this.numericValue);
                sb.append(" (");
                sb.append(this.selector);
                sb.append(") {");
            } else {
                sb.append(this.selector);
                sb.append(" {");
            }
            sb.append(this.msgNode.toString());
            sb.append('}');
            return sb.toString();
        }

        private VariantNode() {
            super();
            this.numericValue = -1.23456789E8d;
        }
    }

    private MessagePatternUtil() {
    }

    public static MessageNode buildMessageNode(String patternString) {
        return buildMessageNode(new MessagePattern(patternString));
    }

    public static MessageNode buildMessageNode(MessagePattern pattern) {
        int limit = pattern.countParts() - 1;
        if (limit < 0) {
            throw new IllegalArgumentException("The MessagePattern is empty");
        } else if (pattern.getPartType(0) == MessagePattern.Part.Type.MSG_START) {
            return buildMessageNode(pattern, 0, limit);
        } else {
            throw new IllegalArgumentException("The MessagePattern does not represent a MessageFormat pattern");
        }
    }

    private static MessageNode buildMessageNode(MessagePattern pattern, int start, int limit) {
        int prevPatternIndex = pattern.getPart(start).getLimit();
        MessageNode node = new MessageNode();
        int i = start + 1;
        while (true) {
            MessagePattern.Part part = pattern.getPart(i);
            int patternIndex = part.getIndex();
            if (prevPatternIndex < patternIndex) {
                node.addContentsNode(new TextNode(pattern.getPatternString().substring(prevPatternIndex, patternIndex)));
            }
            if (i == limit) {
                return node.freeze();
            }
            MessagePattern.Part.Type partType = part.getType();
            if (partType == MessagePattern.Part.Type.ARG_START) {
                int argLimit = pattern.getLimitPartIndex(i);
                node.addContentsNode(buildArgNode(pattern, i, argLimit));
                i = argLimit;
                part = pattern.getPart(i);
            } else if (partType == MessagePattern.Part.Type.REPLACE_NUMBER) {
                node.addContentsNode(MessageContentsNode.createReplaceNumberNode());
            }
            prevPatternIndex = part.getLimit();
            i++;
        }
    }

    private static ArgNode buildArgNode(MessagePattern pattern, int start, int limit) {
        ArgNode node = ArgNode.createArgNode();
        MessagePattern.ArgType argType = node.argType = pattern.getPart(start).getArgType();
        int start2 = start + 1;
        MessagePattern.Part part = pattern.getPart(start2);
        String unused = node.name = pattern.getSubstring(part);
        if (part.getType() == MessagePattern.Part.Type.ARG_NUMBER) {
            int unused2 = node.number = part.getValue();
        }
        int start3 = start2 + 1;
        switch (argType) {
            case SIMPLE:
                int start4 = start3 + 1;
                String unused3 = node.typeName = pattern.getSubstring(pattern.getPart(start3));
                if (start4 < limit) {
                    String unused4 = node.style = pattern.getSubstring(pattern.getPart(start4));
                }
                int i = start4;
                break;
            case CHOICE:
                String unused5 = node.typeName = "choice";
                ComplexArgStyleNode unused6 = node.complexStyle = buildChoiceStyleNode(pattern, start3, limit);
                break;
            case PLURAL:
                String unused7 = node.typeName = "plural";
                ComplexArgStyleNode unused8 = node.complexStyle = buildPluralStyleNode(pattern, start3, limit, argType);
                break;
            case SELECT:
                String unused9 = node.typeName = "select";
                ComplexArgStyleNode unused10 = node.complexStyle = buildSelectStyleNode(pattern, start3, limit);
                break;
            case SELECTORDINAL:
                String unused11 = node.typeName = "selectordinal";
                ComplexArgStyleNode unused12 = node.complexStyle = buildPluralStyleNode(pattern, start3, limit, argType);
                break;
        }
        return node;
    }

    private static ComplexArgStyleNode buildChoiceStyleNode(MessagePattern pattern, int start, int limit) {
        ComplexArgStyleNode node = new ComplexArgStyleNode(MessagePattern.ArgType.CHOICE);
        while (start < limit) {
            int valueIndex = start;
            double value = pattern.getNumericValue(pattern.getPart(start));
            int start2 = start + 2;
            int msgLimit = pattern.getLimitPartIndex(start2);
            VariantNode variant = new VariantNode();
            String unused = variant.selector = pattern.getSubstring(pattern.getPart(valueIndex + 1));
            double unused2 = variant.numericValue = value;
            MessageNode unused3 = variant.msgNode = buildMessageNode(pattern, start2, msgLimit);
            node.addVariant(variant);
            start = msgLimit + 1;
        }
        return node.freeze();
    }

    private static ComplexArgStyleNode buildPluralStyleNode(MessagePattern pattern, int start, int limit, MessagePattern.ArgType argType) {
        ComplexArgStyleNode node = new ComplexArgStyleNode(argType);
        MessagePattern.Part offset = pattern.getPart(start);
        if (offset.getType().hasNumericValue()) {
            boolean unused = node.explicitOffset = true;
            double unused2 = node.offset = pattern.getNumericValue(offset);
            start++;
        }
        while (start < limit) {
            int start2 = start + 1;
            MessagePattern.Part selector = pattern.getPart(start);
            double value = -1.23456789E8d;
            MessagePattern.Part part = pattern.getPart(start2);
            if (part.getType().hasNumericValue()) {
                value = pattern.getNumericValue(part);
                start2++;
            }
            int msgLimit = pattern.getLimitPartIndex(start2);
            VariantNode variant = new VariantNode();
            String unused3 = variant.selector = pattern.getSubstring(selector);
            double unused4 = variant.numericValue = value;
            MessageNode unused5 = variant.msgNode = buildMessageNode(pattern, start2, msgLimit);
            node.addVariant(variant);
            start = msgLimit + 1;
        }
        return node.freeze();
    }

    private static ComplexArgStyleNode buildSelectStyleNode(MessagePattern pattern, int start, int limit) {
        ComplexArgStyleNode node = new ComplexArgStyleNode(MessagePattern.ArgType.SELECT);
        while (start < limit) {
            int start2 = start + 1;
            MessagePattern.Part selector = pattern.getPart(start);
            int msgLimit = pattern.getLimitPartIndex(start2);
            VariantNode variant = new VariantNode();
            String unused = variant.selector = pattern.getSubstring(selector);
            MessageNode unused2 = variant.msgNode = buildMessageNode(pattern, start2, msgLimit);
            node.addVariant(variant);
            start = msgLimit + 1;
        }
        return node.freeze();
    }
}
