package net.youngrok.snippet.lombok;

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
@Table(name = "twokeytable2")
@IdClass(CompositeKeyEntityWithoutLombok.Keys.class)
public class CompositeKeyEntityWithoutLombok {
    @Id
    private String brand;

    @Id
    private String item;

    private String price;

    private LocalDateTime expireTime;

    @Getter
    @Setter
    public static class Keys implements Serializable {
        private String brand;
        private String item;

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Keys)) {
                return false;
            }
            Keys other = (Keys) o;
            if (!other.equals((Object) this)) {
                return false;
            }
            if (this.brand() == null ? other.brand() != null : !this.brand().equals(other.brand())) {
                return false;
            }
            if (this.item() == null ? other.item() != null : !this.item().equals(other.item())) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = (result * PRIME) + (this.brand() == null ? 43 : this.brand().hashCode());
            result = (result * PRIME) + (this.item() == null ? 43 : this.item().hashCode());
            return result;
        }
    }
}