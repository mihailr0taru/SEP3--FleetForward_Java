package dk.via.fleetforward.services.user;
import dk.via.fleetforward.gRPC.Fleetforward.DispatcherProto;
import dk.via.fleetforward.gRPC.Fleetforward.DispatcherListProto;
public interface DispatcherService {
    DispatcherProto create(DispatcherProto dispatcher);
    void update(DispatcherProto dispatcher);
    void delete(int id);
    DispatcherProto getSingle(int id);
    DispatcherListProto getAll();
}
