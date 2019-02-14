package org.bonitasoft.support.plugin.job;

import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.exception.failedJob.SFailedJobReadException;
import org.bonitasoft.engine.scheduler.model.SFailedJob;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class JobHealthCheckService implements TenantLifecycleService {

    private final SessionAccessor sessionAccessor;
    private final UserTransactionService userTransactionService;
    private final SchedulerService schedulerService;
    private final JobService jobService;
    private final TechnicalLogger technicalLogger;
    private final int maxResults;
    private final long tenantId;
    private boolean stateStarted = false;

    @Autowired
    public JobHealthCheckService(SessionAccessor sessionAccessor ,
                                 UserTransactionService userTransactionService,
                                 SchedulerService schedulerService,
                                 JobService jobService,
                                 @Qualifier("tenantTechnicalLoggerService") TechnicalLoggerService technicalLoggerService,
                                 @Value("${org.bonitasoft.support.plugin.job.MAX_RESULTS:100}") int maxResults,
                                 @Value("${tenantId}") long tenantId
                                 ) {
        this.sessionAccessor = sessionAccessor;
        this.userTransactionService = userTransactionService;
        this.schedulerService = schedulerService;
        this.jobService = jobService;
        this.technicalLogger = technicalLoggerService.asLogger(this.getClass());
        this.maxResults = maxResults;
        this.tenantId = tenantId;
    }

    @Override
    public void start() throws SBonitaException {
        technicalLogger.debug("Starting to periodically check the failed jobs.");
        stateStarted = true;
    }

    @Scheduled(fixedDelayString = "${org.bonitasoft.support.plugin.job.DELAY_IN_MILLIS:3600000}")
    private void executeInTransactionHealthCheckAndReplay() throws Exception {
        if (!stateStarted) {
            return;
        }
        sessionAccessor.setTenantId(tenantId);
        userTransactionService.executeInTransaction(this::healthCheckAndReplay);
    }

    private Void healthCheckAndReplay() {
        technicalLogger.info("checking failed jobs.");
        try {
            int startIndex = 0;
            int nbJobsFound = -1;
            int totalNbFailedJobs = 0;
            while (nbJobsFound == -1 || nbJobsFound == maxResults) {
                List<SFailedJob> failedJobs = null;
                failedJobs = jobService.getFailedJobs(startIndex, maxResults);
                nbJobsFound = failedJobs.size();
                totalNbFailedJobs += nbJobsFound;
                for (SFailedJob failedJob : failedJobs) {
                    if (!stateStarted) {
                        return null;
                    }
                    try {
                        schedulerService.executeAgain(failedJob.getJobDescriptorId());
                    } catch (SSchedulerException e) {
                        technicalLogger.error("Failed to replay a failed job with id: {}, retryNumber: {}.", failedJob.getJobDescriptorId(), failedJob.getRetryNumber(), e);
                    }
                }
            }
            technicalLogger.info("{} failed jobs found.", totalNbFailedJobs);
        } catch (SFailedJobReadException e) {
            technicalLogger.error("Failed to retrieve the failed jobs.", e);
        }
        return null;
    }

    @Override
    public void stop() throws SBonitaException {
        technicalLogger.info("Stopping.");
        stateStarted = false;
    }

    @Override
    public void pause() throws SBonitaException {
        technicalLogger.info("Pausing.");
        stateStarted = false;
    }

    @Override
    public void resume() throws SBonitaException {
        technicalLogger.info("Resuming.");
        stateStarted = true;
    }

}
