package dk.via.fleetforward;
import dk.via.fleetforward.networking.FleetServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.IOException;
@SpringBootApplication(scanBasePackages = "dk.via.fleetforward")

public class StartServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        ApplicationContext context =SpringApplication.run(StartServer.class, args);
        // Manually get FleetServer bean and start gRPC
        FleetServer fleetServer = context.getBean(FleetServer.class);
        fleetServer.start();
        System.out.println("Fleetforward gRPC Server started");
    }

}
