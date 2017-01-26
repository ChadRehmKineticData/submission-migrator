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
    private static final int[] FIELDS = {
        Submission.CLOSED_AT,      Submission.CREATED_AT,     Submission.ID,             Submission.REQUEST_ID,     Submission.REQUEST_STATUS,
        Submission.STATUS,         Submission.SUBMITTED_AT,   Submission.SUBMITTER,      Submission.UPDATED_AT,     Submission.VALIDATION_STATUS,
        Submission.ATTRIBUTES[0],  Submission.ATTRIBUTES[1],  Submission.ATTRIBUTES[2],  Submission.ATTRIBUTES[3],  Submission.ATTRIBUTES[4],
        Submission.ATTRIBUTES[5],  Submission.ATTRIBUTES[6],  Submission.ATTRIBUTES[7],  Submission.ATTRIBUTES[8],  Submission.ATTRIBUTES[9],
        Submission.ATTRIBUTES[10], Submission.ATTRIBUTES[11], Submission.ATTRIBUTES[12], Submission.ATTRIBUTES[13], Submission.ATTRIBUTES[14],
        Submission.ATTRIBUTES[15], Submission.ATTRIBUTES[16], Submission.ATTRIBUTES[17], Submission.ATTRIBUTES[18], Submission.ATTRIBUTES[19],
        Submission.ATTRIBUTES[20], Submission.ATTRIBUTES[21], Submission.ATTRIBUTES[22], Submission.ATTRIBUTES[23], Submission.ATTRIBUTES[24],
        Submission.ATTRIBUTES[25], Submission.ATTRIBUTES[26], Submission.ATTRIBUTES[27], Submission.ATTRIBUTES[28], Submission.ATTRIBUTES[29],
        Submission.ATTRIBUTES[30], Submission.ATTRIBUTES[31], Submission.ATTRIBUTES[32], Submission.ATTRIBUTES[33], Submission.ATTRIBUTES[34],
        Submission.ATTRIBUTES[35], Submission.ATTRIBUTES[36], Submission.ATTRIBUTES[37], Submission.ATTRIBUTES[38], Submission.ATTRIBUTES[39],
        Submission.ATTRIBUTES[40], Submission.ATTRIBUTES[41], Submission.ATTRIBUTES[42], Submission.ATTRIBUTES[43], Submission.ATTRIBUTES[44],
        Submission.ATTRIBUTES[45], Submission.ATTRIBUTES[46], Submission.ATTRIBUTES[47], Submission.ATTRIBUTES[48], Submission.ATTRIBUTES[49],
        Submission.ATTRIBUTES[50], Submission.ATTRIBUTES[51], Submission.ATTRIBUTES[52], Submission.ATTRIBUTES[53], Submission.ATTRIBUTES[54],
        Submission.ATTRIBUTES[55], Submission.ATTRIBUTES[56], Submission.ATTRIBUTES[57], Submission.ATTRIBUTES[58], Submission.ATTRIBUTES[59],
        Submission.ATTRIBUTES[60], Submission.ATTRIBUTES[61], Submission.ATTRIBUTES[62], Submission.ATTRIBUTES[63], Submission.ATTRIBUTES[64],
        Submission.ATTRIBUTES[65], Submission.ATTRIBUTES[66], Submission.ATTRIBUTES[67], Submission.ATTRIBUTES[68], Submission.ATTRIBUTES[69]
    };
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
