package com.kineticdata.migrator.workers;

import com.bmc.arsys.api.ARServerUser;
import com.bmc.arsys.api.QualifierInfo;
import com.bmc.arsys.api.SortInfo;
import com.kineticdata.migrator.impl.*;
import com.kineticdata.migrator.models.Submission;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class SubmissionProducer extends Thread {
    private static final int[] FIELDS = { Submission.CLOSED_AT, Submission.CREATED_AT, Submission.ID,
            Submission.REQUEST_ID, Submission.REQUEST_STATUS, Submission.STATUS,
            Submission.SUBMITTED_AT, Submission.SUBMITTER, Submission.UPDATED_AT,
            Submission.VALIDATION_STATUS};
    private static final List<SortInfo> ORDER = Collections.EMPTY_LIST;
    private final ARServerUser user;
    private final QualifierInfo qualification;
    private final int queryLimit;
    private final BlockingQueue<Submission> outQ;

    public SubmissionProducer(Config config, String qualification, BlockingQueue<Submission> outQ) {
        this.user = ArsHelper.createUser(config);
        this.qualification = ArsHelper.parseQual(user, Submission.FORM, qualification);
        this.queryLimit = config.getReQueryLimit();
        this.outQ = outQ;
    }

    public void run() {
        try {
            int count = 0;
            while (true) {
                // get a chunk of submissions
                List<Submission> submissions = getSubmissions(count);
                // add all of the retrieved submissions to the output queue
                for (Submission submission : submissions)
                    outQ.put(submission);
                // if the result was empty we break the while loop, otherwise we increment the count
                // by the size for the next iteration
                if (submissions.isEmpty()) break;
                else count += submissions.size();
            }
            // signal the end of the submissions to the other end of the queue
            outQ.put(Submission.POISON);
        } catch (InterruptedException e) {
            System.out.println("Submission thread was interrupted.");
        }
    }

    public List<Submission> getSubmissions(int offset) {
        return ArsHelper.getEntries(user, Submission.FORM, qualification, offset, queryLimit, ORDER, FIELDS)
                .stream().map(Submission::new).collect(Collectors.toList());
    }
}
