package dk.via.fleetforward.networking;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import dk.via.fleetforward.gRPC.FleetServiceProtoGrpc;


import dk.via.fleetforward.networking.handlers.FleetNetworkHandler;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.stereotype.Service;
import dk.via.fleetforward.gRPC.Fleetforward.HandlerTypeProto;
import dk.via.fleetforward.gRPC.Fleetforward.RequestProto;
import dk.via.fleetforward.gRPC.Fleetforward.ResponseProto;
import dk.via.fleetforward.gRPC.Fleetforward.StatusTypeProto;

import java.util.Map;

/**
 * @author Mario
 * @version 1.0.0
 * The main handler for the gRPC service
 * @implNote extends the gpc service to provide the implementation for the gRPC service
 */
@Service
@GRpcService
public class FleetMainHandler extends FleetServiceProtoGrpc.FleetServiceProtoImplBase {
    private final Map<HandlerTypeProto, FleetNetworkHandler> serviceProvider;
    public FleetMainHandler(Map<HandlerTypeProto, FleetNetworkHandler> serviceProvider) {
        this.serviceProvider = serviceProvider;
    }
    /**
     * Handle the request from the client
     * @param request the request from the client
     * @param responseObserver the observer to send the response to
     */
    @Override
    public void sendRequest(RequestProto request, StreamObserver<ResponseProto> responseObserver) {
        try {
            FleetNetworkHandler handler = serviceProvider.get(request.getHandler());
            if (handler == null) {
                throw new IllegalArgumentException("Unknown handler type");
            }
            // Message is the protobuf object
            Message result = handler.handle(request.getAction(), request.getPayload());
            // Only pack if not already an Any
            Any payload;
            if (result instanceof Any) {
                payload = (Any) result;
            } else {
                payload = Any.pack(result);
            }

            ResponseProto response = ResponseProto.newBuilder()
                    .setStatus(StatusTypeProto.STATUS_OK)
                    .setPayload(payload)
                    .build();
            sendResponseWithHandleException(responseObserver, response);

        } catch (Exception e) {
            sendGrpcError(responseObserver, StatusTypeProto.STATUS_ERROR, e.getMessage());
        }
    }

    /**
     * Send an error response to the client
     * @param observer the observer to send the response to
     * @param status the status of the error
     * @param errorMessage the error message to send to the client
     */
    private void sendGrpcError(StreamObserver<ResponseProto> observer, StatusTypeProto status, String errorMessage) {
        Any payload =Any.pack(StringValue.of(errorMessage));// convert error message to protobuf message
        ResponseProto response = ResponseProto.newBuilder().
                setStatus(status).
                setPayload(payload)
                .build();
        observer.onNext(response);
        observer.onCompleted();
    }

    /**
     * Send a response to the client with a handle exception
     * @param responseObserver the observer to send the response to
     * @param response the response to send to the client
     */
    private void sendResponseWithHandleException(StreamObserver<ResponseProto> responseObserver, ResponseProto response)
    {
        try {
            responseObserver.onNext(response);
        } catch (ClassCastException e) {
            sendGrpcError(responseObserver, StatusTypeProto.STATUS_INVALID_PAYLOAD, "Invalid request");
            return;
        } catch (Exception e) {
            sendGrpcError(responseObserver, StatusTypeProto.STATUS_ERROR, e.getMessage());
            return;
        }
        try {
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.err.println("Error completing gRPC response: " + e.getMessage());
        }
    }

}

