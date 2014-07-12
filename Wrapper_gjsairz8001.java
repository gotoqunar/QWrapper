import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;

import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.exception.QFHttpClientException;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;

/**
 * 双程
 * 
 */
public class Wrapper_gjsairz8001 implements QunarCrawler {
	String fenge = "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$";
	String dafenge = "~~~~~~~~~~~~~~~~~~~~";

	@Override
	public String getHtml(FlightSearchParam param) {
		QFPostMethod post = null;
		QFGetMethod get = null;
		StringBuffer sb = new StringBuffer();
		try {
			QFHttpClient httpClient = new QFHttpClient(param, false);
			// 对于需要cookie的网站，请自己处理cookie（必须） 例如：
			httpClient.getParams().setCookiePolicy(
					CookiePolicy.BROWSER_COMPATIBILITY);

			// 1. 执行第一次请求
			String url1 = "http://www.amaszonas.com/ ";
			// String url1 =
			// " http://www.google-analytics.com/__utm.gif?utmwv=5.5.3&utms=1&utmn=1321638878&utmhn=www.amaszonas.com&utmcs=UTF-8&utmsr=1366x768&utmvp=779x490&utmsc=24-bit&utmul=zh-cn&utmje=1&utmfl=12.0%20r0&utmdt=Home%20-%20Airline%20Amaszonas%20-%20Flights%20Bolivia%2CCuzco%2CSucre&utmhid=611289840&utmr=-&utmp=%2F&utmht=1405001981074&utmac=UA-34657697-1&utmcc=__utma%3D44289076.1265598739.1405001981.1405001981.1405001981.1%3B%2B__utmz%3D44289076.1405001981.1.1.utmcsr%3D(direct)%7Cutmccn%3D(direct)%7Cutmcmd%3D(none)%3B&utmu=q~";
			get = new QFGetMethod(url1);
			// get.setRequestHeader("Referer",
			// "http://www.amaszonas.com/");
			int status = httpClient.executeMethod(get);
			// 2. 执行第二次请求,为了获取搜索条件中的it
			String url2 = "https://www.amaszonas.com/reserva3/avail.php?i=1";
			String cookie = StringUtils.join(
					httpClient.getState().getCookies(), "; ");
			post = new QFPostMethod(url2);
			httpClient.getState().clearCookies();
			post.addRequestHeader("Cookie", cookie);
			Map<String, String> map = this.createMap(param);
			// 生成NameValuePair
			NameValuePair[] url2Names = createNameValuePair(map);
			// 设置请求体
			post.setRequestBody(url2Names);
			// 设置Referer请求体
			post.setRequestHeader("Referer", "http://www.amaszonas.com/");
			// 设置编码格式
			post.getParams().setContentCharset("UTF-8");
			httpClient.executeMethod(post);
			String html = post.getResponseBodyAsString();
			cookie = StringUtils.join(httpClient.getState().getCookies(), "; ");
			// 3.执行第三次请求
			String it = findIt(html);
			String tipo_new = "1";
			String fechaaux = "";
			// 请求去的
			boolean jieguo = requestForInfo(it, tipo_new, fechaaux, cookie,
					httpClient, post, sb);
			if (!jieguo) {
				return sb.toString();
			}
			sb.append(" ");
			sb.append(this.dafenge);
			sb.append(" ");
			// 请求回的
			tipo_new = "2";
			fechaaux = "";
			jieguo = requestForInfo(it, tipo_new, fechaaux, cookie, httpClient,
					post, sb);
			String returnS = sb.toString();
			return returnS;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != get) {
				get.releaseConnection();
			}
		}
		return "Exception";
	}

	/**
	 * 发出请求
	 * 
	 * @param fechaaux
	 * @param tipo_new
	 * @param it
	 * @param cookie
	 * @param httpClient
	 * @param post
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws QFHttpClientException
	 */
	private boolean requestForInfo(String it, String tipo_new, String fechaaux,
			String cookie, QFHttpClient httpClient, QFPostMethod post,
			StringBuffer sb) throws QFHttpClientException, HttpException,
			IOException {
		String html = "";
		String url3 = "https://www.amaszonas.com/reserva3/server/avail_ajax.php";
		post = new QFPostMethod(url3);
		httpClient.getState().clearCookies();
		post.addRequestHeader("Cookie", cookie);
		NameValuePair[] url3Names = { new NameValuePair("it", it),
				new NameValuePair("tipo_new", tipo_new),
				new NameValuePair("fechaaux", fechaaux) };
		// 设置请求体
		post.setRequestBody(url3Names);
		// 设置Referer请求体
		post.setRequestHeader("Referer",
				"https://www.amaszonas.com/reserva3/avail.php?i=1");
		// 设置编码格式
		post.getParams().setContentCharset("UTF-8");

		httpClient.executeMethod(post);
		html = post.getResponseBodyAsString();
		if (html.contains("Error at parameter DestinationLocation")) {
			return false;
		}
		if (html.contains("Undefined index: ")) {
			return false;
		}
		String scrapt = "<script>set_costo(";
		int scraptStart = html.indexOf(scrapt);
		if (scraptStart == -1) {
			return false;
		}
		// 将html添加到返回值中
		sb.append(html.substring(0, scraptStart));
		// 4. 解析scrapt，进行循环请求
		String scraptHtml = html.substring(scraptStart);
		scraptHtml = clearScraptHtml(scraptHtml);
		// 对scraptHtml进行循环请求
		forRequest(sb, scraptHtml, httpClient, it, cookie);
		return true;
	}

	private NameValuePair[] createNameValuePair(Map<String, String> map) {
		NameValuePair[] returnNvp = new NameValuePair[map.size()];
		int i = 0;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			returnNvp[i++] = new NameValuePair(key, value);
		}
		return returnNvp;
	}

	/**
	 * 循环请求
	 * 
	 * @param sb
	 * @param scraptHtml
	 * @param httpClient
	 * @param it
	 * @throws IOException
	 * @throws HttpException
	 * @throws QFHttpClientException
	 */
	private void forRequest(StringBuffer sb, String scraptHtml,
			QFHttpClient httpClient, String it, String cookie)
			throws QFHttpClientException, HttpException, IOException {
		String scrapt = scraptHtml.replace("set_costo(", "");
		scrapt = scrapt.replace("\")", "\"");
		scrapt = scrapt.replace('\'', '"');
		scrapt = scrapt.replace("\"\"", ",");
		scrapt = scrapt.replace("\"", "");
		String[] ss = scrapt.split(",");
		for (int i = 0; i < ss.length; i++) {
			// 请求参数开始
			String varid = ss[i++];
			String tipo = ss[i++];
			String c = ss[i++];
			String op = ss[i];
			// 请求参数结束

			String url4 = "https://www.amaszonas.com/reserva3/server/fare_ajax.php";
			QFPostMethod post = new QFPostMethod(url4);
			httpClient.getState().clearCookies();
			post.addRequestHeader("Cookie", cookie);
			NameValuePair[] url3Names = { new NameValuePair("it", it),
					new NameValuePair("id", varid),
					new NameValuePair("ti", tipo), new NameValuePair("nop", op) };
			// 设置请求体
			post.setRequestBody(url3Names);
			// 设置Referer请求体
			post.setRequestHeader("Referer",
					"https://www.amaszonas.com/reserva3/avail.php?i=1");
			// 设置编码格式
			post.getParams().setContentCharset("UTF-8");

			httpClient.executeMethod(post);
			String html = post.getResponseBodyAsString();
			sb.append(fenge);
			sb.append(html);

		}
	}

	/**
	 * 去掉scraptHtml标签
	 * 
	 * @param scraptHtml
	 */
	private String clearScraptHtml(String scraptHtml) {
		String regEx_html = "<[^>]+>"; // 定义check[0]标签的正则表达式
		Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(scraptHtml);
		return m_html.replaceAll(""); // 过滤html标签
	}

	@Override
	public ProcessResultInfo process(String html, FlightSearchParam param) {
//		String dcPath = "D:\\thisismywork\\QWrapperTemplate_Java\\src\\com\\gotowhere\\rw140708\\amaszonas\\shuangcheng.txt";
//		html = MyUtil.readFile(dcPath);
		ProcessResultInfo result = new ProcessResultInfo();
		// 判断非正常情况
		if ("Exception".equals(html)) {
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;
		}
		if (html.contains("No flights were found available, please try another date")) {
			result.setRet(true);
			result.setStatus(Constants.NO_RESULT);
			return result;
		}
		if (html.contains("Error at parameter DestinationLocation")) {
			result.setRet(false);
			result.setStatus(Constants.PARSING_FAIL);
			return result;
		}
		if (html.contains("Undefined index: ")) {
			result.setRet(false);
			result.setStatus(Constants.PARAM_ERROR);
			return result;
		}
		String[] htmls = html.split("[~]{20}");
		try {

			// 解析去程航班
			List<RoundTripFlightInfo> infoList = processDanGe(htmls[0], param.getDepDate(),param.getDep(),param.getArr(),param.getWrapperid());
			// 解析返航航班
			List<RoundTripFlightInfo> retinfo = processDanGe(htmls[1], param.getRetDate(),param.getArr(),param.getDep(),param.getWrapperid());
//			System.out.println(htmls[1]);
			// 合并成往返信息
			List<RoundTripFlightInfo> flightList = new ArrayList<RoundTripFlightInfo>();
			for (int i = 0; i < infoList.size(); i++) {
				RoundTripFlightInfo qu = infoList.get(i);
				for (int j = 0; j < retinfo.size(); j++) {
					RoundTripFlightInfo hui = retinfo.get(j);
					RoundTripFlightInfo roundTripFlightInfo = new RoundTripFlightInfo();
					// 1. 去的信息
					// 1.0 info list储存航段信息
					roundTripFlightInfo.setInfo(qu.getInfo());
					// 1.2 detail储存航线的基本信息（包括票价，税费等）
					roundTripFlightInfo.setDetail(qu.getDetail());
					// 1.3 outboundPrice 去程价格
					roundTripFlightInfo.setOutboundPrice(qu.getOutboundPrice());
					// 2 返航信息
					// 2.0 retdepdate 返程日期，格式为YYYY-MM-DD，例如2014-06-12
					roundTripFlightInfo.setRetdepdate(hui.getDetail()
							.getDepdate());
					// 2.1 retflightno 返程航班号list，航班号一般在航班有中转时为多个
					roundTripFlightInfo.setRetflightno(hui.getDetail()
							.getFlightno());
					// 2.2 retinfo 返程航段信息
					roundTripFlightInfo.setRetinfo(hui.getInfo());
					// 2.3 returnedPrice 返程价格
					roundTripFlightInfo.setReturnedPrice(hui.getDetail()
							.getPrice());
					flightList.add(roundTripFlightInfo);
				}
			}
			result.setRet(true);
			result.setStatus(Constants.SUCCESS);
			result.setData(flightList);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.setRet(false);
			result.setStatus(Constants.PARSING_FAIL);
			return result;
		}
	}

	// 单个解析航班信息
	private List<RoundTripFlightInfo> processDanGe(String html,
			String depDate,String dep,String arr,String wrapperid) throws ParseException {
		List<RoundTripFlightInfo> flightList = new ArrayList<RoundTripFlightInfo>();
		String[] htmls = html.split("[$]{30}");
		// 航班主题信息
		String bodyHtml = processHtml(htmls[0]);
		bodyHtml = replaceBlank(bodyHtml);
		String[] bodyHtmls = bodyHtml.split("\\s{4,}");
		int bodyHtmlsI = 0;
		// 航班价格信息
		for (int i = 1; i < htmls.length; i++) {
			RoundTripFlightInfo baseFlight = new RoundTripFlightInfo();
			List<FlightSegement> info = new ArrayList<FlightSegement>();
			FlightDetail flightDetail = new FlightDetail();
			FlightSegement seg = new FlightSegement();
			// 航班号
			List<String> flightNoList = new ArrayList<String>();
			// 计算出价格
			String jiage1 = processJiage(htmls[i]);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			// 出发日期，格式为YYYY-MM-DD
			flightDetail.setDepdate(format.parse(depDate));
			// 航班号list，航班号一般在航班有中转时为多个
			flightDetail.setFlightno(flightNoList);
			// 货币单位
			flightDetail.setMonetaryunit("USD");
			double price = Double.parseDouble(jiage1);
			// 最低票价
			flightDetail.setPrice(price);
			// 出发城市或机场，三字码
			flightDetail.setDepcity(dep);
			// 到达城市或机场，三字码
			flightDetail.setArrcity(arr);
			// wrapper的id
			flightDetail.setWrapperid(wrapperid);

			String xuhao = bodyHtmls[bodyHtmlsI].trim();
			if (xuhao.equals(i + "")) {
				seg = processFlightSegement(bodyHtmlsI + 1, bodyHtmls,
						flightNoList, depDate);
				info.add(seg);
				bodyHtmlsI += 5;
			}
			bodyHtmlsI++;
			if (bodyHtmlsI < bodyHtmls.length) {
				String xuhao2 = bodyHtmls[bodyHtmlsI].trim();
				while (!xuhao2.equals((i + 1) + "")) {
					seg = processFlightSegement(bodyHtmlsI, bodyHtmls,
							flightNoList, depDate);
					info.add(seg);
					bodyHtmlsI += 5;
					if (bodyHtmlsI >= bodyHtmls.length) {
						break;
					}
					xuhao2 = bodyHtmls[bodyHtmlsI].trim();
				}
			}
			baseFlight.setDetail(flightDetail);
			baseFlight.setInfo(info);
			flightList.add(baseFlight);
		}
		return flightList;
	}

	/**
	 * 生成航班信息
	 * 
	 * @param bodyHtmlsI
	 * @param htmls
	 * @param flightNoList
	 * @return
	 */
	private FlightSegement processFlightSegement(int bodyHtmlsI,
			String[] htmls, List<String> flightNoList, String depDate) {
		FlightSegement seg = new FlightSegement();
		// 航班号
		String flightno = htmls[bodyHtmlsI].trim();
		// 从哪儿到哪儿
		String whereToWhere = htmls[bodyHtmlsI + 1].trim();
		// 解析出发机场和到达机场
		processAirport(whereToWhere, seg);

		// 出发时间
		String deptime = htmls[bodyHtmlsI + 2].trim();
		// 到达时间
		String arrtime = htmls[bodyHtmlsI + 3].trim();
		// 用时
		String yongshi = htmls[bodyHtmlsI + 4].trim();
		// 添加航班号
		flightNoList.add(flightno);
		seg.setFlightno(flightno);
		// 每一段的起飞日期 默认为第一段的起飞日期
		seg.setDepDate(depDate);
		seg.setArrDate(depDate);
		seg.setDeptime(deptime);
		seg.setArrtime(arrtime);
		return seg;
	}

	private void processAirport(String whereToWhere, FlightSegement seg) {
		// 出发机场三字码
		String depairport = "";
		// 到达机场三字码
		String arrairport = "";

//		whereToWhere = "La Paz (LPB) - Cochabamba (CBB)";
		int kuohu1 = whereToWhere.indexOf("(");
		int kuohu2 = whereToWhere.indexOf(")");
		// 出发机场名称
		String depairportName = whereToWhere.substring(0, kuohu1).trim();
		// 出发机场
		depairport = whereToWhere.substring(kuohu1 + 1, kuohu2).trim();
		whereToWhere = whereToWhere.substring(kuohu2 + 1);
		kuohu1 = whereToWhere.indexOf("(");
		kuohu2 = whereToWhere.indexOf(")");
		// 到达机场名称
		String arrairportName = whereToWhere.substring(
				whereToWhere.indexOf('-') + 1, kuohu1).trim();
		// 到达机场
		arrairport = whereToWhere.substring(kuohu1 + 1, kuohu2);
		seg.setDepairport(depairport);
		seg.setArrairport(arrairport);
	}

	/**
	 * 计算价格
	 * 
	 * @param jiageHtml
	 * @return
	 */
	private String processJiage(String jiageHtml) {
		String jiage1 = jiageHtml;
		int indexOfScript = jiage1.indexOf("<script");
		if (indexOfScript != -1) {
			jiage1 = jiage1.substring(0, indexOfScript);
		}
		jiage1 = clearScraptHtml(jiage1);
		jiage1 = jiage1.substring(jiage1.indexOf("USD"));
		jiage1 = jiage1.replace("USD", "");
		// 计算出价格USD的
		jiage1.trim();
		return jiage1;
	}

	/**
	 * 替换回车、换行制表符等
	 * 
	 * @param str
	 * @return
	 */
	private String replaceBlank(String str) {
		Pattern p = Pattern.compile("\t|\r|\n");
		Matcher m = p.matcher(str);
		String after = m.replaceAll("");
		return after;
	}

	/**
	 * 解析主题的网页
	 * 
	 * @param bodyHtml
	 * @return
	 */
	private String processHtml(String bodyHtml) {// 1. 去掉多余的开头
		String html = bodyHtml;
		String jiexian = "<div class=\"Filas_tabla \" id=\"fila[0-9]+\" >";
		Pattern p_start = Pattern.compile(jiexian, Pattern.CASE_INSENSITIVE);
		Matcher m_start = p_start.matcher(html);
		boolean rs1 = m_start.find();
		int youxiaostart = m_start.start();
		html = html.substring(youxiaostart);
		// 2.去掉script
		String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
		Pattern p_script = Pattern.compile(regEx_script,
				Pattern.CASE_INSENSITIVE);
		Matcher m_script = p_script.matcher(html);
		html = m_script.replaceAll(""); // 过滤script标签
		// 3.去掉标签
		String regEx_html = "<[^>]+>"; // 定义check[0]标签的正则表达式
		Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(html);
		html = m_html.replaceAll(""); // 过滤html标签
		return html;
	}

	private String findIt(String html) {
		String htmlTemp = html;
		int start = htmlTemp.indexOf("data: { it:\"");
		htmlTemp = htmlTemp.substring(start);
		htmlTemp = htmlTemp.substring("data: { it:\"".length());
		htmlTemp = htmlTemp.substring(0, htmlTemp.indexOf('"'));
		return htmlTemp;
	}

	@Override
	public BookingResult getBookingInfo(FlightSearchParam param) {
		String bookingUrlPre = "https://www.amaszonas.com/reserva3/avail.php?i=1";
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("post");
		Map<String, String> map = createMap(param);
		bookingInfo.setInputs(map);
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
		return bookingResult;
	}

	private Map<String, String> createMap(FlightSearchParam param) {// 定义传参变量
		String msn1 = "The+number+of++babies+cannot+be+greater+than+the+number+of+adults.";
		String msn2 = "The+number+of++children+cannot+be+greater+than+the+number+of+adults";
		String msn3 = "Reservation+Code+is+required";
		String msn4 = "Last+name+is+required";
		String tipoviaje = "2";
		// from
		String origen = param.getDep();
		// to
		String destino = param.getArr();
		// 去日期
		String fecIda = formatDate(param.getDepDate());
		// 返航日期
		String fecVuelta = formatDate(param.getRetDate());
		String horaIda = "";
		String fechaida = "";
		String horaVuelta = "";
		String fechaVuelta = "";
		String adultos = "1";
		String menores = "0";
		String bebes = "0";
		String cabina = "Y";
		String botnName = "BotÃ³n";
		String botn = "Search";

		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("msn1", msn1);
		map.put("msn2", msn2);
		map.put("msn3", msn3);
		map.put("msn4", msn4);
		map.put("tipoviaje", tipoviaje);
		// from
		map.put("origen", origen);
		// to
		map.put("destino", destino);
		// 去的日期
		map.put("fecIda", fecIda);
		// 返航日期
		map.put("fecVuelta", fecVuelta);
		map.put("horaIda", horaIda);
		map.put("fechaida", fechaida);
		map.put("horaVuelta", horaVuelta);
		map.put("fechaVuelta", fechaVuelta);
		map.put("adultos", adultos);
		map.put("menores", menores);
		map.put("bebes", bebes);
		map.put("cabina", cabina);
		map.put(botnName, botn);
		return map;
	}

	/**
	 * 日期转换
	 * 
	 * @param depDate
	 * @return
	 */
	private String formatDate(String depDate) {
		SimpleDateFormat ySdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat mSdf = new SimpleDateFormat("dd/MM/yyyy");
		String s = "";
		try {
			s = mSdf.format(ySdf.parse(depDate));
			// s = s.replaceAll("/", "%2F");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return s;
	}

	public static void main(String[] args) {
		FlightSearchParam searchParam = new FlightSearchParam();
		// 有票
		String dep = "LPB";
		String vvi = "VVI";
		// 没有票
		// String dep = "UYU";
		// String vvi = "TDD";
		// 没有提供该航班
		// String dep = "UYU";
		// String vvi = "HKT";

		searchParam.setDep(dep);
		searchParam.setArr(vvi);
		searchParam.setDepDate("2014-08-12");
		searchParam.setRetDate("2014-09-18");
		searchParam.setTimeOut("60000");
		searchParam.setToken("");
		// 测试抓取网页 开始
		 String html = new Wrapper_gjsairz8001().getHtml(searchParam);
		// System.out.println(html);
		// 测试抓取网页 结束
		// if (1 == 1) {
		// return;
		// }
		// 测试解析网页 开始
		ProcessResultInfo result = new Wrapper_gjsairz8001().process(html,
				searchParam);
		if (result.isRet() && result.getStatus().equals(Constants.SUCCESS)) {
			List<RoundTripFlightInfo> flightList = (List<RoundTripFlightInfo>) result
					.getData();
			for (RoundTripFlightInfo in : flightList) {
				System.out.println("************" + in.getInfo().toString());
				System.out.println("************" + in.getRetinfo().toString());
				System.out.println("++++++++++++" + in.getDetail().toString());
			}
		} else {
			System.out.println(result.getStatus());
		}
		System.out.println("");
		// 测试解析网页 结束
	}

}
