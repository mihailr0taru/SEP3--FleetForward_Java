# Handlers and Services

- [Handlers and Services](#handlers-and-services)
  - [Handlers](#handlers)
    - [Interface](#interface)
      - [Implementation example](#implementation-example)
  - [Services](#services)
    - [Service Interfaces](#service-interfaces)
    - [Implementation Service Example](#implementation-service-example)
      - [Creating](#creating)
      - [Update](#update)
      - [Delete](#delete)
      - [GetAll](#getall)

For a better overview of the project go back to [README](https://github.com/MarioIliescu/SEP3-Java/blob/master/README.md)

## Handlers

### Interface

```java
public interface FleetNetworkHandler {
    HandlerTypeProto getType();
    Message handle(ActionTypeProto actionType, Object payload);
}
```

#### Implementation example

Observe the `@Service` to be possible to inject it with Spring `ApplicationContext`, dependency injection.
Observe `getType()` to get the handler for the dependency injection with `@Bean`.

```java
@Service
public class CompanyHandler implements FleetNetworkHandler {

    private CompanyService companyService;

    public CompanyHandler(CompanyService companyService) {
        this.companyService = companyService;
    }
    @Override
    public HandlerTypeProto getType() {
        return HandlerTypeProto.HANDLER_COMPANY;
    }
```

Sending the `Request` after being processed to the `Service` implementation to handle

```java
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
```

## Services

---

### Service Interfaces

---

Use the protobuf Objects.
`@Service` is important to be able to inject it.  
Make an `interface` for every type of `Object` inside the `Database` with the proper methods needed, use CRUD.

```java

@Service
public interface CompanyService {
    //CRUD operations

    CompanyProto create(CompanyProto payload);

    CompanyProto update(CompanyProto payload);

    CompanyProto getSingle(String mcNumber);

    void delete(String mcNumber);

    CompanyProtoList getAll();
}
```

### Implementation Service Example

---

Mark the `Service` as the database implementation, the previous interface can be used for file storage in memory etc... but a database is used in this case: `PostGreSQL`.  
All methods that make changes to the database must be `@Transactional`.

```java
@Service
public class CompanyServiceDatabase implements CompanyService{

    private final CompanyRepository companyRepository;

    /**
     * Constructor
     * @param companyRepository The company repository for database operations using Spring Data JPA
     */
    public CompanyServiceDatabase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }
```

#### Creating

```java
@Transactional
    public CompanyProto create(CompanyProto payload) {
        //Make an Entity from our model
        Company company = new Company(); 
        //Set all the paramethers required, the setters need to have the logic
        company.setMcNumber(payload.getMcNumber());
        company.setCompanyName(payload.getCompanyName());
        //After the entity is created succesfully, save it and get the new created entity with the Id
        Company created = companyRepository.save(company);
        //return the proto 
        return CompanyProto.newBuilder()
                .setMcNumber(created.getMcNumber())
                .setCompanyName(created.getCompanyName()).build();
}
```

#### Update

```java
@Override
    @Transactional
    public CompanyProto update(CompanyProto payload) {
        //First verify the object exists
        Company existing = companyRepository.findById(payload.getId())
                .orElseThrow(() -> new RuntimeException("Company not found"));
        //Set the new values, verified in the entity setters
        existing.setMcNumber(payload.getMcNumber());
        existing.setCompanyName(payload.getCompanyName());
        //save the updated object
        Company updated = companyRepository.save(existing);
        //return the new object
        return CompanyProto.newBuilder()
                .setMcNumber(updated.getMcNumber())
                .setCompanyName(updated.getCompanyName())
                .build();
}
```

#### Delete

```java
public void delete(string mcNumber)
    //Use the magic spell and works. JPA
       companyRepository.deleteById(mcNumber);
    }
```

#### GetAll

```java
@Override
    public CompanyProtoList getAll() {
      List<Company> companies = companyRepository.findAll();

      // Builder for the list
      CompanyProtoList.Builder companiesProtoBuilder = CompanyProtoList.newBuilder();

      // Convert each Company entity to CompanyProto
      for (Company company : companies) {
        .CompanyProto companyProto = CompanyProto.newBuilder()
            .setMcNumber(company.getMcNumber())
            .setCompanyName(company.getCompanyName())
            .build();
        companiesProtoBuilder.addCompanies(companyProto);
      }
      // Build and return the list
     turn companiesProtoBuilder.build();
```#### Get Single

```java
@Override
    public CompanyProto getSingle(string mcNumber) {
        //Wrap in optional in case it doesn't exist
        Optional<Company> fetched = companyRepository.findById(mcNumber); //null safety
        Company company = fetched.orElseThrow(() -> new RuntimeException("Company not found"));
        //return the proto
        return CompanyProto.newBuilder()
                .setMcNumber(company.getMcNumber())
                .setCompanyName(company.getCompanyName())
                .build();
    }
```
