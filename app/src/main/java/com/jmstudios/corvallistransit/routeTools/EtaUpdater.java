package com.jmstudios.corvallistransit.routeTools;

import android.util.Log;

import com.jmstudios.corvallistransit.models.BusRouteStop;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Thread class which updates the ETA for a given Bus Stop which belongs to a route.
 */
public class EtaUpdater implements Runnable {
    private final String TAG = this.getClass().getName();
    private BusRouteStop mStopClosure;
    private String mEtaUrl;

    public EtaUpdater(BusRouteStop stopClosure, String etaUrl) {
        mStopClosure = stopClosure;
        mEtaUrl = etaUrl;
    }

    /**
     * Given a Bus Stop for a route, connects to the CTS XML feed and parses
     * the ETA for that stop.
     */
    @Override
    public void run() {
        CtsXmlParser parser = new CtsXmlParser();
        InputStream stream;

        try {

            long start = System.nanoTime();
            stream = ConnectionsUtils.downloadUrl(mEtaUrl + mStopClosure.stopTag);
            long end = System.nanoTime();

            Log.d(TAG, "Time to connect: " + ((end - start) / 1000000) + " miliseconds");

            parser.parseStopEta(stream, mStopClosure);
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        } catch (XmlPullParserException e) {
            Log.d(TAG, e.toString());
        }
    }
}
