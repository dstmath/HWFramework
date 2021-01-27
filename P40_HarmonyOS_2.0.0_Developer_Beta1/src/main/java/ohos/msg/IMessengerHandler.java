package ohos.msg;

import ohos.utils.Parcel;

public interface IMessengerHandler {
    int onReceiveMessage(Message message) throws MessengerException;

    Message unmarshalling(Parcel parcel) throws MessengerException;
}
