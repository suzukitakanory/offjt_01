package com.attendance.controller;

import com.attendance.dto.ApiErrorResponse;
import com.attendance.dto.LeaveResponse;
import com.attendance.dto.UpdateLeaveRequest;
import com.attendance.exception.LeaveApiException;
import com.attendance.service.LeaveService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 休暇 API の作成・取得契約テスト。
 * 関連要件: REQ-UC004 - 休暇登録
 * 契約: specs/002-register-leave/contracts/leave-api.yaml
 */
@WebMvcTest(LeaveController.class)
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveService leaveService;

    /** 新規登録が201となり、休暇情報の応答に従業員IDを含めないことを確認する。 */
    @Test
    void createsLeaveWithStatus201() throws Exception {
        when(leaveService.create(any())).thenReturn(new LeaveService.CreationResult(
                new LeaveResponse("2026-11-09", "Sick Leave", "体調不良"), false));

        mockMvc.perform(post("/api/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2026-11-09","leaveType":"Sick Leave","memo":"体調不良"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date").value("2026-11-09"))
                .andExpect(jsonPath("$.leaveType").value("Sick Leave"))
                .andExpect(jsonPath("$.memo").value("体調不良"))
                .andExpect(jsonPath("$.employeeId").doesNotExist());
    }

    /** 確認済み勤務日の置換が200となり、確認フラグがサービスへ渡ることを確認する。 */
    @Test
    void replacesConfirmedWorkDayWithStatus200() throws Exception {
        when(leaveService.create(any())).thenReturn(new LeaveService.CreationResult(
                new LeaveResponse("2026-11-10", "Business Trip", ""), true));

        mockMvc.perform(post("/api/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2026-11-10","leaveType":"Business Trip","confirmReplaceWork":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaveType").value("Business Trip"))
                .andExpect(jsonPath("$.memo").value(""));
        verify(leaveService).create(argThat(request -> Boolean.TRUE.equals(request.getConfirmReplaceWork())));
    }

    /** 勤務行がない場合は確認フラグがあっても新規作成として201になることを確認する。 */
    @Test
    void createsUnusedDateWithStatus201EvenWhenConfirmationWasSent() throws Exception {
        when(leaveService.create(any())).thenReturn(new LeaveService.CreationResult(
                new LeaveResponse("2026-11-10", "Business Trip", ""), false));

        mockMvc.perform(post("/api/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2026-11-10","leaveType":"Business Trip","confirmReplaceWork":true}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void getsLeaveByDate() throws Exception {
        when(leaveService.getByDate("2026-11-09"))
                .thenReturn(new LeaveResponse("2026-11-09", "Personal Leave", "私用"));

        mockMvc.perform(get("/api/leaves/2026-11-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-11-09"))
                .andExpect(jsonPath("$.leaveType").value("Personal Leave"))
                .andExpect(jsonPath("$.memo").value("私用"));
    }

    /** 期間一覧を返し、指定範囲がサービスへ渡ることを確認する。 */
    @Test
    void listsLeavesForPeriod() throws Exception {
        when(leaveService.getLeaves("2026-12-01", "2026-12-31"))
                .thenReturn(List.of(new LeaveResponse("2026-12-10", "Sick Leave", "通院")));

        mockMvc.perform(get("/api/leaves")
                        .param("from", "2026-12-01")
                        .param("to", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-12-10"))
                .andExpect(jsonPath("$[0].leaveType").value("Sick Leave"))
                .andExpect(jsonPath("$[0].memo").value("通院"));
        verify(leaveService).getLeaves("2026-12-01", "2026-12-31");
    }

    /** 期間片側指定で400とERR03008を返すことを確認する。 */
    @Test
    void returns400WhenLeaveListPeriodIsIncomplete() throws Exception {
        when(leaveService.getLeaves("2026-12-01", null))
                .thenThrow(LeaveApiException.incompletePeriod());

        mockMvc.perform(get("/api/leaves").param("from", "2026-12-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ERR03008"))
                .andExpect(jsonPath("$.message").value("開始日と終了日は両方指定してください"));
    }

    /** 日付が未指定の場合に400を返し、サービスを呼び出さないことを確認する。 */
    @Test
    void returns400WhenDateIsMissing() throws Exception {
        expectBadRequest("""
                {"leaveType":"Sick Leave"}
                """, ApiErrorResponse.ERR03004, "休暇日を指定してください");
        verify(leaveService, never()).create(any());
    }

    /** 休暇区分が未指定の場合に対応する400エラーを返すことを確認する。 */
    @Test
    void returns400WhenLeaveTypeIsMissing() throws Exception {
        expectBadRequest("""
                {"date":"2026-11-09"}
                """, ApiErrorResponse.ERR03005, "休暇区分を選択してください");
    }

    /** 未対応の休暇区分を400エラーとして拒否することを確認する。 */
    @Test
    void returns400WhenLeaveTypeIsUnsupported() throws Exception {
        expectBadRequest("""
                {"date":"2026-11-09","leaveType":"Paid Leave"}
                """, ApiErrorResponse.ERR03006, "対応する休暇区分を選択してください");
    }

    /** 500文字を超えるメモを400エラーとして拒否することを確認する。 */
    @Test
    void returns400WhenMemoExceeds500Characters() throws Exception {
        expectBadRequest("""
                {"date":"2026-11-09","leaveType":"Sick Leave","memo":"%s"}
                """.formatted("あ".repeat(501)), ApiErrorResponse.ERR03007, "メモは500文字以内で入力してください");
    }

    /** 休暇が存在しない日付の取得が404とERR02002になることを確認する。 */
    @Test
    void returns404WhenLeaveDoesNotExist() throws Exception {
        when(leaveService.getByDate("2026-11-18")).thenThrow(LeaveApiException.leaveNotFound());

        mockMvc.perform(get("/api/leaves/2026-11-18"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ERR02002"))
                .andExpect(jsonPath("$.message").value("指定した日付の休暇はありません"))
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }

    /** 同日に休暇が既にある場合に409とERR02003を返すことを確認する。 */
    @Test
    void returns409WhenLeaveAlreadyExists() throws Exception {
        when(leaveService.create(any())).thenThrow(LeaveApiException.duplicateLeave());

        mockMvc.perform(post("/api/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2026-11-15","leaveType":"Personal Leave"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ERR02003"))
                .andExpect(jsonPath("$.message").value("この日付にはすでに休暇が登録されています。変更または削除を使ってください"))
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }

    /** 勤務日の置換確認がない場合に409とERR02005を返すことを確認する。 */
    @Test
    void returns409WhenWorkDayIsNotConfirmed() throws Exception {
        when(leaveService.create(any())).thenThrow(LeaveApiException.replaceRequiresConfirmation());

        mockMvc.perform(post("/api/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2026-11-16","leaveType":"Business Trip"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ERR02005"))
                .andExpect(jsonPath("$.message").value("この日付には勤務時間が登録されています。休暇にすると出勤・退勤時刻は削除されます"))
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }

    /** 更新要求が200を返し、更新内容と要求値がサービスへ渡ることを検証する。 */
    @Test
    void updatesLeaveWithStatus200() throws Exception {
        when(leaveService.update(eq("2026-11-18"), any()))
                .thenReturn(new LeaveResponse("2026-11-18", "Personal Leave", "更新後"));

        mockMvc.perform(put("/api/leaves/2026-11-18")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"leaveType":"Personal Leave","memo":"更新後"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-11-18"))
                .andExpect(jsonPath("$.leaveType").value("Personal Leave"))
                .andExpect(jsonPath("$.memo").value("更新後"));
        verify(leaveService).update(eq("2026-11-18"), argThat((UpdateLeaveRequest request) ->
                "Personal Leave".equals(request.getLeaveType()) && "更新後".equals(request.getMemo())));
    }

    /** 更新要求で休暇区分が未指定の場合に400となり、サービスを呼ばないことを検証する。 */
    @Test
    void returns400WhenUpdateLeaveTypeIsMissing() throws Exception {
        expectBadUpdateRequest("2026-11-18", "{}", ApiErrorResponse.ERR03005, "休暇区分を選択してください");
        verify(leaveService, never()).update(anyString(), any());
    }

    /** 未対応の休暇区分を含む更新要求が400となり、サービスを呼ばないことを検証する。 */
    @Test
    void returns400WhenUpdateLeaveTypeIsUnsupported() throws Exception {
        expectBadUpdateRequest("2026-11-18", """
                {"leaveType":"Paid Leave"}
                """, ApiErrorResponse.ERR03006, "対応する休暇区分を選択してください");
        verify(leaveService, never()).update(anyString(), any());
    }

    /** 501文字のメモを含む更新要求が400となり、サービスを呼ばないことを検証する。 */
    @Test
    void returns400WhenUpdateMemoExceeds500Characters() throws Exception {
        expectBadUpdateRequest("2026-11-18", """
                {"leaveType":"Sick Leave","memo":"%s"}
                """.formatted("あ".repeat(501)), ApiErrorResponse.ERR03007, "メモは500文字以内で入力してください");
        verify(leaveService, never()).update(anyString(), any());
    }

    /** 更新対象がない場合に404とERR02002を返すことを検証する。 */
    @Test
    void returns404WhenUpdateTargetDoesNotExist() throws Exception {
        when(leaveService.update(eq("2026-11-20"), any())).thenThrow(LeaveApiException.leaveNotFound());

        mockMvc.perform(put("/api/leaves/2026-11-20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"leaveType":"Sick Leave","memo":"更新"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ERR02002"))
                .andExpect(jsonPath("$.message").value("指定した日付の休暇はありません"))
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }

    private void expectBadUpdateRequest(String date, String body, String error, String message) throws Exception {
        mockMvc.perform(put("/api/leaves/" + date)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(error))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }

    private void expectBadRequest(String body, String error, String message) throws Exception {
        mockMvc.perform(post("/api/leaves").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(error))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }

    /** DELETE /api/leaves/{date} が204と出力本体なしで削除完了を返すことを検証する。 */
    @Test
    void deletesLeaveWithStatus204() throws Exception {
        mockMvc.perform(delete("/api/leaves/2026-11-26"))
                .andExpect(status().isNoContent());
        verify(leaveService).delete(eq("2026-11-26"));
    }

    /** 日付不正の場合に400とERR03004を返すことを検証する。 */
    @Test
    void returns400WhenDeleteDateIsInvalid() throws Exception {
        mockMvc.perform(delete("/api/leaves/2026-13-32"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ERR03004"))
                .andExpect(jsonPath("$.message").value("休暇日を指定してください"));
        verify(leaveService, never()).delete(anyString());
    }

    /** 削除対象がない場合に404とERR02002を返すことを検証する。 */
    @Test
    void returns404WhenDeleteTargetDoesNotExist() throws Exception {
        doThrow(LeaveApiException.leaveNotFound()).when(leaveService).delete(eq("2026-11-27"));

        mockMvc.perform(delete("/api/leaves/2026-11-27"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ERR02002"))
                .andExpect(jsonPath("$.message").value("指定した日付の休暇はありません"))
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }
}
