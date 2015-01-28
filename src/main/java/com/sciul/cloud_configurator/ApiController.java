package com.sciul.cloud_configurator;

import com.sciul.cloud_application.models.Cloud;
import com.sciul.cloud_application.models.CloudBlueprint;
import com.sciul.cloud_configurator.services.ApplicationService;
import com.sciul.cloud_configurator.services.ProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * Created by sumeetrohatgi on 1/22/15.
 */
@RestController
public class ApiController {

  private static Logger logger = LoggerFactory.getLogger(ApiController.class);

  @Autowired
  private ApplicationService applicationService;

  @Autowired
  private ProviderService providerService;

  @RequestMapping(value = "/template", method = RequestMethod.PUT)
  String generateTemplate(@RequestBody CloudBlueprint cloudBlueprint) {
    return providerService.template(applicationService.build(cloudBlueprint));
  }

  @RequestMapping(value = "/cloud", method = RequestMethod.POST)
  Cloud buildCloud(@RequestBody CloudBlueprint cloudBlueprint) {
    return providerService.build(applicationService.build(cloudBlueprint));
  }

  @RequestMapping(value = "/", method = RequestMethod.GET)
  String hello() {
    return "Greetings from Spring Boot!";
  }

}