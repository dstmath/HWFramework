package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_zh_TW extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "訊息鍵 ''{0}'' 不在訊息類別 ''{1}'' 中"};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "訊息類別 ''{1}'' 中的訊息 ''{0}'' 格式化失敗。"};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "Serializer 類別 ''{0}'' 不實作 org.xml.sax.ContentHandler。"};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "找不到資源 [ {0} ]。\n {1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "無法載入資源 [ {0} ]：{1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "緩衝區大小 <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "偵測到無效的 UTF-16 代理：{0}?"};
        contents[7] = new Object[]{"ER_OIERROR", "IO 錯誤"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "在產生子項節點之後，或在產生元素之前，不可新增屬性 {0}。屬性會被忽略。"};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "字首 ''{0}'' 的名稱空間尚未宣告。"};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "屬性 ''{0}'' 超出元素外。"};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "名稱空間宣告 ''{0}''=''{1}'' 超出元素外。"};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "無法載入 ''{0}''（檢查 CLASSPATH），目前只使用預設值"};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "試圖輸出不是以指定的輸出編碼 {1} 呈現的整數值 {0} 的字元。"};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "無法載入輸出方法 ''{1}''（檢查 CLASSPATH）的內容檔 ''{0}''"};
        contents[15] = new Object[]{"ER_INVALID_PORT", "無效的埠編號"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "主機為空值時，無法設定埠"};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "主機沒有完整的位址"};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "綱要不是 conformant。"};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "無法從空字串設定綱要"};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "路徑包含無效的跳脫字元"};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "路徑包含無效的字元：{0}"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "片段包含無效的字元"};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "路徑為空值時，無法設定片段"};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "只能對通用的 URI 設定片段"};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "在 URI 找不到綱要"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "無法以空白參數起始設定 URI"};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "片段無法同時在路徑和片段中指定"};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "在路徑及查詢字串中不可指定查詢字串"};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "如果沒有指定主機，不可指定埠"};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "如果沒有指定主機，不可指定 Userinfo"};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "警告：輸出文件的版本要求是 ''{0}''。未支援這個版本的 XML。輸出文件的版本會是 ''1.0''。"};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "綱要是必需的！"};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "傳遞到 SerializerFactory 的 Properties 物件沒有 ''{0}'' 內容。"};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "警告：Java 執行時期不支援編碼 ''{0}''。"};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "無法辨識參數 ''{0}''。"};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "可辨識 ''{0}'' 參數，但所要求的值無法設定。"};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "結果字串過長，無法置入 DOMString: ''{0}'' 中。"};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "這個參數名稱的值類型與期望值類型不相容。"};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "資料要寫入的輸出目的地為空值。"};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "發現不支援的編碼。"};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "節點無法序列化。"};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "CDATA 區段包含一或多個終止標記 ']]>'。"};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "無法建立「形式完整」檢查程式的實例。Well-formed 參數雖設為 true，但無法執行形式完整檢查。"};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "節點 ''{0}'' 包含無效的 XML 字元。"};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "在註解中發現無效的 XML 字元 (Unicode: 0x{0})。"};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "在處理程序 instructiondata 中發現無效的 XML 字元 (Unicode: 0x{0})。"};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "在 CDATASection 的內容中發現無效的 XML 字元 (Unicode: 0x{0})。"};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "在節點的字元資料內容中發現無效的 XML 字元 (Unicode: 0x{0})。"};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "在名為 ''{1}'' 的 ''{0}'' 中發現無效的 XML 字元。"};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "註解中不允許使用字串 \"--\"。"};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "與元素類型 \"{0}\" 相關聯的屬性 \"{1}\" 值不可包含 ''<'' 字元。"};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "不允許使用未剖析的實體參照 \"&{0};\"。"};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "屬性值中不允許使用外部實體參照 \"&{0};\"。"};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "字首 \"{0}\" 無法連結到名稱空間 \"{1}\"。"};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "元素 \"{0}\" 的本端名稱是空值。"};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "屬性 \"{0}\" 的本端名稱是空值。"};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "實體節點 \"{0}\" 的取代文字包含附有已切斷連結字首 \"{2}\" 的元素節點 \"{1}\"。"};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "實體節點 \"{0}\" 的取代文字包含附有已切斷連結字首 \"{2}\" 的屬性節點 \"{1}\"。"};
        return contents;
    }
}
