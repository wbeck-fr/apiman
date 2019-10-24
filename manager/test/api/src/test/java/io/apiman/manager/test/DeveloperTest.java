package io.apiman.manager.test;


import io.apiman.manager.test.junit.ManagerRestTestGatewayLog;
import io.apiman.manager.test.junit.ManagerRestTestPlan;
import io.apiman.manager.test.junit.ManagerRestTester;
import org.junit.runner.RunWith;

/**
 * Runs the developers test plan.
 */
@RunWith(ManagerRestTester.class)
@ManagerRestTestPlan("test-plans/developers-testPlan.xml")
@ManagerRestTestGatewayLog(
        "GET:/mock-gateway/system/status\n" +
        "PUT:/mock-gateway/apis\n" +
        "GET:/mock-gateway/system/status\n" +
        "PUT:/mock-gateway/clients\n"
)
public class DeveloperTest {
}
