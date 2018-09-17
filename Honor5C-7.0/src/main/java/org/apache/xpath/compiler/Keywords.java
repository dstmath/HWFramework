package org.apache.xpath.compiler;

import java.util.Hashtable;

public class Keywords {
    private static final String FROM_ANCESTORS_OR_SELF_STRING = "ancestor-or-self";
    private static final String FROM_ANCESTORS_STRING = "ancestor";
    private static final String FROM_ATTRIBUTES_STRING = "attribute";
    private static final String FROM_CHILDREN_STRING = "child";
    private static final String FROM_DESCENDANTS_OR_SELF_STRING = "descendant-or-self";
    private static final String FROM_DESCENDANTS_STRING = "descendant";
    private static final String FROM_FOLLOWING_SIBLINGS_STRING = "following-sibling";
    private static final String FROM_FOLLOWING_STRING = "following";
    private static final String FROM_NAMESPACE_STRING = "namespace";
    private static final String FROM_PARENT_STRING = "parent";
    private static final String FROM_PRECEDING_SIBLINGS_STRING = "preceding-sibling";
    private static final String FROM_PRECEDING_STRING = "preceding";
    private static final String FROM_SELF_ABBREVIATED_STRING = ".";
    private static final String FROM_SELF_STRING = "self";
    public static final String FUNC_BOOLEAN_STRING = "boolean";
    public static final String FUNC_CEILING_STRING = "ceiling";
    public static final String FUNC_CONCAT_STRING = "concat";
    public static final String FUNC_CONTAINS_STRING = "contains";
    public static final String FUNC_COUNT_STRING = "count";
    public static final String FUNC_CURRENT_STRING = "current";
    public static final String FUNC_DOCLOCATION_STRING = "document-location";
    public static final String FUNC_EXT_ELEM_AVAILABLE_STRING = "element-available";
    public static final String FUNC_EXT_FUNCTION_AVAILABLE_STRING = "function-available";
    public static final String FUNC_FALSE_STRING = "false";
    public static final String FUNC_FLOOR_STRING = "floor";
    public static final String FUNC_GENERATE_ID_STRING = "generate-id";
    static final String FUNC_ID_STRING = "id";
    public static final String FUNC_KEY_STRING = "key";
    public static final String FUNC_LANG_STRING = "lang";
    public static final String FUNC_LAST_STRING = "last";
    public static final String FUNC_LOCAL_PART_STRING = "local-name";
    public static final String FUNC_NAMESPACE_STRING = "namespace-uri";
    public static final String FUNC_NAME_STRING = "name";
    public static final String FUNC_NORMALIZE_SPACE_STRING = "normalize-space";
    public static final String FUNC_NOT_STRING = "not";
    public static final String FUNC_NUMBER_STRING = "number";
    public static final String FUNC_POSITION_STRING = "position";
    public static final String FUNC_ROUND_STRING = "round";
    public static final String FUNC_STARTS_WITH_STRING = "starts-with";
    public static final String FUNC_STRING_LENGTH_STRING = "string-length";
    public static final String FUNC_STRING_STRING = "string";
    public static final String FUNC_SUBSTRING_AFTER_STRING = "substring-after";
    public static final String FUNC_SUBSTRING_BEFORE_STRING = "substring-before";
    public static final String FUNC_SUBSTRING_STRING = "substring";
    public static final String FUNC_SUM_STRING = "sum";
    public static final String FUNC_SYSTEM_PROPERTY_STRING = "system-property";
    public static final String FUNC_TRANSLATE_STRING = "translate";
    public static final String FUNC_TRUE_STRING = "true";
    public static final String FUNC_UNPARSED_ENTITY_URI_STRING = "unparsed-entity-uri";
    private static final String NODETYPE_ANYELEMENT_STRING = "*";
    private static final String NODETYPE_COMMENT_STRING = "comment";
    private static final String NODETYPE_NODE_STRING = "node";
    private static final String NODETYPE_PI_STRING = "processing-instruction";
    private static final String NODETYPE_TEXT_STRING = "text";
    private static Hashtable m_axisnames;
    private static Hashtable m_keywords;
    private static Hashtable m_nodetests;
    private static Hashtable m_nodetypes;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xpath.compiler.Keywords.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xpath.compiler.Keywords.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xpath.compiler.Keywords.<clinit>():void");
    }

    static Object getAxisName(String key) {
        return m_axisnames.get(key);
    }

    static Object lookupNodeTest(String key) {
        return m_nodetests.get(key);
    }

    static Object getKeyWord(String key) {
        return m_keywords.get(key);
    }

    static Object getNodeType(String key) {
        return m_nodetypes.get(key);
    }
}
