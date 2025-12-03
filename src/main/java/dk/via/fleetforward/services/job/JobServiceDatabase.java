package dk.via.fleetforward.services.job;

import com.google.common.collect.Maps;
import com.google.protobuf.Timestamp;
import dk.via.fleetforward.gRPC.Fleetforward.JobListProto;
import dk.via.fleetforward.gRPC.Fleetforward.JobProto;
import dk.via.fleetforward.model.Dispatcher;
import dk.via.fleetforward.model.Driver;
import dk.via.fleetforward.model.Enums.JobStatus;
import dk.via.fleetforward.model.Enums.TrailerType;
import dk.via.fleetforward.model.Enums.UserRole;
import dk.via.fleetforward.model.Job;
import dk.via.fleetforward.model.User;
import dk.via.fleetforward.repositories.database.DispatcherRepository;
import dk.via.fleetforward.repositories.database.DriverRepository;
import dk.via.fleetforward.repositories.database.JobRepository;
import dk.via.fleetforward.utility.ProtoUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


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
    if( jobRepository.existsById(payload.getJobId())) {
      log.warn("Job already exists {}", payload);
      throw new RuntimeException("Job already exists");
    }
    log.info("Creating job {}", payload);
    Job job = new Job(payload);
    job.setDispatcher(dispatcherRepository.findById(payload.getJobDispatcherId()).orElse(null));
    Job created = jobRepository.save(job);
    log.info("Created job {}", created);
    return ProtoUtils.parseFromJobToProto(created);
  }

  public static Instant toInstant(Timestamp ts) {
    if (ts == null) return null;
    return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
  }

  @Override @Transactional public JobProto update(JobProto payload)
  {
    Job existing = jobRepository.findById(payload.getJobId())
            .orElseThrow(() -> new RuntimeException("Job not found"));

    Dispatcher eDispatcher = existing.getDispatcher();

    Driver eDriver = existing.getDriver();

    TrailerType trailerType = TrailerType.valueOf(payload.getJobTrailerType().name());
    Instant pickupTime = toInstant(payload.getPickUpTime());
    Instant deliveryTime = toInstant(payload.getDeliveryTime());
    JobStatus currentJobStatus = JobStatus.valueOf(payload.getCurrentJobStatus().name());

    existing.setDispatcher(eDispatcher);
    existing.setDriver(eDriver);
    existing.setTitle(payload.getTitle());
    existing.setDescription(payload.getDescription());
    existing.setTrailerTypeNeeded(trailerType);
    existing.setLoadedMiles(payload.getLoadedMiles());
    existing.setWeightOfCargo(payload.getWeightOfCargo());
    existing.setTotalPrice(payload.getTotalPrice());
    existing.setCargoInfo(payload.getCargoInfo());
    existing.setPickupTime(pickupTime);
    existing.setDeliveryTime(deliveryTime);
    existing.setPickupLocationState(payload.getPickUpLocationState());
    existing.setPickupLocationZipCode(payload.getPickUpLocationZipCode());
    existing.setDropLocationState(payload.getDropLocationState());
    existing.setDropLocationZipCode(payload.getDropLocationZipCode());
    existing.setCurrentJobStatus(currentJobStatus);

    Job updated = jobRepository.save(existing);
    log.info("Updated job {}", updated);

    return ProtoUtils.parseFromJobToProto(updated);
  }

  @Override @Transactional public void delete(int id)
  {
    jobRepository.deleteById(id);
    log.info("Deleted job {}", id);
  }

  @Override @Transactional public JobProto getSingle(int id)
  {
    Optional<Job> fetched = jobRepository.findById(id); //null safety
    Job job = fetched.orElseThrow(() -> new RuntimeException("Job not found"));
    log.info("Fetched job {}", job);
    return ProtoUtils.parseFromJobToProto(job);
  }

  @Override
  @Transactional
  public JobListProto getAll()
  {
    List<Job> jobs = jobRepository.findAll();
      log.info("Fetched {} jobs", jobs.size());
    JobListProto.Builder builder = JobListProto.newBuilder();

    for (Job job : jobs)
    {
        job.getDispatcher();
        job.getDriver();
      builder.addJobs(ProtoUtils.parseFromJobToProto(job));
      log.info("Added job {}", job);
    }

    log.info("Created list of jobs");

    return builder.build();
  }
}
