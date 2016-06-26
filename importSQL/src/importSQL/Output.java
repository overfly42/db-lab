package importSQL;

import iface.DbInterface;

public class Output implements DbInterface {

	@Override
	public void writeln(String str) {
		System.out.println(str);

	}

}
