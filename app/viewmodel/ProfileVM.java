package viewmodel;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

import models.Location;
import models.PrivacySettings;
import models.User;

public class ProfileVM {
	
    @JsonProperty("dn")	 public String displayName;
    @JsonProperty("yr")  public String birthYear;
    @JsonProperty("gd")  public String gender;
    @JsonProperty("a")   public String aboutMe;
    @JsonProperty("loc") public Location location;
    @JsonProperty("n_fr") public long nofollowers;
    @JsonProperty("n_fn") public long nofollowing;
    @JsonProperty("n_p") public long noProducts;
    @JsonProperty("n_c") public long noCollection;
    @JsonProperty("id")  public long id;

    // admin readonly fields
    @JsonProperty("n")  public String name;
    @JsonProperty("mb")  public boolean mobileSignup;
    @JsonProperty("fb")  public boolean fbLogin;
    @JsonProperty("vl")  public boolean emailValidated;
    @JsonProperty("em")  public String email;
    
    @JsonProperty("ilu")  public boolean isLoggedinUser = false;
    @JsonProperty("ifu")  public boolean isFollowdByUser = false;
    
    public static ProfileVM profile(User user, User localUser) {
        ProfileVM vm = new ProfileVM();
        vm.displayName = user.displayName;
        
        if(user.id == localUser.id){
        	vm.isLoggedinUser = true;
        	vm.isFollowdByUser = false;
        }
        if(user.userInfo != null) {
        	vm.birthYear = user.userInfo.birthYear;
			if(user.userInfo.gender != null) {
				vm.gender = user.userInfo.gender.name();
			}
			vm.aboutMe = user.userInfo.aboutMe;
			vm.location = user.userInfo.location;
		}
        
        vm.id = user.id;
        vm.noProducts = user.productCount;
        vm.noCollection = user.collectionCount;
        vm.nofollowers = user.followersCount;
        vm.nofollowing = user.followingCount;
        vm.isFollowdByUser = user.isFollowedBy(localUser);
        return vm;
    }
}
