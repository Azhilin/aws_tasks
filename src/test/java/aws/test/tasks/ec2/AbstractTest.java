package aws.test.tasks.ec2;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.utils.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public abstract class AbstractTest {
    protected static final Region REGION = Region.EU_WEST_1;
    protected static Ec2Client ec2;

    @BeforeClass
    public static void initParams() {
        String awsProfile = System.getProperty("awsProfile");
        ec2 = Ec2Client.builder()
                .credentialsProvider(ProfileCredentialsProvider.create(awsProfile))
                .region(REGION)
                .build();
    }

    @AfterClass
    public static void close() {
        ec2.close();
    }

    protected Instance getPublicInstance(List<Reservation> reservationsListActual) {
        return reservationsListActual
                .stream()
                .filter((r) -> r.instances().get(0).publicIpAddress() != null)
                .findFirst()
                .get()
                .instances()
                .get(0);
    }

    protected Instance getPrivateInstance(List<Reservation> reservationsListActual) {
        return reservationsListActual
                .stream()
                .filter((r) -> r.instances().get(0).publicIpAddress() == null)
                .findFirst()
                .get()
                .instances()
                .get(0);
    }

    protected List<Reservation> filterTerminatedInstances(List<Reservation> reservations){
        return reservations
                .stream()
                .filter(reservation -> {
                    String state = reservation.instances().get(0).state().name().toString();
                    return !StringUtils.equals("terminated", state);
                })
                .collect(Collectors.toList());
    }
}
