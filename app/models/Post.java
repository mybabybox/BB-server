package models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Query;

import play.db.jpa.JPA;
import babybox.shopping.social.exception.SocialObjectNotCommentableException;
import common.cache.CalcServer;
import common.utils.StringUtil;
import controllers.Application.DeviceType;
import domain.Commentable;
import domain.DefaultValues;
import domain.Likeable;
import domain.PostType;
import domain.SocialObjectType;

@Entity
public class Post extends SocialObject implements Likeable, Commentable {

	public String title;

	@Column(length=2000)
	public String body;

	@ManyToOne(cascade = CascadeType.REMOVE)
	public Folder folder;

	@ManyToOne(cascade=CascadeType.REMOVE)
	public Collection collection;

	@ManyToOne
	public Category category;

	@Enumerated(EnumType.STRING)
	public PostType postType;
	
	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	@OrderBy("CREATED_DATE")
	public List<Comment> comments;

	public Double price = 0.0;

	public boolean sold;
	
	public int noOfComments = 0;
	public int noOfLikes = 0;
	public int noOfBuys = 0;
	public int noOfViews = 0;
	public int noOfChats = 0;
	public Long baseScore = 0L;

	public DeviceType deviceType;

	/**
	 * Ctor
	 */
	public Post() {}

	public Post(User owner, String title, String body, Category category) {
		this.owner = owner;
		this.title = title;
		this.body = body;
		this.category = category;
		this.price = 0.0;
		this.postType = PostType.STORY;
		this.objectType = SocialObjectType.POST;
	}

	public Post(User owner, String title, String body, Category category, Double price) {
		this.owner = owner;
		this.title = title;
		this.body = body;
		this.category = category;
		this.price = price;
		this.postType = PostType.PRODUCT;
		this.objectType = SocialObjectType.POST;
	}

	@Override
	public void onLikedBy(User user) {
		if(!isLikedBy(user)){
			boolean liked = recordLike(user);
			if (liked) {
				this.noOfLikes++;
				user.numLikes++;
				CalcServer.buildBaseScore();
			}
		}
	}

	@Override
	public void onUnlikedBy(User user) {
		if (isLikedBy(user)) {
			boolean unliked = 
					LikeSocialRelation.unlike(
							user.id, SocialObjectType.USER, this.id, SocialObjectType.POST);
			if (unliked) {
				this.noOfLikes--;
				user.numLikes--;
				CalcServer.buildBaseScore();
				CalcServer.removeFromLikeQueue(this.id, user.id);
			}
		}
	}
	
	@Override
	public boolean isLikedBy(User user){
		return CalcServer.isLiked(user.id, this.id);
	}

	@Override
	public void save() {
		super.save();

		if (!this.deleted) {
            switch(this.postType) {
            	case PRODUCT: {
                    recordPostProduct(owner);
                    owner.numProducts++;
                    break;
                }
                case STORY: {
                    recordPostStory(owner);
                    owner.numStories++;
                    break;
                }
            }
        } else {
            switch(this.postType) {
                case PRODUCT: {
                    owner.numProducts--;
                    break;
                }
                case STORY: {
                    owner.numStories--;
                    break;
                }
            }
        }
	}

	public List<Comment> getLatestComments(int count) {
		int start = Math.max(0, comments.size() - count);
		int end = comments.size();
		return comments.subList(start, end);
	}
	
	public List<Comment> getPostComments(Long offset) {
		double maxOffset = Math.floor((double) comments.size() / (double) DefaultValues.DEFAULT_INFINITE_SCROLL_COUNT);
		if (offset > maxOffset) {
			return new ArrayList<>();
		}
		
		int start = Long.valueOf(offset).intValue() * DefaultValues.DEFAULT_INFINITE_SCROLL_COUNT;
		int end = Math.min(start+DefaultValues.DEFAULT_INFINITE_SCROLL_COUNT, comments.size());
		return comments.subList(start, end);
	}
	
	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public void delete(User deletedBy) {
		this.deleted = true;
		this.deletedBy = deletedBy;
		save();
	}

	public Resource addPostPhoto(File source) throws IOException {
		ensureAlbumExist();
		Resource photo = this.folder.addFile(source, SocialObjectType.POST_PHOTO);
		photo.save();
		return photo;
	}

	public void ensureAlbumExist() {
		if (this.folder == null) {
			this.folder = Folder.createFolder(this.owner, "post-ps", "", true);
			this.merge();
		}
	}

	///////////////////// Query APIs /////////////////////
	public static Post findById(Long id) {
		try {
			Query q = JPA.em().createQuery("SELECT p FROM Post p where id = ?1 and deleted = false");
			q.setParameter(1, id);
			return (Post) q.getSingleResult();
		} catch (NoResultException nre) {
			return null;
		}
	}

	public static List<Post> getAllPosts() {
		try {
			Query q = JPA.em().createQuery("SELECT p FROM Post p where deleted = false");
			return (List<Post>) q.getResultList();
		} catch (NoResultException nre) {
			return null;
		}
	}

	public static List<Post> getUserPosts(Long id) {
		try {
			Query q = JPA.em().createQuery("SELECT p FROM Post p where owner = ? and deleted = false");
			q.setParameter(1, User.findById(id));
			return (List<Post>) q.getResultList();
		} catch (NoResultException nre) {
			return null;
		}
	}

	@Override
	public SocialObject onComment(User user, String body) {
		Comment comment = new Comment(this, user, body);
		comment.objectType = SocialObjectType.COMMENT;
		comment.save();

		// merge into Post
		if (comments == null) {
			comments = new ArrayList<>();
		}
		this.comments.add(comment);
		this.noOfComments++;
		JPA.em().merge(this);
		
        // record for notifications
        if (this.postType == PostType.PRODUCT) {
            recordCommentProduct(user, comment);
        } else if (this.postType == PostType.STORY) {
            recordCommentStory(user, comment);
        }
        CalcServer.buildBaseScore();
		return comment;
	}

	@Override
	public void onDeleteComment(User user, String body)
			throws SocialObjectNotCommentableException {
		// TODO Auto-generated method stub
		this.noOfComments--;
	}

	public void onView(User localUser) {
		this.recordView(localUser);
		CalcServer.buildBaseScore();
	}

	public static List<Post> getPostsByCategory(Category category) {
		try {
			Query q = JPA.em().createQuery("SELECT p FROM Post p where category = ? and deleted = false");
			q.setParameter(1,category);
			return (List<Post>) q.getResultList();
		} catch (NoResultException nre) {
			return null;
		}
	}

	public static List<Post> getPosts(List<Long> postIds) {
		try {
			 Query query = JPA.em().createQuery(
			            "select p from Post p where "+
			            "p.id in ("+StringUtil.collectionToString(postIds, ",")+") and "+
			            "p.deleted = false ORDER BY FIELD(p.id,"+StringUtil.collectionToString(postIds, ",")+")");
			 return (List<Post>) query.getResultList();
		} catch (NoResultException nre) {
			return null;
		}
	}
	
	public static List<Post> getPosts(List<Long> postIds, int offset) {
		try {
			 Query query = JPA.em().createQuery(
					 "select p from Post p where "+
							 "p.id in ("+StringUtil.collectionToString(postIds, ",")+") and "+
							 "p.deleted = false ORDER BY FIELD(p.id,"+StringUtil.collectionToString(postIds, ",")+")");
			 query.setFirstResult(offset * DefaultValues.DEFAULT_INFINITE_SCROLL_COUNT);
			 query.setMaxResults(DefaultValues.DEFAULT_INFINITE_SCROLL_COUNT);
			 return (List<Post>) query.getResultList();
		} catch (NoResultException nre) {
			return null;
		}
	}
	
	public List<Conversation> findConversations() {
		return Conversation.findPostConversations(this, this.owner, DefaultValues.CONVERSATION_COUNT);
	}
}
