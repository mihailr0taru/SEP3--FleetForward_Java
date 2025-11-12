package dk.via.fleetforward.model;

import jakarta.persistence.*;

/**
 * Driver entity class for storing user information
 * @implNote This class is mapped to the database table 'app_user'
 */
@Entity
@Table(name = "driver", schema = "fleetforward")

public class Driver
{
  @Id int driver_id;
  @Column(name = "company_mc_number", nullable = false)
  String companyMcNumber;

  @Column(name = "status", nullable = false)
  String status;
  @Column(name = "current_trailer_type",  nullable = false)
  String currentTrailerType;

  @Column(name = "current_location_state", nullable = false)
  String currentLocationState;

  @Column(name = "current_location_zip_code", nullable = false)
  String currentLocationZipCode;

  @Column(name = "role_in_company", nullable = false)
  UserRole roleInCompany;

  public String getCurrentLocationState()
  {
    return currentLocationState;
  }

  public void setCurrentLocationState(String currentLocationState)
  {
    this.currentLocationState = currentLocationState;
  }

  public int getDriver_id()
  {
    return driver_id;
  }

  public void setDriver_id(int driver_id)
  {
    this.driver_id = driver_id;
  }

  public String getCompanyMcNumber()
  {
    return companyMcNumber;
  }

  public void setCompanyMcNumber(String companyMcNumber)
  {
    this.companyMcNumber = companyMcNumber;
  }

  public String getStatus()
  {
    return status;
  }

  public void setStatus(String status)
  {
    this.status = status;
  }

  public String getCurrentTrailerType()
  {
    return currentTrailerType;
  }

  public void setCurrentTrailerType(String currentTrailerType)
  {
    this.currentTrailerType = currentTrailerType;
  }

  public String getCurrentLocationZipCode()
  {
    return currentLocationZipCode;
  }

  public void setCurrentLocationZipCode(String currentLocationZipCode)
  {
    this.currentLocationZipCode = currentLocationZipCode;
  }

  public UserRole getRoleInCompany()
  {
    return roleInCompany;
  }

  public void setRoleInCompany(UserRole roleInCompany)
  {
    this.roleInCompany = roleInCompany;
  }
}
