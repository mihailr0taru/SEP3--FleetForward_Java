package dk.via.fleetforward.services.RecruitDriver;

import dk.via.fleetforward.gRPC.Fleetforward.RecruitDriverProto;
import dk.via.fleetforward.gRPC.Fleetforward.DriverListProto;
import dk.via.fleetforward.gRPC.Fleetforward.DriverProto;

public interface RecruitDriverService {
    DriverProto recruitDriver(RecruitDriverProto proto);
    void fireDriver(RecruitDriverProto proto);
    DriverListProto getDispatcherDriversList(int id);
}
