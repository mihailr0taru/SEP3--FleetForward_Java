package dk.via.fleetforward.services.RecruitDriver;

import dk.via.fleetforward.gRPC.Fleetforward.DriverListProto;
import dk.via.fleetforward.gRPC.Fleetforward.DriverProto;

public interface RecruitDriverS {
    DriverProto recruitDriver(DispatcherProto proto);
    void fireDriver(int id);
    DriverListProto getDispatcherDriversList(int id);
}
