package dk.via.fleetforward.model;


import dk.via.fleetforward.gRPC.Fleetforward.DispatcherProto;
import jakarta.persistence.*;

@Entity
@Table (name = "dispatcher", schema = "fleetforward")
public class Dispatcher
{
    @Id
    private Integer dispatcherId;

    @Column (name ="commission_rate", nullable = false)
    private Double commissionRate;

    public Dispatcher() {}

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

    public static DispatcherProto makeDispatcherProto(Dispatcher dispatcher, User user)
    {
        return DispatcherProto.newBuilder()
                .setCurrentRate(dispatcher.getCommissionRate())
                .setUser(User.makeUserProto(user))
                .build();
    }
}
