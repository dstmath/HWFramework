package ohos.global.icu.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ohos.global.icu.text.MessagePattern;
import ohos.telephony.TelephoneNumberUtils;

public final class MessagePatternUtil {
    private MessagePatternUtil() {
    }

    public static MessageNode buildMessageNode(String str) {
        return buildMessageNode(new MessagePattern(str));
    }

    public static MessageNode buildMessageNode(MessagePattern messagePattern) {
        int countParts = messagePattern.countParts() - 1;
        if (countParts < 0) {
            throw new IllegalArgumentException("The MessagePattern is empty");
        } else if (messagePattern.getPartType(0) == MessagePattern.Part.Type.MSG_START) {
            return buildMessageNode(messagePattern, 0, countParts);
        } else {
            throw new IllegalArgumentException("The MessagePattern does not represent a MessageFormat pattern");
        }
    }

    public static class Node {
        /* synthetic */ Node(AnonymousClass1 r1) {
            this();
        }

        private Node() {
        }
    }

    public static class MessageNode extends Node {
        private volatile List<MessageContentsNode> list;

        /* synthetic */ MessageNode(AnonymousClass1 r1) {
            this();
        }

        public List<MessageContentsNode> getContents() {
            return this.list;
        }

        public String toString() {
            return this.list.toString();
        }

        private MessageNode() {
            super(null);
            this.list = new ArrayList();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addContentsNode(MessageContentsNode messageContentsNode) {
            if ((messageContentsNode instanceof TextNode) && !this.list.isEmpty()) {
                MessageContentsNode messageContentsNode2 = this.list.get(this.list.size() - 1);
                if (messageContentsNode2 instanceof TextNode) {
                    TextNode textNode = (TextNode) messageContentsNode2;
                    textNode.text += ((TextNode) messageContentsNode).text;
                    return;
                }
            }
            this.list.add(messageContentsNode);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private MessageNode freeze() {
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

        public String toString() {
            return "{REPLACE_NUMBER}";
        }

        /* synthetic */ MessageContentsNode(Type type2, AnonymousClass1 r2) {
            this(type2);
        }

        public Type getType() {
            return this.type;
        }

        private MessageContentsNode(Type type2) {
            super(null);
            this.type = type2;
        }

        /* access modifiers changed from: private */
        public static MessageContentsNode createReplaceNumberNode() {
            return new MessageContentsNode(Type.REPLACE_NUMBER);
        }
    }

    public static class TextNode extends MessageContentsNode {
        private String text;

        /* synthetic */ TextNode(String str, AnonymousClass1 r2) {
            this(str);
        }

        public String getText() {
            return this.text;
        }

        @Override // ohos.global.icu.text.MessagePatternUtil.MessageContentsNode
        public String toString() {
            return "«" + this.text + "»";
        }

        private TextNode(String str) {
            super(MessageContentsNode.Type.TEXT, null);
            this.text = str;
        }
    }

    public static class ArgNode extends MessageContentsNode {
        private MessagePattern.ArgType argType;
        private ComplexArgStyleNode complexStyle;
        private String name;
        private int number = -1;
        private String style;
        private String typeName;

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

        @Override // ohos.global.icu.text.MessagePatternUtil.MessageContentsNode
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            sb.append(this.name);
            if (this.argType != MessagePattern.ArgType.NONE) {
                sb.append(TelephoneNumberUtils.PAUSE);
                sb.append(this.typeName);
                if (this.argType != MessagePattern.ArgType.SIMPLE) {
                    sb.append(TelephoneNumberUtils.PAUSE);
                    sb.append(this.complexStyle.toString());
                } else if (this.style != null) {
                    sb.append(TelephoneNumberUtils.PAUSE);
                    sb.append(this.style);
                }
            }
            sb.append('}');
            return sb.toString();
        }

        private ArgNode() {
            super(MessageContentsNode.Type.ARG, null);
        }

        /* access modifiers changed from: private */
        public static ArgNode createArgNode() {
            return new ArgNode();
        }
    }

    public static class ComplexArgStyleNode extends Node {
        private MessagePattern.ArgType argType;
        private boolean explicitOffset;
        private volatile List<VariantNode> list;
        private double offset;

        /* synthetic */ ComplexArgStyleNode(MessagePattern.ArgType argType2, AnonymousClass1 r2) {
            this(argType2);
        }

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

        public VariantNode getVariantsByType(List<VariantNode> list2, List<VariantNode> list3) {
            if (list2 != null) {
                list2.clear();
            }
            list3.clear();
            VariantNode variantNode = null;
            for (VariantNode variantNode2 : this.list) {
                if (variantNode2.isSelectorNumeric()) {
                    list2.add(variantNode2);
                } else if (!"other".equals(variantNode2.getSelector())) {
                    list3.add(variantNode2);
                } else if (variantNode == null) {
                    variantNode = variantNode2;
                }
            }
            return variantNode;
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
            super(null);
            this.list = new ArrayList();
            this.argType = argType2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addVariant(VariantNode variantNode) {
            this.list.add(variantNode);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private ComplexArgStyleNode freeze() {
            this.list = Collections.unmodifiableList(this.list);
            return this;
        }
    }

    public static class VariantNode extends Node {
        private MessageNode msgNode;
        private double numericValue;
        private String selector;

        /* synthetic */ VariantNode(AnonymousClass1 r1) {
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
            super(null);
            this.numericValue = -1.23456789E8d;
        }
    }

    private static MessageNode buildMessageNode(MessagePattern messagePattern, int i, int i2) {
        int limit = messagePattern.getPart(i).getLimit();
        MessageNode messageNode = new MessageNode(null);
        while (true) {
            i++;
            MessagePattern.Part part = messagePattern.getPart(i);
            int index = part.getIndex();
            if (limit < index) {
                messageNode.addContentsNode(new TextNode(messagePattern.getPatternString().substring(limit, index), null));
            }
            if (i == i2) {
                return messageNode.freeze();
            }
            MessagePattern.Part.Type type = part.getType();
            if (type == MessagePattern.Part.Type.ARG_START) {
                int limitPartIndex = messagePattern.getLimitPartIndex(i);
                messageNode.addContentsNode(buildArgNode(messagePattern, i, limitPartIndex));
                part = messagePattern.getPart(limitPartIndex);
                i = limitPartIndex;
            } else if (type == MessagePattern.Part.Type.REPLACE_NUMBER) {
                messageNode.addContentsNode(MessageContentsNode.createReplaceNumberNode());
            }
            limit = part.getLimit();
        }
    }

    private static ArgNode buildArgNode(MessagePattern messagePattern, int i, int i2) {
        ArgNode createArgNode = ArgNode.createArgNode();
        MessagePattern.ArgType argType = createArgNode.argType = messagePattern.getPart(i).getArgType();
        int i3 = i + 1;
        MessagePattern.Part part = messagePattern.getPart(i3);
        createArgNode.name = messagePattern.getSubstring(part);
        if (part.getType() == MessagePattern.Part.Type.ARG_NUMBER) {
            createArgNode.number = part.getValue();
        }
        int i4 = i3 + 1;
        int i5 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$MessagePattern$ArgType[argType.ordinal()];
        if (i5 == 1) {
            int i6 = i4 + 1;
            createArgNode.typeName = messagePattern.getSubstring(messagePattern.getPart(i4));
            if (i6 < i2) {
                createArgNode.style = messagePattern.getSubstring(messagePattern.getPart(i6));
            }
        } else if (i5 == 2) {
            createArgNode.typeName = "choice";
            createArgNode.complexStyle = buildChoiceStyleNode(messagePattern, i4, i2);
        } else if (i5 == 3) {
            createArgNode.typeName = "plural";
            createArgNode.complexStyle = buildPluralStyleNode(messagePattern, i4, i2, argType);
        } else if (i5 == 4) {
            createArgNode.typeName = "select";
            createArgNode.complexStyle = buildSelectStyleNode(messagePattern, i4, i2);
        } else if (i5 == 5) {
            createArgNode.typeName = "selectordinal";
            createArgNode.complexStyle = buildPluralStyleNode(messagePattern, i4, i2, argType);
        }
        return createArgNode;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.text.MessagePatternUtil$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$text$MessagePattern$ArgType = new int[MessagePattern.ArgType.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$text$MessagePattern$ArgType[MessagePattern.ArgType.SIMPLE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$MessagePattern$ArgType[MessagePattern.ArgType.CHOICE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$MessagePattern$ArgType[MessagePattern.ArgType.PLURAL.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$MessagePattern$ArgType[MessagePattern.ArgType.SELECT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$MessagePattern$ArgType[MessagePattern.ArgType.SELECTORDINAL.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    private static ComplexArgStyleNode buildChoiceStyleNode(MessagePattern messagePattern, int i, int i2) {
        ComplexArgStyleNode complexArgStyleNode = new ComplexArgStyleNode(MessagePattern.ArgType.CHOICE, null);
        while (i < i2) {
            double numericValue = messagePattern.getNumericValue(messagePattern.getPart(i));
            int i3 = i + 2;
            int limitPartIndex = messagePattern.getLimitPartIndex(i3);
            VariantNode variantNode = new VariantNode(null);
            variantNode.selector = messagePattern.getSubstring(messagePattern.getPart(i + 1));
            variantNode.numericValue = numericValue;
            variantNode.msgNode = buildMessageNode(messagePattern, i3, limitPartIndex);
            complexArgStyleNode.addVariant(variantNode);
            i = limitPartIndex + 1;
        }
        return complexArgStyleNode.freeze();
    }

    private static ComplexArgStyleNode buildPluralStyleNode(MessagePattern messagePattern, int i, int i2, MessagePattern.ArgType argType) {
        ComplexArgStyleNode complexArgStyleNode = new ComplexArgStyleNode(argType, null);
        MessagePattern.Part part = messagePattern.getPart(i);
        if (part.getType().hasNumericValue()) {
            complexArgStyleNode.explicitOffset = true;
            complexArgStyleNode.offset = messagePattern.getNumericValue(part);
            i++;
        }
        while (i < i2) {
            int i3 = i + 1;
            MessagePattern.Part part2 = messagePattern.getPart(i);
            double d = -1.23456789E8d;
            MessagePattern.Part part3 = messagePattern.getPart(i3);
            if (part3.getType().hasNumericValue()) {
                d = messagePattern.getNumericValue(part3);
                i3++;
            }
            int limitPartIndex = messagePattern.getLimitPartIndex(i3);
            VariantNode variantNode = new VariantNode(null);
            variantNode.selector = messagePattern.getSubstring(part2);
            variantNode.numericValue = d;
            variantNode.msgNode = buildMessageNode(messagePattern, i3, limitPartIndex);
            complexArgStyleNode.addVariant(variantNode);
            i = limitPartIndex + 1;
        }
        return complexArgStyleNode.freeze();
    }

    private static ComplexArgStyleNode buildSelectStyleNode(MessagePattern messagePattern, int i, int i2) {
        ComplexArgStyleNode complexArgStyleNode = new ComplexArgStyleNode(MessagePattern.ArgType.SELECT, null);
        while (i < i2) {
            int i3 = i + 1;
            MessagePattern.Part part = messagePattern.getPart(i);
            int limitPartIndex = messagePattern.getLimitPartIndex(i3);
            VariantNode variantNode = new VariantNode(null);
            variantNode.selector = messagePattern.getSubstring(part);
            variantNode.msgNode = buildMessageNode(messagePattern, i3, limitPartIndex);
            complexArgStyleNode.addVariant(variantNode);
            i = limitPartIndex + 1;
        }
        return complexArgStyleNode.freeze();
    }
}
