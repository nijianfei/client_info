package com.clientInfo.csc;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@SpringBootApplication
public class CscApplication {

	public static void main(String[] args) {
		SpringApplication.run(CscApplication.class, args);
		String url = "https://img2.baidu.com/it/u=34122897,3905758245&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=500";
		String dir = "d://202111";
		String fileName = "test.jpg";
		downloadHttpUrl(url, dir, fileName);
		System.out.println("完成");
	}


	/**
	 * 下载文件---返回下载后的文件存储路径
	 *
	 * @param url 文件地址
	 * @param dir 存储目录
	 * @param fileName 存储文件名
	 * @return
	 */
	public static void downloadHttpUrl(String url, String dir, String fileName) {
		try {
			File dirfile = new File(dir);
			if (!dirfile.exists()) {
				dirfile.mkdirs();
			}

			SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(
					SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
					NoopHostnameVerifier.INSTANCE);
			CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(scsf).build();
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = client.execute(httpget);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();

			int cache = 10 * 1024;
			FileOutputStream fileout = new FileOutputStream(dir+"/"+fileName);
			byte[] buffer = new byte[cache];
			int ch = 0;
			while ((ch = is.read(buffer)) != -1) {
				fileout.write(buffer, 0, ch);
			}
			is.close();
			fileout.flush();
			fileout.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
