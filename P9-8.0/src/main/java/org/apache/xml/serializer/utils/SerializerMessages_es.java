package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_es extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "La clave de mensaje ''{0}'' no está en la clase de mensaje ''{1}''"};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "Se ha producido un error en el formato de mensaje ''{0}'' de la clase de mensaje ''{1}''."};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "La clase serializer ''{0}'' no implementa org.xml.sax.ContentHandler."};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "No se ha podido encontrar el recurso [ {0} ].\n {1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "No se ha podido cargar el recurso [ {0} ]: {1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "Tamaño de almacenamiento intermedio <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "¿Se ha detectado un sustituto UTF-16 no válido: {0}?"};
        contents[7] = new Object[]{"ER_OIERROR", "Error de ES"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "No se puede añadir el atributo {0} después de nodos hijo o antes de que se produzca un elemento.  Se ignorará el atributo."};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "No se ha declarado el espacio de nombres para el prefijo ''{0}''."};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "Atributo ''{0}'' fuera del elemento."};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "Declaración del espacio de nombres ''{0}''=''{1}'' fuera del elemento."};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "No se ha podido cargar ''{0}'' (compruebe la CLASSPATH), ahora sólo se están utilizando los valores predeterminados"};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "Se ha intentado dar salida a un carácter del valor integral {0} que no está representado en la codificación de salida especificada de {1}."};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "No se ha podido cargar el archivo de propiedades ''{0}'' para el método de salida ''{1}'' (compruebe la CLASSPATH)"};
        contents[15] = new Object[]{"ER_INVALID_PORT", "Número de puerto no válido"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "No se puede establecer el puerto si el sistema principal es nulo"};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "El sistema principal no es una dirección bien formada"};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "El esquema no es compatible."};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "No se puede establecer un esquema de una serie nula"};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "La vía de acceso contiene una secuencia de escape no válida"};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "La vía de acceso contiene un carácter no válido: {0}"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "El fragmento contiene un carácter no válido"};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "No se puede establecer el fragmento si la vía de acceso es nula"};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "Sólo se puede establecer el fragmento para un URI genérico"};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "No se ha encontrado un esquema en el URI"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "No se puede inicializar el URI con parámetros vacíos"};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "No se puede especificar el fragmento en la vía de acceso y en el fragmento"};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "No se puede especificar la serie de consulta en la vía de acceso y en la serie de consulta"};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "No se puede especificar el puerto si no se ha especificado el sistema principal"};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "No se puede especificar la información de usuario si no se ha especificado el sistema principal"};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "Aviso: la versión del documento de salida tiene que ser ''{0}''.  No se admite esta versión de XML.  La versión del documento de salida será ''1.0''."};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "¡Se necesita un esquema!"};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "El objeto Properties pasado a SerializerFactory no tiene una propiedad ''{0}''."};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "Aviso: La codificación ''{0}'' no está soportada por Java Runtime."};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "El parámetro ''{0}'' no se reconoce."};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "Se reconoce el parámetro ''{0}'' pero no puede establecerse el valor solicitado."};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "La serie producida es demasiado larga para ajustarse a DOMString: ''{0}''."};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "El tipo de valor para este nombre de parámetro es incompatible con el tipo de valor esperado."};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "El destino de salida de escritura de los datos es nulo."};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "Se ha encontrado una codificación no soportada."};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "No se ha podido serializar el nodo."};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "La sección CDATA contiene uno o más marcadores ']]>' de terminación."};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "No se ha podido crear una instancia del comprobador de gramática correcta.  El parámetro well-formed se ha establecido en true pero no se puede realizar la comprobación de gramática correcta."};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "El nodo ''{0}'' contiene caracteres XML no válidos."};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "Se ha encontrado un carácter XML no válido (Unicode: 0x{0}) en el comentario."};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "Se ha encontrado un carácter XML no válido (Unicode: 0x{0}) en los datos de la instrucción de proceso."};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "Se ha encontrado un carácter XML no válido (Unicode: 0x{0}) en el contenido de CDATASection."};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "Se ha encontrado un carácter XML no válido (Unicode: 0x{0}) en el contenido de datos de caracteres del nodo."};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "Se ha encontrado un carácter o caracteres XML no válidos en el nodo {0} denominado ''{1}''."};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "No se permite la serie \"--\" dentro de los comentarios."};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "El valor del atributo \"{1}\" asociado a un tipo de elemento \"{0}\" no debe contener el carácter ''''<''''."};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "No se permite la referencia de entidad no analizada \"&{0};\"."};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "La referencia de entidad externa \"&{0};\" no está permitida en un valor de atributo."};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "No se puede encontrar el prefijo \"{0}\" en el espacio de nombres \"{1}\"."};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "El nombre local del elemento \"{0}\" es null."};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "El nombre local del atributo \"{0}\" es null."};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "El texto de sustitución del nodo de entidad \"{0}\" contiene un nodo de elemento \"{1}\" con un prefijo no enlazado \"{2}\"."};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "El texto de sustitución del nodo de entidad \"{0}\" contiene un nodo de atributo \"{1}\" con un prefijo no enlazado \"{2}\"."};
        return contents;
    }
}
