package io.xxcxy.spring.data.orientdb.test.integration.IdParser;

public class CustId {
    private int prefix;
    private long content;

    public CustId(final int prefix, final long content) {
        this.prefix = prefix;
        this.content = content;
    }

    public int getPrefix() {
        return prefix;
    }

    public long getContent() {
        return content;
    }
}
