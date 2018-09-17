package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_tr extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "''{0}'' ileti anahtarı ''{1}'' ileti sınıfında yok"};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "''{1}'' ileti sınıfındaki ''{0}'' iletisinin biçimi başarısız."};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "''{0}'' diziselleştirme sınıfı org.xml.sax.ContentHandler sınıfını gerçekleştirmiyor."};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "Kaynak [ {0} ] bulunamadı.\n {1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "Kaynak [ {0} ] yükleyemedi: {1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "Arabellek büyüklüğü <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "UTF-16 yerine kullanılan değer geçersiz: {0} ?"};
        contents[7] = new Object[]{"ER_OIERROR", "GÇ hatası"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "Alt düğümlerden sonra ya da bir öğe üretilmeden önce {0} özniteliği eklenemez.  Öznitelik yoksayılacak."};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "''{0}'' önekine ilişkin ad alanı bildirilmedi."};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "''{0}'' özniteliği öğenin dışında."};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "''{0}''=''{1}'' ad alanı bildirimi öğenin dışında."};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "''{0}'' yüklenemedi (CLASSPATH değişkeninizi inceleyin), yalnızca varsayılanlar kullanılıyor"};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "Belirtilen {1} çıkış kodlamasında gösterilmeyen {0} tümlev değeri karakteri çıkış girişimi."};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "''{1}'' çıkış yöntemi için ''{0}'' özellik dosyası yüklenemedi (CLASSPATH değişkenini inceleyin)"};
        contents[15] = new Object[]{"ER_INVALID_PORT", "Kapı numarası geçersiz"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "Anasistem boş değerliyken kapı tanımlanamaz"};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "Anasistem doğru biçimli bir adres değil"};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "Şema uyumlu değil."};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "Boş değerli dizgiden şema tanımlanamaz"};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "Yol geçersiz kaçış dizisi içeriyor"};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "Yol geçersiz karakter içeriyor: {0}"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "Parça geçersiz karakter içeriyor"};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "Yol boş değerliyken parça tanımlanamaz"};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "Parça yalnızca soysal URI için tanımlanabilir"};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "URI içinde şema bulunamadı"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "Boş değiştirgelerle URI kullanıma hazırlanamaz"};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "Parça hem yolda, hem de parçada belirtilemez"};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "Yol ve sorgu dizgisinde sorgu dizgisi belirtilemez"};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "Anasistem belirtilmediyse kapı belirtilemez"};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "Anasistem belirtilmediyse kullanıcı bilgisi belirtilemez"};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "Uyarı:  Çıkış belgesinin sürümünün ''{0}'' olması isteniyor.  Bu XML sürümü desteklenmez.  Çıkış dosyasının sürümü ''1.0'' olacak."};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "Şema gerekli!"};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "SerializerFactory''ye geçirilen Properties nesnesinin bir ''{0}'' özelliği yok."};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "Uyarı: ''{0}'' kodlaması Java Runtime tarafından desteklenmiyor."};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "''{0}'' değiştirgesi tanınmıyor."};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "''{0}'' değiştirgesi tanınıyor, ancak istenen değer tanımlanamıyor."};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "Sonuç dizgisi DOMString için çok uzun: ''{0}''."};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "Bu değiştirge adına ilişkin değer tipi, beklenen değer tipiyle uyumlu değil."};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "Yazılacak verilerin çıkış hedefi boş değerli."};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "Desteklenmeyen bir kodlama saptandı."};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "Düğüm diziselleştirilemedi."};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "CDATA kısmında bir ya da daha çok ']]>' sonlandırma imleyicisi var."};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "Well-Formedness denetşeyicisinin somut örneği yaratılamadı.  well-formed değiştirgesi true değerine ayarlandı, ancak doğru biçim denetimi gerçekleştirilemiyor."};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "''{0}'' düğümü geçersiz XML karakterleri içeriyor."};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "Açıklamada geçersiz bir XML karakteri (Unicode: 0x{0}) saptandı."};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "İşleme yönergesi verilerinde geçersiz bir XML karakteri (Unicode: 0x{0}) saptandı."};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "CDATASection içeriğinde geçersiz bir XML karakteri (Unicode: 0x{0}) saptandı."};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "Düğümün karakter verileri içeriğinde geçersiz bir XML karakteri (Unicode: 0x{0}) saptandı."};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "''{1}'' adlı {0} düğümünde geçersiz XML karakteri saptandı."};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "Açıklamalar içinde \"--\" dizgisine izin verilmez."};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "\"{0}\" öğe tipiyle ilişkilendirilen \"{1}\" özniteliğinin değeri ''<'' karakteri içermemelidir."};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "\"&{0};\" ayrıştırılmamış varlık başvurusuna izin verilmez."};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "Öznitelik değerinde \"&{0};\" dış varlık başvurusuna izin verilmez."};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "\"{0}\" öneki \"{1}\" ad alanına bağlanamıyor."};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "\"{0}\" öğesinin yerel adı boş değerli."};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "\"{0}\" özniteliğinin yerel adı boş değerli."};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "\"{0}\" varlık düğümünün yerine koyma metninde, bağlanmamış \"{2}\" öneki bulunan bir öğe düğümü (\"{1}\") var."};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "\"{0}\" varlık düğümünün yerine koyma metninde, bağlanmamış \"{2}\" öneki bulunan bir öznitelik düğümü (\"{1}\") var."};
        return contents;
    }
}
