package co.grandcircus.YelpFusion.Controller;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import co.grandcircus.YelpFusion.Model.*;
import co.grandcircus.YelpFusion.Service.*;

@Controller
public class ModelController {

	@Autowired
	HttpSession session;

	@Autowired
	private UserRepository urep;

	@Autowired
	private UserGroupRepository ugrep;

	@Autowired
	private YelpFusionService yfs;

	@Autowired
	private EventRepository erep;

	@Autowired
	private ActivityRepository arep;

	@Autowired
	private BusinessRepository brep;
	
	static Date todayDate = Calendar.getInstance().getTime(); 
	static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
	private static String todayString = formatter.format(todayDate);

	@PostMapping("/creategroup")
	public String creategroup(String email, String groupname, Model model) {
		User user = urep.findByEmail((String) session.getAttribute("useremail"));

		List<UserGroup> currentgroups = user.getUsergroup();
		List<User> memberlist = new ArrayList<User>();
		UserGroup ug = new UserGroup();
		String memberinput[] = email.split(";");

		ug.setGroupname(groupname);

		for (int i = 0; i < memberinput.length; i++) {
			System.out.println(memberinput[0]);
			User memberuser = urep.findByEmail(memberinput[i]);
			if (memberuser != null) {
				List<UserGroup> currentmembergroups = memberuser.getUsergroup();
				currentmembergroups.add(ug);
				memberuser.setUsergroup(currentmembergroups);
				memberlist.add(memberuser);
			}
		}
		memberlist.add(user);
		currentgroups.add(ug);
		user.setUsergroup(currentgroups);
		ug.setUser(memberlist);
		ugrep.save(ug);
		urep.save(user);
		model.addAttribute("groups", user.getUsergroup());
		model.addAttribute("username", session.getAttribute("username"));

		return "/index";

	}

	@GetMapping("/groupdetails/{groupid}")
	public String groupdetails(@PathVariable("groupid") long id, Model model) {

		UserGroup ug = ugrep.findById(id).orElse(null);
		model.addAttribute("groupinfo", ug);
		model.addAttribute("event", ug.getEvents());
		model.addAttribute("todayString", todayString);
		return "groupinfo";
	}

	@PostMapping("/addmembers")
	public String addmembers(@RequestParam(value = "groupid") long groupid, String email, Model model) {
		
		UserGroup userGroup = ugrep.findById(groupid).get();
		String memberinput[] = email.split(";");
		List<User> memberlist = userGroup.getUser();
		
		for (int i = 0; i < memberinput.length; i++) {
			System.out.println(memberinput[0]);
			User memberuser = urep.findByEmail(memberinput[i]);
			if (memberuser != null) {
				List<UserGroup> currentmembergroups = memberuser.getUsergroup();
				currentmembergroups.add(userGroup);
				memberuser.setUsergroup(currentmembergroups);
				memberlist.add(memberuser);		
			}
		}
		ugrep.save(userGroup);
		model.addAttribute("groupinfo", userGroup);
		model.addAttribute("event", userGroup.getEvents());
		model.addAttribute("userid", session.getAttribute("userid"));
		model.addAttribute("todayString", todayString);
		return "groupinfo";
	}
	
	@PostMapping("/createevent")
	public String createevent(String pricerange, @RequestParam(value = "groupid") long groupid, Event event,
			@RequestParam(value = "category") List<String> category, Model model) {
		User user = urep.findByEmail((String) session.getAttribute("useremail"));
		List<Business> listofBusiness = new ArrayList<>();
		List<Activity> activitylist = new ArrayList<>();
		BusinessResponse bs = null;
		UserGroup groupinfo = ugrep.findById(groupid).get();
		System.out.println("groupinfo" + groupinfo);
		event.setUsergroup(groupinfo);
		event.setEventadmin(user.getId());

		erep.save(event);
		// The API call uses the param pricerange="1,2,3,4"
		// or as a single number eg. pricerange=3
		if (pricerange.equals("1")) {
			pricerange = "1";
		} else if (pricerange.equals("2")) {
			pricerange = "1,2";
		} else if (pricerange.equals("3")) {
			pricerange = "1,2,3";
		} else {
			pricerange = "1,2,3,4";
		}

		System.out.println("pricerange" + pricerange);
		for (String c : category) {
			bs = yfs.getBusinesses(event.getEventcity(), c, pricerange);
			if(bs != null) {
			Activity activity = new Activity();
			activity.setActivityname(c);
			activity.setBusiness(bs.getBusinesses());
			activitylist.add(activity);
			activity.setEvent(event);
			arep.save(activity);
			
			for (Business business : bs.getBusinesses()) {
				listofBusiness.add(business);
				business.setActivity(activity);
				brep.save(business);
			}
		}
		}
		model.addAttribute("groupinfo", groupinfo);
		model.addAttribute("event", groupinfo.getEvents());
		model.addAttribute("todayString", todayString);
		model.addAttribute("userid",session.getAttribute("userid"));
		return "groupinfo";
	}

	@GetMapping("/eventdetails")
	public String eventdetails(@RequestParam(value = "event") long id, @RequestParam(value = "group") long groupid,Model model) {
		System.out.println("inside eventdetails");
		Event e = erep.findById(id).orElse(null);
		System.out.println("event " + e);
		model.addAttribute("event", e);
		model.addAttribute("groupid",groupid);
		return "eventdetails";
	}

	@PostMapping("/savevotes")
	public String savevotes(@RequestParam(value = "eventid") long eventid,long groupid,
			@RequestParam(required = false) String restaurants_favorite,
			@RequestParam(required = false) String restaurants_notfavorite,
			@RequestParam(required = false) String parks_favorite,
			@RequestParam(required = false) String parks_notfavorite, Model model) {

		Event event = erep.findById(eventid).get();
		List<Activity> activitylist = event.getActivity();
		String favbusinessname = "";
		String nfavbusinessname = "";
		for (Activity activity : activitylist) {
			String activityname = activity.getActivityname();
			if (activityname.equals("restaurants")) {
				favbusinessname = restaurants_favorite;
				nfavbusinessname = restaurants_notfavorite;
			} else if (activityname.equals("parks")) {
				favbusinessname = parks_favorite;
				nfavbusinessname = parks_notfavorite;
			}
			if ((activityname + "_favorite") != null || (activityname + "_notfavorite") != null) {
				List<Business> businesslist = activity.getBusiness();
				for (Business business : businesslist) {

					if (business.getName().equals(favbusinessname) && (favbusinessname) != null) {
						System.out.println("inside favs");
						business.setFavourite(business.getFavourite() + 1);
					}
					if (business.getName().equals(nfavbusinessname) && (nfavbusinessname) != null) {
						System.out.println("inside notfavs");
						business.setNotfavourite(business.getNotfavourite() + 1);
					}
					brep.save(business);
				}
			}
		}
		model.addAttribute("groupid",groupid);
		model.addAttribute("event", event);
		return "eventdetails";
	}
	
	@GetMapping("/delete")
	public String deleteevent(@RequestParam(value = "eventdetails") long id,@RequestParam(value = "group") long groupid,Model model) {
		UserGroup usergroup = ugrep.findById(groupid).get();
		Event event = erep.findById(id).get();
		List<Activity> activitylist = event.getActivity();
		for(Activity activity : activitylist)
		{
			if(activity != null) {
			List<Business> businesslist = activity.getBusiness();
			for(Business business: businesslist)
			{
				if(business!=null)
				brep.deleteById(business.getBusinessid());
			}
			}
			arep.deleteById(activity.getActivityid());
	}
	
		erep.deleteById(id);
		model.addAttribute("todayString", todayString);
		model.addAttribute("groupinfo",usergroup);
		model.addAttribute("event", usergroup.getEvents());
		model.addAttribute("userid",session.getAttribute("userid"));
		return "groupinfo";
	}

}
