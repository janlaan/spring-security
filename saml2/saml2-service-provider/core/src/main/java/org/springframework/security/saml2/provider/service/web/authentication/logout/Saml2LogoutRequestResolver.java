/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.saml2.provider.service.web.authentication.logout;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequest;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;

/**
 * Creates a signed SAML 2.0 Logout Request based on information from the
 * {@link HttpServletRequest} and current {@link Authentication}.
 *
 * The returned logout request is suitable for sending to the asserting party based on,
 * for example, the location and binding specified in
 * {@link RelyingPartyRegistration#getAssertingPartyDetails()}.
 *
 * @author Josh Cummings
 * @since 5.5
 * @see RelyingPartyRegistration
 */
public interface Saml2LogoutRequestResolver {

	/**
	 * Prepare to create, sign, and serialize a SAML 2.0 Logout Request.
	 *
	 * By default, includes a {@code NameID} based on the {@link Authentication} instance.
	 * @param request the HTTP request
	 * @param authentication the current principal details
	 * @return a builder, useful for overriding any aspects of the SAML 2.0 Logout Request
	 * that the resolver supplied
	 */
	Saml2LogoutRequestBuilder<?> resolveLogoutRequest(HttpServletRequest request, Authentication authentication);

	/**
	 * A partial application, useful for overriding any aspects of the SAML 2.0 Logout
	 * Request that the resolver supplied.
	 *
	 * The request returned from the {@link #logoutRequest()} method is signed and
	 * serialized
	 */
	interface Saml2LogoutRequestBuilder<P extends Saml2LogoutRequestBuilder<P>> {

		/**
		 * Use the given name in the SAML 2.0 Logout Request
		 * @param name the name to use
		 * @return the {@link Saml2LogoutRequestBuilder} for further customizations
		 */
		P name(String name);

		/**
		 * Use this relay state when sending the logout response
		 * @param relayState the relay state to use
		 * @return the {@link Saml2LogoutRequestBuilder} for further customizations
		 */
		P relayState(String relayState);

		/**
		 * Return a signed and serialized SAML 2.0 Logout Request and associated signed
		 * request parameters
		 * @return a signed and serialized SAML 2.0 Logout Request
		 */
		Saml2LogoutRequest logoutRequest();

	}

}
