package com.digitoll.erp.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import com.digitoll.commons.model.Role;
import com.digitoll.erp.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(secure = false)
@ContextConfiguration(classes = { RoleController.class })
public class RoleControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper mapper;

	@MockBean
	RoleService service;

	@Test
	public void testCreateRoleSuccess() throws Exception {

		final Role mockRole = new Role();

		mockRole.setId("1266");
		mockRole.setCode("A65612");
		mockRole.setName("Product Manager");

		Mockito.when(service.createRole(Mockito.refEq(mockRole))).thenReturn(mockRole);

		mockMvc.perform(post("/role").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(mockRole))).andExpect(jsonPath("$.id").value(mockRole.getId()))
				.andExpect(jsonPath("$.name").value(mockRole.getName()))
				.andExpect(jsonPath("$.code").value(mockRole.getCode()));

	}

	@Test
	public void testGetAllRolesSuccess() throws Exception {
		List<Role> roles = Arrays.asList(new Role());

		Mockito.when(service.getAllRoles()).thenReturn(roles);

		mockMvc.perform(get("/roles")).andExpect(jsonPath("$.[0].id").value(roles.get(0).getId()))
				.andExpect(jsonPath("$.[0].code").value(roles.get(0).getCode())).andExpect(status().isOk());
	}

	@Test
	public void testGetAllRolesEmptySuccess() throws Exception {
		List<Role> roles = new ArrayList<Role>();
		

		Mockito.when(service.getAllRoles()).thenReturn(roles);

		mockMvc.perform(get("/roles").contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(content().string("[]"))
		.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	public void testCreateRoleFail() throws Exception {

		mockMvc.perform(post("/role").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is4xxClientError());
	}

}
