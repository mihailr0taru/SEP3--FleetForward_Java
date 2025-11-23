package dk.via.fleetforward.networking.handlers;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import dk.via.fleetforward.gRPC.Fleetforward.HandlerTypeProto;
import dk.via.fleetforward.gRPC.Fleetforward.ActionTypeProto;
import dk.via.fleetforward.services.user.DispatcherService;
import dk.via.fleetforward.gRPC.Fleetforward.DispatcherProto;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class DispatcherHandler implements FleetNetworkHandler {
    private final DispatcherService dispatcherService;
    private final Logger log = org.slf4j.LoggerFactory.getLogger(DispatcherHandler.class);
    public DispatcherHandler(DispatcherService dispatcherService) {
        this.dispatcherService = dispatcherService;
    }
    @Override
    public HandlerTypeProto getType() {
        return HandlerTypeProto.HANDLER_DISPATCHER;
    }
    @Override
    public Message handle(ActionTypeProto actionType, Object payload) {
        Message proto = null;
        Any payloadAny = (Any) payload;
        DispatcherProto request = null;
        try
        {
            request = payloadAny.unpack(DispatcherProto.class);
            log.info("Received request {}", request);
        }
        catch (InvalidProtocolBufferException e)
        {
            log.error("Error unpacking DispatcherProto", e);
            throw new RuntimeException(e);
        }

        switch (actionType)
        {
            case ACTION_CREATE ->
            {
                proto = dispatcherService.create(request);
                log.info("Created dispatcher {}", proto);
            }
            case ACTION_UPDATE ->
            {
                dispatcherService.update(request);
                log.info("Updated dispatcher {}", request);
            }

            case ACTION_DELETE ->
            {
                dispatcherService.delete(request.getUser().getId());
                log.info("Deleted dispatcher {}", request);
            }
            case ACTION_GET ->
            {
                log.info("Received request {}", request.getUser().getId());
                proto = dispatcherService.getSingle(request.getUser().getId());
                log.info("Fetched dispatcher {}", proto);
            }
            case ACTION_LIST ->
            {
                proto = dispatcherService.getAll();
                log.info("Fetched all dispatcher {}", proto);
            }
            default ->
            {
                throw new IllegalArgumentException(
                        "Invalid action type: " + actionType);
            }
        }
        if (proto == null)
        {
            proto = DispatcherProto.newBuilder().build();
        }
        log.info("Returning proto {}", proto);
        return Any.pack(proto);
    }
}
