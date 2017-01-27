package lu.uni.fstc.mics.busbikelux;
import junit.framework.TestCase;
import org.junit.Test;
import static lu.uni.fstc.mics.busbikelux.R.id.busListView;

/**
 * Created by Tatiana-san on 27.01.2017.
 */

public class BusListActivityTest extends TestCase {

    MapsActivity mMapsAct;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mMapsAct = new MapsActivity();
    }

    @Test
    public void busListTest() throws Exception {
        assertNotNull(busListView);
    }
}