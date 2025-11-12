package dk.via.fleetforward.networking.handlers;

import com.google.protobuf.Message;
import dk.via.fleetforward.gRPC.Fleetforward.ActionTypeProto;
import dk.via.fleetforward.gRPC.Fleetforward.HandlerTypeProto;

/**
 * FleetNetworkHandler interface for handling actions from the fleet network
 */
public interface FleetNetworkHandler {
    HandlerTypeProto getType();
    Message handle(ActionTypeProto actionType, Object payload);
}
