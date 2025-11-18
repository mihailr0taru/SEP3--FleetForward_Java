package dk.via.fleetforward.services.user;

import dk.via.fleetforward.gRPC.Fleetforward;
import dk.via.fleetforward.gRPC.Fleetforward.DriverProto;
import dk.via.fleetforward.gRPC.Fleetforward.DriverListProto;
import dk.via.fleetforward.model.Driver;
import dk.via.fleetforward.model.Enums.UserRole;
import dk.via.fleetforward.model.User;
import dk.via.fleetforward.repositories.database.DriverRepository;
import dk.via.fleetforward.repositories.database.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DriverServiceDatabase implements DriverService{
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    public DriverServiceDatabase(UserRepository userRepository, DriverRepository driverRepository) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public DriverProto create(DriverProto payload) {
        User user = new User(payload.getUser());
        user.setRole(UserRole.driver);
        User createdUser = userRepository.save(user);

        Driver driver = new Driver(payload);
        Driver createdDriver = driverRepository.save(driver);
        return Driver.makeDriverProto(createdDriver, createdUser);
    }

    @Override
    public void update(DriverProto payload) {
        userRepository.findById(payload.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found, user must be created first"));
        driverRepository.findById(payload.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Driver not found, driver must be created first"));

        User user = new User(payload.getUser());
        user.setRole(UserRole.driver);
        userRepository.save(user);

        Driver driver = new Driver(payload);
        driverRepository.save(driver);
    }

    @Override
    public void delete(int driverId) {
        driverRepository.deleteById(driverId);
        userRepository.deleteById(driverId);
    }

    @Override
    public DriverProto getSingle(int driverId) {
        User fetchedUser = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("User not found, user must be created first"));
        Driver fetchedDriver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found, driver must be created first"));

        return Driver.makeDriverProto(fetchedDriver, fetchedUser);
    }

    @Override
    public DriverListProto getAll() {
        List<User> users = userRepository.findAllByRole(UserRole.driver);
        List<Driver> drivers = driverRepository.findAll();

        // Map driverId -> Driver
        Map<Integer, Driver> driverMap = drivers.stream()
                .collect(Collectors.toMap(Driver::getDriverId, Function.identity()));

        DriverListProto.Builder builder = DriverListProto.newBuilder();

        for (User user : users) {
            Driver driver = driverMap.get(user.getId());
            if (driver != null) {
                builder.addDrivers(Driver.makeDriverProto(driver, user));
            }
        }

        return builder.build();
    }
}
