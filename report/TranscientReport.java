/*
 * @(#)CommunityDetectionReport.java
 *
 * Copyright 2010 by University of Pittsburgh, released under GPLv3.
 * 
 */
package report;

import java.util.*;

import core.*;
import routing.*;
import routing.community.ChitChatEpidemic;
import routing.community.TranscientEngine;
import routing.community.ChitChatPrioritized;

/**
 * <p>
 * Reports the local communities at each node whenever the done() method is
 * called. Only those nodes whose router is a DecisionEngineRouter and whose
 * RoutingDecisionEngine implements the
 * routing.community.CommunityDetectionEngine are reported. In this way, the
 * report is able to output the result of any of the community detection
 * algorithms.</p>
 *
 * @author PJ Dillon, University of Pittsburgh
 */
public class TranscientReport extends Report implements UpdateListener {

    private Map<DTNHost, List<Map<String, Double>>> trsNodeInterval;

    private int interval;
    private double lastRecord;

    public TranscientReport() {
        init();
    }

    public void init() {
        super.init();
        this.trsNodeInterval = new HashMap<>();
        this.interval = 6000;
        this.lastRecord = 0.0;

        Random rng = new Random();
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();

        for (int i = 0; i < 5; i++) {
            DTNHost random = nodes.get(rng.nextInt(nodes.size()));
//            System.out.println(random);
            if (!this.trsNodeInterval.containsKey(random)) {
                this.trsNodeInterval.put(random, new ArrayList<>());
            }
        }
    }

    @Override
    public void done() {
        write("h");
        for (Map.Entry<DTNHost, List<Map<String, Double>>> entry : this.trsNodeInterval.entrySet()) {
            write(entry.getKey()+"p");
        }
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        if (SimClock.getTime() - lastRecord >= interval) {
            lastRecord = SimClock.getTime();
            for (DTNHost h : hosts) {
                MessageRouter mr = h.getRouter();
                DecisionEngineRouter de = (DecisionEngineRouter) mr;
//                ChitChatEpidemic dbr = (ChitChatEpidemic) de.getDecisionEngine();
                ChitChatPrioritized dbr = (ChitChatPrioritized) de.getDecisionEngine();
                
                TranscientEngine hd = (TranscientEngine) dbr;

                Map<String, Double> transcient = hd.getTranscient();
                if (this.trsNodeInterval.containsKey(h)) {
                    List<Map<String, Double>> trs = new ArrayList<>(this.trsNodeInterval.get(h));
                    trs.add(transcient);
                    this.trsNodeInterval.put(h, trs);
//                    System.out.println(this.trsNodeInterval);
                }
            }
        }
    }

}
