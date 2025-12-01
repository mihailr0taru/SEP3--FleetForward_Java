package dk.via.fleetforward.model;

import jakarta.persistence.*;

@Entity
@Table(name = "drivers_managed_by_dispatcher", schema = "fleetforward")
public class DriversManagedByDispatcher {
    @EmbeddedId
    private DriversManagedByDispatcherId id;

    @MapsId("dispatcherId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispatcher_id", nullable = false)
    private Dispatcher dispatcher;

    @MapsId("driverId")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    public DriversManagedByDispatcherId getId() {
        return id;
    }

    public void setId(DriversManagedByDispatcherId id) {
        this.id = id;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

}