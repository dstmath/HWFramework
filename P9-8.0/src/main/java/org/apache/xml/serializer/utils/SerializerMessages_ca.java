package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_ca extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "La clau del missatge ''{0}'' no està a la classe del missatge ''{1}''"};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "El format del missatge ''{0}'' a la classe del missatge ''{1}'' ha fallat."};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "La classe de serialitzador ''{0}'' no implementa org.xml.sax.ContentHandler."};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "No s''ha trobat el recurs [ {0} ].\n {1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "No s''ha pogut carregar el recurs [ {0} ]: {1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "Grandària del buffer <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "S''ha detectat un suplent UTF-16 no vàlid: {0} ?"};
        contents[7] = new Object[]{"ER_OIERROR", "Error d'E/S"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "No es pot afegir l''atribut {0} després dels nodes subordinats o abans que es produeixi un element. Es passarà per alt l''atribut."};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "No s''ha declarat l''espai de noms pel prefix ''{0}''."};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "L''atribut ''{0}'' es troba fora de l''element."};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "La declaració de l''espai de noms ''{0}''=''{1}'' es troba fora de l''element."};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "No s''ha pogut carregar ''{0}'' (comproveu CLASSPATH), ara s''està fent servir els valors per defecte."};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "S''ha intentat un caràcter de sortida del valor integral {0} que no està representat a una codificació de sortida especificada de {1}."};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "No s''ha pogut carregar el fitxer de propietats ''{0}'' del mètode de sortida ''{1}'' (comproveu CLASSPATH)"};
        contents[15] = new Object[]{"ER_INVALID_PORT", "Número de port no vàlid"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "El port no es pot establir quan el sistema principal és nul"};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "El format de l'adreça del sistema principal no és el correcte"};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "L'esquema no té conformitat."};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "No es pot establir un esquema des d'una cadena nul·la"};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "La via d'accés conté una seqüència d'escapament no vàlida"};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "La via d''accés conté un caràcter no vàlid {0}"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "El fragment conté un caràcter no vàlid"};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "El fragment no es pot establir si la via d'accés és nul·la"};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "El fragment només es pot establir per a un URI genèric"};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "No s'ha trobat cap esquema a l'URI"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "No es pot inicialitzar l'URI amb paràmetres buits"};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "No es pot especificar un fragment tant en la via d'accés com en el fragment"};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "No es pot especificar una cadena de consulta en la via d'accés i la cadena de consulta"};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "No es pot especificar el port si no s'especifica el sistema principal"};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "No es pot especificar informació de l'usuari si no s'especifica el sistema principal"};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "Avís: la versió del document de sortida s''ha sol·licitat que sigui ''{0}''. Aquesta versió de XML no està suportada. La versió del document de sortida serà ''1.0''."};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "Es necessita l'esquema"};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "L''objecte de propietats passat a SerializerFactory no té cap propietat ''{0}''."};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "Avís: el temps d''execució de Java no dóna suport a la codificació ''{0}''."};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "El paràmetre ''{0}'' no es reconeix."};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "El paràmetre ''{0}'' es reconeix però el valor sol·licitat no es pot establir."};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "La cadena resultant és massa llarga per cabre en una DOMString: ''{0}''."};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "El tipus de valor per a aquest nom de paràmetre és incompatible amb el tipus de valor esperat."};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "La destinació de sortida per a les dades que s'ha d'escriure era nul·la."};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "S'ha trobat una codificació no suportada."};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "El node no s'ha pogut serialitzat."};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "La secció CDATA conté un o més marcadors d'acabament ']]>'."};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "No s'ha pogut crear cap instància per comprovar si té un format correcte o no. El paràmetre del tipus ben format es va establir en cert, però la comprovació de format no s'ha pogut realitzar."};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "El node ''{0}'' conté caràcters XML no vàlids."};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "S''ha trobat un caràcter XML no vàlid (Unicode: 0x{0}) en el comentari."};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "S''ha trobat un caràcter XML no vàlid (Unicode: 0x{0}) en les dades d''instrucció de procés."};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "S''ha trobat un caràcter XML no vàlid (Unicode: 0x''{0})'' en els continguts de la CDATASection."};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "S''ha trobat un caràcter XML no vàlid (Unicode: 0x''{0})'' en el contingut de dades de caràcter del node."};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "S''han trobat caràcters XML no vàlids al node {0} anomenat ''{1}''."};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "La cadena \"--\" no està permesa dins dels comentaris."};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "El valor d''atribut \"{1}\" associat amb un tipus d''element \"{0}\" no pot contenir el caràcter ''<''."};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "La referència de l''entitat no analitzada \"&{0};\" no està permesa."};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "La referència externa de l''entitat \"&{0};\" no està permesa en un valor d''atribut."};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "El prefix \"{0}\" no es pot vincular a l''espai de noms \"{1}\"."};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "El nom local de l''element \"{0}\" és nul."};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "El nom local d''atr \"{0}\" és nul."};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "El text de recanvi del node de l''entitat \"{0}\" conté un node d''element \"{1}\" amb un prefix de no enllaçat \"{2}\"."};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "El text de recanvi del node de l''entitat \"{0}\" conté un node d''atribut \"{1}\" amb un prefix de no enllaçat \"{2}\"."};
        return contents;
    }
}
