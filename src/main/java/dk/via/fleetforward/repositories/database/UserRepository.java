package dk.via.fleetforward.repositories.database;

import dk.via.fleetforward.model.Enums.UserRole;
import dk.via.fleetforward.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findAllByRole(UserRole role);
}
