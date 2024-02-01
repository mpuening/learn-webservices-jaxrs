package io.github.learnjaxrs.util.security.identity;

import io.github.learnjaxrs.util.env.Environment;

public class EnvConfiguredLDAPCredentialValidator extends LDAPCredentialValidator {

	public EnvConfiguredLDAPCredentialValidator(
			Environment environment,
			String ldapUrl,
			String userBaseDn,
			String userNameAttribute,
			String groupBaseDn,
			String groupMemberAttribute,
			String groupNameAttribute) {
		super(
				environment.getProperty("ldapUrl", ldapUrl),
				environment.getProperty("userBaseDn", userBaseDn),
				environment.getProperty("userNameAttribute", userNameAttribute),
				environment.getProperty("groupBaseDn", groupBaseDn),
				environment.getProperty("groupMemberAttribute", groupMemberAttribute),
				environment.getProperty("groupNameAttribute", groupNameAttribute));
	}

	public EnvConfiguredLDAPCredentialValidator(Environment environment) {
		this(
				environment,

				"ldap.url",

				"ldap.user.basedn",

				"ldap.user.nameattr",

				"ldap.group.basedn",

				"ldap.group.memberattr",

				"ldap.group.nameattr");
	}
}
