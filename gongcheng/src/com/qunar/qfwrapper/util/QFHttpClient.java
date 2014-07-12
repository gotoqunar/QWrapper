package com.qunar.qfwrapper.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import main.java.MySecureProtocolSocketFactory;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.exception.QFHttpClientException;

public class QFHttpClient extends HttpClient {

	private int TIME_OUT = 60000;
	private FlightSearchParam flightSearchParam = null;
	private Logger wrapperHttpInfo = LoggerFactory.getLogger("wrapperHttpInfo");

	public QFHttpClient(FlightSearchParam flightSearchParam,
			boolean ifAddConnectionCloseHeader) {
		this.flightSearchParam = flightSearchParam;
		init(flightSearchParam.getWrapperid(), ifAddConnectionCloseHeader,
				flightSearchParam.getTimeOut(), flightSearchParam.getToken());
		ProxyHost proxy = new ProxyHost("127.0.0.1", 8888);
		this.getHostConfiguration().setProxyHost(proxy);
		Protocol myhttps = new Protocol("https", new MySecureProtocolSocketFactory (), 443);
		this.getHostConfiguration().setHost("www.whatever.com", 443, myhttps);
	}

	private void init(String codebase, boolean ifAddConnectionCloseHeader,
			String timeout, String token) {
		if (null != timeout && timeout.length() > 0) {
			this.TIME_OUT = Integer.parseInt(timeout);
		}

		Collection<Header> headers = new ArrayList<Header>();
		headers.add(new Header("Accept", "*/*"));
		headers.add(new Header("Accept-Language", "zh-cn"));
		headers.add(new Header("User-Agent",
				"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0)"));
		headers.add(new Header("UA-CPU", "x86"));
		headers.add(new Header("Accept-Encoding", "gzip, deflate"));
		if (ifAddConnectionCloseHeader) {
			headers.add(new Header("Connection", "close"));
		}

		this.getHttpConnectionManager().getParams()
				.setConnectionTimeout(TIME_OUT);
		this.getHttpConnectionManager().getParams().setSoTimeout(TIME_OUT);

		this.getParams().setParameter("http.default-headers", headers);
	}

	public int executeMethod(HttpMethod method) throws IOException,
			HttpException, QFHttpClientException {
		URI uri = method.getURI();
		int status = super.executeMethod(method);
		if (!StringUtils.isEmpty(flightSearchParam.getWrapperid())) {
			wrapperHttpInfo.info("queryId: " + flightSearchParam.getQueryId()
					+ "WrapperID: " + flightSearchParam.getWrapperid()
					+ ", URL: " + uri + ", Method: " + method.getName()
					+ ", Status: " + status);
		}
		if (status >= 400) {
			wrapperHttpInfo.warn("抓取失败，HTTP返回状态：" + status);
			throw new QFHttpClientException(status);
		}

		return status;
	}
}
