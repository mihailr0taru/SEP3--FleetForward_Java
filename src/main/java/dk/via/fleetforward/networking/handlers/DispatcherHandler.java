package dk.via.fleetforward.networking.handlers;

import com.google.protobuf.Message;
import dk.via.fleetforward.gRPC.Fleetforward.HandlerTypeProto;
import dk.via.fleetforward.gRPC.Fleetforward.ActionTypeProto;
import org.springframework.stereotype.Service;

@Service
public class DispatcherHandler implements FleetNetworkHandler {

    @Override
    public HandlerTypeProto getType() {
        return null;
    }

    @Override
    public Message handle(ActionTypeProto actionType, Object payload) {
        return null;
    }
}
