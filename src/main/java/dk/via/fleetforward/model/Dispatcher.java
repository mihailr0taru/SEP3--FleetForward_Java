package dk.via.fleetforward.model;


import dk.via.fleetforward.gRPC.Fleetforward.DispatcherProto;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table (name = "dispatcher", schema = "fleetforward")
public class Dispatcher
{
    @Id
    private Integer dispatcherId;

    @Column (name ="commission_rate", nullable = false)
    private Double commissionRate;

    public Dispatcher() {}

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable
            (
                    name = "drivers_managed_by_dispatcher",
                    schema = "fleetforward",
                    joinColumns = @JoinColumn(name = "dispatcher_id")
            )
    @Column (name = "driver_id")
    private List<Integer> assignedDrivers = new ArrayList<>();

    public Dispatcher(DispatcherProto dispatcherProto, int id)
    {
        setDispatcherId(id);
        setCommissionRate(dispatcherProto.getCurrentRate());
    }

    public void  setDispatcherId(int dispatcherId)
    {
        this.dispatcherId = dispatcherId;
    }

    public int getDispatcherId()
    {
        return dispatcherId;
    }

    public void  setCommissionRate(double commissionRate)
    {
        this.commissionRate = commissionRate;
    }

    public double getCommissionRate()
    {
        return commissionRate;
    }

    public List<Integer> getAssignedDrivers()
    {
        return assignedDrivers;
    }

    public void setAssignedDrivers(ArrayList<Integer> assignedDrivers)
    {
        this.assignedDrivers = assignedDrivers;
    }
}
