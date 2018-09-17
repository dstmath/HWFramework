package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_ja extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "メッセージ・キー ''{0}'' はメッセージ・クラス ''{1}'' にありません。"};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "メッセージ・クラス ''{1}'' のメッセージ ''{0}'' のフォーマット設定が失敗しました。"};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "シリアライザー・クラス ''{0}'' は org.xml.sax.ContentHandler を実装しません。"};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "リソース [ {0} ] は見つかりませんでした。\n {1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "リソース [ {0} ] をロードできませんでした: {1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "バッファー・サイズ <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "無効な UTF-16 サロゲートが検出されました: {0} ?"};
        contents[7] = new Object[]{"ER_OIERROR", "入出力エラー"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "下位ノードの後または要素が生成される前に属性 {0} は追加できません。  属性は無視されます。"};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "接頭部 ''{0}'' の名前空間が宣言されていません。"};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "属性 ''{0}'' が要素の外側です。"};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "名前空間宣言 ''{0}''=''{1}'' が要素の外側です。"};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "''{0}'' をロードできませんでした (CLASSPATH を確認してください)。現在はデフォルトのもののみを使用しています。"};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "{1} の指定された出力エンコードで表せない整数値 {0} の文字の出力を試みました。"};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "出力メソッド ''{1}'' のプロパティー・ファイル ''{0}'' をロードできませんでした (CLASSPATH を確認してください)"};
        contents[15] = new Object[]{"ER_INVALID_PORT", "無効なポート番号"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "ホストがヌルであるとポートを設定できません"};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "ホストはうまく構成されたアドレスでありません"};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "スキームは一致していません。"};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "ヌル・ストリングからはスキームを設定できません"};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "パスに無効なエスケープ・シーケンスが含まれています"};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "パスに無効文字: {0} が含まれています"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "フラグメントに無効文字が含まれています"};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "パスがヌルであるとフラグメントを設定できません"};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "総称 URI のフラグメントしか設定できません"};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "スキームは URI で見つかりません"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "URI は空のパラメーターを使用して初期化できません"};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "フラグメントはパスとフラグメントの両方に指定できません"};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "照会ストリングはパスおよび照会ストリング内に指定できません"};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "ホストが指定されていない場合はポートを指定してはいけません"};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "ホストが指定されていない場合は Userinfo を指定してはいけません"};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "警告: 出力文書のバージョンとして ''{0}'' が要求されました。  このバージョンの XML はサポートされません。  出力文書のバージョンは ''1.0'' になります。"};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "スキームが必要です。"};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "SerializerFactory に渡された Properties オブジェクトには ''{0}'' プロパティーがありません。"};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "警告:  エンコード ''{0}'' はこの Java ランタイムではサポートされていません。"};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "パラメーター ''{0}'' は認識されません。"};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "パラメーター ''{0}'' は認識されますが、要求された値は設定できません。"};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "結果のストリングが長すぎるため、DOMString 内に収まりません: ''{0}''。"};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "このパラメーター名の値の型は、期待される値の型と不適合です。"};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "書き込まれるデータの出力宛先がヌルです。"};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "サポートされないエンコードが検出されました。"};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "ノードを直列化できませんでした。"};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "CDATA セクションに 1 つ以上の終了マーカー ']]>' が含まれています。"};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "整形式性チェッカーのインスタンスを作成できませんでした。  well-formed パラメーターの設定は true でしたが、整形式性の検査は実行できません。"};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "ノード ''{0}'' に無効な XML 文字があります。"};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "コメントの中に無効な XML 文字 (Unicode: 0x{0}) が見つかりました。"};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "処理命令データの中に無効な XML 文字 (Unicode: 0x{0}) が見つかりました。"};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "CDATA セクションの中に無効な XML 文字 (Unicode: 0x{0}) が見つかりました。"};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "ノードの文字データの内容に無効な XML 文字 (Unicode: 0x{0}) が見つかりました。"};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "''{1}'' という名前の {0} ノードの中に無効な XML 文字が見つかりました。"};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "ストリング \"--\" はコメント内では使用できません。"};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "要素型 \"{0}\" に関連した属性 \"{1}\" の値には ''<'' 文字を含めてはいけません。"};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "解析対象外実体参照 \"&{0};\" は許可されません。"};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "属性値での外部実体参照 \"&{0};\" は許可されません。"};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "接頭部 \"{0}\" は名前空間 \"{1}\" に結合できません。"};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "要素 \"{0}\" のローカル名がヌルです。"};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "属性 \"{0}\" のローカル名がヌルです。"};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "実体ノード \"{0}\" の置換テキストに、未結合の接頭部 \"{2}\" を持つ要素ノード \"{1}\" が含まれています。"};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "実体ノード \"{0}\" の置換テキストに、未結合の接頭部 \"{2}\" を持つ属性ノード \"{1}\" が含まれています。"};
        return contents;
    }
}
