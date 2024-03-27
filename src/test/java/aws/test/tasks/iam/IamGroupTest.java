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

//CXQA-IAM-03: 3 IAM users groups are created according to the following requirements:
public class IamGroupTest extends AbstractTest {
    @Parameterized.Parameter
    public String groupNameExpected;

    @Parameterized.Parameter(1)
    public String policyNameExpected;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"FullAccessGroupEC2", "FullAccessPolicyEC2"},
                {"FullAccessGroupS3", "FullAccessPolicyS3"},
                {"ReadAccessGroupS3", "ReadAccessPolicyS3"}
        });
    }

    @Test()
    public void testIamGroup() {
        //Check if group with groupNameExpected created
        assertTrue(String.format("Group with the name '%s' is not created.", groupNameExpected), ifGroupExist());

        //Get list of attached policies
        List<AttachedPolicy> policies = retrievePoliciesByGroupName();

        //Check Group Policies
        assertEquals(String.format("More than 1 policy attached.\nExpected: %s\nActual: %s", policyNameExpected, policies),
                1, policies.size());
        assertEquals(String.format("Attached Policy contains invalid name.\nExpected: %s\nActual: %s", policyNameExpected, policies),
                policyNameExpected,
                policies.get(0).policyName());
    }

    private boolean ifGroupExist() {
        GetGroupRequest groupRequest = GetGroupRequest.builder()
                .groupName(groupNameExpected)
                .build();
        try {
            return nonNull(iamClient.getGroup(groupRequest));
        } catch (IamException ex) {
            System.err.println(ex.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return false;
    }

    private List<AttachedPolicy> retrievePoliciesByGroupName() {
        // Create request to get policies attached to the group
        ListAttachedGroupPoliciesRequest listAttachedGroupPoliciesRequest =
                ListAttachedGroupPoliciesRequest.builder().groupName(groupNameExpected).build();

        // Send the request and get the response
        ListAttachedGroupPoliciesResponse listAttachedGroupPoliciesResponse =
                iamClient.listAttachedGroupPolicies(listAttachedGroupPoliciesRequest);

        // Get list of attached policies
        return listAttachedGroupPoliciesResponse.attachedPolicies();
    }
}
