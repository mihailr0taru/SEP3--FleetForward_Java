package dk.via.fleetforward.services.auth;

import com.google.protobuf.Message;
import dk.via.fleetforward.gRPC.Fleetforward.UserProto;
import dk.via.fleetforward.model.User;
import dk.via.fleetforward.repositories.database.UserRepository;
import dk.via.fleetforward.services.user.DispatcherService;
import dk.via.fleetforward.services.user.DriverService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthentificationServiceProvider implements AuthentificationService {
    private static final Logger log = LoggerFactory.getLogger(AuthentificationServiceProvider.class);
    private final UserRepository userRepository;
    private final DriverService driverService;
    private final DispatcherService dispatcherService;
    public AuthentificationServiceProvider(UserRepository userRepository, DriverService driverService, DispatcherService dispatcherService) {
        this.userRepository = userRepository;
        this.driverService = driverService;
        this.dispatcherService = dispatcherService;
    }
    @Override
    @Transactional
    public Message login(UserProto user) {
        User fetchedUser = userRepository.findByEmail(user.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
            switch (fetchedUser.getRole()) {
                case driver:
                    log.info("Driver {} logged in", fetchedUser);
                    return driverService.getSingle(fetchedUser.getId());
                case dispatcher:
                    log.info("Dispatcher {} logged in", fetchedUser);
                    return dispatcherService.getSingle(fetchedUser.getId());
                default:
                    log.warn("Unsupported role");
                    throw new RuntimeException("Unsupported role");
        }
    }
}
