package com.rowing.controller;

import com.rowing.dto.request.BindingRequest;
import com.rowing.dto.response.ApiResponse;
import com.rowing.dto.response.BindingDTO;
import com.rowing.dto.response.ChangeLogDTO;
import com.rowing.dto.response.PageResult;
import com.rowing.service.BindingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/binding")
@RequiredArgsConstructor
public class BindingController {

    private final BindingService bindingService;

    @PostMapping("/bind")
    public ApiResponse<BindingDTO> bind(@Valid @RequestBody BindingRequest request) {
        return ApiResponse.success(bindingService.bind(request));
    }

    @PostMapping("/unbind")
    public ApiResponse<BindingDTO> unbind(@Valid @RequestBody BindingRequest request) {
        return ApiResponse.success(bindingService.unbind(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<BindingDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(bindingService.getById(id));
    }

    @GetMapping("/bracket/{bracketId}")
    public ApiResponse<List<BindingDTO>> getByBracketId(@PathVariable Long bracketId) {
        return ApiResponse.success(bindingService.findByBracketId(bracketId));
    }

    @GetMapping("/group/{groupId}")
    public ApiResponse<List<BindingDTO>> getByGroupId(@PathVariable Long groupId) {
        return ApiResponse.success(bindingService.findByGroupId(groupId));
    }

    @GetMapping("/active")
    public ApiResponse<List<BindingDTO>> getAllActive() {
        return ApiResponse.success(bindingService.findAllActive());
    }

    @GetMapping("/all")
    public ApiResponse<List<BindingDTO>> getAll() {
        return ApiResponse.success(bindingService.findAll());
    }

    @GetMapping("/logs/bracket/{bracketId}")
    public ApiResponse<List<ChangeLogDTO>> getLogsByBracketId(@PathVariable Long bracketId) {
        return ApiResponse.success(bindingService.getLogsByBracketId(bracketId));
    }

    @GetMapping("/logs/group/{groupId}")
    public ApiResponse<List<ChangeLogDTO>> getLogsByGroupId(@PathVariable Long groupId) {
        return ApiResponse.success(bindingService.getLogsByGroupId(groupId));
    }

    @GetMapping("/logs/page")
    public ApiResponse<PageResult<ChangeLogDTO>> queryLogs(
            @RequestParam(required = false) String bracketCode,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String changeType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        return ApiResponse.success(bindingService.queryLogs(bracketCode, groupName, changeType, pageNum, pageSize));
    }

    @GetMapping("/logs/export")
    public ResponseEntity<byte[]> exportLogs(
            @RequestParam(required = false) String bracketCode,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String changeType) {

        List<ChangeLogDTO> logs = bindingService.listLogsForExport(bracketCode, groupName, changeType);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder csv = new StringBuilder("\uFEFF"); // BOM，Excel 打开中文不乱码
        csv.append("支架编号,组别名称,变更类型,变更前距离(m),变更后距离(m),变更原因,操作人,变更时间\n");
        for (ChangeLogDTO entry : logs) {
            csv.append(csvCell(entry.getBracketCode())).append(',')
                    .append(csvCell(entry.getGroupName())).append(',')
                    .append(csvCell(changeTypeName(entry.getChangeType()))).append(',')
                    .append(entry.getPreviousDistance() != null ? entry.getPreviousDistance() : "").append(',')
                    .append(entry.getNewDistance() != null ? entry.getNewDistance() : "").append(',')
                    .append(csvCell(entry.getChangeReason())).append(',')
                    .append(csvCell(entry.getOperator())).append(',')
                    .append(entry.getChangedAt() != null ? entry.getChangedAt().format(timeFormatter) : "")
                    .append('\n');
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String asciiFilename = "change-logs-" + timestamp + ".csv";
        String utf8Filename = URLEncoder.encode("变更日志-" + timestamp + ".csv", StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + asciiFilename + "\"; filename*=UTF-8''" + utf8Filename)
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String csvCell(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String changeTypeName(String changeType) {
        if (changeType == null) {
            return "";
        }
        return switch (changeType) {
            case "BIND" -> "绑定";
            case "UNBIND" -> "解绑";
            case "UPDATE" -> "更新";
            default -> changeType;
        };
    }

    @GetMapping("/logs/recent")
    public ApiResponse<List<ChangeLogDTO>> getRecentLogs(@RequestParam(defaultValue = "20") Integer limit) {
        return ApiResponse.success(bindingService.getRecentLogs(limit));
    }
}