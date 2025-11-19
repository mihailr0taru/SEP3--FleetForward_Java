package dk.via.fleetforward.services.user;

import dk.via.fleetforward.gRPC.Fleetforward.*;
import dk.via.fleetforward.model.Dispatcher;
import dk.via.fleetforward.model.Enums.UserRole;
import dk.via.fleetforward.model.User;
import dk.via.fleetforward.repositories.database.DispatcherRepository;
import dk.via.fleetforward.repositories.database.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

//TODO implement database operations for DispatcherService interface
//Mihai
@Service
public class DispatcherServiceDatabase implements DispatcherService{
    private static final Logger log = LoggerFactory.getLogger(DispatcherServiceDatabase.class);
    private final UserRepository userRepository;
    private final DispatcherRepository dispatcherRepository;
    public DispatcherServiceDatabase(UserRepository userRepository, DispatcherRepository dispatcherRepository) {
        this.userRepository = userRepository;
        this.dispatcherRepository = dispatcherRepository;
    }
    @Override
    @Transactional
    public DispatcherProto create(DispatcherProto payload)
    {
        User user = new User(payload.getUser());
        user.setRole(UserRole.dispatcher);
        User createdUser = userRepository.save(user);
        log.info("Created user {}", createdUser);

        Dispatcher dispatcher = new Dispatcher(payload, createdUser.getId());
        Dispatcher createdDispatcher = dispatcherRepository.save(dispatcher);
        log.info("Created dispatcher {}", createdDispatcher);
        return Dispatcher.makeDispatcherProto(createdDispatcher, createdUser);
    }

    @Override
    @Transactional
    public void update(DispatcherProto payload)
    {
        userRepository.findById(payload.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found, user must be created first"));
        dispatcherRepository.findById(payload.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Dispatcher not found, dispatcher must be created first"));

        User user = new User(payload.getUser());
        user.setRole(UserRole.dispatcher);
        userRepository.save(user);
        log.info("Updated user {}", user);

        Dispatcher dispatcher = new Dispatcher(payload, user.getId());
        dispatcherRepository.save(dispatcher);
        log.info("Updated dispatcher {}", dispatcher);

    }

    @Override
    @Transactional
    public void delete(int id)
    {
        dispatcherRepository.findById(id);
        log.info("Deleted dispatcher {}", id);
        userRepository.deleteById(id);
        log.info("Deleted user {}", id);
    }

    @Override
    public DispatcherProto getSingle(int id)
    {
        User fetchedUser =  userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found, user must be created first"));
        Dispatcher fetchedDispatcher = dispatcherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dispatcher not found, dispatcher must be created first"));
        log.info("Fetched dispatcher {}", fetchedDispatcher);
        return Dispatcher.makeDispatcherProto(fetchedDispatcher, fetchedUser);
    }

    @Override
    public DispatcherListProto getAll()
    {
        List<User> users = userRepository.findAllByRole(UserRole.dispatcher);
        log.info("Fetched {} users", users.size());
        List<Dispatcher> dispatchers = dispatcherRepository.findAll();
        log.info("Fetched {} dispatchers", dispatchers.size());

        Map<Integer, Dispatcher> dispatcherMap = dispatchers.stream()
                .collect(Collectors.toMap(Dispatcher::getDispatcherId, Function.identity()));

        DispatcherListProto.Builder builder = DispatcherListProto.newBuilder();
        for(User user : users)
        {
            Dispatcher dispatcher = dispatcherMap.get(user.getId());
            if(dispatcher != null)
            {
                builder.addDispatchers(Dispatcher.makeDispatcherProto(dispatcher, user));
                log.info("Added dispatcher {}", dispatcher);
            }
        }
        log.info("Created dispatchers list");

        return builder.build();
    }
}
