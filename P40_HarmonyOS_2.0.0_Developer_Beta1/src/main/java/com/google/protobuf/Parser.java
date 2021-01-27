package com.google.protobuf;

import java.io.InputStream;

public interface Parser<MessageType> {
    MessageType parseDelimitedFrom(InputStream inputStream) throws InvalidProtocolBufferException;

    MessageType parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException;

    MessageType parseFrom(ByteString byteString) throws InvalidProtocolBufferException;

    MessageType parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException;

    MessageType parseFrom(CodedInputStream codedInputStream) throws InvalidProtocolBufferException;

    MessageType parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException;

    MessageType parseFrom(InputStream inputStream) throws InvalidProtocolBufferException;

    MessageType parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException;

    MessageType parseFrom(byte[] bArr) throws InvalidProtocolBufferException;

    MessageType parseFrom(byte[] bArr, int i, int i2) throws InvalidProtocolBufferException;

    MessageType parseFrom(byte[] bArr, int i, int i2, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException;

    MessageType parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException;

    MessageType parsePartialDelimitedFrom(InputStream inputStream) throws InvalidProtocolBufferException;

    MessageType parsePartialDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException;

    MessageType parsePartialFrom(ByteString byteString) throws InvalidProtocolBufferException;

    MessageType parsePartialFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException;

    MessageType parsePartialFrom(CodedInputStream codedInputStream) throws InvalidProtocolBufferException;

    MessageType parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException;

    MessageType parsePartialFrom(InputStream inputStream) throws InvalidProtocolBufferException;

    MessageType parsePartialFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException;

    MessageType parsePartialFrom(byte[] bArr) throws InvalidProtocolBufferException;

    MessageType parsePartialFrom(byte[] bArr, int i, int i2) throws InvalidProtocolBufferException;

    MessageType parsePartialFrom(byte[] bArr, int i, int i2, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException;

    MessageType parsePartialFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException;
}
