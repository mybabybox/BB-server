package babybox.events.listener;

import models.Activity;
import models.Post;
import models.User;
import models.Activity.ActivityType;
import babybox.events.map.SoldEvent;

import com.google.common.eventbus.Subscribe;

import common.cache.CalcServer;
import common.utils.StringUtil;

public class SoldEventListener {
	
	@Subscribe
    public void recordSoldEventInDB(SoldEvent map){
		Post post = (Post) map.get("post");
		User user = (User) map.get("user");
		if (post.onSold(user)) {
			CalcServer.removeFromCategoryQueues(post.id, post.category.id);
			
			/*
			// Need to query chat users as recipients
			Activity activity = new Activity(
					ActivityType.SOLD, 
					user.id,
					user.id,
					user.displayName,
					post.id,
					StringUtil.shortMessage(post.title));
	        activity.save();
	        */
		}
    }
}
