package dk.via.fleetforward.services;

import dk.via.fleetforward.gRPC.Fleetforward.CompanyProto;
import dk.via.fleetforward.gRPC.Fleetforward.CompanyProtoList;
import dk.via.fleetforward.model.Company;
import dk.via.fleetforward.repositories.database.CompanyRepository;
import dk.via.fleetforward.services.company.CompanyServiceDatabase;
import dk.via.fleetforward.utility.ProtoUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ZOMB+E-focused tests for CompanyServiceDatabase
 * Z = Zero, O = One, M = Many, B = Boundaries, E = Exceptions
 *
 * Requirements:
 *  - Company name: 1–50 alphabetic characters only
 *  - Company number: exactly 10 digits
 */
@ExtendWith(MockitoExtension.class)
class CompanyServiceDatabaseTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyServiceDatabase companyService;

    private static final String MC_1 = "0000000001";
    private static final String MC_2 = "0000000002";
    private static final String MC_3 = "0000000003";
    private static final String MC_NEW = "1234567890";

    // ---------------------------------------------------------------------
    // Helper builders
    // ---------------------------------------------------------------------

    private CompanyProto buildCompanyProto(String mcNumber, String name) {
        return CompanyProto.newBuilder()
                .setMcNumber(mcNumber == null ? "" : mcNumber)
                .setCompanyName(name)
                .build();
    }

    private Company buildCompanyEntity(String mcNumber, String name) {
        Company c = new Company();
        c.setMcNumber(mcNumber);   // domain: must be 10 chars
        c.setCompanyName(name);
        return c;
    }

    // ---------------------------------------------------------------------
    // Z = ZERO
    // getAll() with 0 elements
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Z (Zero) - getAll(): returns empty CompanyProtoList when no companies exist")
    void getAll_zeroCompanies_returnsEmptyList() {
        when(companyRepository.findAll()).thenReturn(Collections.emptyList());

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            CompanyProtoList result = companyService.getAll();

            assertNotNull(result);
            assertEquals(0, result.getCompaniesCount());

            verify(companyRepository, times(1)).findAll();
            protoUtilsMock.verifyNoInteractions();
            verifyNoMoreInteractions(companyRepository);
        }
    }

    // ---------------------------------------------------------------------
    // O = ONE
    // getAll() with 1 element
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("O (One) - getAll(): returns a single CompanyProto when one company exists")
    void getAll_oneCompany_returnsSingleEntry() {
        Company company = buildCompanyEntity(MC_1, "OneCo");
        when(companyRepository.findAll()).thenReturn(Collections.singletonList(company));

        CompanyProto proto = buildCompanyProto(MC_1, "OneCo Proto");

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseCompanyProto(company))
                    .thenReturn(proto);

            CompanyProtoList result = companyService.getAll();

            assertNotNull(result);
            assertEquals(1, result.getCompaniesCount());
            assertEquals(proto, result.getCompanies(0));

            verify(companyRepository, times(1)).findAll();
            protoUtilsMock.verify(
                    () -> ProtoUtils.parseCompanyProto(company),
                    times(1)
            );
            verifyNoMoreInteractions(companyRepository);
        }
    }

    // ---------------------------------------------------------------------
    // M = MANY
    // getAll() with multiple elements
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("M (Many) - getAll(): returns all CompanyProtos when many companies exist")
    void getAll_manyCompanies_returnsAllEntries() {
        Company c1 = buildCompanyEntity(MC_1, "C1");
        Company c2 = buildCompanyEntity(MC_2, "C2");
        Company c3 = buildCompanyEntity(MC_3, "C3");
        List<Company> entities = Arrays.asList(c1, c2, c3);

        when(companyRepository.findAll()).thenReturn(entities);

        CompanyProto p1 = buildCompanyProto(MC_1, "C1 P");
        CompanyProto p2 = buildCompanyProto(MC_2, "C2 P");
        CompanyProto p3 = buildCompanyProto(MC_3, "C3 P");

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseCompanyProto(c1)).thenReturn(p1);
            protoUtilsMock.when(() -> ProtoUtils.parseCompanyProto(c2)).thenReturn(p2);
            protoUtilsMock.when(() -> ProtoUtils.parseCompanyProto(c3)).thenReturn(p3);

            CompanyProtoList result = companyService.getAll();

            assertEquals(3, result.getCompaniesCount());
            assertEquals(p1, result.getCompanies(0));
            assertEquals(p2, result.getCompanies(1));
            assertEquals(p3, result.getCompanies(2));

            verify(companyRepository, times(1)).findAll();
            protoUtilsMock.verify(() -> ProtoUtils.parseCompanyProto(c1), times(1));
            protoUtilsMock.verify(() -> ProtoUtils.parseCompanyProto(c2), times(1));
            protoUtilsMock.verify(() -> ProtoUtils.parseCompanyProto(c3), times(1));
            verifyNoMoreInteractions(companyRepository);
        }
    }

    // ---------------------------------------------------------------------
    // E = EXCEPTIONS (service-level flow)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("E - create(): throws when company with same MC already exists")
    void create_existingCompany_throwsRuntimeException() {
        CompanyProto payload = buildCompanyProto(MC_1, "DupCo");
        when(companyRepository.existsById(MC_1)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> companyService.create(payload));

        assertEquals("Company already exists", ex.getMessage());

        verify(companyRepository, times(1)).existsById(MC_1);
        verify(companyRepository, never()).save(any());
        verifyNoMoreInteractions(companyRepository);
    }

    @Test
    @DisplayName("E - update(): throws when company not found")
    void update_nonExistingCompany_throwsRuntimeException() {
        CompanyProto payload = buildCompanyProto(MC_1, "MissingCo");
        when(companyRepository.findById(MC_1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> companyService.update(payload));

        assertEquals("Company not found", ex.getMessage());
        verify(companyRepository, times(1)).findById(MC_1);
        verify(companyRepository, never()).save(any());
        verifyNoMoreInteractions(companyRepository);
    }

    @Test
    @DisplayName("E - getSingle(): throws when company not found")
    void getSingle_nonExistingCompany_throwsRuntimeException() {
        when(companyRepository.findById(MC_1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> companyService.getSingle(MC_1));

        assertEquals("Company not found", ex.getMessage());
        verify(companyRepository, times(1)).findById(MC_1);
        verifyNoMoreInteractions(companyRepository);
    }

    @Test
    @DisplayName("E - create(): throws NullPointerException when payload is null")
    void create_nullPayload_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> companyService.create(null));
        verifyNoInteractions(companyRepository);
    }

    // ---------------------------------------------------------------------
    // Normal behavior tests for create/update/getSingle/delete
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("create(): happy path - saves new company and returns converted proto")
    void create_newCompany_success() {
        CompanyProto payload = buildCompanyProto(MC_NEW, "NewCo");

        when(companyRepository.existsById(MC_NEW)).thenReturn(false);

        Company saved = buildCompanyEntity(MC_NEW, "NewCo");
        when(companyRepository.save(any(Company.class))).thenReturn(saved);

        CompanyProto expected = buildCompanyProto(MC_NEW, "NewCo Proto");

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseCompanyProto(saved))
                    .thenReturn(expected);

            CompanyProto result = companyService.create(payload);

            assertEquals(expected, result);
            verify(companyRepository).existsById(MC_NEW);

            ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
            verify(companyRepository).save(captor.capture());
            Company captured = captor.getValue();
            assertEquals(MC_NEW, captured.getMcNumber());
            assertEquals("NewCo", captured.getCompanyName());

            protoUtilsMock.verify(
                    () -> ProtoUtils.parseCompanyProto(saved),
                    times(1)
            );
            verifyNoMoreInteractions(companyRepository);
        }
    }

    @Test
    @DisplayName("update(): happy path - finds by id, updates name, saves, returns proto")
    void update_existingCompany_success() {
        Company existing = buildCompanyEntity(MC_1, "OldName");
        CompanyProto payload = buildCompanyProto(MC_1, "NewName");

        when(companyRepository.findById(MC_1)).thenReturn(Optional.of(existing));
        when(companyRepository.save(any(Company.class)))
                .thenAnswer((Answer<Company>) invocation -> invocation.getArgument(0));

        CompanyProto expected = buildCompanyProto(MC_1, "NewName Proto");

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseCompanyProto(existing))
                    .thenReturn(expected);

            CompanyProto result = companyService.update(payload);

            assertEquals(expected, result);
            assertEquals("NewName", existing.getCompanyName());

            verify(companyRepository).findById(MC_1);
            verify(companyRepository).save(existing);
            protoUtilsMock.verify(
                    () -> ProtoUtils.parseCompanyProto(existing),
                    times(1)
            );
            verifyNoMoreInteractions(companyRepository);
        }
    }

    @Test
    @DisplayName("getSingle(): happy path - finds entity and converts with ProtoUtils")
    void getSingle_existingCompany_success() {
        Company entity = buildCompanyEntity(MC_1, "SingleCo");
        when(companyRepository.findById(MC_1)).thenReturn(Optional.of(entity));

        CompanyProto expected = buildCompanyProto(MC_1, "SingleCo Proto");

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseCompanyProto(entity))
                    .thenReturn(expected);

            CompanyProto result = companyService.getSingle(MC_1);

            assertEquals(expected, result);
            verify(companyRepository).findById(MC_1);
            protoUtilsMock.verify(
                    () -> ProtoUtils.parseCompanyProto(entity),
                    times(1)
            );
            verifyNoMoreInteractions(companyRepository);
        }
    }

    @Test
    @DisplayName("delete(): delegates to repository.deleteById")
    void delete_delegatesToRepository() {
        companyService.delete(MC_1);

        verify(companyRepository, times(1)).deleteById(MC_1);
        verifyNoMoreInteractions(companyRepository);
    }

    // =====================================================================
    // B = BOUNDARIES / REQUIREMENTS:
    //  - Company number exactly 10 digits
    //  - Company name 1–50 alphabetic chars
    // =====================================================================

    // ---- Company number exactly 10 digits ----

    @Test
    @DisplayName("REQ - company number exactly 10 digits is accepted in create()")
    void create_validCompanyNumber_exactly10Digits() {
        String mc = "1111111111";
        CompanyProto payload = buildCompanyProto(mc, "ValidName");

        when(companyRepository.existsById(mc)).thenReturn(false);
        Company saved = buildCompanyEntity(mc, "ValidName");
        when(companyRepository.save(any(Company.class))).thenReturn(saved);

        CompanyProto expected = buildCompanyProto(mc, "ValidName Proto");

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseCompanyProto(saved))
                    .thenReturn(expected);

            CompanyProto result = companyService.create(payload);

            assertEquals(expected, result);
            verify(companyRepository).existsById(mc);
            verify(companyRepository).save(any(Company.class));
            protoUtilsMock.verify(
                    () -> ProtoUtils.parseCompanyProto(saved),
                    times(1)
            );
            verifyNoMoreInteractions(companyRepository);
        }
    }

    @Test
    @DisplayName("REQ - company number shorter than 10 digits is rejected (no save)")
    void create_invalidCompanyNumber_tooShort() {
        String mc = "123456789"; // 9 digits
        CompanyProto payload = buildCompanyProto(mc, "ValidName");

        assertThrows(IllegalArgumentException.class,
                () -> companyService.create(payload));

        verify(companyRepository, never()).save(any());
    }

    @Test
    @DisplayName("REQ - company number longer than 10 digits is rejected (no save)")
    void create_invalidCompanyNumber_tooLong() {
        String mc = "12345678901"; // 11 digits
        CompanyProto payload = buildCompanyProto(mc, "ValidName");

        assertThrows(IllegalArgumentException.class,
                () -> companyService.create(payload));

        verify(companyRepository, never()).save(any());
    }

    @Test
    @DisplayName("REQ - company number with non-digit characters causes RuntimeException from ProtoUtils")
    void create_invalidCompanyNumber_nonDigits() {
        String mc = "12345A7890"; // includes letter
        CompanyProto payload = buildCompanyProto(mc, "ValidName");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> companyService.create(payload));

        // asserting message to see where from (ProtoUtils)
        assertEquals("Company is null", ex.getMessage());
    }


    // ---- Company name 1–50 alphabetic characters only ----

    @Test
    @DisplayName("REQ - company name length 1 and alphabetic is accepted")
    void create_validCompanyName_minLength() {
        String name = "A";
        CompanyProto payload = buildCompanyProto(MC_NEW, name);

        when(companyRepository.existsById(MC_NEW)).thenReturn(false);
        Company saved = buildCompanyEntity(MC_NEW, name);
        when(companyRepository.save(any(Company.class))).thenReturn(saved);

        CompanyProto expected = buildCompanyProto(MC_NEW, "A Proto");

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseCompanyProto(saved))
                    .thenReturn(expected);

            CompanyProto result = companyService.create(payload);

            assertEquals(expected, result);
        }
    }

    @Test
    @DisplayName("REQ - company name length 50 and alphabetic is accepted")
    void create_validCompanyName_maxLength() {
        String name = "A".repeat(50);
        CompanyProto payload = buildCompanyProto(MC_NEW, name);

        when(companyRepository.existsById(MC_NEW)).thenReturn(false);
        Company saved = buildCompanyEntity(MC_NEW, name);
        when(companyRepository.save(any(Company.class))).thenReturn(saved);

        CompanyProto expected = buildCompanyProto(MC_NEW, name + "P");

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseCompanyProto(saved))
                    .thenReturn(expected);

            CompanyProto result = companyService.create(payload);

            assertEquals(expected, result);
        }
    }

    @Test
    @DisplayName("REQ - company name empty is rejected (no save)")
    void create_invalidCompanyName_tooShort() {
        String name = "";
        CompanyProto payload = buildCompanyProto(MC_NEW, name);

        assertThrows(IllegalArgumentException.class,
                () -> companyService.create(payload));

        verify(companyRepository, never()).save(any());
    }

    @Test
    @DisplayName("REQ - company name longer than 50 characters is rejected (no save)")
    void create_invalidCompanyName_tooLong() {
        String name = "A".repeat(51);
        CompanyProto payload = buildCompanyProto(MC_NEW, name);

        assertThrows(IllegalArgumentException.class,
                () -> companyService.create(payload));

        verify(companyRepository, never()).save(any());
    }

    @Test
    @DisplayName("REQ - company name containing non-alphabetic characters causes RuntimeException from ProtoUtils")
    void create_invalidCompanyName_nonAlphabetic() {
        String name = "Comp4ny!";
        CompanyProto payload = buildCompanyProto(MC_NEW, name);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> companyService.create(payload));

        assertEquals("Company is null", ex.getMessage());

    }

}
