package dk.via.fleetforward.services.job;

import dk.via.fleetforward.gRPC.Fleetforward;
import dk.via.fleetforward.gRPC.Fleetforward.JobProto;
import dk.via.fleetforward.gRPC.Fleetforward.JobListProto;
import dk.via.fleetforward.model.Company;
import dk.via.fleetforward.model.Driver;
import dk.via.fleetforward.model.Job;
import dk.via.fleetforward.model.User;
import dk.via.fleetforward.repositories.database.DispatcherRepository;
import dk.via.fleetforward.repositories.database.DriverRepository;
import dk.via.fleetforward.repositories.database.JobRepository;
import dk.via.fleetforward.services.company.CompanyServiceDatabase;
import dk.via.fleetforward.utility.ProtoUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service public class JobServiceDatabase implements JobService
{
  private final JobRepository jobRepository;
  private final DriverRepository driverRepository;
  private final DispatcherRepository dispatcherRepository;

  private static final Logger log = LoggerFactory.getLogger(
      JobServiceDatabase.class);

  public JobServiceDatabase(JobRepository jobRepository,
      DriverRepository driverRepository,
      DispatcherRepository dispatcherRepository)
  {
    this.jobRepository = jobRepository;
    this.driverRepository = driverRepository;
    this.dispatcherRepository = dispatcherRepository;
  }

  @Override @Transactional public JobProto create(JobProto payload)
  {
    Job job = new Job(payload);
    Driver driver = driverRepository.findById(payload.getJobDriverId())
        .orElseThrow(() -> new RuntimeException("User not found, user must be created first"));

    job.setDriver() jobRepository.save(job);
    log.info("Generated job ID: {}", job.getId());

    Job jobWithValues = new Job(payload, job.getId());

    jobWithValues.setId(job.getId());

    jobWithValues = jobRepository.save(jobWithValues);

    return ProtoUtils.parseJobProto(jobWithValues);
  }

  @Override @Transactional public void update(JobProto payload)
  {

  }

  @Override @Transactional public void delete(int id)
  {

  }

  @Override @Transactional public JobProto getSingle(int id)
  {
    return null;
  }

  @Override @Transactional public JobListProto getAll()
  {
    List<Job> jobs = jobRepository.findAll();
    log.info("Fetched {} jobs", jobs.size());

    // Builder for the list
    Fleetforward.JobListProto.Builder jobsProtoBuilder = Fleetforward.JobListProto.newBuilder();

    // Convert each Company entity to CompanyProto
    for (Job job : jobs)
    {
      Fleetforward.JobProto jobProto = ProtoUtils.parseJobProto(job);
      jobsProtoBuilder.addJobs(jobProto);
    }
    log.info("Created proto company list");
    // Build and return the list
    return jobsProtoBuilder.build();
  }
}
