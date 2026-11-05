package com.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 勤怠管理システム バックエンドアプリケーション
 * 関連要件: REQ-UC001 - システム起動
 */
@SpringBootApplication
public class AttendanceSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceSystemApplication.class, args);
    }
}
