package dk.via.fleetforward.networking.handlers;


import com.google.protobuf.Message;
import dk.via.fleetforward.gRPC.Fleetforward.HandlerTypeProto;
import dk.via.fleetforward.gRPC.Fleetforward.ActionTypeProto;
import dk.via.fleetforward.services.job.JobService;

public class JobHandler implements FleetNetworkHandler{
    private final JobService jobService;
    JobHandler(JobService jobService) {
        this.jobService = jobService;
    }
    @Override
    public HandlerTypeProto getType() {
        return HandlerTypeProto.HANDLER_JOB;
    }

    @Override
    public Message handle(ActionTypeProto actionType, Object payload) {
        return null;
    }
}
