package net.youngrok.snippet.database;

import lombok.Getter;
import lombok.Setter;
import net.youngrok.snippet.web.AppLog;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
@Entity
@Table(name = "applog")
public class AppLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user", length = 20, nullable = false)
    private String user;

    @Column(name = "market", length = 10, nullable = false)
    private String market;

    @Column(name = "idfa", length = 40)
    private String idfa;

    @Column(name = "aaid", length = 40)
    private String aaid;

    @Column(name = "eventlog", length = 3000, nullable = false)
    private String eventLog;

    @Column(name = "eventtime", nullable = false)
    private LocalDateTime eventTime;

    public static AppLogEntity newEntity(AppLog appLog) {
        return new AppLogEntity()
                .setUser(appLog.getUser())
                .setMarket(appLog.getMarket())
                .setIdfa(appLog.getIdfa())
                .setAaid(appLog.getAaid())
                .setEventLog(appLog.getEventLog())
                .setEventTime(LocalDateTime.ofEpochSecond(appLog.getEventTimeEpoch(), 0, ZoneOffset.UTC));
    }
}
