package dk.via.fleetforward.repositories.database;

import dk.via.fleetforward.model.Dispatcher;
import dk.via.fleetforward.model.Driver;
import dk.via.fleetforward.model.DriversManagedByDispatcher;
import dk.via.fleetforward.model.DriversManagedByDispatcherId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriversManagedByDispatcherRepository extends JpaRepository<DriversManagedByDispatcher, DriversManagedByDispatcherId> {
    List<DriversManagedByDispatcher> findAllByDispatcher(Dispatcher dispatcher);
}
