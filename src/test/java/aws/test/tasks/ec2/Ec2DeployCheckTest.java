package aws.test.tasks.ec2;

import org.junit.Test;
import org.junit.runners.Parameterized;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Reservation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//CXQA-EC2-01: 2 application instances should be deployed: public and private
public class Ec2DeployCheckTest extends AbstractTest {
    @Parameterized.Parameter
    public int numberOfInstancesExpected;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {2}
        });
    }

    @Test
    public void ec2DeployCheckTest() {
        DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
        DescribeInstancesResponse response = ec2.describeInstances(request);

        //Get list of reservations
        List<Reservation> reservationsListActual = filterTerminatedInstances(response.reservations());

        //Check Number of Instances
        assertEquals(String.format(
                        "Invalid number of instances found\nExpected: %s\nActual list: %s\n",
                        numberOfInstancesExpected,
                        reservationsListActual),
                numberOfInstancesExpected,
                reservationsListActual.size());

        //Check if instance with public IP exists in the list on instances
        assertTrue("Instance with Public Ip is not present in the instances list",
                reservationsListActual
                        .stream()
                        .anyMatch((r) -> r.instances().get(0).publicIpAddress() != null));

        //Check if instance with no public IP exists in the list on instances
        assertTrue("Instance with no Public Ip is not present in the instances list",
                reservationsListActual
                        .stream()
                        .anyMatch((r) -> r.instances().get(0).publicIpAddress() == null));
    }
}
