package dk.via.fleetforward.model;

import dk.via.fleetforward.utility.StringUtility;
import jakarta.persistence.*;

/**
 * Company entity class for storing company information
 * @implNote This class is mapped to the database table 'company'
 * @see StringUtility
 */
@Entity
@Table(name = "company", schema = "fleetforward")
public class Company {
    @Id
    @Column(name = "mc_number", nullable = false, unique = true)
    String mcNumber;
    @Column(name = "company_name", nullable = false)
    String companyName;
    /**
     * Get the mc number of the company
     * @return The mc number
     */
    public String getMcNumber() {
        return mcNumber;
    }

    /**
     * Set the mc number of the company
     * @param mcNumber The mc number to set
     * @implNote MC number must be 10 characters long
     * @throws IllegalArgumentException if the mc number is null or empty or not 10 characters long
     */
    public void setMcNumber(String mcNumber) {
        if (StringUtility.isNullOrEmpty(mcNumber)) {
            throw new IllegalArgumentException("MC number cannot be null or empty");
        }
        else if (mcNumber.length() != 10) {
            throw new IllegalArgumentException("MC number must be 10 characters long");
        }
        this.mcNumber = mcNumber;
    }

    /**
     * Get the name of the company
     * @return The name of the company
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Set the name of the company
     * @param companyName The name of the company
     */
    public void setCompanyName(String companyName) {
        if (StringUtility.isNullOrEmpty(companyName)) {
            throw new IllegalArgumentException("Company name cannot be null or empty");
        }
        else if (companyName.length() > 50) {
            throw new IllegalArgumentException("Company name must be 50 characters or less");
        }
        this.companyName = companyName;
    }
}
