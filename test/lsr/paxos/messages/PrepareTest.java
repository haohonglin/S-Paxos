package lsr.paxos.messages;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.testng.annotations.Test;

@Test(groups = { "unit" })
public class PrepareTest {
	private int _view = 12;
	private int _firstUncommitted = 32;
	private Prepare _prepare;

	public void setUp() {
		_prepare = new Prepare(_view, _firstUncommitted);
	}

	public void testDefaultConstructor() {
		assertEquals(_view, _prepare.getView());
		assertEquals(_firstUncommitted, _prepare.getFirstUncommitted());
	}

	public void testSerialization() throws IOException {
		byte[] bytes = _prepare.toByteArray();
		assertEquals(bytes.length, _prepare.byteSize());

		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bis);

		MessageType type = MessageType.values()[dis.readByte()];
		Prepare deserializedPrepare = new Prepare(dis);

		assertEquals(MessageType.Prepare, type);
		compare(_prepare, deserializedPrepare);
		assertEquals(0, dis.available());
	}

	public void testCorrectMessageType() {
		assertEquals(MessageType.Prepare, _prepare.getType());
	}

	private void compare(Prepare expected, Prepare actual) {
		assertEquals(expected.getView(), actual.getView());
		assertEquals(expected.getSentTime(), actual.getSentTime());
		assertEquals(expected.getType(), actual.getType());

		assertEquals(expected.getFirstUncommitted(), actual.getFirstUncommitted());
	}
}
