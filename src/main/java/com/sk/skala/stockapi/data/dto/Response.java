package com.sk.skala.stockapi.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Response {
    private int result;      // 성공/실패 플래그 용도(프로젝트 규칙대로)
    private int code;        // Error code or 0
    private String message;  // 안내 메시지
    private Object body;     // 실제 데이터
    private Object error;    // 필요 시 상세 에러(옵션)
}
