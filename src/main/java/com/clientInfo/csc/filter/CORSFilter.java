//package com.clientInfo.csc.filter;
//
//
//import com.sun.net.httpserver.Filter;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.ServletRequest;
//import jakarta.servlet.ServletResponse;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//public class CORSFilter implements Filter {
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws IOException, ServletException {
//        HttpServletResponse rsp = (HttpServletResponse) response;
//        rsp.addHeader("Access-Control-Allow-Credentials", "true");
//        rsp.addHeader("Access-Control-Allow-Origin", "*");
//        rsp.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
//        rsp.addHeader("Access-Control-Allow-Headers", "Content-Type,X-CAF-Authorization-Token,sessionToken,X-TOKEN");
//
//        chain.doFilter(request, response);
//    }
//
