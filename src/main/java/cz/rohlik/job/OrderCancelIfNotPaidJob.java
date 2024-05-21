package cz.rohlik.job;

import cz.rohlik.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancelIfNotPaidJob implements Job {
    private final OrderService orderService;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Executing OrderCancelIfNotPaidJob for order with ID: {}", context.getJobDetail().getJobDataMap().get("orderId"));
        orderService.cancelOrderIfNotPaid((Long) context.getJobDetail().getJobDataMap().get("orderId"));
    }
}
