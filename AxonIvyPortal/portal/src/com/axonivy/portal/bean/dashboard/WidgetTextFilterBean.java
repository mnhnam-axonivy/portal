package com.axonivy.portal.bean.dashboard;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.axonivy.portal.dto.dashboard.filter.DashboardFilter;
import com.axonivy.portal.enums.dashboard.filter.FilterOperator;

@ManagedBean
@ViewScoped
public class WidgetTextFilterBean implements Serializable {

  private static final long serialVersionUID = 3218284115015773931L;

  private static List<FilterOperator> operators = FilterOperator.TEXT_OPERATORS.stream().toList();

  public List<FilterOperator> getOperators() {
    return operators;
  }

  public void onChangeOperator(DashboardFilter filter) {
  }

  public boolean isShowTextListPanel(DashboardFilter filter) {
    if (!filter.isTextField()) {
      return false;
    }

    for (FilterOperator operator : operators) {
      if (operator == FilterOperator.EMPTY || operator == FilterOperator.NOT_EMPTY) {
        continue;
      }
      if (operator == filter.getOperator()) {
        return true;
      }
    }

    return false;
  }

}
