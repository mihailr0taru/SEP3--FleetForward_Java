package dk.via.fleetforward.services.job;

import dk.via.fleetforward.gRPC.Fleetforward.JobProto;
import dk.via.fleetforward.gRPC.Fleetforward.JobListProto;

public interface JobService {
    JobProto create(JobProto payload);
    JobProto update(JobProto payload);
    void delete(int id);
    JobProto getSingle (int id);
    JobListProto getAll();
}
