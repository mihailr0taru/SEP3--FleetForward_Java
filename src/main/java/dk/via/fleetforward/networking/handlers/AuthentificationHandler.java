package dk.via.fleetforward.networking.handlers;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import dk.via.fleetforward.gRPC.Fleetforward;
import dk.via.fleetforward.gRPC.Fleetforward.UserProto;
import dk.via.fleetforward.gRPC.Fleetforward.HandlerTypeProto;
import dk.via.fleetforward.gRPC.Fleetforward.ActionTypeProto;
import dk.via.fleetforward.services.auth.AuthentificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthentificationHandler implements FleetNetworkHandler{
    private static final Logger log = LoggerFactory.getLogger(AuthentificationHandler.class);
    private final AuthentificationService authentificationService;
    public AuthentificationHandler(AuthentificationService authentificationService) {
        this.authentificationService = authentificationService;
    }
    @Override
    public HandlerTypeProto getType() {
        return HandlerTypeProto.HANDLER_AUTH;
    }

    @Override
    public Message handle(ActionTypeProto actionType, Object payload) {
        Message proto = null;
        Any payloadAny = (Any) payload;
        UserProto user = null;
        try {
            user = payloadAny.unpack(UserProto.class);
        } catch (InvalidProtocolBufferException e) {
            log.error("Error unpacking UserProto", e);
            throw new RuntimeException(e);
        }
        log.info("Authenticating user {}", user);
        return authentificationService.login(user);
    }
}
