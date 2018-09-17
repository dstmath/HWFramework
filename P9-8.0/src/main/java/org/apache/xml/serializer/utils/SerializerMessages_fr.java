package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_fr extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "La clé du message ''{0}'' ne se trouve pas dans la classe du message ''{1}''"};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "Le format du message ''{0}'' de la classe du message ''{1}'' est incorrect."};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "La classe de la méthode de sérialisation ''{0}'' n''implémente pas org.xml.sax.ContentHandler."};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "La ressource [ {0} ] est introuvable.\n {1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "La ressource [ {0} ] n''a pas pu charger : {1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "Taille du tampon <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "Substitut UTF-16 non valide détecté : {0} ?"};
        contents[7] = new Object[]{"ER_OIERROR", "Erreur d'E-S"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "Ajout impossible de l''attribut {0} après des noeuds enfants ou avant la production d''un élément.  L''attribut est ignoré."};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "L''espace de noms du préfixe ''{0}'' n''a pas été déclaré."};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "L''attribut ''{0}'' est à l''extérieur de l''élément."};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "La déclaration d''espace de noms ''{0}''=''{1}'' est à l''extérieur de l''élément."};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "Impossible de charger ''{0}'' (vérifier CLASSPATH), les valeurs par défaut sont donc employées"};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "Tentative de sortie d''un caractère de la valeur entière {0} non représentée dans l''encodage de sortie de {1}."};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "Impossible de charger le fichier de propriétés ''{0}'' pour la méthode de sortie ''{1}'' (vérifier CLASSPATH)"};
        contents[15] = new Object[]{"ER_INVALID_PORT", "Numéro de port non valide"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "Le port ne peut être défini quand l'hôte est vide"};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "L'hôte n'est pas une adresse bien formée"};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "Le processus n'est pas conforme."};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "Impossible de définir le processus à partir de la chaîne vide"};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "Le chemin d'accès contient une séquence d'échappement non valide"};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "Le chemin contient un caractère non valide : {0}"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "Le fragment contient un caractère non valide"};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "Le fragment ne peut être défini quand le chemin d'accès est vide"};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "Le fragment ne peut être défini que pour un URI générique"};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "Processus introuvable dans l'URI"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "Impossible d'initialiser l'URI avec des paramètres vides"};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "Le fragment ne doit pas être indiqué à la fois dans le chemin et dans le fragment"};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "La chaîne de requête ne doit pas figurer dans un chemin et une chaîne de requête"};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "Le port peut ne pas être spécifié si l'hôte n'est pas spécifié"};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "Userinfo ne peut être spécifié si l'hôte ne l'est pas"};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "Avertissement : La version du document de sortie doit être ''{0}''.  Cette version XML n''est pas prise en charge.  La version du document de sortie sera ''1.0''."};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "Processus requis !"};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "L''objet Properties transmis à SerializerFactory ne dispose pas de propriété ''{0}''."};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "Avertissement : Le codage ''{0}'' n''est pas pris en charge par l''environnement d''exécution Java."};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "Le paramètre ''{0}'' n''est pas reconnu."};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "Le paramètre ''{0}'' est reconnu mas la valeur demandée ne peut pas être définie."};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "La chaîne obtenue est trop longue pour un DOMString : ''{0}''."};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "Le type de valeur de ce paramètre est incompatible avec le type de valeur attendu."};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "La sortie de destination des données à écrire était vide."};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "Codage non pris en charge."};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "Le noeud ne peut pas être sérialisé."};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "La section CDATA contient un ou plusieurs marqueurs de fin ']]>'."};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "Aucune instance du programme de vérification de la formation n'a pu être créée.  La valeur true a été attribuée au paramètre well-formed mais la vérification de la formation n'a pas pu être effectuée."};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "Le noeud ''{0}'' contient des caractères XML non valides."};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "Un caractère XML non valide (Unicode : 0x{0}) a été trouvé dans le commentaire."};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "Un caractère XML non valide (Unicode : 0x{0}) a été trouvé dans les données de l''instruction de traitement."};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "Un caractère XML non valide (Unicode: 0x{0}) a été trouvé dans le contenu de la CDATASection"};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "Un caractère XML non valide (Unicode : 0x{0}) a été trouvé dans le contenu des données de type caractères du noeud."};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "Un ou plusieurs caractères non valides ont été trouvés dans le noeud {0} nommé ''{1}''."};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "La chaîne \"--\" est interdite dans des commentaires."};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "La valeur de l''attribut \"{1}\" associé à un type d''élément \"{0}\" ne doit pas contenir le caractère ''<''."};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "La référence d''entité non analysée \"&{0};\" n''est pas admise."};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "La référence d''entité externe \"&{0};\" n''est pas admise dans une valeur d''attribut."};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "Le préfixe \"{0}\" ne peut pas être lié à l''espace de noms \"{1}\"."};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "Le nom local de l''élément \"{0}\" a une valeur null."};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "Le nom local de l''attribut \"{0}\" a une valeur null."};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "le texte de remplacement du noeud de l''entité \"{0}\" contaient un noeud d''élément \"{1}\" avec un préfixe non lié \"{2}\"."};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "Le texte de remplacement du noeud de l''entité \"{0}\" contient un noeud d''attribut \"{1}\" avec un préfixe non lié \"{2}\"."};
        return contents;
    }
}
