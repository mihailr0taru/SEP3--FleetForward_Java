package dk.via.fleetforward.model;

import dk.via.fleetforward.gRPC.Fleetforward;
import dk.via.fleetforward.model.Enums.JobStatus;
import dk.via.fleetforward.model.Enums.TrailerType;
import dk.via.fleetforward.utility.ProtoUtils;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "type_of_trailer_needed", columnDefinition = "fleetforward.trailer_type", nullable = false)
  private TrailerType TrailerTypeNeeded;

  @Column(name = "total_price", nullable = false) private Integer totalPrice;

  @Column(name = "cargo_info", nullable = false, length = 30) private String cargoInfo;

  @Column(name = "pickup_time", nullable = false) private Instant pickupTime;
  @Column(name = "delivery_time", nullable = false) private Instant deliveryTime;
  @Column(name = "pickup_location_state", nullable = false)
  private String pickupLocationState;

  @Column(name = "pickup_location_zip_code", nullable = false)
  private int pickupLocationZipCode;

  @Column(name = "drop_location_state", nullable = false)
  private String dropLocationState;
  @Column(name = "drop_location_zip_code", nullable = false)
  private int dropLocationZipCode;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "current_job_status", columnDefinition = "fleetforward.job_status", nullable = false)
  private JobStatus currentJobStatus;


  public Job(){}

  public Job(Fleetforward.JobProto jobProto, int id) {
    setId(id);

    // Dispatcher
    if (jobProto.getJobDispatcherId() != 0) {
      Dispatcher dispatcher = new Dispatcher();
      dispatcher.setDispatcherId(jobProto.getJobDispatcherId());
      setDispatcher(dispatcher);
    }

    // Driver
    if (jobProto.getJobDriverId() != 0) {
      Driver driver = new Driver();
      driver.setDriverId(jobProto.getJobDriverId());
      setDriver(driver);
    }

    setTitle(jobProto.getTitle());
    setDescription(jobProto.getDescription());
    setLoadedMiles(jobProto.getLoadedMiles());
    setWeightOfCargo(jobProto.getWeightOfCargo());
    setTotalPrice(jobProto.getTotalPrice());
    setCargoInfo(jobProto.getCargoInfo());

    setTrailerTypeNeeded(ProtoUtils.parseTrailerType(jobProto.getJobTrailerType()));
    setCurrentJobStatus(ProtoUtils.parseJobStatus(jobProto.getCurrentJobStatus()));

    setPickupTime(Instant.ofEpochSecond(
        jobProto.getPickUpTime().getSeconds(),
        jobProto.getPickUpTime().getNanos()
    ));

    setDeliveryTime(Instant.ofEpochSecond(
        jobProto.getDeliveryTime().getSeconds(),
        jobProto.getDeliveryTime().getNanos()
    ));

    setPickupLocationState(jobProto.getPickUpLocationState());
    setPickupLocationZipCode(jobProto.getPickUpLocationZipCode());
    setDropLocationState(jobProto.getDropLocationState());
    setDropLocationZipCode(jobProto.getDropLocationZipCode());
  }

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

  public void setTrailerTypeNeeded(TrailerType TrailerTypeNeeded)
  {
    this.TrailerTypeNeeded = TrailerTypeNeeded;
  }

  public TrailerType getTrailerTypeNeeded()
  {
    return TrailerTypeNeeded;
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

  public void setPickupLocationState(String pState)
  {
    this.pickupLocationState = pState;
  }
  public String getPickupLocationState()
  {
    return pickupLocationState;
  }

  public void setPickupLocationZipCode(int pZipCode)
  {
    this.pickupLocationZipCode = pZipCode;
  }

  public int getPickupLocationZipCode()
  {
    return pickupLocationZipCode;
  }

  public void setDropLocationState(String dState)
  {
    this.dropLocationState = dState;
  }

  public String getDropLocationState()
  {
    return dropLocationState;
  }

  public void setDropLocationZipCode(int dZipCode)
  {
    this.dropLocationZipCode = dZipCode;
  }

  public int getDropLocationZipCode()
  {
    return dropLocationZipCode;
  }

  public void setCurrentJobStatus(JobStatus currentJobStatus)
  {
    this.currentJobStatus = currentJobStatus;
  }

  public JobStatus getCurrentJobStatus()
  {
    return currentJobStatus;
  }



}