package info.kgeorgiy.java.advanced.implementor;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Test {
	public static void main(String[] args) throws ImplerException {
		Implementor i = new Implementor();
		Path path = Paths.get("E:\\java2016\\hw3");
		i.implement(ImplMe.class, path);
	}
}