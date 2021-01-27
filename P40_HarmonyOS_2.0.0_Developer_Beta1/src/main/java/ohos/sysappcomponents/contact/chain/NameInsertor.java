package ohos.sysappcomponents.contact.chain;

import java.util.ArrayList;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.Name;

public class NameInsertor extends Insertor {
    @Override // ohos.sysappcomponents.contact.chain.Insertor
    public void fillOperation(Contact contact, ArrayList<DataAbilityOperation> arrayList, Insertor.OperationType operationType) {
        if (!Stream.of(contact, arrayList).anyMatch($$Lambda$NameInsertor$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            if (contact.getName() == null) {
                fillNextItem(contact, arrayList, operationType);
                return;
            }
            Name name = contact.getName();
            DataAbilityOperation.Builder predicatesBuilder = getPredicatesBuilder(Attribute.CommonDataKinds.Name.CONTENT_ITEM_TYPE, operationType, name.getId());
            if (!isEmpty(name.getFullName())) {
                fillStringContent(predicatesBuilder, "data1", name.getFullName());
            }
            if (!isEmpty(name.getGivenName())) {
                fillStringContent(predicatesBuilder, "data2", name.getGivenName());
            }
            if (!isEmpty(name.getFamilyName())) {
                fillStringContent(predicatesBuilder, "data3", name.getFamilyName());
            }
            if (!isEmpty(name.getNamePrefix())) {
                fillStringContent(predicatesBuilder, "data4", name.getNamePrefix());
            }
            if (!isEmpty(name.getMiddleName())) {
                fillStringContent(predicatesBuilder, "data5", name.getMiddleName());
            }
            if (!isEmpty(name.getNameSuffix())) {
                fillStringContent(predicatesBuilder, "data6", name.getNameSuffix());
            }
            if (!isEmpty(name.getGivenNamePhonetic())) {
                fillStringContent(predicatesBuilder, "data7", name.getGivenNamePhonetic());
            }
            if (!isEmpty(name.getMiddleNamePhonetic())) {
                fillStringContent(predicatesBuilder, "data8", name.getMiddleNamePhonetic());
            }
            if (!isEmpty(name.getFamilyNamePhonetic())) {
                fillStringContent(predicatesBuilder, "data9", name.getFamilyNamePhonetic());
            }
            arrayList.add(predicatesBuilder.build());
            fillNextItem(contact, arrayList, operationType);
        }
    }
}
