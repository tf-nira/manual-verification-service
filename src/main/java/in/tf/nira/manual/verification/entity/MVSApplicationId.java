package in.tf.nira.manual.verification.entity;

import java.io.Serializable;
import java.util.Objects;

import lombok.Data;

@Data
public class MVSApplicationId implements Serializable {
    private String regId;
    private String verifiedOfficerRole;

    public MVSApplicationId() {
    }

    public MVSApplicationId(String regId, String verifiedOfficerRole) {
        this.regId = regId;
        this.verifiedOfficerRole = verifiedOfficerRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MVSApplicationId that = (MVSApplicationId) o;
        return Objects.equals(regId, that.regId) && 
               Objects.equals(verifiedOfficerRole, that.verifiedOfficerRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regId, verifiedOfficerRole);
    }
}
