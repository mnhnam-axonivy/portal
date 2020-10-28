package ch.addon.portal.generic.menu;

import ch.addon.portal.generic.userprofile.homepage.HomepageType;
import ch.ivy.addon.portal.generic.navigation.PortalNavigator;
import ch.ivy.addon.portalkit.enums.MenuKind;
import ch.ivy.addon.portalkit.service.ApplicationMultiLanguage;

public class StatisticSubMenuItem extends SubMenuItem {
  public StatisticSubMenuItem() {
    this.icon = "icon ivyicon-pie-line-graph";
    this.menuKind = MenuKind.STATISTICS;
    this.label = ApplicationMultiLanguage.getCmsValueByUserLocale("/ch.ivy.addon.portalkit.ui.jsf/statistic/chart/statistics");
    this.name = HomepageType.STATISTICS.name();
    this.link = PortalNavigator.getSubMenuItemUrlOfCurrentApplication(MenuKind.STATISTICS);
  }
}