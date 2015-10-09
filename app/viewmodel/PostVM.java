package viewmodel;

import org.codehaus.jackson.annotate.JsonProperty;

import models.Post;
import models.User;

public class PostVM extends PostVMLite {
	@JsonProperty("createdDate") public Long createdDate;
	@JsonProperty("updatedDate") public Long updatedDate;
	@JsonProperty("ownerId") public Long ownerId;
	@JsonProperty("ownerName") public String ownerName;
	@JsonProperty("ownerNumFollowers") public Long ownerNumFollowers;
	@JsonProperty("body") public String body;
	@JsonProperty("categoryId") public Long categoryId;
	@JsonProperty("categoryName") public String categoryName;
	@JsonProperty("categoryIcon") public String categoryIcon;
	@JsonProperty("categoryType") public String categoryType;	
    
	@JsonProperty("isOwner") public boolean isOwner = false;
	@JsonProperty("isFollowingOwner") public boolean isFollowingOwner = false;
    
	@JsonProperty("deviceType") public String deviceType;
	
    public PostVM(Post post, User user) {
    	super(post, user);
        this.ownerId = post.owner.id;
        this.ownerName = post.owner.displayName;
        this.ownerNumFollowers = post.owner.numFollowers;
        this.createdDate = post.getCreatedDate().getTime();
        this.updatedDate = post.getUpdatedDate().getTime();
        this.body = post.body;
        this.categoryType = post.category.categoryType.toString();
        this.categoryName = post.category.name;
        this.categoryIcon = post.category.icon;
        this.categoryId = post.category.id;

        this.isOwner = (post.owner.id == user.id);
        this.isFollowingOwner = user.isFollowing(post.owner);
        
        this.deviceType = post.deviceType == null? "" : post.deviceType.name();
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Long getOwnerNumFollowers() {
        return ownerNumFollowers;
    }

    public void setOwnerNumFollowers(Long ownerNumFollowers) {
        this.ownerNumFollowers = ownerNumFollowers;
    }
    
    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public Long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Long updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPostType() {
        return postType;
    }

    public void setType(String postType) {
        this.postType = postType;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public boolean isOwner() {
        return isOwner;
    }

    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }
    
    public boolean isFollowingOwner() {
        return isFollowingOwner;
    }

    public void setIsFollowingOwner(boolean isFollowingOwner) {
        this.isFollowingOwner = isFollowingOwner;
    }
}