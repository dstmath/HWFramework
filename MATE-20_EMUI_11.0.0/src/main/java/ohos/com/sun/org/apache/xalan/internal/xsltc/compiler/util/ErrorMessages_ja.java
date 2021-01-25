package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import java.util.ListResourceBundle;

public class ErrorMessages_ja extends ListResourceBundle {
    @Override // java.util.ListResourceBundle
    public Object[][] getContents() {
        return new Object[][]{new Object[]{ErrorMsg.MULTIPLE_STYLESHEET_ERR, "同じファイルに複数のスタイルシートが定義されています。"}, new Object[]{ErrorMsg.TEMPLATE_REDEF_ERR, "テンプレート''{0}''はこのスタイルシート内ですでに定義されています。"}, new Object[]{ErrorMsg.TEMPLATE_UNDEF_ERR, "テンプレート''{0}''はこのスタイルシート内で定義されていません。"}, new Object[]{ErrorMsg.VARIABLE_REDEF_ERR, "変数''{0}''は同じスコープ内で複数定義されています。"}, new Object[]{ErrorMsg.VARIABLE_UNDEF_ERR, "変数またはパラメータ''{0}''が未定義です。"}, new Object[]{ErrorMsg.CLASS_NOT_FOUND_ERR, "クラス''{0}''が見つかりません。"}, new Object[]{ErrorMsg.METHOD_NOT_FOUND_ERR, "外部メソッド''{0}''が見つかりません(publicである必要があります)。"}, new Object[]{ErrorMsg.ARGUMENT_CONVERSION_ERR, "メソッド''{0}''の呼出しの引数タイプまたは戻り型を変換できません"}, new Object[]{ErrorMsg.FILE_NOT_FOUND_ERR, "ファイルまたはURI ''{0}''が見つかりません。"}, new Object[]{ErrorMsg.INVALID_URI_ERR, "URI ''{0}''が無効です。"}, new Object[]{ErrorMsg.FILE_ACCESS_ERR, "ファイルまたはURI ''{0}''を開くことができません。"}, new Object[]{ErrorMsg.MISSING_ROOT_ERR, "<xsl:stylesheet>または<xsl:transform>の要素がありません。"}, new Object[]{ErrorMsg.NAMESPACE_UNDEF_ERR, "ネームスペースの接頭辞''{0}''は宣言されていません。"}, new Object[]{ErrorMsg.FUNCTION_RESOLVE_ERR, "関数''{0}''の呼出しを解決できません。"}, new Object[]{ErrorMsg.NEED_LITERAL_ERR, "''{0}''への引数はリテラル文字列である必要があります。"}, new Object[]{ErrorMsg.XPATH_PARSER_ERR, "XPath式''{0}''の解析中にエラーが発生しました。"}, new Object[]{ErrorMsg.REQUIRED_ATTR_ERR, "必須属性''{0}''がありません。"}, new Object[]{ErrorMsg.ILLEGAL_CHAR_ERR, "XPath式の文字''{0}''は無効です。"}, new Object[]{ErrorMsg.ILLEGAL_PI_ERR, "処理命令の名前''{0}''は無効です。"}, new Object[]{"STRAY_ATTRIBUTE_ERR", "属性''{0}''が要素の外側にあります。"}, new Object[]{ErrorMsg.ILLEGAL_ATTRIBUTE_ERR, "不正な属性''{0}''です。"}, new Object[]{ErrorMsg.CIRCULAR_INCLUDE_ERR, "インポートまたはインクルードが循環しています。スタイルシート''{0}''はすでにロードされています。"}, new Object[]{ErrorMsg.RESULT_TREE_SORT_ERR, "結果ツリー・フラグメントはソートできません(<xsl:sort>要素は無視されます)。結果ツリーを作成するときにノードをソートする必要があります。"}, new Object[]{ErrorMsg.SYMBOLS_REDEF_ERR, "10進数フォーマット''{0}''はすでに定義されています。"}, new Object[]{ErrorMsg.XSL_VERSION_ERR, "XSLバージョン''{0}''はXSLTCによってサポートされていません。"}, new Object[]{ErrorMsg.CIRCULAR_VARIABLE_ERR, "''{0}''内の変数参照またはパラメータ参照が循環しています。"}, new Object[]{ErrorMsg.ILLEGAL_BINARY_OP_ERR, "2進数の式に対する不明な演算子です。"}, new Object[]{ErrorMsg.ILLEGAL_ARG_ERR, "関数呼出しの引数が不正です。"}, new Object[]{ErrorMsg.DOCUMENT_ARG_ERR, "document()関数の2番目の引数はノードセットである必要があります。"}, new Object[]{ErrorMsg.MISSING_WHEN_ERR, "<xsl:choose>内には少なくとも1つの<xsl:when>要素が必要です。"}, new Object[]{ErrorMsg.MULTIPLE_OTHERWISE_ERR, "<xsl:choose>内では1つの<xsl:otherwise>要素のみが許可されています。"}, new Object[]{ErrorMsg.STRAY_OTHERWISE_ERR, "<xsl:otherwise>は<xsl:choose>内でのみ使用できます。"}, new Object[]{ErrorMsg.STRAY_WHEN_ERR, "<xsl:when>は<xsl:choose>内でのみ使用できます。"}, new Object[]{ErrorMsg.WHEN_ELEMENT_ERR, "<xsl:choose>内では<xsl:when>と<xsl:otherwise>の要素のみが許可されます。"}, new Object[]{ErrorMsg.UNNAMED_ATTRIBSET_ERR, "<xsl:attribute-set>に'name'属性がありません。"}, new Object[]{ErrorMsg.ILLEGAL_CHILD_ERR, "子要素が不正です。"}, new Object[]{ErrorMsg.ILLEGAL_ELEM_NAME_ERR, "要素''{0}''を呼び出すことはできません"}, new Object[]{ErrorMsg.ILLEGAL_ATTR_NAME_ERR, "属性''{0}''を呼び出すことはできません"}, new Object[]{ErrorMsg.ILLEGAL_TEXT_NODE_ERR, "テキスト・データはトップレベルの<xsl:stylesheet>要素の外側にあります。"}, new Object[]{ErrorMsg.SAX_PARSER_CONFIG_ERR, "JAXPパーサーが正しく構成されていません"}, new Object[]{ErrorMsg.INTERNAL_ERR, "リカバリ不能なXSLTC内部エラー: ''{0}''"}, new Object[]{"UNSUPPORTED_XSL_ERR", "XSL要素''{0}''はサポートされていません。"}, new Object[]{"UNSUPPORTED_EXT_ERR", "XSLTC拡張''{0}''は認識されません。"}, new Object[]{ErrorMsg.MISSING_XSLT_URI_ERR, "入力ドキュメントはスタイルシートではありません(XSLのネームスペースはルート要素内で宣言されていません)。"}, new Object[]{ErrorMsg.MISSING_XSLT_TARGET_ERR, "スタイルシート・ターゲット''{0}''が見つかりませんでした。"}, new Object[]{ErrorMsg.ACCESSING_XSLT_TARGET_ERR, "accessExternalStylesheetプロパティで設定された制限により''{1}''アクセスが許可されていないため、スタイルシート・ターゲット''{0}''を読み取れませんでした。"}, new Object[]{ErrorMsg.NOT_IMPLEMENTED_ERR, "''{0}''が実装されていません。"}, new Object[]{ErrorMsg.NOT_STYLESHEET_ERR, "入力ドキュメントにXSLスタイルシートが含まれていません。"}, new Object[]{ErrorMsg.ELEMENT_PARSE_ERR, "要素''{0}''を解析できませんでした"}, new Object[]{ErrorMsg.KEY_USE_ATTR_ERR, "<key>のuse属性は、ノード、ノードセット、文字列または数値である必要があります。"}, new Object[]{ErrorMsg.OUTPUT_VERSION_ERR, "出力XMLドキュメントのバージョンは1.0である必要があります"}, new Object[]{ErrorMsg.ILLEGAL_RELAT_OP_ERR, "関係式の不明な演算子です"}, new Object[]{ErrorMsg.ATTRIBSET_UNDEF_ERR, "存在しない属性セット''{0}''を使用しようとしました。"}, new Object[]{ErrorMsg.ATTR_VAL_TEMPLATE_ERR, "属性値テンプレート''{0}''を解析できません。"}, new Object[]{ErrorMsg.UNKNOWN_SIG_TYPE_ERR, "クラス''{0}''の署名に不明なデータ型があります。"}, new Object[]{"DATA_CONVERSION_ERR", "データ型''{0}''を''{1}''に変換できません。"}, new Object[]{ErrorMsg.NO_TRANSLET_CLASS_ERR, "このテンプレートには有効なtransletクラス定義が含まれていません。"}, new Object[]{ErrorMsg.NO_MAIN_TRANSLET_ERR, "このテンプレートには名前''{0}''を持つクラスが含まれていません。"}, new Object[]{ErrorMsg.TRANSLET_CLASS_ERR, "transletクラス''{0}''をロードできませんでした。"}, new Object[]{ErrorMsg.TRANSLET_OBJECT_ERR, "Transletクラスがロードされましたが、transletインスタンスを作成できません。"}, new Object[]{ErrorMsg.ERROR_LISTENER_NULL_ERR, "''{0}''のErrorListenerをnullに設定しようとしました"}, new Object[]{ErrorMsg.JAXP_UNKNOWN_SOURCE_ERR, "StreamSource、SAXSourceおよびDOMSourceのみがXSLTCによってサポートされています"}, new Object[]{ErrorMsg.JAXP_NO_SOURCE_ERR, "''{0}''に渡されたソース・オブジェクトにコンテンツがありません。"}, new Object[]{ErrorMsg.JAXP_COMPILE_ERR, "スタイルシートをコンパイルできませんでした"}, new Object[]{ErrorMsg.JAXP_INVALID_ATTR_ERR, "TransformerFactoryは属性''{0}''を認識しません。"}, new Object[]{ErrorMsg.JAXP_INVALID_ATTR_VALUE_ERR, "''{0}''属性に指定された値が正しくありません。"}, new Object[]{ErrorMsg.JAXP_SET_RESULT_ERR, "setResult()はstartDocument()よりも前に呼び出す必要があります。"}, new Object[]{ErrorMsg.JAXP_NO_TRANSLET_ERR, "トランスフォーマにはカプセル化されたtransletオブジェクトがありません。"}, new Object[]{ErrorMsg.JAXP_NO_HANDLER_ERR, "変換結果に対して定義済の出力ハンドラがありません。"}, new Object[]{ErrorMsg.JAXP_NO_RESULT_ERR, "''{0}''に渡された結果オブジェクトは無効です。"}, new Object[]{ErrorMsg.JAXP_UNKNOWN_PROP_ERR, "無効なトランスフォーマ・プロパティ''{0}''にアクセスしようとしました。"}, new Object[]{ErrorMsg.SAX2DOM_ADAPTER_ERR, "SAX2DOMアダプタ''{0}''を作成できませんでした。"}, new Object[]{ErrorMsg.XSLTC_SOURCE_ERR, "systemIdを設定せずにXSLTCSource.build()が呼び出されました。"}, new Object[]{"ER_RESULT_NULL", "結果はnullにできません"}, new Object[]{ErrorMsg.JAXP_INVALID_SET_PARAM_VALUE, "パラメータ{0}は有効なJavaオブジェクトである必要があります"}, new Object[]{ErrorMsg.COMPILE_STDIN_ERR, "-iオプションは-oオプションとともに使用する必要があります。"}, new Object[]{ErrorMsg.COMPILE_USAGE_STR, "形式\n   java ohos.com.sun.org.apache.xalan.internal.xsltc.cmdline.Compile [-o <output>]\n      [-d <directory>] [-j <jarfile>] [-p <package>]\n      [-n] [-x] [-u] [-v] [-h] { <stylesheet> | -i }\n\nOPTIONS\n   -o <output>    名前<output>を生成済transletに\n                  割り当てる。デフォルトでは、translet名は\n                  <stylesheet>名に由来します。このオプションは\n                  複数のスタイルシートをコンパイルする場合は無視されます。\n   -d <directory> transletの宛先ディレクトリを指定する\n   -j <jarfile>   <jarfile>で指定される名前のjarファイルにtransletクラスを\n                  パッケージする\n   -p <package>   生成されるすべてのtransletクラスのパッケージ名\n                  接頭辞を指定する。\n   -n             テンプレートのインライン化を有効にする(平均してデフォルト動作の方が\n                  優れています)。\n   -x             追加のデバッグ・メッセージ出力をオンにする\n   -u             <stylesheet>引数をURLとして解釈する\n   -i             スタイルシートをstdinから読み込むことをコンパイラに強制する\n   -v             コンパイラのバージョンを出力する\n   -h             この使用方法の文を出力する\n"}, new Object[]{ErrorMsg.TRANSFORM_USAGE_STR, "形式 \n   java ohos.com.sun.org.apache.xalan.internal.xsltc.cmdline.Transform [-j <jarfile>]\n      [-x] [-n <iterations>] {-u <document_url> | <document>}\n      <class> [<param1>=<value1> ...]\n\n   translet <class>を使用して、<document>で指定される\n   XMLドキュメントを変換する。translet <class>は\n   ユーザーのCLASSPATH内か、オプションで指定された<jarfile>内にあります。\nOPTIONS\n   -j <jarfile>    transletをロードするjarfileを指定する\n   -x              追加のデバッグ・メッセージ出力をオンにする\n   -n <iterations> 変換を<iterations>回実行し、\n                   プロファイリング情報を表示する\n   -u <document_url> XML入力ドキュメントをURLとして指定する\n"}, new Object[]{ErrorMsg.STRAY_SORT_ERR, "<xsl:sort>は<xsl:for-each>または<xsl:apply-templates>の内部でのみ使用できます。"}, new Object[]{ErrorMsg.UNSUPPORTED_ENCODING, "出力エンコーディング''{0}''はこのJVMではサポートされていません。"}, new Object[]{ErrorMsg.SYNTAX_ERR, "''{0}''に構文エラーがあります。"}, new Object[]{ErrorMsg.CONSTRUCTOR_NOT_FOUND, "外部コンストラクタ''{0}''が見つかりません。"}, new Object[]{ErrorMsg.NO_JAVA_FUNCT_THIS_REF, "staticでないJava関数''{0}''の最初の引数は無効なオブジェクト参照です。"}, new Object[]{ErrorMsg.TYPE_CHECK_ERR, "式''{0}''のタイプの確認中にエラーが発生しました。"}, new Object[]{ErrorMsg.TYPE_CHECK_UNK_LOC_ERR, "不明な場所での式のタイプの確認中にエラーが発生しました。"}, new Object[]{ErrorMsg.ILLEGAL_CMDLINE_OPTION_ERR, "コマンド行オプション''{0}''は無効です。"}, new Object[]{ErrorMsg.CMDLINE_OPT_MISSING_ARG_ERR, "コマンド行オプション''{0}''に必須の引数がありません。"}, new Object[]{ErrorMsg.WARNING_PLUS_WRAPPED_MSG, "WARNING:  ''{0}''\n       :{1}"}, new Object[]{ErrorMsg.WARNING_MSG, "WARNING:  ''{0}''"}, new Object[]{ErrorMsg.FATAL_ERR_PLUS_WRAPPED_MSG, "FATAL ERROR:  ''{0}''\n           :{1}"}, new Object[]{ErrorMsg.FATAL_ERR_MSG, "FATAL ERROR:  ''{0}''"}, new Object[]{ErrorMsg.ERROR_PLUS_WRAPPED_MSG, "ERROR:  ''{0}''\n     :{1}"}, new Object[]{ErrorMsg.ERROR_MSG, "ERROR:  ''{0}''"}, new Object[]{ErrorMsg.TRANSFORM_WITH_TRANSLET_STR, "translet ''{0}''を使用して変換します "}, new Object[]{ErrorMsg.TRANSFORM_WITH_JAR_STR, "translet ''{0}''を使用してjarファイル''{1}''から変換します"}, new Object[]{ErrorMsg.COULD_NOT_CREATE_TRANS_FACT, "TransformerFactoryクラス''{0}''のインスタンスを作成できませんでした。"}, new Object[]{ErrorMsg.TRANSLET_NAME_JAVA_CONFLICT, "名前''{0}''にはJavaクラスの名前に許可されていない文字が含まれているため、transletクラスの名前として使用できませんでした。名前''{1}''がかわりに使用されます。"}, new Object[]{ErrorMsg.COMPILER_ERROR_KEY, "コンパイラ・エラー:"}, new Object[]{ErrorMsg.COMPILER_WARNING_KEY, "コンパイラの警告:"}, new Object[]{ErrorMsg.RUNTIME_ERROR_KEY, "Transletエラー:"}, new Object[]{"INVALID_QNAME_ERR", "値が1つのQNameまたはQNameの空白文字区切りリストであることが必要な属性の値が''{0}''でした"}, new Object[]{"INVALID_NCNAME_ERR", "値がNCNameであることが必要な属性の値が''{0}''でした"}, new Object[]{ErrorMsg.INVALID_METHOD_IN_OUTPUT, "<xsl:output>要素のメソッド属性の値が''{0}''でした。値は''xml''、''html''、''text''またはqname-but-not-ncnameのいずれかである必要があります"}, new Object[]{ErrorMsg.JAXP_GET_FEATURE_NULL_NAME, "機能名はTransformerFactory.getFeature(String name)内でnullにできません。"}, new Object[]{ErrorMsg.JAXP_SET_FEATURE_NULL_NAME, "機能名はTransformerFactory.setFeature(String name, boolean value)内でnullにできません。"}, new Object[]{ErrorMsg.JAXP_UNSUPPORTED_FEATURE, "機能''{0}''をこのTransformerFactoryに設定できません。"}, new Object[]{ErrorMsg.JAXP_SECUREPROCESSING_FEATURE, "FEATURE_SECURE_PROCESSING: セキュリティ・マネージャが存在するとき、機能をfalseに設定できません。"}, new Object[]{ErrorMsg.OUTLINE_ERR_TRY_CATCH, "内部XSLTCエラー: 生成されたバイト・コードは、try-catch-finallyブロックを含んでいるため、アウトライン化できません。"}, new Object[]{ErrorMsg.OUTLINE_ERR_UNBALANCED_MARKERS, "内部XSLTCエラー: OutlineableChunkStartマーカーとOutlineableChunkEndマーカーは、対になっており、かつ正しくネストされている必要があります。"}, new Object[]{ErrorMsg.OUTLINE_ERR_DELETED_TARGET, "内部XSLTCエラー: アウトライン化されたバイト・コードのブロックの一部であった命令は、元のメソッドの中でまだ参照されています。"}, new Object[]{ErrorMsg.OUTLINE_ERR_METHOD_TOO_BIG, "内部XSLTCエラー: トランスレット内のメソッドが、Java仮想マシンの制限(1メソッドの長さは最大64キロバイト)を超えています。一般的に、スタイルシート内のテンプレートのサイズが大き過ぎることが原因として考えられます。小さいサイズのテンプレートを使用して、スタイルシートを再構成してください。"}, new Object[]{ErrorMsg.DESERIALIZE_TRANSLET_ERR, "Javaセキュリティが有効化されている場合、TemplatesImplのデシリアライズのサポートは無効化されます。これは、jdk.xml.enableTemplatesImplDeserializationシステム・プロパティをtrueに設定してオーバーライドできます。"}};
    }
}
