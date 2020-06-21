package net.youngrok.snippet.withoutlombok;

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
@IdClass(CompositeKeyEntity2.Keys.class)
public class CompositeKeyEntity2 {
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
            if (this.getBrand() == null ? other.getBrand() != null : !this.getBrand().equals(other.getBrand())) {
                return false;
            }
            if (this.getItem() == null ? other.getItem() != null : !this.getItem().equals(other.getItem())) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = (result * PRIME) + (this.getBrand() == null ? 43 : this.getBrand().hashCode());
            result = (result * PRIME) + (this.getItem() == null ? 43 : this.getItem().hashCode());
            return result;
        }
    }
}