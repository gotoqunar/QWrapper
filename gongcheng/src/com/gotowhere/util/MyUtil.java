package com.gotowhere.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyUtil {

	public static String readFile(String path) {
		String txt = "";
		File file = new File(path);
		try {
			FileReader reader = new FileReader(file);
			int fileLen = (int) file.length();
			char[] chars = new char[fileLen];
			reader.read(chars);
			txt = String.valueOf(chars);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return txt;
	}

	public static Matcher easyMatcher(String regEx, String html) {
		Pattern pat = Pattern.compile(regEx);
		Matcher mat = pat.matcher(html);
		boolean rs = mat.find();
		return mat;
	}

	public static void main(String[] args) throws Exception {
		String whereToWhere = "La Paz (LPB) - Cochabamba (CBB)";
		int kuohu1 = whereToWhere.indexOf("(");
		int kuohu2 = whereToWhere.indexOf(")");
		// 出发机场名称
		String depairportName = whereToWhere.substring(0, kuohu1).trim();
		// 出发机场
		String depairport = whereToWhere.substring(kuohu1 + 1, kuohu2).trim();
		whereToWhere = whereToWhere.substring(kuohu2 + 1);
		kuohu1 = whereToWhere.indexOf("(");
		kuohu2 = whereToWhere.indexOf(")");
		// 到达机场名称
		String arrairportName = whereToWhere.substring(
				whereToWhere.indexOf('-') + 1, kuohu1).trim();
		// 到达机场
		String arrairport = whereToWhere.substring(kuohu1 + 1, kuohu2);
		System.out.println("depairportName : " + depairportName);
		if (1 == 1) {
			return;
		}
		String dcPath = "D:\\thisismywork\\QWrapperTemplate_Java\\src\\com\\gotowhere\\rw140708\\amaszonas\\jiage.txt";
		String html = readFile(dcPath);
		String jg = clearScraptHtml(html);
		String[] jiage = jg.substring(jg.lastIndexOf("USD")).split(" ");
		String it = findIt(html);
		System.out.println(it);
		// System.out.println(html);
		String scrapt = "<script>set_costo(";
		int scraptStart = html.indexOf(scrapt);
		// System.out.println(html.substring(0, scraptStart));
		String scraptHtml = html.substring(scraptStart);
		// System.out.println(scraptHtml);
		scraptHtml = clearScraptHtml(scraptHtml);
		forRequest(scraptHtml);
		if (1 == 1) {
			return;
		}
		String jiexian = "<div class=\"Filas_tabla \" id=\"fila[0-9]+\" >";
		Pattern pat1 = Pattern.compile(jiexian, Pattern.CASE_INSENSITIVE);
		Matcher mat1 = pat1.matcher(html);
		boolean rs1 = mat1.find();
		System.out.println(mat1.start());
		for (int i = 1; i <= mat1.groupCount(); i++) {
			System.out.println(mat1.group(i));
		}

		Matcher matcher = easyMatcher(jiexian, html);
		for (int i = 1; i <= matcher.groupCount(); i++) {
			System.out.println(matcher.group(i));
		}
		// int start = html.indexOf(jiexian);
		// System.out.println(html.substring(start));
		if (1 == 1) {
			return;
		}
		String regEx_html = "<[^>]+>"; // 定义check[0]标签的正则表达式
		Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(html);
		html = m_html.replaceAll(""); // 过滤html标签
		System.out.println(html);

		String regEx = "<(.*)>(.*)<\\/(.*)>|<(.*)\\/>";
		Pattern pat = Pattern.compile(regEx);
		Matcher mat = pat.matcher(html);
		boolean rs = mat.find();
		for (int i = 1; i <= mat.groupCount(); i++) {
			System.out.println(mat.group(i));
		}
	}

	private static String findIt(String html) {
		String htmlTemp = html;
		int start = htmlTemp.indexOf("data: { it:\"");
		htmlTemp = htmlTemp.substring(start);
		htmlTemp = htmlTemp.substring("data: { it:\"".length());
		htmlTemp = htmlTemp.substring(0, htmlTemp.indexOf('"'));
		return htmlTemp;
	}

	private static void forRequest(String scraptHtml) {
		String scrapt = scraptHtml.replace("set_costo(", "");
		scrapt = scrapt.replace(")", "");
		scrapt = scrapt.replace("\"\"", ",");
		scrapt = scrapt.replace("\"", "");
		System.out.println(scrapt);
		String[] ss = scrapt.split(",");
		for (int i = 0; i < ss.length; i++) {
			String a = ss[i++];
			String b = ss[i++];
			String c = ss[i++];
			String d = ss[i];

		}
	}

	/**
	 * 去掉scraptHtml标签
	 * 
	 * @param scraptHtml
	 */
	private static String clearScraptHtml(String scraptHtml) {
		String regEx_html = "<[^>]+>"; // 定义check[0]标签的正则表达式
		Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(scraptHtml);
		return m_html.replaceAll(""); // 过滤html标签
	}
}
