package aws.test.tasks.iam;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//CXQA-IAM-03: 3 IAM users groups are created according to the following requirements:
@RunWith(Parameterized.class)
public class IamGroupTest {

    private static final Region REGION = Region.EU_NORTH_1;
    private static IamClient iamClient;

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

    @BeforeClass
    public static void initParams() {
        String awsProfile = System.getProperty("awsProfile");
        iamClient = IamClient
                .builder()
                .credentialsProvider(ProfileCredentialsProvider.create(awsProfile))
                .region(REGION)
                .build();
    }

    @AfterClass
    public static void close() {
        iamClient.close();
    }

    @Test()
    public void testIamGroup() {
        //Check if role with roleNameExpected created
        assertTrue(String.format("Role with the name '%s' is not created.", groupNameExpected), ifGroupExist());

        //Get list of attached policies
        List<AttachedPolicy> policies = retrievePoliciesByGroupName();

        //Check Role Policies
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

        boolean ifExist = false;

        try {
            ifExist = nonNull(iamClient.getGroup(groupRequest));
        } catch (Exception ex) {

        }

        return ifExist;
    }

    private List<AttachedPolicy> retrievePoliciesByGroupName() {
        // Create request to get policies attached to the role
        ListAttachedGroupPoliciesRequest listAttachedGroupPoliciesRequest =
                ListAttachedGroupPoliciesRequest.builder().groupName(groupNameExpected).build();

        // Send the request and get the response
        ListAttachedGroupPoliciesResponse listAttachedGroupPoliciesResponse =
                iamClient.listAttachedGroupPolicies(listAttachedGroupPoliciesRequest);

        // Get list of attached policies
        return listAttachedGroupPoliciesResponse.attachedPolicies();
    }
}
