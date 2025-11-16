package dk.via.fleetforward.services.user;

import dk.via.fleetforward.gRPC.Fleetforward;
import dk.via.fleetforward.model.User;
import dk.via.fleetforward.repositories.database.UserRepository;
import org.springframework.stereotype.Service;
//TODO implement database operations for DispatcherService interface
//Richard/Mihai
@Service
public class DispatcherServiceDatabase implements DispatcherService{
    private final UserRepository userRepository;
    //ADD DISPATCHER REPOSITORY HERE
    public DispatcherServiceDatabase(UserRepository userRepository) {
        this.userRepository = userRepository;
        //ADD DISPATCHER REPOSITORY INITIALIZATION HERE
    }
    @Override
    public Fleetforward.DispatcherProto create(Fleetforward.DispatcherProto dispatcher) {
        return null;
    }

    @Override
    public void update(Fleetforward.DispatcherProto dispatcher) {

    }

    @Override
    public void delete(int id) {

    }

    @Override
    public Fleetforward.DispatcherProto getSingle(int id) {
        return null;
    }

    @Override
    public Fleetforward.DispatcherListProto getAll() {
        return null;
    }
}
