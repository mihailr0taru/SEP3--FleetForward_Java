package dk.via.fleetforward.networking.handlers;

import com.google.protobuf.Message;
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
        if(!(payload instanceof UserProto))
        {
            log.error("Invalid payload, must be a UserProto");
            throw new RuntimeException("Invalid payload, must be a UserProto");
        }
        UserProto user = (UserProto) payload;
        log.info("Authenticating user {}", user);
        return authentificationService.login(user);
    }
}
