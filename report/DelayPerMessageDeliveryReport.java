package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Reports delivered messages' delays (one line per delivered message)
 * and cumulative delivery probability sorted by message delays.
 * Ignores the messages that were created during the warm up period.
 *
 * @author Kalis, Universitas Sanata Darma
 */
public class DelayPerMessageDeliveryReport extends Report implements MessageListener {
    public static final String HEADER = "Message ID \t Delay";

	/** DelayPerMessageDeliveryReport for each message */
	private Map<String, Double> delays;

	/**
	 * Constructor.
	 */
	public DelayPerMessageDeliveryReport() {
		init();
	}

	@Override
	public void init() {
		super.init();
		write(HEADER);
		this.delays = new HashMap<String, Double>(); // menyimpan nilai delay setiap pesan
	}

	public void newMessage(Message m) { //pesan yang baru dibuat tidak akan memiliki nilai delay sebelum berhasil dikirim
		// do nothing
	}

	public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
		if (!delays.containsKey(m.getId())) { //Cek sudah pernah dikirim atau blm, jika belum
			delays.put(m.getId(), 0.0);  //Message akan diberi nilai delay awal 0
		}

		double currentDelay = getSimTime() - m.getCreationTime();
		double delay = delays.get(m.getId());

		if (currentDelay > delay) { //Memperbarui nilai delay
			delays.put(m.getId(), currentDelay);
		}
		
		if (m.getTo() == to && firstDelivery) { //Menambahkan pesan yang berhasil dikirim dan sampai ke tujuan ke dalam laporan
			write(m.getId() + "\t" + delays.get(m.getId()));
		}
	}

	@Override
	public void done() {
		super.done();
	}

	// nothing to implement for the rest
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {}
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {}
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {}
}
