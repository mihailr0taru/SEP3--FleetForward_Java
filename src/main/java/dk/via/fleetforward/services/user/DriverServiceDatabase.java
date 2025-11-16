package dk.via.fleetforward.services.user;

import dk.via.fleetforward.gRPC.Fleetforward.DriverProto;
import dk.via.fleetforward.gRPC.Fleetforward.DriverListProto;
import dk.via.fleetforward.repositories.database.DriverRepository;
import dk.via.fleetforward.repositories.database.UserRepository;
import org.springframework.stereotype.Service;
//TODO implement database operations for DriverService interface
@Service
public class DriverServiceDatabase implements DriverService{
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    public DriverServiceDatabase(UserRepository userRepository, DriverRepository driverRepository) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public DriverProto create(DriverProto driver) {
        return null;
    }

    @Override
    public void update(DriverProto driver) {

    }

    @Override
    public void delete(int driverId) {

    }

    @Override
    public DriverProto getSingle(int driverId) {
        return null;
    }

    @Override
    public DriverListProto getAll() {
        return null;
    }
}
