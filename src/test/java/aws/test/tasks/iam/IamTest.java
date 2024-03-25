package aws.test.tasks.iam;

import org.junit.Test;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.ListPoliciesResponse;
import software.amazon.awssdk.services.iam.model.Policy;

import java.util.List;

public class IamTest {

    @Test()
    public void testIam() {
        String profile = "058264215258_AdministratorAccess";
        Region region = Region.EU_NORTH_1;
        IamClient iam = IamClient.builder()
                .credentialsProvider(ProfileCredentialsProvider.create(profile))
                .region(region)
                .build();

        listPolicies(iam);
    }

    public static void listPolicies(IamClient iam) {
        ListPoliciesResponse response = iam.listPolicies();
        List<Policy> polList = response.policies();
        for (Policy policy : polList) {
            System.out.println("Policy Name: " + policy.policyName());
        }
    }
}
