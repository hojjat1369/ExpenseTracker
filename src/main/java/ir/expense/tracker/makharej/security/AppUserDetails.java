package ir.expense.tracker.makharej.security;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;


/**
 * The authenticated principal: a Spring Security user that also knows its database id, so controllers can tell
 * who is calling without another lookup.
 */
public class AppUserDetails extends User {

	private final Long id;

	public AppUserDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {

		super(username, password, authorities);
		this.id = id;
	}

	public Long getId() {

		return id;
	}
}
