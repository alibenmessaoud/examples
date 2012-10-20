package com.paremus.example.datanucleus.service;

import java.io.PrintStream;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.felix.service.command.Descriptor;
import org.datanucleus.samples.jdo.tutorial.Product;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

@Component(
		provide = Object.class,
		properties = {
			"osgi.command.scope=product",
			"osgi.command.function=create|list"
		})
public class ExampleComponent {

	private PersistenceManagerFactory pmf;
	private PersistenceManager pm;
	
	@Reference
	public void setPMFBuilder(PersistenceManagerFactory pmf) {
		this.pmf = pmf;
	}
	
	@Activate
	public void activate() throws Exception {
		pm = pmf.getPersistenceManager();
	}
	
	@Deactivate
	public void deactivate() {
		pm.close();
	}

	@Descriptor("Create a new product and save it in the database.")
	public void create(@Descriptor("Product name") String name, double price) {
		Transaction tx = pm.currentTransaction();
		try {
			tx.begin();
		    Product product = new Product(name, "Description not provided", price);
		    pm.makePersistent(product);
		    tx.commit();
	    } finally {
			if (tx.isActive()) tx.rollback();
		}
	}

	@Descriptor("List existing products in the database.")
	public void list() {
		PrintStream out = System.out;
		out.println("Querying products");
		
		Query query = pm.newQuery("SELECT FROM " + Product.class.getName() + " ORDER BY price ASC");
		@SuppressWarnings("unchecked")
		List<Product> products = (List<Product>) query.execute();
		out.printf("Listing %d products in database.\n", products.size());
		
		for (Product product : products) {
			out.printf("ID=%d, NAME=%s, PRICE=%.2f\n", product.getId(), product.getName(), product.getPrice());
		}
		
	}

}