import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Test {

	public static void main(String[] args) {
		SimpleDateFormat ySdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat mSdf = new SimpleDateFormat("dd/MM/yyyy");
		try {
			String s = mSdf.format(ySdf.parse("2014-10-30"));
			s = s.replaceAll("/","%2F");
			System.out.println(s);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
