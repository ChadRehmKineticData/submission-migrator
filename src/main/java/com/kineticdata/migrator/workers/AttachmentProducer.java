package com.kineticdata.migrator.workers;

import com.bmc.arsys.api.*;
import com.google.common.base.Strings;
import com.kineticdata.migrator.App;
import com.kineticdata.migrator.impl.ArsHelper;
import com.kineticdata.migrator.impl.Config;
import com.kineticdata.migrator.impl.Utils;
import com.kineticdata.migrator.models.Answer;
import com.kineticdata.migrator.models.Attachment;
import com.kineticdata.migrator.models.Submission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class AttachmentProducer extends Thread {
    private static final int[] FIELDS = { Attachment.ID, Attachment.ENTRY_ID, Attachment.FILE };
    private static final List<SortInfo> ORDER = Collections.EMPTY_LIST;
    private final ARServerUser user;
    private final int queryLimit;
    private final File outputDir;
    private final BlockingQueue<Submission> inQ;
    private final BlockingQueue<Submission> outQ;

    public AttachmentProducer(Config config, File outputDir, BlockingQueue<Submission> inQ,
                              BlockingQueue<Submission> outQ) {
        this.user = ArsHelper.createUser(config);
        this.queryLimit = config.getReQueryLimit();
        this.outputDir = outputDir;
        this.inQ = inQ;
        this.outQ = outQ;
    }

    @Override
    public void run() {
        try {
            Submission submission;
            while ((submission = inQ.take()) != Submission.POISON)
                outQ.put(process(submission));
            outQ.put(Submission.POISON);
        } catch (InterruptedException e) { System.out.println("Attachment thread interrupted."); }
    }

    public Submission process(Submission submission) {
        Submission result = submission.withAttachments(
                retrieveAttachments(
                        getAttachmentIds(submission)));
        downloadAttachments(result);
        return result;
    }

    public void downloadAttachments(Submission submission)  {
        for (Attachment attachment : submission.getAttachments()) {
            File directory = Utils.createDirectory(outputDir, App.ATTACHMENT_DIR, submission.getId(),
                    attachment.getId());
            try (FileOutputStream out = new FileOutputStream(new File(directory, attachment.getFileName()))) {
                byte[] entryBlob = user.getEntryBlob(Attachment.FORM, attachment.getEntryId(), Attachment.FILE);
                if (entryBlob == null)
                    entryBlob = new byte[0];
                out.write(entryBlob);
            } catch (IOException | ARException e) {
                throw new RuntimeException("Caught exception in Attachment thread.", e);
            }
        }
    }

    public List<String> getAttachmentIds(Submission submission) {
        return submission.getAnswers().stream()
            .map(Answer::getAttachmentId)
            .filter(id -> !Strings.isNullOrEmpty(id))
            .collect(Collectors.toList());
    }

    public List<Attachment> retrieveAttachments(List<String> attachmentIds) {
        QualifierInfo qualifierInfo = ArsHelper.buildQual(Attachment.ID, attachmentIds);
        return ArsHelper.getAllEntries(user, Attachment.FORM, qualifierInfo, queryLimit, ORDER, FIELDS).stream()
                .map(Attachment::new).collect(Collectors.toList());
    }
}
