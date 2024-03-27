package aws.test.tasks.iam;

import org.junit.Test;
import org.junit.runners.Parameterized;
import software.amazon.awssdk.services.iam.model.GetUserRequest;
import software.amazon.awssdk.services.iam.model.Group;
import software.amazon.awssdk.services.iam.model.IamException;
import software.amazon.awssdk.services.iam.model.ListGroupsForUserRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//CXQA-IAM-04: 3 IAM users are created according to the following requirements:
public class IamUserTest extends AbstractTest {
    @Parameterized.Parameter
    public String userNameExpected;

    @Parameterized.Parameter(1)
    public String groupNameExpected;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"FullAccessUserEC2", "FullAccessGroupEC2"},
                {"FullAccessUserS3", "FullAccessGroupS3"},
                {"ReadAccessUserS3", "ReadAccessGroupS3"}
        });
    }

    @Test()
    public void testIamUser() {
        //Check if user with userNameExpected created
        assertTrue(String.format("User with the name '%s' is not created.", userNameExpected), ifUserExist());

        //Get Groups attached to the AWS User
        List<Group> groups = getGroupsForUser();

        //Check Groups attached
        assertEquals(String.format("More than 1 policy attached.\nExpected: %s\nActual: %s", groupNameExpected, groups),
                1, groups.size());
        assertEquals(String.format("Attached Policy contains invalid name.\nExpected: %s\nActual: %s", groupNameExpected, groups),
                groupNameExpected,
                groups.get(0).groupName());
    }

    private boolean ifUserExist() {
        GetUserRequest userRequest = GetUserRequest.builder()
                .userName(userNameExpected)
                .build();

        boolean ifExist = false;

        try {
            ifExist = nonNull(iamClient.getUser(userRequest));
        } catch (IamException ex) {
            System.err.println(ex.awsErrorDetails().errorMessage());
            System.exit(1);
        }

        return ifExist;
    }

    public List<Group> getGroupsForUser() {
        try {
            ListGroupsForUserRequest listGroupsForUserRequest = ListGroupsForUserRequest
                    .builder().userName(userNameExpected).build();
            return iamClient.listGroupsForUser(listGroupsForUserRequest).groups();
        } catch (IamException e) {
            System.out.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return new ArrayList<>();
    }
}
