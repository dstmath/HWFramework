package com.huawei.g11n.tmr.address.pt;

import com.huawei.g11n.tmr.util.Regexs;

public class ReguExp extends Regexs {
    private static String AT = ("(?:\\b(?:(?i)at|no|em?)\\s+|@\\s*)(" + words2_Str + "\\s+" + keyBui_B + ")");
    private static String POS = "\\b(?:(?i)cep\\s*:?\\s*)\\d{4,5}(?:-\\d{3})?\\b";
    private static String _cityNoCode = ("\\s*\\(.*\\)|" + con + "\\(?(?:" + word + "(?:\\s+(?:(?:(?i)de|das?|do)\\s+)?|\\s*(?:,|/|-|\\(|\\))\\s*)){0,4}" + word + "(?:\\s*\\))?");
    private static String _cityWithCode = (con + codeCity);
    private static String bs = (buiStr + "|" + "(?<street2>" + str + ")");
    private static String bui = (keyBui + "\\s+(?:(?:de|dos?|das?)\\s+)?" + words3_Str);
    private static String buiStr = (bui + "(?:(?:\\s+(?:dos?\\s+)?|\\s*\\(\\s*|\\s*,\\s*)" + "(?<street1>" + str + ")" + "(?:\\s*\\))?)?");
    private static String cat = "\\s*,\\s*";
    private static String code = "\\b(?:(?i)cep\\s*:\\s*)?\\d{4,5}(?:-\\d{3})?\\b";
    private static String codeCity = ("(?:" + word + "(?:\\s+" + word + ")?(?:\\s*,\\s*|\\s+))?" + "(?:\\bAPARTADO\\s*\\d{4,5}\\s*,\\s*)?" + "(?:" + word + "(?:\\s+(?:(?:(?i)de|das?)\\s+)?|\\s*(?:-|,)\\s*)){0,4}" + code + "(?:\\s*(,\\s*)?(?:" + word + "(?:\\s+(?:(?:(?i)de|das?)\\s+)?|\\s*(?:-|,)\\s*)){0,3}" + word + ")?");
    private static String con = "(?:,?\\s+(?:(?i)em?\\s+|de\\s+|in\\s+|na\\s+|das?\\s+|no\\s+)?|\\s*(?:,|-)\\s*)";
    private static String keyBui = "\\b(?<build>(?i)(?:Condomínio|Apartamento|Loja|Hispital|Catedral|Lote|Conjunto|Andar|Bairro|Freguesia|Estado|Residencial|Auditório|Igreja|Arquipélago|Catetral|Mosteiro|Convento|Capela|Santuário|Memorial|Casa|Paço|Monumento|Cemitério|Templo|Panteão|Castelo|Forte Duque de Caixas|Câmara municipal|Direcção|Chilindró|Tribunal|Fundação|Agência|Edifício|Prédio|Posto|Estacionamento|Correios|Editora|Banco|Clínica|Congregação Cristã|Mercado|Feira|Shopping|Loja|Mall|Livraria|Hall|Outlet|Centro|Supermercado|acessório|comboio|Exposição|Alameda|Pálace|Laboratório|Colégio|Faculdade|Cinema|Teatro|Theatro|Torre|Ponte|Museu|Miradouro|Miradouro|Mirante|Aquário|Galeria|Bar|Parque|Lounge|Clube|Porto|Cais|Jardim|Elevador|Passeio|Palácio|Hospital|Hotel|Hotéis|Hostel|Motel|Bodega|Taverna|Taberna|Condomínio|Pizzaria|Clinica|Aeroporto|Estação|Parada|Rodoviária|Restaurante|Lanchonete|Café|Churrascaria|Cervejaria|Cantina|Hamburgueria|Biblioteca|Bar|Pastelaria|Terminal|Padaria|Bistro|Universidade|Campus|Estádio|Arena|Residência|Praia|Metrô|Salão|Oficina|Palace|Lagoa|Campo|Square|Mercadinho|Praça|Fazenda|Academia|Vale|Bancários|Ilha|Ponto|Empório|Ginásio)\\b|apt\\.|Apto\\.|Conj\\.|s\\.m\\.|\\s+Sé|Pte\\.)";
    private static String keyBui_B = "\\b((?:Condomínio|Apartamento|Loja|Hispital|Catedral|Lote|Conjunto|Andar|Bairro|Freguesia|Estado|Residencial|Auditório|Igreja|Arquipélago|\\s+Sé|Catetral|Mosteiro|Convento|Capela|Santuário|Memorial|Casa|Paço|Monumento|Cemitério|Templo|Panteão|Castelo|Forte Duque\\s+de\\s+Caixas|Câmara municipal|Direcção|Chilindró|Tribunal|Fundação|Agência|Edifício|Prédio|Posto|Estacionamento|Correios|Editora|Banco|Clínica|Congregação\\s+Cristã|Mercado|Feira|Shopping|Loja|Mall|Livraria|Hall|Outlet|Centro|Supermercado|acessório|comboio|Exposição|Alameda|Pálace|Laboratório|Colégio|Faculdade|Cinema|Teatro|Theatro|Torre|Ponte|Pte\\.|Museu|Miradouro|Miradouro|Mirante|Aquário|Galeria|Bar|Parque|Lounge|Clube|Porto|Cais|Jardim|Elevador|Passeio|Palácio|Hospital|Hotel|Hotéis|Hostel|Motel|Bodega|Taverna|Taberna|Condomínio|Pizzaria|Clinica|Aeroporto|Estação|Parada|Rodoviária|Restaurante|Lanchonete|Café|Churrascaria|Cervejaria|Cantina|Hamburgueria|Biblioteca|Bar|Pastelaria|Terminal|Padaria|Bistro|Universidade|Campus|Estádio|Arena|Residência|Praia|Metrô|Salão|Oficina|Palace|Lagoa|Campo|Square|Mercadinho|Praça|Fazenda|Academia|Vale|Bancários|Ilha|Ponto|Empório|Ginásio)\\b|apt\\.|Apto\\.|Conj\\.|S\\.M\\.)";
    private static String keyStr = "\\b((?i)Rota\\b|Rua\\b|R\\.|R\\b|Avenida\\b|Av\\.|Av\\b|Avª\\b|Travessa\\b|Tv\\.|Calçada\\b|Calç\\.|Estrada\\b|Estr\\.|Rovovia\\b|Rod\\.|Praça\\b|Pç\\b|Largo\\b|Lg\\.|Campo\\b|Rodovia\\b|Túnel\\b|Ladeirados\\b|Ladeira\\b|Viaduto\\b|Boulevard\\b|Bulevar\\b|Lig\\.|Estradas\\b|No\\.|Bloco\\b)";
    private static String str = ("(?:\\b(?i)lote\\s+\\d{1,4}\\s*)?" + keyStr + "\\s+(?:(?:(?i)das?|dos?)?\\s+)?" + words3_Str + "(?:(?:\\s+(?i)lote)?\\s*(?:,\\s*)?" + strNum + ")?");
    private static String strNum = "\\b(?:(?i)nº\\s*)?\\d{1,4}(?:\\s*(/|-)\\s*\\d{1,3})?\\b";
    private static String word = "[A-ZÁÃÀÂÉÈÊÌÎÍÒÔÓÕÚÙÛ0-9][0-9'A-ZÁÃÀÂÉÈÊÌÎÍÒÔÓÚÙÛa-záãàâéèêìîíòôóõúùûªç]*";
    private static String words2_Str = ("\\b(?:" + word + "(?:\\s+(?:(?:(?i)das?|dos?|de)\\s+)?|\\s*(?:/|-)\\s*)){0,2}" + word + "\\b");
    private static String words3_Str = ("\\b(?:" + word + "(?:\\s+(?:(?:(?i)das?|dos?|de)\\s+)?|\\s*(?:/|-)\\s*)){0,5}" + word + "\\b");

    public void init() {
        put("pbs", bs);
        put("pat", AT);
        put("pCat", cat);
        put("pnum", "\\d{4,5}");
        put("pCityWithCode", _cityWithCode);
        put("pCityNoCode", _cityNoCode);
        put("pPos", POS);
    }
}
