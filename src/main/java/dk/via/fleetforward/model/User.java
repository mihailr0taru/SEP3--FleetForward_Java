package dk.via.fleetforward.model;

import dk.via.fleetforward.model.Enums.UserRole;
import jakarta.persistence.*;

import dk.via.fleetforward.gRPC.Fleetforward.UserProto;

/**
 * User entity class for storing user information
 * @implNote This class is mapped to the database table 'app_user'
 */
@Entity
@Table(name = "app_user", schema = "fleetforward")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name", unique = true, nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;


    public User(UserProto tempUser) {
        setId(tempUser.getId());
        setFirstName(tempUser.getFirstName());
        setLastName(tempUser.getLastName());
        setEmail(tempUser.getEmail());
        setPhoneNumber(tempUser.getPhoneNumber());
    }

    public User() {};

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return hashedPassword;
    }

    public void setPassword(String password) {
        this.hashedPassword = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
