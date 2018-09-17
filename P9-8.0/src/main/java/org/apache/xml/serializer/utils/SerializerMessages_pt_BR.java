package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_pt_BR extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "A chave de mensagem ''{0}'' não está na classe de mensagem ''{1}''"};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "O formato da mensagem ''{0}'' na classe de mensagem ''{1}'' falhou."};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "A classe de serializador ''{0}'' não implementa org.xml.sax.ContentHandler."};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "O recurso [ {0} ] não pôde ser encontrado.\n{1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "O recurso [ {0} ] não pôde carregar: {1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "Tamanho do buffer <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "Detectado substituto UTF-16 inválido: {0} ?"};
        contents[7] = new Object[]{"ER_OIERROR", "Erro de E/S"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "Impossível incluir atributo {0} depois de nós filhos ou antes da geração de um elemento. O atributo será ignorado."};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "O espaço de nomes do prefixo ''{0}'' não foi declarado. "};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "Atributo ''{0}'' fora do elemento. "};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "Declaração de espaço de nomes ''{0}''=''{1}'' fora do elemento. "};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "Não foi possível carregar ''{0}'' (verifique CLASSPATH) agora , utilizando somente os padrões"};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "Tentativa de processar o caractere de um valor integral {0} que não é representado na codificação de saída especificada de {1}."};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "Não foi possível carregar o arquivo de propriedade ''{0}'' para o método de saída ''{1}'' (verifique CLASSPATH)"};
        contents[15] = new Object[]{"ER_INVALID_PORT", "Número de porta inválido"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "A porta não pode ser definida quando o host é nulo"};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "O host não é um endereço formado corretamente"};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "O esquema não está em conformidade."};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "Impossível definir esquema a partir da cadeia nula"};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "O caminho contém seqüência de escape inválida"};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "O caminho contém caractere inválido: {0}"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "O fragmento contém caractere inválido"};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "O fragmento não pode ser definido quando o caminho é nulo"};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "O fragmento só pode ser definido para um URI genérico"};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "Nenhum esquema encontrado no URI"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "Impossível inicializar URI com parâmetros vazios"};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "O fragmento não pode ser especificado no caminho e fragmento"};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "A cadeia de consulta não pode ser especificada na cadeia de consulta e caminho"};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "Port não pode ser especificado se host não for especificado"};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "Userinfo não pode ser especificado se host não for especificado"};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "Aviso:  A versão do documento de saída precisa ser ''{0}''.  Essa versão do XML não é suportada. A versão do documento de saída será ''1.0''."};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "O esquema é obrigatório!"};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "O objeto Properties transmitido para SerializerFactory não tem uma propriedade ''{0}''."};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "Aviso:  A codificação ''{0}'' não é suportada pelo Java Runtime."};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "O parâmetro ''{0}'' não é reconhecido."};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "O parâmetro ''{0}'' é reconhecido, mas o valor pedido não pode ser definido. "};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "A cadeia resultante é muito longa para caber em uma DOMString: ''{0}''. "};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "O tipo de valor para este nome de parâmetro é incompatível com o tipo de valor esperado. "};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "O destino de saída para os dados a serem gravados era nulo. "};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "Uma codificação não suportada foi encontrada. "};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "O nó não pôde ser serializado."};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "A Seção CDATA contém um ou mais marcadores de término ']]>'."};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "Uma instância do verificador Well-Formedness não pôde ser criada. O parâmetro well-formed foi definido como true, mas a verificação well-formedness não pode ser executada."};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "O nó ''{0}'' contém caracteres XML inválidos. "};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "Um caractere XML inválido (Unicode: 0x{0}) foi encontrado no comentário. "};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "Um caractere XML inválido (Unicode: 0x{0}) foi encontrado no processo instructiondata."};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "Um caractere XML inválido (Unicode: 0x{0}) foi encontrado nos conteúdos do CDATASection. "};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "Um caractere XML inválido (Unicode: 0x{0}) foi encontrado no conteúdo dos dados de caractere dos nós. "};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "Um caractere inválido foi encontrado no {0} do nó denominado ''{1}''."};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "A cadeia \"--\" não é permitida dentro dos comentários. "};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "O valor do atributo \"{1}\" associado a um tipo de elemento \"{0}\" não deve conter o caractere ''<''. "};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "A referência de entidade não analisada \"&{0};\" não é permitida. "};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "A referência de entidade externa \"&{0};\" não é permitida em um valor de atributo. "};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "O prefixo \"{0}\" não pode ser vinculado ao espaço de nomes \"{1}\"."};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "O nome local do elemento \"{0}\" é nulo."};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "O nome local do atributo \"{0}\" é nulo."};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "O texto de substituição do nó de entidade \"{0}\" contém um nó de elemento \"{1}\" com um prefixo não vinculado \"{2}\"."};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "O texto de substituição do nó de entidade \"{0}\" contém um nó de atributo \"{1}\" com um prefixo não vinculado \"{2}\"."};
        return contents;
    }
}
