package ohos.com.sun.org.apache.xalan.internal.xsltc.runtime;

import java.util.ListResourceBundle;

public class ErrorMessages_cs extends ListResourceBundle {
    @Override // java.util.ListResourceBundle
    public Object[][] getContents() {
        return new Object[][]{new Object[]{BasisLibrary.RUN_TIME_INTERNAL_ERR, "Vnitřní běhová chyba v ''{0}''"}, new Object[]{BasisLibrary.RUN_TIME_COPY_ERR, "Vnitřní běhová chyba při provádění funkce <xsl:copy>."}, new Object[]{"DATA_CONVERSION_ERR", "Neplatná konverze z ''{0}'' do ''{1}''."}, new Object[]{BasisLibrary.EXTERNAL_FUNC_ERR, "Externí funkce ''{0}'' není podporována produktem SLTC."}, new Object[]{BasisLibrary.EQUALITY_EXPR_ERR, "Neznámý typ argumentu ve výrazu rovnosti."}, new Object[]{BasisLibrary.INVALID_ARGUMENT_ERR, "Neplatný typ argumentu ''{0}'' při volání ''{1}''"}, new Object[]{BasisLibrary.FORMAT_NUMBER_ERR, "Pokus formátovat číslo ''{0}'' použitím vzorku ''{1}''."}, new Object[]{BasisLibrary.ITERATOR_CLONE_ERR, "Nelze klonovat iterátor ''{0}''."}, new Object[]{BasisLibrary.AXIS_SUPPORT_ERR, "Iterátor pro osu ''{0}'' není podporován."}, new Object[]{BasisLibrary.TYPED_AXIS_SUPPORT_ERR, "Iterátor pro typizovanou osu ''{0}'' není podporován."}, new Object[]{"STRAY_ATTRIBUTE_ERR", "Atribut ''{0}'' je vně prvku."}, new Object[]{BasisLibrary.STRAY_NAMESPACE_ERR, "Deklarace oboru názvů ''{0}''=''{1}'' je vně prvku."}, new Object[]{BasisLibrary.NAMESPACE_PREFIX_ERR, "Obor názvů pro předponu ''{0}'' nebyl deklarován."}, new Object[]{BasisLibrary.DOM_ADAPTER_INIT_ERR, "DOMAdapter byl vytvořen s použitím chybného typu zdroje DOM."}, new Object[]{BasisLibrary.PARSER_DTD_SUPPORT_ERR, "Použitý analyzátor SAX nemůže manipulovat s deklaračními událostmi DTD."}, new Object[]{BasisLibrary.NAMESPACES_SUPPORT_ERR, "Použitý analyzátor SAX nemůže podporovat obory názvů pro XML."}, new Object[]{BasisLibrary.CANT_RESOLVE_RELATIVE_URI_ERR, "Nelze přeložit odkazy URI ''{0}''."}};
    }
}
