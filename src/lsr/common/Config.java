package lsr.common;

public class Config {
	/*---------------------------------------------
	 * The following properties are read from the 
	 * paxos.properties file  
	 *---------------------------------------------*/
	/**
	 * Defines the default window size - that is, the maximum number of
	 * concurrently proposed instances.
	 * 
	 * As for now, the window size does not change in runtime.
	 */
	public static final String WINDOW_SIZE = "WindowSize";
	public static final int DEFAULT_WINDOW_SIZE = 1;	

	/**
	 * 1500 - maximum ethernet payload 
	 * 20/40 - ipv4/6 header
	 * 8 - udp header 
	 */
	public static final String MAX_UDP_PACKET_SIZE = "MaxUDPPacketSize";
	// Maximum UDP packet size in java is 65507. Higher than that and 
	// the send method throws an exception. 
	public static final int DEFAULT_MAX_UDP_PACKET_SIZE = 1500 - 28;
	
	/**
	 * The maximum size of batched request.
	 */
	public static final String BATCH_SIZE = "BatchSize";	
	public static final int DEFAULT_BATCH_SIZE = DEFAULT_MAX_UDP_PACKET_SIZE;
	
	
	/**
	 * If <code>_taskQueue</code> grows to more than this 
	 * value, the system is considered as being busy.
	 * This is used to refuse additional work from clients,
	 * thus preventing the queues from growing too much.
	 */
	public static final int DEFAULT_BUSY_THRESHOLD = 10*1024;
	public static final String BUSY_THRESHOLD = "BusyThreshold";
	
	/*---------------------------------------------
	 * The following properties are compile time 
	 * constants.
	 *---------------------------------------------*/	
	
	/**
	 * If enabled, all objects are transformed into byte[] or I/O streams using
	 * java's object input/output streams.
	 * 
	 * Otherwise user defined functions are used for that.
	 */
	public static final boolean javaSerialization = false;

	/**
	 * Before any snapshot was made, we need to have an estimate of snapshot
	 * size. Value given as for now is 1 KB
	 */
	public static final double firstSnapshotSizeEstimate = 1024;

	/** Minimum size of the log before a snapshot is attempted */
	public static final int SNAPSHOT_MIN_LOG_SIZE = 20*1024*1024;
	
	/** Ratio = \frac{log}{snapshot}. How bigger the log must be to ask */
	public static final double SNAPSHOT_ASK_RATIO = 1;

	/** Ratio = \frac{log}{snapshot}. How bigger the log must be to force */
	public static final double SNAPSHOT_FORCE_RATIO = 2;

//	// when first we get accept message (propose was lost) then send accept to
//	// others
//	public static final boolean SEND_ACCEPT_WITHOUT_PROPOSE = true;

	public static final int UDP_RECEIVE_BUFFER_SIZE = 64 * 1024;
	public static final int UDP_SEND_BUFFER_SIZE = 64 * 1024;

	public static final long RETRANSMIT_TIMEOUT = 1000;

	/** for re-sending catch-up query we use a separate, self-adjusting timeout */
	public static final long CATCHUP_MIN_RESEND_TIMEOUT = 50;

	/** This is the timeout designed for periodic Catch-Up */
	public static final long PERIODIC_CATCHUP_TIMEOUT = 2000;
	
}
