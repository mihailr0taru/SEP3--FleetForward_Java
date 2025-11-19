package dk.via.fleetforward.services.company;
import dk.via.fleetforward.model.Company;
import dk.via.fleetforward.repositories.database.CompanyRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import dk.via.fleetforward.gRPC.Fleetforward.CompanyProto;
import dk.via.fleetforward.gRPC.Fleetforward.CompanyProtoList;
import java.util.List;
import java.util.Optional;
/**
 * @author Mario
 * @version 1.0.0
 * Company service implementation for database operations
 * @implNote This class is a Spring component and is instantiated by Spring<br>
 * This class is transactional (makes changes to the database)
 * @see CompanyService
 */
@Service()
public class CompanyServiceDatabase implements CompanyService{
    private static final Logger log = LoggerFactory.getLogger(CompanyServiceDatabase.class);
    private final CompanyRepository companyRepository;

    /**
     * Constructor
     * @param companyRepository The company repository for database operations using Spring Data JPA
     */
    public CompanyServiceDatabase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }
    /**
     * {@inheritDoc}
     *
     * @param payload {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Transactional
    public CompanyProto create(CompanyProto payload) {
        Company company = new Company();
        company.setMcNumber(payload.getMcNumber());
        company.setCompanyName(payload.getCompanyName());
        Company created = companyRepository.save(company);
        log.info("Created company {}", created);
        return CompanyProto.newBuilder()
                .setMcNumber(created.getMcNumber())
                .setCompanyName(created.getCompanyName()).build();
    }
    /**
     * {@inheritDoc}
     *
     * @param payload {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    @Transactional
    public CompanyProto update(CompanyProto payload) {
        Company existing = companyRepository.findById(payload.getMcNumber())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        existing.setCompanyName(payload.getCompanyName());

        Company updated = companyRepository.save(existing);
        log.info("Updated company {}", updated);

        return CompanyProto.newBuilder()
                .setMcNumber(updated.getMcNumber())
                .setCompanyName(updated.getCompanyName())
                .build();
    }
    /**
     * {@inheritDoc}
     *
     * @param mcNumber {@inheritDoc}
     * @return {@inheritDoc}
     * @implNote Optional is in case the company is not found in the database to ensure null safety.
     */
    @Override
    public CompanyProto getSingle(String mcNumber) {
        Optional<Company> fetched = companyRepository.findById(mcNumber); //null safety
        Company company = fetched.orElseThrow(() -> new RuntimeException("Company not found"));
        log.info("Fetched company {}", company);
        return CompanyProto.newBuilder()
                .setMcNumber(company.getMcNumber())
                .setCompanyName(company.getCompanyName())
                .build();
    }
    /**
     * {@inheritDoc}
     *
     * @param mcNumber {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(String mcNumber) {
      companyRepository.deleteById(mcNumber);
        log.info("Deleted company {}", mcNumber);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public CompanyProtoList getAll() {
      List<Company> companies = companyRepository.findAll();
      log.info("Fetched {} companies", companies.size());

      // Builder for the list
      CompanyProtoList.Builder companiesProtoBuilder = CompanyProtoList.newBuilder();

      // Convert each Company entity to CompanyProto
      for (Company company : companies) {
        CompanyProto companyProto = CompanyProto.newBuilder()
            .setMcNumber(company.getMcNumber())
            .setCompanyName(company.getCompanyName())
            .build();
        companiesProtoBuilder.addCompanies(companyProto);
        log.info("Added company {}", companyProto);
      }
      log.info("Created proto company list");
      // Build and return the list
      return companiesProtoBuilder.build();
    }
}
