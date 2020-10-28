package ch.addon.portal.generic.menu;

import ch.addon.portal.generic.userprofile.homepage.HomepageType;
import ch.ivy.addon.portal.generic.navigation.PortalNavigator;
import ch.ivy.addon.portalkit.enums.MenuKind;
import ch.ivy.addon.portalkit.service.ApplicationMultiLanguage;

public class CaseSubMenuItem extends SubMenuItem {
  public CaseSubMenuItem() {
    this.icon = "icon ivyicon-layout-bullets";
    this.menuKind = MenuKind.CASE;
    this.label = ApplicationMultiLanguage.getCmsValueByUserLocale("/ch.ivy.addon.portalkit.ui.jsf/caseList/cases");
    this.name = HomepageType.CASE.name();
    this.link = PortalNavigator.getSubMenuItemUrlOfCurrentApplication(MenuKind.CASE);
  }
}