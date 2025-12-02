package dk.via.fleetforward.services.user;

import dk.via.fleetforward.gRPC.Fleetforward.DriverProto;
import dk.via.fleetforward.gRPC.Fleetforward.DriverListProto;
import dk.via.fleetforward.model.Driver;
import dk.via.fleetforward.model.Enums.UserRole;
import dk.via.fleetforward.model.User;
import dk.via.fleetforward.repositories.database.DriverRepository;
import dk.via.fleetforward.repositories.database.UserRepository;
import dk.via.fleetforward.utility.ProtoUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DriverServiceDatabase implements DriverService{
    private static final Logger log = LoggerFactory.getLogger(DriverServiceDatabase.class);
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    public DriverServiceDatabase(UserRepository userRepository, DriverRepository driverRepository) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    @Transactional
    public DriverProto create(DriverProto payload) {
        User user = new User(payload.getUser());
        user.setId(null);//create not update
        // if id is not null and an entry with the same id exist JPA will update instead of create
        user.setRole(UserRole.driver);
        User createdUser = userRepository.save(user);
        log.info("Created user {}", createdUser);

        Driver driver = new Driver(payload,createdUser.getId());
        Driver createdDriver = driverRepository.save(driver);
        log.info("Created driver {}", createdDriver);
        return ProtoUtils.parseDriverProto(createdDriver, createdUser);
    }

    @Override
    @Transactional
    public void update(DriverProto payload) {
        userRepository.findById(payload.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found, user must be created first"));
        driverRepository.findById(payload.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Driver not found, driver must be created first"));

        User user = new User(payload.getUser());
        user.setRole(UserRole.driver);
        userRepository.save(user);
        log.info("Updated user {}", user);

        Driver driver = new Driver(payload, user.getId());
        driverRepository.save(driver);
        log.info("Updated driver {}", driver);
    }

    @Override
    @Transactional
    public void delete(int driverId) {
        driverRepository.deleteById(driverId);
        log.info("Deleted driver {}", driverId);
        userRepository.deleteById(driverId);
        log.info("Deleted user {}", driverId);
    }

    @Override
    @Transactional
    public DriverProto getSingle(int driverId) {
        User fetchedUser = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("User not found, user must be created first"));
        Driver fetchedDriver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found, driver must be created first"));
        log.info("Fetched driver {}", fetchedDriver);
        return ProtoUtils.parseDriverProto(fetchedDriver, fetchedUser);
    }

    @Override
    @Transactional
    public DriverListProto getAll() {
        List<User> users = userRepository.findAllByRole(UserRole.driver);
        List<Driver> drivers = driverRepository.findAll();
        log.info("Fetched {} users", users.size());
        log.info("Fetched {} drivers", drivers.size());
        // Map driverId -> Driver
        Map<Integer, Driver> driverMap = drivers.stream()
                .collect(Collectors.toMap(Driver::getDriverId, Function.identity()));

        DriverListProto.Builder builder = DriverListProto.newBuilder();
        for (User user : users) {
            Driver driver = driverMap.get(user.getId());
            if (driver != null) {
                builder.addDrivers(ProtoUtils.parseDriverProto(driver, user));
                log.info("Added driver {}", driver);
            }
        }
        log.info("Created proto driver list");

        return builder.build();
    }
}
