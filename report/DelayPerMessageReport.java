package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Tuple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reports delivered messages' delays (one line per delivered message) and
 * cumulative delivery probability sorted by message delays. Ignores the
 * messages that were created during the warm up period.
 *
 * @author Andre, Universitas Sanata Darma
 */
public class DelayPerMessageReport extends Report implements MessageListener {

    public static final String HEADER = "Message ID \t Delay";

    /**
     * Delay for each message
     */
    private String M_TOPIC = "Message Topic";
    private Map<String, List<Double>> delays;

    /**
     * Constructor.
     */
    public DelayPerMessageReport() {
        init();
    }

    @Override
    public void init() {
        super.init();
        write(HEADER);
        this.delays = new HashMap<String, List<Double>>(); // menyimpan nilai delay setiap pesan
    }

    public void newMessage(Message m) { //pesan yang baru dibuat tidak akan memiliki nilai delay sebelum berhasil dikirim
        // do nothing
    }

    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
//        List<Double> delayList;
        double currentDelay;

        Tuple<String, String> topic = (Tuple<String, String>) m.getProperty(M_TOPIC);
        if (to.getSocialProfile().contains(topic.getKey()) || m.getId().contains("S")) {
            if (delays.containsKey(m.getId())) {
                List<Double> delayList = delays.get(m.getId());
//                currentDelay = getSimTime() - delayList.get((delayList.size() - 1)); // mengambil nilai waktu yang paling 
                currentDelay = getSimTime() - m.getReceiveTime(); // mengambil nilai waktu yang paling 
                delayList.add(currentDelay);
                delays.put(m.getId(), delayList);
            } else {
                List<Double> delayList = new ArrayList<Double>();
                currentDelay = getSimTime() - m.getCreationTime();
                delayList.add(currentDelay);
                delays.put(m.getId(), delayList);
            }
        }

//        if (delayList == null) { // Jika pesan belum pernah dikirim sebelumnya
//            
//            delays.put(m.getId(), delayList);
//        }
//        if (!delays.containsKey(m.getId())) { //Cek sudah pernah dikirim atau blm, jika belum
//            delays.put(m.getId(), 0.0);  //Message akan diberi nilai delay awal 0
//        }
//
//        double currentDelay = getSimTime() - m.getCreationTime(); // masih salah perlu dirubah, karena ini menghitungnya dari waktu awal dibuat hingga waktu terakhir di forward
//        double delay = delays.get(m.getId());
//        if (currentDelay > delay) { //Memperbarui nilai delay
//            delays.put(m.getId(), currentDelay);
//        }
    }

    private double calculateAverageDelay(List<Double> delayList) {
        double sum = 0.0;
        for (Double delay : delayList) {
            sum += delay;
        }
        System.out.println("SUM :" + sum);
        return sum / delayList.size();
    }

    @Override
    public void done() {
//        for (String messageId : delays.keySet()) {
//            List<Double> delayList = delays.get(messageId);
//            double averageDelay = calculateAverageDelay(delayList);
//            write(messageId + "\t" + averageDelay);
//        }

        for (String messageId : delays.keySet()) {
            List<Double> delayList = delays.get(messageId);

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

//        for (String messageId : delays.keySet()) {
//            write(messageId + "\t" + delays.get(messageId));
//        }
        super.done();
    }

    // nothing to implement for the rest
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
    }

    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
    }

    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
    }
}
