package net.youngrok.snippet.web;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppLog {
    private String user;
    private String market;
    private String aaid;
    private String idfa;

    private String eventLog;
    private long eventTimeEpoch;
}
