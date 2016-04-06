package com.kineticdata.migrator.workers;

import com.kineticdata.migrator.models.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class SubmissionPrinter extends Thread {
    private static final String SUBMISSIONS_FILE = "submissions.csv";
    private final File outputDir;
    private final List<Question> questions;
    private final BlockingQueue<Submission> inQ;

    public SubmissionPrinter(File outputDir, List<Question> questions, BlockingQueue<Submission> inQ)  {
        this.outputDir = outputDir;
        this.questions = questions;
        this.inQ = inQ;
    }

    @Override
    public void run() {
        File submissionsFile = new File(outputDir, SUBMISSIONS_FILE);
        try(CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(submissionsFile), CSVFormat.DEFAULT)) {
            printHeader(csvPrinter, questions);
            Submission submission;
            while ((submission = inQ.take()) != Submission.POISON)
                printSubmission(csvPrinter, submission);
        } catch (IOException e) {
            throw new RuntimeException("Caught exception in CSV Printer thread.",e );
        } catch (InterruptedException e ){
            System.out.println("CSV Printer thread interrupted.");
        }
    }

    public void printHeader(CSVPrinter csvPrinter, List<Question> questions) throws IOException {
        List<String> header = new ArrayList<>(Arrays.asList("Request Id", "Instance Id", "Status",
                "Request Status", "Validation Status", "Submitter", "Created At", "Submitted At",
                "Closed At", "Updated At"));
        header.addAll(questions.stream().map(Question::getName).collect(Collectors.toList()));
        csvPrinter.printRecord(header);
    }

    public void printSubmission(CSVPrinter csvPrinter, Submission submission) throws IOException {
        // initialize the row, setting some of the submission values
        List<String> row = new ArrayList<>(Arrays.asList(submission.getRequestId(), submission.getId(),
                submission.getStatus(), submission.getRequestStatus(), submission.getValidationStatus(),
                submission.getSubmitter(), submission.getCreatedAt(), submission.getSubmittedAt(),
                submission.getClosedAt(), submission.getUpdatedAt()));
        // for each of the questions we add the corresponding answer (or null) to the row
        row.addAll(questions.stream().map(Question::getId).map(questionId ->
            submission.getUnlimitedAnswers().stream()
                    .filter(unlimitedAnswer -> unlimitedAnswer.getQuestionId().equals(questionId))
                    .findFirst()
                    .map(UnlimitedAnswer::getUnlimitedAnswer)
                    .orElse(submission.getAnswers().stream()
                            .filter(answer -> answer.getQuestionId().equals(questionId))
                            .findFirst()
                            .map(Answer::getDisplayedAnswer)
                            .orElse(null))
        ).collect(Collectors.toList()));
        // print the current row to the csv
        csvPrinter.printRecord(row);
    }
}
