package ohos.sysappcomponents.contact.chain;

import java.util.ArrayList;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.Identity;

public class IdentityInsertor extends Insertor {
    @Override // ohos.sysappcomponents.contact.chain.Insertor
    public void fillOperation(Contact contact, ArrayList<DataAbilityOperation> arrayList, Insertor.OperationType operationType) {
        if (!Stream.of(contact, arrayList).anyMatch($$Lambda$IdentityInsertor$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            if (contact.getIdentity() == null) {
                fillNextItem(contact, arrayList, operationType);
                return;
            }
            Identity identity = contact.getIdentity();
            DataAbilityOperation.Builder predicatesBuilder = getPredicatesBuilder(Attribute.CommonDataKinds.Identity.CONTENT_ITEM_TYPE, operationType, identity.getId());
            if (!isEmpty(identity.getIdentity())) {
                fillStringContent(predicatesBuilder, "data1", identity.getIdentity());
            }
            if (!isEmpty(identity.getNameSpace())) {
                fillStringContent(predicatesBuilder, "data2", identity.getNameSpace());
            }
            arrayList.add(predicatesBuilder.build());
            fillNextItem(contact, arrayList, operationType);
        }
    }
}
