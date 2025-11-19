package dk.via.fleetforward.networking.handlers;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import dk.via.fleetforward.gRPC.Fleetforward;
import dk.via.fleetforward.gRPC.Fleetforward.DriverProto;
import dk.via.fleetforward.gRPC.Fleetforward.HandlerTypeProto;
import dk.via.fleetforward.gRPC.Fleetforward.ActionTypeProto;
import dk.via.fleetforward.services.user.DriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Mario, Patrik
 * @version 1.0.0
 * The DriverHandler class implements the FleetNetworkHandler interface and is responsible
 * for handling actions related to driver management. It acts as an adapter between the
 * fleet network commands and the underlying DriverService to perform operations on drivers.
 */
@Service public class DriverHandler implements FleetNetworkHandler
{

    private static final Logger log = LoggerFactory.getLogger(DriverHandler.class);
    private final DriverService driverService;

  public DriverHandler(DriverService driverService)
  {
    this.driverService = driverService;
  }

  @Override public HandlerTypeProto getType()
  {
    return HandlerTypeProto.HANDLER_DRIVER;
  }

  @Override public Message handle(ActionTypeProto actionType, Object payload)
  {
    Message proto = null;
    Any payloadAny = (Any) payload;
    DriverProto request = null;

    try
    {
      request = payloadAny.unpack(DriverProto.class);
      log.info("Received request {}", request);
    }
    catch (InvalidProtocolBufferException e)
    {
        log.error("Error unpacking DriverProto", e);
      throw new RuntimeException(e);
    }

    switch (actionType)
    {
      case ACTION_CREATE ->
      {
        proto = driverService.create(request);
        log.info("Created driver {}", proto);
      }
      case ACTION_UPDATE ->
      {
        driverService.update(request);
        log.info("Updated driver {}", request);
      }

      case ACTION_DELETE ->
      {
        driverService.delete(request.getUser().getId());
        log.info("Deleted driver {}", request);
      }
      case ACTION_GET ->
      {
        proto = driverService.getSingle(request.getUser().getId());
        log.info("Fetched driver {}", proto);
      }
      case ACTION_LIST ->
      {
        proto = driverService.getAll();
        log.info("Fetched all drivers {}", proto);
      }
      default ->
      {
        throw new IllegalArgumentException(
            "Invalid action type: " + actionType);
      }
    }
    if (proto == null)
    {
      proto = Fleetforward.DriverProto.newBuilder().build();
    }
    log.info("Returning proto {}", proto);
    return Any.pack(proto);

  }
}
