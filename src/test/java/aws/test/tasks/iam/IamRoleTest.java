package aws.test.tasks.iam;

import org.junit.Test;
import org.junit.runners.Parameterized;
import software.amazon.awssdk.services.iam.model.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//CXQA-IAM-02: 3 IAM roles are created according to the following requirements:
public class IamRoleTest extends AbstractTest {

    @Parameterized.Parameter
    public String roleNameExpected;

    @Parameterized.Parameter(1)
    public String policyNameExpected;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"FullAccessRoleEC2", "FullAccessPolicyEC2"},
                {"FullAccessRoleS3", "FullAccessPolicyS3"},
                {"ReadAccessRoleS3", "ReadAccessPolicyS3"}
        });
    }

    @Test()
    public void testIamRole() {
        //Check if role with roleNameExpected created
        assertTrue(String.format("Role with the name '%s' is not created.", roleNameExpected), ifRoleExist());

        //Get list of attached policies
        List<AttachedPolicy> policies = retrievePoliciesByRoleName();

        //Check Role Policies
        assertEquals(String.format("More than 1 policy attached.\nExpected: %s\nActual: %s", policyNameExpected, policies),
                1, policies.size());
        assertEquals(String.format("Attached Policy contains invalid name.\nExpected: %s\nActual: %s", policyNameExpected, policies),
                policyNameExpected,
                policies.get(0).policyName());
    }

    private boolean ifRoleExist() {
        GetRoleRequest roleRequest = GetRoleRequest.builder()
                .roleName(roleNameExpected)
                .build();
        try {
            return nonNull(iamClient.getRole(roleRequest));
        } catch (IamException ex) {
            System.err.println(ex.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return false;
    }

    private List<AttachedPolicy> retrievePoliciesByRoleName() {
        // Create request to get policies attached to the role
        ListAttachedRolePoliciesRequest listAttachedRolePoliciesRequest =
                ListAttachedRolePoliciesRequest.builder().roleName(roleNameExpected).build();

        // Send the request and get the response
        ListAttachedRolePoliciesResponse rolePoliciesResponse =
                iamClient.listAttachedRolePolicies(listAttachedRolePoliciesRequest);

        // Get list of attached policies
        return rolePoliciesResponse.attachedPolicies();
    }
}
