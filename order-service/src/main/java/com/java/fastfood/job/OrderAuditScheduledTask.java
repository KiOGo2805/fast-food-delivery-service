package com.java.fastfood.job;

import com.java.fastfood.domain.enums.OrderStatus;
import com.java.fastfood.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAuditScheduledTask {

    private final OrderRepository orderRepository;

    @Value("${server.port:8081}")
    private String serverPort;

    @Scheduled(fixedRateString = "${app.scheduling.order-audit-rate-ms:10000}")
    @SchedulerLock(
            name = "OrderAuditScheduledTask_auditPendingOrders",
            lockAtLeastFor = "PT8S",
            lockAtMostFor = "PT1M"
    )
    public void auditPendingOrders() {
        String host = resolveHostName();
        long pendingCount = orderRepository.findByStatus(OrderStatus.PENDING).size();
        long totalCount = orderRepository.count();

        log.info("[ShedLock] Інстанс host='{}', port={} виконав аудит о {}. PENDING={}, Всього={}",
                host, serverPort, Instant.now(), pendingCount, totalCount);
    }

    private String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-host";
        }
    }
}