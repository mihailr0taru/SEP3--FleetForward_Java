package dk.via.fleetforward.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DriversManagedByDispatcherId implements Serializable {
    private static final long serialVersionUID = -2149140189219153662L;
    @Column(name = "dispatcher_id", nullable = false)
    private Integer dispatcherId;

    @Column(name = "driver_id", nullable = false)
    private Integer driverId;
    public DriversManagedByDispatcherId() {}
    public DriversManagedByDispatcherId(int dispatcherId, int driverId) {
        this.dispatcherId = dispatcherId;
        this.driverId = driverId;
    }
    public Integer getDispatcherId() {
        return dispatcherId;
    }

    public void setDispatcherId(Integer dispatcherId) {
        this.dispatcherId = dispatcherId;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DriversManagedByDispatcherId entity = (DriversManagedByDispatcherId) o;
        return Objects.equals(this.driverId, entity.driverId) &&
                Objects.equals(this.dispatcherId, entity.dispatcherId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverId, dispatcherId);
    }

}