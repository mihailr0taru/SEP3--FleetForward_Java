package dk.via.fleetforward.services.auth;

import com.google.protobuf.Message;
import dk.via.fleetforward.gRPC.Fleetforward.UserProto;
import dk.via.fleetforward.model.User;
import dk.via.fleetforward.repositories.database.UserRepository;
import dk.via.fleetforward.services.user.DispatcherService;
import dk.via.fleetforward.services.user.DriverService;
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
    public Message login(UserProto user) {
        User fetchedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found, user must be created first"));
        if (user.getPassword().equals(fetchedUser.getPassword())) {
            log.info("User {} logged in", fetchedUser);
            switch (fetchedUser.getRole()) {
                case driver:
                    log.info("Driver {} logged in", fetchedUser);
                    return driverService.getSingle(user.getId());
                case dispatcher:
                    log.info("Dispatcher {} logged in", fetchedUser);
                    return dispatcherService.getSingle(user.getId());
                default:
                    log.warn("Unsupported role");
                    throw new RuntimeException("Unsupported role");
            }
        }
        log.warn("Invalid password for user {}", fetchedUser);
        throw new RuntimeException("Invalid password");
    }
}
