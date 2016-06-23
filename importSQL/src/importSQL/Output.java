package importSQL;

public class Output implements dbInterface.Output {

	@Override
	public void writeln(String str) {
		System.out.println(str);

	}

}
