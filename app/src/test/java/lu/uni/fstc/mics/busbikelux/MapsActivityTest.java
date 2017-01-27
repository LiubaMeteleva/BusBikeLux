package lu.uni.fstc.mics.busbikelux;
/**
 * Created by Tatiana-san on 27.01.2017.
 */
import org.junit.Test;

import static lu.uni.fstc.mics.busbikelux.R.id.toggBtnBike;
import static lu.uni.fstc.mics.busbikelux.R.id.toggBtnBus;
import junit.framework.TestCase;

public class MapsActivityTest extends TestCase {

    MapsActivity mMapsAct;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mMapsAct = new MapsActivity();
    }

    @Test
    public void toggleBtnBusTest() throws Exception {
        assertNotNull(toggBtnBus);
    }
    @Test
    public void toggleBtnBikeTest() throws Exception {
        assertNotNull(toggBtnBike);
    }
}
