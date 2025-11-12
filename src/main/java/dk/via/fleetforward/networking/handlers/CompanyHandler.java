package dk.via.fleetforward.networking.handlers;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import dk.via.fleetforward.services.company.CompanyService;
import dk.via.fleetforward.gRPC.Fleetforward.ActionTypeProto;
import dk.via.fleetforward.gRPC.Fleetforward.CompanyProto;
import dk.via.fleetforward.gRPC.Fleetforward.HandlerTypeProto;
import org.springframework.stereotype.Service;

/**
 * The CompanyHandler class implements the FleetNetworkHandler interface and is responsible
 * for handling actions related to company management. It acts as an adapter between the
 * fleet network commands and the underlying CompanyService to perform operations on companies.
 */
@Service
public class CompanyHandler implements FleetNetworkHandler {

    private final CompanyService companyService;

    public CompanyHandler(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Override
    public HandlerTypeProto getType() {
        return HandlerTypeProto.HANDLER_COMPANY;
    }

    /**
     * Handles various actions related to a company based on the provided action type and payload.
     * This method delegates the operation to the appropriate handler or service method based on
     * the specified {@code ActionType}.
     *
     * @param actionType The type of action to perform. This can be one of the following:
     *                   {@code ACTION_CREATE}, {@code ACTION_GET}, {@code ACTION_UPDATE},
     *                   {@code ACTION_DELETE}, or {@code ACTION_LIST}. Other types will result
     *                   in an {@code IllegalArgumentException}.
     * @param payload    The input data required to perform the specified action. This should be
     *                   an instance of {@code CompanyProto}.
     * @return A {@code Message} object containing the result of the action. If the action is
     *         {@code ACTION_DELETE}, this method may return {@code null}.
     * @throws IllegalArgumentException if the provided action type is invalid or unsupported.
     */
    @Override
    public Message handle(ActionTypeProto actionType, Object payload) {
        Message proto = null;
        Any payloadAny = (Any) payload;
      CompanyProto request = null;
      try
      {
        request = payloadAny.unpack(CompanyProto.class);
      }
      catch (InvalidProtocolBufferException e)
      {
        throw new RuntimeException(e);
      }
      switch (actionType) {
            case ACTION_GET -> {
                proto = companyService.getSingle(request.getMcNumber());
            }
            case ACTION_CREATE -> {
                proto = companyService.create(request);
            }
            case ACTION_UPDATE -> {
                proto = companyService.update(request);
            }
            case ACTION_DELETE -> {
                companyService.delete(request.getMcNumber());
            }
            case ACTION_LIST -> {
                proto = companyService.getAll();
            }
            default -> {
                throw new IllegalArgumentException("Invalid action type: " + actionType);
            }
        }
        //sometimes it will return null, no need to check for that
        //in case of delete
      if (proto == null) {
        proto = CompanyProto.newBuilder().build();
      }
      return Any.pack(proto) ;
    }
}

