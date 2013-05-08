package com.paremus.examples.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Marshaller {
	
	public static byte[] marshal(Serializable obj) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		new ObjectOutputStream(buffer).writeObject(obj);
		return buffer.toByteArray();
	}
	
	public static Object unmarshal(byte[] payload) throws IOException, ClassNotFoundException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		Object result= new ObjectInputStream(stream).readObject();
		return result;
	}
	
}
