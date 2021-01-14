package ch.ivy.addon.portalkit.publicapi;

import ch.ivy.addon.portal.generic.navigation.PortalNavigator;

/**
 * Portal API for navigation not in iFrame
 *
 */
public final class PortalNavigatorAPI {
  private PortalNavigatorAPI() {}
  
  /**
   * Navigate to Portal home
   */
  public static void navigateToPortalHome() {
    PortalNavigator.navigateToPortalHome();
  }

  /**
   * Navigate to PortalEndPage without finishing a task, e.g. clicking on Cancel button then back to previous page:
   * task list or task details or global search NOTES: is only used for the task not started in Portal IFrame
   */
  public static void navigateToPortalEndPage() {
    PortalNavigator.navigateToPortalEndPage();
  }
}
