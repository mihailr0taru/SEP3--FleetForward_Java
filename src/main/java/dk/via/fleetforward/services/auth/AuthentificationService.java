package dk.via.fleetforward.services.auth;

import com.google.protobuf.Message;
import dk.via.fleetforward.gRPC.Fleetforward.DriverProto;
import dk.via.fleetforward.gRPC.Fleetforward.UserProto;
import dk.via.fleetforward.gRPC.Fleetforward.DispatcherProto;

public interface AuthentificationService {
    Message login(UserProto user);
}
