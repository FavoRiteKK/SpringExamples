package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfiguration.class);

	@Autowired
	AccountRepository accountRepository;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {

        logger.info("Inside WebSecurityConfiguration.init()");

		auth.userDetailsService(userDetailsService());
	}

	@Bean
	UserDetailsService userDetailsService() {

        logger.info("Inside WebSecurityConfiguration.userDetailsService()");

		return username -> {

            logger.info("Inside UserDetailsService.loadUserByUsername()");

            return accountRepository
                    .findByUsername(username)
                    .map(a -> new User(a.username, a.password, true, true, true, true,
                            AuthorityUtils.createAuthorityList("USER", "write")))
                    .orElseThrow(
                            () -> new UsernameNotFoundException("could not find the user '"
                                    + username + "'"));
        };
	}
}