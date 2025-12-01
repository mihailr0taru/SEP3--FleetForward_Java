package dk.via.fleetforward.repositories.database;

import dk.via.fleetforward.model.DriversManagedByDispatcher;
import dk.via.fleetforward.model.DriversManagedByDispatcherId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface DriversManagedByDispatcherRepository extends JpaRepository<DriversManagedByDispatcher, DriversManagedByDispatcherId> {
}
