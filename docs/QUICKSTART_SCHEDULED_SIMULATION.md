# Quick Start: Scheduled Background Simulation

## 5-Minute Setup

### 1. Enable Scheduled Simulation

Add to `application.properties`:
```properties
simulation.scheduled.enabled=true
```

### 2. (Optional) Adjust Timing

Default is 5 seconds between runs. To change:
```properties
simulation.scheduled.fixedDelayMs=10000  # Run every 10 seconds
```

### 3. Start Application

```bash
./run-local.sh
```

### 4. Verify It's Running

Check logs for:
```
INFO  ScheduledSimulationService - Scheduled simulation ENABLED - will run every 5000ms after 10000ms initial delay, minPerTreatment=350
```

After 10 seconds:
```
INFO  ScheduledSimulationService - Starting scheduled simulation with minPerTreatment=350
INFO  ScheduledSimulationService - Scheduled simulation initiated: status=STARTED
```

### 5. Monitor Status

```bash
curl http://localhost:8080/api/simulate/status
```

Expected response:
```json
{
  "running": true,
  "usersProcessed": 150,
  "targetMinPerTreatment": 350,
  "totalImpressions": 600,
  "totalEvents": 450,
  "message": "User 150 — 600 impressions, 450 events..."
}
```

## Done! 🎉

The simulation now runs automatically every 5 seconds in the background without blocking your application.

---

## Common Configurations

### High Frequency (Development)
```properties
simulation.scheduled.enabled=true
simulation.scheduled.fixedDelayMs=3000
simulation.scheduled.minPerTreatment=100
```
**Use case**: Rapid testing, continuous event generation

### Medium Frequency (Staging)
```properties
simulation.scheduled.enabled=true
simulation.scheduled.fixedDelayMs=10000
simulation.scheduled.minPerTreatment=350
```
**Use case**: Realistic staging environment testing

### Low Frequency (Production)
```properties
simulation.scheduled.enabled=true
simulation.scheduled.fixedDelayMs=60000
simulation.scheduled.minPerTreatment=500
```
**Use case**: Periodic production monitoring

### Disabled (Manual Only)
```properties
simulation.scheduled.enabled=false
```
**Use case**: Only trigger simulations manually via API/UI

---

## Environment Variables

Override without editing `application.properties`:

```bash
export SIMULATION_SCHEDULED_ENABLED=true
export SIMULATION_SCHEDULED_FIXED_DELAY_MS=5000
export SIMULATION_SCHEDULED_MIN_PER_TREATMENT=350

java -jar application.jar
```

---

## Spring Profiles

Use the pre-configured scheduled profile:

```bash
java -jar application.jar --spring.profiles.active=scheduled
```

Or via environment variable:
```bash
export SPRING_PROFILES_ACTIVE=scheduled
java -jar application.jar
```

---

## Docker

Add environment variables to your Dockerfile or docker-compose.yml:

```yaml
services:
  fme-app:
    image: fme-workshop:latest
    environment:
      - SIMULATION_SCHEDULED_ENABLED=true
      - SIMULATION_SCHEDULED_FIXED_DELAY_MS=5000
      - SPLIT_SDK_KEY=${SPLIT_SDK_KEY}
```

---

## Kubernetes

Add to your deployment ConfigMap or pod spec:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: fme-config
data:
  SIMULATION_SCHEDULED_ENABLED: "true"
  SIMULATION_SCHEDULED_FIXED_DELAY_MS: "5000"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: fme-workshop
spec:
  template:
    spec:
      containers:
      - name: app
        envFrom:
        - configMapRef:
            name: fme-config
```

---

## Harness CD Pipeline

Update `values.yaml`:

```yaml
env:
  SPLIT_SDK_KEY: <+serviceVariables.sdk_key>
  SIMULATION_SCHEDULED_ENABLED: "true"
  SIMULATION_SCHEDULED_FIXED_DELAY_MS: "5000"
```

---

## Troubleshooting

### ❌ Not running

**Check 1**: Verify property is set
```bash
curl http://localhost:8080/actuator/env | grep simulation.scheduled.enabled
```

**Check 2**: Check logs for startup message
```bash
grep "Scheduled simulation" logs/application.log
```

**Check 3**: Verify `@EnableScheduling` is present
```bash
grep -r "@EnableScheduling" src/main/java
```

### ❌ Memory issues

**Reduce user count**:
```properties
simulation.maxSyntheticUsers=5000
simulation.scheduled.minPerTreatment=200
```

**Increase delay between runs**:
```properties
simulation.scheduled.fixedDelayMs=30000
```

**Monitor memory**:
```bash
jconsole
# Or
jstat -gcutil <pid> 1000
```

### ❌ Too many events

**Lower frequency**:
```properties
simulation.scheduled.fixedDelayMs=60000
```

**Reduce samples per run**:
```properties
simulation.scheduled.minPerTreatment=100
```

### ❌ Overlapping simulations

Not possible - the service checks if one is already running and skips. You'll see:
```
DEBUG ScheduledSimulationService - Scheduled simulation skipped - another simulation is already running
```

If you see this frequently, increase `fixedDelayMs`.

---

## Manual Control Alongside Scheduled

### Trigger manually even with scheduling enabled

```bash
curl -X POST "http://localhost:8080/api/simulate?minPerTreatment=500"
```

If a scheduled simulation is running, you'll get:
```json
{
  "status": "ALREADY_RUNNING",
  "message": "Simulation already in progress"
}
```

### Stop scheduling temporarily

Restart with:
```bash
java -jar application.jar -Dsimulation.scheduled.enabled=false
```

Manual triggers still work.

---

## Monitoring

### Health Check Endpoint

```bash
curl http://localhost:8080/api/simulate/status
```

### Log Patterns

**Successful execution**:
```
INFO  ScheduledSimulationService - Starting scheduled simulation with minPerTreatment=350
INFO  TrafficSimulationService - Simulation started (impressions → delay → events per metric)…
INFO  TrafficSimulationService - Complete: impressions first, then events (3000ms delay)
INFO  ScheduledSimulationService - Scheduled simulation initiated: status=COMPLETE
```

**Skipped (already running)**:
```
DEBUG ScheduledSimulationService - Scheduled simulation skipped - another simulation is already running
```

**Error**:
```
ERROR ScheduledSimulationService - Scheduled simulation failed to start
```

### Metrics to Track

| Metric | Source | Ideal Value |
|--------|--------|-------------|
| Execution frequency | Logs | Every `fixedDelayMs` |
| Completion rate | `/api/simulate/status` | 100% |
| Average duration | Logs (timestamp diff) | < 60s for 350 samples |
| Memory usage | `jstat -gcutil` | Stable, no growth |
| Skip count | `grep "skipped"` | Low (<10% of runs) |

---

## Advanced Configuration

### Different schedules per environment

**Development** (`application-dev.properties`):
```properties
simulation.scheduled.enabled=true
simulation.scheduled.fixedDelayMs=3000
```

**Production** (`application-prod.properties`):
```properties
simulation.scheduled.enabled=true
simulation.scheduled.fixedDelayMs=60000
```

Activate with:
```bash
java -jar app.jar --spring.profiles.active=prod
```

### Conditional enabling via environment

Only enable if `ENABLE_SIMULATION` is set:
```bash
if [ "$ENABLE_SIMULATION" = "true" ]; then
  export SIMULATION_SCHEDULED_ENABLED=true
fi
java -jar application.jar
```

### Custom JMX monitoring

Enable JMX:
```bash
java -Dcom.sun.management.jmxremote \
     -Dcom.sun.management.jmxremote.port=9010 \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Dcom.sun.management.jmxremote.ssl=false \
     -jar application.jar
```

Connect with `jconsole localhost:9010` to monitor:
- Thread count
- Heap usage
- GC activity

---

## Testing

### Unit Tests

```bash
cd application
./mvnw test -Dtest=ScheduledSimulationServiceTest
```

### Integration Test with Scheduling Enabled

```bash
export SIMULATION_SCHEDULED_ENABLED=true
./mvnw test -Dtest=TrafficSimulationApiIT
```

### Manual End-to-End Test

1. Start app with scheduling enabled:
   ```bash
   export SIMULATION_SCHEDULED_ENABLED=true
   ./run-local.sh
   ```

2. Wait 10 seconds (initial delay)

3. Check status every 5 seconds:
   ```bash
   watch -n 5 'curl -s http://localhost:8080/api/simulate/status | jq'
   ```

4. Verify events in Harness FME (Admin → Event types)

---

## Rollback

### Immediate Disable (No Restart)

Not supported - requires restart. But you can:

1. Stop the app
2. Restart with:
   ```bash
   java -jar app.jar -Dsimulation.scheduled.enabled=false
   ```

Manual triggers continue working.

### Permanent Disable

Remove or set to false in `application.properties`:
```properties
simulation.scheduled.enabled=false
```

---

## Next Steps

- 📖 Read [SCHEDULED_SIMULATION.md](SCHEDULED_SIMULATION.md) for detailed documentation
- 📊 Review [ARCHITECTURE_DIAGRAM.md](ARCHITECTURE_DIAGRAM.md) to understand the design
- 🔍 See [REFACTORING_SUMMARY.md](REFACTORING_SUMMARY.md) for implementation details

---

## Support

**Questions?**  
- Check logs: `logs/application.log`
- Monitor status: `GET /api/simulate/status`
- Verify config: `curl localhost:8080/actuator/env | grep simulation`

**Issues?**  
- GitHub Issues: [fme-repo/issues](https://github.com/nikpapag/fme-repo/issues)
- Harness Support: [support.harness.io](https://support.harness.io)
