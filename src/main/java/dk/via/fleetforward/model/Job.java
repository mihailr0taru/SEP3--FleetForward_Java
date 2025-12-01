package dk.via.fleetforward.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity @Table(name = "job", schema = "fleetforward") public class Job
{
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id", nullable = false) private Integer id;

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "dispatcher_id") private Dispatcher dispatcher;

  @ManyToOne(fetch = FetchType.LAZY) @ColumnDefault("0") @JoinColumn(name = "driver_id") private Driver driver;

  @Column(name = "title", nullable = false, length = 20) private String title;

  @Column(name = "description", nullable = false, length = 300) private String description;

  @Column(name = "loaded_miles", nullable = false) private Integer loadedMiles;

  @Column(name = "weight_of_cargo", nullable = false) private Integer weightOfCargo;

  @Column(name = "total_price", nullable = false) private Integer totalPrice;

  @Column(name = "cargo_info", nullable = false, length = 30) private String cargoInfo;

  @Column(name = "pickup_time", nullable = false) private Instant pickupTime;
  @Column(name = "delivery_time", nullable = false) private Instant deliveryTime;

  public Integer getId()
  {
    return id;
  }

  public void setId(Integer id)
  {
    this.id = id;
  }

  public Dispatcher getDispatcher()
  {
    return dispatcher;
  }

  public void setDispatcher(Dispatcher dispatcher)
  {
    this.dispatcher = dispatcher;
  }

  public Driver getDriver()
  {
    return driver;
  }

  public void setDriver(Driver driver)
  {
    this.driver = driver;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public Integer getLoadedMiles()
  {
    return loadedMiles;
  }

  public void setLoadedMiles(Integer loadedMiles)
  {
    this.loadedMiles = loadedMiles;
  }

  public Integer getWeightOfCargo()
  {
    return weightOfCargo;
  }

  public void setWeightOfCargo(Integer weightOfCargo)
  {
    this.weightOfCargo = weightOfCargo;
  }

  public Integer getTotalPrice()
  {
    return totalPrice;
  }

  public void setTotalPrice(Integer totalPrice)
  {
    this.totalPrice = totalPrice;
  }

  public String getCargoInfo()
  {
    return cargoInfo;
  }

  public void setCargoInfo(String cargoInfo)
  {
    this.cargoInfo = cargoInfo;
  }

  public Instant getPickupTime()
  {
    return pickupTime;
  }

  public void setPickupTime(Instant pickupTime)
  {
    this.pickupTime = pickupTime;
  }

  public Instant getDeliveryTime()
  {
    return deliveryTime;
  }

  public void setDeliveryTime(Instant deliveryTime)
  {
    this.deliveryTime = deliveryTime;
  }

/*
 TODO [Reverse Engineering] create field to map the 'type_of_trailer_needed' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "type_of_trailer_needed", columnDefinition = "trailer_type not null") private Object typeOfTrailerNeeded;
*/
/*
 TODO [Reverse Engineering] create field to map the 'current_job_status' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @ColumnDefault("'available'") @Column(name = "current_job_status", columnDefinition = "job_status not null") private Object currentJobStatus;
*/
}