# Server, MainHandler and protobuf

- [Server, MainHandler and protobuf](#server-mainhandler-and-protobuf)
  - [`Request` and `Response`](#request-and-response)
    - [GRPC important notes](#grpc-important-notes)
  - [Server](#server)
    - [gRPC Server explained](#grpc-server-explained)
    - [Dependency injection for handlers](#dependency-injection-for-handlers)
    - [FleetMainHandler](#fleetmainhandler)

For a better overview of the project go back to [README](https://github.com/MarioIliescu/SEP3-Java/blob/master/README.md)  

## `Request` and `Response`

---

` Request ` is send by the client on the C# Server side.
They contain gRPC objects of type `Request` and are met with objects of type `Response`.  
Instead of `string` to clarify which `Handler` will be used or which `Action` is required or the `Status` returned by the `Server`, enums have been used, if another `ActionType`, `HandlerType` or `StatusType` they need to be added to the list. Enums are better for consistency, making sure typos do not sneak into the class and much easier to debug.

```protobuf
syntax = "proto3";

package dk.via.fleetforward.gRPC;

import "google/protobuf/any.proto";

enum HandlerTypeProto {
  HANDLER_UNKNOWN = 0;
  HANDLER_COMPANY = 1;
}

enum ActionTypeProto {
  ACTION_UNKNOWN = 0;
  ACTION_CREATE = 1;
  ACTION_GET = 2;
  ACTION_UPDATE = 3;
  ACTION_DELETE = 4;
  ACTION_LIST = 5;
}
enum StatusTypeProto {
  STATUS_UNKNOWN = 0;
  STATUS_OK = 1;
  STATUS_ERROR = 2;
  STATUS_INVALID_PAYLOAD = 3;
}
message RequestProto {
  HandlerTypeProto handler = 1;
  ActionTypeProto action = 2;
  google.protobuf.Any payload = 3;
}

message ResponseProto {
  StatusTypeProto status = 1;
  google.protobuf.Any payload = 2;
}
```

### GRPC important notes

---

- `Message` is the gRPC version of the type Object in Java
- `Any.pack(Message message)` is a way to pack any `Object` into a gRPC `Message`
- google.protobuf.Any means that the `Object` inside the payload is any type of `Object` so there can be anything. It's used to transfer all the gRPC generated objects between the servers.

## Server

---

The server accepts Requests and sends them to the ` FleetMainHandler ` (the name does not matter it can be any name).

### gRPC Server explained

---

Importing grpc tools `io.grpc.Server;` and `io.grpc.ServerBuilder` to make the `Server` infrastracture with listening threads and everything needs to know to accept requests.  
Open a `PORT` on 6032 but it can be any available port, in this case the connection is `TCP`.  
In the constructor give `FleetMainhandler` as a `Service` to take care of `Requests`.

```java
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.stereotype.Component;

public class FleetServer  {

    private static final int PORT = 6032;
    private final FleetMainHandler mainHandler;
    private Server grpcServer;

    public FleetServer(FleetMainHandler mainHandler) {
        this.mainHandler = mainHandler;
    }
```

Creating the method `start()`  
Put the server into a new `Thread`  
Use the `ServerBuilder` to build the `Server` on the designated `PORT` and injecting the `FleetMainHandler` as a `Service`.  
`Runtime.getRuntime().addShutdownHook` adding a hook to initiate the shutdown of the `Server` when Runtime gets terminated and displays a red message with `System.err.println()`.  
Non `Daemon` to keep the JVM alive until termination.  

```java
public void start() {
        Thread grpcThread = new Thread(() -> {
            try {
                grpcServer = ServerBuilder.forPort(PORT)
                        .addService(mainHandler)
                        .build()
                        .start();

                System.out.println("Fleet gRPC Server started on port " + PORT);

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.err.println("Shutting down gRPC server...");
                    if (grpcServer != null) grpcServer.shutdown();
                }));

                grpcServer.awaitTermination(); // keep server alive
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }, "gRPC-Server-Thread");

        grpcThread.setDaemon(false); // non-daemon keeps JVM alive
        grpcThread.start();
    }
```

### Dependency injection for handlers

This is a Spring `@Configuration`
Used to inject the correct handler for the request using `SpringBean`

```java
@Configuration
public class HandlerConfig {

    @Bean
    public Map<HandlerTypeProto, FleetNetworkHandler> handlerMap(
            List<FleetNetworkHandler> handlers) {
              //Depending on the getType() method inside the interface 
              // it gets the implementation with the same handlerType returned.
        return handlers.stream()
                .collect(Collectors.toMap(FleetNetworkHandler::getType, Function.identity()));
    }
}
```

### FleetMainHandler  

---

Add the service to be able to extend it.  
Need to add it to the `proto` file.

```protobuf
service FleetService {
  rpc sendRequest (RequestProto) returns (ResponseProto);
}
```

Use `@GRpcService` to be able to inject it into the `Server`.  
Due to the automatic injection in configuration, there is no need to change this class for extra futures.  
`extends FleetServiceGrpc.FleetServiceImplBase` important as a `GRpcService`. (can inject it into the `ServerBuilder`)

```java
@Service
@GRpcService
public class FleetMainHandler extends FleetServiceGrpc.FleetServiceImplBase {\
    //Bean Injection
    private final Map<HandlerTypeProto, FleetNetworkHandler> serviceProvider;
    public FleetMainHandler(Map<HandlerTypeProto, FleetNetworkHandler> serviceProvider) {
        this.serviceProvider = serviceProvider;
    }
```

Sending a `Request` and observing `Responses` using GRpc.

```java
    @Override
    public void sendRequest(RequestProto request, StreamObserver<ResponseProto> responseObserver) {
        try {
            // Route request based on HandlerType
            FleetNetworkHandler handler = serviceProvider.get(request.getHandler());
            if (handler == null) {
                throw new IllegalArgumentException("Unknown handler type");
            }
            };
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
```

`ErrorHandling` method to handle errors.

```java
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


```

`GRpcError` method that sends Errors if something goes wrong.

```java
private void sendGrpcError(StreamObserver<ResponseProto> observer, StatusTypeProto status, String errorMessage) {
        Any payload =Any.pack(StringValue.of(errorMessage));// convert error message to protobuf message
        ResponseProto response = ResponseProto.newBuilder().
                setStatus(status).
                setPayload(payload)
                .build();
        observer.onNext(response);
        observer.onCompleted();
    }
```
