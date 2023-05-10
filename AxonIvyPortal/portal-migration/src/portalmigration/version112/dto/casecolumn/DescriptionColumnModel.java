package portalmigration.version112.dto.casecolumn;

import java.io.Serializable;
import ch.ivyteam.ivy.workflow.ICase;
import portalmigration.version112.enums.DashboardStandardCaseColumn;

public class DescriptionColumnModel extends CaseColumnModel implements Serializable {

  private static final long serialVersionUID = 1111740263997339274L;

  @Override
  public void initDefaultValue() {
    super.initDefaultValue();
    this.field = DashboardStandardCaseColumn.DESCRIPTION.getField();
    this.style = defaultIfEmpty(this.style, getDefaultStyle());
    this.styleClass = defaultIfEmpty(this.styleClass, getDefaultStyleClass());
    this.sortable = defaultIfEmpty(this.sortable, getDefaultSortable());
  }

  @Override
  public String getDefaultHeaderCMS() {
    return "/ch.ivy.addon.portalkit.ui.jsf/common/description";
  }

  @Override
  public Boolean getDefaultSortable() {
    return false;
  }

  @Override
  public String getDefaultStyle() {
    return "width: 200px";
  }

  @Override
  public String getDefaultStyleClass() {
    return "dashboard-cases__description";
  }

  @Override
  public Object display(ICase caze) {
    if (caze == null) {
      return null;
    }
    return caze.descriptions().current();
  }
}
