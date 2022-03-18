package ch.ivy.addon.portalkit.test.util;

import ch.ivy.addon.portalkit.enums.PortalVariable;
import ch.ivyteam.ivy.environment.Ivy;

public class DashboardCreationUtils {
  private static String PRIVATE_CONFIG =
      "[{\"id\":\"af93bcf30eac4f328ae6ced3aaa5e2f9\",\"title\":\"private 1\",\"permissions\":[\"Everybody\"]},{\"id\":\"1fdb4cbd0d844f9e90273eab22040d71\",\"title\":\"private 2\",\"permissions\":[\"Everybody\"]}]";

  private static String PUBLIC_CONFIG =
      "[{\"id\":\"0b37be74a50b4ce0a8c8e8abbd54e24f\",\"title\":\"public 1\",\"permissions\":[\"Everybody\"]},{\"id\":\"6365e9439db84bd3a8a18f8d47529413\",\"title\":\"public 2\",\"permissions\":[\"Everybody\"]},{\"id\":\"2d0a8d2838ae428c9272ff71dbd93ba1\",\"title\":\"public 3\",\"permissions\":[\"CostObject\"]}]";

  public static void createDashboard() {
    Ivy.var().set(PortalVariable.DASHBOARD.key, PUBLIC_CONFIG);
    Ivy.session().getSessionUser().setProperty(PortalVariable.DASHBOARD.key, PRIVATE_CONFIG);
  }
}
