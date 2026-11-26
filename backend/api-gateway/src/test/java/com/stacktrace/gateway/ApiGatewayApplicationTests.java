package com.stacktrace.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/** Exercises the gateway over real HTTP, the way the frontend BFF talks to it. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "jwt.secret=test-secret-test-secret-test-secret-0123456789",
        "CORS_ALLOWED_ORIGINS=https://app.example.com",
        "AUTH_RATE_LIMIT_PER_SECOND=1",
        "AUTH_RATE_LIMIT_BURST=3"
})
@Testcontainers
class ApiGatewayApplicationTests {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    /** Stub downstream service: answers every request instantly with 200. */
    static final HttpServer downstream;

    static {
        try {
            downstream = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            downstream.createContext("/", ex -> {
                ex.sendResponseHeaders(200, -1);
                ex.close();
            });
            downstream.start();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.data.redis.url", () -> "redis://" + redis.getHost() + ":" + redis.getMappedPort(6379));
        r.add("USER_SERVICE_URL", () -> "http://127.0.0.1:" + downstream.getAddress().getPort());
    }

    @LocalServerPort
    int port;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private HttpResponse<String> send(HttpRequest.Builder b) throws Exception {
        return http.send(b.timeout(Duration.ofSeconds(10)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder req(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
    }

    @Test
    void internalEndpointsAreNeverRouted() throws Exception {
        assertThat(send(req("/internal/users/batch?ids=1").GET()).statusCode()).isEqualTo(404);
    }

    @Test
    void corsAllowsOnlyTheFrontendOrigin() throws Exception {
        HttpResponse<String> ok = send(req("/api/posts").method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", "https://app.example.com")
                .header("Access-Control-Request-Method", "GET"));
        assertThat(ok.statusCode()).isEqualTo(200);
        assertThat(ok.headers().firstValue("Access-Control-Allow-Origin")).contains("https://app.example.com");

        HttpResponse<String> denied = send(req("/api/posts").method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", "https://evil.example.com")
                .header("Access-Control-Request-Method", "GET"));
        assertThat(denied.statusCode()).isEqualTo(403);
    }

    @Test
    void authEndpointsAreRateLimitedPerIp() throws Exception {
        int limited = 0;
        for (int i = 0; i < 8; i++) {
            int status = send(req("/api/auth/login").POST(HttpRequest.BodyPublishers.noBody())
                    .header("X-Forwarded-For", "203.0.113.7")).statusCode();
            if (status == 429) {
                limited++;
            }
        }
        assertThat(limited).isGreaterThanOrEqualTo(4);
        int other = send(req("/api/auth/login").POST(HttpRequest.BodyPublishers.noBody())
                .header("X-Forwarded-For", "198.51.100.1")).statusCode();
        assertThat(other).isNotEqualTo(429);
    }
}
