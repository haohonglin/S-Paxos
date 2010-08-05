package lsr.paxos.storage;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Contains data related with one consensus instance.
 */
public class ConsensusInstance implements Serializable {
	private static final long serialVersionUID = 1L;
	protected final int _id;
	protected int _view;
	protected byte[] _value;
	protected LogEntryState _state;
	private transient BitSet _accepts = new BitSet();

	/**
	 * Represents possible states of consensus instance.
	 */
	public enum LogEntryState {
		/**
		 * Represents the empty consensus state. There is no information about
		 * current view nor value.
		 */
		UNKNOWN,
		/**
		 * The consensus in this state received the <code>PROPOSE</code> message
		 * from the leader but hasn't received the majority of the
		 * <code>ACCEPT</code> messages. In this state there is some view and
		 * value specified, but they can be changed later.
		 */
		KNOWN,
		/**
		 * Represents state when {@link Learner} received majority of
		 * <code>Accept</code> message. In this state the view and value of
		 * consensus instance cannot be changed.
		 */
		DECIDED
	}

	/**
	 * Initializes new instance of consensus with all value specified.
	 * 
	 * @param id
	 *            - the id of instance to create
	 * @param state
	 *            - the state of consensus
	 * @param view
	 *            - the view of last message in this consensus
	 * @param value
	 *            - the value accepted or decided in this instance
	 */
	public ConsensusInstance(int id, LogEntryState state, int view, byte[] value) {
		if (state == LogEntryState.UNKNOWN && value != null)
			throw new IllegalArgumentException("Unknown instance with value different than null");
		_id = id;
		_state = state;
		_view = view;
		_value = value;
	}

	/**
	 * Initializes new empty instance of consensus. The initial state is set to
	 * <code>UNKNOWN</code>, view to <code>-1</code> and value to
	 * <code>null</code>.
	 * 
	 * @param id
	 *            the id of instance to create
	 */
	public ConsensusInstance(int id) {
		this(id, LogEntryState.UNKNOWN, -1, null);
	}

	public ConsensusInstance(DataInputStream input) throws IOException {
		_id = input.readInt();
		_view = input.readInt();
		_state = LogEntryState.values()[input.readInt()];

		int size = input.readInt();
		if (size == -1) {
			_value = null;
		} else {
			_value = new byte[size];
			input.readFully(_value);
		}
	}

	/**
	 * Gets the number of the consensus instance. Different instances should
	 * have different id's.
	 * 
	 * @return number of instance
	 */
	public int getId() {
		return _id;
	}

	/**
	 * Changes the view to the newest one. It cannot be changed to value less
	 * than current view, and shouldn't be changed if the consensus is already
	 * in <code>Decided</code> state.
	 * 
	 * @param view
	 *            the new view value
	 */
	public void setView(int view) {
		assert _view <= view : "Cannot set smaller view.";
		_view = view;
	}

	/**
	 * Gets the current view of this instance. The view of instance is
	 * represented by the view of last message. If the current state of
	 * consensus is decided, then view should not be changed.
	 * 
	 * @return the view number of current instance
	 */
	public int getView() {
		return _view;
	}

	/**
	 * Writes new value holding by this instance to the disk and cache. Each
	 * value has view in which it is valid, so it has to be set here also. If
	 * the current state was <code>UNKNOWN</code>, then it will be automatically
	 * changed to <code>KNOWN</code>.
	 * 
	 * @param view
	 *            the view number in which value is valid
	 * @param value
	 *            the value which was accepted by this instance
	 */
	public void setValue(int view, byte[] value) {
		if (view < _view)
			return;

		if (_state == LogEntryState.UNKNOWN)
			_state = LogEntryState.KNOWN;

//		if (_value == null || !Arrays.equals(value, _value)) {
//			assert _state != LogEntryState.DECIDED : "Cannot change value in decided instance.";
//			assert _view != view : "Different value for the same view";
//			_value = value;
//		}
		if (_state == LogEntryState.DECIDED && !Arrays.equals(_value, value)) {
			throw new RuntimeException("Cannot change values on a decided instance: " + this);
		}
		
		if (view > _view) {
			// Higher view value. Accept any value
			_view = view;
		} else {
			assert _view == view;
			// Same view. Accept a value only if the current value is null
			// or if the current value is equal to the new value
			assert _value == null || Arrays.equals(value, _value) 
			: "Different value for the same view";
		}
		
		_value = value;
	}

	/**
	 * Returns the value holding by this consensus. It represents last value
	 * which was accepted by <code>Acceptor</code>.
	 * 
	 * @return the current value of this instance
	 */
	public byte[] getValue() {
		return _value;
	}

	/**
	 * Gets the current state of this instance. When the state is set to
	 * <code>Decided</code> no values should be changed.
	 * 
	 * @return current state of consensus instance.
	 */
	public LogEntryState getState() {
		return _state;
	}

	/**
	 * Gets the set of replicas from which we get the <code>Accept</code>
	 * message from the current <code>view</code>.
	 * 
	 * @return id's of replicas
	 */
	public BitSet getAccepts() {
		return _accepts;
	}
	
		
	public boolean isMajority(int n) {
		return _accepts.cardinality() > (n / 2);
	}

	/**
	 * Changes the current state of this instance to <code>DECIDED</code>. This
	 * instance cannot be changed so <code>accepts</code> value will be set to
	 * <code>null</code>.
	 * 
	 * @see #getAccepts()
	 */
	public void setDecided() {
		_state = LogEntryState.DECIDED;
		_accepts = null;
	}

//	public byte[] toByteArray() {
//		ByteArrayOutputStream bas = new ByteArrayOutputStream(4 + 8);
//		DataOutputStream os = new DataOutputStream(bas);
//		try {
//			write(os);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//
//		return bas.toByteArray();
//	}
	
	public byte[] toByteArray() {
		ByteBuffer bb = ByteBuffer.allocate(byteSize());
		try {
			write(bb);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return bb.array();
	}

//	public void write(DataOutputStream os) throws IOException {
//		os.writeInt(_id);
//		os.writeInt(_view);
//		os.writeInt(_state.ordinal());
//		if (_value == null) {
//			os.writeInt(-1);
//		} else {
//			os.writeInt(_value.length);
//			os.write(_value);
//		}
//	}
	
	public void write(ByteBuffer bb) throws IOException {
		bb.putInt(_id);
		bb.putInt(_view);
		bb.putInt(_state.ordinal());
		if (_value == null) {
			bb.putInt(-1);
		} else {
			bb.putInt(_value.length);
			bb.put(_value);
		}
	}

	public int byteSize() {
		int size = (_value == null ? 0 : _value.length) + 4 /* length of array */;
		size += 3 * 4 /* ID, view and state */;
		return size;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _id;
		result = prime * result + ((_state == null) ? 0 : _state.hashCode());
		result = prime * result + Arrays.hashCode(_value);
		result = prime * result + _view;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConsensusInstance other = (ConsensusInstance) obj;
		if (_id != other._id)
			return false;
		if (_state == null) {
			if (other._state != null)
				return false;
		} else if (!_state.equals(other._state))
			return false;
		if (!Arrays.equals(_value, other._value))
			return false;
		if (_view != other._view)
			return false;
		return true;
	}

	public String toString() {
		return "Instance=" + _id + ", state=" + _state + ", view=" + _view;
	}

}
