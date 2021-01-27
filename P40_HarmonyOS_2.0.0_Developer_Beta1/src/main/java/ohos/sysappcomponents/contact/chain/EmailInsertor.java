package ohos.sysappcomponents.contact.chain;

import java.util.ArrayList;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.Email;

public class EmailInsertor extends Insertor {
    @Override // ohos.sysappcomponents.contact.chain.Insertor
    public void fillOperation(Contact contact, ArrayList<DataAbilityOperation> arrayList, Insertor.OperationType operationType) {
        if (!Stream.of(contact, arrayList).anyMatch($$Lambda$EmailInsertor$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            if (contact.getEmails() == null || contact.getEmails().isEmpty()) {
                fillNextItem(contact, arrayList, operationType);
                return;
            }
            for (Email email : contact.getEmails()) {
                DataAbilityOperation.Builder predicatesBuilder = getPredicatesBuilder(Attribute.CommonDataKinds.Email.CONTENT_ITEM_TYPE, operationType, email.getId());
                if (!isEmpty(email.getEmail())) {
                    fillStringContent(predicatesBuilder, "data1", email.getEmail());
                }
                if (email.getLabelId() != -1) {
                    fillIntegerContent(predicatesBuilder, "data2", email.getLabelId());
                }
                if (email.getLabelId() == 0 && !isEmpty(email.getLabelName())) {
                    fillStringContent(predicatesBuilder, "data3", email.getLabelName());
                }
                if (!isEmpty(email.getDisplayName())) {
                    fillStringContent(predicatesBuilder, "data4", email.getDisplayName());
                }
                arrayList.add(predicatesBuilder.build());
            }
            fillNextItem(contact, arrayList, operationType);
        }
    }
}
