
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
/*
// https://leetcode.com/discuss/interview-question/5049157/Uber-machine-coding
// Implement simple circuit breaker given a rpc method.
// please consider the generic circuit breaker options like the following:
//
// * timeWindowSec - (10s) time sliding window size.
// * failureRatioThreshhold - (50%) failure ratio threshold in which the circuit open
// * circuitCloseTimeSec - (5s) required time to circuit re-close
// * min requests - 10


// Main class should be named 'Solution' and should not be public.
 */
/**
 * A simple demonstration of a circuit breaker around an RPC call.
 * <p>
 * Requirements:
 * - timeWindowSec: 10s sliding window
 * - failureRatioThreshold: 50%
 * - circuitCloseTimeSec: 5s cool down before attempting to close
 * - minRequests: 10
 * <p>
 * States:
 * - CLOSED: Normal operation. Track requests. If failure ratio over threshold after minRequests => OPEN.
 * - OPEN: Fails fast. After circuitCloseTimeSec has passed, move to HALF_OPEN.
 * - HALF_OPEN: Let one request through. If success => CLOSED, else => OPEN again.
 * <p>
 * Code is kept simple and aligns with SOLID principles:
 * - Single Responsibility: The CircuitBreaker focuses solely on circuit state management.
 * - Open/Closed: The code is easily extensible if we want to change thresholds.
 * - Liskov Substitution: RpcService is an interface, easily swappable.
 * - Interface Segregation: RpcService is minimal, other functionality separate.
 * - Dependency Inversion: The CircuitBreaker depends on an abstract RpcService, not a concrete implementation.
 */
class RPCCallCircuitBreaker {

    public static void main(String[] args) throws InterruptedException {
        RpcService service = new FailingEverySecondCallRpcService();
        CircuitBreakerConfig config = new CircuitBreakerConfig(
                10,   // timeWindowSec
                0.5,  // failureRatioThreshold = 50%
                5,    // circuitCloseTimeSec
                10    // minRequests
        );
        CircuitBreaker breaker = new CircuitBreaker(service, config);

        for (int i = 0; i < 50; i++) {
            try {
                String result = breaker.call();
                System.out.println("Call " + i + ": SUCCESS: " + result);
            } catch (Exception e) {
                System.out.println("Call " + i + ": FAILURE: " + e.getMessage());
            }
            Thread.sleep(500); // simulate some delay between calls
        }
    }
}

/**
 * RPC service that fails every second call to ensure the circuit eventually opens.
 */
class FailingEverySecondCallRpcService implements RpcService {
    private int counter = 0;

    @Override
    public String call() throws Exception {
        counter++;
        // Fail every even call to achieve ~50% failure
        if (counter % 2 == 0) {
            throw new RuntimeException("RPC failure");
        }
        return "OK-" + counter;
    }
}

/**
 * Abstract definition of an RPC service.
 */
interface RpcService {
    String call() throws Exception;
}

/**
 * Configuration for the Circuit Breaker.
 */
class CircuitBreakerConfig {
    private final int timeWindowSec;
    private final double failureRatioThreshold;
    private final int circuitCloseTimeSec;
    private final int minRequests;

    public CircuitBreakerConfig(int timeWindowSec, double failureRatioThreshold, int circuitCloseTimeSec, int minRequests) {
        this.timeWindowSec = timeWindowSec;
        this.failureRatioThreshold = failureRatioThreshold;
        this.circuitCloseTimeSec = circuitCloseTimeSec;
        this.minRequests = minRequests;
    }

    public int getTimeWindowSec() {
        return timeWindowSec;
    }

    public double getFailureRatioThreshold() {
        return failureRatioThreshold;
    }

    public int getCircuitCloseTimeSec() {
        return circuitCloseTimeSec;
    }

    public int getMinRequests() {
        return minRequests;
    }
}

enum CircuitState {
    CLOSED,
    OPEN,
    HALF_OPEN
}

class RequestResult {
    private final long timestampMillis;
    private final boolean success;

    public RequestResult(boolean success) {
        this.success = success;
        this.timestampMillis = System.currentTimeMillis();
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public boolean isSuccess() {
        return success;
    }
}

/**
 * A simple circuit breaker implementation.
 * - CLOSED: normal operation, monitoring failure ratio.
 * - OPEN: short-circuits calls. After cooldown, goes HALF_OPEN.
 * - HALF_OPEN: allows one call. If success => CLOSED, else => OPEN.
 */
class CircuitBreaker {
    private final RpcService service;
    private final CircuitBreakerConfig config;

    private CircuitState currentState = CircuitState.CLOSED;
    private long openStateStartTime = 0; // Timestamp when circuit opened
    private final Deque<RequestResult> recentRequests = new ArrayDeque<>();

    public CircuitBreaker(RpcService service, CircuitBreakerConfig config) {
        this.service = Objects.requireNonNull(service, "service cannot be null");
        this.config = Objects.requireNonNull(config, "config cannot be null");
    }

    public String call() throws Exception {
        purgeOldRequests();

        switch (currentState) {
            case OPEN:
                // Check if we can attempt to go HALF_OPEN
                if (canAttemptHalfOpen()) {
                    transitionToHalfOpen();
                    return attemptCallInHalfOpen();
                } else {
                    throw new RuntimeException("Circuit is OPEN. Please try again later.");
                }

            case HALF_OPEN:
                return attemptCallInHalfOpen();

            case CLOSED:
            default:
                return attemptCallInClosed();
        }
    }

    private String attemptCallInClosed() throws Exception {
        try {
            String result = service.call();
            recordRequest(true);
            checkIfShouldOpen();
            return result;
        } catch (Exception e) {
            recordRequest(false);
            checkIfShouldOpen();
            throw e;
        }
    }

    private String attemptCallInHalfOpen() throws Exception {
        try {
            String result = service.call();
            recordRequest(true);
            // Success in HALF_OPEN => CLOSED
            transitionToClosed();
            return result;
        } catch (Exception e) {
            recordRequest(false);
            // Failure in HALF_OPEN => OPEN again
            openCircuit();
            throw e;
        }
    }

    private void checkIfShouldOpen() {
        // Only in CLOSED do we consider switching to OPEN
        if (currentState == CircuitState.CLOSED && enoughRequests()) {
            double failureRatio = calculateFailureRatio();
            if (failureRatio >= config.getFailureRatioThreshold()) {
                openCircuit();
            }
        }
    }

    private void openCircuit() {
        currentState = CircuitState.OPEN;
        openStateStartTime = System.currentTimeMillis();
        System.out.println("CIRCUIT TRANSITION: CLOSED -> OPEN");
    }

    private boolean canAttemptHalfOpen() {
        long now = System.currentTimeMillis();
        return (now - openStateStartTime) >= config.getCircuitCloseTimeSec() * 1000L;
    }

    private void transitionToHalfOpen() {
        currentState = CircuitState.HALF_OPEN;
        System.out.println("CIRCUIT TRANSITION: OPEN -> HALF_OPEN");
    }

    private void transitionToClosed() {
        currentState = CircuitState.CLOSED;
        System.out.println("CIRCUIT TRANSITION: HALF_OPEN -> CLOSED");
    }

    private void recordRequest(boolean success) {
        recentRequests.addLast(new RequestResult(success));
        purgeOldRequests();
    }

    private void purgeOldRequests() {
        long now = System.currentTimeMillis();
        long cutoff = now - (config.getTimeWindowSec() * 1000L);
        while (!recentRequests.isEmpty() && recentRequests.peekFirst().getTimestampMillis() < cutoff) {
            recentRequests.pollFirst();
        }
    }

    private boolean enoughRequests() {
        return recentRequests.size() >= config.getMinRequests();
    }

    private double calculateFailureRatio() {
        if (recentRequests.isEmpty()) {
            return 0.0;
        }
        int failures = 0;
        for (RequestResult r : recentRequests) {
            if (!r.isSuccess()) failures++;
        }
        return (double) failures / recentRequests.size();
    }
}
