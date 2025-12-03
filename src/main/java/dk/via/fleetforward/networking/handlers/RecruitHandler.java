package dk.via.fleetforward.networking.handlers;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import dk.via.fleetforward.gRPC.Fleetforward.RecruitDriverProto;
import dk.via.fleetforward.gRPC.Fleetforward.HandlerTypeProto;
import dk.via.fleetforward.gRPC.Fleetforward.ActionTypeProto;
import dk.via.fleetforward.services.RecruitDriver.RecruitDriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RecruitHandler implements FleetNetworkHandler{

    private static final Logger log = LoggerFactory.getLogger(RecruitHandler.class);
    private final RecruitDriverService recruitService;
    public RecruitHandler(RecruitDriverService recruitService) {
        this.recruitService = recruitService;
    }
    @Override
    public HandlerTypeProto getType() {
        return HandlerTypeProto.HANDLER_RECRUIT;
    }

    @Override
    public Message handle(ActionTypeProto actionType, Object payload) {
        Message proto = null;
        Any payloadAny = (Any) payload;
        RecruitDriverProto request = null;
        try
        {
            request = payloadAny.unpack(RecruitDriverProto.class);
            log.info("Received request {}", request);
        }
        catch (InvalidProtocolBufferException e)
        {
            log.error("Error unpacking RecruitDriverProto", e);
            throw new RuntimeException(e);
        }

        switch (actionType)
        {
            case ACTION_CREATE, ACTION_UPDATE ->
            {
                proto = recruitService.recruitDriver(request);
                log.info("Recruited driver {}", proto);
            }
            case ACTION_DELETE ->
            {
                recruitService.fireDriver(request);
                log.info("Fired driver {}", request.getDriver());
            }
            case ACTION_GET ->
            {
                proto = recruitService.getDispatcherDriversList(request.getDispatcher().getUser().getId());
                log.info("Fetched dispatcher list of drivers {}", proto);
            }
            case ACTION_LIST ->
            {
                proto = recruitService.getDriverListWoDispatcher();
                log.info("Fetched list of drivers without dispatcher {}", proto);
            }
            default ->
            {
                throw new IllegalArgumentException(
                        "Invalid action type: " + actionType);
            }
        }
        if (proto == null)
        {
            proto = RecruitDriverProto.newBuilder().build();
        }
        log.info("Returning proto {}", proto);
        return Any.pack(proto);
    }
}
