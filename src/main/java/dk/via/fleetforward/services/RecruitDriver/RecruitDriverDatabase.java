package dk.via.fleetforward.services.RecruitDriver;

import dk.via.fleetforward.gRPC.Fleetforward.RecruitDriverProto;
import dk.via.fleetforward.gRPC.Fleetforward.DriverProto;
import dk.via.fleetforward.gRPC.Fleetforward.DriverListProto;
import dk.via.fleetforward.model.Dispatcher;
import dk.via.fleetforward.model.Driver;
import dk.via.fleetforward.model.DriversManagedByDispatcher;
import dk.via.fleetforward.model.User;
import dk.via.fleetforward.repositories.database.DispatcherRepository;
import dk.via.fleetforward.repositories.database.DriversManagedByDispatcherRepository;
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
public class RecruitDriverDatabase implements RecruitDriverService {
    private static final Logger log = LoggerFactory.getLogger(RecruitDriverDatabase.class);
    private final DriversManagedByDispatcherRepository recruitRepository;
    private final UserRepository userRepository;
    private final DispatcherRepository dispatcherRepository;
    public RecruitDriverDatabase(DriversManagedByDispatcherRepository recruitRepository
            ,UserRepository userRepository
            ,DispatcherRepository dispatcherRepository) {
        this.dispatcherRepository = dispatcherRepository;
        this.recruitRepository = recruitRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public DriverProto recruitDriver(RecruitDriverProto proto) {
        DriversManagedByDispatcher newDriver = new DriversManagedByDispatcher(
                new Dispatcher(proto.getDispatcher(), proto.getDispatcher().getUser().getId())
                , new Driver(proto.getDriver(), proto.getDriver().getUser().getId())
        );
        log.info("Assigning Driver {}", newDriver.getDriver());
        DriversManagedByDispatcher created = recruitRepository.save(newDriver);
        log.info("Driver assigned to dispatcher{}", created.getDispatcher());
        Driver driver = created.getDriver();
        User user = userRepository.findById(driver.getDriverId()).orElseThrow(()->new RuntimeException("Driver not found"));
        return ProtoUtils.parseDriverProto(driver, user);

    }

    @Override
    @Transactional
    public void fireDriver(RecruitDriverProto proto) {
        DriversManagedByDispatcher firedDriver = new DriversManagedByDispatcher(
                new Dispatcher(proto.getDispatcher(), proto.getDispatcher().getUser().getId())
                , new Driver(proto.getDriver(), proto.getDriver().getUser().getId())
        );
        recruitRepository.deleteById(firedDriver.getId());
    }

    @Override
    @Transactional
    public DriverListProto getDispatcherDriversList(int id) {
        Dispatcher dispatcher = dispatcherRepository.findById(id).orElseThrow(()->new RuntimeException("Dispatcher not found"));
        List<Driver> recruits =
                recruitRepository.findAllByDispatcher(dispatcher)
                        .stream()
                        .map(DriversManagedByDispatcher::getDriver)
                        .toList();
        List<Integer> userIds =
                recruits.stream()
                        .map(Driver::getDriverId)
                        .toList();
        List<User> users = userRepository.findAllById(userIds);
        Map<Integer, Driver> driverMap = recruits.stream()
                .collect(Collectors.toMap(Driver::getDriverId, Function.identity()));
        DriverListProto.Builder builder = DriverListProto.newBuilder();
        for (User user : users) {
            Driver driver = driverMap.get(user.getId());
            if (driver != null) {
                builder.addDrivers(ProtoUtils.parseDriverProto(driver, user));
            }
        }
        log.info("Found {} drivers for dispatcher {}", recruits.size(), dispatcher);
        return builder.build();
    }
}
