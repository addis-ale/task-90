package com.example.ricms.controller;

import com.example.ricms.dto.request.WorkOrderCostRequest;
import com.example.ricms.dto.request.WorkOrderCreateRequest;
import com.example.ricms.dto.request.WorkOrderEventRequest;
import com.example.ricms.dto.request.WorkOrderRatingRequest;
import com.example.ricms.dto.response.WorkOrderAnalyticsResponse;
import com.example.ricms.dto.response.WorkOrderResponse;
import com.example.ricms.security.SecurityUtils;
import com.example.ricms.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/v1/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @PostMapping
    public ResponseEntity<WorkOrderResponse> createWorkOrder(@RequestBody WorkOrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workOrderService.createWorkOrder(request, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/{workOrderId}")
    public ResponseEntity<WorkOrderResponse> getWorkOrder(@PathVariable UUID workOrderId) {
        return ResponseEntity.ok(workOrderService.getWorkOrder(workOrderId));
    }

    @PostMapping("/{workOrderId}/claim")
    public ResponseEntity<WorkOrderResponse> claimWorkOrder(@PathVariable UUID workOrderId) {
        return ResponseEntity.ok(workOrderService.claimWorkOrder(workOrderId, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/{workOrderId}/events")
    public ResponseEntity<WorkOrderResponse> addEvent(
            @PathVariable UUID workOrderId,
            @Valid @RequestBody WorkOrderEventRequest request) {
        return ResponseEntity.ok(workOrderService.addEvent(
                workOrderId, request.getEventType(), request.getPayload(), SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/{workOrderId}/cost")
    public ResponseEntity<WorkOrderResponse> updateCost(
            @PathVariable UUID workOrderId,
            @RequestBody WorkOrderCostRequest request) {
        return ResponseEntity.ok(workOrderService.updateCost(workOrderId, request, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/{workOrderId}/rating")
    public ResponseEntity<WorkOrderResponse> submitRating(
            @PathVariable UUID workOrderId,
            @Valid @RequestBody WorkOrderRatingRequest request) {
        return ResponseEntity.ok(workOrderService.submitRating(workOrderId, request, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/analytics")
    public ResponseEntity<WorkOrderAnalyticsResponse> getAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID technicianId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(workOrderService.getAnalytics(from, to, technicianId, status));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportWorkOrders(
            @RequestParam(defaultValue = "csv") String format) {
        byte[] data = workOrderService.exportCsv();
        String contentType = "text/csv";
        String filename = "work_orders.csv";
        if ("excel".equalsIgnoreCase(format)) {
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            filename = "work_orders.xlsx";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }
}
