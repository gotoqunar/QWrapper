import java.util.regex.*;

public class GroupTest {
	public static void main(String[] args) throws Exception {
		Pattern p = Pattern.compile("(ca)(t)");
		Matcher m = p.matcher("one cat,two cats in the yard");
		StringBuffer sb = new StringBuffer();
		boolean result = m.find();
		System.out.println("该次查找获得匹配组的数量为：" + m.groupCount());
		for (int i = 1; i <= m.groupCount(); i++) {
		}
	}
}