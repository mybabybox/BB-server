package babybox.events.listener;

import java.util.Date;

import models.Activity;
import models.User;
import models.Activity.ActivityType;
import babybox.events.map.FollowEvent;
import babybox.events.map.UnFollowEvent;

import com.google.common.eventbus.Subscribe;

import common.cache.CalcServer;

public class FollowEventListener {
	
	@Subscribe
    public void recordFollowEventInDB(FollowEvent map){
		User localUser = (User) map.get("localUser");
		User user = (User) map.get("user");
		
       	// why we require this, if we are renewing HOME_FOLLOWING feed after every 2 mins 
		if (localUser.onFollow(user)) {
			Long score = new Date().getTime();		// ideally use FollowSocialRelation.CREATED_DATE
			CalcServer.addToFollowQueue(localUser.id, user.id, score.doubleValue());
			
			Activity activity = new Activity(
					ActivityType.FOLLOWED, 
					user.id,
					localUser.id,
					localUser.displayName,
					user.id,
					user.displayName);
			activity.save();
		}
    }
	
	@Subscribe
    public void recordUnFollowEventInDB(UnFollowEvent map){
		User localUser = (User) map.get("localUser");
		User user = (User) map.get("user");
		if (localUser.onUnFollow(user)) {
			CalcServer.removeFromFollowQueue(localUser.id, user.id);
		}
    }
}
