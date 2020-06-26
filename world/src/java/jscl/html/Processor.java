package jscl.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jscl.converter.Converter;

public class Processor extends HttpServlet {
	private final Converter converter = new Converter();

	protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String uri = req.getRequestURI().substring(req.getContextPath().length());
		String title = uri.substring(1, uri.lastIndexOf("."));
		boolean source = "true".equals(getInitParameter("source"));
		URL url = new URL(getInitParameter("target") + uri);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.addRequestProperty("Cache-Control", "no-cache,max-age=0");
		conn.addRequestProperty("Pragma", "no-cache");
		int status = conn.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) {
			resp.sendError(status, url.toString());
		} else {
			resp.setContentType("text/xml; charset=utf-8");
			try (final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8")); final PrintWriter writer = resp.getWriter()) {
				converter.apply(reader, "/mathmlc2p.xsl", title, null, null, source ? url.toString() : null, false, writer);
			}
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>
}
