package com.axonivy.portal.migration.dashboardfilter.converter.v112;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.EnumUtils;

import com.axonivy.portal.bo.jsonversion.AbstractJsonVersion;
import com.axonivy.portal.bo.jsonversion.DashboardFilterJsonVersion;
import com.axonivy.portal.migration.common.IJsonConverter;
import com.axonivy.portal.migration.common.search.JCondition;
import com.axonivy.portal.migration.common.search.JsonDashboardConfigurationSearch;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;

import ch.ivy.addon.portalkit.enums.DashboardStandardCaseColumn;
import ch.ivy.addon.portalkit.enums.DashboardWidgetType;
import ch.ivyteam.ivy.workflow.CaseState;
import ch.ivyteam.ivy.workflow.caze.CaseBusinessState;

public class DashboardCaseWidgetFilterConverter implements IJsonConverter {

  @Override
  public AbstractJsonVersion version() {
    return new DashboardFilterJsonVersion("11.2.0");
  }

  @Override
  public void convert(JsonNode jsonNode) {
      new JsonDashboardConfigurationSearch(jsonNode)
      .type(DashboardWidgetType.CASE.name())
      .findFilterableColumns()
      .ifPresent(columns -> columns.elements().forEachRemaining(col -> {
          if (JCondition.isField(DashboardStandardCaseColumn.STATE.getField()).test(col)) {
            convertCaseBusinessStates(col.get("userFilterList"));
          }
      }));
  }

  /**
   * Adapt case business state for widget filter set
   * IVYPORTAL-14663: Introduce CaseBusinessState
   * 
   */
  private void convertCaseBusinessStates(JsonNode statesNode) {
    List<TextNode> newStates = new ArrayList<>();
    statesNode.elements().forEachRemaining(node -> {
      TextNode newState = convertCaseBusinessState(node.asText());
      if (newState != null) {
        newStates.add(newState);
      }
    });

    ((ArrayNode)statesNode).removeAll().addAll(newStates.stream().distinct().toList());
  }

  /**
   * Adapt case business state for widget filter set
   * IVYPORTAL-14663: Introduce CaseBusinessState
   * 
   */
  private TextNode convertCaseBusinessState(String oldStateString) {
    CaseState oldState = EnumUtils.getEnum(CaseState.class, oldStateString);
    TextNode result = new TextNode(oldStateString);

    if (Objects.nonNull(oldState)) {
      result =  switch(oldState) {
        case RUNNING -> new TextNode(CaseBusinessState.OPEN.name());
        case CREATED -> new TextNode(CaseBusinessState.OPEN.name());
        case DONE -> new TextNode(CaseBusinessState.DONE.name());
        case DESTROYED -> new TextNode(CaseBusinessState.DESTROYED.name());
        default -> null;
      };
    }

    return result;
  }
}
