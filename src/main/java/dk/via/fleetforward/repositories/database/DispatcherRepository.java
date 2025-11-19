package dk.via.fleetforward.repositories.database;

import dk.via.fleetforward.model.Dispatcher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispatcherRepository extends JpaRepository<Dispatcher, Integer> {
}
