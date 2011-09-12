/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jscl.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jscl.converter.Converter;

/**
 *
 * @author raphael
 */
public class Processor extends HttpServlet {

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
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
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			PrintWriter writer = resp.getWriter();
			Writer w = new StringWriter();
			pipe(reader, w);
			w.close();
			String str = Converter.convert(w.toString(), "/mathmlc2p.xsl", title, source ? url.toString() : null);
			Reader r = new StringReader(str);
			pipe(r, writer);
			r.close();
			reader.close();
			writer.close();
		}
	}

	static void pipe(Reader in, Writer out) throws IOException {
		while (true) {
			int c = in.read();
			if (c == -1) {
				break;
			}
			out.write(c);
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>
}
