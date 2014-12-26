package com.sciul.cloud_configurator.dsl;

import com.sciul.cloud_configurator.Provider;

import javax.json.*;

/**
 * Created by sumeetrohatgi on 12/23/14.
 */
public class VPC extends Resource {
  private final String defaultTenancy = "default";
  private final boolean dnsSupport;
  private final boolean dnsHostname;
  private final String cidrBlock;

  public VPC(String ciderBlock, String region, ResourceList resourceList) {
    this(ciderBlock, region, true, true, resourceList);
  }

  public VPC(String ciderBlock, String region, boolean dnsSupport, boolean dnsHostname,
             ResourceList resourceList) {
    this.cidrBlock = ciderBlock;
    this.dnsSupport = dnsSupport;
    this.dnsHostname = dnsHostname;
    this.resourceList = resourceList;
    setName("VPC");

    resourceList.add(new DHCPOptions("DHCPOptions", region + ".compute.internal", resourceList));
    resourceList.add(new InternetGateway("InternetGateway", resourceList));
    resourceList.add(new NetworkAcl("Acl", getName(), resourceList));
  }

  public VPC subnet(String name, String zone, String ciderBlock) {
    Subnet subnet = new Subnet(name, ciderBlock, zone, this);
    resourceList.add(subnet);
    return this;
  }

  public ResourceList next() {
    return resourceList;
  }

  public String getDefaultTenancy() {
    return defaultTenancy;
  }

  public boolean isDnsSupport() {
    return dnsSupport;
  }

  public boolean isDnsHostname() {
    return dnsHostname;
  }

  public String getCidrBlock() {
    return cidrBlock;
  }

  @Override
  public JsonObject toJson(Provider provider) {
    return provider.createVPC(this);
  }

  @Override
  public VPC tag(String name, String value) {
    tags.put(name, value);
    return this;
  }

}
