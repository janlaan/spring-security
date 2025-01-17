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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequest;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.TestRelyingPartyRegistrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link HttpSessionLogoutRequestRepository}
 */
public class HttpSessionLogoutRequestRepositoryTests {

	private HttpSessionLogoutRequestRepository logoutRequestRepository = new HttpSessionLogoutRequestRepository();

	@Test
	public void loadLogoutRequestWhenHttpServletRequestIsNullThenThrowIllegalArgumentException() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.logoutRequestRepository.loadLogoutRequest(null));
	}

	@Test
	public void loadLogoutRequestWhenNotSavedThenReturnNull() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("RelayState", "state-1234");
		Saml2LogoutRequest logoutRequest = this.logoutRequestRepository.loadLogoutRequest(request);
		assertThat(logoutRequest).isNull();
	}

	@Test
	public void loadLogoutRequestWhenSavedThenReturnLogoutRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		Saml2LogoutRequest logoutRequest = createLogoutRequest().build();
		this.logoutRequestRepository.saveLogoutRequest(logoutRequest, request, response);
		request.addParameter("RelayState", logoutRequest.getRelayState());
		Saml2LogoutRequest loadedLogoutRequest = this.logoutRequestRepository.loadLogoutRequest(request);
		assertThat(loadedLogoutRequest).isEqualTo(logoutRequest);
	}

	// gh-5110
	@Test
	public void loadLogoutRequestWhenMultipleSavedThenReturnMatchingLogoutRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		String state1 = "state-1122";
		Saml2LogoutRequest logoutRequest1 = createLogoutRequest().relayState(state1).build();
		this.logoutRequestRepository.saveLogoutRequest(logoutRequest1, request, response);
		String state2 = "state-3344";
		Saml2LogoutRequest logoutRequest2 = createLogoutRequest().relayState(state2).build();
		this.logoutRequestRepository.saveLogoutRequest(logoutRequest2, request, response);
		String state3 = "state-5566";
		Saml2LogoutRequest logoutRequest3 = createLogoutRequest().relayState(state3).build();
		this.logoutRequestRepository.saveLogoutRequest(logoutRequest3, request, response);
		request.addParameter("RelayState", state1);
		Saml2LogoutRequest loadedLogoutRequest1 = this.logoutRequestRepository.loadLogoutRequest(request);
		assertThat(loadedLogoutRequest1).isEqualTo(logoutRequest1);
		request.removeParameter("RelayState");
		request.addParameter("RelayState", state2);
		Saml2LogoutRequest loadedLogoutRequest2 = this.logoutRequestRepository.loadLogoutRequest(request);
		assertThat(loadedLogoutRequest2).isEqualTo(logoutRequest2);
		request.removeParameter("RelayState");
		request.addParameter("RelayState", state3);
		Saml2LogoutRequest loadedLogoutRequest3 = this.logoutRequestRepository.loadLogoutRequest(request);
		assertThat(loadedLogoutRequest3).isEqualTo(logoutRequest3);
	}

	@Test
	public void loadLogoutRequestWhenSavedAndStateParameterNullThenReturnNull() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		Saml2LogoutRequest logoutRequest = createLogoutRequest().build();
		this.logoutRequestRepository.saveLogoutRequest(logoutRequest, request, new MockHttpServletResponse());
		assertThat(this.logoutRequestRepository.loadLogoutRequest(request)).isNull();
	}

	@Test
	public void saveLogoutRequestWhenHttpServletRequestIsNullThenThrowIllegalArgumentException() {
		Saml2LogoutRequest logoutRequest = createLogoutRequest().build();
		assertThatIllegalArgumentException().isThrownBy(() -> this.logoutRequestRepository
				.saveLogoutRequest(logoutRequest, null, new MockHttpServletResponse()));
	}

	@Test
	public void saveLogoutRequestWhenHttpServletResponseIsNullThenThrowIllegalArgumentException() {
		Saml2LogoutRequest logoutRequest = createLogoutRequest().build();
		assertThatIllegalArgumentException().isThrownBy(() -> this.logoutRequestRepository
				.saveLogoutRequest(logoutRequest, new MockHttpServletRequest(), null));
	}

	@Test
	public void saveLogoutRequestWhenStateNullThenThrowIllegalArgumentException() {
		Saml2LogoutRequest logoutRequest = createLogoutRequest().relayState(null).build();
		assertThatIllegalArgumentException().isThrownBy(() -> this.logoutRequestRepository
				.saveLogoutRequest(logoutRequest, new MockHttpServletRequest(), new MockHttpServletResponse()));
	}

	@Test
	public void saveLogoutRequestWhenNotNullThenSaved() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		Saml2LogoutRequest logoutRequest = createLogoutRequest().build();
		this.logoutRequestRepository.saveLogoutRequest(logoutRequest, request, new MockHttpServletResponse());
		request.addParameter("RelayState", logoutRequest.getRelayState());
		Saml2LogoutRequest loadedLogoutRequest = this.logoutRequestRepository.loadLogoutRequest(request);
		assertThat(loadedLogoutRequest).isEqualTo(logoutRequest);
	}

	@Test
	public void saveLogoutRequestWhenNoExistingSessionAndDistributedSessionThenSaved() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new MockDistributedHttpSession());
		Saml2LogoutRequest logoutRequest = createLogoutRequest().build();
		this.logoutRequestRepository.saveLogoutRequest(logoutRequest, request, new MockHttpServletResponse());
		request.addParameter("RelayState", logoutRequest.getRelayState());
		Saml2LogoutRequest loadedLogoutRequest = this.logoutRequestRepository.loadLogoutRequest(request);
		assertThat(loadedLogoutRequest).isEqualTo(logoutRequest);
	}

	@Test
	public void saveLogoutRequestWhenExistingSessionAndDistributedSessionThenSaved() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new MockDistributedHttpSession());
		Saml2LogoutRequest logoutRequest1 = createLogoutRequest().build();
		this.logoutRequestRepository.saveLogoutRequest(logoutRequest1, request, new MockHttpServletResponse());
		Saml2LogoutRequest logoutRequest2 = createLogoutRequest().build();
		this.logoutRequestRepository.saveLogoutRequest(logoutRequest2, request, new MockHttpServletResponse());
		request.addParameter("RelayState", logoutRequest2.getRelayState());
		Saml2LogoutRequest loadedLogoutRequest = this.logoutRequestRepository.loadLogoutRequest(request);
		assertThat(loadedLogoutRequest).isEqualTo(logoutRequest2);
	}

	@Test
	public void saveLogoutRequestWhenNullThenRemoved() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		Saml2LogoutRequest logoutRequest = createLogoutRequest().build();
		this.logoutRequestRepository.saveLogoutRequest(logoutRequest, request, response);
		request.addParameter("RelayState", logoutRequest.getRelayState());
		this.logoutRequestRepository.saveLogoutRequest(null, request, response);
		Saml2LogoutRequest loadedLogoutRequest = this.logoutRequestRepository.loadLogoutRequest(request);
		assertThat(loadedLogoutRequest).isNull();
	}

	@Test
	public void removeLogoutRequestWhenHttpServletRequestIsNullThenThrowIllegalArgumentException() {
		assertThatIllegalArgumentException().isThrownBy(
				() -> this.logoutRequestRepository.removeLogoutRequest(null, new MockHttpServletResponse()));
	}

	@Test
	public void removeLogoutRequestWhenHttpServletResponseIsNullThenThrowIllegalArgumentException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> this.logoutRequestRepository.removeLogoutRequest(new MockHttpServletRequest(), null));
	}

	@Test
	public void removeLogoutRequestWhenSavedThenRemoved() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		Saml2LogoutRequest logoutRequest = createLogoutRequest().build();
		this.logoutRequestRepository.saveLogoutRequest(logoutRequest, request, response);
		request.addParameter("RelayState", logoutRequest.getRelayState());
		Saml2LogoutRequest removedLogoutRequest = this.logoutRequestRepository.removeLogoutRequest(request, response);
		Saml2LogoutRequest loadedLogoutRequest = this.logoutRequestRepository.loadLogoutRequest(request);
		assertThat(removedLogoutRequest).isNotNull();
		assertThat(loadedLogoutRequest).isNull();
	}

	// gh-5263
	@Test
	public void removeLogoutRequestWhenSavedThenRemovedFromSession() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		Saml2LogoutRequest logoutRequest = createLogoutRequest().build();
		this.logoutRequestRepository.saveLogoutRequest(logoutRequest, request, response);
		request.addParameter("RelayState", logoutRequest.getRelayState());
		Saml2LogoutRequest removedLogoutRequest = this.logoutRequestRepository.removeLogoutRequest(request, response);
		String sessionAttributeName = HttpSessionLogoutRequestRepository.class.getName() + ".AUTHORIZATION_REQUEST";
		assertThat(removedLogoutRequest).isNotNull();
		assertThat(request.getSession().getAttribute(sessionAttributeName)).isNull();
	}

	@Test
	public void removeLogoutRequestWhenNotSavedThenNotRemoved() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("RelayState", "state-1234");
		MockHttpServletResponse response = new MockHttpServletResponse();
		Saml2LogoutRequest removedLogoutRequest = this.logoutRequestRepository.removeLogoutRequest(request, response);
		assertThat(removedLogoutRequest).isNull();
	}

	private Saml2LogoutRequest.Builder createLogoutRequest() {
		RelyingPartyRegistration registration = TestRelyingPartyRegistrations.full().build();
		return Saml2LogoutRequest.withRelyingPartyRegistration(registration).samlRequest("request").id("id")
				.parameters((params) -> params.put("RelayState", "state-1234"));
	}

	static class MockDistributedHttpSession extends MockHttpSession {

		@Override
		public Object getAttribute(String name) {
			return wrap(super.getAttribute(name));
		}

		@Override
		public void setAttribute(String name, Object value) {
			super.setAttribute(name, wrap(value));
		}

		private Object wrap(Object object) {
			if (object instanceof Map) {
				object = new HashMap<>((Map<Object, Object>) object);
			}
			return object;
		}

	}

}
