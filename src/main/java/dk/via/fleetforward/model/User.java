package dk.via.fleetforward.model;

import dk.via.fleetforward.utility.StringUtility;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * User entity class for storing user information
 * @implNote This class is mapped to the database table '...'
 * @see StringUtility
 */
@Entity
@Table(name = "app_user", schema = "fleetforward")

public class User {
    @Id
    Integer id;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    String password;

    /**
     * Get the id of the user
     * @return The id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Set the id of the user
     * @param id The id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get the first name of the user
     * @return The first name
     */
    public String getFirstName() {
        return firstName;
    }
}
