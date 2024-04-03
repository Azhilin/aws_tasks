package aws.test.tasks.ec2;

import org.junit.Test;
import org.junit.runners.Parameterized;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//CXQA-EC2-02: Each EC2 instance should have the following configuration:
//
//Instance type: t2.micro
//Instance tags: cloudx: qa
// Root block device size: 8 GB
//Instance OS: Amazon Linux 2
//The public instance should have public IP assigned
//The private instance should not have public IP assigned
public class Ec2DeployDetailsTest extends AbstractTest {
    private final InstanceType instanceTypeExpected = InstanceType.T2_MICRO;
    public final Map.Entry<String, String> instanceTagExpected = new AbstractMap.SimpleEntry<>("cloudx", "qa");
    public final int instanceEbsSizeExpected = 8;
    public final String imageIdStartExpected = "ami-";

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {}
        });
    }

    @Test
    public void ec2PublicInstanceDetailsTest() {
        DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
        DescribeInstancesResponse response = ec2.describeInstances(request);

        //Get list of reservations
        List<Reservation> reservationsListActual = filterTerminatedInstances(response.reservations());

        //Check if instance with public IP exists in the list on instances
        assertTrue("Instance with Public Ip is not present in the instances list",
                reservationsListActual
                        .stream()
                        .anyMatch((r) -> r.instances().get(0).publicIpAddress() != null));

        //Get public instance
        Instance instanceActual = getPublicInstance(reservationsListActual);

        //Check instance type: t2.micro
        assertEquals(String.format("Instance Type is invalid\nExpected: %s\nActual: %s",
                        instanceTypeExpected,
                        instanceActual.instanceType())
                , instanceActual.instanceType()
                , instanceTypeExpected);

        //Get Root block device size
        int rootDeviceSizeActual = getRootBlockSize(getRootVolumeId(instanceActual));

        //Check Root block device size: 8 GB
        assertEquals(String.format("Instance Type is invalid\nExpected: %s\nActual: %s",
                        instanceEbsSizeExpected,
                        rootDeviceSizeActual)
                , rootDeviceSizeActual
                , instanceEbsSizeExpected);

        //Check OS Amazon Linux 2: imageIdStartExpected = "ami-"
        assertTrue(
                String.format("Invalid OS name.\nExpected: OS Amazon Linux 2: imageIdStartExpected = %s\n" +
                                "Actual: imageIdStartExpected = %s",
                        imageIdStartExpected,
                        instanceActual.imageId()
                ),
                instanceActual.imageId().startsWith(imageIdStartExpected));

        //Check Instance tags: cloudx: qa
        assertTrue(String.format("Expected TAG is absent in the list." +
                                "\nExpected: %s" +
                                "\nActual tags: %s",
                        instanceTagExpected,
                        instanceActual.tags()
                ),
                instanceActual
                        .tags()
                        .stream()
                        .anyMatch(tag -> tag.key().equals(instanceTagExpected.getKey())
                                && tag.value().equals(instanceTagExpected.getValue())));
    }

    @Test
    public void ec2PrivateInstanceDetailsTest() {
        DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
        DescribeInstancesResponse response = ec2.describeInstances(request);

        //Get list of reservations
        List<Reservation> reservationsListActual = filterTerminatedInstances(response.reservations());

        //Check if instance with no public IP exists in the list on instances
        assertTrue("Instance with no Public Ip is not present in the instances list",
                reservationsListActual
                        .stream()
                        .anyMatch((r) -> r.instances().get(0).publicIpAddress() == null));

        //Get private instance
        Instance instanceActual = getPrivateInstance(reservationsListActual);

        //Check instance type: t2.micro
        assertEquals(String.format("Instance Type is invalid\nExpected: %s\nActual: %s",
                        instanceTypeExpected,
                        instanceActual.instanceType())
                , instanceActual.instanceType()
                , instanceTypeExpected);

        //Get Root block device size
        int rootDeviceSizeActual = getRootBlockSize(getRootVolumeId(instanceActual));

        //Check Root block device size: 8 GB
        assertEquals(String.format("Instance Type is invalid\nExpected: %s\nActual: %s",
                        instanceEbsSizeExpected,
                        rootDeviceSizeActual)
                , rootDeviceSizeActual
                , instanceEbsSizeExpected);

        //Check OS Amazon Linux 2: imageIdStartExpected = "ami-"
        assertTrue(
                String.format("Invalid OS name.\nExpected: OS Amazon Linux 2: imageIdStartExpected = %s\n" +
                                "Actual: imageIdStartExpected = %s",
                        imageIdStartExpected,
                        instanceActual.imageId()
                ),
                instanceActual.imageId().startsWith(imageIdStartExpected));

        //Check Instance tags: cloudx: qa
        assertTrue(String.format("Expected TAG is absent in the list." +
                                "\nExpected: %s" +
                                "\nActual tags: %s",
                        instanceTagExpected,
                        instanceActual.tags()
                ),
                instanceActual
                        .tags()
                        .stream()
                        .anyMatch(tag -> tag.key().equals(instanceTagExpected.getKey())
                                && tag.value().equals(instanceTagExpected.getValue())));
    }

    private int getRootBlockSize(String rootVolumeId) {
        DescribeVolumesRequest volumeRequest = DescribeVolumesRequest.builder()
                .volumeIds(rootVolumeId)
                .build();

        DescribeVolumesResponse volumeResponse = ec2.describeVolumes(volumeRequest);
        return volumeResponse.volumes().get(0).size();
    }

    private String getRootVolumeId(Instance instanceActual) {
        return instanceActual.blockDeviceMappings().stream()
                .filter(mapping -> mapping.deviceName().equals(instanceActual.rootDeviceName()))
                .findFirst()
                .map(mapping -> mapping.ebs().volumeId())
                .orElse("");
    }
}
