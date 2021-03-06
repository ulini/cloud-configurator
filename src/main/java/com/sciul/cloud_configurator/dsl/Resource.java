package com.sciul.cloud_configurator.dsl;

import javax.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Resources required by a typical cloud.
 *
 * Purposefully shying away from using a parent-child relationship.
 *
 * Created by sumeetrohatgi on 12/23/14.
 */
public abstract class Resource {
  protected Map<String, String> tags = new HashMap<>();
  protected ResourceList resourceList;
  protected String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    name = name.replace("-","");
    this.name = resourceList.getName() + name;
    tags.put("Name", this.name);
  }

  /**
   * visitor design pattern method, allows a provider
   * to customize data readout
   *
   * @param provider cloud provider like AWS, Azure, GCE, ...
   * @return <code>JsonObject</code>
   */
  public abstract JsonObject toJson(Provider provider);

  /**
   * for grouping resources of the same type
   * @param name tag name
   * @param value any
   * @return <code>this</code>
   */
  public abstract Resource tag(String name, String value);

  /**
   * read all tags on a given resource;
   * inherits tags from <code>ResourceList</code>
   * @return
   */
  public Set<Map.Entry<String, String>> tags() {
    Map<String, String> myTags = resourceList.getTags();
    myTags.putAll(tags);
    return myTags.entrySet();
  }
}
