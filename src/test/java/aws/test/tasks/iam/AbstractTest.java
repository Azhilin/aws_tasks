package aws.test.tasks.iam;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;

@RunWith(Parameterized.class)
public abstract class AbstractTest {
    protected static final Region REGION = Region.EU_NORTH_1;
    protected static IamClient iamClient;

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
}
