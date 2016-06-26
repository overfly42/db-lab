package importAssertion;

import iface.DbInterface;

public class OutputConsole implements DbInterface {

	@Override
	public void writeln(String str) {
		System.out.println(str);

	}

}
