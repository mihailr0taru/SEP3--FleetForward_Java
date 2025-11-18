package dk.via.fleetforward.model;

import dk.via.fleetforward.gRPC.Fleetforward.UserProto;
import dk.via.fleetforward.gRPC.Fleetforward.DriverCompanyRoleProto;
import dk.via.fleetforward.gRPC.Fleetforward.TrailerTypeProto;
import dk.via.fleetforward.gRPC.Fleetforward.StatusDriverProto;
import dk.via.fleetforward.gRPC.Fleetforward.DriverProto;
import dk.via.fleetforward.model.Enums.DriverCompanyRole;
import dk.via.fleetforward.model.Enums.StatusDriver;
import dk.via.fleetforward.model.Enums.TrailerType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;

/**
 * Driver entity class for storing user information
 *
 * @implNote This class is mapped to the database table 'app_user'
 */
@Entity
@Table(name = "driver", schema = "fleetforward")
public class Driver {
    @Id
    private Integer driverId;

    @Column(name = "company_mc_number", nullable = false)
    private String companyMcNumber;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "fleetforward.driver_status", nullable = false)
    private StatusDriver status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "current_trailer_type", columnDefinition = "fleetforward.trailer_type", nullable = false)
    private TrailerType currentTrailerType;

    @Column(name = "current_location_state", nullable = false)
    private String currentLocationState;

    @Column(name = "current_location_zip_code", nullable = false)
    private int currentLocationZipCode;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role_in_company", columnDefinition = "fleetforward.driver_company_role", nullable = false)
    private DriverCompanyRole roleInCompany;

    public Driver() {}

    public Driver(DriverProto driverProto) {
        setDriverId(driverProto.getUser().getId());
        setCompanyMcNumber(driverProto.getCompanyMcNumber());
        setCurrentLocationState(driverProto.getCurrentState());
        setCurrentLocationZipCode(driverProto.getCurrentZIPCODE());
        switch (driverProto.getDriverStatus())
        {
            case AVAILABLE:
                setStatus(StatusDriver.available);
                break;
            case BUSY:
                setStatus(StatusDriver.busy);
                break;
            case OFF_DUTY:
                setStatus(StatusDriver.off_duty);
                break;
            default:
                throw new RuntimeException("Unknown status");
        }
        switch (driverProto.getTrailerType())
        {
            case FLATBED:
                setCurrentTrailerType(TrailerType.flatbed);
                break;
            case REEFER:
                setCurrentTrailerType(TrailerType.reefer);
                break;
            case DRY_VAN:
                setCurrentTrailerType(TrailerType.dry_van);
                break;
            default:
                throw new RuntimeException("Unknown trailer type");
        }
        switch (driverProto.getCompanyRole())
        {
            case DRIVER:
                setDriverCompanyRole(DriverCompanyRole.driver);
                break;
            case OWNER_OPERATOR:
                setDriverCompanyRole(DriverCompanyRole.owner_operator);
                break;
            default:
                throw new RuntimeException("Unknown role");
        }
    }
    public String getCurrentLocationState() {
        return currentLocationState;
    }

    public void setCurrentLocationState(String currentLocationState) {
        this.currentLocationState = currentLocationState;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driver_id) {
        this.driverId = driver_id;
    }

    public String getCompanyMcNumber() {
        return companyMcNumber;
    }

    public void setCompanyMcNumber(String companyMcNumber) {
        this.companyMcNumber = companyMcNumber;
    }

    public StatusDriver getStatus() {
        return status;
    }

    public void setStatus(StatusDriver status) {
        this.status = status;
    }

    public TrailerType getCurrentTrailerType() {
        return currentTrailerType;
    }

    public void setCurrentTrailerType(TrailerType currentTrailerType) {
        this.currentTrailerType = currentTrailerType;
    }

    public int getCurrentLocationZipCode() {
        return currentLocationZipCode;
    }

    public void setCurrentLocationZipCode(int currentLocationZipCode) {
        this.currentLocationZipCode = currentLocationZipCode;
    }

    public DriverCompanyRole getDriverCompanyRole() {
        return roleInCompany;
    }

    public void setDriverCompanyRole(DriverCompanyRole driverCompanyRole) {
        this.roleInCompany = driverCompanyRole;
    }

    public static DriverProto makeDriverProto(Driver driver, User user) {
        return DriverProto.newBuilder()
                .setCompanyMcNumber(driver.getCompanyMcNumber())
                .setDriverStatus(
                        switch (driver.status) {
                            case available -> StatusDriverProto.AVAILABLE;
                            case busy -> StatusDriverProto.BUSY;
                            case off_duty -> StatusDriverProto.OFF_DUTY;
                            case null, default -> throw new RuntimeException("Unknown status");
                        }
                )
                .setCompanyRole(
                        switch (driver.roleInCompany) {
                            case driver -> DriverCompanyRoleProto.DRIVER;
                            case owner_operator -> DriverCompanyRoleProto.OWNER_OPERATOR;
                            case null, default -> throw new RuntimeException("Unknown role");
                        }
                )
                .setTrailerType(
                        switch (driver.currentTrailerType) {
                            case flatbed -> TrailerTypeProto.FLATBED;
                            case dry_van -> TrailerTypeProto.DRY_VAN;
                            case reefer -> TrailerTypeProto.REEFER;
                            case null, default -> throw new RuntimeException("Unknown trailer type");
                        }
                )
                .setCurrentState(driver.getCurrentLocationState())
                .setCurrentZIPCODE(driver.getCurrentLocationZipCode())
                .setUser(User.makeUserProto(user))
                .build();
    }
}
