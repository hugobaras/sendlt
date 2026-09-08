package com.sendlt.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendlt.api.dto.attempt.AttemptResponse;
import com.sendlt.api.dto.attempt.CreateAttemptRequest;
import com.sendlt.api.dto.boulder.BoulderResponse;
import com.sendlt.api.dto.boulder.CreateBoulderRequest;
import com.sendlt.api.dto.gym.CreateGymRequest;
import com.sendlt.api.dto.gym.GymResponse;
import com.sendlt.api.dto.sector.CreateSectorRequest;
import com.sendlt.api.dto.sector.SectorResponse;
import com.sendlt.api.dto.session.CreateSessionRequest;
import com.sendlt.api.dto.session.JoinSessionRequest;
import com.sendlt.api.dto.session.SessionResponse;
import com.sendlt.api.dto.stats.GradePyramidResponse;
import com.sendlt.api.dto.stats.SessionAverageGradeResponse;
import com.sendlt.api.dto.stats.VolumeStatsResponse;
import com.sendlt.domain.enums.AttemptType;
import com.sendlt.domain.enums.BoulderColor;
import com.sendlt.domain.enums.HoldStyle;
import com.sendlt.domain.enums.ScoringMode;
import com.sendlt.domain.repository.UserRepository;
import com.sendlt.infrastructure.websocket.StompDestinations;
import com.sendlt.support.AbstractIntegrationTest;
import com.sendlt.support.TestAdminSupport;
import com.sendlt.support.TestApiClient;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

class SendltApiIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private WebSocketStompClient stompClient;
    private StompSession listenerSession;

    @BeforeEach
    void setUpStompClient() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);
    }

    @AfterEach
    void tearDownStomp() {
        if (listenerSession != null && listenerSession.isConnected()) {
            listenerSession.disconnect();
            listenerSession = null;
        }
    }

    @Test
    void fullScenarioFromRegisterToStats() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String adminEmail = "admin-" + suffix + "@test.sendlt";
        String climberEmail = "climber-" + suffix + "@test.sendlt";

        String adminToken = TestAdminSupport.registerSetterAdmin(
                restClient, userRepository, adminEmail, "Admin " + suffix);
        String climberToken =
                TestApiClient.registerAndLogin(restClient, climberEmail, "Climber " + suffix);

        GymResponse gym = restClient
                .post()
                .uri("/api/gyms")
                .header("Authorization", TestApiClient.bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateGymRequest("Test Gym " + suffix, "Paris", ScoringMode.FONT))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(GymResponse.class)
                .returnResult()
                .getResponseBody();

        SectorResponse sector = restClient
                .post()
                .uri("/api/sectors")
                .header("Authorization", TestApiClient.bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateSectorRequest(gym.id(), "Sector A"))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(SectorResponse.class)
                .returnResult()
                .getResponseBody();

        BoulderResponse boulder5a = createBoulder(adminToken, sector.id(), "5a");
        BoulderResponse boulder6a = createBoulder(adminToken, sector.id(), "6a");

        SessionResponse session = restClient
                .post()
                .uri("/api/sessions")
                .header("Authorization", TestApiClient.bearer(climberToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateSessionRequest(gym.id()))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(SessionResponse.class)
                .returnResult()
                .getResponseBody();

        postAttempt(climberToken, session.id(), boulder5a.id(), AttemptType.FLASH, 1);
        postAttempt(climberToken, session.id(), boulder6a.id(), AttemptType.TOP_AFTER_TRIES, 2);

        VolumeStatsResponse volume = restClient
                .get()
                .uri("/api/stats/me/volume")
                .header("Authorization", TestApiClient.bearer(climberToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(VolumeStatsResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(volume.successfulBoulders()).isEqualTo(2);

        GradePyramidResponse pyramid = restClient
                .get()
                .uri("/api/stats/me/grade-pyramid")
                .header("Authorization", TestApiClient.bearer(climberToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(GradePyramidResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(pyramid.entries()).extracting(GradePyramidResponse.GradePyramidEntry::grade)
                .containsExactly("5a", "6a");
        assertThat(pyramid.entries()).extracting(GradePyramidResponse.GradePyramidEntry::count)
                .containsExactly(1L, 1L);

        SessionAverageGradeResponse average = restClient
                .get()
                .uri("/api/stats/sessions/{sessionId}/average-grade", session.id())
                .header("Authorization", TestApiClient.bearer(climberToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(SessionAverageGradeResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(average.successfulAttempts()).isEqualTo(2);
        assertThat(average.averageGradeLabel()).isNotBlank();
        assertThat(average.averageScore()).isGreaterThan(0);
    }

    @Test
    void notFoundReturnsErrorBody() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String climberEmail = "nf-" + suffix + "@test.sendlt";
        String climberToken = TestApiClient.registerAndLogin(restClient, climberEmail, "Climber");

        restClient
                .get()
                .uri("/api/boulders/{id}", UUID.randomUUID())
                .header("Authorization", TestApiClient.bearer(climberToken))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo(404)
                .jsonPath("$.error")
                .isEqualTo("Not Found")
                .jsonPath("$.path")
                .exists();
    }

    @Test
    void flashRuleRejectsInvalidTriesCount() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String adminEmail = "admin-flash-" + suffix + "@test.sendlt";
        String climberEmail = "climber-flash-" + suffix + "@test.sendlt";

        String adminToken = TestAdminSupport.registerSetterAdmin(
                restClient, userRepository, adminEmail, "Admin");
        String climberToken = TestApiClient.registerAndLogin(restClient, climberEmail, "Climber");

        GymResponse gym = restClient
                .post()
                .uri("/api/gyms")
                .header("Authorization", TestApiClient.bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateGymRequest("Gym", "Lyon", ScoringMode.FONT))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(GymResponse.class)
                .returnResult()
                .getResponseBody();

        SectorResponse sector = restClient
                .post()
                .uri("/api/sectors")
                .header("Authorization", TestApiClient.bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateSectorRequest(gym.id(), "S1"))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(SectorResponse.class)
                .returnResult()
                .getResponseBody();

        BoulderResponse boulder = createBoulder(adminToken, sector.id(), "5a");

        SessionResponse session = restClient
                .post()
                .uri("/api/sessions")
                .header("Authorization", TestApiClient.bearer(climberToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateSessionRequest(gym.id()))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(SessionResponse.class)
                .returnResult()
                .getResponseBody();

        restClient
                .post()
                .uri("/api/sessions/{sessionId}/attempts", session.id())
                .header("Authorization", TestApiClient.bearer(climberToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateAttemptRequest(boulder.id(), AttemptType.FLASH, 3))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void attemptOwnershipIsEnforced() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String adminEmail = "admin-own-" + suffix + "@test.sendlt";
        String ownerEmail = "owner-" + suffix + "@test.sendlt";
        String intruderEmail = "intruder-" + suffix + "@test.sendlt";

        String adminToken = TestAdminSupport.registerSetterAdmin(
                restClient, userRepository, adminEmail, "Admin");
        String ownerToken = TestApiClient.registerAndLogin(restClient, ownerEmail, "Owner");
        String intruderToken = TestApiClient.registerAndLogin(restClient, intruderEmail, "Intruder");

        GymResponse gym = restClient
                .post()
                .uri("/api/gyms")
                .header("Authorization", TestApiClient.bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateGymRequest("Gym", "Nantes", ScoringMode.FONT))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(GymResponse.class)
                .returnResult()
                .getResponseBody();

        SectorResponse sector = restClient
                .post()
                .uri("/api/sectors")
                .header("Authorization", TestApiClient.bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateSectorRequest(gym.id(), "S1"))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(SectorResponse.class)
                .returnResult()
                .getResponseBody();

        BoulderResponse boulder = createBoulder(adminToken, sector.id(), "5a");

        SessionResponse session = restClient
                .post()
                .uri("/api/sessions")
                .header("Authorization", TestApiClient.bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateSessionRequest(gym.id()))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(SessionResponse.class)
                .returnResult()
                .getResponseBody();

        restClient
                .post()
                .uri("/api/sessions/join")
                .header("Authorization", TestApiClient.bearer(intruderToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new JoinSessionRequest(session.roomCode()))
                .exchange()
                .expectStatus()
                .isOk();

        AttemptResponse attempt = postAttempt(ownerToken, session.id(), boulder.id(), AttemptType.FLASH, 1);

        restClient
                .delete()
                .uri("/api/sessions/{sessionId}/attempts/{attemptId}", session.id(), attempt.id())
                .header("Authorization", TestApiClient.bearer(intruderToken))
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void listenerReceivesAttemptCreatedEventWhenPeerPostsViaRest() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String adminEmail = "admin-ws-" + suffix + "@test.sendlt";
        String posterEmail = "poster-ws-" + suffix + "@test.sendlt";
        String listenerEmail = "listener-ws-" + suffix + "@test.sendlt";

        String adminToken = TestAdminSupport.registerSetterAdmin(
                restClient, userRepository, adminEmail, "Admin");
        String posterToken = TestApiClient.registerAndLogin(restClient, posterEmail, "Poster");
        String listenerToken = TestApiClient.registerAndLogin(restClient, listenerEmail, "Listener");

        GymResponse gym = restClient
                .post()
                .uri("/api/gyms")
                .header("Authorization", TestApiClient.bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateGymRequest("WS Gym", "Paris", ScoringMode.FONT))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(GymResponse.class)
                .returnResult()
                .getResponseBody();

        SectorResponse sector = restClient
                .post()
                .uri("/api/sectors")
                .header("Authorization", TestApiClient.bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateSectorRequest(gym.id(), "WS Sector"))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(SectorResponse.class)
                .returnResult()
                .getResponseBody();

        BoulderResponse boulder = createBoulder(adminToken, sector.id(), "5a");

        SessionResponse session = restClient
                .post()
                .uri("/api/sessions")
                .header("Authorization", TestApiClient.bearer(posterToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateSessionRequest(gym.id()))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(SessionResponse.class)
                .returnResult()
                .getResponseBody();

        restClient
                .post()
                .uri("/api/sessions/join")
                .header("Authorization", TestApiClient.bearer(listenerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new JoinSessionRequest(session.roomCode()))
                .exchange()
                .expectStatus()
                .isOk();

        BlockingQueue<String> rawMessages = new LinkedBlockingQueue<>();
        String wsUrl = "ws://localhost:" + port + "/ws-native";

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", TestApiClient.bearer(listenerToken));

        listenerSession = stompClient
                .connectAsync(
                        wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        String topic = StompDestinations.sessionActivity(session.id());
        listenerSession.subscribe(
                topic,
                new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return byte[].class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        rawMessages.offer(new String((byte[]) payload, StandardCharsets.UTF_8));
                    }
                });

        Thread.sleep(500);

        postAttempt(posterToken, session.id(), boulder.id(), AttemptType.FLASH, 1);

        String raw = rawMessages.poll(10, TimeUnit.SECONDS);
        assertThat(raw).isNotBlank();
        assertThat(raw).contains("\"eventType\":\"ATTEMPT_CREATED\"");
        assertThat(raw).contains(session.id().toString());
        assertThat(raw).contains(boulder.id().toString());
        assertThat(raw).contains("\"type\":\"FLASH\"");
        assertThat(raw).contains("\"triesCount\":1");
    }

    private BoulderResponse createBoulder(String adminToken, UUID sectorId, String gradeFont) {
        return restClient
                .post()
                .uri("/api/boulders")
                .header("Authorization", TestApiClient.bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateBoulderRequest(
                        sectorId,
                        BoulderColor.BLEU,
                        gradeFont,
                        null,
                        HoldStyle.TECHNICAL,
                        LocalDate.of(2026, 1, 1)))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(BoulderResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private AttemptResponse postAttempt(
            String token, UUID sessionId, UUID boulderId, AttemptType type, int triesCount) {
        return restClient
                .post()
                .uri("/api/sessions/{sessionId}/attempts", sessionId)
                .header("Authorization", TestApiClient.bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateAttemptRequest(boulderId, type, triesCount))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(AttemptResponse.class)
                .returnResult()
                .getResponseBody();
    }
}
