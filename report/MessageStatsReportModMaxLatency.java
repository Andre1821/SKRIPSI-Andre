/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.SimScenario;
import core.Tuple;
import static java.util.Collections.list;
import java.util.HashSet;
import java.util.Set;
import routing.*;
import routing.community.ChitChatPrioritized;

/**
 * Report for generating different kind of total statistics about message
 * relaying performance. Messages that were created during the warm up period
 * are ignored.
 * <P>
 * <strong>Note:</strong> if some statistics could not be created (e.g. overhead
 * ratio if no messages were delivered) "NaN" is reported for double values and
 * zero for integer median(s).
 */
public class MessageStatsReportModMaxLatency extends Report implements MessageListener {

    private Map<String, Double> creationTimes;
    private List<Double> latencies;
    private List<Integer> hopCounts;
    private List<Double> msgBufferTime;
    private List<Double> rtt; // round trip times
    private List<Double> delivery;
    private double persen;
    private double lastPersen;

    private Map<String, Set<DTNHost>> nrofMsgForward; //menyimpan masing-masing jumlah pesan yg terforward
    private Map<String, List<Double>> mapLatensi; //menyimpan masing-masing jumlah pesan yg terforward
    private Map<String, Message> messages; //menyimpan pesan yang dibuat
    private Map<String, Double> batasLatensi; //menyimpan latensi terkahir dari setiap pesannya

    private int nrofDropped;
    private int nrofRemoved;
    private int nrofStarted;
    private int nrofAborted;
    private int nrofRelayed;
    private int nrofCreated;
    private int nrofResponseReqCreated;
    private int nrofResponseDelivered;
    private int nrofDelivered;

    private String M_TOPIC = "Message Topic";

    /**
     * Constructor.
     */
    public MessageStatsReportModMaxLatency() {
        init();
    }

    @Override
    protected void init() {
        super.init();
        this.creationTimes = new HashMap<String, Double>();
        this.latencies = new ArrayList<Double>();
        this.msgBufferTime = new ArrayList<Double>();
        this.hopCounts = new ArrayList<Integer>();
        this.rtt = new ArrayList<Double>();
        this.delivery = new ArrayList<Double>();

        this.nrofDropped = 0;
        this.nrofRemoved = 0;
        this.nrofStarted = 0;
        this.nrofAborted = 0;
        this.nrofRelayed = 0;
        this.nrofCreated = 0;
        this.nrofResponseReqCreated = 0;
        this.nrofResponseDelivered = 0;
        this.nrofDelivered = 0;

        this.nrofMsgForward = new HashMap<String, Set<DTNHost>>();
        this.mapLatensi = new HashMap<String, List<Double>>();
        this.messages = new HashMap<String, Message>();
        this.batasLatensi = new HashMap<String, Double>();
        this.persen = 0.3;
    }

    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        if (isWarmupID(m.getId())) {
            return;
        }

        if (dropped) {
            this.nrofDropped++;
        } else {
            this.nrofRemoved++;
        }

        this.msgBufferTime.add(getSimTime() - m.getReceiveTime());
    }

    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
        if (isWarmupID(m.getId())) {
            return;
        }

        this.nrofAborted++;
    }

    public void messageTransferred(Message m, DTNHost from, DTNHost to,
            boolean finalTarget) {
        if (isWarmupID(m.getId())) {
            return;
        }

        this.nrofRelayed++;

        if (nrofMsgForward.containsKey(m.getId()) && m.getId().contains("S")) {
            Set<DTNHost> hosts = nrofMsgForward.get(m.getId());
            hosts.add(to);
            nrofMsgForward.put(m.getId(), hosts);
            int totalNodes = SimScenario.getInstance().getHosts().size();
            double percentRecieved = (double) hosts.size() / totalNodes;

            if (batasLatensi.get(m.getId()) == null) {
                batasLatensi.put(m.getId(), 0.0);
            }
            
            if (percentRecieved - batasLatensi.get(m.getId()) >= persen) { // 30%, 60%, 90% node sudah menerima pesan
                if (mapLatensi.containsKey(m.getId())) {
                    List<Double> laten = mapLatensi.get(m.getId());
                    laten.add(getSimTime() - this.creationTimes.get(m.getId()));
                    mapLatensi.put(m.getId(), laten);
                    batasLatensi.put(m.getId(), percentRecieved);
                } else {
                    List<Double> laten = new ArrayList<>();
                    laten.add(getSimTime() - this.creationTimes.get(m.getId()));
                    mapLatensi.put(m.getId(), laten);
                    batasLatensi.put(m.getId(), 0.3);
                }
            }
        } else {
            Set<DTNHost> hosts = new HashSet<>();
            hosts.add(to);
            nrofMsgForward.put(m.getId(), hosts);
        }

        if (finalTarget) {
            this.latencies.add(getSimTime() - this.creationTimes.get(m.getId()));
            this.nrofDelivered++;
            this.hopCounts.add(m.getHops().size() - 1);

            if (m.isResponse()) {
                this.rtt.add(getSimTime() - m.getRequest().getCreationTime());
                this.nrofResponseDelivered++;
            }
        }
    }

    public void newMessage(Message m) {
        if (isWarmup()) {
            addWarmupID(m.getId());
            return;
        }

        this.creationTimes.put(m.getId(), getSimTime());
        this.nrofCreated++;
        this.messages.put(m.getId(), m);
        if (m.getResponseSize() > 0) {
            this.nrofResponseReqCreated++;
        }
    }

    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
        if (isWarmupID(m.getId())) {
            return;
        }

        this.nrofStarted++;
    }

    @Override
    public void done() {
        for (String messageId : mapLatensi.keySet()) {
            List<Double> delayList = mapLatensi.get(messageId);

            StringBuilder delayStringBuilder = new StringBuilder();
            for (Double delay : delayList) {
                delayStringBuilder.append(delay).append(", ");
            }

            String delayString = delayStringBuilder.toString();
            if (delayString.endsWith(", ")) {
                delayString = delayString.substring(0, delayString.length() - 2);
            }

            write(messageId + "\t" + delayString);
        }

        super.done();
    }

}
