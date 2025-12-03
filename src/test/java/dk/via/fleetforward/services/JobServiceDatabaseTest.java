package dk.via.fleetforward.services;

import com.google.protobuf.Timestamp;
import dk.via.fleetforward.gRPC.Fleetforward;
import dk.via.fleetforward.gRPC.Fleetforward.JobListProto;
import dk.via.fleetforward.gRPC.Fleetforward.JobProto;
import dk.via.fleetforward.model.Dispatcher;
import dk.via.fleetforward.model.Driver;
import dk.via.fleetforward.model.Job;
import dk.via.fleetforward.model.Enums.JobStatus;
import dk.via.fleetforward.model.Enums.TrailerType;
import dk.via.fleetforward.repositories.database.DispatcherRepository;
import dk.via.fleetforward.repositories.database.DriverRepository;
import dk.via.fleetforward.repositories.database.JobRepository;
import dk.via.fleetforward.services.job.JobServiceDatabase;
import dk.via.fleetforward.utility.ProtoUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ZOMB+E-focused tests for JobServiceDatabase
 * Z = Zero, O = One, M = Many, B = Boundaries, E = Exceptions
 */
@ExtendWith(MockitoExtension.class)
class JobServiceDatabaseTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DispatcherRepository dispatcherRepository;

    @InjectMocks
    private JobServiceDatabase jobService;

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private Timestamp tsOf(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    /**
     * Build a JobProto with valid enums (DRY_VAN, JOB_AVAILABLE) and all required fields.
     */
    private JobProto buildJobProto(int jobId, int dispatcherId, int driverId) {
        Instant now = Instant.now();
        return JobProto.newBuilder()
                .setJobId(jobId)
                .setJobDispatcherId(dispatcherId)
                .setJobDriverId(driverId)
                .setTitle("Title")
                .setDescription("Desc")
                .setCargoInfo("Cargo")
                .setLoadedMiles(100)
                .setWeightOfCargo(2000)
                .setTotalPrice(5000)
                .setJobTrailerType(Fleetforward.TrailerTypeProto.DRY_VAN)
                .setCurrentJobStatus(Fleetforward.JobStatusProto.JOB_AVAILABLE)
                .setPickUpTime(tsOf(now))
                .setDeliveryTime(tsOf(now.plusSeconds(3600)))
                .setPickUpLocationState("AL")
                .setPickUpLocationZipCode(35010)
                .setDropLocationState("AL")
                .setDropLocationZipCode(35011)
                .build();
    }

    private Job buildJobEntity(int id) {
        Job job = new Job();
        job.setId(id);
        job.setTitle("Title");
        job.setDescription("Desc");
        job.setCargoInfo("Cargo");
        job.setLoadedMiles(100);
        job.setWeightOfCargo(2000);
        job.setTotalPrice(5000);
        job.setTrailerTypeNeeded(TrailerType.dry_van);
        job.setCurrentJobStatus(JobStatus.available);
        job.setPickupTime(Instant.now());
        job.setDeliveryTime(Instant.now().plusSeconds(3600));
        job.setPickupLocationState("AL");
        job.setPickupLocationZipCode(35010);
        job.setDropLocationState("AL");
        job.setDropLocationZipCode(35011);
        return job;
    }

    private Driver buildDriverEntity(int id) {
        Driver d = new Driver();
        d.setDriverId(id);
        return d;
    }

    private Dispatcher buildDispatcherEntity(int id) {
        Dispatcher dis = new Dispatcher();
        dis.setDispatcherId(id);
        return dis;
    }

    // =====================================================================
    // Z = ZERO, O = ONE, M = MANY for getAll()
    // =====================================================================

    @Test
    @DisplayName("Z (Zero) - getAll(): returns empty JobListProto when no jobs exist")
    void getAll_zeroJobs_returnsEmptyList() {
        when(jobRepository.findAll()).thenReturn(Collections.emptyList());

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {

            JobListProto result = jobService.getAll();

            assertNotNull(result);
            assertEquals(0, result.getJobsCount());

            verify(jobRepository).findAll();
            protoUtilsMock.verifyNoInteractions();
            verifyNoMoreInteractions(jobRepository);
            verifyNoInteractions(driverRepository, dispatcherRepository);
        }
    }

    @Test
    @DisplayName("O (One) - getAll(): returns one job when one exists")
    void getAll_oneJob_returnsSingleEntry() {
        Job j1 = buildJobEntity(1);
        when(jobRepository.findAll()).thenReturn(Collections.singletonList(j1));

        JobProto p1 = buildJobProto(1, 10, 20);

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseFromJobToProto(j1))
                    .thenReturn(p1);

            JobListProto result = jobService.getAll();

            assertEquals(1, result.getJobsCount());
            assertEquals(p1, result.getJobs(0));

            verify(jobRepository).findAll();
            protoUtilsMock.verify(
                    () -> ProtoUtils.parseFromJobToProto(j1),
                    times(1)
            );
            verifyNoMoreInteractions(jobRepository);
            verifyNoInteractions(driverRepository, dispatcherRepository);
        }
    }

    @Test
    @DisplayName("M (Many) - getAll(): returns all jobs in the list")
    void getAll_manyJobs_returnsAllEntries() {
        Job j1 = buildJobEntity(1);
        Job j2 = buildJobEntity(2);
        Job j3 = buildJobEntity(3);
        List<Job> jobs = Arrays.asList(j1, j2, j3);

        when(jobRepository.findAll()).thenReturn(jobs);

        JobProto p1 = buildJobProto(1, 10, 20);
        JobProto p2 = buildJobProto(2, 11, 21);
        JobProto p3 = buildJobProto(3, 12, 22);

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseFromJobToProto(j1)).thenReturn(p1);
            protoUtilsMock.when(() -> ProtoUtils.parseFromJobToProto(j2)).thenReturn(p2);
            protoUtilsMock.when(() -> ProtoUtils.parseFromJobToProto(j3)).thenReturn(p3);

            JobListProto result = jobService.getAll();

            assertEquals(3, result.getJobsCount());
            assertEquals(p1, result.getJobs(0));
            assertEquals(p2, result.getJobs(1));
            assertEquals(p3, result.getJobs(2));

            verify(jobRepository).findAll();
            protoUtilsMock.verify(() -> ProtoUtils.parseFromJobToProto(j1), times(1));
            protoUtilsMock.verify(() -> ProtoUtils.parseFromJobToProto(j2), times(1));
            protoUtilsMock.verify(() -> ProtoUtils.parseFromJobToProto(j3), times(1));
            verifyNoMoreInteractions(jobRepository);
            verifyNoInteractions(driverRepository, dispatcherRepository);
        }
    }

    // =====================================================================
    // create()
    // =====================================================================

    @Test
    @DisplayName("create(): happy path - creates new job and returns proto")
    void create_newJob_success() {
        JobProto payload = buildJobProto(1, 10, 20);

        when(jobRepository.existsById(1)).thenReturn(false);

        Job saved = buildJobEntity(1);
        when(jobRepository.save(any(Job.class))).thenReturn(saved);

        JobProto expected = buildJobProto(1, 10, 20);

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseFromJobToProto(saved))
                    .thenReturn(expected);

            JobProto result = jobService.create(payload);

            assertEquals(expected, result);

            verify(jobRepository).existsById(1);

            ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(captor.capture());
            Job captured = captor.getValue();

            // Black-box-ish checks that are clearly owned by the service
            assertEquals(1, captured.getId());
            assertEquals("Title", captured.getTitle());
            assertEquals("Desc", captured.getDescription());
            assertEquals("Cargo", captured.getCargoInfo());
            assertEquals(100, captured.getLoadedMiles());
            assertEquals(2000, captured.getWeightOfCargo());
            assertEquals(5000, captured.getTotalPrice());

            protoUtilsMock.verify(
                    () -> ProtoUtils.parseFromJobToProto(saved),
                    times(1)
            );

            // Service calls dispatcherRepository.findById(jobDispatcherId)
            verify(dispatcherRepository).findById(10);

            verifyNoMoreInteractions(jobRepository);
            verifyNoMoreInteractions(dispatcherRepository);
            verifyNoInteractions(driverRepository);
        }
    }

    @Test
    @DisplayName("create(): throws when job with same ID already exists")
    void create_existingJob_throwsRuntimeException() {
        JobProto payload = buildJobProto(1, 10, 20);

        when(jobRepository.existsById(1)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> jobService.create(payload));

        assertEquals("Job already exists", ex.getMessage());

        verify(jobRepository).existsById(1);
        verify(jobRepository, never()).save(any());
        verifyNoMoreInteractions(jobRepository);
        verifyNoInteractions(driverRepository, dispatcherRepository);
    }

    @Test
    @DisplayName("create(): throws NullPointerException when payload is null")
    void create_nullPayload_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> jobService.create(null));
        verifyNoInteractions(jobRepository, driverRepository, dispatcherRepository);
    }

    // =====================================================================
    // update()
    // =====================================================================

    @Test
    @DisplayName("update(): throws when job not found")
    void update_jobNotFound_throwsRuntimeException() {
        JobProto payload = buildJobProto(1, 10, 20);

        when(jobRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> jobService.update(payload));

        assertEquals("Job not found", ex.getMessage());

        verify(jobRepository).findById(1);
        verify(dispatcherRepository, never()).findById(anyInt());
        verify(driverRepository, never()).findById(anyInt());
        verify(jobRepository, never()).save(any());
        verifyNoMoreInteractions(jobRepository);
        verifyNoInteractions(driverRepository, dispatcherRepository);
    }

    @Test
    @DisplayName("update(): throws RuntimeException when update payload cannot be processed (dispatcher case)")
    void update_dispatcherNotFound_throwsRuntimeException() {
        JobProto payload = buildJobProto(1, 10, 20);

        Job existing = buildJobEntity(1);
        when(jobRepository.findById(1)).thenReturn(Optional.of(existing));
        // we no longer stub dispatcherRepository here, because update() doesn't look it up anymore

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> jobService.update(payload));

        // We only know: jobRepository was used, others were not
        verify(jobRepository).findById(1);
        verifyNoMoreInteractions(jobRepository);
        verifyNoInteractions(dispatcherRepository, driverRepository);
    }

    @Test
    @DisplayName("update(): throws RuntimeException when update payload cannot be processed (driver case)")
    void update_driverNotFound_throwsRuntimeException() {
        JobProto payload = buildJobProto(1, 10, 20);

        Job existing = buildJobEntity(1);
        when(jobRepository.findById(1)).thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> jobService.update(payload));

        verify(jobRepository).findById(1);
        verifyNoMoreInteractions(jobRepository);
        verifyNoInteractions(dispatcherRepository, driverRepository);
    }

    // =====================================================================
    // delete()
    // =====================================================================

    @Test
    @DisplayName("delete(): delegates to jobRepository.deleteById")
    void delete_deletesJobById() {
        jobService.delete(5);

        verify(jobRepository, times(1)).deleteById(5);
        verifyNoMoreInteractions(jobRepository);
        verifyNoInteractions(driverRepository, dispatcherRepository);
    }

    // =====================================================================
    // getSingle()
    // =====================================================================

    @Test
    @DisplayName("getSingle(): happy path - returns job proto for existing job")
    void getSingle_existingJob_success() {
        Job entity = buildJobEntity(1);
        when(jobRepository.findById(1)).thenReturn(Optional.of(entity));

        JobProto expected = buildJobProto(1, 10, 20);

        try (MockedStatic<ProtoUtils> protoUtilsMock = Mockito.mockStatic(ProtoUtils.class)) {
            protoUtilsMock.when(() -> ProtoUtils.parseFromJobToProto(entity))
                    .thenReturn(expected);

            JobProto result = jobService.getSingle(1);

            assertEquals(expected, result);

            verify(jobRepository).findById(1);
            protoUtilsMock.verify(
                    () -> ProtoUtils.parseFromJobToProto(entity),
                    times(1)
            );
            verifyNoMoreInteractions(jobRepository);
            verifyNoInteractions(driverRepository, dispatcherRepository);
        }
    }

    @Test
    @DisplayName("getSingle(): throws when job not found")
    void getSingle_jobNotFound_throwsRuntimeException() {
        when(jobRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> jobService.getSingle(1));

        assertEquals("Job not found", ex.getMessage());

        verify(jobRepository).findById(1);
        verifyNoMoreInteractions(jobRepository);
        verifyNoInteractions(driverRepository, dispatcherRepository);
    }

    // =====================================================================
    // toInstant() helper
    // =====================================================================

    @Test
    @DisplayName("toInstant(): returns null when Timestamp is null")
    void toInstant_null_returnsNull() {
        assertNull(JobServiceDatabase.toInstant(null));
    }

    @Test
    @DisplayName("toInstant(): converts Timestamp to Instant correctly")
    void toInstant_validTimestamp_convertsCorrectly() {
        Instant now = Instant.now();
        Timestamp ts = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        Instant result = JobServiceDatabase.toInstant(ts);

        assertEquals(now.getEpochSecond(), result.getEpochSecond());
        assertEquals(now.getNano(), result.getNano());
    }
}
