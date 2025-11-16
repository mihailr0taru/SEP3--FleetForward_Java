package dk.via.fleetforward.services.user;

import dk.via.fleetforward.gRPC.Fleetforward.DriverProto;
import dk.via.fleetforward.gRPC.Fleetforward.DriverListProto;

public interface DriverService {
    DriverProto create(DriverProto driver);
    void update(DriverProto driver);
    void delete(int driverId);
    DriverProto getSingle(String driverId);
    DriverListProto getAll();
}
