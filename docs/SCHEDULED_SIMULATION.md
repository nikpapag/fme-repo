# Scheduled Background Simulation

## Overview

The FME Event Service now supports automatic background simulation that runs periodically as a scheduled thread. This allows continuous generation of events without blocking the main application logic.

## Features

- **Non-blocking execution**: Runs as a background thread using Spring's `@Scheduled` annotation
- **Memory optimized**: Only one simulation runs at a time; GC hints every 100 users
- **Configurable schedule**: Control initial delay and repeat interval via properties
- **Auto-start on JVM startup**: Begins after application initialization completes
- **Safe concurrency**: Respects the existing simulation lock to prevent overlapping runs

## Configuration

Add these properties to `application.properties`:

```properties
# Enable scheduled background simulation
simulation.scheduled.enabled=true

# Minimum events per treatment (default: 350)
simulation.scheduled.minPerTreatment=350

# Wait 10 seconds after JVM startup before first run (default: 10000ms)
simulation.scheduled.initialDelayMs=10000

# Run every 5 seconds after previous execution completes (default: 5000ms)
simulation.scheduled.fixedDelayMs=5000
```

### Environment Variables

You can override via environment variables:

```bash
export SIMULATION_SCHEDULED_ENABLED=true
export SIMULATION_SCHEDULED_FIXED_DELAY_MS=5000
```

Or pass as JVM arguments:

```bash
java -jar application.jar \
  -Dsimulation.scheduled.enabled=true \
  -Dsimulation.scheduled.fixedDelayMs=5000
```

## How It Works

1. **Application Startup**: Spring Boot application starts with `@EnableScheduling`
2. **Initial Delay**: Waits `initialDelayMs` (default 10 seconds) for the app to stabilize
3. **First Execution**: Runs the first simulation with configured `minPerTreatment`
4. **Fixed Delay Schedule**: After each completion, waits `fixedDelayMs` before next run
5. **Concurrency Control**: Skips execution if another simulation is already running

### Timing Diagram

```
JVM Start → [10s delay] → Simulation 1 → [5s delay] → Simulation 2 → [5s delay] → ...
```

## Memory Management

The service implements several memory optimization strategies:

1. **Single simulation lock**: Prevents multiple concurrent simulations
2. **GC hints**: Calls `System.gc()` every 100 users to suggest garbage collection
3. **Minimal object retention**: Only keeps `previousUser` reference for impersonation events
4. **Non-blocking async start**: Uses `CompletableFuture` for background execution

## API Compatibility

The existing manual trigger API remains fully functional:

```bash
# Manual trigger (still works)
POST /api/simulate?minPerTreatment=350&blocking=false

# Check status
GET /api/simulate/status
```

## Monitoring

Check logs for scheduled execution:

```
2026-07-07 10:00:00 INFO  ScheduledSimulationService - Scheduled simulation ENABLED - will run every 5000ms after 10000ms initial delay, minPerTreatment=350
2026-07-07 10:00:10 INFO  ScheduledSimulationService - Starting scheduled simulation with minPerTreatment=350
2026-07-07 10:00:15 INFO  ScheduledSimulationService - Scheduled simulation initiated: status=STARTED
```

If another simulation is running:

```
2026-07-07 10:00:20 DEBUG ScheduledSimulationService - Scheduled simulation skipped - another simulation is already running
```

## Disabling

To disable scheduled simulation while keeping manual triggers:

```properties
simulation.scheduled.enabled=false
```

Or don't set the property at all (disabled by default).

## Performance Tuning

### High-Frequency Scenarios

For continuous event generation:

```properties
simulation.scheduled.enabled=true
simulation.scheduled.fixedDelayMs=3000
simulation.scheduled.minPerTreatment=100
```

### Low-Frequency Scenarios

For periodic batch generation:

```properties
simulation.scheduled.enabled=true
simulation.scheduled.fixedDelayMs=60000
simulation.scheduled.minPerTreatment=1000
```

### Memory-Constrained Environments

Reduce synthetic user count to minimize memory:

```properties
simulation.maxSyntheticUsers=5000
simulation.scheduled.minPerTreatment=200
```

## Troubleshooting

### Simulation not running

1. Check `simulation.scheduled.enabled=true` in properties
2. Verify logs show "Scheduled simulation ENABLED" on startup
3. Ensure `@EnableScheduling` is present on `WorkshopApplication`

### High memory usage

1. Lower `simulation.maxSyntheticUsers`
2. Increase `fixedDelayMs` to allow more GC time between runs
3. Monitor with `jconsole` or similar JVM tools

### Simulations overlapping

Not possible - the service checks `simulationService.isRunning()` and skips if busy.

## Architecture

```
ScheduledSimulationService (new)
  ↓ @Scheduled(fixedDelay)
  ↓ calls
TrafficSimulationService (existing)
  ↓ uses
FmeEventService (existing)
  ↓ tracks to
SplitClient SDK
```

The new `ScheduledSimulationService` wraps the existing `TrafficSimulationService` without modifying its logic, ensuring backward compatibility.
