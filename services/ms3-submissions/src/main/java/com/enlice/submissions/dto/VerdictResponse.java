package com.enlice.submissions.dto;

import java.time.Instant;
import java.util.UUID;

public record VerdictResponse(
        UUID submissionId,
        String result,
        Integer passed,
        Integer failed,
        Integer total,
        Double coverage,
        String logsUrl,
        Instant verifiedAt) {

    public static VerdictResponse from(com.enlice.submissions.domain.Verdict verdict) {
        return new VerdictResponse(verdict.getSubmissionId(), verdict.getResult(), verdict.getPassed(),
                verdict.getFailed(), verdict.getTotal(), verdict.getCoverage(), verdict.getLogsUrl(),
                verdict.getVerifiedAt());
    }
}
