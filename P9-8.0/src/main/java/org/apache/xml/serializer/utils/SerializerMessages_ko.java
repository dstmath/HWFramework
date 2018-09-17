package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_ko extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "''{0}'' 메시지 키가 ''{1}'' 메시지 클래스에 없습니다."};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "''{1}'' 메시지 클래스에 있는 ''{0}'' 메시지의 형식이 잘못 되었습니다."};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "''{0}'' 직렬 프로그램 클래스가 org.xml.sax.ContentHandler를 구현하지 않습니다."};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "[ {0} ] 자원을 찾을 수 없습니다.\n{1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "[ {0} ] 자원이 {1} \n {2} \t {3}을(를) 로드할 수 없습니다."};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "버퍼 크기 <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "잘못된 UTF-16 대리자(surrogate)가 발견되었습니다: {0} ?"};
        contents[7] = new Object[]{"ER_OIERROR", "IO 오류"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "하위 노드가 생성된 이후 또는 요소가 작성되기 이전에 {0} 속성을 추가할 수 없습니다. 속성이 무시됩니다."};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "''{0}'' 접두부에 대한 이름 공간이 선언되지 않았습니다."};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "''{0}'' 속성이 요소의 외부에 있습니다."};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "''{0}''=''{1}'' 이름 공간 선언이 요소의 외부에 있습니다."};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "''{0}''(CLASSPATH 확인)을(를) 로드할 수 없으므로, 현재 기본값만을 사용하는 중입니다."};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "{1}의 지정된 출력 인코딩에 표시되지 않은 무결성 값 {0}의 문자를 출력하십시오. "};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "''{1}'' 출력 메소드(CLASSPATH 확인)에 대한 ''{0}'' 특성 파일을 로드할 수 없습니다."};
        contents[15] = new Object[]{"ER_INVALID_PORT", "잘못된 포트 번호"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "호스트가 널(null)이면 포트를 설정할 수 없습니다."};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "호스트가 완전한 주소가 아닙니다."};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "스키마가 일치하지 않습니다."};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "널(null) 문자열에서 스키마를 설정할 수 없습니다."};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "경로에 잘못된 이스케이프 순서가 있습니다."};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "경로에 잘못된 문자가 있습니다: {0}"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "단편에 잘못된 문자가 있습니다."};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "경로가 널(null)이면 단편을 설정할 수 없습니다."};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "일반 URI에 대해서만 단편을 설정할 수 있습니다."};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "URI에 스키마가 없습니다."};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "빈 매개변수로 URI를 초기화할 수 없습니다."};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "경로 및 단편 둘 다에 단편을 지정할 수 없습니다."};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "경로 및 조회 문자열에 조회 문자열을 지정할 수 없습니다."};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "호스트를 지정하지 않은 경우에는 포트를 지정할 수 없습니다."};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "호스트를 지정하지 않은 경우에는 Userinfo를 지정할 수 없습니다."};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "경고:  요청된 출력 문서의 버전은 ''{0}''입니다. 하지만 이 XML 버전은 지원되지 않습니다. 출력 문서의 버전은 ''1.0''이 됩니다."};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "스키마가 필요합니다."};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "SerializerFactory에 전달된 특성 오브젝트에 ''{0}'' 특성이 없습니다."};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "경고: ''{0}'' 인코딩은 Java Runtime을 지원하지 않습니다."};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "''{0}'' 매개변수를 인식할 수 없습니다."};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "''{0}'' 매개변수는 인식할 수 있으나 요청된 값을 설정할 수 없습니다."};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "결과 문자열이 너무 길어 DOMString에 맞지 않습니다: ''{0}'' "};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "이 매개변수 이름에 대한 값 유형이 예상 값 유형과 호환되지 않습니다."};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "데이터를 기록할 출력 대상이 널(null)입니다."};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "지원되지 않는 인코딩이 발견되었습니다."};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "노드를 직렬화할 수 없습니다."};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "CDATA 섹션에 종료 표시 문자인 ']]>'가 하나 이상 포함되어 있습니다."};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "Well-Formedness 검사기의 인스턴스를 작성할 수 없습니다. Well-Formed 매개변수가 true로 설정되었지만 Well-Formedness 검사를 수행할 수 없습니다."};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "''{0}'' 노드에 유효하지 않은 XML 문자가 있습니다."};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "설명에 유효하지 않은 XML 문자(Unicode: 0x{0})가 있습니다. "};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "처리 명령어 데이터에 유효하지 않은 XML 문자(Unicode: 0x{0})가 있습니다 "};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "CDATASection의 내용에 유효하지 않은 XML 문자(Unicode: 0x{0})가 있습니다. "};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "노드의 문자 데이터 내용에 유효하지 않은 XML 문자(Unicode: 0x{0})가 있습니다. "};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "이름이 ''{1}''인 {0} 노드에 유효하지 않은 XML 문자가 있습니다. "};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "설명 내에서는 \"--\" 문자열이 허용되지 않습니다."};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "\"{0}\" 요소 유형과 연관된 \"{1}\" 속성값에 ''<'' 문자가 포함되면 안됩니다."};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "\"&{0};\"의 구분 분석되지 않은 엔티티 참조는 허용되지 않습니다. "};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "속성값에는 \"&{0};\" 외부 엔티티 참조가 허용되지 않습니다. "};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "\"{0}\" 접두부를 \"{1}\" 이름 공간에 바인드할 수 없습니다."};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "\"{0}\" 요소의 로컬 이름이 널(null)입니다."};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "\"{0}\" 속성의 로컬 이름이 널(null)입니다."};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "\"{0}\" 엔티티 노드의 대체 텍스트에 바인드되지 않은 접두부 \"{2}\"을(를) 갖는 \"{1}\" 요소 노드가 있습니다."};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "\"{0}\" 엔티티 노드의 대체 텍스트에 바인드되지 않은 접두부 \"{2}\"을(를) 갖는 \"{1}\" 속성 노드가 있습니다."};
        return contents;
    }
}
