package services;

import dk.via.fleetforward.gRPC.Fleetforward.DispatcherListProto;
import dk.via.fleetforward.gRPC.Fleetforward.DispatcherProto;
import dk.via.fleetforward.gRPC.Fleetforward;
import dk.via.fleetforward.model.Dispatcher;
import dk.via.fleetforward.model.Enums.UserRole;
import dk.via.fleetforward.model.User;
import dk.via.fleetforward.repositories.database.DispatcherRepository;
import dk.via.fleetforward.repositories.database.UserRepository;
import dk.via.fleetforward.services.user.DispatcherServiceDatabase;
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
 * ZOMB+E-focused tests for DispatcherServiceDatabase
 * Z = Zero, O = One, M = Many, B = Boundaries, E = Exceptions
 */
@ExtendWith(MockitoExtension.class)
class DispatcherServiceDatabaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DispatcherRepository dispatcherRepository;

    @InjectMocks
    private DispatcherServiceDatabase dispatcherService;

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private Fleetforward.UserProto buildUserProto(int id) {
        return Fleetforward.UserProto.newBuilder()
                .setId(id)
                .build();
    }

    private DispatcherProto buildDispatcherProto(int userId) {
        return DispatcherProto.newBuilder()
                .setUser(buildUserProto(userId))
                .setCurrentRate(0.15)           // any valid rate
                // driversAssigned left empty by default
                .build();
    }

    private User buildUserEntity(int id, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        return u;
    }

    private Dispatcher buildDispatcherEntity(int id, double rate) {
        Dispatcher d = new Dispatcher();
        // we know getDispatcherId exists (used in service.getAll())
        d.setDispatcherId(id);
        d.setCommissionRate(rate);
        return d;
    }

    // =====================================================================
    // Z = ZERO, O = ONE, M = MANY for getAll()
    // =====================================================================

    @Test
    @DisplayName("Z (Zero) - getAll(): returns empty DispatcherListProto when no users/dispatchers exist")
    void getAll_zeroDispatchers_returnsEmptyList() {
        when(userRepository.findAllByRole(UserRole.dispatcher)).thenReturn(Collections.emptyList());
        when(dispatcherRepository.findAll()).thenReturn(Collections.emptyList());

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            DispatcherListProto result = dispatcherService.getAll();

            assertNotNull(result);
            assertEquals(0, result.getDispatchersCount());

            verify(userRepository).findAllByRole(UserRole.dispatcher);
            verify(dispatcherRepository).findAll();
            protoUtilsMock.verifyNoInteractions();
            verifyNoMoreInteractions(userRepository, dispatcherRepository);
        }
    }

    @Test
    @DisplayName("O (One) - getAll(): returns one dispatcher when one user and matching dispatcher exist")
    void getAll_oneDispatcher_returnsSingleEntry() {
        int id = 1;
        User user = buildUserEntity(id, UserRole.dispatcher);
        Dispatcher dispatcher = buildDispatcherEntity(id, 0.2);

        when(userRepository.findAllByRole(UserRole.dispatcher))
                .thenReturn(Collections.singletonList(user));
        when(dispatcherRepository.findAll())
                .thenReturn(Collections.singletonList(dispatcher));

        DispatcherProto expectedProto = buildDispatcherProto(id);

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseDispatcherProto(dispatcher, user))
                    .thenReturn(expectedProto);

            DispatcherListProto result = dispatcherService.getAll();

            assertNotNull(result);
            assertEquals(1, result.getDispatchersCount());
            assertEquals(expectedProto, result.getDispatchers(0));

            verify(userRepository).findAllByRole(UserRole.dispatcher);
            verify(dispatcherRepository).findAll();
            protoUtilsMock.verify(
                    () -> ProtoUtils.parseDispatcherProto(dispatcher, user),
                    times(1)
            );
            verifyNoMoreInteractions(userRepository, dispatcherRepository);
        }
    }

    @Test
    @DisplayName("M (Many) - getAll(): returns only dispatchers that have matching users")
    void getAll_manyDispatchers_returnsMatchedEntriesOnly() {
        User user1 = buildUserEntity(1, UserRole.dispatcher);
        User user2 = buildUserEntity(2, UserRole.dispatcher);
        List<User> users = Arrays.asList(user1, user2);

        Dispatcher d1 = buildDispatcherEntity(1, 0.1);
        Dispatcher d2 = buildDispatcherEntity(2, 0.2);
        Dispatcher d3 = buildDispatcherEntity(999, 0.3); // no matching user, ignored
        List<Dispatcher> dispatchers = Arrays.asList(d1, d2, d3);

        when(userRepository.findAllByRole(UserRole.dispatcher)).thenReturn(users);
        when(dispatcherRepository.findAll()).thenReturn(dispatchers);

        DispatcherProto p1 = buildDispatcherProto(1);
        DispatcherProto p2 = buildDispatcherProto(2);

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseDispatcherProto(d1, user1)).thenReturn(p1);
            protoUtilsMock.when(() -> ProtoUtils.parseDispatcherProto(d2, user2)).thenReturn(p2);

            DispatcherListProto result = dispatcherService.getAll();

            assertEquals(2, result.getDispatchersCount());
            assertEquals(p1, result.getDispatchers(0));
            assertEquals(p2, result.getDispatchers(1));

            verify(userRepository).findAllByRole(UserRole.dispatcher);
            verify(dispatcherRepository).findAll();
            protoUtilsMock.verify(
                    () -> ProtoUtils.parseDispatcherProto(d1, user1),
                    times(1)
            );
            protoUtilsMock.verify(
                    () -> ProtoUtils.parseDispatcherProto(d2, user2),
                    times(1)
            );
            verifyNoMoreInteractions(userRepository, dispatcherRepository);
        }
    }

    @Test
    @DisplayName("getAll(): user without matching dispatcher is skipped")
    void getAll_userWithoutDispatcher_skipsEntry() {
        User user = buildUserEntity(1, UserRole.dispatcher);
        List<User> users = Collections.singletonList(user);

        when(userRepository.findAllByRole(UserRole.dispatcher)).thenReturn(users);
        when(dispatcherRepository.findAll()).thenReturn(Collections.emptyList());

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            DispatcherListProto result = dispatcherService.getAll();

            assertEquals(0, result.getDispatchersCount());

            verify(userRepository).findAllByRole(UserRole.dispatcher);
            verify(dispatcherRepository).findAll();
            protoUtilsMock.verifyNoInteractions();
            verifyNoMoreInteractions(userRepository, dispatcherRepository);
        }
    }

    // =====================================================================
    // create()
    // =====================================================================

    @Test
    @DisplayName("create(): happy path - creates user with role=dispatcher, then dispatcher, then returns proto")
    void create_newDispatcher_success() {
        DispatcherProto payload = buildDispatcherProto(0); // id ignored by service

        User createdUser = buildUserEntity(10, UserRole.dispatcher);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        Dispatcher createdDispatcher = buildDispatcherEntity(10, 0.25);
        when(dispatcherRepository.save(any(Dispatcher.class))).thenReturn(createdDispatcher);

        DispatcherProto expectedProto = buildDispatcherProto(10);

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseDispatcherProto(createdDispatcher, createdUser))
                    .thenReturn(expectedProto);

            DispatcherProto result = dispatcherService.create(payload);

            assertEquals(expectedProto, result);

            // Verify user saved with id null and role dispatcher
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User capturedUser = userCaptor.getValue();
            assertNull(capturedUser.getId());
            assertEquals(UserRole.dispatcher, capturedUser.getRole());

            // Verify dispatcher saved with dispatcherId equal to createdUser.id
            ArgumentCaptor<Dispatcher> dispatcherCaptor = ArgumentCaptor.forClass(Dispatcher.class);
            verify(dispatcherRepository).save(dispatcherCaptor.capture());
            Dispatcher capturedDispatcher = dispatcherCaptor.getValue();
            assertEquals(createdUser.getId(), capturedDispatcher.getDispatcherId());

            protoUtilsMock.verify(
                    () -> ProtoUtils.parseDispatcherProto(createdDispatcher, createdUser),
                    times(1)
            );

            verifyNoMoreInteractions(userRepository, dispatcherRepository);
        }
    }

    @Test
    @DisplayName("create(): throws NullPointerException when payload is null")
    void create_nullPayload_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> dispatcherService.create(null));
        verifyNoInteractions(userRepository, dispatcherRepository);
    }

    // =====================================================================
    // update()
    // =====================================================================

    @Test
    @DisplayName("update(): happy path - finds existing user & dispatcher, updates both")
    void update_existingDispatcher_success() {
        int id = 5;
        DispatcherProto payload = buildDispatcherProto(id);

        User existingUser = buildUserEntity(id, UserRole.dispatcher);
        Dispatcher existingDispatcher = buildDispatcherEntity(id, 0.3);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(dispatcherRepository.findById(id)).thenReturn(Optional.of(existingDispatcher));

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dispatcherRepository.save(any(Dispatcher.class))).thenAnswer(inv -> inv.getArgument(0));

        dispatcherService.update(payload);

        verify(userRepository).findById(id);
        verify(dispatcherRepository).findById(id);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(UserRole.dispatcher, savedUser.getRole());
        assertEquals(id, savedUser.getId());

        ArgumentCaptor<Dispatcher> dispatcherCaptor = ArgumentCaptor.forClass(Dispatcher.class);
        verify(dispatcherRepository).save(dispatcherCaptor.capture());
        Dispatcher savedDispatcher = dispatcherCaptor.getValue();
        assertEquals(id, savedDispatcher.getDispatcherId());

        verifyNoMoreInteractions(userRepository, dispatcherRepository);
    }

    @Test
    @DisplayName("update(): throws when user not found")
    void update_userNotFound_throws() {
        int id = 7;
        DispatcherProto payload = buildDispatcherProto(id);

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dispatcherService.update(payload));

        assertEquals("User not found, user must be created first", ex.getMessage());

        verify(userRepository).findById(id);
        verify(dispatcherRepository, never()).findById(anyInt());
        verify(dispatcherRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository, dispatcherRepository);
    }

    @Test
    @DisplayName("update(): throws when dispatcher not found")
    void update_dispatcherNotFound_throws() {
        int id = 7;
        DispatcherProto payload = buildDispatcherProto(id);

        User existingUser = buildUserEntity(id, UserRole.dispatcher);
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(dispatcherRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dispatcherService.update(payload));

        assertEquals("Dispatcher not found, dispatcher must be created first", ex.getMessage());

        verify(userRepository).findById(id);
        verify(dispatcherRepository).findById(id);
        verify(userRepository, never()).save(any());
        verify(dispatcherRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository, dispatcherRepository);
    }

    // =====================================================================
    // delete()
    // =====================================================================

    @Test
    @DisplayName("delete(): calls dispatcherRepository.findById and userRepository.deleteById")
    void delete_callsFindOnDispatcherAndDeletesUser() {
        int id = 3;

        dispatcherService.delete(id);

        // According to implementation, it only calls findById on dispatcher, not deleteById
        verify(dispatcherRepository, times(1)).findById(id);
        verify(userRepository, times(1)).deleteById(id);
        verifyNoMoreInteractions(dispatcherRepository, userRepository);
    }

    // =====================================================================
    // getSingle()
    // =====================================================================

    @Test
    @DisplayName("getSingle(): happy path - returns dispatcher proto for existing user & dispatcher")
    void getSingle_existingDispatcher_success() {
        int id = 11;
        User user = buildUserEntity(id, UserRole.dispatcher);
        Dispatcher dispatcher = buildDispatcherEntity(id, 0.4);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(dispatcherRepository.findById(id)).thenReturn(Optional.of(dispatcher));

        DispatcherProto expected = buildDispatcherProto(id);

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseDispatcherProto(dispatcher, user))
                    .thenReturn(expected);

            DispatcherProto result = dispatcherService.getSingle(id);

            assertEquals(expected, result);
            verify(userRepository).findById(id);
            verify(dispatcherRepository).findById(id);
            protoUtilsMock.verify(
                    () -> ProtoUtils.parseDispatcherProto(dispatcher, user),
                    times(1)
            );
            verifyNoMoreInteractions(userRepository, dispatcherRepository);
        }
    }

    @Test
    @DisplayName("getSingle(): throws when user not found")
    void getSingle_userNotFound_throws() {
        int id = 12;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dispatcherService.getSingle(id));

        assertEquals("User not found, user must be created first", ex.getMessage());

        verify(userRepository).findById(id);
        verify(dispatcherRepository, never()).findById(anyInt());
        verifyNoMoreInteractions(userRepository, dispatcherRepository);
    }

    @Test
    @DisplayName("getSingle(): throws when dispatcher not found")
    void getSingle_dispatcherNotFound_throws() {
        int id = 12;
        User user = buildUserEntity(id, UserRole.dispatcher);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(dispatcherRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dispatcherService.getSingle(id));

        assertEquals("Dispatcher not found, dispatcher must be created first", ex.getMessage());

        verify(userRepository).findById(id);
        verify(dispatcherRepository).findById(id);
        verifyNoMoreInteractions(userRepository, dispatcherRepository);
    }
}
