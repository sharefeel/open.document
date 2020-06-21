package net.youngrok.snippet.web;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class LogCount {
    private String market;
    private long count;

    public static LogCount getSample() {
        return new LogCount().setMarket("appstore").setCount(10);
    }
}
