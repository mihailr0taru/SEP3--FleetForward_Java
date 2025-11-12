package dk.via.fleetforward.config;

import dk.via.fleetforward.gRPC.Fleetforward.HandlerTypeProto;
import dk.via.fleetforward.networking.handlers.FleetNetworkHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class HandlerConfig {

    @Bean
    public Map<HandlerTypeProto, FleetNetworkHandler> handlerMap(
            List<FleetNetworkHandler> handlers) {

        return handlers.stream()
                .collect(Collectors.toMap(FleetNetworkHandler::getType, Function.identity()));
    }
}