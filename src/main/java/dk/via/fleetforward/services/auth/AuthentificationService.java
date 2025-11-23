package dk.via.fleetforward.services.auth;

import com.google.protobuf.Message;
import dk.via.fleetforward.gRPC.Fleetforward.UserProto;

public interface AuthentificationService {
    Message login(UserProto user);
}
