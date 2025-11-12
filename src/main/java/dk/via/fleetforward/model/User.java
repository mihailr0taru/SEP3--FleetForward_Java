package dk.via.fleetforward.model;

import jakarta.persistence.*;

/**
 * User entity class for storing user information
 * @implNote This class is mapped to the database table 'app_user'
 */
@Entity
@Table(name = "app_user", schema = "fleetforward")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(name = "first_name", unique = true, nullable = false)
    String firstName;
    @Column(name = "last_name", nullable = false)
    String lastName;
    @Column(name = "email", nullable = false)
    String email;
    @Column(name = "phone_number", unique = true, nullable = false)
    String phoneNumber;
    @Column(name = "hashed_password", nullable = false)
    String password;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    UserRole role;

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
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
