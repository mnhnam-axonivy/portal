package com.axonivy.portal.util.filter.field.task;

import com.axonivy.portal.dto.dashboard.filter.DashboardFilter;
import com.axonivy.portal.enums.dashboard.filter.FilterFormat;
import com.axonivy.portal.enums.dashboard.filter.FilterOperator;
import com.axonivy.portal.util.filter.field.FilterField;
import com.axonivy.portal.util.filter.operator.task.createddate.CreatedDateAfterOperatorHandler;
import com.axonivy.portal.util.filter.operator.task.createddate.CreatedDateBeforeOperatorHandler;
import com.axonivy.portal.util.filter.operator.task.createddate.CreatedDateBetweenOperatorHandler;
import com.axonivy.portal.util.filter.operator.task.createddate.CreatedDateCurrentPeriodOperatorHandler;
import com.axonivy.portal.util.filter.operator.task.createddate.CreatedDateIsOperatorHandler;
import com.axonivy.portal.util.filter.operator.task.createddate.CreatedDateNumberOfPeriodsOperatorHandler;
import com.axonivy.portal.util.filter.operator.task.createddate.CreatedDateTodayOperatorHandler;
import com.axonivy.portal.util.filter.operator.task.createddate.CreatedDateYesterdayOperatorHandler;

import ch.ivy.addon.portalkit.enums.DashboardColumnType;
import ch.ivy.addon.portalkit.enums.DashboardStandardTaskColumn;
import ch.ivyteam.ivy.workflow.query.CaseQuery;
import ch.ivyteam.ivy.workflow.query.TaskQuery;

public class TaskFilterFieldCreatedDate extends FilterField {

  public TaskFilterFieldCreatedDate() {
    super(DashboardStandardTaskColumn.CREATED.getField());
  }

  public String getLabel() {
    return DashboardStandardTaskColumn.CREATED.getLabel();
  }

  @Override
  public void initFilter(DashboardFilter filter) {
    filter.setFilterField(this);
    filter.setFilterType(DashboardColumnType.STANDARD);
    filter.setFilterFormat(FilterFormat.DATE);
    filter.setField(getName());
  }

  @Override
  public void addNewFilter(DashboardFilter filter) {
    initFilter(filter);
    filter.setOperator(FilterOperator.TODAY);
    filter.setFromDate(null);
    filter.setToDate(null);
  }

  @Override
  public TaskQuery generateFilterTaskQuery(DashboardFilter filter) {
    return switch (filter.getOperator()) {
      case BETWEEN -> CreatedDateBetweenOperatorHandler.getInstance().buildBetweenQuery(filter);
      case NOT_BETWEEN -> CreatedDateBetweenOperatorHandler.getInstance().buildNotBetweenQuery(filter);
      case BEFORE -> CreatedDateBeforeOperatorHandler.getInstance().buildQuery(filter);
      case AFTER -> CreatedDateAfterOperatorHandler.getInstance().buildQuery(filter);
      case TODAY -> CreatedDateTodayOperatorHandler.getInstance().buildQuery();
      case YESTERDAY -> CreatedDateYesterdayOperatorHandler.getInstance().buildQuery();
      case CURRENT -> CreatedDateCurrentPeriodOperatorHandler.getInstance().buildQuery(filter);
      case LAST -> CreatedDateNumberOfPeriodsOperatorHandler.getInstance().buildLastPeriodQuery(filter);
      case NEXT -> CreatedDateNumberOfPeriodsOperatorHandler.getInstance().buildNextPeriodQuery(filter);
      case IS -> CreatedDateIsOperatorHandler.getInstance().buildIsQuery(filter);
      case IS_NOT -> CreatedDateIsOperatorHandler.getInstance().buildIsNotQuery(filter);
      default -> null;
    };
  }

  @Override
  public CaseQuery generateFilterQuery(DashboardFilter filter) {
    return null;
  }
}
