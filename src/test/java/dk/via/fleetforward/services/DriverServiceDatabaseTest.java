package dk.via.fleetforward.services;

import dk.via.fleetforward.gRPC.Fleetforward;
import dk.via.fleetforward.gRPC.Fleetforward.DriverListProto;
import dk.via.fleetforward.gRPC.Fleetforward.DriverProto;
import dk.via.fleetforward.model.Driver;
import dk.via.fleetforward.model.Enums.UserRole;
import dk.via.fleetforward.model.Enums.DriverCompanyRole;  // <-- NEW
import dk.via.fleetforward.model.User;
import dk.via.fleetforward.repositories.database.DriverRepository;
import dk.via.fleetforward.repositories.database.UserRepository;
import dk.via.fleetforward.services.user.DriverServiceDatabase;
import dk.via.fleetforward.utility.ProtoUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ZOMB+E-focused tests for DriverServiceDatabase
 * Z = Zero, O = One, M = Many, B = Boundaries, E = Exceptions
 */
@ExtendWith(MockitoExtension.class)
class DriverServiceDatabaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverServiceDatabase driverService;

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private Fleetforward.UserProto buildUserProto(int id) {
        return Fleetforward.UserProto.newBuilder()
                .setId(id)
                .build();
    }

    // default: AVAILABLE + DRY_VAN + DRIVER
    private DriverProto buildDriverProto(int id) {
        return buildDriverProto(
                id,
                Fleetforward.StatusDriverProto.AVAILABLE
        );
    }

    private DriverProto buildDriverProto(int id, Fleetforward.StatusDriverProto status) {
        return DriverProto.newBuilder()
                .setUser(buildUserProto(id))
                .setDriverStatus(status)
                .setTrailerType(Fleetforward.TrailerTypeProto.DRY_VAN)
                .setCompanyRole(Fleetforward.DriverCompanyRoleProto.DRIVER)
                .build();
    }

    private User buildUserEntity(int id, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        return u;
    }

    private Driver buildDriverEntity(int id) {
        Driver d = new Driver();
        d.setDriverId(id);
        return d;
    }

    // =====================================================================
    // Z = ZERO, O = ONE, M = MANY for getAll()
    // =====================================================================

    @Test
    @DisplayName("Z (Zero) - getAll(): returns empty DriverListProto when no users/drivers exist")
    void getAll_zeroDrivers_returnsEmptyList() {
        when(userRepository.findAllByRole(UserRole.driver)).thenReturn(Collections.emptyList());
        when(driverRepository.findAll()).thenReturn(Collections.emptyList());

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            DriverListProto result = driverService.getAll();

            assertNotNull(result);
            assertEquals(0, result.getDriversCount());

            verify(userRepository, times(1)).findAllByRole(UserRole.driver);
            verify(driverRepository, times(1)).findAll();
            protoUtilsMock.verifyNoInteractions();
            verifyNoMoreInteractions(userRepository, driverRepository);
        }
    }

    @Test
    @DisplayName("O (One) - getAll(): returns one driver when one user and matching driver exist")
    void getAll_oneDriver_returnsSingleEntry() {
        int id = 1;
        User user = buildUserEntity(id, UserRole.driver);
        Driver driver = buildDriverEntity(id);

        when(userRepository.findAllByRole(UserRole.driver))
                .thenReturn(Collections.singletonList(user));
        when(driverRepository.findAll())
                .thenReturn(Collections.singletonList(driver));

        DriverProto expectedProto = buildDriverProto(id);

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseDriverProto(driver, user))
                    .thenReturn(expectedProto);

            DriverListProto result = driverService.getAll();

            assertNotNull(result);
            assertEquals(1, result.getDriversCount());
            assertEquals(expectedProto, result.getDrivers(0));

            verify(userRepository).findAllByRole(UserRole.driver);
            verify(driverRepository).findAll();
            protoUtilsMock.verify(
                    () -> ProtoUtils.parseDriverProto(driver, user),
                    times(1)
            );
            verifyNoMoreInteractions(userRepository, driverRepository);
        }
    }

    @Test
    @DisplayName("M (Many) - getAll(): returns only drivers that have matching users")
    void getAll_manyDrivers_returnsMatchedEntriesOnly() {
        User user1 = buildUserEntity(1, UserRole.driver);
        User user2 = buildUserEntity(2, UserRole.driver);
        List<User> users = Arrays.asList(user1, user2);

        Driver d1 = buildDriverEntity(1);
        Driver d2 = buildDriverEntity(2);
        Driver d3 = buildDriverEntity(999); // no matching user, ignored
        List<Driver> drivers = Arrays.asList(d1, d2, d3);

        when(userRepository.findAllByRole(UserRole.driver)).thenReturn(users);
        when(driverRepository.findAll()).thenReturn(drivers);

        DriverProto p1 = buildDriverProto(1);
        DriverProto p2 = buildDriverProto(2);

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseDriverProto(d1, user1)).thenReturn(p1);
            protoUtilsMock.when(() -> ProtoUtils.parseDriverProto(d2, user2)).thenReturn(p2);

            DriverListProto result = driverService.getAll();

            assertEquals(2, result.getDriversCount());
            assertEquals(p1, result.getDrivers(0));
            assertEquals(p2, result.getDrivers(1));

            verify(userRepository).findAllByRole(UserRole.driver);
            verify(driverRepository).findAll();
            protoUtilsMock.verify(
                    () -> ProtoUtils.parseDriverProto(d1, user1),
                    times(1)
            );
            protoUtilsMock.verify(
                    () -> ProtoUtils.parseDriverProto(d2, user2),
                    times(1)
            );
            verifyNoMoreInteractions(userRepository, driverRepository);
        }
    }

    @Test
    @DisplayName("getAll(): user without matching driver is skipped")
    void getAll_userWithoutDriver_skipsEntry() {
        User user = buildUserEntity(1, UserRole.driver);
        List<User> users = Collections.singletonList(user);

        when(userRepository.findAllByRole(UserRole.driver)).thenReturn(users);
        when(driverRepository.findAll()).thenReturn(Collections.emptyList());

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            DriverListProto result = driverService.getAll();

            assertEquals(0, result.getDriversCount());

            verify(userRepository).findAllByRole(UserRole.driver);
            verify(driverRepository).findAll();
            protoUtilsMock.verifyNoInteractions();
            verifyNoMoreInteractions(userRepository, driverRepository);
        }
    }

    // =====================================================================
    // create()
    // =====================================================================

    @Test
    @DisplayName("create(): happy path - creates user with role=driver, then driver, then returns proto")
    void create_newDriver_success() {
        DriverProto payload = buildDriverProto(0); // uses AVAILABLE status

        User createdUser = buildUserEntity(10, UserRole.driver);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        Driver createdDriver = buildDriverEntity(10);
        when(driverRepository.save(any(Driver.class))).thenReturn(createdDriver);

        DriverProto expectedProto = buildDriverProto(10);

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseDriverProto(createdDriver, createdUser))
                    .thenReturn(expectedProto);

            DriverProto result = driverService.create(payload);

            assertEquals(expectedProto, result);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User capturedUser = userCaptor.getValue();
            assertNull(capturedUser.getId());
            assertEquals(UserRole.driver, capturedUser.getRole());

            ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
            verify(driverRepository).save(driverCaptor.capture());
            Driver capturedDriver = driverCaptor.getValue();
            assertEquals(createdUser.getId(), capturedDriver.getDriverId());

            protoUtilsMock.verify(
                    () -> ProtoUtils.parseDriverProto(createdDriver, createdUser),
                    times(1)
            );

            verifyNoMoreInteractions(userRepository, driverRepository);
        }
    }

    // =====================================================================
    // STATUS TESTS (AVAILABLE, BUSY, OFF_DUTY, UNKNOWN, UNRECOGNIZED)
    // =====================================================================

    @Test
    @DisplayName("create(): supports status AVAILABLE")
    void create_statusAvailable_success() {
        DriverProto payload = buildDriverProto(0, Fleetforward.StatusDriverProto.AVAILABLE);

        User createdUser = buildUserEntity(20, UserRole.driver);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        Driver createdDriver = buildDriverEntity(20);
        when(driverRepository.save(any(Driver.class))).thenReturn(createdDriver);

        DriverProto expected = buildDriverProto(20, Fleetforward.StatusDriverProto.AVAILABLE);

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseDriverProto(createdDriver, createdUser))
                    .thenReturn(expected);

            DriverProto result = driverService.create(payload);

            assertEquals(expected, result);
        }
    }

    @Test
    @DisplayName("create(): supports status BUSY")
    void create_statusBusy_success() {
        DriverProto payload = buildDriverProto(0, Fleetforward.StatusDriverProto.BUSY);

        User createdUser = buildUserEntity(21, UserRole.driver);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        Driver createdDriver = buildDriverEntity(21);
        when(driverRepository.save(any(Driver.class))).thenReturn(createdDriver);

        DriverProto expected = buildDriverProto(21, Fleetforward.StatusDriverProto.BUSY);

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseDriverProto(createdDriver, createdUser))
                    .thenReturn(expected);

            DriverProto result = driverService.create(payload);

            assertEquals(expected, result);
        }
    }

    @Test
    @DisplayName("create(): supports status OFF_DUTY")
    void create_statusOffDuty_success() {
        DriverProto payload = buildDriverProto(0, Fleetforward.StatusDriverProto.OFF_DUTY);

        User createdUser = buildUserEntity(22, UserRole.driver);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        Driver createdDriver = buildDriverEntity(22);
        when(driverRepository.save(any(Driver.class))).thenReturn(createdDriver);

        DriverProto expected = buildDriverProto(22, Fleetforward.StatusDriverProto.OFF_DUTY);

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseDriverProto(createdDriver, createdUser))
                    .thenReturn(expected);

            DriverProto result = driverService.create(payload);

            assertEquals(expected, result);
        }
    }

    @Test
    @DisplayName("create(): UNKNOWN driverStatus causes RuntimeException from ProtoUtils/Driver")
    void create_statusUnknown_throwsRuntimeException() {
        DriverProto payload = buildDriverProto(0, Fleetforward.StatusDriverProto.UNKNOWN);

        User createdUser = buildUserEntity(30, UserRole.driver);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverService.create(payload));

        // optional: if ProtoUtils throws "Unknown status"
        assertTrue(ex.getMessage() == null || ex.getMessage().contains("Unknown status"));

        // Driver construction fails before driverRepository.save is called
        verify(driverRepository, never()).save(any());
    }

    @Test
    @DisplayName("create(): UNRECOGNIZED driverStatus (value -1) also causes RuntimeException")
    void create_statusUnrecognized_throwsRuntimeException() {
        DriverProto payload = DriverProto.newBuilder()
                .setUser(buildUserProto(0))
                .setDriverStatusValue(-1) // UNRECOGNIZED
                .build();

        User createdUser = buildUserEntity(31, UserRole.driver);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverService.create(payload));

        assertTrue(ex.getMessage() == null || ex.getMessage().contains("Unknown status"));

        verify(driverRepository, never()).save(any());
    }

    // =====================================================================
    // update()
    // =====================================================================

    @Test
    @DisplayName("update(): happy path - finds existing user & driver, updates both")
    void update_existingDriver_success() {
        int id = 5;
        DriverProto payload = buildDriverProto(id); // AVAILABLE

        User existingUser = buildUserEntity(id, UserRole.driver);
        Driver existingDriver = buildDriverEntity(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(driverRepository.findById(id)).thenReturn(Optional.of(existingDriver));

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

        driverService.update(payload);

        verify(userRepository).findById(id);
        verify(driverRepository).findById(id);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(UserRole.driver, savedUser.getRole());
        assertEquals(id, savedUser.getId());

        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(driverCaptor.capture());
        Driver savedDriver = driverCaptor.getValue();
        assertEquals(id, savedDriver.getDriverId());

        verifyNoMoreInteractions(userRepository, driverRepository);
    }

    @Test
    @DisplayName("update(): throws when user not found")
    void update_userNotFound_throws() {
        int id = 7;
        DriverProto payload = buildDriverProto(id);

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverService.update(payload));

        assertEquals("User not found, user must be created first", ex.getMessage());

        verify(userRepository).findById(id);
        verify(driverRepository, never()).findById(anyInt());
        verify(driverRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository, driverRepository);
    }

    @Test
    @DisplayName("update(): throws when driver not found")
    void update_driverNotFound_throws() {
        int id = 7;
        DriverProto payload = buildDriverProto(id);

        User existingUser = buildUserEntity(id, UserRole.driver);
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(driverRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverService.update(payload));

        assertEquals("Driver not found, driver must be created first", ex.getMessage());

        verify(userRepository).findById(id);
        verify(driverRepository).findById(id);
        verify(userRepository, never()).save(any());
        verify(driverRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository, driverRepository);
    }

    // =====================================================================
    // delete()
    // =====================================================================

    @Test
    @DisplayName("delete(): deletes driver and then user with same id")
    void delete_deletesDriverThenUser() {
        int id = 3;

        driverService.delete(id);

        InOrder inOrder = inOrder(driverRepository, userRepository);
        inOrder.verify(driverRepository).deleteById(id);
        inOrder.verify(userRepository).deleteById(id);

        verifyNoMoreInteractions(driverRepository, userRepository);
    }

    // =====================================================================
    // getSingle()
    // =====================================================================

    @Test
    @DisplayName("getSingle(): happy path - returns driver proto for existing user & driver")
    void getSingle_existingDriver_success() {
        int id = 11;
        User user = buildUserEntity(id, UserRole.driver);
        Driver driver = buildDriverEntity(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(driverRepository.findById(id)).thenReturn(Optional.of(driver));

        DriverProto expected = buildDriverProto(id);

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseDriverProto(driver, user))
                    .thenReturn(expected);

            DriverProto result = driverService.getSingle(id);

            assertEquals(expected, result);

            verify(userRepository).findById(id);
            verify(driverRepository).findById(id);
            protoUtilsMock.verify(
                    () -> ProtoUtils.parseDriverProto(driver, user),
                    times(1)
            );
            verifyNoMoreInteractions(userRepository, driverRepository);
        }
    }

    @Test
    @DisplayName("getSingle(): throws when user not found")
    void getSingle_userNotFound_throws() {
        int id = 12;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverService.getSingle(id));

        assertEquals("User not found, user must be created first", ex.getMessage());

        verify(userRepository).findById(id);
        verify(driverRepository, never()).findById(anyInt());
        verifyNoMoreInteractions(userRepository, driverRepository);
    }

    @Test
    @DisplayName("getSingle(): throws when driver not found")
    void getSingle_driverNotFound_throws() {
        int id = 12;
        User user = buildUserEntity(id, UserRole.driver);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(driverRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverService.getSingle(id));

        assertEquals("Driver not found, driver must be created first", ex.getMessage());

        verify(userRepository).findById(id);
        verify(driverRepository).findById(id);
        verifyNoMoreInteractions(userRepository, driverRepository);
    }

    // =====================================================================
    // ROLE TESTS (DRIVER, OWNER_OPERATOR, UNKNOWN, UNRECOGNIZED)
    // =====================================================================

    @Test
    @DisplayName("create(): supports company role DRIVER (maps to DriverCompanyRole.driver)")
    void create_roleDriver_success() {
        DriverProto payload = DriverProto.newBuilder()
                .setUser(buildUserProto(0))
                .setDriverStatus(Fleetforward.StatusDriverProto.AVAILABLE)
                .setTrailerType(Fleetforward.TrailerTypeProto.DRY_VAN)
                .setCompanyRole(Fleetforward.DriverCompanyRoleProto.DRIVER)
                .build();

        User createdUser = buildUserEntity(40, UserRole.driver);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        Driver createdDriver = buildDriverEntity(40);
        when(driverRepository.save(any(Driver.class))).thenReturn(createdDriver);

        driverService.create(payload);

        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(driverCaptor.capture());
        Driver savedDriver = driverCaptor.getValue();

        assertEquals(DriverCompanyRole.driver, savedDriver.getDriverCompanyRole());
    }

    @Test
    @DisplayName("create(): supports company role OWNER_OPERATOR (maps to DriverCompanyRole.owner_operator)")
    void create_roleOwnerOperator_success() {
        DriverProto payload = DriverProto.newBuilder()
                .setUser(buildUserProto(0))
                .setDriverStatus(Fleetforward.StatusDriverProto.AVAILABLE)
                .setTrailerType(Fleetforward.TrailerTypeProto.DRY_VAN)
                .setCompanyRole(Fleetforward.DriverCompanyRoleProto.OWNER_OPERATOR)
                .build();

        User createdUser = buildUserEntity(41, UserRole.driver);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        // Let the constructor run so ProtoUtils is actually called
        when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

        driverService.create(payload);

        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(driverCaptor.capture());
        Driver savedDriver = driverCaptor.getValue();

        assertEquals(DriverCompanyRole.owner_operator, savedDriver.getDriverCompanyRole());
    }

    @Test
    @DisplayName("create(): UNKNOWN companyRole causes RuntimeException from ProtoUtils/Driver")
    void create_roleUnknown_throwsRuntimeException() {
        DriverProto payload = DriverProto.newBuilder()
                .setUser(buildUserProto(0))
                .setDriverStatus(Fleetforward.StatusDriverProto.AVAILABLE)
                .setTrailerType(Fleetforward.TrailerTypeProto.DRY_VAN)
                .setCompanyRole(Fleetforward.DriverCompanyRoleProto.UNKNOWN_ROLE_COMPANY)
                .build();

        User createdUser = buildUserEntity(42, UserRole.driver);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverService.create(payload));

        assertTrue(ex.getMessage() == null || ex.getMessage().contains("Unknown role"));

        verify(driverRepository, never()).save(any());
    }

    @Test
    @DisplayName("create(): UNRECOGNIZED companyRole (value -1) also causes RuntimeException")
    void create_roleUnrecognized_throwsRuntimeException() {
        DriverProto payload = DriverProto.newBuilder()
                .setUser(buildUserProto(0))
                .setDriverStatus(Fleetforward.StatusDriverProto.AVAILABLE)
                .setTrailerType(Fleetforward.TrailerTypeProto.DRY_VAN)
                .setCompanyRoleValue(-1) // UNRECOGNIZED
                .build();

        User createdUser = buildUserEntity(43, UserRole.driver);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverService.create(payload));

        assertTrue(ex.getMessage() == null || ex.getMessage().contains("Unknown role"));

        verify(driverRepository, never()).save(any());
    }
}
