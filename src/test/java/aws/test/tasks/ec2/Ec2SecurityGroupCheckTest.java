package aws.test.tasks.ec2;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

//CXQA-EC2-03: The security groups' configuration:
public class Ec2SecurityGroupCheckTest extends AbstractTest {
    private final List<Integer> PORTS = List.of(22, 80);

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {}
        });
    }

    @Test
    public void ec2PublicInstanceCheckTest() {
        DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
        DescribeInstancesResponse response = ec2.describeInstances(request);

        //Get list of reservations
        List<Reservation> reservationsListActual = filterTerminatedInstances(response.reservations());

        //Check if instance with public IP exists in the list on instances
        assertTrue("Instance with Public Ip is not present in the instances list",
                reservationsListActual
                        .stream()
                        .anyMatch((r) -> r.instances().get(0).publicIpAddress() != null));

        //Get securityGroupId
        String securityGroupId = getPublicInstance(reservationsListActual).securityGroups().get(0).groupId();

        DescribeSecurityGroupsRequest sgRequest = DescribeSecurityGroupsRequest.builder()
                .groupIds(securityGroupId)
                .build();
        DescribeSecurityGroupsResponse sgResponse = ec2.describeSecurityGroups(sgRequest);
        List<IpPermission> ipPermissions = sgResponse.securityGroups().get(0).ipPermissions();

        //Check Ip Permissions for Port 80 and Port 22
        assertFalse("ipPermissions are empty.\n Expected size is 2 items for Port 80 and Port 22", ipPermissions.isEmpty());
        assertEquals("\"Invalid number of ipPermissions.\\n Expected size is 2 items for Port 80 and Port 22\"", 2, ipPermissions.size());
        //Check that both Port 80 and Port 22 are present in Ip Permissions
        PORTS.stream()
                .map(port -> ipPermissions.stream()
                        .anyMatch(p -> p.ipProtocol().equals("tcp") && p.fromPort().equals(port)))
                .forEach(Assert::assertTrue);
    }
}
