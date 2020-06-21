package net.youngrok.snippet.withlombok;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "twokeytable1")
@IdClass(CompositeKeyEntity.Keys.class)
public class CompositeKeyEntity {
    @Id
    private String brand;

    @Id
    private String item;

    private String price;

    private LocalDateTime expireTime;

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class Keys implements Serializable {
        private String brand;
        private String item;
    }
}
