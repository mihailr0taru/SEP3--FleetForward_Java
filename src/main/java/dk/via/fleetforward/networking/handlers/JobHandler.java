package dk.via.fleetforward.networking.handlers;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import dk.via.fleetforward.gRPC.Fleetforward;
import dk.via.fleetforward.gRPC.Fleetforward.JobProto;
import dk.via.fleetforward.gRPC.Fleetforward.HandlerTypeProto;
import dk.via.fleetforward.gRPC.Fleetforward.ActionTypeProto;
import dk.via.fleetforward.services.job.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobHandler implements FleetNetworkHandler{
    private static final Logger log = LoggerFactory.getLogger(DriverHandler.class);
    private final JobService jobService;
    public JobHandler(JobService jobService) {

        this.jobService = jobService;
    }
    @Override
    public HandlerTypeProto getType() {
        return HandlerTypeProto.HANDLER_JOB;
    }

    @Override
    public Message handle(ActionTypeProto actionType, Object payload) {

        Message proto = null;
        Any payloadAny = (Any) payload;
        Fleetforward.JobProto request = null;

        try
        {
            request = payloadAny.unpack(Fleetforward.JobProto.class);
            log.info("Received request {}", request);
        }
        catch (InvalidProtocolBufferException e)
        {
            log.error("Error unpacking JobProto", e);
            throw new RuntimeException(e);
        }

        switch (actionType)
        {
            case ACTION_CREATE ->
            {
                proto = jobService.create(request);
                log.info("Created job {}", proto);
            }
            case ACTION_UPDATE ->
            {
                jobService.update(request);
                log.info("Updated job {}", request);
            }

            case ACTION_DELETE ->
            {
                jobService.delete(request.getJobId());
                log.info("Deleted job {}", request);
            }
            case ACTION_GET ->
            {
                log.info("Received request {}", request.getJobId());
                proto = jobService.getSingle(request.getJobId());
                log.info("Fetched job {}", proto);
            }
            case ACTION_LIST ->
            {
                proto = jobService.getAll();
                log.info("Fetched all job {}", proto);
            }
            default ->
            {
                throw new IllegalArgumentException(
                        "Invalid action type: " + actionType);
            }
        }
        if (proto == null)
        {
            proto = Fleetforward.JobProto.newBuilder().build();
        }
        log.info("Returning proto {}", proto);
        return Any.pack(proto);

    }
}
