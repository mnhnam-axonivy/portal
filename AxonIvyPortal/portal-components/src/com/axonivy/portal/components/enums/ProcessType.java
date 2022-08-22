package com.axonivy.portal.components.enums;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import ch.ivyteam.ivy.environment.Ivy;

public enum ProcessType {
  CASE_MAP("casemap"), IVY_PROCESS("process-start");

  private final String value;

  private ProcessType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public String getLabel() {
    String label = Ivy.cms().co("/Dialogs/com/axonivy/portal/components/ProcessViewer/ProcessType/" + name());
    return StringUtils.isBlank(label) ? name() : label;
  }

  public static ProcessType typeOf(String value) {
    return Arrays.stream(values()).filter(type -> type.getValue().equalsIgnoreCase(value)).findAny().orElse(null);
  }
}
