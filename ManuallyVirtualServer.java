import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ManuallyVirtualServer extends HttpServlet {
	
	

	String cacheURL = "/usr/local/webserver/tomcat_8.0.3/webapps/test/WEB-INF/cache.json";
	String logURL = "/usr/local/webserver/tomcat_8.0.3/webapps/test/WEB-INF/log.json";

	String indexHtml = "<html> <meta http-equiv='content-type' content='text/html;charset=utf-8'> <body> <form action='http://test.drakeet.me/' method='POST'> 记录值:</br> <tr> <td></td> <td><textarea name='set' cols='40' rows='4' style='OVERFLOW: hidden'></textarea></td> </tr></br> <input type='submit' value='提交'> </form> <form action='http://test.drakeet.me/?getCache=yes' method='POST' target='_blank'> <input type='submit' value='获取缓存'> </form> </body> </html>";
	String indexHtml1 = "<html> <title>人工服务器 Beta1.1</title> <meta http-equiv='content-type' content='text/html;charset=utf-8'> <body> <form action='http://test.drakeet.me/' method='POST'> key:</br> <tr> <td></td> <td><input type='text' name='set'></input></td> </tr> </br>value:</br> <tr> <td></td> <td><textarea name='value' cols='40' rows='4' style='OVERFLOW: hidden'></textarea></td> </tr></br> <input type='submit' value='提交'> </form> <form action='http://test.drakeet.me/' method='GET' target='_blank'> key:</br> <tr> <td></td> <td><input type='text' name='key' value='";
	String indexHtml2 = "'></input></td> </tr> <input type='submit' value='获取缓存'> </form> </body> </html>";
	List<Request> mList = new ArrayList<Request>();
	RequestSet mSet = new RequestSet(mList);
	RequestSet mRequestSet;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, resp);

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//判断系统平台
		Properties prop = System.getProperties();
		String os = prop.getProperty("os.name");
		if (os.startsWith("win") || os.startsWith("Win")) {
			System.out.println(os);
			cacheURL = "C:/Users/asus/Desktop/cache.json";
			logURL = "C:/Users/asus/Desktop/log.json";
		}
		//获取ip
		String remoteIp = getIpAddr(req);
		
		// resp.getWriter().write("<a href='http://drakeet.me'>hehe</>");
		resp.setContentType("text/html;charset=utf-8");
		PrintWriter printWriter = resp.getWriter();
		String[] heheStrings = req.getParameterValues("get");
		String setString = req.getParameter("set");
		String valueString = req.getParameter("value");
		String keyString = req.getParameter("key");
		String getCache = req.getParameter("getCache");
		// req.getParameterNames() //遍历打印出名与值，待实现。

		if (heheStrings != null) {
			int t = heheStrings.length;
			int i = 0;
			printWriter.print("你要的是:");
			while (t-- != 0) {
				printWriter.print(heheStrings[i]);
				if (i != heheStrings.length - 1) {
					printWriter.print("/");
				}
				i++;
			}
			printWriter.flush();
		} else if (setString != null && valueString != null) {

			// setString = URLEncoder.encode(setString, "UTF-8");
			setString = new String(setString.getBytes("ISO-8859-1"), "UTF-8");
			
			valueString = new String(valueString.getBytes("ISO-8859-1"),
					"UTF-8");
			String look = readTextFile(cacheURL);
			if (look != null || !look.isEmpty() ) {
				mSet = new Gson().fromJson(look, RequestSet.class);
			}
			Request request = new Request();
			request.setKey(setString);
			request.setValue(valueString);
			mSet.add(request);
			// 写入json到文件
			Gson gson = new Gson();
			System.out.println("写入json到文件结果："
					+ writeTextFile(gson.toJson(mSet), cacheURL, false));
			System.out.println(mSet);
			// 刷新设置页面
			printWriter.print("欢迎！你可以通过预设http返回内容，然后通过“获取缓存”得到一个新的页面，</br>"
					+ "页面地址是固定的，因此你可以通过这个url地址获得你预设的返回内容，作为虚拟服务。</br>&nbsp;");
			printWriter.print(indexHtml1 + setString + indexHtml2);
			printWriter.print("成功存入缓存：</br>key=" + setString + "</br>value="
					+ valueString);

		} else if (keyString != null) {

			keyString = new String(keyString.getBytes("ISO-8859-1"), "UTF-8");
			String jsonString = readTextFile(cacheURL);

			mRequestSet = new Gson().fromJson(jsonString, RequestSet.class);
			Request temprRequest = mRequestSet.getItem(keyString);
			System.out.println("temprRequest:" + temprRequest);
			// 记录日志
			if (temprRequest != null) {
				writeTextFile(remoteIp + "\nkey=" + temprRequest.getKey() + "\n" + "value="
						+ temprRequest.getValue() + "\n\n", logURL, true);
				printWriter.print(temprRequest.getValue());
			} else {
				//否则刷新页面
				printWriter.print("欢迎！你可以通过预设http返回内容，然后通过“获取缓存”得到一个新的页面，</br>"
						+ "页面地址是固定的，因此你可以通过这个url地址获得你预设的返回内容，作为虚拟服务。</br>&nbsp;");
				printWriter.print(indexHtml1 + indexHtml2);
				printWriter.print("找不到此key值！请重新输入或提交建立此key-value");
			}
		} else {
			printWriter.print("欢迎！你可以通过预设http返回内容，然后通过“获取缓存”得到一个新的页面，</br>"
					+ "页面地址是固定的，因此你可以通过这个url地址获得你预设的key对应的value内容，作为虚拟服务。</br>&nbsp;");
			printWriter.print(indexHtml1 + indexHtml2);
			printWriter.flush();
			printWriter.close();
		}

	}

	public String readTextFile(String url) {
		BufferedReader reader = null;
		StringBuffer stringBuffer = null;
		try {
			reader = new BufferedReader(new FileReader(url));
			String s = reader.readLine();
			stringBuffer = new StringBuffer();
			while (s != null) {
				stringBuffer.append(s);
				s = reader.readLine();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (stringBuffer == null) {
			return null;
		}
		return stringBuffer.toString();
	}

	/**
	 * 从writeWhat读取数据写入本地文件
	 * 
	 * @author drakeet
	 * 
	 */
	public boolean writeTextFile(String writeWhat, String url, boolean isXuXie)
			throws IOException {
		// 输入流
		InputStream in = new ByteArrayInputStream(writeWhat.getBytes());
		// 输出流
		OutputStream out = new FileOutputStream(url, isXuXie);

		try {
			byte[] buffer = new byte[1024];
			while (true) {
				int byteRead = in.read(buffer);
				if (byteRead == -1)
					break;
				out.write(buffer, 0, byteRead);
			}
		}

		catch (MalformedURLException e) {
			e.printStackTrace();
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	
	/**
	 * 获取Ip地址
	 * @return
	 */
	public String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		System.out.println("hn:" + request.getHeaderNames());
		
		
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}



}
