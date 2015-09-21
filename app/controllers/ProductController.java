package controllers;

import static play.data.Form.form;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import babybox.shopping.social.exception.SocialObjectNotJoinableException;


import models.Category;
import models.Collection;
import models.Product;
import models.Resource;
import models.User;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import viewmodel.FeedProductVM;
import viewmodel.ProductInfoVM;
import viewmodel.UserVM;
import common.utils.HtmlUtil;
import common.utils.ImageFileUtil;

public class ProductController extends Controller{
	private static play.api.Logger logger = play.api.Logger.apply(ProductController.class);

	@Transactional
	public static Result createProduct() {
		final User localUser = Application.getLocalUser(session());
		if (!localUser.isLoggedIn()) {
			logger.underlyingLogger().error(String.format("[u=%d] User not logged in", localUser.id));
			return status(500);
		}

		DynamicForm dynamicForm = DynamicForm.form().bindFromRequest();
		Long catId = Long.parseLong(dynamicForm.get("catId"));
		Long price = Long.parseLong(dynamicForm.get("price"));
		
		String name = dynamicForm.get("title");
		String desc = dynamicForm.get("desc");
		
		Category category = Category.findById(catId);
		
//		switch(dynamicForm.get("productType")){
//		case "Product":
//			product.productType = ProductType.Product;
//			if(!dynamicForm.get("category").equals("")){
//				product.category = Category.findById(Long.parseLong(dynamicForm.get("category")));
//			}
//			break;
//		case "story":
//			product.productType = ProductType.Post;
//			break;
//		}
		
		List<FilePart> pictures = request().body().asMultipartFormData().getFiles();
		try {
			Product newProduct = localUser.createProduct(name, desc, category, price);
			if (newProduct == null) {
				return status(505, "Failed to create community. Invalid parameters.");
			}
			for(FilePart picture : pictures){
				String fileName = picture.getFilename();
				File file = picture.getFile();
				File fileTo = ImageFileUtil.copyImageFileToTemp(file, fileName);
				newProduct.addProductPhoto(fileTo);
			}
			return ok(Json.toJson(newProduct.id));
		} catch (SocialObjectNotJoinableException e) {
			logger.underlyingLogger().error("Error in createCommunity", e);
		} catch (IOException e) {
			logger.underlyingLogger().error("Error in createCommunity", e);
		}
		return status(500);
	}



	@Transactional
	public static Result createCollection() {
		final User localUser = Application.getLocalUser(session());
		DynamicForm form1 = DynamicForm.form().bindFromRequest();	
		Category category = Category.findById(Long.parseLong(form1.get("category")));
		Collection newCollection = localUser.createCollection(form1.get("name"), form1.get("description"), category);
		if (newCollection == null) {
			return status(505, "Failed to create Collection. Invalid parameters.");
		}
		return ok(Json.toJson(newCollection.id));
	}
	
	@Transactional
	public static Result addToCollection() {
		final User localUser = Application.getLocalUser(session());
		DynamicForm dynamicForm = DynamicForm.form().bindFromRequest();
		Long productId = Long.parseLong(dynamicForm.get("product_id"));
		Long collectionId = Long.parseLong(dynamicForm.get("collectionId"));
		Collection collection = null;
		if(collectionId == 0){
			String collectionName = dynamicForm.get("collectionName");
			collection = localUser.createCollection(collectionName);
		} else {
			collection = Collection.findById(collectionId);
		}
		collection.products.add(Product.findById(productId));
		return ok();
	}

	@Transactional
	public static Result getAllFeedProducts() {
		List<FeedProductVM> vms = new ArrayList<>();
		for(Product product : Product.getAllFeedProducts()) {
			FeedProductVM vm = new FeedProductVM(product);
			vm.isLiked = product.isLikedBy(Application.getLocalUser(session()));
			vms.add(vm);
		}
		return ok(Json.toJson(vms));
	}
	
	@Transactional
	public static Result getAllsimilarProducts() {
		List<FeedProductVM> vms = new ArrayList<>();
		for(Product product : Product.getAllFeedProducts()) {
			FeedProductVM vm = new FeedProductVM(product);
			vm.isLiked = product.isLikedBy(Application.getLocalUser(session()));
			vms.add(vm);
		}
		return ok(Json.toJson(vms));
	}

	@Transactional
	public static Result getProductImageById(Long id) {
		response().setHeader("Cache-Control", "max-age=604800");
		Resource resource = Resource.findById(id);
		if (resource == null || resource.getThumbnailFile() == null) {
			return ok();
		}
		return ok(resource.getThumbnailFile());
	}

	@Transactional
	public static Result getFullProductImageById(Long id) {
		response().setHeader("Cache-Control", "max-age=604800");
		Resource resource = Resource.findById(id);
		if (resource == null || resource.getRealFile() == null) {
			return ok();
		}
		return ok(resource.getRealFile());
	}

	@Transactional
	public static Result product(Long id) {
		final User localUser = Application.getLocalUser(session());
		return ok(views.html.babybox.web.product.render(Json.stringify(Json.toJson(getProductInfoVM(id))), Json.stringify(Json.toJson(new UserVM(localUser)))));
	}
	
	@Transactional
	public static Result getProductInfo(Long id) {
		return ok(Json.toJson(getProductInfoVM(id)));
	}
	

	public static ProductInfoVM getProductInfoVM(Long id) {
		Product product = Product.findById(id);
		ProductInfoVM vm = new ProductInfoVM(product, Application.getLocalUser(session()));
		return vm;
	}

	@Transactional
	public static Result likePost(Long id) {
		Product.findById(id).onLikedBy(Application.getLocalUser(session()));
		return ok();
	}

	@Transactional
	public static Result unlikePost(Long id) {
		Product.findById(id).onUnlikedBy(Application.getLocalUser(session()));
		return ok();
	}

	@Transactional
	public static Result newComment() {
		DynamicForm form = form().bindFromRequest();
		Long postId = Long.parseLong(form.get("postId"));
		String desc = HtmlUtil.convertTextToHtml(form.get("desc"));
		System.out.println(postId+"  "+desc);
		
		Product.findById(postId).onComment(Application.getLocalUser(session()), desc);
		return ok();
	}
	
	@Transactional
	public static Result onView(Long id) {
		Product.findById(id).onView(Application.getLocalUser(session()));
		return ok();
	}
	
}