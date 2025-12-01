package dk.via.fleetforward.services.job;

import dk.via.fleetforward.gRPC.Fleetforward.JobProto;
import dk.via.fleetforward.gRPC.Fleetforward.JobListProto;
import dk.via.fleetforward.repositories.database.JobRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class JobServiceDatabase implements JobService {
    private final JobRepository jobRepository;
    public JobServiceDatabase(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }
    @Override
    @Transactional
    public JobProto create(JobProto payload) {
        return null;
    }

    @Override
    @Transactional
    public void update(JobProto payload) {

    }

    @Override
    @Transactional
    public void delete(int id) {

    }

    @Override
    @Transactional
    public JobProto getSingle(int id) {
        return null;
    }

    @Override
    @Transactional
    public JobListProto getAll() {
        return null;
    }
}
