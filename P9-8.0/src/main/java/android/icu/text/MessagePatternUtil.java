package android.icu.text;

import android.icu.text.MessagePattern.ArgType;
import android.icu.text.MessagePattern.Part;
import android.icu.text.MessagePattern.Part.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MessagePatternUtil {
    private static final /* synthetic */ int[] -android-icu-text-MessagePattern$ArgTypeSwitchesValues = null;

    public static class Node {
        /* synthetic */ Node(Node -this0) {
            this();
        }

        private Node() {
        }
    }

    public static class MessageContentsNode extends Node {
        private Type type;

        public enum Type {
            TEXT,
            ARG,
            REPLACE_NUMBER
        }

        /* synthetic */ MessageContentsNode(Type type, MessageContentsNode -this1) {
            this(type);
        }

        public Type getType() {
            return this.type;
        }

        public String toString() {
            return "{REPLACE_NUMBER}";
        }

        private MessageContentsNode(Type type) {
            super();
            this.type = type;
        }

        private static MessageContentsNode createReplaceNumberNode() {
            return new MessageContentsNode(Type.REPLACE_NUMBER);
        }
    }

    public static class ArgNode extends MessageContentsNode {
        private ArgType argType;
        private ComplexArgStyleNode complexStyle;
        private String name;
        private int number = -1;
        private String style;
        private String typeName;

        public ArgType getArgType() {
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
            sb.append('{').append(this.name);
            if (this.argType != ArgType.NONE) {
                sb.append(',').append(this.typeName);
                if (this.argType != ArgType.SIMPLE) {
                    sb.append(',').append(this.complexStyle.toString());
                } else if (this.style != null) {
                    sb.append(',').append(this.style);
                }
            }
            return sb.append('}').toString();
        }

        private ArgNode() {
            super(Type.ARG, null);
        }

        private static ArgNode createArgNode() {
            return new ArgNode();
        }
    }

    public static class ComplexArgStyleNode extends Node {
        private ArgType argType;
        private boolean explicitOffset;
        private volatile List<VariantNode> list;
        private double offset;

        /* synthetic */ ComplexArgStyleNode(ArgType argType, ComplexArgStyleNode -this1) {
            this(argType);
        }

        public ArgType getArgType() {
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
                } else if (!"other".equals(variant.getSelector())) {
                    keywordVariants.add(variant);
                } else if (other == null) {
                    other = variant;
                }
            }
            return other;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('(').append(this.argType.toString()).append(" style) ");
            if (hasExplicitOffset()) {
                sb.append("offset:").append(this.offset).append(' ');
            }
            return sb.append(this.list.toString()).toString();
        }

        private ComplexArgStyleNode(ArgType argType) {
            super();
            this.list = new ArrayList();
            this.argType = argType;
        }

        private void addVariant(VariantNode variant) {
            this.list.add(variant);
        }

        private ComplexArgStyleNode freeze() {
            this.list = Collections.unmodifiableList(this.list);
            return this;
        }
    }

    public static class MessageNode extends Node {
        private volatile List<MessageContentsNode> list;

        /* synthetic */ MessageNode(MessageNode -this0) {
            this();
        }

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

        private void addContentsNode(MessageContentsNode node) {
            if ((node instanceof TextNode) && (this.list.isEmpty() ^ 1) != 0) {
                MessageContentsNode lastNode = (MessageContentsNode) this.list.get(this.list.size() - 1);
                if (lastNode instanceof TextNode) {
                    TextNode textNode = (TextNode) lastNode;
                    textNode.text = textNode.text + ((TextNode) node).text;
                    return;
                }
            }
            this.list.add(node);
        }

        private MessageNode freeze() {
            this.list = Collections.unmodifiableList(this.list);
            return this;
        }
    }

    public static class TextNode extends MessageContentsNode {
        private String text;

        /* synthetic */ TextNode(String text, TextNode -this1) {
            this(text);
        }

        public String getText() {
            return this.text;
        }

        public String toString() {
            return "«" + this.text + "»";
        }

        private TextNode(String text) {
            super(Type.TEXT, null);
            this.text = text;
        }
    }

    public static class VariantNode extends Node {
        private MessageNode msgNode;
        private double numericValue;
        private String selector;

        /* synthetic */ VariantNode(VariantNode -this0) {
            this();
        }

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
                sb.append(this.numericValue).append(" (").append(this.selector).append(") {");
            } else {
                sb.append(this.selector).append(" {");
            }
            return sb.append(this.msgNode.toString()).append('}').toString();
        }

        private VariantNode() {
            super();
            this.numericValue = -1.23456789E8d;
        }
    }

    private static /* synthetic */ int[] -getandroid-icu-text-MessagePattern$ArgTypeSwitchesValues() {
        if (-android-icu-text-MessagePattern$ArgTypeSwitchesValues != null) {
            return -android-icu-text-MessagePattern$ArgTypeSwitchesValues;
        }
        int[] iArr = new int[ArgType.values().length];
        try {
            iArr[ArgType.CHOICE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ArgType.NONE.ordinal()] = 6;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ArgType.PLURAL.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ArgType.SELECT.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ArgType.SELECTORDINAL.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ArgType.SIMPLE.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        -android-icu-text-MessagePattern$ArgTypeSwitchesValues = iArr;
        return iArr;
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
        } else if (pattern.getPartType(0) == Type.MSG_START) {
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
            Part part = pattern.getPart(i);
            int patternIndex = part.getIndex();
            if (prevPatternIndex < patternIndex) {
                node.addContentsNode(new TextNode(pattern.getPatternString().substring(prevPatternIndex, patternIndex), null));
            }
            if (i == limit) {
                return node.freeze();
            }
            Type partType = part.getType();
            if (partType == Type.ARG_START) {
                int argLimit = pattern.getLimitPartIndex(i);
                node.addContentsNode(buildArgNode(pattern, i, argLimit));
                i = argLimit;
                part = pattern.getPart(argLimit);
            } else if (partType == Type.REPLACE_NUMBER) {
                node.addContentsNode(MessageContentsNode.createReplaceNumberNode());
            }
            prevPatternIndex = part.getLimit();
            i++;
        }
    }

    private static ArgNode buildArgNode(MessagePattern pattern, int start, int limit) {
        ArgNode node = ArgNode.createArgNode();
        ArgType argType = node.argType = pattern.getPart(start).getArgType();
        start++;
        Part part = pattern.getPart(start);
        node.name = pattern.getSubstring(part);
        if (part.getType() == Type.ARG_NUMBER) {
            node.number = part.getValue();
        }
        start++;
        switch (-getandroid-icu-text-MessagePattern$ArgTypeSwitchesValues()[argType.ordinal()]) {
            case 1:
                node.typeName = "choice";
                node.complexStyle = buildChoiceStyleNode(pattern, start, limit);
                break;
            case 2:
                node.typeName = "plural";
                node.complexStyle = buildPluralStyleNode(pattern, start, limit, argType);
                break;
            case 3:
                node.typeName = "select";
                node.complexStyle = buildSelectStyleNode(pattern, start, limit);
                break;
            case 4:
                node.typeName = "selectordinal";
                node.complexStyle = buildPluralStyleNode(pattern, start, limit, argType);
                break;
            case 5:
                int start2 = start + 1;
                node.typeName = pattern.getSubstring(pattern.getPart(start));
                if (start2 < limit) {
                    node.style = pattern.getSubstring(pattern.getPart(start2));
                }
                start = start2;
                break;
        }
        return node;
    }

    private static ComplexArgStyleNode buildChoiceStyleNode(MessagePattern pattern, int start, int limit) {
        ComplexArgStyleNode node = new ComplexArgStyleNode(ArgType.CHOICE, null);
        while (start < limit) {
            int valueIndex = start;
            double value = pattern.getNumericValue(pattern.getPart(start));
            start += 2;
            int msgLimit = pattern.getLimitPartIndex(start);
            VariantNode variant = new VariantNode();
            variant.selector = pattern.getSubstring(pattern.getPart(valueIndex + 1));
            variant.numericValue = value;
            variant.msgNode = buildMessageNode(pattern, start, msgLimit);
            node.addVariant(variant);
            start = msgLimit + 1;
        }
        return node.freeze();
    }

    private static ComplexArgStyleNode buildPluralStyleNode(MessagePattern pattern, int start, int limit, ArgType argType) {
        int start2;
        ComplexArgStyleNode node = new ComplexArgStyleNode(argType, null);
        Part offset = pattern.getPart(start);
        if (offset.getType().hasNumericValue()) {
            node.explicitOffset = true;
            node.offset = pattern.getNumericValue(offset);
            start2 = start + 1;
        } else {
            start2 = start;
        }
        while (start2 < limit) {
            start = start2 + 1;
            Part selector = pattern.getPart(start2);
            double value = -1.23456789E8d;
            Part part = pattern.getPart(start);
            if (part.getType().hasNumericValue()) {
                value = pattern.getNumericValue(part);
                start++;
            }
            int msgLimit = pattern.getLimitPartIndex(start);
            VariantNode variant = new VariantNode();
            variant.selector = pattern.getSubstring(selector);
            variant.numericValue = value;
            variant.msgNode = buildMessageNode(pattern, start, msgLimit);
            node.addVariant(variant);
            start2 = msgLimit + 1;
        }
        return node.freeze();
    }

    private static ComplexArgStyleNode buildSelectStyleNode(MessagePattern pattern, int start, int limit) {
        ComplexArgStyleNode node = new ComplexArgStyleNode(ArgType.SELECT, null);
        int start2 = start;
        while (start2 < limit) {
            start = start2 + 1;
            Part selector = pattern.getPart(start2);
            int msgLimit = pattern.getLimitPartIndex(start);
            VariantNode variant = new VariantNode();
            variant.selector = pattern.getSubstring(selector);
            variant.msgNode = buildMessageNode(pattern, start, msgLimit);
            node.addVariant(variant);
            start2 = msgLimit + 1;
        }
        return node.freeze();
    }
}
