package dk.via.fleetforward.networking.handlers;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import dk.via.fleetforward.gRPC.Fleetforward;
import dk.via.fleetforward.gRPC.Fleetforward.DriverProto;
import dk.via.fleetforward.gRPC.Fleetforward.HandlerTypeProto;
import dk.via.fleetforward.gRPC.Fleetforward.ActionTypeProto;
import dk.via.fleetforward.services.user.DriverService;
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
    }
    catch (InvalidProtocolBufferException e)
    {
      throw new RuntimeException(e);
    }

    switch (actionType)
    {
      case ACTION_CREATE ->
      {
        proto = driverService.create(request);
      }
      case ACTION_UPDATE ->
      {
        driverService.update(request);
      }

      case ACTION_DELETE ->
      {
        driverService.delete(request.getUser().getId());
      }
      case ACTION_GET ->
      {
        proto = driverService.getSingle(request.getUser().getId());
      }
      case ACTION_LIST ->
      {
        proto = driverService.getAll();
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
    return Any.pack(proto);

  }
}
