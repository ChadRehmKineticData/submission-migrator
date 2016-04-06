package com.kineticdata.migrator.models;

import com.bmc.arsys.api.Entry;
import com.google.common.base.Strings;

public class Answer {
    public static final String FORM = "KS_SRV_SurveyAnswer";
    public static final int FULL_ANSWER = 700002832;
    public static final int ATTACHMENT_ID = 700002850;
    public static final int SUBMISSION_ID = 700001850;
    public static final int QUESTION_ID = 700001890;
    private final String fullAnswer;
    private final String attachmentId;
    private final String submissionId;
    private final String questionId;

    public Answer(Entry entry) {
        this.fullAnswer = (String) entry.get(FULL_ANSWER).getValue();
        this.attachmentId = (String) entry.get(ATTACHMENT_ID).getValue();
        this.submissionId = (String) entry.get(SUBMISSION_ID).getValue();
        this.questionId = (String) entry.get(QUESTION_ID).getValue();
    }

    public String getFullAnswer() {
        return fullAnswer;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getDisplayedAnswer() {
        return Strings.isNullOrEmpty(attachmentId) ? fullAnswer : attachmentId;
    }
}