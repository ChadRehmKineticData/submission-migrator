package com.kineticdata.migrator.models;

import com.bmc.arsys.api.Entry;
import com.bmc.arsys.api.Timestamp;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Submission {
    public static final String FORM = "KS_SRV_CustomerSurvey_base";
    public static final int CLOSED_AT = 700088489;
    public static final int CREATED_AT = 3;
    public static final int ID = 179;
    public static final int REQUEST_ID = 1;
    public static final int REQUEST_STATUS = 700089541;
    public static final int ORIGINATING_ID =  600000310;
    public static final int ORIGINATINGID_DISPLAY = 700088607;
    public static final int STATUS = 7;
    public static final int SUBMITTED_AT = 700001285;
    public static final int SUBMITTER = 2;
    public static final int TEMPLATE_ID = 700000800;
    public static final int UPDATED_AT = 6;
    public static final int VALIDATION_STATUS = 700002400;
    public static final int[] ATTRIBUTES = { 300299400, 300299500, 300299600, 300299700, 300299800,
            700001806, 700001807, 700001808, 700001809, 700001810, 700001811, 700001812, 700001813, 700001814, 700001815,
            700001816, 700001817, 700001818, 700001819, 700001820, 700001821, 700001822, 700001823, 700001824, 700001825,
            700001826, 700001827, 700001828, 700001829, 700001830, 700001831, 700001832, 700001833, 700001834, 700001835,
            700001836, 700001837, 700001838, 700001839, 700001840, 700001841, 700001842, 700001843, 700001844, 700001845,
            700001846, 700001847, 700001848, 700001849, 700001850, 700001851, 700001852, 700001853, 700001854, 700001855,
            700001856, 700001857, 700001858, 700001859, 700001860, 700001861, 700001862, 700001863, 700001864, 700001865,
            700001866, 700001867, 700001868, 700001869, 700001870 };
    private static final List<String> REQUEST_STATUSES = Arrays.asList("Open", "Closed");
    private static final List<String> STATUSES = Arrays.asList("New", "Sent", "Completed",
            "Expired", "Delete", "In Progress", "Opt Out");
    public static final Submission POISON = new Submission();
    private final String closedAt;
    private final String createdAt;
    private final String id;
    private final String requestId;
    private final Integer requestStatus;
    // private final <> originatingId;
    // private final <> originatingIdDisplay;
    private final Integer status;
    private final String submittedAt;
    private final String submitter;
    private final String updatedAt;
    private final String validationStatus;
    private final List<String> attributeValues;
    private final List<Answer> answers;
    private final List<UnlimitedAnswer> unlimitedAnswers;
    private final List<Attachment> attachments;

    private Submission() {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public Submission(Entry entry) {
        this((String) entry.get(ID).getValue(),
             (String) entry.get(REQUEST_ID).getValue(),
             (Integer) entry.get(STATUS).getValue(),
             (Integer) entry.get(REQUEST_STATUS).getValue(),
             System.out.println(entry.get(ORIGINATING_ID).getValue()),
             System.out.println(entry.get(ORIGINATINGID_DISPLAY).getValue()),
             // ()entry.get(ORIGINATING_ID).getValue(),
             // ()entry.get(ORIGINATINGID_DISPLAY).getValue(),
             (String) entry.get(VALIDATION_STATUS).getValue(),
             (String) entry.get(SUBMITTER).getValue(),
             timestampToString((Timestamp)entry.get(CLOSED_AT).getValue()),
             timestampToString((Timestamp)entry.get(CREATED_AT).getValue()),
             timestampToString((Timestamp)entry.get(SUBMITTED_AT).getValue()),
             timestampToString((Timestamp)entry.get(UPDATED_AT).getValue()),
             Arrays.stream(ATTRIBUTES).mapToObj(entry::get)
                                      .map(value -> value == null ? "" : (String) value.getValue())
                                      .collect(Collectors.toList()),
             null, null, null);
    }

    private Submission(String id, String requestId, Integer status, Integer requestStatus,
                       String validationStatus, String submitter, String closedAt, String createdAt,
                       String submittedAt, String updatedAt, List<String> attributeValues, List<Answer> answers,
                       List<UnlimitedAnswer> unlimitedAnswers, List<Attachment> attachments) {
        this.closedAt = closedAt;
        this.createdAt = createdAt;
        this.id = id;
        this.requestId = requestId;
        this.requestStatus = requestStatus;
        // this.originatingId = originatingId;
        // this.originatingIdDisplay = originatingIdDisplay;
        this.status = status;
        this.submittedAt = submittedAt;
        this.submitter = submitter;
        this.updatedAt = updatedAt;
        this.validationStatus = validationStatus;
        this.attributeValues = attributeValues;
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

    // public String getOriginatingId() {
    //     return originatingId;
    // }

    // public String getOriginatingIdDisplay() {
    //     return originatingIdDisplay;
    // }

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

    public List<String> getAttributeValues() {
        return attributeValues;
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
                closedAt, createdAt, submittedAt, updatedAt, attributeValues, answers, unlimitedAnswers, attachments);
    }

    public Submission withUnlimitedAnswers(List<UnlimitedAnswer> unlimitedAnswers) {
        return new Submission(id, requestId, status, requestStatus, validationStatus, submitter,
                closedAt, createdAt, submittedAt, updatedAt, attributeValues, answers, unlimitedAnswers, attachments);
    }

    public Submission withAttachments(List<Attachment> attachments) {
        return new Submission(id, requestId, status, requestStatus, validationStatus, submitter,
                closedAt, createdAt, submittedAt, updatedAt, attributeValues, answers, unlimitedAnswers, attachments);
    }

    private static String timestampToString(Timestamp timestamp) {
        return timestamp == null
                ? null
                : Instant.ofEpochSecond(timestamp.getValue()).toString();
    }

}