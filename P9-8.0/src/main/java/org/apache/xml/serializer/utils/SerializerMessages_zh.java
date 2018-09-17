package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_zh extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[58][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "消息密钥“{0}”不在消息类“{1}”中"};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "消息类“{1}”中的消息“{0}”的格式无效。"};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "串行器类“{0}”不能实现 org.xml.sax.ContentHandler。"};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "找不到资源 [ {0} ]。\n {1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "资源 [ {0} ] 无法装入：{1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "缓冲区大小 <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "检测到无效的 UTF-16 超大字符集：{0}？"};
        contents[7] = new Object[]{"ER_OIERROR", "IO 错误"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "在生成子节点之后或在生成元素之前无法添加属性 {0}。将忽略属性。"};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "尚未声明前缀“{0}”的名称空间。"};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "属性“{0}”在元素外。"};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "名称空间声明“{0}”=“{1}”在元素外。"};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "无法装入“{0}”（检查 CLASSPATH），现在只使用缺省值"};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "尝试输出整数值 {0}（它不是以指定的 {1} 输出编码表示）的字符。"};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "无法为输出方法“{1}”装入属性文件“{0}”（检查 CLASSPATH）"};
        contents[15] = new Object[]{"ER_INVALID_PORT", "端口号无效"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "主机为空时，无法设置端口"};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "主机不是格式正确的地址"};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "模式不一致。"};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "无法从空字符串设置模式"};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "路径包含无效的转义序列"};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "路径包含无效的字符：{0}"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "片段包含无效的字符"};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "路径为空时，无法设置片段"};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "只能为类属 URI 设置片段"};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "URI 中找不到任何模式"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "不能以空参数初始化 URI"};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "路径和片段中都不能指定片段"};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "路径和查询字符串中不能指定查询字符串"};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "如果没有指定主机，则不可以指定端口"};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "如果没有指定主机，则不可以指定用户信息"};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "警告：要求输出文档的版本是“{0}”。不支持此 XML 版本。输出文档的版本将会是“1.0”。"};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "模式是必需的！"};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "传递给 SerializerFactory 的 Properties 对象不具有属性“{0}”。"};
        contents[34] = new Object[]{"FEATURE_NOT_FOUND", "未识别出参数“{0}”。"};
        contents[35] = new Object[]{"FEATURE_NOT_SUPPORTED", "已识别出参数“{0}”，但无法设置请求的值。"};
        contents[36] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "产生的字符串过长不能装入 DOMString：“{0}”。"};
        contents[37] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "此参数名称的值类型与期望的值类型不兼容。"};
        contents[38] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "将要写入数据的输出目标为空。"};
        contents[39] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "遇到不受支持的编码。"};
        contents[40] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "无法将节点序列化。 "};
        contents[41] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "CDATA 部分包含一个或多个终止标记“]]>”。"};
        contents[42] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "无法创建格式正确性检查器的实例。“格式正确”参数已设置为 true，但无法执行格式正确性检查。"};
        contents[43] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "节点“{0}”包含无效的 XML 字符。"};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "在注释中找到无效的 XML 字符 (Unicode: 0x''{0})''。"};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "在处理指令数据中找到无效的 XML 字符 (Unicode: 0x''{0})''。"};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "在 CDATA 部分的内容中找到无效的 XML 字符 (Unicode: 0x''{0})''。"};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "在节点的字符数据内容中找到无效的 XML 字符 (Unicode: 0x''{0})''。"};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "名称为“{1})”的“{0})”中找到无效的 XML 字符。"};
        contents[49] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "注释中不允许有字符串“--”。"};
        contents[50] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "与元素类型“{0}”关联的属性“{1}”的值不得包含“<”字符。"};
        contents[51] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "不允许有未解析的实体引用“&{0};”。"};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "属性值中不允许有外部实体引用“&{0};”。"};
        contents[53] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "前缀“{0}”不能绑定到名称空间“{1}”。"};
        contents[54] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "元素“{0}”的局部名为空。"};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "属性“{0}”的局部名为空。"};
        contents[56] = new Object[]{"unbound-prefix-in-entity-reference", "实体节点“{0}”的替代文本中包含元素节点“{1}”，该节点具有未绑定的前缀“{2}”。"};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "实体节点“{0}”的替代文本中包含属性节点“{1}”，该节点具有未绑定的前缀“{2}”。"};
        return contents;
    }
}
