package com.harness.workshop;

import com.harness.workshop.model.SimulationResult;
import com.harness.workshop.service.ScheduledSimulationService;
import com.harness.workshop.service.TrafficSimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class ScheduledSimulationServiceTest {

    @Mock
    private TrafficSimulationService mockSimulationService;

    private ScheduledSimulationService scheduledService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenEnabled_shouldRunSimulation() {
        // Given: scheduled simulation is enabled
        scheduledService = new ScheduledSimulationService(
                mockSimulationService,
                true,  // enabled
                350,
                10000,
                5000
        );

        SimulationResult mockResult = new SimulationResult(
                "STARTED", "Simulation started", 0, 0, 0, 350, 0, null, null);
        when(mockSimulationService.isRunning()).thenReturn(false);
        when(mockSimulationService.startSimulation(anyInt())).thenReturn(mockResult);

        // When: scheduled method runs
        scheduledService.runScheduledSimulation();

        // Then: simulation should be triggered
        verify(mockSimulationService, times(1)).startSimulation(350);
    }

    @Test
    void whenDisabled_shouldNotRunSimulation() {
        // Given: scheduled simulation is disabled
        scheduledService = new ScheduledSimulationService(
                mockSimulationService,
                false,  // disabled
                350,
                10000,
                5000
        );

        // When: scheduled method runs
        scheduledService.runScheduledSimulation();

        // Then: simulation should NOT be triggered
        verify(mockSimulationService, never()).startSimulation(anyInt());
    }

    @Test
    void whenSimulationAlreadyRunning_shouldSkip() {
        // Given: a simulation is already running
        scheduledService = new ScheduledSimulationService(
                mockSimulationService,
                true,  // enabled
                350,
                10000,
                5000
        );

        when(mockSimulationService.isRunning()).thenReturn(true);

        // When: scheduled method runs
        scheduledService.runScheduledSimulation();

        // Then: simulation should NOT be triggered again
        verify(mockSimulationService, never()).startSimulation(anyInt());
    }

    @Test
    void whenEnabledAndNotRunning_shouldStartSimulationWithConfiguredMinPerTreatment() {
        // Given: custom minPerTreatment
        int customMinPerTreatment = 500;
        scheduledService = new ScheduledSimulationService(
                mockSimulationService,
                true,
                customMinPerTreatment,
                10000,
                5000
        );

        SimulationResult mockResult = new SimulationResult(
                "STARTED", "Simulation started", 0, 0, 0, customMinPerTreatment, 0, null, null);
        when(mockSimulationService.isRunning()).thenReturn(false);
        when(mockSimulationService.startSimulation(customMinPerTreatment)).thenReturn(mockResult);

        // When: scheduled method runs
        scheduledService.runScheduledSimulation();

        // Then: simulation should use custom minPerTreatment
        verify(mockSimulationService, times(1)).startSimulation(customMinPerTreatment);
    }

    @Test
    void whenExceptionOccurs_shouldHandleGracefully() {
        // Given: simulation throws exception
        scheduledService = new ScheduledSimulationService(
                mockSimulationService,
                true,
                350,
                10000,
                5000
        );

        when(mockSimulationService.isRunning()).thenReturn(false);
        when(mockSimulationService.startSimulation(anyInt()))
                .thenThrow(new RuntimeException("Simulated failure"));

        // When/Then: should not propagate exception (logged instead)
        scheduledService.runScheduledSimulation();

        // Verify the attempt was made
        verify(mockSimulationService, times(1)).startSimulation(350);
    }
}
