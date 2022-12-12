package org.mtcg.HTTP;

public class HeaderParser {
    public Header parseHeader(String rawHeader){
        if (rawHeader == null) {
            return null;
        }
        String [] split = rawHeader.split(":", 2);
        final String name = split[0];
        final String value = split[1].trim();
        final Header header = new Header();
        header.setName(name);
        header.setValue(value);
        return header;
    }
}
