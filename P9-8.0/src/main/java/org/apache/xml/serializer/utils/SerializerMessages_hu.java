package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_hu extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "A(z) ''{0}'' üzenetkulcs nem található a(z) ''{1}'' üzenetosztályban."};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "A(z) ''{1}'' üzenetosztály ''{0}'' üzenetének formátuma hibás."};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "A(z) ''{0}'' példányosító osztály nem valósítja meg az org.xml.sax.ContentHandler függvényt."};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "A(z) [ {0} ] erőforrás nem található.\n {1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "A(z) [ {0} ] erőforrást nem lehet betölteni: {1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "Pufferméret <= 0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "Érvénytelen UTF-16 helyettesítés: {0} ?"};
        contents[7] = new Object[]{"ER_OIERROR", "IO hiba"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "Nem lehet {0} attribútumot hozzáadni utód csomópontok után vagy egy elem előállítása előtt.  Az attribútum figyelmen kívül marad."};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "A(z) ''{0}'' előtag névtere nincs deklarálva."};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "A(z) ''{0}'' attribútum kívül esik az elemen."};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "A(z) ''{0}''=''{1}'' névtérdeklaráció kívül esik az elemen."};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "Nem lehet betölteni ''{0}'' erőforrást (ellenőrizze a CLASSPATH beállítást), a rendszer az alapértelmezéseket használja."};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "Kísérletet tett {0} értékének karakteres kiírására, de nem jeleníthető meg a megadott {1} kimeneti kódolással."};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "Nem lehet betölteni a(z) ''{0}'' tulajdonságfájlt a(z) ''{1}'' metódushoz (ellenőrizze a CLASSPATH beállítást)"};
        contents[15] = new Object[]{"ER_INVALID_PORT", "Érvénytelen portszám"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "A portot nem állíthatja be, ha a hoszt null"};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "A hoszt nem jól formázott cím"};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "A séma nem megfelelő."};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "Nem lehet beállítani a sémát null karaktersorozatból"};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "Az elérési út érvénytelen vezérlő jelsorozatot tartalmaz"};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "Az elérési út érvénytelen karaktert tartalmaz: {0}"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "A töredék érvénytelen karaktert tartalmaz"};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "A töredéket nem állíthatja be, ha az elérési út null"};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "Csak általános URI-hoz állíthat be töredéket"};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "Nem található séma az URI-ban"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "Az URI nem inicializálható üres paraméterekkel"};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "Nem adhat meg töredéket az elérési útban és a töredékben is"};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "Nem adhat meg lekérdezési karaktersorozatot az elérési útban és a lekérdezési karaktersorozatban"};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "Nem adhatja meg a portot, ha nincs megadva hoszt"};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "Nem adhatja meg a felhasználói információkat, ha nincs megadva hoszt"};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "Figyelmeztetés: A kimeneti dokumentum kért verziója ''{0}''.  Az XML ezen verziója nem támogatott.  A kimeneti dokumentum verziója ''1.0'' lesz."};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "Sémára van szükség!"};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "A SerializerFactory osztálynak átadott Properties objektumnak nincs ''{0}'' tulajdonsága."};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "Figyelmeztetés: A(z) ''{0}'' kódolást nem támogatja a Java futási környezet."};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "A(z) ''{0}'' paraméter nem ismerhető fel."};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "A(z) ''{0}'' paraméter ismert, de a kért érték nem állítható be."};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "A létrejövő karaktersorozat túl hosszú, nem fér el egy DOMString-ben: ''{0}''."};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "A paraméternév értékének típusa nem kompatibilis a várt típussal."};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "Az adatkiírás céljaként megadott érték üres volt."};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "Nem támogatott kódolás."};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "A csomópont nem példányosítható."};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "A CDATA szakasz legalább egy ']]>' lezáró jelzőt tartalmaz."};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "A szabályos formázást ellenőrző példányt nem sikerült létrehozni.  A well-formed paraméter értéke true, de a szabályos formázást nem lehet ellenőrizni."};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "A(z) ''{0}'' csomópont érvénytelen XML karaktereket tartalmaz."};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "Érvénytelen XML karakter (Unicode: 0x{0}) szerepelt a megjegyzésben."};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "Érvénytelen XML karakter (Unicode: 0x{0}) szerepelt a feldolgozási utasításadatokban."};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "Érvénytelen XML karakter (Unicode: 0x{0}) szerepelt a CDATASection tartalmában."};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "Érvénytelen XML karakter (Unicode: 0x{0}) szerepelt a csomópont karakteradat tartalmában."};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "Érvénytelen XML karakter található a(z) ''{1}'' nevű {0} csomópontban."};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "A \"--\" karaktersorozat nem megengedett a megjegyzésekben."};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "A(z) \"{0}\" elemtípussal társított \"{1}\" attribútum értéke nem tartalmazhat ''<'' karaktert."};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "Az értelmezés nélküli \"&{0};\" entitáshivatkozás nem megengedett."};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "A(z) \"&{0};\" külső entitáshivatkozás nem megengedett egy attribútumértékben."};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "A(z) \"{0}\" előtag nem köthető a(z) \"{1}\" névtérhez."};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "A(z) \"{0}\" elem helyi neve null."};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "A(z) \"{0}\" attribútum helyi neve null."};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "A(z) \"{0}\" entitáscsomópont helyettesítő szövege a(z) \"{1}\" elemcsomópontot tartalmazza, amelynek nem kötött előtagja \"{2}\"."};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "A(z) \"{0}\" entitáscsomópont helyettesítő szövege a(z) \"{1}\" attribútum-csomópontot tartalmazza, amelynek nem kötött előtagja \"{2}\"."};
        return contents;
    }
}
