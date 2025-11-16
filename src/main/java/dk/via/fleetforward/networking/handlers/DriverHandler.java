package dk.via.fleetforward.networking.handlers;

import com.google.protobuf.Message;
import dk.via.fleetforward.gRPC.Fleetforward.HandlerTypeProto;
import dk.via.fleetforward.gRPC.Fleetforward.ActionTypeProto;
import org.springframework.stereotype.Service;

/**
 * @author Mario, Patrik
 * @version 1.0.0
 * The DriverHandler class implements the FleetNetworkHandler interface and is responsible
 * for handling actions related to driver management. It acts as an adapter between the
 * fleet network commands and the underlying DriverService to perform operations on drivers.
 */
@Service
public class DriverHandler implements FleetNetworkHandler {
    @Override
    public HandlerTypeProto getType() {
        return null;
    }

    @Override
    public Message handle(ActionTypeProto actionType, Object payload) {
        return null;
    }
}
