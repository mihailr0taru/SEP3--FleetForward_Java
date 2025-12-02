package dk.via.fleetforward.utility;

import com.google.protobuf.Timestamp;
import dk.via.fleetforward.gRPC.Fleetforward.*;
import dk.via.fleetforward.model.*;
import dk.via.fleetforward.model.Enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;

public class ProtoUtils {

    private static final Logger log = LoggerFactory.getLogger(ProtoUtils.class);

    public static DriverProto parseDriverProto(Driver driver, User user) {
        if (driver == null) {
            log.error("Driver is null");
            throw new RuntimeException("Driver is null");
        }
        if (user == null) {
            log.error("User is null");
            throw new RuntimeException("User is null");
        }
        return DriverProto.newBuilder()
                .setCompanyMcNumber(driver.getCompanyMcNumber() ==
                        null ? "" : driver.getCompanyMcNumber())
                .setDriverStatus(parseStatusDriver(driver.getStatus()
                        == null ? StatusDriver.available : driver.getStatus())
                )
                .setCompanyRole(parseDriverCompanyRole(driver.getDriverCompanyRole()
                        == null ? DriverCompanyRole.driver : driver.getDriverCompanyRole())
                )
                .setTrailerType(parseTrailerTypeProto(driver.getCurrentTrailerType()
                        == null ? TrailerType.dry_van : driver.getCurrentTrailerType())
                )
                .setCurrentState(driver.getCurrentLocationState()
                        == null ? "" : driver.getCurrentLocationState())
                .setCurrentZIPCODE(driver.getCurrentLocationZipCode()
                        == 0 ? 0 : driver.getCurrentLocationZipCode())
                .setUser(parseUserProto(user))
                .build();
    }
    public static StatusDriverProto parseStatusDriver(StatusDriver status) {
        return switch (status) {
            case available -> StatusDriverProto.AVAILABLE;
            case busy -> StatusDriverProto.BUSY;
            case off_duty -> StatusDriverProto.OFF_DUTY;
            case null, default ->
                    {
                        log.error("Unknown status {}", status);
                        throw new RuntimeException("Unknown status");
                    }
        };
    }
    public static DriverCompanyRoleProto parseDriverCompanyRole(DriverCompanyRole role) {
        return switch (role) {
            case driver -> DriverCompanyRoleProto.DRIVER;
            case owner_operator -> DriverCompanyRoleProto.OWNER_OPERATOR;
            case null, default -> {
                log.error("Unknown role {}", role);
                throw new RuntimeException("Unknown role");
            }
        };
    }
    public static DriverCompanyRole parseDriverCompanyRoleProto(DriverCompanyRoleProto role) {
        return switch (role) {
            case DRIVER -> DriverCompanyRole.driver;
            case OWNER_OPERATOR -> DriverCompanyRole.owner_operator;
            case null, default -> {
                log.error("Unknown role {}", role);
                throw new RuntimeException("Unknown role");
            }
        };
    }
    public static StatusDriver parseStatusDriverProto(StatusDriverProto status) {
        return switch (status) {
            case AVAILABLE -> StatusDriver.available;
            case BUSY -> StatusDriver.busy;
            case OFF_DUTY -> StatusDriver.off_duty;
            case null, default -> {
                log.error("Unknown status {}", status);
                throw new RuntimeException("Unknown status");
            }
        };
    }
    public static TrailerTypeProto parseTrailerTypeProto(TrailerType trailerType) {
        return switch (trailerType)
        {
            case dry_van -> TrailerTypeProto.DRY_VAN;
            case reefer -> TrailerTypeProto.REEFER;
            case flatbed -> TrailerTypeProto.FLATBED;
            case null, default -> {
                log.error("Unknown trailer type {}", trailerType);
                throw new RuntimeException("Unknown trailer type");
            }
        };
    }
    public static TrailerType parseTrailerType(TrailerTypeProto trailerType) {
        return switch (trailerType)
        {
            case REEFER -> TrailerType.reefer;
            case FLATBED -> TrailerType.flatbed;
            case DRY_VAN -> TrailerType.dry_van;
            case null, default -> {
                log.error("Unknown trailer type {}", trailerType);
                throw new RuntimeException("Unknown trailer type");
            }
        };
    }
    public static UserProto parseUserProto(User user)
    {
        if (user == null) {
            log.error("User is null in ParseUserProto");
            throw new RuntimeException("User is null");
        }
        return UserProto.newBuilder()
                .setId(user.getId())
                .setFirstName(user.getFirstName()
                        == null ? "" : user.getFirstName())
                .setLastName(user.getLastName()
                        == null ? "" : user.getLastName())
                .setEmail(user.getEmail()
                        == null ? "" : user.getEmail())
                .setPhoneNumber(user.getPhoneNumber()
                        == null ? "" : user.getPhoneNumber())
                .setPassword(user.getPassword()
                        == null ? "" : user.getPassword())
                .setRole(parseFromUserRoleToProto(user.getRole()))
                .build();
    }
    public static DispatcherProto parseDispatcherProto(Dispatcher dispatcher, User user)
    {
        if (dispatcher == null) {
            log.error("Dispatcher is null");
            throw new RuntimeException("Dispatcher is null");
        }
        if (user == null) {
            log.error("User is null in ParseDispatcherProto");
            throw new RuntimeException("User is null");
        }
        return DispatcherProto.newBuilder().setCurrentRate(dispatcher.getCommissionRate())
                .setUser(parseUserProto(user))
                .addAllDriversAssigned(dispatcher.getAssignedDrivers()
                        == null ? new ArrayList<>() : dispatcher.getAssignedDrivers())
                .build();
    }
    public static CompanyProto parseCompanyProto(Company company)
    {
        if (company == null) {
            log.error("Company is null");
            throw new RuntimeException("Company is null");
        }
        return CompanyProto.newBuilder()
                .setMcNumber(company.getMcNumber())
                .setCompanyName(company.getCompanyName())
                .build();
    }
    public static UserRoleProto parseFromUserRoleToProto(UserRole role)
    {
        if(role == null)
        {
            log.error("Role is null");
            throw new RuntimeException("Role is null");
        }
        switch (role) {
            case driver -> {
                return UserRoleProto.USER_DRIVER;
            }
            case dispatcher -> {
                return UserRoleProto.USER_DISPATCHER;
            }
            default -> {
                log.error("Unknown user role");
                throw new RuntimeException("Unknown role");
            }
        }
    }
    public static UserRole parseFromUserRoleProto(UserRoleProto role)
    {
        if(role == null)
        {
            log.error("Role is null");
            throw new RuntimeException("Role is null");
        }
        switch (role) {
            case USER_DRIVER -> {
                return UserRole.driver;
            }
            case USER_DISPATCHER -> {
                return UserRole.dispatcher;
            }
            default -> {
                log.error("Unknown user role");
                throw new RuntimeException("Unknown role");
            }
        }
    }
    public static JobProto parseFromJobToProto(Job job)
    {
        if (job == null) {
            log.error("Job is null");
            throw new RuntimeException("Job is null");
        }
        return JobProto.newBuilder()
                .setJobId(job.getId())
                .setJobDriverId(job.getDriver() == null ? 0 : job.getDriver().getDriverId())
                .setJobDispatcherId(job.getDispatcher() == null ? 0 : job.getDispatcher().getDispatcherId())
                .setTitle(job.getTitle() == null ? "" : job.getTitle())
                .setCargoInfo(job.getCargoInfo() == null ? "" : job.getCargoInfo())
                .setDescription(job.getDescription() == null ? "": job.getDescription())
                .setDeliveryTime(job.getDeliveryTime() == null ? parseToTimestampProto(Instant.now()) : parseToTimestampProto(job.getDeliveryTime()))
                .setPickUpTime(job.getPickupTime() == null ? parseToTimestampProto(Instant.now()) : parseToTimestampProto(job.getPickupTime()))
                .setDropLocationState(job.getDropLocationState() == null ? "AL" : job.getDropLocationState())
                .setDropLocationZipCode(job.getDropLocationZipCode() == 0 ? 35010 : job.getDropLocationZipCode())
                .setPickUpLocationState(job.getPickupLocationState() ==null ? "AL" : job.getPickupLocationState())
                .setPickUpLocationZipCode(job.getPickupLocationZipCode() == 0 ? 35010 : job.getPickupLocationZipCode())
                .setLoadedMiles(job.getLoadedMiles())
                .setTotalPrice(job.getTotalPrice() == null ? 0 : job.getTotalPrice())
                .setWeightOfCargo(job.getWeightOfCargo())
                .setJobTrailerType(job.getTrailerTypeNeeded() == null ? TrailerTypeProto.DRY_VAN : parseTrailerTypeProto(job.getTrailerTypeNeeded()))
                .setCurrentJobStatus(job.getCurrentJobStatus() == null ? JobStatusProto.JOB_AVAILABLE : parseJobStatusToProto(job.getCurrentJobStatus()))
                .build();
    }
    public static Instant  parseFromTimestampProto(Timestamp timestamp)
    {
        if (timestamp == null) {
            log.error("Timestamp is null");
            throw new RuntimeException("Timestamp is null");
        }
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
    public static Timestamp parseToTimestampProto(Instant instant)
    {
        if (instant == null) {
            log.error("Instant is null");
            throw new RuntimeException("Instant is null");
        }
        return Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
    }
    public static JobStatusProto parseJobStatusToProto(JobStatus jobStatus)
    {
        if (jobStatus == null) {
            log.error("JobStatus is null");
            throw new RuntimeException("JobStatus is null");
        }
        return switch (jobStatus)
        {
            case assigned ->  JobStatusProto.JOB_ASSIGNED;
            case completed ->  JobStatusProto.JOB_COMPLETED;
            case expired ->   JobStatusProto.JOB_EXPIRED;
            case ongoing ->   JobStatusProto.JOB_ONGOING;
            case available ->   JobStatusProto.JOB_AVAILABLE;
            case null, default ->
                throw new RuntimeException("Unknown job status");
        };
    }
    public static JobStatus parseJobStatusFromProto(JobStatusProto jobStatus)
    {
        if (jobStatus == null) {
            log.error("JobStatusProto is null");
            throw new RuntimeException("JobStatus is null");
        }
        return switch (jobStatus)
        {
            case JOB_AVAILABLE ->   JobStatus.available;
            case JOB_ASSIGNED ->   JobStatus.assigned;
            case JOB_COMPLETED ->   JobStatus.completed;
            case JOB_EXPIRED ->   JobStatus.expired;
            case JOB_ONGOING ->   JobStatus.ongoing;
            case null, default ->
                throw new RuntimeException("Unknown job status");
        };
    }
}
