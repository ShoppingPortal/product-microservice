package com.cisco.microservices.products;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * RESTful Client controller, fetches Product info from the microservice via
 * {@link productDao}.
 * 
 * @author Sandip Bastapure
 */
@RestController
@RequestMapping("/product")
public class ProductController {

	protected Logger logger = Logger.getLogger(ProductController.class.getName());

	@Autowired
	private ProductDAO productDao;

	@Autowired
	public ProductController(ProductDAO productDao) {
		logger.info("UserRepository says system has " + productDao.count() + " users");
	}

	/**
	 * This method is used to add product with .
	 * 
	 * @param args
	 *            Product : productName, Description
	 */
	@RequestMapping(value = "/addProduct", method = RequestMethod.POST, consumes = "application/json")
	public Response addProduct(@RequestBody Product product) {
		String productName = product.getProductName();
		List<Product> productList = new ArrayList<Product>();
		Response res = new Response();
		logger.info("Product's addProduct() invoked for :" + productName);

		if (productName == "") {
			res.setStatus("400");
			res.setDescription("Invalid productName!");
			ProductError err = new ProductError();
			err.setMsg("productName can not be blank!");
			err.setParam("productName");
			err.setValue(productName);
			res.setError(err);
			logger.info("productName can not be blank!");
		} else if (validateProductName(productName)) {
			res.setStatus("400");
			res.setDescription("Product already exist!");
			ProductError err = new ProductError();
			err.setMsg("Product already exist");
			err.setParam("productName");
			err.setValue(productName);
			res.setError(err);
			System.out.println("Product already exist");
			logger.info("Product already exist!");
		} else {

			try {
				Product p = new Product();
				p.setProductName(productName);
				p.setDescription(product.getDescription());
				p.setAddedDate(new Date());
				Product addedProduct = productDao.save(p);
				logger.info("Product added!");
				productList.add(addedProduct);
				res.setStatus("200");
				res.setData(productList);
				res.setDescription("Product added successfully!");
				logger.info("Product added successfully!");

			} catch (ConstraintViolationException e) {
				res.setStatus("400");
				res.setDescription("Server error!");
				ProductError err = new ProductError();
				err.setMsg("Something went wrong");
				err.setParam("productName");
				err.setValue(productName);
				res.setError(err);
				logger.info("Something went wrong!");
			}
		}
		return res;
	}

	/**
	 * Updates product info with below params
	 * 
	 * @param args
	 *            Product : productName, Description
	 */
	@RequestMapping(value = "/updateProduct", method = RequestMethod.POST, consumes = "application/json")
	public Response updateProduct(@RequestBody Product product) {
		String productName = product.getProductName();
		List<Product> productList = new ArrayList<Product>();
		Response res = new Response();
		logger.info("Product's updateProduct() invoked for :" + productName);

		if (validateProductName(productName)) {
			try {
				Product p = new Product();
				p.setProductName(productName);
				p.setDescription(product.getDescription());
				p.setAddedDate(new Date());
				int isUpdated = productDao.updateProduct(product.getDescription(), new Date(), productName);
				if (isUpdated == 1) {
					res.setStatus("200");
					res.setDescription("Product Updated successfully!");
					logger.info("Product updated successfully!" + isUpdated);
				}
				productList.add(p);
				res.setData(productList);
			} catch (Exception e) {
				logger.info("Exception in the updateProduct()");
				e.printStackTrace();
				res.setStatus("400");
				res.setDescription("Server error!");
				ProductError err = new ProductError();
				err.setMsg("Something went wrong");
				err.setParam("productName");
				err.setValue(productName);
				res.setError(err);
			}

		} else {
			res.setStatus("400");
			res.setDescription("Invalid product id");
			ProductError err = new ProductError();
			err.setMsg("Product does not exists");
			err.setParam("productName");
			err.setValue(productName);
			res.setError(err);
			logger.info("Product does not exists!");
		}
		return res;
	}

	/**
	 * Deletes product of given id
	 * 
	 * @param args
	 *            Product : productId
	 */
	@RequestMapping(value = "/delete/{productId}", method = RequestMethod.GET)
	public Response deleteProduct(@PathVariable("productId") Long productId) {
		logger.info("Product deleteProduct() invoked for: " + productId);
		Response res = new Response();
		List<Product> list = new ArrayList<Product>();
		System.out.println(productId);
		Product deletedProduct = new Product();
		deletedProduct.setId(productId);
		list.add(deletedProduct);
		try {
			int isDeleted = productDao.deleteById(productId);
			if (isDeleted == 1) {
				res.setStatus("200");
				res.setDescription("Product deleted successfully!");
				res.setData(list);
				logger.info("Product deleted successfully");
			} else {
				res.setStatus("400");
				res.setDescription("Invalid product id");
				ProductError err = new ProductError();
				err.setMsg("Product does not exists");
				err.setParam("productId");
				err.setValue("" + productId);
				res.setError(err);
				logger.info("Product does not exists");
			}
			System.out.println(isDeleted);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Fetch all list of all products
	 * 
	 * @param args
	 *            NA
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	private Response list() {
		Response res = new Response();
		List<Product> productList = productDao.findAll();
		if (productList.size() <= 0) {
			res.setStatus("200");
			res.setDescription("OOPs! it seems there are no product's added yet!");
			logger.info("there are no product's added yet");
		} else {
			res.setStatus("200");
			res.setDescription("This is the product's list");
			logger.info("fetched product's list");
		}
		res.setData(productList);
		return res;
	}

	/**
	 * Verify whether the product already added or not
	 * 
	 * @param args
	 *            NA
	 */
	private boolean validateProductName(String productName) {
		List<Product> product = null;
		try {
			System.out.println(productName);
			product = productDao.findByProductName(productName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (product.size() <= 0) {
			return false;
		} else {
			return true;
		}
	}
}
