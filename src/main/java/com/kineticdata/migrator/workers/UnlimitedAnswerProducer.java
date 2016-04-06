package com.kineticdata.migrator.workers;

import com.bmc.arsys.api.ARServerUser;
import com.bmc.arsys.api.Entry;
import com.bmc.arsys.api.QualifierInfo;
import com.bmc.arsys.api.SortInfo;
import com.kineticdata.migrator.impl.ArsHelper;
import com.kineticdata.migrator.impl.Config;
import com.kineticdata.migrator.models.Submission;
import com.kineticdata.migrator.models.UnlimitedAnswer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class UnlimitedAnswerProducer extends Thread {
    private static final int[] FIELDS = { UnlimitedAnswer.QUESTION_ID, UnlimitedAnswer.SUBMISSION_ID,
            UnlimitedAnswer.UNLIMITED_ANSWER };
    private static final List<SortInfo> ORDER = Collections.EMPTY_LIST;
    private final ARServerUser user;
    private final int queryLimit;
    private final int submissionChunkSize;
    private final BlockingQueue<Submission> inQ;
    private final BlockingQueue<Submission> outQ;

    public UnlimitedAnswerProducer(Config config, int submissionChunkSize,
                                   BlockingQueue<Submission> inQ, BlockingQueue<Submission> outQ) {
        this.user = ArsHelper.createUser(config);
        this.queryLimit = config.getReQueryLimit();
        this.submissionChunkSize = submissionChunkSize;
        this.inQ = inQ;
        this.outQ = outQ;
    }

    @Override
    public void run() {
        try {
            boolean done = false;
            while (!done) {
                List<Submission> submissionsChunk = new ArrayList<>(submissionChunkSize);
                while (!done && submissionsChunk.size() < submissionChunkSize) {
                    Submission submission = inQ.take();
                    if (submission == Submission.POISON) done = true;
                    else submissionsChunk.add(submission);
                }
                for (Submission submission : processSubmissions(submissionsChunk))
                    outQ.put(submission);
            }
            outQ.put(Submission.POISON);
        } catch (InterruptedException e) { System.out.println("Unlimited answer thread was interrupted."); }
    }

    public List<Submission> processSubmissions(List<Submission> submissions) {
        QualifierInfo qualInfo = ArsHelper.buildQual(UnlimitedAnswer.SUBMISSION_ID,
                submissions.stream().map(Submission::getId).collect(Collectors.toList()));
        List<Entry> entries = ArsHelper.getAllEntries(user, UnlimitedAnswer.FORM, qualInfo, queryLimit, ORDER, FIELDS);
        List<UnlimitedAnswer> answers = entries.stream().map(UnlimitedAnswer::new).collect(Collectors.toList());
        // create a map that maps the submission ids to the list of answers for that submission,
        // this helps us return the lists of answers in the same order as the submission ids we
        // were given
        Map<String,List<UnlimitedAnswer>> answersMap = submissions.stream().collect(
                Collectors.toMap(Submission::getId, x -> new ArrayList<>()));
        answers.forEach(answer -> answersMap.get(answer.getSubmissionId()).add(answer));
        // return a mapping of the submissions that adds the unlimited answer lists to them
        return submissions.stream()
                .map(submission -> submission.withUnlimitedAnswers(answersMap.get(submission.getId())))
                .collect(Collectors.toList());
    }
}
