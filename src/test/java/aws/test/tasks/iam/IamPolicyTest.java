package aws.test.tasks.iam;

import com.google.gson.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.*;
import software.amazon.awssdk.utils.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.junit.Assert.*;

//CXQA-IAM-01: 3 IAM policies are created according to the following requirements:
@RunWith(Parameterized.class)
public class IamPolicyTest {
    private static final String[] INVALID_SYMBOLS = {"\"", "[", "]", " "};
    private static final Region REGION = Region.EU_NORTH_1;
    private static IamClient iam;

    @Parameterized.Parameter
    public String policyNameExpected;

    @Parameterized.Parameter(1)
    public String actionsExpected;

    @Parameterized.Parameter(2)
    public String resourcesExpected;

    @Parameterized.Parameter(3)
    public String effectExpected;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"FullAccessPolicyEC2", "ec2:*", "*", "Allow"},
                {"FullAccessPolicyS3", "s3:*", "*", "Allow"},
                {"ReadAccessPolicyS3", "s3:Describe*,s3:Get*,s3:List*", "*", "Allow"}
        });
    }

    @BeforeClass
    public static void init() {
        String awsProfile = System.getProperty("awsProfile");
        iam = IamClient
                .builder()
                .credentialsProvider(ProfileCredentialsProvider.create(awsProfile))
                .region(REGION)
                .build();
    }

    @AfterClass
    public static void close() {
        iam.close();
    }

    @Test()
    public void testIamPolicy() {
        List<Policy> policies = iam.listPolicies().policies();

        Optional<Policy> optionalPolicyMatching = policies
                .stream()
                .filter((pActual) -> StringUtils.equals(policyNameExpected, pActual.policyName()))
                .findAny();

        //check if policy with 'policyName' is presented in the AWS policy list
        assertTrue("Text", optionalPolicyMatching.isPresent());

        JsonObject policyJson = retrievePolicyJson(optionalPolicyMatching.get().arn());

        //check actionsAllowed
        if (isArray(actionsExpected)) {
            String[] actionsArrayActual = cleanUpInvalidSymbols(valueOf(policyJson.get("Action"))).split(",");
            String[] actionsArrayExpected = cleanUpInvalidSymbols(actionsExpected).split(",");
            assertArraysEquals(actionsArrayActual, actionsArrayExpected);
        } else {
            String actionsActual = valueOf(policyJson.get("Action"));
            assertStringEquals(actionsActual, actionsExpected);
        }

        //check resources
        String resourcesActual = valueOf(policyJson.get("Resource"));
        assertStringEquals(resourcesActual, resourcesExpected);

        //check effect
        String effectActual = valueOf(policyJson.get("Effect"));
        assertStringEquals(effectActual, effectExpected);
    }

    private void assertStringEquals(String actual, String expected) {
        String actualResult = cleanUpInvalidSymbols(actual);
        assertEquals(
                format("Assertion Failed \nActual: %s \nExpected: %s", actualResult, expected)
                , actualResult
                , expected
        );
    }

    private void assertArraysEquals(String[] actual, String[] expected) {
        assertArrayEquals(
                "Assertion Failed \nActual: "
                        + Arrays.toString(actual)
                        + " \nExpected: "
                        + Arrays.toString(expected)
                , actual
                , expected
        );
    }

    private boolean isArray(String str) {
        return str.contains(",");
    }

    private String cleanUpInvalidSymbols(String str) {
        for (String s : INVALID_SYMBOLS) {
            str = str.replace(s, "");
        }
        return str.trim();
    }

    private JsonObject retrievePolicyJson(String policyArn) {
        GetPolicyRequest request = GetPolicyRequest.builder().policyArn(policyArn).build();
        GetPolicyResponse response = iam.getPolicy(request);

        GetPolicyVersionRequest policyVersionRequest = GetPolicyVersionRequest
                .builder().policyArn(policyArn).versionId(response.policy().defaultVersionId()).build();
        GetPolicyVersionResponse policyVersionResponse = iam.getPolicyVersion(policyVersionRequest);

        // Parse policy JSON using Gson
        JsonElement jsonElement = JsonParser.parseString(policyVersionResponse.policyVersion().document());
        String encodedJson = jsonElement.getAsString();

        //Decode URL-encoded string
        String decodedJson = URLDecoder.decode(encodedJson, StandardCharsets.UTF_8);

        // Parse the JSON string to a JsonObject
        JsonObject jsonObject = new Gson().fromJson(decodedJson, JsonObject.class);
        JsonArray jsonArray = jsonObject.getAsJsonArray("Statement");

        return jsonArray.get(0).getAsJsonObject();
    }
}
