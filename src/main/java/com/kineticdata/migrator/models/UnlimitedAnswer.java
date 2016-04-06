package com.kineticdata.migrator.models;

import com.bmc.arsys.api.Entry;

public class UnlimitedAnswer {
    public static final String FORM = "KS_SRV_SurveyAnswerUnlimited";
    public static final int UNLIMITED_ANSWER = 700003832;
    public static final int QUESTION_ID = 700001890;
    public static final int SUBMISSION_ID = 700001850;
    private final String unlimitedAnswer;
    private final String questionId;
    private final String submissionId;

    public UnlimitedAnswer(Entry entry) {
        this.unlimitedAnswer = (String) entry.get(UNLIMITED_ANSWER).getValue();
        this.questionId = (String) entry.get(QUESTION_ID).getValue();
        this.submissionId = (String) entry.get(SUBMISSION_ID).getValue();
    }

    public String getUnlimitedAnswer() {
        return unlimitedAnswer;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getSubmissionId() {
        return submissionId;
    }
}