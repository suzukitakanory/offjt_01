package com.attendance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * アプリケーション起動テスト
 * 関連要件: REQ-UC001 - システム起動
 */
@SpringBootTest
@Testcontainers
class AttendanceSystemApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Test
    void contextLoads() {
        // TODO: REQ-UC001 - アプリケーションコンテキストの起動確認
        assertTrue(true);
    }
}
