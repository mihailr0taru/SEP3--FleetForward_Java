package dk.via.fleetforward.utility;

import dk.via.fleetforward.gRPC.Fleetforward.CompanyProto;
import dk.via.fleetforward.gRPC.Fleetforward.DispatcherProto;
import dk.via.fleetforward.gRPC.Fleetforward.UserProto;
import dk.via.fleetforward.gRPC.Fleetforward.DriverProto;
import dk.via.fleetforward.gRPC.Fleetforward.DriverCompanyRoleProto;
import dk.via.fleetforward.gRPC.Fleetforward.StatusDriverProto;
import dk.via.fleetforward.gRPC.Fleetforward.TrailerTypeProto;
import dk.via.fleetforward.model.Company;
import dk.via.fleetforward.model.Dispatcher;
import dk.via.fleetforward.model.Driver;
import dk.via.fleetforward.model.Enums.DriverCompanyRole;
import dk.via.fleetforward.model.Enums.StatusDriver;
import dk.via.fleetforward.model.Enums.TrailerType;
import dk.via.fleetforward.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            log.error("User is null");
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
                .build();
    }
    public static DispatcherProto parseDispatcherProto(Dispatcher dispatcher, User user)
    {
        return DispatcherProto.newBuilder()
                .setCurrentRate(dispatcher.getCommissionRate())
                .setUser(parseUserProto(user))
                .addAllDriversAssigned(dispatcher.getAssignedDrivers())
                .build();
    }
    public static CompanyProto parseCompanyProto(Company company)
    {
        return CompanyProto.newBuilder()
                .setMcNumber(company.getMcNumber())
                .setCompanyName(company.getCompanyName())
                .build();
    }
}
