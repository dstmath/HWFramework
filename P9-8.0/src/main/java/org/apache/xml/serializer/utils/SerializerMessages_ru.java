package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_ru extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "Ключ сообщения ''{0}'' не относится к классу сообщения ''{1}''"};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "Формат сообщения ''{0}'' в классе сообщения ''{1}'' вызвал ошибку. "};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "Класс сериализатора ''{0}'' не применяет org.xml.sax.ContentHandler. "};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "Ресурс [ {0} ] не найден.\n {1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "Не удалось загрузить ресурс [ {0} ]: {1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "Размер буфера <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "Обнаружено недопустимое значение UTF-16: {0} ?"};
        contents[7] = new Object[]{"ER_OIERROR", "Ошибка ввода-вывода"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "Атрибут {0} нельзя добавлять после дочерних узлов и до создания элемента. Атрибут будет проигнорирован. "};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "Пространство имен для префикса ''{0}'' не объявлено. "};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "Атрибут ''{0}'' вне элемента. "};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "Объявление пространства имен ''{0}''=''{1}'' вне элемента. "};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "Не удалось загрузить ''{0}'' (проверьте CLASSPATH), применяются значения по умолчанию"};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "Попытка вывода символа, интегральное значение {0} которого не представлено в указанной кодировке вывода {1}. "};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "Невозможно загрузить файл свойств ''{0}'' для метода вывода ''{1}'' (проверьте CLASSPATH)"};
        contents[15] = new Object[]{"ER_INVALID_PORT", "Недопустимый номер порта"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "Невозможно задать порт для пустого адреса хоста"};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "Неправильно сформирован адрес хоста"};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "Схема не конформативна."};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "Невозможно задать схему для пустой строки"};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "В имени пути встречается недопустимая Esc-последовательность"};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "В имени пути обнаружен недопустимый символ: {0}"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "Фрагмент содержит недопустимый символ"};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "Невозможно задать фрагмент для пустого пути"};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "Фрагмент можно задать только для шаблона URI"};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "В URI не найдена схема"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "Невозможно инициализировать URI с пустыми параметрами"};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "Невозможно задать фрагмент одновременно для пути и фрагмента"};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "Нельзя указывать строку запроса в строке пути и запроса"};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "Нельзя указывать порт, если не задан хост"};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "Нельзя указывать информацию о пользователе, если не задан хост"};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "Предупреждение: Необходима версия документа вывода ''{0}''. Эта версия XML не поддерживается. Версией документа вывода будет ''1.0''. "};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "Необходима схема!"};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "Объект свойств, переданный в SerializerFactory, не обладает свойством ''{0}''. "};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "Предупреждение:  Кодировка ''{0}'' не поддерживается средой выполнения Java."};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "Параметр ''{0}'' не распознан. "};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "Параметр ''{0}'' распознан, но запрошенное значение задать не удалось. "};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "Строка результата слишком длинная для размещения в DOMString: ''{0}''. "};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "Тип значения для параметра с эти именем несовместим с ожидаемым типом значения. "};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "Не указан целевой каталог для вывода данных. "};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "Обнаружена неподдерживаемая кодировка. "};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "Невозможно сериализовать узел. "};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "Раздел CDATA содержит один или несколько маркеров разделителей ']]>'. "};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "Невозможно создать экземпляр проверки допустимости. Допустимый параметр имеет значение true, но проверку допустимости выполнить не удалось. "};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "Узел ''{0}'' содержит недопустимые символы XML. "};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "В комментарии обнаружен недопустимый символ XML (Юникод: 0x{0})."};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "При обработке instructiondata был обнаружен недопустимый символ XML (Юникод: 0x{0}). "};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "В содержимом CDATASection обнаружен недопустимый символ XML (Юникод: 0x{0})."};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "В содержимом символьных данных узла обнаружен недопустимый символ XML (Юникод: 0x{0})."};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "Недопустимые символы XML обнаружены в узле {0} с именем ''{1}''. "};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "Строка \"--\" недопустима в комментарии. "};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "Значение атрибута \"{1}\", связанного с типом элемента \"{0}\", не должно содержать символ ''<''. "};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "Необработанная ссылка на элемент \"&{0};\" недопустима. "};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "Внешняя ссылка на элемент \"&{0};\" недопустима в значении атрибута. "};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "Префикс \"{0}\" не может находиться в пространстве  имен \"{1}\". "};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "Локальное имя элемента \"{0}\" пусто. "};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "Локальное имя атрибута \"{0}\" пусто.  "};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "Текст замены для узла записи \"{0}\" содержит узел элементов \"{1}\" с несвязанным префиксом \"{2}\". "};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "Текст замены для узла записи \"{0}\" содержит узел атрибутов \"{1}\" с несвязанным префиксом \"{2}\". "};
        return contents;
    }
}
