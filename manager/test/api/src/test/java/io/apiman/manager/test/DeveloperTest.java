package io.apiman.manager.test;


import io.apiman.manager.test.junit.ManagerRestTestPlan;
import io.apiman.manager.test.junit.ManagerRestTester;
import org.junit.runner.RunWith;

/**
 * Runs the developers test plan.
 */
@RunWith(ManagerRestTester.class)
@ManagerRestTestPlan("test-plans/developers-testPlan.xml")
public class DeveloperTest {
}
