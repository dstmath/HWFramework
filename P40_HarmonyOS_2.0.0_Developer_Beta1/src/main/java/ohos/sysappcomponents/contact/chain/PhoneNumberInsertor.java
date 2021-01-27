package ohos.sysappcomponents.contact.chain;

import java.util.ArrayList;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.PhoneNumber;

public class PhoneNumberInsertor extends Insertor {
    @Override // ohos.sysappcomponents.contact.chain.Insertor
    public void fillOperation(Contact contact, ArrayList<DataAbilityOperation> arrayList, Insertor.OperationType operationType) {
        if (!Stream.of(contact, arrayList).anyMatch($$Lambda$PhoneNumberInsertor$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            if (contact.getPhoneNumbers() == null || contact.getPhoneNumbers().isEmpty()) {
                fillNextItem(contact, arrayList, operationType);
                return;
            }
            for (PhoneNumber phoneNumber : contact.getPhoneNumbers()) {
                DataAbilityOperation.Builder predicatesBuilder = getPredicatesBuilder(Attribute.CommonDataKinds.PhoneNumber.CONTENT_ITEM_TYPE, operationType, phoneNumber.getId());
                if (!isEmpty(phoneNumber.getPhoneNumber())) {
                    fillStringContent(predicatesBuilder, "data1", phoneNumber.getPhoneNumber());
                }
                if (phoneNumber.getLabelId() != -1) {
                    fillIntegerContent(predicatesBuilder, "data2", phoneNumber.getLabelId());
                }
                if (phoneNumber.getLabelId() == 0 && !isEmpty(phoneNumber.getLabelName())) {
                    fillStringContent(predicatesBuilder, "data3", phoneNumber.getLabelName());
                }
                arrayList.add(predicatesBuilder.build());
            }
            fillNextItem(contact, arrayList, operationType);
        }
    }
}
