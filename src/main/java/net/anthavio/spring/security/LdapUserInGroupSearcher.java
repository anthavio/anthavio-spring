package net.anthavio.spring.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.naming.directory.SearchControls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.util.Assert;

/**
 * @author vanek
 * 
 *         Spring Security LDAP komponenta, ktera pro zadanou skupinu vrati
 *         seznam uzivatelu
 */
public class LdapUserInGroupSearcher {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private SearchControls searchControls = new SearchControls();

	private String groupSearchBase;

	private String groupSearchFilter = "cn={0}";

	private String groupReturnAttribute = "member";

	private String userSearchFilter = "(objectCategory=user)";

	private final ContextMapper userMapper;

	private SpringSecurityLdapTemplate ldapTemplate;

	public LdapUserInGroupSearcher(ContextSource contextSource, ContextMapper userMapper) {
		Assert.notNull(contextSource, "contextSource must not be null");
		ldapTemplate = new SpringSecurityLdapTemplate(contextSource);
		ldapTemplate.setSearchControls(searchControls);
		Assert.notNull(userMapper, "userMapper must not be null");
		this.userMapper = userMapper;
	}

	public List<String[]> findUsersInGroup(String groupname) {

		if (logger.isDebugEnabled()) {
			logger.debug("Searching for users in group '" + groupname + "', with filter "
					+ groupSearchFilter + " in search base '" + getGroupSearchBase() + "'");
		}

		Set<String> userDns = ldapTemplate.searchForSingleAttributeValues(getGroupSearchBase(),
				groupSearchFilter, new String[] { groupname }, groupReturnAttribute);

		if (logger.isDebugEnabled()) {
			logger.debug("Users DN from group membership search: " + userDns);
		}

		List<String[]> users = new ArrayList<String[]>(userDns.size());
		for (String userDn : userDns) {
			String[] record = findUserByDn(userDn);
			if (record != null) {
				users.add(record);
			}
		}

		return users;
	}

	@SuppressWarnings("unchecked")
	private String[] findUserByDn(String userDn) {
		List<String[]> records = ldapTemplate.search(userDn, userSearchFilter, searchControls,
				userMapper);
		String[] record;
		if (records.size() == 1) {
			record = records.iterator().next();
		} else if (records.size() > 1) {
			logger.warn("More than one record found for DN " + userDn);
			record = records.iterator().next();
			for (Object value : records) {
				logger.warn(String.valueOf(value));
			}
		} else {
			logger.warn("No record found for DN " + userDn);
			record = null;
		}
		return record;
	}

	public String getGroupSearchBase() {
		return groupSearchBase;
	}

	public void setGroupSearchBase(String groupSearchBase) {
		this.groupSearchBase = groupSearchBase;
	}

	public void setSearchSubtree(boolean searchSubtree) {
		int searchScope = searchSubtree ? SearchControls.SUBTREE_SCOPE : SearchControls.ONELEVEL_SCOPE;
		searchControls.setSearchScope(searchScope);
	}

	public String getGroupSearchFilter() {
		return groupSearchFilter;
	}

	public SearchControls getSearchControls() {
		return searchControls;
	}

	public void setSearchControls(SearchControls searchControls) {
		this.searchControls = searchControls;
	}

	public String getGroupReturnAttribute() {
		return groupReturnAttribute;
	}

	public void setGroupReturnAttribute(String returnAttribute) {
		this.groupReturnAttribute = returnAttribute;
	}

	public SpringSecurityLdapTemplate getLdapTemplate() {
		return ldapTemplate;
	}

	public void setLdapTemplate(SpringSecurityLdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public void setGroupSearchFilter(String searchFilter) {
		this.groupSearchFilter = searchFilter;
	}

	public String getUserSearchFilter() {
		return userSearchFilter;
	}

	public void setUserSearchFilter(String userSearchFilter) {
		this.userSearchFilter = userSearchFilter;
	}

	public static void main(String[] args) {
		try {
			DefaultSpringSecurityContextSource context = new DefaultSpringSecurityContextSource(
					"ldap://frogstar.komix.com:389/");
			context.setUserDn("KOMIX\\projectx");
			context.setPassword("kokosak");
			context.afterPropertiesSet();

			ContextMapper mapper = new ContextMapper() {
				public Object mapFromContext(Object ctx) {
					DirContextAdapter adapter = (DirContextAdapter) ctx;
					String ident = adapter.getStringAttribute("sAMAccountName").toLowerCase();
					String email = adapter.getStringAttribute("mail").toLowerCase();
					return new String[] { ident, email };
				}
			};

			LdapUserInGroupSearcher ldapSearcher = new LdapUserInGroupSearcher(context, mapper);
			ldapSearcher.setSearchSubtree(true);
			ldapSearcher.setGroupSearchBase("OU=Uzivatele,DC=komix,DC=com");
			ldapSearcher.setGroupSearchFilter("(&(objectCategory=group)(sAMAccountName={0}))");

			List<String[]> usersInGroup = ldapSearcher.findUsersInGroup("OD-IS-BlHr");
			for (String[] string : usersInGroup) {
				System.out.println(string[0] + "," + string[1]);
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

}
