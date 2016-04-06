package com.kineticdata.migrator.models;

import com.bmc.arsys.api.Entry;
import com.bmc.arsys.api.Timestamp;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class Submission {
    public static final String FORM = "KS_SRV_CustomerSurvey_base";
    public static final int CLOSED_AT = 700088489;
    public static final int CREATED_AT = 3;
    public static final int ID = 179;
    public static final int REQUEST_ID = 1;
    public static final int REQUEST_STATUS = 700089541;
    public static final int STATUS = 7;
    public static final int SUBMITTED_AT = 700001285;
    public static final int SUBMITTER = 2;
    public static final int TEMPLATE_ID = 700000800;
    public static final int UPDATED_AT = 6;
    public static final int VALIDATION_STATUS = 700002400;
    private static final List<String> REQUEST_STATUSES = Arrays.asList(
            "Open", "Closed");
    private static final List<String> STATUSES = Arrays.asList(
            "New", "Sent", "Completed", "Expired", "Delete", "In Progress", "Opt Out");
    public static final Submission POISON = new Submission();
    private final String closedAt;
    private final String createdAt;
    private final String id;
    private final String requestId;
    private final Integer requestStatus;
    private final Integer status;
    private final String submittedAt;
    private final String submitter;
    private final String updatedAt;
    private final String validationStatus;
    private final List<Answer> answers;
    private final List<UnlimitedAnswer> unlimitedAnswers;
    private final List<Attachment> attachments;

    private Submission() {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public Submission(Entry entry) {
        this((String) entry.get(ID).getValue(),
             (String) entry.get(REQUEST_ID).getValue(),
             (Integer) entry.get(STATUS).getValue(),
             (Integer) entry.get(REQUEST_STATUS).getValue(),
             (String) entry.get(VALIDATION_STATUS).getValue(),
             (String) entry.get(SUBMITTER).getValue(),
             timestampToString((Timestamp)entry.get(CLOSED_AT).getValue()),
             timestampToString((Timestamp)entry.get(CREATED_AT).getValue()),
             timestampToString((Timestamp)entry.get(SUBMITTED_AT).getValue()),
             timestampToString((Timestamp)entry.get(UPDATED_AT).getValue()),
             null, null, null);
    }

    private Submission(String id, String requestId, Integer status, Integer requestStatus,
                       String validationStatus, String submitter, String closedAt, String createdAt,
                       String submittedAt, String updatedAt, List<Answer> answers,
                       List<UnlimitedAnswer> unlimitedAnswers, List<Attachment> attachments) {
        this.closedAt = closedAt;
        this.createdAt = createdAt;
        this.id = id;
        this.requestId = requestId;
        this.requestStatus = requestStatus;
        this.status = status;
        this.submittedAt = submittedAt;
        this.submitter = submitter;
        this.updatedAt = updatedAt;
        this.validationStatus = validationStatus;
        this.answers = answers;
        this.unlimitedAnswers = unlimitedAnswers;
        this.attachments = attachments;
    }

    public String getClosedAt() {
        return closedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getId() {
        return id;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getRequestStatus() {
        return REQUEST_STATUSES.get(requestStatus);
    }

    public String getStatus() {
        return STATUSES.get(status);
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public String getSubmitter() {
        return submitter;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public List<UnlimitedAnswer> getUnlimitedAnswers() {
        return unlimitedAnswers;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public Submission withAnswers(List<Answer> answers) {
        return new Submission(id, requestId, status, requestStatus, validationStatus, submitter,
                closedAt, createdAt, submittedAt, updatedAt, answers, unlimitedAnswers, attachments);
    }

    public Submission withUnlimitedAnswers(List<UnlimitedAnswer> unlimitedAnswers) {
        return new Submission(id, requestId, status, requestStatus, validationStatus, submitter,
                closedAt, createdAt, submittedAt, updatedAt, answers, unlimitedAnswers, attachments);
    }

    public Submission withAttachments(List<Attachment> attachments) {
        return new Submission(id, requestId, status, requestStatus, validationStatus, submitter,
                closedAt, createdAt, submittedAt, updatedAt, answers, unlimitedAnswers, attachments);
    }

    private static String timestampToString(Timestamp timestamp) {
        return timestamp == null
                ? null
                : Instant.ofEpochSecond(timestamp.getValue()).toString();
    }

}