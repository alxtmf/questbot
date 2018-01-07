package ru.p03.questbot.bot.document.spi;

/**
 */
public interface DocumentMarshalFactory {
    <T> String marshal(T document, Long docType);

    <T> String marshal(T document, String docTypeCode);

    <T> T unmarshal(String xml, Long docType);

    <T> T unmarshal(String xml, String docTypeCode);
}
