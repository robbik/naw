package org.naw.core.util.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class ObjectUtils {

	public static <T> T coalesce(T... values) {
		for (int i = 0, len = values.length; i < len; ++i) {
			T value = values[i];

			if (value != null) {
				return value;
			}
		}

		return null;
	}

	public static boolean equals(Object a, Object b) {
		if (a == b) {
			return true;
		}

		if ((a == null) || (b == null)) {
			return false;
		}

		return a.equals(b);
	}

	private static byte[] readBytes(ObjectInputStream in) throws IOException {
		int length = in.readInt();

		byte[] bytes = new byte[length];
		in.readFully(bytes, 0, length);

		return bytes;
	}

	private static void writeBytes(byte[] bytes, ObjectOutputStream out)
			throws IOException {
		out.writeInt(bytes.length);
		out.write(bytes);
	}

	public static void writeBytes(Object o, ObjectOutputStream out)
			throws IOException {
		if (o instanceof byte[]) {
			out.write('b');
			out.write(']');
			writeBytes((byte[]) o, out);
		} else if (o instanceof String) {
			out.write('S');
			out.write(' ');
			writeBytes(((String) o).getBytes("UTF-8"), out);
		} else if (o instanceof char[]) {
			out.write('c');
			out.write(']');
			writeBytes(String.valueOf((char[]) o).getBytes("UTF-8"), out);
		} else if (byte.class.isInstance(o)) {
			out.write('b');
			out.write(' ');
			out.writeByte(byte.class.cast(o));
		} else if (short.class.isInstance(o)) {
			out.write('s');
			out.write(' ');
			out.writeShort(short.class.cast(o));
		} else if (char.class.isInstance(o)) {
			out.write('c');
			out.write(' ');
			out.writeChar(char.class.cast(o));
		} else if (int.class.isInstance(o)) {
			out.write('i');
			out.write(' ');
			out.writeInt(int.class.cast(o));
		} else if (long.class.isInstance(o)) {
			out.write('l');
			out.write(' ');
			out.writeLong(long.class.cast(o));
		} else if (Byte.class.isInstance(o)) {
			out.write('B');
			out.write(' ');
			out.writeByte(Byte.class.cast(o).byteValue());
		} else if (Short.class.isInstance(o)) {
			out.write('S');
			out.write(' ');
			out.writeShort(Short.class.cast(o).shortValue());
		} else if (Character.class.isInstance(o)) {
			out.write('C');
			out.write(' ');
			out.writeChar(Character.class.cast(o).charValue());
		} else if (Integer.class.isInstance(o)) {
			out.write('I');
			out.write(' ');
			out.writeInt(Integer.class.cast(o).intValue());
		} else if (Long.class.isInstance(o)) {
			out.write('L');
			out.write(' ');
			out.writeLong(Long.class.cast(o).longValue());
		} else {
			out.write('J');
			out.write('O');
			out.writeObject(o);
		}
	}

	private static Object readFromBytes(ObjectInputStream in)
			throws IOException, ClassNotFoundException {

		char h1 = in.readChar();
		char h2 = in.readChar();

		if ((h1 == 'b') && (h2 == ']')) {
			return readBytes(in);
		}

		if ((h1 == 'c') && (h2 == ']')) {
			return new String(readBytes(in), "UTF-8").toCharArray();
		}
		if ((h1 == 'S') && (h2 == ' ')) {
			return new String(readBytes(in), "UTF-8");
		}

		if ((h1 == 'b') && (h2 == ' ')) {
			return in.readByte();
		}
		if ((h1 == 's') && (h2 == ' ')) {
			return in.readShort();
		}
		if ((h1 == 'c') && (h2 == ' ')) {
			return in.readChar();
		}
		if ((h1 == 'i') && (h2 == ' ')) {
			return in.readInt();
		}
		if ((h1 == 'l') && (h2 == ' ')) {
			return in.readLong();
		}

		if ((h1 == 'B') && (h2 == ' ')) {
			return Byte.valueOf(in.readByte());
		}
		if ((h1 == 'S') && (h2 == ' ')) {
			return Short.valueOf(in.readShort());
		}
		if ((h1 == 'C') && (h2 == ' ')) {
			return Character.valueOf(in.readChar());
		}
		if ((h1 == 'I') && (h2 == ' ')) {
			return Integer.valueOf(in.readInt());
		}
		if ((h1 == 'L') && (h2 == ' ')) {
			return Long.valueOf(in.readLong());
		}

		if ((h1 == 'J') && (h2 == 'O')) {
			return in.readObject();
		}

		throw new IOException("unknown content " + h1 + h2);
	}

	public static byte[] toBytes(Object o) {
		if (o == null) {
			return null;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			ObjectOutputStream oos = new ObjectOutputStream(out);
			writeBytes(o, oos);

			oos.flush();
		} catch (Throwable t) {
			// do nothing
		}

		return out.toByteArray();
	}

	public static Object fromBytes(byte[] bytes) {
		if (bytes == null) {
			return null;
		}

		try {
			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(bytes));

			return readFromBytes(ois);
		} catch (Throwable t) {
			return null;
		}
	}
}
