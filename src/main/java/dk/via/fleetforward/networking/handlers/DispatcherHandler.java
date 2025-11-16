package dk.via.fleetforward.networking.handlers;

import com.google.protobuf.Message;
import dk.via.fleetforward.gRPC.Fleetforward.HandlerTypeProto;
import dk.via.fleetforward.gRPC.Fleetforward.ActionTypeProto;
import dk.via.fleetforward.services.user.DispatcherService;
import dk.via.fleetforward.gRPC.Fleetforward.DispatcherProto;
import org.springframework.stereotype.Service;

@Service
public class DispatcherHandler implements FleetNetworkHandler {
    private final DispatcherService dispatcherService;
    public DispatcherHandler(DispatcherService dispatcherService) {
        this.dispatcherService = dispatcherService;
    }
    @Override
    public HandlerTypeProto getType() {
        return HandlerTypeProto.HANDLER_DISPATCHER;
    }

    @Override
    public Message handle(ActionTypeProto actionType, Object payload) {
        return null;
    }
}
