package com.sciul.cloud_configurator;

import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.sciul.cloud_application.dsl.Application;
import com.sciul.cloud_application.models.CloudBlueprint;
import com.sciul.cloud_configurator.dsl.Provider;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

import java.io.*;
import java.util.HashMap;

@Configuration
@ComponentScan
@PropertySource("classpath:application.properties")
public class CmdlineApp {

  private static Logger logger = LoggerFactory.getLogger(CmdlineApp.class);

  private CommandLineParser parser = new BasicParser();

  @Autowired
  Provider provider;

  public static void main(String[] args) {
    try {

      ApplicationContext context = new AnnotationConfigApplicationContext(CmdlineApp.class);
      CmdlineApp obj = (CmdlineApp) context.getBean("configure");

      obj.configure(args);

    } catch (Exception e) {
      logger.error("Error running Configurator App", e);
      System.exit(1);
    }
  }

  @Bean(name = "configure")
  public CmdlineApp myApp() {
    return new CmdlineApp();
  }

  public void configure(String... args) {
    Options options = new Options() {
      {
        addOption("c", "command", true, "command to run, example: update, list");
        addOption("r", "region", true, "aws region");
        addOption("a", "api domain", true, "api hosted on this domain");
        addOption("w", "web domain", true, "webapp hosted on this domain");
        addOption("p", "prefix", true, "environment prefix");
        addOption("f", "file", true, "file to read input/ write output");
        addOption("h", "help", false, "this help message");
      }
    };

    try {

      final CommandLine cmd = parser.parse(options, args);

      if (cmd.hasOption("h")) {
        writeHelp(options);
        return;
      }

      if (!cmd.hasOption("c")) {
        throw new MissingArgumentException("no command specified");
      }

      if (!cmd.hasOption("r")) {
        throw new MissingArgumentException("no region specified");
      }

      String region = cmd.getOptionValue("r");
      provider.setRegion(region);

      switch (cmd.getOptionValue("c")) {
        case "generate":
          generateApplicationConfiguration(cmd, region);
          break;

        case "update":
          updateApplication(cmd, region);
          break;

        case "list":
          listExistingApplications(cmd, region);
          break;

        default:
          logger.warn("no commands specified!");
          writeHelp(options);
          break;
      }

    } catch (ParseException e) {
      writeHelp(options);
      throw new RuntimeException("Unable to parse command line options", e);
    }
  }

  private void writeHelp(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(80, "run.sh -c <update|list|generate> -r <region> [-p <prefix> -w <web domain> " +
            "-a <api domain>] [-f <filename>] [-h]",
        "Configure your cloud environment", options, "");
  }

  private CloudBlueprint buildTemplate(CommandLine cmd, String region) throws MissingArgumentException {
    if (!cmd.hasOption("p")) {
      throw new MissingArgumentException("no prefix specified");
    }

    if (!cmd.hasOption("a")) {
      throw new MissingArgumentException("no api domain specified");
    }

    if (!cmd.hasOption("w")) {
      throw new MissingArgumentException("no web domain specified");
    }

    String prefix = cmd.getOptionValue("p");
    String webDomain = cmd.getOptionValue("w");
    String apiDomain = cmd.getOptionValue("a");

    //Template template = new Template(prefix, region, apiDomain, webDomain);
    CloudBlueprint cloudBlueprint = new CloudBlueprint();
    cloudBlueprint.setName(prefix);
    cloudBlueprint.setRegion(region);
    cloudBlueprint.setWebDomain(webDomain);
    cloudBlueprint.setWebKey("");
    cloudBlueprint.setApiDomain(apiDomain);
    cloudBlueprint.setWebKey("");
    cloudBlueprint.setServices(new HashMap<String, Integer[]>() {{
      put("C*", new Integer[] { 9042 });
    }});

    return cloudBlueprint;
  }

  private void updateApplication(CommandLine cmd, String region) throws MissingArgumentException {
    CloudBlueprint cloudBlueprint = buildTemplate(cmd, region);

    provider.createStack(Application.buildResourceList(cloudBlueprint));

    logger.debug("****************Stack Creation Started****************");
  }

  private void listExistingApplications(CommandLine cmd, String region) {
    logger.debug("list environments for region: {}", region);

    DescribeStacksResult dst = null;
    if (cmd.hasOption("e")) {
      dst = provider.describeStacks(cmd.getOptionValue("e"));
    } else {
      dst = provider.describeStacks(null);
    }

    logger.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    logger.info("active applications: {}", dst);
    logger.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
  }

  private void generateApplicationConfiguration(CommandLine cmd, String region) throws MissingArgumentException {
    CloudBlueprint cloudBlueprint = buildTemplate(cmd, region);
    String templateBody = provider.generateStackTemplate(Application.buildResourceList(cloudBlueprint));

    if (!cmd.hasOption("f")) {
      logger.info("generated template: {}", templateBody);
      return;
    }

    Writer writer = null;

    try {
      writer = new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(cmd.getOptionValue("f")), "utf-8"));
      writer.write(templateBody);
    } catch (IOException ex) {
      throw new RuntimeException("unable to write to file", ex);
    } finally {
      try {
        writer.close();
        logger.info("wrote template to file: {}", cmd.getOptionValue("f"));
      } catch (Exception ex) {
        throw new RuntimeException("unable to write to file", ex);
      }
    }
  }

}
