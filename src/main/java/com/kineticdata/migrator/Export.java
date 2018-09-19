package com.kineticdata.migrator;

import com.bmc.arsys.api.ARServerUser;
import com.bmc.arsys.api.Entry;
import com.bmc.arsys.api.SortInfo;
import com.bmc.thirdparty.org.apache.commons.lang.StringUtils;
import com.kineticdata.migrator.impl.ArsHelper;
import com.kineticdata.migrator.impl.Config;
import com.kineticdata.migrator.impl.Utils;
import com.kineticdata.migrator.models.Catalog;
import com.kineticdata.migrator.models.Question;
import com.kineticdata.migrator.models.Submission;
import com.kineticdata.migrator.models.Template;
import org.yaml.snakeyaml.Yaml;
import com.kineticdata.migrator.workers.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class Export {
	// constants used for ars queries made in this class
	private static final int[] CATALOG_FIELDS = new int[] { Catalog.NAME };
	private static final int[] TEMPLATE_FIELDS = new int[] { Template.ID, Template.NAME, Template.CATALOG };
	private static final int[] QUESTION_FIELDS = new int[] { Question.ID, Question.NAME, Question.TYPE,
			Question.LIST_TYPE };
	private static final List<SortInfo> CATALOG_ORDER = Collections.singletonList(new SortInfo(Catalog.NAME, 1));
	private static final List<SortInfo> TEMPLATE_ORDER = Collections.singletonList(new SortInfo(Template.NAME, 1));
	private static final List<SortInfo> QUESTION_ORDER = Collections.singletonList(new SortInfo(Question.ORDER, 1));
	private static final String CATALOG_QUAL = "1=1";
	private static final Function<String, String> TEMPLATE_QUAL = (catalogName) -> format("'%s'=\"%s\"",
			Template.CATALOG, catalogName);
	private static final BiFunction<String, String, String> TEMPLATE_NAME_QUAL = (catalog,
			template) -> format("'%s'=\"%s\" AND '%s'=\"%s\"", Template.CATALOG, catalog, Template.NAME, template);
	private static final Function<String, String> QUESTION_QUAL = (templateId) -> format("'%s'=\"%s\" AND '%s'='%s'",
			Question.TEMPLATE_ID, templateId, Question.ID, Question.PARENT_QUESTION_ID);
	private static final BiFunction<String, Config, String> SUBMISSION_QUAL = (templateId, config) -> {
		String qualification;
		if (config.getQualification() == "null") {
			qualification = format("'%s'=\"%s\" AND %s", Submission.TEMPLATE_ID, templateId, config.getQualification());
		} else {
			qualification = format("'%s'=\"%s\"", Submission.TEMPLATE_ID, templateId);
		}
		System.out.println(format("Qualification: %s", qualification));
		return qualification;
	};
	// configuration static variables that may eventually be arguments/config
	// options
	private static final int SUBMISSION_CHUNK_SIZE = 100;
	private static final int QUEUE_MULTIPLIER = 2;
	private static final String INVALID_CHARS = "[^a-zA-Z0-9- ]";

	public static void export(Config config, ARServerUser user) {
		for (String catalogName : getCatalogNames(user, config.getReQueryLimit()))
			export(config, user, catalogName);
	}

	public static void export(Config config, ARServerUser user, String catalogName) {
		for (Template template : getTemplates(user, config.getReQueryLimit(), catalogName))
			export(config, user, template);
	}

	public static void export(Config config, ARServerUser user, String catalogName, String templateName) {
		Template template = getTemplate(user, catalogName, templateName);
		export(config, user, template);
	}

	public static void export(Config config, ARServerUser user, Template template) {
		List<Question> questions = getQuestions(user, config.getReQueryLimit(), template.getId());
		// sanitize the template name and setup the output directory for the current
		// template
		String templateDirName = template.getName().replaceAll(INVALID_CHARS, StringUtils.EMPTY);
		File templateDir = Utils.createDirectory("data", template.getCatalog(), templateDirName);
		try {
			writeTemplateFile(templateDir, template, questions);
			// create the queues of submissions, at each step the submissions get decorated
			// with
			// more information by the workers
			int queueSize = config.getReQueryLimit() * QUEUE_MULTIPLIER;
			BlockingQueue<Submission> submissions = new ArrayBlockingQueue<>(queueSize),
					withAnswers = new ArrayBlockingQueue<>(queueSize),
					// withUnlimitedAnswers = new ArrayBlockingQueue<>(queueSize),
					withAttachments = new ArrayBlockingQueue<>(queueSize);
			// create the workers
			List<Thread> threads = Arrays.asList(
					new SubmissionProducer(config, SUBMISSION_QUAL.apply(template.getId(), config), submissions),
					new AnswerProducer(config, SUBMISSION_CHUNK_SIZE, submissions, withAnswers),
					// new UnlimitedAnswerProducer(config, SUBMISSION_CHUNK_SIZE, withAnswers,
					// withUnlimitedAnswers),
					new AttachmentProducer(config, templateDir, withAnswers, withAttachments),
					new SubmissionPrinter(templateDir, questions, withAttachments));
			// start the threads and block until they are complete
			for (Thread thread : threads)
				thread.start();
			for (Thread thread : threads)
				thread.join();
		} catch (Exception e) {
			System.out.println(format("Error processing %s - %s", template.getCatalog(), template.getName()));
			try (FileWriter fileWriter = new FileWriter(new File(templateDir, "error.txt"))) {
				fileWriter.write(e.getMessage());
				for (StackTraceElement ste : e.getStackTrace())
					fileWriter.write(ste.toString());
			} catch (IOException e2) {
				System.out.println(format("Error handling error %s - %s", template.getCatalog(), template.getName()));
				System.out.println(e2.getMessage());
				e2.printStackTrace(System.out);
			}
		}
	}

	public static List<String> getCatalogNames(ARServerUser user, int queryLimit) {
		return ArsHelper.getAllEntries(user, Catalog.FORM, CATALOG_QUAL, queryLimit, CATALOG_ORDER, CATALOG_FIELDS)
				.stream().map(Catalog::new).map(Catalog::getName).collect(Collectors.toList());
	}

	public static List<Template> getTemplates(ARServerUser user, int queryLimit, String catalogName) {
		return ArsHelper.getAllEntries(user, Template.FORM, TEMPLATE_QUAL.apply(catalogName), queryLimit,
				TEMPLATE_ORDER, TEMPLATE_FIELDS).stream().map(Template::new).collect(Collectors.toList());
	}

	public static Template getTemplate(ARServerUser user, String catalogName, String templateName) {
		Entry entry = ArsHelper.getOneEntry(user, Template.FORM, TEMPLATE_NAME_QUAL.apply(catalogName, templateName),
				TEMPLATE_FIELDS);
		return entry == null ? null : new Template(entry);
	}

	public static List<Question> getQuestions(ARServerUser user, int queryLimit, String templateId) {
		return ArsHelper.getAllEntries(user, Question.FORM, QUESTION_QUAL.apply(templateId), queryLimit, QUESTION_ORDER,
				QUESTION_FIELDS).stream().map(Question::new).collect(Collectors.toList());
	}

	public static void writeTemplateFile(File templateDir, Template template, List<Question> questions) {
		Map<String, Object> templateData = new LinkedHashMap<String, Object>() {
			{
				put("template_name", template.getName());
				put("template_id", template.getId());
				put("questions", questions.stream().map(question -> {
					return new LinkedHashMap<String, Object>() {
						{
							put("name", question.getName());
							put("type", question.getType());
						}
					};
				}).collect(Collectors.toList()));
			}
		};
		String yamlString = new Yaml().dumpAsMap(templateData);
		File templateFile = new File(templateDir, App.TEMPLATE_YAML_FILE);
		try (FileWriter fileWriter = new FileWriter(templateFile)) {
			fileWriter.write(yamlString);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
