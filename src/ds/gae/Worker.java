package ds.gae;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.apphosting.utils.config.ClientDeployYamlMaker.Request;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ds.gae.entities.Quote;
import ds.gae.entities.Log;
import ds.gae.servlets.ConfirmQuotesServlet;
import ds.gae.view.JSPSite;
import ds.gae.view.ViewTools;

public class Worker extends HttpServlet {
	private static final long serialVersionUID = -7058685883212377590L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String id = req.getParameter("key");
		String payload = req.getParameter("payload");
		String renter = req.getParameter("renter");

		Type wow = new TypeToken<ArrayList<Quote>>() {}.getType();
		ArrayList<Quote> qs = new Gson().fromJson(payload, wow);
		EntityManager em = EMF.get().createEntityManager();
		try {
			CarRentalModel.get().confirmQuotes(qs);
			for (Quote q : qs){
				Log lg = new Log(q.getCarRenter(), new Date(), q.toString());
				em.persist(lg);
			}
		} catch (ReservationException e) {
			Log lg = new Log(renter, new Date(), e.getMessage());
			em.persist(lg);
		} finally {
			em.close();
		}

	}
}
