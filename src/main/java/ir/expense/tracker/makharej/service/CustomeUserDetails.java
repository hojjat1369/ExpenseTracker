package ir.expense.tracker.makharej.service;


import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.expense.tracker.makharej.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Setter
@Getter
public class CustomeUserDetails implements UserDetails {

	private Long id;
	private String username;
	@JsonIgnore
	private String password;
	private Collection<? extends GrantedAuthority> authorities;

	public CustomeUserDetails(Long id, String username, String password,
							  Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.authorities = authorities;
	}

	public static CustomeUserDetails build(User user) {
		return new CustomeUserDetails(
				user.getId(),
				user.getUsername(),
				user.getPassword(),
				new ArrayList<>());
	}

	@Override
	public boolean isAccountNonExpired() {

		return true;
	}

	@Override
	public boolean isAccountNonLocked() {

		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {

		return true;
	}

	@Override
	public boolean isEnabled() {

		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CustomeUserDetails user = (CustomeUserDetails) o;
		return Objects.equals(id, user.id);
	}
}
