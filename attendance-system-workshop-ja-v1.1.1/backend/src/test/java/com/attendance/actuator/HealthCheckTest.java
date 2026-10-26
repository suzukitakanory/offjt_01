package com.attendance.actuator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * システムヘルスチェックエンドポイントのテスト
 * 関連要件: REQ-UC001 - システム起動
 * 関連シーケンス: SEQ-002 - システムヘルスチェック
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class HealthCheckTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private MockMvc mockMvc;

    /**
     * ヘルスチェックエンドポイントが正常なステータスを返すことを確認
     * 
     * テストシナリオ: システムヘルスチェックが正常に動作する
     * 期待結果:
     * - HTTPステータス 200 が返される
     * - レスポンスに "UP" ステータスが含まれる
     */
    @Test
    void testHealthCheckReturnsUpStatus() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    /**
     * ヘルスチェックエンドポイントがコンポーネント詳細を含むことを確認
     * 
     * テストシナリオ: ヘルスチェックがコンポーネント情報を返す
     * 期待結果:
     * - レスポンスに "components" フィールドが含まれる
     * - データベースコンポーネントの情報が含まれる
     */
    @Test
    void testHealthCheckIncludesComponents() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components").exists())
                .andExpect(jsonPath("$.components.db").exists());
    }

    /**
     * ヘルスチェックエンドポイントがJSON形式で応答することを確認
     * 
     * テストシナリオ: ヘルスチェックがJSON形式で応答する
     * 期待結果:
     * - Content-Type が application/json である
     */
    @Test
    void testHealthCheckReturnsJson() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.spring-boot.actuator.v3+json"));
    }
}
