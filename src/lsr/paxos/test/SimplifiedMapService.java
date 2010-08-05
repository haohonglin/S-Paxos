package lsr.paxos.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import lsr.common.MapServiceCommand;
import lsr.service.SimplifiedService;

public class SimplifiedMapService extends SimplifiedService {
	private HashMap<Long, Long> _map = new HashMap<Long, Long>();

	@Override
	protected byte[] execute(byte[] value) {
		MapServiceCommand command;
		try {
			command = new MapServiceCommand(value);
		} catch (IOException e) {
			_logger.log(Level.WARNING, "Incorrect request", e);
			return null;
		}

		Long x = _map.get(command.getKey());
		if (x == null)
			x = new Long(0);

		x = command.getA() * x + command.getB();
		_map.put(command.getKey(), x);

		ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
		DataOutputStream dataOutput = new DataOutputStream(byteArrayOutput);
		try {
			dataOutput.writeLong(x);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return byteArrayOutput.toByteArray();
	}

	@Override
	protected byte[] makeSnapshot() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(stream);
			objectOutputStream.writeObject(_map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return stream.toByteArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void updateToSnapshot(byte[] snapshot) {
		ByteArrayInputStream stream = new ByteArrayInputStream(snapshot);
		ObjectInputStream objectInputStream;
		try {
			objectInputStream = new ObjectInputStream(stream);
			_map = (HashMap<Long, Long>) objectInputStream.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static final Logger _logger = Logger.getLogger(MapService.class.getCanonicalName());

	public void instanceExecuted(int instanceId) {
		// ignoring
	}
}
