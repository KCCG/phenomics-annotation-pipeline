package au.org.garvan.kccg.annotations.pipeline.engine.connectors;

import org.junit.Test;

import java.util.Arrays;

public class GeoDataConnectorTest {

    GeoDataConnector geoDataConnector = new GeoDataConnector();



    @Test
    public void getLinkedIds() {
        geoDataConnector.getLinkedIds(Arrays.asList("25643280"));
    }

    @Test
    public void getLinkedData() {
        geoDataConnector.getLinkedData(Arrays.asList(5000L,6000L,9000L));

    }
}