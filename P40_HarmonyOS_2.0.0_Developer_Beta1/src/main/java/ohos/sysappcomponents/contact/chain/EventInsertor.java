package ohos.sysappcomponents.contact.chain;

import java.util.ArrayList;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.Event;

public class EventInsertor extends Insertor {
    @Override // ohos.sysappcomponents.contact.chain.Insertor
    public void fillOperation(Contact contact, ArrayList<DataAbilityOperation> arrayList, Insertor.OperationType operationType) {
        if (!Stream.of(contact, arrayList).anyMatch($$Lambda$EventInsertor$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            if (contact.getEvents() == null || contact.getEvents().isEmpty()) {
                fillNextItem(contact, arrayList, operationType);
                return;
            }
            for (Event event : contact.getEvents()) {
                DataAbilityOperation.Builder predicatesBuilder = getPredicatesBuilder(Attribute.CommonDataKinds.Event.CONTENT_ITEM_TYPE, operationType, event.getId());
                if (!isEmpty(event.getEventDate())) {
                    fillStringContent(predicatesBuilder, "data1", event.getEventDate());
                }
                if (event.getLabelId() != -1) {
                    fillIntegerContent(predicatesBuilder, "data2", event.getLabelId());
                }
                if (event.getLabelId() == 0 && !isEmpty(event.getLabelName())) {
                    fillStringContent(predicatesBuilder, "data3", event.getLabelName());
                }
                arrayList.add(predicatesBuilder.build());
            }
            fillNextItem(contact, arrayList, operationType);
        }
    }
}
